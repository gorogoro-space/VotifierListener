# VotifierListener

NuVotifier の投票を受け取り、投票したプレイヤーにプレゼント(コンソールからのコマンド実行)を渡す Paper 用プラグイン(Paper 1.21.11 以降・Java 21 必須)。
リポジトリ: https://github.com/gorogoro-space/VotifierListener (GPL-3.0)

## 作業の進め方(必ず守ること)

- **設計が確定するまで実装しない。** 機能追加や仕様変更は、まず設計案(何を・なぜ・どう変えるか、影響範囲)を提示し、承認を得てからコードを書く。
- 判断が必要な点は、選択肢を示して質問する。勝手に決めない。
- やり取りは日本語で行う。新しく書くコードのコメントは日本語でよい。既存の英語のコメントは、頼まれない限り書き換えない。
- 変更は必要最小限にする。頼まれていないリファクタリングや機能追加はしない。
- 作業後は、変更・追加・削除したファイルの一覧と変更内容を報告する。
- 仕様を変えたら README.md と CLAUDE.md も合わせて更新する。
- `git push` の前には必ず確認を取る。コミットは意味のある単位で分ける。
- リポジトリへの初回の `git push` の前には、「GitHub で Watch 設定(Custom → Issues と Pull requests)はしましたか?」と日本語で確認する。自動 Watch は廃止されており、設定しないと issue や PR の通知が届かない。
- コミットメッセージや PR に `Co-Authored-By: Claude` などの署名を付けない。`.claude/` は Git に入れない(`.git/info/exclude` で除外する)。
- 実装中に設計の抜けや穴に気づいたら、黙って対処せず報告して相談する。
- プルリクエストをチェックするときは、次の観点で確認して報告する。
  - 変更概要(何を・なぜ変えているか)
  - 脆弱性(権限チェックの漏れ、入力値の検証、パスの扱い、コマンドに埋め込む文字列の検証など)
  - 安全面(データの破損・消失、再読み込み時の後始末、他プラグインの妨げなど)
  - 性能(TPS など)の低下(高頻度イベントでの重い処理、メインスレッドでの同期 I/O、定期タスクの追加など。「最重要の設計方針」に沿っているか)

## 環境

- Paper 1.21.11(`io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT`、`compileOnly`)/ `plugin.yml` の `api-version: '1.21.11'`(1.21.11 未満のサーバーでは読み込まれない)
- NuVotifier 2.7.2(jitpack の `com.github.NuVotifier:NuVotifier:2.7.2`、`compileOnly`)。`plugin.yml` の `depend: [Votifier]`(NuVotifier のプラグイン名は `Votifier`)
- **Java 21 必須。** `build.gradle.kts` の toolchain で JDK 21 を使い、Java 21 の class ファイルを出力する。勝手に変えない
- ビルド: Gradle(Kotlin DSL、wrapper 9.7.1)。`gradlew.bat clean build`(Windows)。**JDK 21 で実行する**(`JAVA_HOME` を `C:\Program Files\Java\jdk-21` にする。既定の `java` は JDK 27)。「ビルドして」と言われたら常にクリーンビルドする。成果物は `build/libs/VotifierListener-<version>.jar`
  - IntelliJ の「アーティファクトのビルド」はクラスファイルが入らないことがある。必ず Gradle でビルドする
- バージョンは `gradle.properties` の `version` だけで管理する。`plugin.yml` の `version: '${version}'` は `processResources` でビルド時に置き換わる
- `ChatColor` など非推奨の API を使っているため、コンパイル時に deprecation の警告が出る(Maven から Gradle に移したときからそのまま。直すのは頼まれたとき)
- パッケージ: `space.gorogoro.votifierlistener`(**すべて小文字**。大文字が混ざると plugin.yml の main と一致せず起動しない)
- 動作確認はサーバーを再起動して行う。PlugManX での読み込みは権限やコマンドの登録が不完全になることがある

