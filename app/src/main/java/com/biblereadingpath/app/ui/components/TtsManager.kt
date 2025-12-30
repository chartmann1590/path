package com.biblereadingpath.app.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized = _isInitialized.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<Voice>>(emptyList())
    val availableVoices = _availableVoices.asStateFlow()

    // Word-by-word progress tracking
    private val _currentWordIndex = MutableStateFlow<Int?>(null)
    val currentWordIndex = _currentWordIndex.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking = _isSpeaking.asStateFlow()

    // Store current words to calculate word indices from character positions
    private var currentWords: List<String> = emptyList()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                _isInitialized.value = true
                loadVoices()
                setupProgressListener()
            }
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                if (utteranceId == "path_tts") {
                    _isSpeaking.value = true
                    _currentWordIndex.value = 0
                }
            }

            override fun onDone(utteranceId: String?) {
                if (utteranceId == "path_tts") {
                    _isSpeaking.value = false
                    _currentWordIndex.value = null
                    currentWords = emptyList()
                }
            }

            override fun onError(utteranceId: String?) {
                if (utteranceId == "path_tts") {
                    _isSpeaking.value = false
                    _currentWordIndex.value = null
                    currentWords = emptyList()
                }
            }

            override fun onRangeStart(
                utteranceId: String?,
                start: Int,
                end: Int,
                frame: Int
            ) {
                if (utteranceId == "path_tts" && currentWords.isNotEmpty()) {
                    // Calculate which word we're on based on character position
                    var charCount = 0
                    var foundWordIndex = 0
                    for (i in currentWords.indices) {
                        val wordLength = currentWords[i].length
                        if (charCount <= start && start < charCount + wordLength) {
                            foundWordIndex = i
                            break
                        }
                        charCount += wordLength + 1 // +1 for space
                    }
                    _currentWordIndex.value = foundWordIndex
                }
            }
        })
    }

    private fun loadVoices() {
        tts?.let { engine ->
            val voices = engine.voices
                ?.filter { it.locale.language == Locale.ENGLISH.language && !it.isNetworkConnectionRequired }
                ?.sortedBy { it.name }
                ?: emptyList()
            _availableVoices.value = voices
        }
    }

    fun setVoice(voiceName: String) {
        tts?.let { engine ->
            val voice = engine.voices?.find { it.name == voiceName }
            if (voice != null) {
                engine.voice = voice
            } else {
                // Fallback to default language
                engine.language = Locale.US
            }
        }
    }

    fun speak(text: String) {
        _currentWordIndex.value = null
        currentWords = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "path_tts")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _currentWordIndex.value = null
    }

    fun shutdown() {
        tts?.shutdown()
    }
}
