package com.example.lestarithriftshop.ui.profile.riwayat_order;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class TimerService extends Service {

    private final IBinder binder = new LocalBinder();
    private CountDownTimer countDownTimer;
    private long timeRemaining = 2 * 60 * 60 * 1000; // 2 hours in milliseconds
    private TimerListener listener;

    public class LocalBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences = getSharedPreferences("TimerPrefs", MODE_PRIVATE);
        timeRemaining = preferences.getLong("timeRemaining", 2 * 60 * 60 * 1000);
        startTimer(timeRemaining);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveTimeRemaining(timeRemaining);
    }

    private void saveTimeRemaining(long time) {
        SharedPreferences preferences = getSharedPreferences("TimerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("timeRemaining", time);
        editor.apply(); // Save asynchronously

        // Logging untuk memeriksa nilai yang disimpan
        long savedTime = preferences.getLong("timeRemaining", -1); // -1 adalah default value jika tidak ditemukan
        Log.d("TimerService", "Saved timeRemaining: " + savedTime);
    }

    private void startTimer(long duration) {
        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                saveTimeRemaining(timeRemaining); // Periodically save timeRemaining
                long hours = millisUntilFinished / (60 * 60 * 1000);
                long minutes = (millisUntilFinished % (60 * 60 * 1000)) / (60 * 1000);
                long seconds = (millisUntilFinished % (60 * 1000)) / 1000;

                if (listener != null) {
                    listener.onTick(hours, minutes, seconds);
                }
            }

            @Override
            public void onFinish() {
                if (listener != null) {
                    listener.onFinish();
                }
            }
        }.start();
    }

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    public void setListener(TimerListener listener) {
        this.listener = listener;
    }

    public interface TimerListener {
        void onTick(long hours, long minutes, long seconds);
        void onFinish();
    }
}