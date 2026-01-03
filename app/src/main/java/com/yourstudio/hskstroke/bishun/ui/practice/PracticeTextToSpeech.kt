package com.yourstudio.hskstroke.bishun.ui.practice

import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

data class TextToSpeechController(
    val speak: (String) -> Unit,
    val isSpeaking: State<Boolean>,
    val isAvailable: State<Boolean>,
)

@Composable
fun rememberTextToSpeechController(): TextToSpeechController {
    val context = LocalContext.current
    val speakingState = remember { mutableStateOf(false) }
    val availableState = remember { mutableStateOf(false) }
    val handler = remember { Handler(Looper.getMainLooper()) }
    val ttsState = remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val appContext = context.applicationContext
        lateinit var textToSpeech: TextToSpeech
        textToSpeech = TextToSpeech(appContext) { status ->
            if (status != TextToSpeech.SUCCESS) {
                handler.post { availableState.value = false }
                return@TextToSpeech
            }
            val selectedLocale = selectBestChineseLocale(textToSpeech)
            val result = selectedLocale?.let { locale ->
                textToSpeech.setLanguage(locale)
            } ?: TextToSpeech.LANG_NOT_SUPPORTED
            val available = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            handler.post { availableState.value = available }
        }
        ttsState.value = textToSpeech
        val listener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                handler.post { speakingState.value = true }
            }

            override fun onDone(utteranceId: String?) {
                handler.post { speakingState.value = false }
            }

            override fun onError(utteranceId: String?) {
                handler.post { speakingState.value = false }
            }
        }
        textToSpeech.setOnUtteranceProgressListener(listener)
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
            ttsState.value = null
            handler.post {
                speakingState.value = false
                availableState.value = false
            }
        }
    }
    val speak: (String) -> Unit = speak@{ word ->
        val tts = ttsState.value ?: return@speak
        if (!availableState.value) return@speak
        val trimmed = word.trim()
        if (trimmed.isNotBlank()) {
            tts.speak(trimmed, TextToSpeech.QUEUE_FLUSH, null, "word-$trimmed")
        }
    }
    return TextToSpeechController(speak = speak, isSpeaking = speakingState, isAvailable = availableState)
}

private fun selectBestChineseLocale(textToSpeech: TextToSpeech): Locale? {
    val candidates = listOf(
        Locale.SIMPLIFIED_CHINESE,
        Locale.CHINA,
        Locale.CHINESE,
        Locale.forLanguageTag("zh-CN"),
    )
    return candidates.firstOrNull { locale ->
        val availability = textToSpeech.isLanguageAvailable(locale)
        availability != TextToSpeech.LANG_MISSING_DATA && availability != TextToSpeech.LANG_NOT_SUPPORTED
    }
}
