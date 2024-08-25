package com.example.lestarithriftshop;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class CustomToast {
    public static void showToast(Context context, String message, int duration) {
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);

        // Show the toast initially
        toast.show();

        // Create a handler to cancel the toast after the specified duration
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }
}
