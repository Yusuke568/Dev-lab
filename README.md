# Dev-lab: 勤怠管理・勤務報告書システム

本リポジトリは、Java (Jakarta EE / Servlet / JSP) と PostgreSQL による勤怠管理・勤務報告書Webアプリケーションの開発・学習用プロジェクトです。

一般的なMVC（Model 2）の課題を解消するため、**「Front Controller + Command パターン」** および **「4層レイヤードアーキテクチャ」** を採用しています。

---

## 📚 ドキュメント一覧

設計の詳細や要件定義書は `00.document/` ディレクトリに格納されています。

- 🌟 **[MVC経験者のためのアーキテクチャ設計解説書](00.document/08_MVC経験者のためのアーキテクチャ設計解説書.md)**（※MVCとの対比・クラス設計・開発フロー）
- 📄 [00_README（要件定義書インデックス）](00.document/00_README.md)
- 📄 [01_システム概要書](00.document/01_システム概要書.md)
- 📄 [02_機能要件定義書](00.document/02_機能要件定義書.md)
- 📄 [03_非機能要件定義書](00.document/03_非機能要件定義書.md)
- 📄 [04_画面設計書](00.document/04_画面設計書.md)
- 📄 [05_データ定義書](00.document/05_データ定義書.md)
- 📄 [07_新アーキテクチャ設計書](00.document/07_新アーキテクチャ設計書.md)

---

## 🏛 アーキテクチャの概要（MVCとの違い）

本プロジェクトは「Controllerが肥大化しやすい（Fat Controller）」「Modelの責務が曖昧になる」といったMVCの課題を解決するため、責務を4つのレイヤーに明確に分離しています。

```mermaid
graph TD
    subgraph Presentation [1. プレゼンテーション層]
        FC[FrontController] --> Act[Action / ApiAction]
        Act --> JSP[JSP / JSON]
    end

    subgraph Application [2. アプリケーション層]
        Act --> Svc[Service]
        Svc --> TM[TransactionManager]
    end

    subgraph Domain [3. ドメイン層]
        Entity[Entity (POJO)]
    end

    subgraph Infrastructure [4. インフラストラクチャ層]
        Svc --> DAO[DAO]
        DAO --> SQL[外部SQLファイル]
        DAO --> DB[(PostgreSQL)]
        TM --> DB
    end

    Act -.-> Entity
    Svc -.-> Entity
    DAO -.-> Entity
```

### MVCとの対応比較表

| MVCの要素 | 本プロジェクトの実装 | 主な役割 |
| :--- | :--- | :--- |
| **Controller** | **FrontController** + **Action** | `FrontController` が全 `*.do` リクエストを受け付け、URLに対応する個別 `Action`（Commandパターン）を呼び出します。 |
| **View** | **JSP**（または **JSON API**） | 画面描画に専念。スクリプトレット（`<% %>`）は原則廃止し、JSTL/EL式を使用します。 |
| **Model**<br>*(3分割)* | **Service**（アプリケーション層） | ビジネスロジックの実行、および `TransactionManager` によるトランザクション管理（コミット/ロールバック）。 |
| | **Entity**（ドメイン層） | データを表現する純粋なJavaオブジェクト（POJO）。 |
| | **DAO**（インフラ層） | DBアクセス（CRUD）専門クラス。SQLは外部ファイル（`.sql`）で管理。 |

---

## 📁 ディレクトリ構成

プロジェクト全体のディレクトリ構成および各パッケージの責務は以下の通りです。

