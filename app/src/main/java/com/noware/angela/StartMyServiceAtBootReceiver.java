package com.noware.angela;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartMyServiceAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        TODO: Decomment this lines when the app is completed
//        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
//            Intent serviceIntent = new Intent(context, MyService.class);
//            context.startService(serviceIntent);
//        }
    }
}