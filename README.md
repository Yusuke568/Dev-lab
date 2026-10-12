# Dev-lab: SES勤怠管理・勤務報告書システム

本リポジトリは、Java 11 (Servlet 4.0 / JSP / JSTL) と MySQL 8.0 によるSES勤怠管理・勤務報告書Webアプリケーションです。

肥大化しやすいMVC（Fat Controller / Fat Service）の課題を解消し、**「ヘキサゴナル／クリーンアーキテクチャ（Ports & Adapters）」** および **「Front Controller + Command パターン」** を採用しています。

---

## 📚 ドキュメント一覧 (`00.document/`)

| ドキュメント | 概要 |
| :--- | :--- |
| [00_README](00.document/00_README.md) | 要件定義書インデックス・全体概要 |
| [01_システム概要書](00.document/01_システム概要書.md) | システム目的、対象業務、新機能スコープ |
| [02_機能要件定義書](00.document/02_機能要件定義書.md) | 社員管理、SES勤怠、精算計算、週報、有給管理の機能仕様 |
| [03_非機能要件定義書](00.document/03_非機能要件定義書.md) | 性能、セキュリティ、MySQL/Tomcat実行環境要件 |
| [04_画面設計書](00.document/04_画面設計書.md) | Tailwind CSSによるモダンUI・各画面レイアウト仕様 |
| [05_データ定義書](00.document/05_データ定義書.md) | 全テーブル定義（基本テーブル + SES拡張テーブル） |
| [06_リファクタリング提案書](06_リファクタリング提案書.md) | MVCからヘキサゴナル移行までの変遷と設計判断 |
| [07_新アーキテクチャ設計書](00.document/07_新アーキテクチャ設計書.md) | 4層クリーン構造、自己防衛ドメイン、トランザクション設計 |
| [08_オンボーディングガイド](00.document/08_オンボーディングガイド.md) | 3ステップ環境構築、動作確認、初期アカウント一覧 |
| [09_MVCからの設計転換ガイド](00.document/09_MVCからの設計転換ガイド.md) | MVC経験者向け設計転換、Before/Afterコード |
| [10_アーキテクチャ設計思想と開発の考え方](00.document/10_アーキテクチャ設計思想と開発の考え方.md) | レイヤー依存則、ドメイン自己完結、実装判断フロー |
| [11_SESドメイン業務ルール解説書](00.document/11_SESドメイン業務ルール解説書.md) | 精算幅、36協定判定、中抜け休憩、週報承認フロー |

---

## 🏛 アーキテクチャ概要

ドメインモデルを中心に配置し、UIやデータベースなどの外部技術を交換可能なアダプターとして扱う構成です。

```mermaid
flowchart TD
    subgraph Presentation [1. プレゼンテーション層 (Driving Adapter)]
        FC[FrontController] --> Act[Action / ApiAction]
        Act --> View[JSP / JSON]
    end

    subgraph Usecase [2. ユースケース層 (Inbound Port 実装)]
        Act --> UC[UseCase / Service]
    end

    subgraph Domain [3. ドメイン層 (アプリケーションコア)]
        UC --> DM[Domain Model / Business Rules]
        DM --> RepoIF[Repository Interface (Outbound Port)]
    end

    subgraph Infrastructure [4. インフラストラクチャ層 (Driven Adapter)]
        RepoImpl[JdbcRepository / DAO] -.->|implements| RepoIF
        UC --> TM[TransactionManager]
        RepoImpl --> DB[(MySQL 8.0)]
        TM --> DB
    end
```

### レイヤー対応表

| レイヤー | パッケージ | 主な役割 |
| :--- | :--- | :--- |
| **プレゼンテーション** | `controller` | `FrontController` で全 `*.do` リクエストを受け、各 `Action` にルーティング。JSP描画またはJSON返却。 |
| **ユースケース** | `usecase` | 業務ユースケースの手順制御、トランザクション境界の管理。 |
| **ドメイン** | `domain` | 契約精算ルール、36協定・有給コンプライアンス、勤怠計算などの純粋な業務ルール（POJO）。 |
| **インフラ** | `infrastructure` | DBアクセス（JDBC / DAO / SQL）、トランザクション制御（`TransactionManager`）。 |

