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
)

@Composable
fun rememberTextToSpeechController(): TextToSpeechController {
    val context = LocalContext.current
    val speakingState = remember { mutableStateOf(false) }
    val handler = remember { Handler(Looper.getMainLooper()) }
    val textToSpeech = remember { TextToSpeech(context) { } }
    DisposableEffect(textToSpeech) {
        textToSpeech.language = Locale.CHINA
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
        }
    }
    val speak: (String) -> Unit = { word ->
        if (word.isNotBlank()) {
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word-$word")
        }
    }
    return TextToSpeechController(speak = speak, isSpeaking = speakingState)
}
