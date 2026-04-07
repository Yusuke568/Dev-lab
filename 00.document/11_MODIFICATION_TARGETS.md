# テストユースケース対応 修正対象リスト

本ドキュメントは、`10_TEST_USECASES.md` の要件と現在のプロジェクト実装との差異を俯瞰し、修正が必要な対象ファイルとその内容をリストアップしたものです。

## 1. ログイン・ログアウト関連
- **UC-LOG-05 (ログアウト機能の欠如)**
  - **対象ファイル**: 
    - `com.example.controller.action.LogoutAction.java` (新規作成)
    - `com.example.controller.FrontController.java` (ルーティングの追加)
    - `webapp/WEB-INF/view/menu.jsp`, `kintai.jsp` 等 (ログアウトボタンの追加)

## 2. メニュー・画面表示関連
- **UC-NAV-01, 02 (権限によるメニュー表示制御の欠如)**
  - **対象ファイル**:
    - `com.example.controller.action.MenuAction.java` またはログイン処理 (セッションに権限情報を保持)
    - `webapp/WEB-INF/view/menu.jsp` (`<c:if>` による管理者用メニューの非表示化)
- **UC-NAV-03 (モバイル端末向け最適化UIの欠如)**
  - **対象ファイル**:
    - `webapp/WEB-INF/view/*.jsp` (`viewport` メタタグの追加)
    - `webapp/css/style.css` (レスポンシブデザイン・メディアクエリの追加)

## 3. 勤怠入力機能
- **UC-KIN-04 (休憩時間の自動控除機能の欠如)**
  - **対象ファイル**:
    - `resources/js/kintai/overtime.js` または `kintai.js` (休憩時間分を控除する計算ロジック)
- **UC-KIN-05 (SE所感コメント・定型文機能の欠如)**
  - **対象ファイル**:
    - `webapp/WEB-INF/view/kintai.jsp` (テキストエリア、定型文挿入ボタンの追加)
    - `resources/js/kintai/kintai.js` (定型文の挿入処理)
- **UC-KIN-06 (一時保存機能の欠如)**
  - **対象ファイル**:
    - `webapp/WEB-INF/view/kintai.jsp` (「一時保存」ボタンの追加)
    - `resources/js/kintai/save.js` (本番提出と一時保存を切り分けるフラグ処理)

## 4. 整合性自動チェック機能 [F006] (フロントエンド・バックエンドバリデーション)
- **UC-VAL-01〜04 (退勤<出勤、24H超過、有給重複、時間外差異のチェック欠如)**
  - **対象ファイル**:
    - `resources/js/kintai/save.js` (保存前のフロントエンド検証)
    - `com.example.application.service.UpdateKintaiService.java` (バックエンドの検証ロジックとエラーハンドリング)
    - `webapp/WEB-INF/view/kintai.jsp` (エラーメッセージの表示領域)

## 5. 複数日一括入力機能 [F004]
- **UC-BLK-01, 02 (一括入力UIおよび処理の欠如)**
  - **対象ファイル**:
    - `webapp/WEB-INF/view/kintai.jsp` (行選択チェックボックス、一括入力用フォームの追加)
    - `resources/js/kintai/kintai.js` (選択行に対する値の一括セットロジック)

## 6. 申請・承認ワークフロー連携 [F001, F005, F014]
- **UC-REQ-01〜04 (時間外・有給の申請/承認機能の欠如)**
  - **対象ファイル**:
    - `webapp/WEB-INF/view/kintai.jsp` (申請ボタン・申請ステータス表示、事由入力欄の展開)
    - `com.example.application.service.ApprovalService.java` 等 (承認ロジックの新規作成)
    - `webapp/WEB-INF/view/approval.jsp` 等 (承認者向け管理画面の追加)

## 7. アラート・通知・自動処理 [F011, F013]
- **UC-ALT-01〜03 (未入力・未提出アラートの欠如)**
  - **対象ファイル**:
    - `webapp/WEB-INF/view/menu.jsp` または `com.example.controller.action.MenuAction.java` (ログイン直後のダッシュボード画面に、未入力の警告メッセージを表示するロジック)
