# 勤務報告書テンプレート化システム (Enhanced Time Reporting System)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Architecture: Hexagonal](https://img.shields.io/badge/Architecture-Hexagonal-blue.svg)](00.document/04_アーキテクチャ設計書.md)

本プロジェクトは、従来の勤怠管理システムをモダンな**ヘキサゴナルアーキテクチャ（Port & Adapter）**へと刷新し、勤務報告書のテンプレート化やスマートフォン対応を実現した次世代勤怠管理プラットフォームです。

---

## 🚀 主な特徴

- **テンプレートエンジン**: 過去の勤務パターンから報告書を自動生成し、入力時間を大幅に削減。
- **マルチデバイス対応**: レスポンシブ設計により、PCだけでなく外出先のスマホからも快適に入力可能。
- **高保守性設計**: ヘキサゴナルアーキテクチャの採用により、ビジネスロジックが外部依存（DBやWeb）から完全に独立。
- **非同期ユーザー体験**: AJAX（Fetch API）を活用した、ページ遷移のないスムーズな操作感。

## 🏗 アーキテクチャ

本システムは、ドメイン駆動設計（DDD）の原則に基づいたヘキサゴナルアーキテクチャを採用しています。

- **Domain**: ビジネスの核となるルールとモデル（外部依存なし）。
- **Application**: ユースケースの実現とトランザクション制御。
- **Adapter**: 外部世界（Web, DB, API）との接続。

詳細な設計については、[アーキテクチャ設計書](00.document/04_アーキテクチャ設計書.md) を参照してください。

## 🛠 技術スタック

- **Back-end**: Java 11 / Jakarta EE (JSP/Servlet)
- **Front-end**: HTML5 / CSS3 / JavaScript (Vanilla JS)
- **Database**: MySQL 8.0
- **Server**: Apache Tomcat 9.0
- **Build**: Maven

## 📂 フォルダ構成

- `00.document/`: 要件定義、設計書、移行計画などのドキュメント群。
- `10.project/`: Javaアプリケーションのソースコード。
- `20.db/`: データベース構築用SQLスクリプト。

## 🏁 はじめに (Setup)

1. **データベースの構築**:
   `20.db/createtb/` 配下のSQLを実行してテーブルを作成し、`sampleinsert/` で初期データを投入してください。
2. **プロジェクトのインポート**:
   EclipseまたはVSCodeで `10.project/webapp2` をMavenプロジェクトとしてインポートします。
3. **サーバーの起動**:
   Tomcat 9.0 を設定し、`webapp2` をデプロイして起動してください。

---

## 🤝 貢献について

開発への参加を歓迎します！詳細は [CONTRIBUTING.md](CONTRIBUTING.md) をご覧ください。

---
*最終更新日: 2026年5月5日*
