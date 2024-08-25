package com.example.lestarithriftshop.ui.cart.checkout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.ui.profile.riwayat_order.ActivityOrder;

import java.text.NumberFormat;
import java.util.Locale;

public class ActivitySuccess extends AppCompatActivity {
    Button btn_riwayat_order;
    TextView jumlah_bayar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_success);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        double jumlahBayar = intent.getDoubleExtra("JUMLAH_BAYAR", 0.0);

        jumlah_bayar = findViewById(R.id.jumlah_bayar);

        // Format the double value to the currency format used in the original code
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        String formattedTotalBelanja = formatRupiah.format(jumlahBayar).replace(",00", "");

        jumlah_bayar.setText(formattedTotalBelanja);

        btn_riwayat_order = findViewById(R.id.btn_riwayat_order);
        btn_riwayat_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitySuccess.this, ActivityOrder.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Do nothing to disable the back button
        super.onBackPressed();
    }
}