---

## 📁 ディレクトリ構成

```text
Dev-lab/
├── 00.document/                  # 設計書・技術解説書群 (00〜11)
├── 10.project/                   # Webアプリケーション本体
│   └── webapp2/
│       ├── pom.xml               # Mavenビルド定義 (Java 11 / Tomcat 9)
│       └── src/
│           ├── main/
│           │   ├── java/
│           │   │   ├── common/           # ユーティリティ (MyUtil)
│           │   │   ├── controller/       # プレゼンテーション層
│           │   │   │   ├── FrontController.java
│           │   │   │   ├── Action.java / View.java
│           │   │   │   ├── action/       # 画面遷移Action (ShainList, SesAttendance等)
│           │   │   │   └── api/          # 非同期JSON Action (SesAttendanceApiAction)
│           │   │   ├── domain/           # ドメイン層
│           │   │   │   ├── attendance/   # 勤怠集計・勤務区分
│           │   │   │   ├── auth/         # ユーザー認証・ロール
│           │   │   │   ├── compliance/   # 36協定・有給義務判定
│           │   │   │   ├── contract/     # SES案件契約・精算計算
│           │   │   │   ├── shain/        # 社員・有給付与ルール
│           │   │   │   └── weeklyreport/ # 週報・レビューコメント
│           │   │   ├── usecase/          # ユースケース層
│           │   │   │   ├── attendance/   # テンプレート適用・一括反映
│           │   │   │   ├── contract/     # 精算金額算出
│           │   │   │   ├── shain/        # 社員・有給サービス
│           │   │   │   └── weeklyreport/ # 週報提出・承認
│           │   │   └── infrastructure/   # インフラ層
│           │   │       ├── db/           # TransactionManager / ConnectionBase
│           │   │       └── persistence/  # JdbcRepository / DAO
│           │   ├── resources/sql/        # 外部SQLファイル群
│           │   └── webapp/
│           │       ├── css/              # tailwind.min.css / style.css
│           │       ├── js/               # フロントエンドJavaScript
│           │       └── WEB-INF/view/     # JSP画面群 (ses_attendance, shainlist等)
│           └── test/                     # ドメイン単体テスト (JUnit 5)
├── 20.db/                        # DDL・初期データ (MySQL 8.0)
│   ├── createtb/                 # all_createtables.sql, ses_tables.sql
│   └── sampleinsert/             # サンプルデータ投入SQL
├── CONTRIBUTING.md               # 開発ガイドライン
└── README.md                     # 本ドキュメント
```

---

## 🛠 技術スタック

| 分類 | 技術・ツール | 補足 |
| :--- | :--- | :--- |
| **言語** | Java 11 | |
| **Web仕様** | Java EE 8 / Servlet 4.0, JSP 2.3, JSTL 1.2 | Tomcat 9.0準拠 |
| **サーバー** | Apache Tomcat 9.0 | |
| **データベース** | MySQL 8.0 (InnoDB, utf8mb4) | `META-INF/context.xml` にてJNDI DataSource接続 |
| **ビルド** | Apache Maven | `war` パッケージング |
| **JSON処理** | Jackson Databind 2.15+ | |
| **CSS** | Tailwind CSS | モダンレスポンシブUI |
| **テスト** | JUnit 5 | ドメイン層の高速な単体テスト |

---

## 🚀 環境構築・実行手順

### 1. データベース構築 (MySQL 8.0)
```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS kintai DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p kintai < 20.db/createtb/all_createtables.sql
mysql -u root -p kintai < 20.db/createtb/all_createviews.sql
mysql -u root -p kintai < 20.db/createtb/ses_tables.sql
for f in 20.db/sampleinsert/*.sql; do mysql -u root -p kintai < "$f"; done
```

### 2. アプリケーションのビルド
```bash
cd 10.project/webapp2
mvn clean package
```

### 3. アクセスURL
- ログイン画面: `http://localhost:8080/webapp2/login.do`
- SES勤怠管理画面: `http://localhost:8080/webapp2/sesAttendance.do`
- 社員一覧画面: `http://localhost:8080/webapp2/shainList.do`
- 有給休暇管理画面: `http://localhost:8080/webapp2/paidLeaveAdmin.do`
