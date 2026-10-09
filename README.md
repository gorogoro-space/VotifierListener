# VotifierListener
[![Paper 1.21.11](https://img.shields.io/badge/Paper-1.21.11-brightgreen.svg)](https://fill-ui.papermc.io/projects/paper/version/1.21.11)
[![GitHub release](https://img.shields.io/github/release/gorogoro-space/VotifierListener.svg)](https://github.com/gorogoro-space/VotifierListener/releases)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/gorogoro-space/VotifierListener/issues)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/gorogoro-space/VotifierListener/blob/main/LICENSE)

This plugin gives rewards to players who voted for the server, using NuVotifier.

投票サイトでサーバーに投票したプレイヤーに、NuVotifier を通じてプレゼント(コマンドの実行)を渡すプラグインです。

# Requirements
- Paper 1.21.11
- Java 21
- NuVotifier(プラグイン名 `Votifier`)

# Installation method
Please place the .jar file in the Paper plugins folder.

.jar ファイルを Paper の plugins フォルダに置き、サーバーを再起動してください。

# Features
- 投票があると、オンラインの全員に `broadcast-message` を表示します
- 投票した人がオンラインなら、`vote-command-list` のコマンドをコンソールから実行し、本人に `vote-message` を表示します
- オフラインなら `offline-vote-list` に記録し、次にログインしたときにコマンドを実行して `offline-vote-message` を表示します
  - 同じ人・同じ投票サイトの記録は 1 件にまとめます
  - 記録は最大 `offline-vote-limit-rows` 件です
- 統合版(Floodgate)のプレイヤーにも対応しています。ユーザー名の先頭に `bedrock-prefix`(既定 `.`)が付いた名前を受け付けます

# Config
`plugins/VotifierListener/config.yml`
- `vote-message` — 投票した本人に表示するメッセージ
- `broadcast-message` — 全員に表示するメッセージ(`%name%` はユーザー名、`%service%` は投票サイト名に置き換わります)
- `vote-command-list` — 投票した人に実行するコマンド(`%name%` はユーザー名に置き換わります)
- `offline-vote-message` — オフライン中の投票分を渡したときに表示するメッセージ
- `bedrock-prefix` — 統合版のユーザー名の先頭に付く文字。Floodgate の `username-prefix` と合わせてください。空にすると統合版の名前を受け付けません
- `offline-vote-limit-rows` — オフライン中の投票を記録する最大件数
- `offline-vote-list` — オフライン中の投票の記録(プラグインが書き込みます)

# Disclaimer
Do not assume any responsibility by use. Please use it at your own risk.

## ビルド

JDK 21 が必要です。Gradle はラッパー(`gradlew`)が自動でダウンロードするので、別途インストールする必要はありません。

```
gradlew.bat clean build
```

(macOS / Linux では `./gradlew clean build`)

## IntelliJ IDEA でのビルド手順

本プロジェクトはビルドツールに Gradle を使用しています。
IntelliJ IDEA 上で正しくプラグイン（JARファイル）を生成するには、以下の手順を実行してください。
「ビルド → アーティファクトのビルド」はクラスファイルが入らないことがあるので使わないでください。

### 🛠️ ビルド手順

1. IntelliJ IDEA の画面右端にある **「Gradle」タブ** をクリックして開きます。
2. プロジェクト名（VotifierListener）を展開し、 **`Tasks`** ツリーを開きます。
3. リスト内にある **`clean`** をダブルクリックして実行します（古いビルドキャッシュを削除します）。
4. 続けてリスト内にある **`build`** をダブルクリックして実行します。

### 📦 生成されたファイルの場所
ビルドが成功すると、プロジェクトのルート直下に `build/libs` フォルダが作成（または更新）され、その中に中身の詰まった正しい JAR ファイルが生成されます。

* **生成先:** `build/libs/VotifierListener-<version>.jar`(例: `VotifierListener-1.2.jar`。バージョンは `gradle.properties` の `version`)

この JAR ファイルを Minecraft サーバーの `plugins` フォルダに配置してください。

## 開発(IntelliJ IDEA で Claude Code を使う)

このリポジトリには、AI コーディングツール [Claude Code](https://docs.claude.com/ja/docs/claude-code/overview) 向けの作業ルールを書いた `CLAUDE.md` があります。IntelliJ IDEA で使う手順は次のとおりです。

1. Claude の有料プラン(Pro / Max)か、Anthropic API のアカウントを用意します。
2. Windows では、先に [Git for Windows](https://git-scm.com/downloads/win) をインストールします。
3. Claude Code 本体をインストールします。Windows では PowerShell で次を実行します。
   ```
   irm https://claude.ai/install.ps1 | iex
   ```
4. IntelliJ IDEA の「設定 → プラグイン → Marketplace」で「Claude Code」を検索してインストールし、IDE を再起動します。検索結果には Anthropic 以外が作った似た名前のプラグインも表示されます。プラグイン名の下に書かれた提供元が「Anthropic」になっているものを選んでください。
5. プロジェクトを開いて、ターミナルで `claude` を実行します(`Ctrl+Esc` でも起動できます)。初回はブラウザでログインします。

起動すると `CLAUDE.md` が自動で読み込まれ、このプロジェクトの設計方針に沿って作業します。

## ライセンス

[LICENSE](LICENSE) を参照してください。

## kubotan へのメモ

リポジトリを新規作成したら、**必ず Watch を設定すること**(忘れない!)。
GitHub の自動 Watch 機能は 2025 年 5 月に廃止されたため、設定しないと他の人が立てた issue や PR の通知が届かない。

1. リポジトリのページ右上の **「Watch」** を押す
2. **「Custom」** を選び、**Issues** と **Pull requests** にチェックを入れる
