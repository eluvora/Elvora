package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Zero-budget, 100% original dreamy romantic retro 1960s indie-pop instrumental (44.1kHz PCM).
 * Features a swaying ~68 BPM romantic chord progression (Fmaj9 -> Am7/D7b9 -> Gm9 -> C13sus4/C7b9),
 * tender lyrical hook with slow portamento & vibrato, chorus-drenched electric guitar arpeggios,
 * warm melodic bass, hazy analog pad, subtle psychedelic reverse-shimmer, soft brushed vintage drums,
 * and vintage vinyl warmth—pre-rendered once for zero playback lag.
 */
class AmbientSynthPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var playbackJob: Job? = null
    private var audioTrack: AudioTrack? = null

    @Volatile
    private var isMuted: Boolean = true

    @Volatile
    private var volume: Float = 0.35f

    private val sampleRate = 44100

    private data class NoteStep(
        val stepOffset: Int,
        val freqHz: Double,
        val velocity: Double,
        val decayRate: Double
    )

    private data class LeadHookNote(
        val startStep: Int,
        val lengthSteps: Int,
        val freqHz: Double,
        val gain: Double = 1.0
    )

    private class PluckedVoice(
        var freqHz: Double = 0.0,
        var velocity: Double = 0.0,
        var decay1Mul: Double = 0.999,
        var decay2Mul: Double = 0.998,
        var decay3Mul: Double = 0.996,
        var env1: Double = 0.0,
        var env2: Double = 0.0,
        var env3: Double = 0.0,
        var phase1: Double = 0.0,
        var phase2: Double = 0.0,
        var phase3: Double = 0.0,
        var chorusPhase: Double = 0.0,
        var ageSamples: Int = 0,
        var active: Boolean = false
    )

    // Tender, glowing romantic 1960s progression: Fmaj9 -> Am7 -> Gm9 -> C9sus4
    private val padChords: List<DoubleArray> = listOf(
        doubleArrayOf(174.61, 220.00, 261.63, 329.63, 392.00), // Fmaj9 (warm romantic glow)
        doubleArrayOf(220.00, 261.63, 329.63, 392.00, 493.88), // Am9 (tender sweet lift)
        doubleArrayOf(196.00, 233.08, 293.66, 349.23, 440.00), // Gm9 (bittersweet longing)
        doubleArrayOf(130.81, 233.08, 293.66, 329.63, 392.00)  // C9sus4 -> C13 romantic melt
    )

    // Warm melodic 1960s flatwound bassline (flowing pocket, not dragged)
    private val bassLine: List<NoteStep> = listOf(
        // Bar 1: Fmaj9
        NoteStep(0, 87.31, 0.90, 2.0), NoteStep(6, 87.31, 0.72, 2.3),
        NoteStep(8, 130.81, 0.82, 2.0), NoteStep(12, 110.00, 0.76, 2.1), NoteStep(14, 98.00, 0.70, 2.4),
        // Bar 2: Am9
        NoteStep(16, 110.00, 0.88, 2.0), NoteStep(22, 110.00, 0.72, 2.3),
        NoteStep(24, 164.81, 0.80, 2.0), NoteStep(28, 130.81, 0.76, 2.1), NoteStep(30, 103.83, 0.68, 2.4),
        // Bar 3: Gm9
        NoteStep(32, 98.00, 0.90, 2.0), NoteStep(38, 146.83, 0.74, 2.2),
        NoteStep(40, 196.00, 0.82, 2.0), NoteStep(44, 174.61, 0.76, 2.2), NoteStep(46, 116.54, 0.70, 2.4),
        // Bar 4: C9sus4 -> C7
        NoteStep(48, 130.81, 0.90, 2.0), NoteStep(54, 98.00, 0.74, 2.2),
        NoteStep(56, 116.54, 0.80, 2.0), NoteStep(60, 110.00, 0.74, 2.2), NoteStep(62, 82.41, 0.72, 2.4)
    )

    // Lush chorus clean electric guitar arpeggios & sweet chord kisses
    private val guitarNotes: List<NoteStep> = listOf(
        // Bar 1: Fmaj9
        NoteStep(0, 174.61, 0.56, 1.25), NoteStep(0, 261.63, 0.52, 1.30), NoteStep(0, 329.63, 0.56, 1.25),
        NoteStep(3, 392.00, 0.48, 1.50), NoteStep(6, 440.00, 0.52, 1.45), NoteStep(10, 329.63, 0.48, 1.50), NoteStep(12, 523.25, 0.50, 1.45),
        // Bar 2: Am9
        NoteStep(16, 220.00, 0.56, 1.25), NoteStep(16, 329.63, 0.50, 1.30), NoteStep(16, 493.88, 0.54, 1.25),
        NoteStep(19, 392.00, 0.48, 1.50), NoteStep(22, 440.00, 0.52, 1.45), NoteStep(26, 329.63, 0.48, 1.50), NoteStep(28, 493.88, 0.50, 1.45),
        // Bar 3: Gm9
        NoteStep(32, 196.00, 0.56, 1.25), NoteStep(32, 293.66, 0.50, 1.30), NoteStep(32, 349.23, 0.54, 1.25),
        NoteStep(35, 440.00, 0.48, 1.50), NoteStep(38, 466.16, 0.52, 1.45), NoteStep(42, 349.23, 0.48, 1.50), NoteStep(44, 440.00, 0.50, 1.45),
        // Bar 4: C9sus4 -> C13
        NoteStep(48, 130.81, 0.56, 1.25), NoteStep(48, 261.63, 0.50, 1.30), NoteStep(48, 349.23, 0.54, 1.25),
        NoteStep(51, 392.00, 0.48, 1.50), NoteStep(54, 440.00, 0.52, 1.45), NoteStep(58, 329.63, 0.50, 1.45), NoteStep(60, 392.00, 0.50, 1.40)
    )

    // Sweeter, more romantic & less dragged catchy 1960s instrumental hook (with crisp breathing spaces)
    private val hookMelody: List<LeadHookNote> = listOf(
        // Bar 1 (Fmaj9): heart-warming major-7th motif
        LeadHookNote(0, 3, 440.00, 0.88),  // A4
        LeadHookNote(3, 3, 523.25, 0.90),  // C5
        LeadHookNote(6, 4, 659.25, 0.96),  // E5 (romantic major 7th)
        LeadHookNote(11, 3, 587.33, 0.86), // D5 (step 14-15 natural breath)
        // Bar 2 (Am9): tender rising response
        LeadHookNote(16, 3, 523.25, 0.88), // C5
        LeadHookNote(19, 3, 659.25, 0.92), // E5
        LeadHookNote(22, 4, 783.99, 0.96), // G5
        LeadHookNote(27, 4, 659.25, 0.86), // E5 (step 31 breath)
        // Bar 3 (Gm9): affectionate falling phrase
        LeadHookNote(32, 3, 698.46, 0.94), // F5
        LeadHookNote(35, 3, 587.33, 0.88), // D5
        LeadHookNote(38, 4, 466.16, 0.90), // Bb4
        LeadHookNote(43, 4, 440.00, 0.85), // A4
        // Bar 4 (C9sus4 -> C7): sweet resolving whisper back to Fmaj9
        LeadHookNote(48, 3, 392.00, 0.88), // G4
        LeadHookNote(51, 3, 440.00, 0.90), // A4
        LeadHookNote(54, 4, 466.16, 0.92), // Bb4
        LeadHookNote(59, 3, 329.63, 0.84)  // E4 (clean 2-step pause before loop)
    )

    private val sinLutSize = 4096
    private val sinLutMask = sinLutSize - 1
    private val sinScale = sinLutSize.toDouble() / (2.0 * PI)
    private val sinLut = DoubleArray(sinLutSize) { i -> sin(2.0 * PI * i / sinLutSize) }

    private fun fastSin(rad: Double): Double {
        val idx = (rad * sinScale).toInt() and sinLutMask
        return sinLut[idx]
    }

    private val loopPcm: ShortArray by lazy { renderSeamlessRetroLoop() }

    private fun renderSeamlessRetroLoop(): ShortArray {
        val stepSeconds = 0.182 // Flowing ~82 BPM romantic retro groove (neither rushed nor dragged)
        val stepSamples = (sampleRate * stepSeconds).toInt()
        val totalSteps = 64
        val loopLen = stepSamples * totalSteps
        val crossfadeLen = 1024
        val totalRenderSamples = (loopLen * 2) + crossfadeLen
        val dt = 1.0 / sampleRate

        val outLoop = ShortArray(loopLen)
        val tailOverlap = DoubleArray(crossfadeLen)

        val guitarPool = Array(14) { PluckedVoice() }
        var nextGuitarIdx = 0

        val bassByStep = arrayOfNulls<NoteStep>(totalSteps)
        bassLine.forEach { bassByStep[it.stepOffset] = it }

        val guitarByStep = Array(totalSteps) { mutableListOf<NoteStep>() }
        guitarNotes.forEach { guitarByStep[it.stepOffset].add(it) }

        val hookByStep = arrayOfNulls<LeadHookNote>(totalSteps)
        val hookIsStart = BooleanArray(totalSteps)
        for (h in hookMelody) {
            for (s in h.startStep until (h.startStep + h.lengthSteps).coerceAtMost(totalSteps)) {
                hookByStep[s] = h
                if (s == h.startStep) hookIsStart[s] = true
            }
        }

        var bassPhase = 0.0
        var bassCurrFreq = 87.31
        var bassTargetFreq = 87.31
        var bassEnv = 0.0
        var bassDecayMul = 0.9999
        var bassVel = 0.0
        var bassAge = 0

        val padPhases = DoubleArray(5)
        val padFreqs = padChords[0].copyOf()

        var hookPhase1 = 0.0
        var hookPhase2 = 0.0
        var hookCurrFreq = 440.00
        var hookTargetFreq = 440.00
        var hookTargetGain = 0.0
        var hookEnv = 0.0
        var hookAgeSamples = 0

        var kickAge = 100000
        var kickPhase = 0.0
        var kickEnv = 0.0
        val kickDecayMul = exp(-dt * 16.0)

        var snareAge = 100000
        var snarePhase = 0.0
        var snareBrushEnv = 0.0
        var snareShellEnv = 0.0
        val snareBrushDecay = exp(-dt * 23.0)
        val snareShellDecay = exp(-dt * 32.0)

        var hatAge = 100000
        var hatEnv = 0.0
        val hatDecayMul = exp(-dt * 68.0)

        var snareLp = 0.0
        var hatHpPrev = 0.0
        var hatLp = 0.0

        var psychAge = 100000
        var psychPhase = 0.0
        var psychFreq = 659.25

        var wowPhase1 = 0.0
        var wowPhase2 = 0.0
        var chorusPhaseLfo = 0.0
        var vinylLp = 0.0
        var masterLp = 0.0
        var crackleEnv = 0.0
        val rng = Random(1967)

        val delaySize = (sampleRate * 0.275).toInt() // Tighter 275ms warm romantic tape echo
        val delayLine = DoubleArray(delaySize)
        var delayPos = 0
        var delayLp = 0.0

        var lastStep = -1
        val twoPiDt = 2.0 * PI * dt

        for (sampleIdx in 0 until totalRenderSamples) {
            val globalStep = sampleIdx / stepSamples
            if (globalStep != lastStep) {
                lastStep = globalStep
                val step = globalStep % totalSteps
                val barStep = step % 16

                // Soft brushed vintage drums in a clean laid-back pocket
                if (barStep == 0 || barStep == 6 || barStep == 8 || barStep == 14) {
                    kickAge = 0
                    kickPhase = 0.0
                    kickEnv = 1.0
                }
                if (barStep == 4 || barStep == 12) {
                    snareAge = 0
                    snarePhase = 0.0
                    snareBrushEnv = 1.0
                    snareShellEnv = 1.0
                }
                if (barStep % 2 == 0) {
                    hatAge = 0
                    hatEnv = 1.0
                }

                bassByStep[step]?.let { b ->
                    bassTargetFreq = b.freqHz
                    bassVel = b.velocity
                    bassDecayMul = exp(-dt * b.decayRate)
                    bassEnv = 1.0
                    bassAge = 0
                }

                val gList = guitarByStep[step]
                for (k in gList.indices) {
                    val g = gList[k]
                    val v = guitarPool[nextGuitarIdx]
                    v.freqHz = g.freqHz
                    v.velocity = g.velocity
                    v.decay1Mul = exp(-dt * g.decayRate)
                    v.decay2Mul = exp(-dt * (g.decayRate * 2.3))
                    v.decay3Mul = exp(-dt * (g.decayRate * 4.8))
                    v.env1 = 1.0
                    v.env2 = 1.0
                    v.env3 = 1.0
                    v.phase1 = 0.0
                    v.phase2 = 0.0
                    v.phase3 = 0.0
                    v.chorusPhase = 0.35
                    v.ageSamples = 0
                    v.active = true
                    nextGuitarIdx = (nextGuitarIdx + 1) % guitarPool.size
                }

                val activeHook = hookByStep[step]
                if (activeHook != null) {
                    if (hookIsStart[step]) hookAgeSamples = 0
                    hookTargetFreq = activeHook.freqHz
                    hookTargetGain = activeHook.gain
                } else {
                    hookTargetGain = 0.0
                }

                if (step == 26 || step == 58) {
                    psychAge = 0
                    psychPhase = 0.0
                    psychFreq = if (step == 26) 880.0 else 1046.50
                }
            }

            wowPhase1 += twoPiDt * 0.29
            wowPhase2 += twoPiDt * 0.68
            chorusPhaseLfo += twoPiDt * 0.46
            val tapeWow = 1.0 + 0.0014 * fastSin(wowPhase1) + 0.0006 * fastSin(wowPhase2)
            val chorusDetune = 1.0 + 0.0022 * fastSin(chorusPhaseLfo)

            // A. Soft Brushed Vintage Drums
            var drumMix = 0.0
            if (kickAge < 11000) {
                val kAgeSec = kickAge * dt
                val kickFreq = 45.0 + 50.0 * (1.0 - (kAgeSec * 12.0).coerceAtMost(0.95))
                kickPhase += twoPiDt * kickFreq
                val kAttack = (kickAge / 200.0).coerceAtMost(1.0)
                drumMix += 0.39 * fastSin(kickPhase) * kAttack * kickEnv
                kickEnv *= kickDecayMul
                kickAge++
            }
            val whiteNoise = rng.nextDouble() * 2.0 - 1.0
            if (snareAge < 8500) {
                snareLp += (whiteNoise - snareLp) * 0.14
                snarePhase += twoPiDt * 182.0
                drumMix += (snareLp * snareBrushEnv * 0.19) + (fastSin(snarePhase) * snareShellEnv * 0.10)
                snareBrushEnv *= snareBrushDecay
                snareShellEnv *= snareShellDecay
                snareAge++
            }
            if (hatAge < 2600) {
                val hp = whiteNoise - hatHpPrev
                hatHpPrev = whiteNoise
                hatLp += (hp - hatLp) * 0.20
                drumMix += hatLp * hatEnv * 0.055
                hatEnv *= hatDecayMul
                hatAge++
            }

            // B. Warm Melodic Flatwound Bass
            var bassMix = 0.0
            if (bassEnv > 0.002) {
                bassCurrFreq += (bassTargetFreq - bassCurrFreq) * 0.022
                bassPhase += twoPiDt * (bassCurrFreq * tapeWow)
                val bAttack = (bassAge / 480.0).coerceAtMost(1.0)
                bassMix = (0.85 * fastSin(bassPhase) + 0.15 * fastSin(2.0 * bassPhase)) * bAttack * bassEnv * bassVel * 0.36
                bassEnv *= bassDecayMul
                bassAge++
            }

            // C. Dreamy Chorus Electric Guitar
            var dryGuitar = 0.0
            for (g in guitarPool) {
                if (!g.active) continue
                if (g.env1 < 0.008) {
                    g.active = false
                    continue
                }
                val gAttack = (g.ageSamples / 420.0).coerceAtMost(1.0)
                val f = g.freqHz * tapeWow
                val dPhase = twoPiDt * f
                g.phase1 += dPhase
                g.phase2 += dPhase * 2.0
                g.phase3 += dPhase * 3.0
                g.chorusPhase += dPhase * chorusDetune

                val pickup = 0.70 * g.env1 * fastSin(g.phase1) +
                    0.23 * g.env2 * fastSin(g.phase2) +
                    0.07 * g.env3 * fastSin(g.phase3)
                val chorus = 0.34 * g.env1 * fastSin(g.chorusPhase)
                dryGuitar += (pickup + chorus) * gAttack * g.velocity

                g.env1 *= g.decay1Mul
                g.env2 *= g.decay2Mul
                g.env3 *= g.decay3Mul
                g.ageSamples++
            }

            // D. Hazy Romantic Analog Synth Pad
            val currentBar = ((sampleIdx / stepSamples) % totalSteps) / 16
            val targetChord = padChords[currentBar]
            var padMix = 0.0
            for (p in 0 until 5) {
                padFreqs[p] += (targetChord[p] - padFreqs[p]) * 0.0011
                padPhases[p] += twoPiDt * (padFreqs[p] * tapeWow * chorusDetune)
                padMix += fastSin(padPhases[p])
            }
            val hazyPad = (padMix * 0.2) * 0.24

            // E. Subtle Psychedelic Reverse-Envelope Swell
            var psychMix = 0.0
            val psychMaxSamples = 25000 // ~0.56s tighter psychedelic kiss
            if (psychAge < psychMaxSamples) {
                psychPhase += twoPiDt * (psychFreq * tapeWow)
                val progress = psychAge.toDouble() / psychMaxSamples
                val revEnv = if (progress < 0.94) {
                    progress * progress * progress
                } else {
                    1.0 - ((progress - 0.94) / 0.06)
                }
                psychMix = fastSin(psychPhase) * revEnv.coerceIn(0.0, 1.0) * 0.13
                psychAge++
            }

            // F. Crisp Romantic Hook (clean natural phrasing, not dragged/stretched)
            hookCurrFreq += (hookTargetFreq - hookCurrFreq) * 0.0055
            val hookSpeed = if (hookTargetGain > hookEnv) 0.00080 else 0.00055
            hookEnv += (hookTargetGain - hookEnv) * hookSpeed
            val hookAgeSec = hookAgeSamples * dt
            val hookVib = 1.0 + 0.0030 * ((hookAgeSec - 0.18) / 0.28).coerceIn(0.0, 1.0) * fastSin(2.0 * PI * 4.8 * hookAgeSec)
            val fHook = hookCurrFreq * tapeWow * hookVib
            hookPhase1 += twoPiDt * fHook
            hookPhase2 += twoPiDt * (fHook * 2.0)
            val dryHook = (0.87 * fastSin(hookPhase1) + 0.13 * fastSin(hookPhase2)) * hookEnv * 0.28
            hookAgeSamples++

            // G. Tighter Analog Tape Echo
            val spatialBus = (dryGuitar * 0.46) + hazyPad + psychMix + dryHook
            val delayed = delayLine[delayPos]
            delayLp += (delayed - delayLp) * 0.19
            delayLine[delayPos] = (spatialBus * 0.44) + (delayLp * 0.24)
            delayPos = (delayPos + 1) % delaySize

            // H. Vintage Lo-Fi Vinyl Texture
            vinylLp += (whiteNoise - vinylLp) * 0.05
            if (rng.nextDouble() < 0.00018) crackleEnv = 0.022
            crackleEnv *= 0.96
            val vinylTexture = (vinylLp * 0.011) + (whiteNoise * crackleEnv)

            // Master Warmth + Soft Analog Saturation
            val rawMaster = drumMix + bassMix + (spatialBus * 0.72) + (delayLp * 0.36) + vinylTexture
            masterLp += (rawMaster - masterLp) * 0.42
            val saturated = (masterLp - (0.16 * masterLp * masterLp * masterLp)) * 0.56

            if (sampleIdx in loopLen until (loopLen * 2)) {
                val outIdx = sampleIdx - loopLen
                outLoop[outIdx] = (saturated * Short.MAX_VALUE)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    .toShort()
            } else if (sampleIdx >= loopLen * 2) {
                val tailIdx = sampleIdx - (loopLen * 2)
                if (tailIdx < crossfadeLen) {
                    tailOverlap[tailIdx] = saturated
                }
            }
        }

        for (i in 0 until crossfadeLen) {
            val alpha = i.toDouble() / crossfadeLen
            val pass2Start = outLoop[i].toDouble() / Short.MAX_VALUE
            val blended = (pass2Start * alpha) + (tailOverlap[i] * (1.0 - alpha))
            outLoop[i] = (blended * Short.MAX_VALUE)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }

        return outLoop
    }

    fun start() {
        if (playbackJob?.isActive == true) return
        playbackJob = scope.launch {
            runCatching { Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO) }

            val pcmLoop = loopPcm
            val loopLen = pcmLoop.size

            val chunkSamples = 4096
            val minBuffer = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(chunkSamples * 2)
            val trackBufferBytes = (minBuffer * 3).coerceAtLeast(chunkSamples * 2 * 4)

            val track = try {
                AudioTrack.Builder()
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
                    .setBufferSizeInBytes(trackBufferBytes)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (_: Throwable) {
                null
            } ?: return@launch

            audioTrack = track
            try {
                track.play()
                val outChunk = ShortArray(chunkSamples)
                var readIdx = 0
                var smoothedGain = 0.0

                while (isActive) {
                    val userVol = volume.coerceIn(0f, 1f)
                    val targetGain = if (isMuted) 0.0 else (userVol * userVol * 0.25)

                    for (i in 0 until chunkSamples) {
                        smoothedGain += (targetGain - smoothedGain) * 0.005
                        if (smoothedGain <= 0.0001 && targetGain == 0.0) {
                            outChunk[i] = 0
                        } else {
                            outChunk[i] = (pcmLoop[readIdx] * smoothedGain)
                                .toInt()
                                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                                .toShort()
                        }
                        readIdx++
                        if (readIdx >= loopLen) readIdx = 0
                    }
                    track.write(outChunk, 0, chunkSamples)
                }
            } catch (_: Throwable) {
                // Gracefully ignore audio hardware interruptions
            } finally {
                runCatching {
                    track.stop()
                    track.release()
                }
                if (audioTrack === track) audioTrack = null
            }
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
    }
}
