# Dev-lab: SES勤怠管理・勤務報告書システム

本リポジトリは、Java 11 (Servlet 4.0 / JSP / JSTL) と MySQL 8.0 によるSES勤怠管理・勤務報告書Webアプリケーションです。

肥大化しやすいMVC（Fat Controller / Fat Service）の課題を根本解消し、**「ヘキサゴナル／クリーンアーキテクチャ（Ports & Adapters）」** および **「Front Controller + Command パターン」** を採用しています。

---

## 📚 ドキュメント体系一覧 (`00.document/`)

要件定義から基本設計、詳細設計、解説、環境構築まで完全体系化されています。

| 番号 | ドキュメント名 | 概要 |
| :--- | :--- | :--- |
| **00** | [ドキュメント総合インデックス](00.document/00_ドキュメント総合インデックス.md) | 全ドキュメントの構成、ロール別読書ガイド、文書管理規約 |
| **01** | [システム概要・企画背景書](00.document/01_システム概要・企画背景書.md) | SES業界の構造的課題、システム目的・KPI、ユーザーペルソナ |
| **02** | [機能要件定義書](00.document/02_機能要件定義書.md) | 全18業務機能の詳細仕様、入出力バリデーション、業務フロー |
| **03** | [非機能要件定義書](00.document/03_非機能要件定義書.md) | 性能目標、MySQLコネクションプール、セキュリティ、品質基準 |
| **04** | [アーキテクチャ設計書](00.document/04_アーキテクチャ設計書.md) | 4層ヘキサゴナル構造、Port/Adapter分離、TransactionManager |
| **05** | [データモデル設計書](00.document/05_データモデル設計書.md) | ER図、全13テーブルの完全定義、インデックス、ビュー |
| **06** | [画面・UI設計書](00.document/06_画面・UI設計書.md) | 全8画面のレイアウト、Tailwind CSS、Fetch API非同期設計 |
| **07** | [APIインターフェース仕様書](00.document/07_APIインターフェース仕様書.md) | 非同期JSON APIエンドポイント、スキーマ、ステータスコード |
| **08** | [ドメイン業務ロジック詳細設計書](00.document/08_ドメイン業務ロジック詳細設計書.md) | SES精算計算式、36協定4段階判定、有給自動付与アルゴリズム |
| **09** | [アーキテクチャ設計思想とMVC転換ガイド](00.document/09_アーキテクチャ設計思想とMVC転換ガイド.md) | Switchの例えによるPorts & Adapters解説、Before/Afterコード |
| **10** | [SESドメイン業務知識解説書](00.document/10_SESドメイン業務知識解説書.md) | 準委任契約の商習慣、労基法第36条・39条、メンタルヘルス管理 |
| **11** | [オンボーディング・開発環境構築手順書](00.document/11_オンボーディング・開発環境構築手順書.md) | 3ステップ環境構築、初期アカウント一覧、テスト実行、NG集 |

---

## 🏛 アーキテクチャ概要

ビジネスロジック（ドメイン）を中心に据え、Web（Servlet）やDB（MySQL）を外部アダプターとして疎結合化しています。

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

---

## 📁 ディレクトリ構成

```text
Dev-lab/
├── 00.document/                  # ドキュメント体系 (00〜11)
├── 10.project/                   # Webアプリケーション本体
│   └── webapp2/
│       ├── pom.xml               # Mavenビルド定義 (Java 11 / Tomcat 9)
│       └── src/
│           ├── main/
│           │   ├── java/
│           │   │   ├── common/           # ユーティリティ (MyUtil)
│           │   │   ├── controller/       # プレゼンテーション層 (FrontController, Action)
│           │   │   ├── domain/           # ドメイン層 (純粋POJO / 業務ルール)
│           │   │   ├── usecase/          # ユースケース層 (手順制御)
│           │   │   └── infrastructure/   # インフラ層 (JDBC / DAO / TransactionManager)
│           │   ├── resources/sql/        # 外部SQLファイル群
│           │   └── webapp/
│           │       ├── css/              # Tailwind CSS
│           │       ├── js/               # フロントエンドJavaScript
│           │       └── WEB-INF/view/     # JSP画面群 (全8画面)
│           └── test/                     # ドメイン単体テスト (JUnit 5: 20件全パス)
├── 20.db/                        # DDL・初期データ (MySQL 8.0)
│   ├── createtb/                 # all_createtables.sql, ses_tables.sql
│   └── sampleinsert/             # サンプルデータ投入SQL
├── CONTRIBUTING.md               # 開発ガイドライン
└── README.md                     # 本ドキュメント
```

---

## 🚀 環境構築・実行手順（クイックスタート）

詳細な手順は [11_オンボーディング・開発環境構築手順書](00.document/11_オンボーディング・開発環境構築手順書.md) を参照してください。

```bash
# 1. データベース構築 (MySQL 8.0)
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS kintai DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p kintai < 20.db/createtb/all_createtables.sql
mysql -u root -p kintai < 20.db/createtb/all_createviews.sql
mysql -u root -p kintai < 20.db/createtb/ses_tables.sql
for f in 20.db/sampleinsert/*.sql; do mysql -u root -p kintai < "$f"; done

# 2. ビルドおよびテスト実行
cd 10.project/webapp2
mvn clean test
```
