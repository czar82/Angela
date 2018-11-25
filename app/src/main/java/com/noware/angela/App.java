package com.noware.angela;

import android.app.Application;

import com.noware.speech.Logger;
import com.noware.speech.Speech;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Speech.init(this, getPackageName());
        Logger.setLogLevel(Logger.LogLevel.DEBUG);
    }
}
