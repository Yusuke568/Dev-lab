# リファクタリング提案：新アーキテクチャ設計

## フェーズ1：現状分析と新アーキテクチャの提案

現状は、古典的なServlet/JSPアーキテクチャから、責務分離を目指したレイヤードアーキテクチャへの過渡期にあると見受けられます。この移行は正しい方向性ですが、評価基準である「保守性、拡張性、テスト容易性」を最高レベルで満たすため、より洗練された設計思想を導入します。

### 1. 採用すべき設計思想・アーキテクチャパターン

**クリーンアーキテクチャ**の原則を取り入れた**ヘキサゴナルアーキテクチャ（別名：ポートとアダプタ）**を強く推奨します。

*   **採用理由:**
    1.  **ビジネスロジックの純粋性と独立性:** このアーキテクチャの核心は、アプリケーションの心臓部である「ドメインロジック」を、UI、データベース、外部APIといった外部要因（インフラストラクチャ）から完全に隔離することにあります。これにより、ビジネスルールが技術的詳細に汚染されず、純粋なJavaオブジェクトとして表現できます。
    2.  **依存関係の逆転:** 従来の上から下への依存（Controller → Service → DAO）ではなく、「内側は外側を知らない」というルールを徹底します。アプリケーションの中心（ドメイン）は、外部（インフラ）が実装すべき**インターフェース（ポート）**を定義するだけです。これにより、データベースをJDBCからJPAへ、WebフレームワークをJSPからReact/Vue.jsへ変更する際も、ビジネスロジックへの影響を最小限に抑えられます。
    3.  **最高のテスト容易性:** ドメインロジックは外部依存がないため、単体テストが極めて容易になります。アプリケーションのユースケース層も、インターフェース（ポート）に対してテストを書くことで、実際のデータベースなしで振る舞いを検証できます。
    4.  **明確な責務分離:** 「何をするか（ドメイン）」と「どうやって実現するか（インフラ）」が明確に分離されるため、コードの可読性が飛躍的に向上し、複数人での開発もスムーズに進みます。

### 2. 新しいディレクトリ構造の提案

上記の設計思想を、Mavenの標準ディレクトリ構造に落とし込みます。パッケージ名をドメインの境界と責務に応じて設計します。

```
src/main/java/com/example/kintai/
├── domain
│   ├── model          // ■ ドメインモデル（ビジネスの核となるオブジェクト）
│   │   ├── employee
│   │   │   ├── Employee.java          // 社員エンティティ
│   │   │   └── EmployeeId.java        // 値オブジェクト
│   │   ├── attendance
│   │   │   ├── AttendanceRecord.java  // 勤怠記録エンティティ
│   │   │   └── WorkTime.java          // 勤務時間（値オブジェクト）
│   │   └── leave
│   │       └── PaidLeave.java         // 有給休暇エンティティ
│   │
│   └── port.out       // ■ 外向きポート（ドメインが外部に要求するIF）
│       ├── LoadEmployeePort.java      // 社員情報を読み込むIF
│       ├── SaveEmployeePort.java      // 社員情報を保存するIF
│       └── LoadAttendanceRecordPort.java
│
├── application
│   ├── port.in        // ■ 内向きポート（外部がドメインに要求するIF）
│   │   ├── RecordAttendanceUseCase.java    // 勤怠を記録するユースケース
│   │   └── GetMonthlyAttendanceUseCase.java// 月次勤怠を取得するユースケース
│   │
│   └── service        // ■ ユースケースの実装
│       ├── RecordAttendanceService.java    // 上記ユースケースの実装クラス
│       └── GetMonthlyAttendanceService.java
│
├── adapter
│   ├── in             // ■ 内向きアダプタ（外部からの入力を受け付ける）
│   │   └── web        // Webからの入力を処理するアダプタ
│   │       ├── FrontController.java
│   │       ├── Action.java
│   │       ├── KintaiDisplayAction.java
│   │       └── dto                      // DTO (Data Transfer Object)
│   │           └── MonthlyAttendanceDto.java
│   │
│   └── out            // ■ 外向きアダプタ（外部への出力を処理する）
│       └── persistence // 永続化を担当するアダプタ
│           ├── EmployeePersistenceAdapter.java // Repositoryポートの実装
│           └── AttendancePersistenceAdapter.java
│
└── configuration      // ■ 設定・DIコンテナ
    └── DependencyFactory.java // 各クラスの依存関係を解決・注入する
```

