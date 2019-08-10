package com.laminalfalah.menu.makanan.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;

public class ServiceApp extends Service {

    private static final String TAG = ServiceApp.class.getSimpleName();

    private int counter = 0;
    private Context context;
    private Timer timer;
    private TimerTask timerTask;

    public ServiceApp() {
    }

    public ServiceApp(Context context) {
        super();
        this.context = context;
        Log.i(TAG, "ServiceApp: Here Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: EXIT");
        sendBroadcast(new Intent("com.laminalfalah.menu.makanan.ActivityRecognition.RestartSensor"));
        stopTimerTask();
    }

    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask,1000,1000);
    }

    private void initializeTimerTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // Counter Timer
            }
        };
    }

    private void stopTimerTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
