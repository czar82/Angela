package com.noware.angela;

import android.Manifest.permission;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.user.speechrecognizationasservice.R;
import com.noware.speech.GoogleVoiceTypingDisabledException;
import com.noware.speech.Speech;
import com.noware.speech.SpeechDelegate;
import com.noware.speech.SpeechRecognitionNotAvailable;
import com.noware.speech.TextToSpeechCallback;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MyService extends Service implements SpeechDelegate, Speech.stopDueToDelay {

    public static SpeechDelegate delegate;
    private boolean isSpeaking;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        try {
            if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
                ((AudioManager) Objects.requireNonNull(
                        getSystemService(Context.AUDIO_SERVICE))).setStreamMute(AudioManager.STREAM_SYSTEM, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isSpeaking = false;
//        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if(status != TextToSpeech.ERROR) {
//                    t1.setLanguage(Locale.getDefault());
//                }
//            }
//        });
//        t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
//            @Override
//            public void onStart(String utteranceId) {
//                isSpeaking = true;
//            }
//
//            @Override
//            public void onDone(String utteranceId) {
//                    isSpeaking = false;
//                    startListening(MyService.this);
//            }
//
//            @Override
//            public void onError(String utteranceId) {
//                isSpeaking = false;
//                startListening(MyService.this);
//            }
//
//            @Override
//            public void onStop(String utteranceId, boolean interrupted) {
//                isSpeaking = false;
//                startListening(MyService.this);
//            }
//        });
        Speech.init(this);
        delegate = this;
        Speech.getInstance().setListener(this);

        if (Speech.getInstance().isListening()) {
            Speech.getInstance().stopListening();
            muteBeepSoundOfRecorder();
        } else {
            System.setProperty("rx.unsafe-disable", "True");
            RxPermissions.getInstance(this).request(permission.RECORD_AUDIO).subscribe(granted -> {
                if (granted) { // Always true pre-M
                    try {
                        Speech.getInstance().stopTextToSpeech();
                        Speech.getInstance().startListening(null, this);
                    } catch (SpeechRecognitionNotAvailable exc) {
                        //showSpeechNotSupportedDialog();

                    } catch (GoogleVoiceTypingDisabledException exc) {
                        //showEnableGoogleVoiceTyping();
                    }
                } else {
                    Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
                }
            });
            muteBeepSoundOfRecorder();
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    @Override
    public void onStartOfSpeech() {
        Log.d("onStartOfSpeech", "test... i min");
    }

    @Override
    public void onSpeechRmsChanged(float value) {

    }

    @Override
    public void onSpeechPartialResults(List<String> results) {
        for (String partial : results) {
            Log.d("Result", partial+"");
        }
    }

    private void unmuteAudio() {
        AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audio.setStreamMute(AudioManager.STREAM_NOTIFICATION,   false);
        audio.setStreamMute(AudioManager.STREAM_ALARM,          false);
        audio.setStreamMute(AudioManager.STREAM_MUSIC,          false);
        audio.setStreamMute(AudioManager.STREAM_RING,           false);
        audio.setStreamMute(AudioManager.STREAM_SYSTEM,         false);

        audio.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0);
//        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
//        this.setVolumeControlStream(AudioManager.STREAM_RING);
//        this.setVolumeControlStream(AudioManager.STREAM_ALARM);
//        this.setVolumeControlStream(AudioManager.STREAM_NOTIFICATION);
//        this.setVolumeControlStream(AudioManager.STREAM_SYSTEM);
//        this.setVolumeControlStream(AudioManager.STREAM_VOICECALL);
    }

//    TextToSpeech t1;

    @Override
    public void onSpeechResult(String result) {
        Log.d("Result", result+"");
        if (!TextUtils.isEmpty(result)) {
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

//            Speech.getInstance().stopListening();
//            unmuteAudio();
//            t1.speak(result, TextToSpeech.QUEUE_FLUSH, null);

            isSpeaking = true;
            Speech.getInstance().stopListening();
            unmuteAudio();
            Speech.getInstance().say(result, new TextToSpeechCallback() {
                @Override
                public void onStart() {

                    Log.d("txt2sp", "Start");
                    isSpeaking = true;
                }

                @Override
                public void onCompleted() {

                    Log.d("txt2sp", "onCompleted");
                    isSpeaking = false;
                    startListening(MyService.this);
                }

                @Override
                public void onError() {

                    Log.d("txt2sp", "error");
                    isSpeaking = false;
                    startListening(MyService.this);
                }
            });
        }
    }

    @Override
    public void onSpecifiedCommandPronounced(String event) {
        try {
            if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
                ((AudioManager) Objects.requireNonNull(
                        getSystemService(Context.AUDIO_SERVICE))).setStreamMute(AudioManager.STREAM_SYSTEM, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Speech.getInstance().isListening()) {
            muteBeepSoundOfRecorder();
            Speech.getInstance().stopListening();
        } else if (!isSpeaking){
            RxPermissions.getInstance(this).request(permission.RECORD_AUDIO).subscribe(granted -> {
                if (granted) { // Always true pre-M
                    startListening(this);
                    try {
                        //la chiave sono questa:
                        Speech.getInstance().stopTextToSpeech();
                        Speech.getInstance().startListening(null, this);
                    } catch (SpeechRecognitionNotAvailable exc) {
                        //showSpeechNotSupportedDialog();

                    } catch (GoogleVoiceTypingDisabledException exc) {
                        //showEnableGoogleVoiceTyping();
                    }
                } else {
                    Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
                }
            });
            //e quest'altra
            muteBeepSoundOfRecorder();
        }
    }

    private void startListening(MyService myService) {
        try {
            //la chiave sono questa:
            Speech.getInstance().stopTextToSpeech();
            Speech.getInstance().startListening(null, myService);
        } catch (SpeechRecognitionNotAvailable exc) {
            //showSpeechNotSupportedDialog();

        } catch (GoogleVoiceTypingDisabledException exc) {
            //showEnableGoogleVoiceTyping();
        }

    }

    /**
     * Function to remove the beep sound of voice recognizer.
     */
    private void muteBeepSoundOfRecorder() {
        AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (amanager != null) {
            amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
            amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            amanager.setStreamMute(AudioManager.STREAM_RING, true);
            amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Speech.getInstance().stopTextToSpeech();
        Speech.getInstance().stopListening();

        Speech.getInstance().shutdown();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //Restarting the service if it is removed.
        PendingIntent service =
                PendingIntent.getService(getApplicationContext(), new Random().nextInt(),
                        new Intent(getApplicationContext(), MyService.class), PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, service);
        super.onTaskRemoved(rootIntent);
    }
}