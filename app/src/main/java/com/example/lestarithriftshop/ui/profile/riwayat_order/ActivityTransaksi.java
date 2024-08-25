package com.example.lestarithriftshop.ui.profile.riwayat_order;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lestarithriftshop.MainActivity;
import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.RegisterAPI;
import com.example.lestarithriftshop.ServerAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ActivityTransaksi extends AppCompatActivity {
    private Button btnRiwayatOrder, btnBeranda;
    private TextView tvTanggal, tvInvoice, tvTotalHarga;
    private String valueInvoice = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaksi);
        getSupportActionBar().hide();

        initializeViews();
        retrieveInvoiceFromIntent();
        setupButtonListeners();

        getDetailOrder(valueInvoice);
    }

    private void initializeViews() {
        tvTanggal = findViewById(R.id.tv_tanggal);
        tvInvoice = findViewById(R.id.tv_invoice);
        tvTotalHarga = findViewById(R.id.tv_total_harga);

        btnRiwayatOrder = findViewById(R.id.btn_riwayat_order);
        btnBeranda = findViewById(R.id.btn_beranda);
    }

    private void retrieveInvoiceFromIntent() {
        Intent intent = getIntent();
        valueInvoice = intent.getStringExtra("INVOICE");
    }

    private void setupButtonListeners() {
        btnRiwayatOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToActivity(ActivityOrder.class);
            }
        });

        btnBeranda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToActivity(MainActivity.class);
            }
        });
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(ActivityTransaksi.this, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void getDetailOrder(String vInvoice) {
        ServerAPI urlAPI = new ServerAPI();
        String URL = urlAPI.BASE_URL;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.getDetailOrder(vInvoice).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        parseOrderDetailResponse(jsonResponse);
                    } catch (JSONException | IOException e) {
                        Log.e("Error", "Failed to parse response", e);
                    }
                } else {
                    Log.e("Error", "Response unsuccessful");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Error", "Request failed", t);
            }
        });
    }

    private void parseOrderDetailResponse(JSONObject jsonResponse) throws JSONException {
        int result = jsonResponse.getInt("result");
        if (result == 1) {
            JSONArray dataArray = jsonResponse.getJSONArray("data");

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject data = dataArray.getJSONObject(i);

                int id = data.getInt("order_id");
                String invoice = data.getString("invoice");
                String tanggal = data.getString("tanggal");
                String jam = data.getString("jam");
                double totalHarga = data.getDouble("total_harga");

                updateUI(invoice, tanggal, jam, totalHarga);
                getOrderConfirm(id);
            }
        } else {
            Log.e("Error", "Result is not 1");
        }
    }

    private void updateUI(String invoice, String tanggal, String jam, double totalHarga) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        String formattedTotalHarga = formatRupiah.format(totalHarga).replace(",00", "");
        tvTotalHarga.setText(formattedTotalHarga);

        tvInvoice.setText("#" + invoice);
        String formattedDate = formatDate(tanggal);
        tvTanggal.setText(formattedDate + ", " + jam + " WIB");
    }

    private void getOrderConfirm(int orderId) {
        ServerAPI urlAPI = new ServerAPI();
        String URL = urlAPI.BASE_URL;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.getOrderConfirm(orderId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        parseOrderConfirmResponse(jsonResponse);
                    } catch (JSONException | IOException e) {
                        Log.e("Error", "Failed to parse response", e);
                    }
                } else {
                    Log.e("Error", "Response unsuccessful");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Error", "Request failed", t);
            }
        });
    }

    private void parseOrderConfirmResponse(JSONObject jsonResponse) throws JSONException {
        int result = jsonResponse.getInt("result");
        if (result == 1) {
            JSONArray dataArray = jsonResponse.getJSONArray("data");

            if (dataArray.length() > 0) {
                JSONObject data = dataArray.getJSONObject(0);

                String confirmTanggal = data.getString("confirm_tanggal");
                String confirmJam = data.getString("confirm_jam");

                String formattedDate = formatDate(confirmTanggal);
                tvTanggal.setText(formattedDate + ", " + confirmJam + " WIB");
            } else {
                Log.e("Error", "No data found in dataArray");
            }
        } else {
            Log.e("Error", "Result is not 1");
        }
    }

    private String formatDate(String date) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat targetFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("in", "ID"));
            return targetFormat.format(originalFormat.parse(date));
        } catch (Exception e) {
            Log.e("Error", "Failed to format date", e);
            return date;
        }
    }

    @Override
    public void onBackPressed() {
        // Disable the back button
    }
}