```text
Dev-lab/
├── 00.document/                  # 設計書・要件定義書・アーキテクチャドキュメント群
├── 10.project/                   # Webアプリケーション本体 (Mavenプロジェクト)
│   ├── Servers/                  # Eclipse/Tomcat サーバー設定
│   └── webapp2/                  # Java Webアプリケーション
│       ├── pom.xml               # Mavenビルド・依存ライブラリ設定
│       └── src/
│           ├── main/
│           │   ├── java/
│           │   │   ├── com/example/
│           │   │   │   ├── common/           # 共通ユーティリティ（文字列・日付・JSON変換等）
│           │   │   │   ├── controller/       # [プレゼンテーション層]
│           │   │   │   │   ├── FrontController.java # 一元受付けサーブレット
│           │   │   │   │   ├── Action.java   # コマンドインターフェース
│           │   │   │   │   ├── View.java     # 遷移先情報保持クラス
│           │   │   │   │   ├── action/       # 画面遷移系Action（例: ShainListAction）
│           │   │   │   │   └── api/          # 非同期JSON通信Action（例: KintaiUpdateApiAction）
│           │   │   │   ├── entity/           # [ドメイン層] データエンティティ（Shain, Kintai等）
│           │   │   │   ├── infra/            # [インフラ層] DB接続基盤（ConnectionBase）
│           │   │   │   └── service/          # [アプリケーション層] ビジネスロジック・ユースケース
│           │   │   │       ├── ShainService.java
│           │   │   │       ├── KintaiService.java
│           │   │   │       ├── LeaveService.java
│           │   │   │       └── transaction/  # トランザクション管理（TransactionManager）
│           │   │   └── dao/                  # [インフラ層] データアクセス（ShainDao, KintaiDao等）
│           │   ├── resources/
│           │   │   └── sql/                  # 外部化されたSQLファイル
│           │   │       ├── shainlist/        # 社員操作SQL
│           │   │       ├── kintai/           # 勤怠操作SQL
│           │   │       └── classmaster/      # 区分マスタSQL
│           │   └── webapp/                   # Webリソース
│           │       ├── WEB-INF/
│           │       │   ├── view/             # 画面JSPファイル（セキュリティのためWEB-INF配下）
│           │       │   └── web.xml           # URLルーティング設定 (*.do -> FrontController)
│           │       ├── css/                  # スタイルシート
│           │       └── js/                   # クライアントサイドJavaScript
│           └── test/                         # 単体テストコード (JUnit 5)
├── 20.db/                        # データベース構築用DDL・初期データ
│   ├── createtb/                 # テーブル・ビュー作成スクリプト (all_createtables.sql等)
│   └── sampleinsert/             # サンプルデータ投入スクリプト
├── CONTRIBUTING.md               # 開発ガイドライン・コーディング規約
└── README.md                     # 本ドキュメント
```

---

## 🛠 技術スタック

| 分類 | 技術・ツール | 補足 |
| :--- | :--- | :--- |
| **言語** | Java 11+ | |
| **仕様** | Jakarta EE (Servlet 4.0, JSP 2.3, JSTL 2.0) | |
| **サーバー** | Apache Tomcat 9.0 | |
| **データベース** | PostgreSQL 42.7+ | |
| **ビルド** | Apache Maven | `war` パッケージング |
| **JSON処理** | Jackson Databind 2.15+ | API用ActionでのJSON変換 |
| **テスト** | JUnit 5 | |
| **フロントエンド** | HTML5, CSS3 (Modern/Responsive), Vanilla JavaScript | |

---

## 🚀 環境構築・実行手順

### 1. データベースの準備
PostgreSQL に接続し、データベース・テーブル・初期データを投入します。

```bash
# データベース作成
createdb devlab_db

# テーブルおよびビューの作成
psql -d devlab_db -f 20.db/createtb/all_createtables.sql
psql -d devlab_db -f 20.db/createtb/all_createviews.sql

# 初期データの投入
for f in 20.db/sampleinsert/*.sql; do
    psql -d devlab_db -f "$f"
done
```

### 2. アプリケーションのビルド
```bash
cd 10.project/webapp2
mvn clean package
```

### 3. Tomcatへのデプロイ
生成された `target/webapp2.war` を Tomcat の `webapps/` に配置するか、Eclipse / VSCode のサーバー連携機能で `10.project/webapp2` を起動します。

### 4. ブラウザでアクセス
```text
http://localhost:8080/webapp2/shainList.do
```

---

## 💡 新機能実装の流れ（クイックガイド）

新機能を追加する際は、以下の順番で下位レイヤーから作成します：

1. **SQLファイル**: `src/main/resources/sql/機能名/*.sql` を作成
2. **Entity**: `com.example.entity` にデータ保持用クラスを作成
3. **DAO**: `dao` に `Connection` を受け取ってSQLを実行するメソッドを作成
4. **Service**: `com.example.service` に `TransactionManager.execute(...)` を使ってビジネスロジックを実装
5. **Action**: `com.example.controller.action`（または `api`）に `Action` インターフェースを実装したクラスを作成（クラス名は `URL名Action` とする）
6. **JSP / View**: `src/main/webapp/WEB-INF/view/` にJSPを作成
