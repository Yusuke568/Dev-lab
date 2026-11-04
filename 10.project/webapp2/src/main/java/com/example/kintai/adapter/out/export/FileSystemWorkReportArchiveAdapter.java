package com.example.kintai.adapter.out.export;

import com.example.kintai.domain.port.out.WorkReportArchivePort;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * 作業報告書を、サーバーのフォルダに置く。
 *
 * 置き場所：{基準フォルダ}/{年}/{月}/作業報告書_{年月}_{社員ID}_{氏名}.xlsm
 * 同じファイルがあれば {基準フォルダ}/{年}/{月}/_backup/ に日時付きで移してから置く。
 *
 * 基準フォルダは、Tomcat の起動オプション -Dkintai.workReport.archiveDir=... か、
 * 環境変数 KINTAI_WORK_REPORT_DIR で指定する。
 * どちらもなければ /srv/kintai/work-reports を使う（ただし、そのフォルダがあるときだけ置く）。
 */
public class FileSystemWorkReportArchiveAdapter implements WorkReportArchivePort {

    public static final String SYSTEM_PROPERTY = "kintai.workReport.archiveDir";
    public static final String ENVIRONMENT_VARIABLE = "KINTAI_WORK_REPORT_DIR";
    /** 設定がないときの置き場所（Tomcat の設定をしなくても、このフォルダを作れば置かれる） */
    public static final Path DEFAULT_DIR = Path.of("/srv/kintai/work-reports");

    private static final DateTimeFormatter BACKUP_SUFFIX = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    /** 共有フォルダの利用者（Tomcat 以外のユーザー）も編集できるよう、グループに書き込みを許す */
    private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS = PosixFilePermissions.fromString("rwxrwxr-x");
    private static final Set<PosixFilePermission> FILE_PERMISSIONS = PosixFilePermissions.fromString("rw-rw-r--");

    private final Path baseDir;
    private final Clock clock;
    /** true なら、基準フォルダがあるときだけ置く（設定がなく既定のフォルダを使うとき） */
    private final boolean onlyIfExists;

    public FileSystemWorkReportArchiveAdapter(Path baseDir, Clock clock) {
        this(baseDir, clock, false);
    }

    FileSystemWorkReportArchiveAdapter(Path baseDir, Clock clock, boolean onlyIfExists) {
        this.baseDir = baseDir;
        this.clock = clock;
        this.onlyIfExists = onlyIfExists;
    }

    /**
     * 起動オプションか環境変数から基準フォルダを読む。どちらもなければ既定のフォルダ（あるときだけ）を使う。
     * どこに置くかを Tomcat のログ（標準出力）に出す。
     */
    public static FileSystemWorkReportArchiveAdapter fromSystemSettings() {
        String dir = System.getProperty(SYSTEM_PROPERTY);
        if (dir == null || dir.isBlank()) {
            dir = System.getenv(ENVIRONMENT_VARIABLE);
        }
        FileSystemWorkReportArchiveAdapter adapter = dir == null || dir.isBlank()
                ? new FileSystemWorkReportArchiveAdapter(DEFAULT_DIR, Clock.systemDefaultZone(), true)
                : new FileSystemWorkReportArchiveAdapter(Path.of(dir.trim()), Clock.systemDefaultZone(), false);
        System.out.println("[kintai] " + adapter.describe());
        return adapter;
    }

    /** 設定の状態の説明（起動時のログ用）。 */
    String describe() {
        if (baseDir == null) {
            return "作業報告書のサーバー保存: しない（置き場所の設定なし）";
        }
        if (onlyIfExists) {
            return "作業報告書のサーバー保存: " + baseDir + "（設定なしのため既定のフォルダ。フォルダがあるときだけ置く。現在"
                    + (Files.isDirectory(baseDir) ? "あり）" : "なし）");
        }
        return "作業報告書のサーバー保存: " + baseDir;
    }

    @Override
    public boolean isEnabled() {
        if (baseDir == null) {
            return false;
        }
        // 既定のフォルダは、作ってあるときだけ使う（Tomcat を起動した後に作っても使える）
        return !onlyIfExists || Files.isDirectory(baseDir);
    }

    @Override
    public String archive(String staffId, String staffName, YearMonth yearMonth, byte[] content) throws IOException {
        if (!isEnabled()) {
            throw new IllegalStateException("作業報告書の置き場所が設定されていません。");
        }
        Path monthDir = baseDir.resolve(String.valueOf(yearMonth.getYear()))
                .resolve(String.format("%02d", yearMonth.getMonthValue()));
        createDirectories(monthDir);

        String baseName = String.format("作業報告書_%d%02d_%s_%s", yearMonth.getYear(), yearMonth.getMonthValue(),
                safe(staffId), safe(staffName));
        Path target = monthDir.resolve(baseName + ".xlsm");

        if (Files.exists(target)) {
            Path backupDir = monthDir.resolve("_backup");
            createDirectories(backupDir);
            String suffix = LocalDateTime.now(clock).format(BACKUP_SUFFIX);
            Files.move(target, backupDir.resolve(baseName + "_" + suffix + ".xlsm"), StandardCopyOption.REPLACE_EXISTING);
        }

        // 書き込み途中のファイルを共有フォルダの利用者が開かないよう、一時ファイルに書いてから置き換える
        Path temp = Files.createTempFile(monthDir, ".tmp_" + baseName, ".xlsm");
        try {
            Files.write(temp, content);
            setPermissions(temp, FILE_PERMISSIONS);
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
        return target.toString();
    }

    private void createDirectories(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            Files.createDirectories(dir);
            setPermissions(dir, DIRECTORY_PERMISSIONS);
        }
    }

    /** Linux では共有用の権限を付ける（Windows など POSIX 権限のない環境では何もしない）。 */
    private static void setPermissions(Path path, Set<PosixFilePermission> permissions) {
        try {
            Files.setPosixFilePermissions(path, permissions);
        } catch (UnsupportedOperationException | IOException ignored) {
            // 権限を変えられなくても、置くこと自体は続ける
        }
    }

    /** ファイル名に使えない文字や空白を「_」にする。 */
    static String safe(String value) {
        if (value == null || value.isBlank()) {
            return "_";
        }
        return value.trim().replaceAll("[\\\\/:*?\"<>|\\s\\u3000]+", "_");
    }
}