*   **各層の役割:**
    *   `domain`: システムの中心。ビジネスルールとデータ構造を定義します。外部ライブラリへの依存は最小限に抑えます。
    *   `application`: アプリケーション固有のユースケース（ユーザーが何をしたいか）を定義し、実装します。ドメインモデルを操作してタスクを実行します。
    *   `adapter`: 外部の世界とアプリケーションをつなぐ「変換器」です。Webコントローラー、DB永続化処理、外部APIクライアントなどがここに属します。
    *   `configuration`: アプリケーション全体の起動設定や、各コンポーネントの依存関係を構築・注入（Dependency Injection）する役割を担います。

### 3. 移行にあたっての主要な変更点とメリット

1.  **ポートとアダプタの導入**
    *   **変更点:** `Service`が直接`DAO`を呼ぶのではなく、`UseCase`（ポート）を`Service`が実装し、`Repository`（ポート）を`PersistenceAdapter`が実装する形にします。`Service`は`Repository`の**インターフェースのみ**に依存します。
    *   **メリット:** **依存性の逆転**が実現され、ビジネスロジック（`application`, `domain`）がインフラ（`adapter`）から完全に独立します。DBをPostgreSQLからMySQLに変えても、ビジネスロジックは一切変更不要です。

2.  **UseCase層の明確化**
    *   **変更点:** `〇〇Service`という曖昧な名前ではなく、「ユーザーが何を実現したいか」を明確に示す`XxxUseCase`というインターフェースを導入します。（例: `社員を登録する` → `RegisterEmployeeUseCase`）
    *   **メリット:** アプリケーションの機能一覧が`application/port/in`を見るだけで把握でき、システムの目的が明確になります。

3.  **アダプタ層による関心の分離**
    *   **変更点:** Webリクエストの処理、DBとのやり取りといった技術的詳細はすべて`adapter`パッケージに隔離します。`FrontController`や`Action`は`adapter.in.web`へ、DBアクセス実装は`adapter.out.persistence`へ配置します。
    *   **メリット:** 技術的な関心事が一箇所にまとまることで、ビジネスロジックとインフラのコードが混ざり合うのを防ぎ、それぞれの専門家が責任を持って開発しやすくなります。

4.  **手動DIによる依存性注入**
    *   **変更点:** フレームワークを使わないため、`DependencyFactory`のようなクラスを用意し、アプリケーション起動時に`Service`に`PersistenceAdapter`を注入するなど、依存関係の解決を手動で行います。
    *   **メリット:** DIフレームワークの学習コストなしに、DIの恩恵（疎結合、テスト容易性）を享受できます。クラス間の関係性がコードで明示されるため、システムの構造を理解しやすくなります。

---
## フェーズ2：コアモジュールのインターフェース設計

フェーズ1で合意したアーキテクチャに基づき、システムの骨格となる主要なインターフェースとデータモデルを設計します。ここでは「勤怠」と「社員」ドメインを中心に定義します。

### 1. システム全体の連携フロー

主要なユースケースである「月次勤怠情報の取得」を例に、各コンポーネントがどのように連携するかを示します。

```
[Webブラウザ]
      │ 1. リクエスト (GET /kintai?year=2023&month=10)
      v
[adapter.in.web.KintaiDisplayAction]
      │ 2. UseCaseのコマンドを作成し、呼び出す
      v
[application.port.in.GetMonthlyAttendanceUseCase] (IF)
      │ 3. (実装クラスであるServiceが呼ばれる)
      v
[application.service.GetMonthlyAttendanceService]
      │ 4. port.outを呼び出して永続化層からデータを取得
      v
[domain.port.out.LoadAttendanceRecordPort] (IF)
      │ 5. (実装クラスであるAdapterが呼ばれる)
      v
[adapter.out.persistence.AttendancePersistenceAdapter]
      │ 6. データベースにSQLを発行してデータを取得
      v
[ドメインモデル: AttendanceRecord]
      │ 7. 取得したデータをドメインモデルに変換して返す
      v
[application.service.GetMonthlyAttendanceService]
      │ 8. ドメインモデルからDTOを作成して返す
      v
[adapter.in.web.KintaiDisplayAction]
      │ 9. DTOをView(JSP)が扱いやすいViewModelに変換
      v
[JSP]
      │ 10. ViewModelの情報を画面に表示
      v
[Webブラウザ]
```

