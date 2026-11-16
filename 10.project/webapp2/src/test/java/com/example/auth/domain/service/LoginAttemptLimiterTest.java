package com.example.auth.domain.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptLimiterTest {

    /** 進められる時計 */
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-11-14T09:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private final MutableClock clock = new MutableClock();
    private final LoginAttemptLimiter limiter = new LoginAttemptLimiter(clock);

    private void fail(String user, String address, int times) {
        for (int i = 0; i < times; i++) {
            limiter.recordFailure(user, address);
        }
    }

    @Test
    void 失敗が上限より少なければ止めない() {
        fail("126", "10.0.0.1", LoginAttemptLimiter.MAX_FAILURES_PER_ACCOUNT - 1);

        assertTrue(limiter.lockedFor("126", "10.0.0.1").isEmpty());
    }

    @Test
    void 同じユーザー名で続けて失敗したら止める() {
        fail("126", "10.0.0.1", LoginAttemptLimiter.MAX_FAILURES_PER_ACCOUNT - 1);

        assertTrue(limiter.recordFailure("126", "10.0.0.1"), "上限に達した失敗で止まる");
        assertEquals(LoginAttemptLimiter.LOCK, limiter.lockedFor("126", "10.0.0.1").orElseThrow());
    }

    @Test
    void ユーザー名で止めたら別の接続元からも入れない() {
        fail("126", "10.0.0.1", LoginAttemptLimiter.MAX_FAILURES_PER_ACCOUNT);

        assertTrue(limiter.lockedFor("126", "10.0.0.99").isPresent());
        assertTrue(limiter.lockedFor("127", "10.0.0.99").isEmpty(), "ほかのユーザー名は止めない");
    }

    @Test
    void 止めた時間が過ぎたら入れて回数も数え直す() {
        fail("126", "10.0.0.1", LoginAttemptLimiter.MAX_FAILURES_PER_ACCOUNT);
        clock.advance(LoginAttemptLimiter.LOCK.minusSeconds(1));
        assertTrue(limiter.lockedFor("126", "10.0.0.1").isPresent());

        clock.advance(Duration.ofSeconds(1));
        assertTrue(limiter.lockedFor("126", "10.0.0.1").isEmpty());

        assertFalse(limiter.recordFailure("126", "10.0.0.1"), "解除後の1回目の失敗ではまだ止めない");
        assertTrue(limiter.lockedFor("126", "10.0.0.1").isEmpty());
    }

    @Test
    void 数える期間を過ぎた失敗は数えない() {
        fail("126", "10.0.0.1", LoginAttemptLimiter.MAX_FAILURES_PER_ACCOUNT - 1);
        clock.advance(LoginAttemptLimiter.WINDOW);

        fail("126", "10.0.0.1", LoginAttemptLimiter.MAX_FAILURES_PER_ACCOUNT - 1);

        assertTrue(limiter.lockedFor("126", "10.0.0.1").isEmpty());
    }

    @Test
    void ログインできたらそのユーザー名の回数を消す() {
        fail("126", "10.0.0.1", LoginAttemptLimiter.MAX_FAILURES_PER_ACCOUNT - 1);
        limiter.recordSuccess("126");

        fail("126", "10.0.0.1", LoginAttemptLimiter.MAX_FAILURES_PER_ACCOUNT - 1);

        assertTrue(limiter.lockedFor("126", "10.0.0.1").isEmpty());
    }

    @Test
    void ユーザー名を変えながら試す接続元を止める() {
        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES_PER_ADDRESS; i++) {
            limiter.recordFailure(String.valueOf(100 + i), "10.0.0.66");
        }

        assertTrue(limiter.lockedFor("999", "10.0.0.66").isPresent(), "この接続元からは、どのユーザー名でも入れない");
        assertTrue(limiter.lockedFor("999", "10.0.0.1").isEmpty(), "ほかの接続元は止めない");
    }

    @Test
    void 自分のアカウントでログインしても接続元の回数は消えない() {
        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES_PER_ADDRESS - 1; i++) {
            limiter.recordFailure(String.valueOf(100 + i), "10.0.0.66");
        }
        limiter.recordSuccess("500");

        assertTrue(limiter.recordFailure("200", "10.0.0.66"));
    }

    @Test
    void 止めているユーザー名と接続元を一覧にする() {
        fail("126", "10.0.0.1", LoginAttemptLimiter.MAX_FAILURES_PER_ACCOUNT);
        fail("127", "10.0.0.2", 1); // 止めていない
        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES_PER_ADDRESS; i++) {
            limiter.recordFailure(String.valueOf(300 + i), "10.0.0.66");
        }

        java.util.List<LoginAttemptLimiter.Lock> locks = limiter.currentLocks();

        assertTrue(locks.stream().anyMatch(l -> l.getKind() == LoginAttemptLimiter.Lock.Kind.ACCOUNT && l.getValue().equals("126")));
        assertTrue(locks.stream().anyMatch(l -> l.getKind() == LoginAttemptLimiter.Lock.Kind.ADDRESS && l.getValue().equals("10.0.0.66")));
        assertTrue(locks.stream().noneMatch(l -> l.getValue().equals("127")), "止めていないものは出さない");
        assertEquals(15, locks.get(0).getRemainingMinutes());
    }

    @Test
    void 管理者がユーザー名のロックを解除したらすぐ入れて回数も数え直す() {
        fail("126", "10.0.0.1", LoginAttemptLimiter.MAX_FAILURES_PER_ACCOUNT);

        assertTrue(limiter.unlockAccount("126"));

        assertTrue(limiter.lockedFor("126", "10.0.0.2").isEmpty());
        assertFalse(limiter.recordFailure("126", "10.0.0.2"), "解除後の1回目の失敗ではまだ止めない");
        assertFalse(limiter.unlockAccount("126"), "止めていなければ false");
    }

    @Test
    void 管理者が接続元のロックを解除できる() {
        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES_PER_ADDRESS; i++) {
            limiter.recordFailure(String.valueOf(100 + i), "10.0.0.66");
        }

        assertTrue(limiter.unlockAddress("10.0.0.66"));

        assertTrue(limiter.lockedFor("999", "10.0.0.66").isEmpty());
        assertTrue(limiter.currentLocks().isEmpty());
    }

    @Test
    void 存在しないユーザー名も同じように止める() {
        fail("no-such-user", "10.0.0.1", LoginAttemptLimiter.MAX_FAILURES_PER_ACCOUNT);

        assertTrue(limiter.lockedFor("no-such-user", "10.0.0.2").isPresent());
    }
}
