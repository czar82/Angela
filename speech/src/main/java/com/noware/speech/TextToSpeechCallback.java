package com.noware.speech;

public interface TextToSpeechCallback {
    void onStart();
    void onCompleted();
    void onError();
}
