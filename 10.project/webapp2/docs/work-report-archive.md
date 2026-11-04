# 作業報告書をサーバーのフォルダにも置く設定

勤怠画面の「作業報告書(Excel)出力」でダウンロードするたびに、同じファイルをサーバーの所定フォルダにも置く。
そのフォルダを共有すると、ほかのパソコンから開いて編集（リーダー所見の記入など）できる。

## 置かれるファイル

```
{基準フォルダ}/{年}/{月}/作業報告書_{年月}_{社員ID}_{氏名}.xlsm
例: /srv/kintai/work-reports/2026/11/作業報告書_202611_126_山田_太郎.xlsm
```

- 同じ社員・同じ月をもう一度出力すると、前のファイルを `{年}/{月}/_backup/` に日時付きで移してから置く。
  共有フォルダ上で書き足した内容が、再出力で消えないようにするため。
- 基準フォルダが設定されていなければ何も置かない（ダウンロードはこれまでどおり）。
- 置けなかったとき（権限がないなど）もダウンロードは続け、次に勤怠画面を開いたときに理由を表示する。

## 1. フォルダを作る（Ubuntu サーバー）

Tomcat と共有フォルダの利用者の両方が書けるよう、共通のグループを作る。

```bash
sudo groupadd kintai
sudo usermod -aG kintai tomcat          # Tomcat の実行ユーザー（環境によって tomcat9 など）
sudo mkdir -p /srv/kintai/work-reports
sudo chown tomcat:kintai /srv/kintai/work-reports
sudo chmod 2775 /srv/kintai/work-reports   # 2 = 中に作ったファイルも kintai グループになる
```

アプリは、作るフォルダを `rwxrwxr-x`、ファイルを `rw-rw-r--` にする（グループの利用者も編集できる）。

## 2. Tomcat に基準フォルダを教える

起動オプション `-Dkintai.workReport.archiveDir` か、環境変数 `KINTAI_WORK_REPORT_DIR` のどちらかで指定する。

### apt で入れた Tomcat（tomcat9 パッケージ）の場合

systemd の設定で、環境変数と書き込みを許すフォルダを足す（このパッケージは決まったフォルダ以外に書き込めない）。

```bash
sudo systemctl edit tomcat9
```

```ini
[Service]
Environment="KINTAI_WORK_REPORT_DIR=/srv/kintai/work-reports"
ReadWritePaths=/srv/kintai/work-reports
```

```bash
sudo systemctl restart tomcat9
```

### 手で入れた Tomcat の場合

`{Tomcat}/bin/setenv.sh` に書いて再起動する。

```bash
export CATALINA_OPTS="$CATALINA_OPTS -Dkintai.workReport.archiveDir=/srv/kintai/work-reports"
```

## 3. フォルダを共有する

### 社内（同じネットワーク）から：Samba でネットワークドライブにする

```bash
sudo apt install samba
sudo nano /etc/samba/smb.conf
```

末尾に追加する。

```ini
[kintai-reports]
   path = /srv/kintai/work-reports
   valid users = @kintai
   read only = no
   force group = kintai
   create mask = 0664
   directory mask = 2775
```

共有を使う人ごとに、Linux ユーザーを kintai グループに入れて Samba のパスワードを設定する。

```bash
sudo usermod -aG kintai <ユーザー名>
sudo smbpasswd -a <ユーザー名>
sudo systemctl restart smbd
```

Windows では、エクスプローラーの「ネットワーク ドライブの割り当て」で `\\192.168.10.110\kintai-reports` を指定する。

### 動作の確認

```bash
# Tomcat の実行ユーザーを確かめる（1. の usermod で使う名前）
ps -o user= -C java
# Tomcat が kintai グループに入っているか（入れた後は Tomcat の再起動が必要）
id tomcat
# Samba の設定に誤りがないか
testparm -s
# サーバー上で共有にアクセスできるか
smbclient //localhost/kintai-reports -U <ユーザー名> -c 'ls'
```

勤怠画面で作業報告書を出力し、`/srv/kintai/work-reports/{年}/{月}/` にファイルができることを確かめる。

### うまくいかないとき

| 症状 | 確かめること |
|---|---|
| 勤怠画面に「サーバーのフォルダに置けませんでした」と出る | フォルダの所有者・グループ（`ls -ld /srv/kintai/work-reports`）、Tomcat が kintai グループに入っているか、tomcat9 パッケージなら `ReadWritePaths` を足したか |
| ファイルができない（メッセージも出ない） | 基準フォルダが Tomcat に渡っているか（`sudo systemctl show tomcat9 -p Environment`）。設定後に Tomcat を再起動したか |
| Windows からつながらない | サーバーのファイアウォールで Samba を許可しているか（`sudo ufw allow samba`）。`\\192.168.10.110\kintai-reports` の表記 |
| Windows から開けるが保存できない | 接続したユーザーが kintai グループに入っているか。`smb.conf` の `read only = no` |
| ファイルが別の人に使われていて開けない | Excel はほかの人が開いていると読み取り専用で開く。閉じてもらうか、読み取り専用で開いて別名で保存する |

### 社外（インターネット経由）から

Samba（ポート445）をインターネットに直接公開してはいけない。次のどれかにする。

- **VPN（Tailscale など）**：社外のパソコンを社内ネットワークにつなぎ、上の Samba 共有をそのまま使う。設定が少なく安全。
- **Nextcloud**：サーバーに入れて、このフォルダを「外部ストレージ」として登録する。ブラウザやスマホのアプリ、Windows のドライブとして使える（HTTPS の設定が必要）。
- **クラウドドライブ（OneDrive・Google ドライブなど）と同期**：rclone でこのフォルダを定期的に同期する。
  クラウド側で編集した内容を戻すには双方向の同期（rclone bisync）が必要で、同時に編集すると衝突する点に注意。
