package com.laminalfalah.menu.makanan.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReceiverBroadcastService extends BroadcastReceiver {

    private static final String TAG = ReceiverBroadcastService.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: Service Stop !");
        context.startService(new Intent(context, ServiceApp.class));
    }
}
