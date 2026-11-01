# ログインパスワードの登録・変更手順

`login_table.PASSWORD` には平文ではなく、PBKDF2WithHmacSHA256 のハッシュを保存します。

```
pbkdf2_sha256$<反復回数>$<Base64のソルト>$<Base64のハッシュ>
```

- 照合: `infrastructure.persistence.JdbcAuthRepository`（SQL は `sql/shainlist/select_login.sql`）
- ハッシュの作成・照合: `domain.auth.PasswordHasher`
- 社員登録画面から社員を登録すると、初期パスワード `password` のハッシュで login_table にアカウントが作られます（`ShainDao.create`）。

初期パスワード以外にしたい場合や、パスワードを変更する場合は、サーバーで次の手順で行います。

## 1. ハッシュを作る

`PasswordHasher` は Java 標準機能だけで動くので、デプロイ済みの WAR の `WEB-INF/classes` をクラスパスにすれば実行できます。

```bash
java -cp "$CATALINA_HOME/webapps/webapp2/WEB-INF/classes" domain.auth.PasswordHasher
```

`Password:` と `Password (again):` が表示されるので、同じパスワードを 2 回入力します（画面には表示されません）。
ハッシュが 1 行出力されます。

```
pbkdf2_sha256$210000$3q2+7w0MXl...==$Wm9vYmFy...=
```

- パスワードはコマンド引数では渡せません（コマンド履歴に残るため）。
- 開発環境では `target/classes` を指定しても同じです。
- 同じパスワードでも実行するたびに違うハッシュになります（ソルトが毎回違うため）。どれも正しく照合されます。
- ログイン画面は入力されたパスワードの前後の空白を取り除いて照合します。前後に空白のあるパスワードは使わないでください。

## 2. DB に登録する

出力されたハッシュを、**シングルクォートで囲んで**そのまま SQL に入れます。
`USERNAME` は `staff_table.ID`（社員ID）です。

### ログインアカウントを登録する

```sql
INSERT INTO login_table (USERNAME, PASSWORD, ENABLED)
VALUES (1001, 'pbkdf2_sha256$210000$<ソルト>$<ハッシュ>', 1);
```

### パスワードを変更する

```sql
UPDATE login_table
SET PASSWORD = 'pbkdf2_sha256$210000$<ソルト>$<ハッシュ>'
WHERE USERNAME = 1001;
```

### ログインを止める / 再開する

```sql
UPDATE login_table SET ENABLED = 0 WHERE USERNAME = 1001;  -- 停止
UPDATE login_table SET ENABLED = 1 WHERE USERNAME = 1001;  -- 再開
```

`ENABLED = 1` のユーザーだけがログインできます。

## 3. 平文パスワードからの移行（初回のみ）

このハッシュ対応版をデプロイすると、平文のまま保存されているユーザーはログインできなくなります。
平文での照合は残していません。次の順で移行します。

`PASSWORD` 列はすでに `VARCHAR(500)` なので、列の変更は不要です。

1. 既存ユーザーごとに手順 1 でハッシュを作り、手順 2 の UPDATE で置き換える
2. 平文が残っていないことを確認する（0 件になれば完了）

   ```sql
   SELECT USERNAME FROM login_table WHERE PASSWORD NOT LIKE 'pbkdf2_sha256$%' OR PASSWORD IS NULL;
   ```

3. 新しい WAR をデプロイする

ユーザーに今のパスワードを使い続けてもらう場合は、平文の値を手順 1 で入力すればハッシュになります。
作業後は、平文のパスワードを書いたメモやファイルを残さないようにしてください。

サンプルデータ `20.db/sampleinsert/add_existing_users_login.sql` は、パスワード `password` のハッシュで登録するようにしてあります。
