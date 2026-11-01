# ログインパスワードの登録・変更手順

`login_table.password` には平文ではなく、PBKDF2WithHmacSHA256 のハッシュを保存します。

```
pbkdf2_sha256$<反復回数>$<Base64のソルト>$<Base64のハッシュ>
```

アプリには login_table に書き込む画面がありません。ユーザーの登録やパスワードの変更は、
サーバーで次の手順で行います。

## 1. ハッシュを作る

`PasswordHasher` は Java 標準機能だけで動くので、デプロイ済みの WAR の `WEB-INF/classes` をクラスパスにすれば実行できます。

```bash
java -cp "$CATALINA_HOME/webapps/webapp2/WEB-INF/classes" com.example.auth.domain.service.PasswordHasher
```

`Password:` と `Password (again):` が表示されるので、同じパスワードを 2 回入力します（画面には表示されません）。
ハッシュが 1 行出力されます。

```
pbkdf2_sha256$210000$3q2+7w0MXl...==$Wm9vYmFy...=
```

- パスワードはコマンド引数では渡せません（コマンド履歴に残るため）。
- 開発環境では `target/classes` を指定しても同じです。
- 同じパスワードでも実行するたびに違うハッシュになります（ソルトが毎回違うため）。どれも正しく照合されます。

## 2. DB に登録する

出力されたハッシュを、**シングルクォートで囲んで**そのまま SQL に入れます。
`$` が含まれますが、シングルクォートの中なら PostgreSQL ではそのまま文字として扱われます。

### ユーザーを新しく登録する

```sql
INSERT INTO login_table (username, password, enabled)
VALUES ('1001', 'pbkdf2_sha256$210000$<ソルト>$<ハッシュ>', 1);
```

`username` は staff_table の社員IDと同じ値にします（ログイン後に社員情報を取得するため）。

### パスワードを変更する

```sql
UPDATE login_table
SET password = 'pbkdf2_sha256$210000$<ソルト>$<ハッシュ>'
WHERE username = '1001';
```

### ログインを止める / 再開する

```sql
UPDATE login_table SET enabled = 0 WHERE username = '1001';  -- 停止
UPDATE login_table SET enabled = 1 WHERE username = '1001';  -- 再開
```

`enabled = 1` のユーザーだけがログインできます。

## 3. 平文パスワードからの移行（初回のみ）

このハッシュ対応版をデプロイすると、平文のまま保存されているユーザーはログインできなくなります。
平文での照合は残していません。次の順で移行します。

1. `src/main/resources/schema/migrations/20261101_widen_login_password.sql` を実行して、`password` 列を `varchar(255)` に広げる
2. 既存ユーザーごとに手順 1 でハッシュを作り、手順 2 の UPDATE で置き換える
3. 平文が残っていないことを確認する（0 件になれば完了）

   ```sql
   SELECT username FROM login_table WHERE password NOT LIKE 'pbkdf2_sha256$%' OR password IS NULL;
   ```

4. 新しい WAR をデプロイする

ユーザーに今のパスワードを使い続けてもらう場合は、平文の値を手順 1 で入力すればハッシュになります。
作業後は、平文のパスワードを書いたメモやファイルを残さないようにしてください。
