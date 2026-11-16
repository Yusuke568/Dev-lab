package com.example.auth.domain.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ログインの総当たりを防ぐため、失敗の回数を数えて一時的にログインを止める。
 * <ul>
 *   <li>同じユーザー名で {@link #MAX_FAILURES_PER_ACCOUNT} 回続けて失敗したら、そのユーザー名を {@link #LOCK} の間止める</li>
 *   <li>同じ接続元から {@link #MAX_FAILURES_PER_ADDRESS} 回失敗したら、その接続元を {@link #LOCK} の間止める
 *       （ユーザー名を変えながら試すやり方を防ぐ）</li>
 * </ul>
 * 失敗は {@link #WINDOW} の間だけ数え、過ぎたら0から数え直す。止めている間は、正しいパスワードでもログインさせない。
 * 存在しないユーザー名も同じように数えるので、止まったかどうかで社員IDの有無は分からない。
 * 回数はメモリに持つ（サーバーを再起動すると消える）。管理者は社員一覧の画面からロックを解除できる。
 */
public class LoginAttemptLimiter {

    public static final int MAX_FAILURES_PER_ACCOUNT = 5;
    public static final int MAX_FAILURES_PER_ADDRESS = 20;
    public static final Duration WINDOW = Duration.ofMinutes(15);
    public static final Duration LOCK = Duration.ofMinutes(15);

    /** 記録の数がこれを超えたら、期限の過ぎた記録を捨てる（大量のユーザー名でメモリを使わせない） */
    private static final int PURGE_THRESHOLD = 10_000;
    /** 長いユーザー名を送られてもメモリを使い過ぎないよう、キーはこの長さで切る */
    private static final int MAX_KEY_LENGTH = 64;

    private final Clock clock;
    private final Map<String, Counter> accounts = new HashMap<>();
    private final Map<String, Counter> addresses = new HashMap<>();

    public LoginAttemptLimiter(Clock clock) {
        this.clock = clock;
    }

    /**
     * ログインを止めているなら、解除までの残り時間を返す。止めていなければ空。
     */
    public synchronized Optional<Duration> lockedFor(String username, String address) {
        Instant now = clock.instant();
        Duration account = remaining(accounts.get(key(username)), now);
        Duration addr = remaining(addresses.get(key(address)), now);
        Duration longer = account.compareTo(addr) >= 0 ? account : addr;
        return longer.isZero() ? Optional.empty() : Optional.of(longer);
    }

    /**
     * 失敗を記録する。この失敗でログインを止めることになったら true を返す。
     */
    public synchronized boolean recordFailure(String username, String address) {
        Instant now = clock.instant();
        purgeIfLarge(now);
        boolean accountLocked = count(accounts, key(username), MAX_FAILURES_PER_ACCOUNT, now);
        boolean addressLocked = count(addresses, key(address), MAX_FAILURES_PER_ADDRESS, now);
        return accountLocked || addressLocked;
    }

    /**
     * ログインできたら、そのユーザー名の失敗の回数を消す。
     * 接続元の回数は消さない（自分のアカウントでログインして、ほかの人への総当たりの回数を消せないように）。
     */
    public synchronized void recordSuccess(String username) {
        accounts.remove(key(username));
    }

    /**
     * いまログインを止めているユーザー名と接続元の一覧（管理者の画面用）。解除までの残りが長い順。
     */
    public synchronized List<Lock> currentLocks() {
        Instant now = clock.instant();
        List<Lock> locks = new ArrayList<>();
        collect(accounts, Lock.Kind.ACCOUNT, now, locks);
        collect(addresses, Lock.Kind.ADDRESS, now, locks);
        locks.sort(Comparator.comparing(Lock::getRemaining).reversed());
        return locks;
    }

    /**
     * 管理者がユーザー名のロックを解除する。失敗の回数も消す。止めていなかったら false。
     */
    public synchronized boolean unlockAccount(String username) {
        return unlock(accounts, key(username));
    }

    /**
     * 管理者が接続元のロックを解除する。失敗の回数も消す。止めていなかったら false。
     */
    public synchronized boolean unlockAddress(String address) {
        return unlock(addresses, key(address));
    }

    private boolean unlock(Map<String, Counter> map, String key) {
        boolean wasLocked = !remaining(map.get(key), clock.instant()).isZero();
        map.remove(key);
        return wasLocked;
    }

    private static void collect(Map<String, Counter> map, Lock.Kind kind, Instant now, List<Lock> out) {
        map.forEach((key, counter) -> {
            Duration left = remaining(counter, now);
            if (!left.isZero()) {
                out.add(new Lock(kind, key, left));
            }
        });
    }

    /** 止めているユーザー名か接続元 */
    public static final class Lock {
        public enum Kind { ACCOUNT, ADDRESS }

        private final Kind kind;
        private final String value;
        private final Duration remaining;

        Lock(Kind kind, String value, Duration remaining) {
            this.kind = kind;
            this.value = value;
            this.remaining = remaining;
        }

        public Kind getKind() {
            return kind;
        }

        /** JSP 用（ACCOUNT / ADDRESS） */
        public String getKindName() {
            return kind.name();
        }

        public String getValue() {
            return value;
        }

        public Duration getRemaining() {
            return remaining;
        }

        /** 解除までの残り（分、切り上げ） */
        public long getRemainingMinutes() {
            return Math.max(1, (remaining.getSeconds() + 59) / 60);
        }
    }

    private boolean count(Map<String, Counter> map, String key, int max, Instant now) {
        Counter counter = map.get(key);
        if (counter == null || counter.isExpired(now)) {
            counter = new Counter(now);
            map.put(key, counter);
        }
        counter.failures++;
        if (counter.lockedUntil == null && counter.failures >= max) {
            counter.lockedUntil = now.plus(LOCK);
            return true;
        }
        return false;
    }

    private static Duration remaining(Counter counter, Instant now) {
        if (counter == null || counter.lockedUntil == null || !now.isBefore(counter.lockedUntil)) {
            return Duration.ZERO;
        }
        return Duration.between(now, counter.lockedUntil);
    }

    private void purgeIfLarge(Instant now) {
        if (accounts.size() + addresses.size() > PURGE_THRESHOLD) {
            accounts.values().removeIf(c -> c.isExpired(now));
            addresses.values().removeIf(c -> c.isExpired(now));
        }
    }

    private static String key(String value) {
        String v = value == null ? "" : value.trim();
        return v.length() > MAX_KEY_LENGTH ? v.substring(0, MAX_KEY_LENGTH) : v;
    }

    private static final class Counter {
        private final Instant windowStart;
        private int failures;
        private Instant lockedUntil;

        Counter(Instant windowStart) {
            this.windowStart = windowStart;
        }

        /** 止めていた時間が過ぎたか、止めずに数える期間が過ぎたら、0から数え直す */
        boolean isExpired(Instant now) {
            if (lockedUntil != null) {
                return !now.isBefore(lockedUntil);
            }
            return !now.isBefore(windowStart.plus(WINDOW));
        }
    }
}
