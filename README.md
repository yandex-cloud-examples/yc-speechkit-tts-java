# Синтез речи с помощью Yandex SpeechKit API v3

Пример использования Yandex SpeechKit API v3 на Java для синтеза речи.

Документация: https://cloud.yandex.ru/docs/speechkit/tts/api/tts-examples-v3

## Использование

Сборка:

`mvn clean install`

`cd target`

В переменную окружения `$API_KEY` сохраните API-ключ сервисного аккаунта.

`API_KEY=<apikey>`

Чтобы запустить пример, выполните команду:

`java -cp speechkit_examples-1.0-SNAPSHOT.jar yandex.cloud.speechkit.examples.TtsV3Client "<text>"`

Результат синтеза будет сохранен в файле `result.wav`.
