# リファクタリング提案書

## 1. はじめに

本ドキュメントは、現在の学習用Webアプリケーションに対して、より実践的かつ保守性の高い設計を学ぶことを目的としたリファクタリング案を提示するものです。

特定のフレームワークに依存せず、Webアプリケーションの基盤となる設計パターン（MVC、フロントコントローラーなど）や技術（JSTL、非同期通信など）を深く理解し、拡張性の高いコードベースを構築することを目指します。

## 2. 現状のアーキテクチャと課題

現在のシステムは、古典的な「Servlet/JSPモデル（モデル2アーキテクチャ）」に基づいて構築されています。これはWebアプリケーションの基本を学ぶ上で優れた出発点ですが、規模が大きくなるにつれていくつかの課題が生じます。

- **URLごとにServletが存在 (`ShainList`Servlet, `ShainInsert`Servlet, etc.)**
- **Servletがビジネスロジックと画面遷移の両方を担当**
- **JSPがHTMLの表示と、一部Javaコードによる動的処理を担当**
- **DAOがデータベースとのやり取りを担当**

この構成における主な課題は以下の通りです。

- **課題1: 処理の重複と見通しの悪さ**
  リクエストの文字コード設定や認証チェックといった複数のServletで必要になる共通処理が、それぞれのServletクラスに分散・重複してしまいがちです。

- **課題2: ビジネスロジックとビューの密結合**
  JSPファイル内に`<% ... %>`（スクリプトレット）を用いてJavaのコードを記述すると、ロジックと見た目の分離が不十分になり、デザイナーとプログラマーの分業が困難になります。

- **課題3: 煩雑なリソース管理**
  データベース接続（Connection）やSQL実行（Statement）のリソース管理を手動で行う必要があり、`try-catch-finally`ブロックが複雑化し、リソースの解放漏れといったバグの原因になりやすいです。

- **課題4: 同期的な画面遷移によるユーザー体験の低下**
  ボタンのクリックなど、少しの操作でも常にページ全体が再読み込みされるため、ユーザー体験が損なわれることがあります。

## 3. 提案するアーキテクチャとリファクタリング方針

上記の課題を解決するため、フレームワークを使わずに以下の4つの方針でリファクタリングを進めることを提案します。

### 3.1. フロントコントローラーパターンの導入

すべてのHTTPリクエストを単一の窓口である`FrontController` Servletで一元的に受け付けます。`FrontController`は、リクエストURLやパラメータに応じて、実際の処理を行う`Action`クラス（POJO: Plain Old Java Object）を呼び出します。

![フロントコントローラーパターン](https://i.imgur.com/q7ykJ3c.png)

- **FrontControllerの役割**:
  - 文字コード設定、認証チェックなどの共通処理
  - リクエスト内容に応じた適切な`Action`クラスの特定と呼び出し
- **Actionクラスの役割**:
  - Service層を呼び出し、ビジネスロジックを実行
  - 実行結果をリクエストスコープなどに設定
  - 表示すべきJSPのパスを`FrontController`に返す

これにより、共通処理が集約され、個々の`Action`クラスはビジネスロジックの呼び出しに専念でき、コードの見通しが大幅に改善されます。

### 3.2. JSTLとELによるビューの改善

JSP内の`<% ... %>`（スクリプトレット）を完全に排除し、**JSTL (JSP Standard Tag Library)** と **EL (Expression Language)** に置き換えます。

**【変更前】**
```jsp
<%
  List<ShainBean> shainList = (List<ShainBean>) request.getAttribute("shainList");
  for (ShainBean shain : shainList) {
%>
  <tr>
    <td><%= shain.getShainNo() %></td>
    <td><%= shain.getShainName() %></td>
  </tr>
<%
  }
%>
```

**【変更後】**
```jsp
<c:forEach items="${shainList}" var="shain">
  <tr>
    <td>${shain.shainNo}</td>
    <td>${shain.shainName}</td>
  </tr>
</c:forEach>
```
このように、JSPからJavaコードをなくすことで、ビューとロジックを完全に分離し、可読性と保守性を高めます。

### 3.3. DAO/Service層の責務分離とリソース管理の改善

`try-with-resources`構文を全面的に採用し、データベース接続リソースの管理を簡潔かつ安全にします。

**【変更前】**
```java
Connection con = null;
try {
  con = ConnectionBase.getConnection();
  // ... SQL実行処理 ...
} catch (SQLException e) {
  // ...
} finally {
  if (con != null) {
    try { con.close(); } catch (SQLException e) { /* ignore */ }
  }
}
```

**【変更後】**
```java
try (Connection con = ConnectionBase.getConnection();
     PreparedStatement ps = con.prepareStatement(SQL)) {
  // ... SQL実行処理 ...
} catch (SQLException e) {
  // ...
}
```

また、ビジネスロジックを担う`Service`層でトランザクション管理（コミット/ロールバック）を行い、`DAO`層はSQLの実行のみに専念するよう、責務を明確に分離します。

### 3.4. API導入による非同期通信の実装

画面の一部分だけを更新するために、JSON形式でデータを返す専用の`ApiController`（Servlet）を設けます。フロントエンドのJavaScriptからは`fetch` APIを使ってこのServletを呼び出します。

**【処理の流れ】**
1.  **ユーザー操作**: ブラウザでボタンをクリックする。
2.  **JavaScript**: `fetch`を使い、`ApiController`へリクエストを送信する。
3.  **ApiController**: Service/DAOを呼び出してデータを処理し、結果をJSON形式でレスポンスとして返す。
4.  **JavaScript**: 受け取ったJSONデータを元に、DOM操作で画面表示を動的に書き換える。

これにより、ページ全体の再読み込みなしに快適な操作が実現でき、現代的なWebアプリケーションの挙動を学ぶことができます。

## 4. リファクタリングの進め方（推奨ステップ）

影響範囲を最小限に抑えながら、安全にリファクタリングを進めるために、以下のステップを推奨します。

1.  **準備**: `pom.xml`に`jstl`の依存関係を追加する。
2.  **Step 1**: まずは1つの機能（例：社員一覧表示）を対象に、フロントコントローラーパターンを適用する。
3.  **Step 2**: 対象機能のJSPをJSTLとELを使って書き換える。
4.  **Step 3**: `try-with-resources`を導入し、DB接続部分をリファクタリングする。
5.  **Step 4**: 社員情報の部分更新など、小さな機能にJSON API + `fetch`を導入してみる。
6.  **Step 5**: 上記のサイクルを、他の機能にも順次適用していく。

## 5. 期待される効果

- **設計パターンの習得**: MVC、フロントコントローラー、DAOといった、フレームワークの基礎となっている重要な設計パターンを深く理解できる。
- **コード品質の向上**: 処理の重複が排除され、クラスの責務が明確になることで、コードの可読性、保守性、テスト容易性が向上する。
- **モダンな技術の学習**: JavaScriptによる非同期通信など、現代のWeb開発に不可欠な技術の基礎を実践的に学べる。
