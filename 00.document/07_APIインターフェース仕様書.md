# SES勤怠管理・勤務報告書システム APIインターフェース仕様書

## 1. API共通仕様

### 1.1 基本情報
- **プロトコル**: HTTP / HTTPS (POSTメソッド推奨)
- **ベースURL**: `http://localhost:8080/webapp2/api/`
- **データ形式**: JSON (`Content-Type: application/json; charset=UTF-8`)
- **認証方式**: Cookieベースセッション認証 (`JSESSIONID`)。未認証リクエストには HTTP 401 を返却。
- **文字コード**: UTF-8

### 1.2 共通レスポンス構造

#### 成功時レスポンス (HTTP 200 OK)
```json
{
  "status": "success",
  "data": { ... },
  "message": "処理が正常に完了しました。"
}
```

#### エラー時レスポンス (HTTP 400 / 401 / 403 / 500)
```json
{
  "status": "error",
  "errorCode": "INVALID_PARAMETER",
  "message": "終業時刻は始業時刻より後である必要があります。",
  "details": [
    { "field": "endTime", "issue": "Must be greater than startTime" }
  ]
}
```

### 1.3 エラーコード一覧
| エラーコード | HTTPステータス | 原因・説明 |
| :--- | :---: | :--- |
| `UNAUTHORIZED` | 401 | セッションが無効、または未ログイン状態でのAPI呼出し。 |
| `FORBIDDEN` | 403 | 操作対象のデータに対する編集権限がない（他人の勤怠編集等）。 |
| `INVALID_PARAMETER` | 400 | リクエストJSONのバリデーションエラー（フォーマット不正、値域違反）。 |
| `BUSINESS_RULE_VIOLATION`| 400 | ドメインバリデーション違反（締め済み勤怠の変更等）。 |
| `INTERNAL_SERVER_ERROR` | 500 | サーバー内部例外、DB接続障害等。 |

---

## 2. エンドポイント詳細仕様

### 2.1 日次勤怠保存・再集計API

- **エンドポイント**: `POST /api/sesAttendance.do`
- **担当クラス**: `controller.api.SesAttendanceApiAction`
- **概要**: 単一日の出退勤時刻・中抜け・区分を登録し、即時に当日の実働・残業および当月の月次サマリーを再計算して返却する。

#### リクエストパラメータ (JSON)
```json
{
  "employeeId": "1001",
  "workDate": "2026-10-05",
  "startTime": "09:00",
  "endTime": "19:30",
  "breakMinutes": 60,
  "middleBreakMinutes": 30,
  "abstractId": 1,
  "leaveType": "NORMAL",
  "remarks": "案件進捗会議、通院中抜け30分"
}
```

| フィールド名 | 型 | 必須 | 制約・バリデーション | 説明 |
| :--- | :--- | :---: | :--- | :--- |
| `employeeId` | String | ◯ | 4桁以上の半角数字 | 対象社員ID |
| `workDate` | String | ◯ | `YYYY-MM-DD` 形式 | 勤怠対象日付 |
| `startTime` | String | - | `HH:mm` 形式 (00:00〜23:59) | 始業時刻（未入力可） |
| `endTime` | String | - | `HH:mm` 形式。startTimeより後 | 終業時刻（未入力可） |
| `breakMinutes` | Integer | ◯ | 0以上の整数 | 標準休憩時間（分） |
| `middleBreakMinutes`| Integer | - | 0以上の整数。デフォルト0 | 中抜け休憩時間（分） |
| `abstractId` | Integer | ◯ | `abstract_master` に存在するID | 適用区分ID (1:通常, 2:有給等) |
| `leaveType` | String | ◯ | 'NORMAL', 'PAID_LEAVE', 'SPECIAL'等 | ドメインEnum値 |
| `remarks` | String | - | 最大200文字 | 備考・所感 |

#### レスポンスデータ (HTTP 200 OK)
```json
{
  "status": "success",
  "data": {
    "dailyResult": {
      "workDate": "2026-10-05",
      "actualWorkMinutes": 600,
      "actualWorkHours": 10.0,
      "overtimeMinutes": 120,
      "overtimeHours": 2.0,
      "isLeave": false
    },
    "monthlySummary": {
      "yearMonth": "2026-10",
      "totalWorkHours": 168.0,
      "totalOvertimeHours": 12.0,
      "settlementStatus": "WITHIN_RANGE",
      "settlementAmount": 650000,
      "settlementDiffAmount": 0,
      "overtimeLevel": "NORMAL",
      "paidLeaveDaysUsed": 0.0
    }
  },
  "message": "勤怠情報を保存しました。"
}
```

---

### 2.2 月間テンプレート一括適用API

- **エンドポイント**: `POST /api/applyMonthlyTemplate.do`
- **担当クラス**: `usecase.attendance.ApplyMonthlyTemplateUseCase`
- **概要**: 指定した年月の全平日に対し、契約の標準就業時間（例: 09:00〜18:00、休憩60分）を一括投入する。

#### リクエストパラメータ (JSON)
```json
{
  "employeeId": "1001",
  "yearMonth": "2026-10",
  "overwriteExisting": false
}
```

| フィールド名 | 型 | 必須 | 説明 |
| :--- | :--- | :---: | :--- |
| `employeeId` | String | ◯ | 対象社員ID |
| `yearMonth` | String | ◯ | `YYYY-MM` 形式 |
| `overwriteExisting` | Boolean| ◯ | 入力済みの平日を上書きするか（デフォルト `false`） |

#### レスポンスデータ (HTTP 200 OK)
```json
{
  "status": "success",
  "data": {
    "appliedDaysCount": 21,
    "skippedDaysCount": 0,
    "monthlySummary": {
      "totalWorkHours": 168.0,
      "settlementStatus": "WITHIN_RANGE"
    }
  },
  "message": "月間テンプレート（21日分）を適用しました。"
}
```

---

### 2.3 週報提出API

- **エンドポイント**: `POST /api/weeklyReport.do`
- **担当クラス**: `usecase.weeklyreport.SubmitWeeklyReportUseCase`
- **概要**: 週次業務報告、心身健康スコア、負荷スコアを登録し、必要に応じてエスカレーションを発火する。

#### リクエストパラメータ (JSON)
```json
{
  "employeeId": "1001",
  "yearWeek": "2026-W41",
  "taskTitle": "フロントエンド画面実装および結合テスト",
  "taskProgress": "全画面のTailwind CSS移行完了。テストカバレッジ85%達成。",
  "personalReflection": "中抜け機能の実装時にドメインモデルの責務分離がうまく機能した。",
  "nextWeekPlan": "精算計算APIの結合テスト実施予定。",
  "workloadScore": 3,
  "healthScore": 4,
  "escalationNeeded": false,
  "submit": true
}
```

#### レスポンスデータ (HTTP 200 OK)
```json
{
  "status": "success",
  "data": {
    "reportId": "rep-2026-w41-1001",
    "status": "SUBMITTED",
    "isAlertTriggered": false
  },
  "message": "週報を正常に提出しました。"
}
```