## 最重要の設計方針

### TPS に影響させない
- 高頻度イベント(PlayerInteractEvent、PlayerMoveEvent、AsyncPlayerChatEvent など)を使うときは、安い判定を先に行い、対象外なら即座に抜ける
- BlockPhysicsEvent、VehicleMoveEvent など発生頻度が極端に高いイベントは使わない
- メインスレッドでファイルや DB の同期 I/O をしない(起動時・リロード時に一度だけ行う小さなファイルの読み込みは除く)
- 設定ファイルを追加する場合は、起動時・リロード時に一度だけ解析して保持する
- 定期タスクは最小限にし、追加するときは頻度と理由を設計案に書く

### 投票の安全性
- 投票のユーザー名は外部(投票サイト)から届き、`vote-command-list` のコマンドにそのまま埋め込まれる。ユーザー名の検証(英数字と `_`、統合版はプレフィックス付き)を緩めない
- 投票サイト名はそのまま保存せず、SHA-256 にして `offline-vote-list` に保存する

### データの保存
- `plugins/VotifierListener/config.yml` の `offline-vote-list` に、オフライン中の投票を `ユーザー名,投票サイト名の SHA-256` の形で保存する
- 現状は `saveConfig()` をメインスレッドで同期的に呼んでいる(投票時・ログイン時のみ。Maven から Gradle に移したときからそのまま。直すのは頼まれたとき)
- プラグインフォルダ以外には何も書き込まない

### 他プラグインとの関係
- 既存の機能(例: GSit、看板の click_event など)を妨げないこと。イベントをキャンセルする範囲は必要最小限にする

## 機能仕様

(現在の実装の動作。仕様を変えたらここを更新する)

- **投票時(`VotifierEvent`)**: ユーザー名を検証し、不正なら警告ログを出して何もしない
  - Java 版: `^[_a-zA-Z0-9]{3,16}$`
  - 統合版: config.yml の `bedrock-prefix`(既定 `.`。Floodgate の `username-prefix` に合わせる)で始まり、残りが英数字と `_` の 1 文字以上、全体で 16 文字以内。`bedrock-prefix` が空なら統合版の名前は受け付けない
  - オンラインの全員に `broadcast-message`(`%name%`、`%service%` を置換)を送る
  - 本人がオンライン(`getPlayerExact`)なら `vote-command-list` をコンソールから実行(`%name%` を置換)し、`vote-message` を送る
  - オフラインなら `offline-vote-list` に追加する(同じ行があれば追加しない)。名前順に並べ替えたあと、`offline-vote-limit-rows` を超えたら 先頭(名前順で最初)から削って上限の件数だけを残す(以前は `subList(1, 上限)` で、上限 − 1 件しか残らなかった)
- **ログイン時(`PlayerJoinEvent`)**: `offline-vote-list` から本人の名前の行を取り出し、行の数だけ `vote-command-list` を実行する。1 件以上あれば `offline-vote-message` を送って保存する
- 例外は `logStackTrace` でスタックトレースを警告ログに出す

## 過去にハマった点(他プロジェクトでの経験)

- `config.getString(path, "")` のように既定値を渡すと、jar 内 config.yml の既定値が参照されない。既定値なしで取得して null を判定すること
- plugin.yml で `default: true` にした権限でも、登録されないと Bukkit は「OP のみ」として扱う。全員向けの機能を権限で縛らない

## ファイル構成

- `src/main/java/space/gorogoro/votifierlistener/VotifierListener.java` — メインクラス。投票の受け取り、ログイン時のオフライン分の受け渡し、config.yml の保存をすべて持つ
- `src/main/resources/plugin.yml` — プラグイン定義
- `src/main/resources/config.yml` — メッセージ、実行するコマンド、統合版のプレフィックス、オフライン投票の記録
- `build.gradle.kts` / `settings.gradle.kts` / `gradle.properties` — Gradle の設定(バージョンは `gradle.properties`)