### 2. ドメインモデル (`domain.model`)

ビジネスの核心となるオブジェクトです。setterは持たず、コンストラクタで初期化し、状態が不変（Immutable）であることを目指します。

```java
// domain/model/employee/EmployeeId.java
/**
 * 社員ID（値オブジェクト）
 */
public record EmployeeId(String value) {
    public EmployeeId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("EmployeeId cannot be blank.");
        }
    }
}

// domain/model/employee/Employee.java
/**
 * 社員（エンティティ）
 */
public class Employee {
    private final EmployeeId id;
    private String name;
    // ... 他の社員情報

    public Employee(EmployeeId id, String name) {
        this.id = id;
        this.name = name;
    }
    // getName(), getId() 등의 getterのみ
}

// domain/model/attendance/AttendanceRecord.java
/**
 * 特定の日付の勤怠記録（エンティティ）
 */
public class AttendanceRecord {
    private final EmployeeId employeeId;
    private final LocalDate workDate;
    private WorkTime workTime;
    // ... 休憩時間, 作業内容など

    public AttendanceRecord(EmployeeId employeeId, LocalDate workDate, WorkTime workTime) {
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.workTime = workTime;
    }

    /**
     * 勤務時間を計算する
     * @return 勤務時間
     */
    public Duration calculateWorkDuration() {
        return workTime != null ? workTime.getDuration() : Duration.ZERO;
    }
}
```

### 3. ポート (インターフェース)

#### 内向きポート (UseCases)

外部（Webアダプタなど）がアプリケーションに何を要求できるかを定義します。

```java
// application/port/in/GetMonthlyAttendanceUseCase.java
/**
 * 月次勤怠情報を取得するユースケース
 */
public interface GetMonthlyAttendanceUseCase {

    MonthlyAttendanceDto getMonthlyAttendance(GetMonthlyAttendanceCommand command);

    /**
     * ユースケースへの入力コマンド
     */
    record GetMonthlyAttendanceCommand(EmployeeId employeeId, YearMonth yearMonth) {}
}
```

#### 外向きポート (Repositories)

アプリケーションが外部（永続化アダプタなど）に何を要求するかを定義します。

```java
// domain/port/out/LoadAttendanceRecordPort.java
/**
 * 勤怠記録を読み込むためのポート
 */
public interface LoadAttendanceRecordPort {
    List<AttendanceRecord> loadByEmployeeAndMonth(EmployeeId employeeId, YearMonth yearMonth);
}

// domain/port/out/SaveAttendanceRecordPort.java
/**
 * 勤怠記録を保存するためのポート
 */
public interface SaveAttendanceRecordPort {
    void save(AttendanceRecord record);
}
```

### 4. DTO (Data Transfer Object)

ユースケースがアダプタに返すための、ドメイン知識を含まない単純なデータコンテナです。

```java
// adapter/in/web/dto/MonthlyAttendanceDto.java
/**
 * 月次勤怠情報を表現するDTO
 */
public class MonthlyAttendanceDto {
    private final YearMonth yearMonth;
    private final List<DailyAttendanceDto> dailyRecords;
    private final String totalWorkHours; // 例: "160.5h"

    // Constructor, Getters
}

// adapter/in/web/dto/DailyAttendanceDto.java
/**
 * 日次勤怠情報を表現するDTO
 */
public class DailyAttendanceDto {
    private final LocalDate date;
    private final String startTime;  // 例: "09:00"
    private final String endTime;    // 例: "18:00"
    private final String workHours;  // 例: "8.0h"

    // Constructor, Getters
}
```
