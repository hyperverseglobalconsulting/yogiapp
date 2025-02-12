package com.example.yogiapp.managers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechRecognitionManager implements RecognitionListener {

    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private OnCommandListener commandListener;

    public interface OnCommandListener {
        void onCommandReceived(String command);
    }

    public SpeechRecognitionManager(Context context, OnCommandListener listener) {
        commandListener = listener;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(this);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
    }

    public void startListening() {
        speechRecognizer.startListening(recognizerIntent);
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String command = matches.get(0).toLowerCase();
            if (command.startsWith("yogi")) {
                commandListener.onCommandReceived(command);
            }
        }
        startListening();
    }

    @Override
    public void onError(int error) {
        Log.e("SpeechRecognition", "Error: " + error);
        startListening();
    }

    @Override public void onReadyForSpeech(Bundle params) { }
    @Override public void onBeginningOfSpeech() { }
    @Override public void onRmsChanged(float rmsdB) { }
    @Override public void onBufferReceived(byte[] buffer) { }
    @Override public void onEndOfSpeech() { }
    @Override public void onPartialResults(Bundle partialResults) { }
    @Override public void onEvent(int eventType, Bundle params) { }
}
