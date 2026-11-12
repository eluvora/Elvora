package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Zero-budget, offline crisp tactile card-flip tap sound generator.
 * Pre-renders a subtle 55ms soft wooden/paper tap waveform at 44.1kHz so FlipCard taps
 * produce a pleasant audible feedback regardless of system touch-sound settings.
 */
object TapSoundPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private const val SAMPLE_RATE = 44100

    private val tapPcm: ShortArray by lazy {
        val durationSec = 0.055
        val count = (SAMPLE_RATE * durationSec).toInt()
        val data = ShortArray(count)
        var phaseLow = 0.0
        var phaseHigh = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val attack = (t / 0.002).coerceAtMost(1.0)
            // Soft pitch drop (320 Hz -> 110 Hz) gives a warm tactile card-tap thock
            val freqLow = 110.0 + 210.0 * exp(-t * 85.0)
            phaseLow += 2.0 * PI * freqLow / SAMPLE_RATE
            phaseHigh += 2.0 * PI * 1350.0 / SAMPLE_RATE

            val body = 0.78 * sin(phaseLow) * exp(-t * 72.0)
            val crisp = 0.22 * sin(phaseHigh) * exp(-t * 210.0)
            val sample = ((body + crisp) * attack * 0.42 * Short.MAX_VALUE)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            data[i] = sample.toShort()
        }
        data
    }

    fun playCardTap() {
        scope.launch {
            runCatching {
                val samples = tapPcm
                val byteCount = samples.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(byteCount)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()
                delay(90L)
                runCatching {
                    track.stop()
                    track.release()
                }
            }
        }
    }
}
