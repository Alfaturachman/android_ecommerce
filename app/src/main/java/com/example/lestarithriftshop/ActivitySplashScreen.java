package com.example.lestarithriftshop;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class ActivitySplashScreen extends AppCompatActivity {

    private static final long SPLASH_SCREEN_DELAY = 2000; // Waktu tampilan splash screen (dalam milidetik)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();

        // Tambahkan delay sebelum memulai MainActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ActivitySplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish(); // Tutup ActivitySplashScreen agar tidak dapat diakses kembali setelah navigasi
            }
        }, SPLASH_SCREEN_DELAY);
    }
}
