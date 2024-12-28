### jl
Java Library

---
### サーブレット及びJSPを使った、Webアプリケーションです。

---
### 本ソフトを使用するための前提事項
- Windows上で動かすことができます。
- eclipseが必要です。(Pleiadesの新しい版を推奨します)
- Java17が必要です。(Pleiadesに付属のもので構いません)
- Tomcat10が必要です。(Pleiadesに付属のもので構いません)
- firefoxが必要です。
- MySQLを使う場合はdockerもしくはローカル上にインストールされたMySQLが必要です。  
(本資料ではdockerによるMySQL構築をご紹介します)

---
### 使い方

- ローカルに[jl]をクローンしてください。
```
[GitBash]
git clone https://github.com/freedomRemains/jl
```
- 次のフォルダを作成してください。  
```
C:\local\project\remainz
```
- 配下の構成は、次の通りとしてください。
```
C:\local\project\remainz
├─dbmng
│  ├─h2
│  │  ├─10_dbdef
│  │  │  ├─10_authorized
│  │  │  ├─20_auto_created
│  │  │  └─30_for_update
│  │  ├─20_dbdata
│  │  │  ├─10_authorized
│  │  │  ├─20_auto_created
│  │  │  └─30_for_update
│  │  └─30_sql
│  │      ├─10_authorized
│  │      ├─20_auto_created
│  │      └─30_for_update
│  └─mysql
│      ├─10_dbdef
│      │  ├─10_authorized
│      │  ├─20_auto_created
│      │  └─30_for_update
│      ├─20_dbdata
│      │  ├─10_authorized
│      │  ├─20_auto_created
│      │  └─30_for_update
│      └─30_sql
│          ├─10_authorized
│          ├─20_auto_created
│          └─30_for_update
└─log
```
- ディレクトリ構成を作るコマンドは、次の通りです。
```
mkdir C:\local\project\remainz
cd C:\local\project\remainz
mkdir dbmng
mkdir dbmng\h2
mkdir dbmng\h2\10_dbdef
mkdir dbmng\h2\10_dbdef\10_authorized
mkdir dbmng\h2\10_dbdef\20_auto_created
mkdir dbmng\h2\10_dbdef\30_for_update
mkdir dbmng\h2\20_dbdata
mkdir dbmng\h2\20_dbdata\10_authorized
mkdir dbmng\h2\20_dbdata\20_auto_created
mkdir dbmng\h2\20_dbdata\30_for_update
mkdir dbmng\h2\30_sql
mkdir dbmng\h2\30_sql\10_authorized
mkdir dbmng\h2\30_sql\20_auto_created
mkdir dbmng\h2\30_sql\30_for_update
mkdir dbmng\mysql
mkdir dbmng\mysql\10_dbdef
mkdir dbmng\mysql\10_dbdef\10_authorized
mkdir dbmng\mysql\10_dbdef\20_auto_created
mkdir dbmng\mysql\10_dbdef\30_for_update
mkdir dbmng\mysql\20_dbdata
mkdir dbmng\mysql\20_dbdata\10_authorized
mkdir dbmng\mysql\20_dbdata\20_auto_created
mkdir dbmng\mysql\20_dbdata\30_for_update
mkdir dbmng\mysql\30_sql
mkdir dbmng\mysql\30_sql\10_authorized
mkdir dbmng\mysql\30_sql\20_auto_created
mkdir dbmng\mysql\30_sql\30_for_update
mkdir log
```
- eclipseにて[インポート]-[Gradle]-[既存のGradleプロジェクト]で[jl]を取り込めます。  
(単独のJavaプロジェクトとして取り込むことができます)
- [jl]のプロパティから[リソース]-[テキスト・ファイル・エンコード]と進み、[その他]-[UTF-8]を選択すると、  
eclipseプロジェクトの警告が消えます。
- [jl]のプロパティから[デプロイメント・アセンブリー]-[追加]で、[フォルダ]を追加し、[WebContent]を  
選択してください。([ソース]が[/WebContent]、[デプロイ・パス]が[/]となっていることを確認します)  
※　この設定がないとJSPを認識できず、プログラムを起動してもブラウザからアクセスできません。
- [サーバー]で[Tomcat10]-[Java17]を選択して[jl]を起動します。
- firefoxを起動して次のURLを入力し、画面が表示されたら起動成功です。
```
http://localhost:8080/jl/service/top.html
```
- [マイページ]をクリックし、[メールアドレス]と[パスワード]は変えずにそのまま[送信]でログインできます。
- [DBメンテナンス]をクリックし、一番下にある[DB構成取得]を実行すると、次の位置にDB資材が出現します。
```
C:\local\project\remainz\dbmng\h2\10_dbdef\20_auto_created
C:\local\project\remainz\dbmng\h2\20_dbdata\20_auto_created
C:\local\project\remainz\dbmng\h2\30_sql\20_auto_created
```
- [10_dbdef]配下の[tableNameList.txt]、[20_dbdata]配下の[MTBLDEF.txt]を編集し、  
[10_dbdef]配下の定義ファイル、[20_dbdata]配下のデータファイルを整合させ、  
[DBメンテナンス]の[DB構成更新]を実行すると、編集内容通りにDBの定義やデータを変更できます。
- 現状は単なる使いづらいDB管理ツールですが、URLパターンや画面部品などを組み合わせると  
コードをほとんど書かずにWebアプリを作れるローコードのツールになります。  
本プロジェクトはその原型です。
- 次のGradleコマンドでビルド及び全テストが可能です。
```
cd [jlをクローンしたディレクトリ]
chcp 65001
set JAVA_HOME=[Javaの位置(Pleiades配下のJavaの位置で構いません)]
set JAVA_OPTS=-Dfile.encoding=UTF-8
gradlew clean
gradlew build
＜例＞
cd C:\local\project\jl
chcp 65001
set JAVA_HOME=C:\eclipse_plaiades\java\17
set JAVA_OPTS=-Dfile.encoding=UTF-8
gradlew clean
gradlew build
```
- Gradleでビルド及び全テストを実行すると、カバレッジやテスト結果を確認することができます。
```
jl\build\reports\jacoco\test\html\index.html
jl\build\reports\spotbugs\main.html
jl\build\reports\tests\test\index.html
```
