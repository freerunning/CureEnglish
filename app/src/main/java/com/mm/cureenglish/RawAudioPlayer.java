package com.mm.cureenglish;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

import static com.mm.cureenglish.AudioRecorder.AUDIO_ENCODING;
import static com.mm.cureenglish.AudioRecorder.AUDIO_SAMPLE_RATE;

/**
 * author:       dingyu
 * date:         20-4-2
 */
public class RawAudioPlayer {
    AudioTrack audioTrack;

    public void init() {
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AUDIO_ENCODING);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AUDIO_ENCODING, bufferSizeInBytes, AudioTrack.MODE_STREAM);

        audioTrack.play();
    }

    public void write(byte[] date, int len) {
        audioTrack.write(date, 0, len);
    }
}
