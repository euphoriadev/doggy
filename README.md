# Doggy
[![Doggy](https://img.shields.io/badge/build-passed-brightgreen)](https://github.com/euphoriadev/doggy)

Помощник с большим функционалом для вашей страницы ВКонтакте

<a href="https://play.google.com/store/apps/details?id=ru.euphoria.doggy">
<img src="https://play.google.com/intl/en_us/badges/static/images/badges/ru_badge_web_generic.png" alt="Download on Google Play" height="100" /><
</a>

## Возможности
* Музыка с Вашей странице
* Полной анализ диалога
* Восстановление чата
* Карта фотографий
* Очистка от собачек (мертвых страниц)
* Очистка групп
* Дата регистрации
* Перевод голосовых сообщений в текст
* Вечный онлайн

## А как собрать проект?
1. Импортируйте проект в [Android Studio](https://developer.android.com/studio)
2. Добавьте `KEY_ALIAS`, `KEY_PASSWORD`, `KEYSTORE_PASSWORD`, `KEYSTORE_FILE` в `gradle.properties` для release сборки
3. Нажмите Run
4. Профит

Так же приложение использует сторонние сервисы (SpeechKit, AppMetrica, ...) для которых желательно получить ключи доступа.

Их необходимо добавить в [Tokens.java](https://github.com/euphoriadev/doggy/blob/master/app/src/main/java/ru/euphoria/doggy/common/Tokens.java)

## Контакты
У нас есть [Telegram Канал](https://t.me/euphoria_devs) на который вы можете подписаться

## Лицензия

Doggy выпускается под лицензией **GNU General Public License v3.0 (GPLv3)**, которую можно найти в файле `LICENSE` в корне этого проекта
