package com.example.lestarithriftshop.ui.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lestarithriftshop.MainActivity;
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
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ActivityLogin extends AppCompatActivity {
    Button btn_login, btn_halaman_register;
    ImageButton btn_kembali;
    SharedPreferences sharedPreferences;
    EditText etEmail, etPassword;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);

        btn_kembali = findViewById(R.id.btn_kembali);
        btn_kembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityLogin.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_login = findViewById(R.id.btn_login); // Inisialisasi button login
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if (!email.isEmpty() && !password.isEmpty()) {
                    proses_login(email, password);
                } else {
                    Toast.makeText(ActivityLogin.this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_halaman_register = findViewById(R.id.btn_halaman_register); // Inisialisasi button login
        btn_halaman_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent untuk pindah ke layar login
                Intent intent = new Intent(ActivityLogin.this, ActivityRegister.class); // Ganti TargetActivity dengan aktivitas tujuan
                startActivity(intent);
            }
        });
    }

    void proses_login (String v_email, String v_password) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.login(v_email, v_password).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        // Inside onResponse method after successful login
                        if (jsonObject.getString("result").equals("1")) {
                            // Save user data in SharedPreferences upon successful login
                            int customer_id = jsonObject.getJSONObject("data").getInt("id");
                            String email = jsonObject.getJSONObject("data").getString("email");
                            String nama = jsonObject.getJSONObject("data").getString("nama");
                            saveUserData(customer_id, nama, email); // Call saveUserData here

                            // Mengambil data dari SharedPreferences setelah berhasil login
                            int savedCustomerId = sharedPreferences.getInt("customer_id", -1);
                            String savedNama = sharedPreferences.getString("nama", "");
                            String savedEmail = sharedPreferences.getString("email", "");

                            Log.d("SharedPref Id","Id: " + savedCustomerId);
                            Log.d("SharedPref Nama",", Nama: " + savedNama);
                            Log.d("SharedPref Email",", Email: " + savedEmail);
                            Toast.makeText(ActivityLogin.this, "Login Berhasil", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(ActivityLogin.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // User not found or other error
                            AlertDialog.Builder msg = new AlertDialog.Builder(ActivityLogin.this);
                            msg.setMessage("Email atau password salah")
                                    .setNegativeButton("Retry", null)
                                    .create().show();
                        }
                    } else {
                        // Response not successful
                        Log.e("API Response", "Response not successful: " + response.code());
                        AlertDialog.Builder msg = new AlertDialog.Builder(ActivityLogin.this);
                        msg.setMessage("Terjadi kesalahan saat melakukan login")
                                .setNegativeButton("Retry", null)
                                .create().show();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("Info Load", "Load Gagal "+t.toString());
                // Handle failure, e.g., show a dialog or toast indicating network failure
            }
        });
    }

    private void saveUserData(int customer_id, String nama, String email) {
        // Save user data in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("customer_id", customer_id);
        editor.putString("nama", nama);
        editor.putString("email", email);
        editor.apply();
    }
}