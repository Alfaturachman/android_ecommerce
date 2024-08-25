package com.example.lestarithriftshop.ui.profile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.RegisterAPI;
import com.example.lestarithriftshop.ServerAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ActivityGantiPassword extends AppCompatActivity {
    ImageButton btnKembali;
    Button btnSimpan;
    EditText etPassword, etKonfirmasiPassword;
    SharedPreferences sharedPreferences;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ganti_password);
        getSupportActionBar().hide();

        etPassword = findViewById(R.id.etPassword);
        etKonfirmasiPassword = findViewById(R.id.etKonfirmasiPassword);

        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");

        btnKembali = findViewById(R.id.btn_kembali);
        btnKembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSimpan = findViewById(R.id.btn_simpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    DataUser data = new DataUser();
                    data.setPassword(etPassword.getText().toString());
                    data.setEmail(email);
                    updateProfile(data);
                } else {

                }
            }
        });
    }

    private boolean validateFields() {
        String password = etPassword.getText().toString();
        String confirmPassword = etKonfirmasiPassword.getText().toString();

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Password Belum Diisi Semua!");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showAlert("Password dan Konfirmasi Password tidak cocok!");
            return false;
        }
        return true;
    }


    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setNegativeButton("OK", null)
                .create()
                .show();
    }

    private void updateProfile(DataUser data) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Call<ResponseBody> call = api.updatePassword(
                data.getPassword(),
                data.getEmail());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String message = jsonObject.getString("message");
                        etPassword.setText("");
                        etKonfirmasiPassword.setText("");
                        Toast.makeText(ActivityGantiPassword.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ActivityGantiPassword.this, "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                AlertDialog.Builder msg = new AlertDialog.Builder(ActivityGantiPassword.this);
                msg.setMessage("Simpan Gagal, Error:" + t.toString()).setNegativeButton("coba lagi", null).create().show();
            }
        });
    }
}
