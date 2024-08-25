package com.example.lestarithriftshop.ui.profile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lestarithriftshop.R;

public class ActivityContact extends AppCompatActivity {
    ImageButton imgbtn_kembali;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact);
        getSupportActionBar().hide();

        imgbtn_kembali = findViewById(R.id.btn_kembali);
        imgbtn_kembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}