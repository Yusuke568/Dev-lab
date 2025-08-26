#!/bin/bash

# 変更されたファイルを取得
files=$(git status --porcelain | awk '{print $2}')

# ファイルがなければ終了
if [ -z "$files" ]; then
  echo "変更されたファイルはないみたいだよ！"
  exit 0
fi

# 選択肢を表示
echo "どのファイルをコミットする？（スペース区切りで複数選べるよ）"
echo "$files"
echo ""
read -p "ファイル名を入力: " selected

# コミットメッセージを入力
read -p "コミットメッセージ: " msg

# 実行
git add $selected
git commit -m "$msg"
git push
