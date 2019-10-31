package com.example.myapplication;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class AudioRecorder {

    private String LOG_TAG="";
    private static final int SAMPLE_RATE = 22050;
    private static final int CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private boolean isRecording = false;
    private int bufferSize;
    private Thread recordingThread = null;
    private byte[] audioBuffer;
    public boolean serviceIsDestroyed;


    public AudioRecorder() {
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNELS, ENCODING);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        audioBuffer = new byte[bufferSize]; // container for read values
        serviceIsDestroyed = false;
    }

    public void start(File file) {
        final File audioFile = file;
        if (serviceIsDestroyed) {return;}
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isRecording = true;

                recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE,
                        CHANNELS, ENCODING, bufferSize);

                if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e(LOG_TAG, "Failed to initialize the audio record");
                    return;
                }

                if (serviceIsDestroyed) {return;}
                recorder.startRecording();

                FileOutputStream outputStream;

                try {
                    outputStream = new FileOutputStream(audioFile);
                    // write wav header file
                    writeWavHeader(outputStream, CHANNELS, SAMPLE_RATE, ENCODING);

                    System.out.println("Audio: " + Thread.currentThread().getName());

                    // write audio samples into the file
                    while (isRecording) {
                        writeAudioDataToFile(outputStream);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                stop();
            }
        });
        recordingThread.start();
    }

    public void stop() {
        isRecording = false;
        try {
            if (recorder != null) {
                recorder.stop();
                if (recorder != null) {
                    recorder.release();
                }
            }
        } catch(IllegalStateException e) {
            System.out.println(e.toString());
            System.out.println("Error in Audio Recorder: stopRecording");
        }
        recorder = null;
        recordingThread = null;
    }


    private void writeAudioDataToFile(FileOutputStream outputStream) {

        int read = recorder.read(audioBuffer, 0, audioBuffer.length);
        if (read == AudioRecord.ERROR_INVALID_OPERATION ||
                read == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(LOG_TAG, "Error reading audio data!");
            return;
        }

        try {
            outputStream.write(audioBuffer, 0, read);
        } catch (Exception e) {
            System.out.println("Error in Audio Recorder");
        e.printStackTrace();
        }
    }


    private static void writeWavHeader(OutputStream out, int channelMask, int sampleRate, int encoding) throws IOException {
        short channels;
        switch (channelMask) {
            case AudioFormat.CHANNEL_IN_MONO:
                channels = 1;
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
                channels = 2;
                break;
            default:
                throw new IllegalArgumentException("Unacceptable channel mask");
        }

        short bitDepth;
        switch (encoding) {
            case AudioFormat.ENCODING_PCM_8BIT:
                bitDepth = 8;
                break;
            case AudioFormat.ENCODING_PCM_16BIT:
                bitDepth = 16;
                break;
            case AudioFormat.ENCODING_PCM_FLOAT:
                bitDepth = 32;
                break;
            default:
                throw new IllegalArgumentException("Unacceptable encoding");
        }

        writeWavHeader(out, channels, sampleRate, bitDepth);
    }

    private static void writeWavHeader(OutputStream out, short channels, int sampleRate, short bitDepth) throws IOException {
        // Convert the multi-byte integers to raw bytes in little endian format as required by the spec
        byte[] littleBytes = ByteBuffer
                .allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(channels)
                .putInt(sampleRate)
                .putInt(sampleRate * channels * (bitDepth / 8))
                .putShort((short) (channels * (bitDepth / 8)))
                .putShort(bitDepth)
                .array();

        // Not necessarily the best, but it's very easy to visualize this way
        out.write(new byte[]{
                // RIFF header
                'R', 'I', 'F', 'F', // ChunkID
                0, 0, 0, 0, // ChunkSize (must be updated later)
                'W', 'A', 'V', 'E', // Format
                // fmt subchunk
                'f', 'm', 't', ' ', // Subchunk1ID
                16, 0, 0, 0, // Subchunk1Size
                1, 0, // AudioFormat
                littleBytes[0], littleBytes[1], // NumChannels
                littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5], // SampleRate
                littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9], // ByteRate
                littleBytes[10], littleBytes[11], // BlockAlign
                littleBytes[12], littleBytes[13], // BitsPerSample
                // data subchunk
                'd', 'a', 't', 'a', // Subchunk2ID
                0, 0, 0, 0, // Subchunk2Size (must be updated later)
        });
    }

}
