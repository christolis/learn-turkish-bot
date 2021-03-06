![Logo](assets/logo.jpg)

![Travis Status](https://app.travis-ci.com/ChristolisOfficial/learn-turkish-bot.svg?token=qPTycAnPYpHQrM4QJxMb&branch=main)

The **Learn Turkish Bot** is a Discord bot that was created for a server called Learn English and it primarily helped with automating tasks during the 2022 April Fools Day.

## :pencil: Features
- Automatically translates text entries in a Discord server into a different language with the use of a specially-crafted text file!
- Backs up the original translations directly from the server in order to revert to them at any time the bot is instructed to do so!

## :inbox_tray: Installation
To run the Discord bot from your own machine, you will need a version of the Java SDK 8+ or higher. Since the project is making use of Gradle, you will have to clone the project and execute `./gradlew build -Penv=dev` inside its root folder to get the development version JAR. To get the production one, use `./gradlew build -Penv=prod`.

Keep in mind that you will have to make two copies of the sample configuration file (`config.sample.json`) located in the main resources directory. One should be called `dev-config.json` that will be used in the development version and the other should be called `prod-config.json` for the production version.

## :scroll: License
Learn Turkish Bot is published under the Apache 2.0 license. For more information, read the [LICENSE](https://github.com/christolisofficial/learn-turkish-bot/blob/main/LICENSE) file.
