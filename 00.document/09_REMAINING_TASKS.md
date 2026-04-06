# 残タスク一覧（リファクタリング）

このドキュメントは、アプリケーションを新アーキテクチャへ移行するための残存タスクをまとめたものです。
作業を再開する際は、この一覧を上から順に進めてください。

## フェーズ1: 依存性注入（DI）の確立

**【完了】**

- **タスク:** `DependencyFactory` クラスの完成と適用
- **目的:** クラス間の依存関係を疎結合にし、テスト容易性を向上させる。
- **結果:**
    1. 未登録だったすべての `Action` クラス（`ShainDeleteExecuteAction`, `MenuAction`, `PaidLeaveAdminAction`, `UpdateLeaveAction`, `GrantLeaveByYearAction`, `GrantLeaveSelectedAction`）をリファクタリングし、`DependencyFactory` に登録しました。
    2. 上記 `Action` が依存していた古い `LeaveService`, `ShainService` を排除し、新しい `UseCase` 層（アプリケーションサービス層）経由で処理するように修正しました。
    3. `FrontController` は既に `DependencyFactory` を利用する形式だったため、変更は不要でした。

## フェーズ2: 軽微な修正と整理

**【完了】**

- **タスク:** ファイル名のタイポ修正
  - `10.project/webapp2/src/main/resources/sql/kintai/inset_staff_work.sql` を `insert_staff_work.sql` にリネームしました。
- **タスク:** SQLファイルの場所の統一
  - `WEB-INF/classes` 配下に散在していたSQLファイルおよびJSファイルを削除し、`src/main/resources` に集約されていることを確認しました。
- **タスク:** トランザクション管理の再設計
  - アプリケーションサービス層（`TransactionManager` インターフェース）およびインフラ層（`TransactionManagerImpl`、`ConnectionBase` の ThreadLocal 対応）へトランザクション管理の仕組みを再実装・統合しました。

## フェーズ3: 主要機能のアーキテクチャ移行

現在の作業ステップです。

- **【完了】「勤務区分」機能の完全なリファクタリング:**
  - `kintai.jsp` の勤務区分ドロップダウンを機能させる。
  - **手順:**
    1. `WorkType` のドメインモデル（エンティティ、値オブジェクト）を定義する。（既存のエンティティを利用・確認）
    2. データベースと対話するための永続化ポート（`WorkTypePort`）とアダプタ（`WorkTypePersistenceAdapter`）を実装する。
    3. 勤務区分リストを取得するためのユースケース（`GetWorkTypesUseCase`, `GetWorkTypesService`）を作成する。
    4. `KintaiDisplayAction` を修正し、このユースケースを呼び出して取得したデータをJSPに渡すようにする。
- **【完了】JavaScript APIのバックエンド再構築:**
  - 「出勤」「退勤」「保存」ボタンなどが非同期通信で呼び出しているバックエンﾄﾞAPIを、新しいアーキテクチャ（`API Action` -> `UseCase`）に沿って再設計・実装しました（`KintaiUpdateApiAction`, `UpdateKintaiService` の実装と `KintaiRecordDto` の導入）。
- **【完了】未移行のActionクラスの全面リファクタリング:**
  - `ShainListAction`, `ShainDeleteExecuteAction` など、まだ旧来の作りのまま残っているすべての `Action` クラスを、新しいアーキテクチャに沿ってリファクタリングしました。

## フェーズ4: 単体テストの実装と旧コードの完全な削除

**【完了】**

- **【完了】単体テストの追加:**
  - **ドメイン層:** 新しく作成した値オブジェクトやエンティティのビジネスロジックを検証するテストを作成しました。
  - **アプリケーション層:** 永続化層（リポジトリ）をモックに差し替え、ユースケースが正しく振る舞うかを検証するテスト（`GetMonthlyAttendanceServiceTest` など）を作成しました。
- **【完了】旧コードの完全な削除:**
  - 新アーキテクチャへの移行が完了した機能に対応する、古いクラスファイル（`KintaiService`, `ShainService`, 各種 `*Dao` クラス群, `CalendarDay` など）と古いテストをプロジェクトから完全に削除しました。
