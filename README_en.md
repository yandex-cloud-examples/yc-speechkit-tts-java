# Synthesizing speech with Yandex SpeechKit API v3

This is an example of using Yandex SpeechKit API v3 to synthesize speech.

For documentation, refer to [this section](https://cloud.yandex.ru/docs/speechkit/tts/api/tts-examples-v3).

## Usage

Creating a build:

`mvn clean install`

`cd target`

Save the API key of your service account into the `$API_KEY` environment variable.

`API_KEY=<apikey>`

To start the example, run the following command:

`java -cp speechkit_examples-1.0-SNAPSHOT.jar yandex.cloud.speechkit.examples.TtsV3Client "<text>"`

The synthesis result will be saved to the `result.wav` file.
