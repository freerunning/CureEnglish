package com.mm.cureenglish;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * author:       dingyu
 * date:         20-4-2
 */
public class AudioRecorder {
    private static final String TAG = "AudioRecorder";

    private static AudioRecorder sAudioRecorder;

    public final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    public final static int AUDIO_SAMPLE_RATE = 48000;
    public final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    public final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private int bufferSizeInBytes = 0;
    private AudioRecord audioRecord;

    public interface IRecordListener {
        void onStarted();
        void onStopped();
        void onAudioDataAvailable(byte[] data, int len);
    }

    private IRecordListener listener;

    private AudioRecorder() {
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING);
        audioRecord = new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, bufferSizeInBytes);
    }

    public void setListener(IRecordListener listener) {
        this.listener = listener;
    }

    public static AudioRecorder getInstance() {
        if (sAudioRecorder == null) {
            sAudioRecorder = new AudioRecorder();
        }
        return sAudioRecorder;
    }

    public void start() {
        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            return;
        }

        audioRecord.startRecording();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int readsize = 0;
                if (listener != null) {
                    listener.onStarted();
                }

                while (audioRecord != null) {
                    if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                        break;
                    }

                    byte[] audiodata = new byte[bufferSizeInBytes];
                    readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);

                    if (readsize <= 0) {
                        Log.e(TAG, "read size is " + readsize);
                        break;
                    }
                    if (listener != null) {
                        listener.onAudioDataAvailable(audiodata, readsize);
                    }
                }

                if (listener != null) {
                    listener.onStopped();
                }
            }
        }).start();
    }

    public void stop() {
        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop();
        }
    }
}
