package yandex.cloud.speechkit.examples;

import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import speechkit.common.v3.Common;
import syandex.cloud.api.ai.tts.v3.Tts;
import yandex.cloud.api.ai.tts.v3.SynthesizerGrpc;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TtsV3Client {

    private final SynthesizerGrpc.SynthesizerStub client;

    public TtsV3Client(String host, int port, String apikey) {
        client = ttsV3Client(host, port, apikey);
    }

    public void synthesize(String text, File output) throws UnsupportedAudioFileException, IOException {

        // tts request with synthesis settings and text
        var request = Tts.UtteranceSynthesisRequest
                .newBuilder()
                .setText(text)
                .setOutputAudioSpec(Common.AudioFormatOptions
                        .newBuilder()
                        .setContainerAudio(Common.ContainerAudio
                                .newBuilder()
                                .setContainerAudioType(Common.ContainerAudio.ContainerAudioType.WAV)
                                .build()))
                .setLoudnessNormalizationType(Tts.UtteranceSynthesisRequest.LoudnessNormalizationType.LUFS)
                .build();

        // for one directional streaming we provide an instance of response observer
        var observer = new TtsStreamObserver();

        System.out.println("Sending ... ");

        // send request
        client.utteranceSynthesis(request, observer);

        // wait for response to come
        var bytes = observer.awaitResult(5000);

        // create audio stream with default settings
        var audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes));

        // write results to file
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, output);
    }

    static class TtsStreamObserver implements StreamObserver<Tts.UtteranceSynthesisResponse> {

        ByteArrayOutputStream result = new ByteArrayOutputStream();

        private static CountDownLatch count = new CountDownLatch(1);

        @Override
        public void onNext(Tts.UtteranceSynthesisResponse response) {

            if (response.hasAudioChunk()) {
                try {
                    result.write(response.getAudioChunk().getData().toByteArray());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        @Override
        public void onError(Throwable t) {
            System.out.println("Tts streaming error occurred " + t);
            t.printStackTrace();
        }

        @Override
        public void onCompleted() {
            System.out.println("Tts stream completed");
            count.countDown();
        }

        byte[] awaitResult(int timeoutSeconds) {
            try {
                count.await(timeoutSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return result.toByteArray();
        }
    }

    private static SynthesizerGrpc.SynthesizerStub ttsV3Client(String host, int port, String apiKey) {
        var channel = ManagedChannelBuilder
                .forAddress(host, port)
                .build();

        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), "Api-Key " + apiKey);
        var requestId = UUID.randomUUID().toString();
        headers.put(Metadata.Key.of("x-client-request-id", Metadata.ASCII_STRING_MARSHALLER), requestId);

        return SynthesizerGrpc.newStub(channel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException {

        var outputLocation = "result.wav";
        var text = "здравствуйте глеб меня зовут анастасия чем я могу вам помочь";
        if (args.length > 0) {
            // get the wav file from arguments
            text = args[0];
        }
        // $API_KEY env must be set
        var apikey = System.getenv("API_KEY");


        var client = new TtsV3Client("tts.api.cloud.yandex.net", 443, apikey);

        client.synthesize(text, new File("result.wav"));

    }
}
