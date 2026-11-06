package com.example.soundboard

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.sin

class ToneGeneratorHelper {

    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    var currentFrequency: Int = 440

    fun startTone(frequency: Int) {
        if (isPlaying) stopTone()

        currentFrequency = frequency
        isPlaying = true

        val sampleRate = 44100
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        // Synthesize sine wave samples on a background thread
        thread {
            val samples = ShortArray(bufferSize)
            var sampleIndex = 0

            while (isPlaying) {
                for (i in samples.indices) {
                    val angle = 2.0 * Math.PI * sampleIndex * currentFrequency / sampleRate
                    samples[i] = (sin(angle) * Short.MAX_VALUE).toInt().toShort()
                    sampleIndex++
                }
                audioTrack?.write(samples, 0, samples.size)
            }
        }
    }

    fun updateFrequency(newFreq: Int) {
        currentFrequency = newFreq
    }

    fun stopTone() {
        isPlaying = false
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}