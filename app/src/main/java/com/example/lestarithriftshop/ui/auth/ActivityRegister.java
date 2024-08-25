package com.example.lestarithriftshop.ui.auth;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ActivityRegister extends AppCompatActivity {
    Button btn_register, btn_halaman_login;
    ImageButton btn_kembali;
    EditText etNama, etEmail, etAlamat, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        etNama = findViewById(R.id.etNama);
        etEmail = findViewById(R.id.etEmail);
        etAlamat = findViewById(R.id.etAlamat);
        etPassword = findViewById(R.id.etPassword);

        btn_kembali = findViewById(R.id.btn_kembali);
        btn_kembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityRegister.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_register = findViewById(R.id.btn_daftar);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prosesSubmit (
                        etEmail.getText().toString(),
                        etNama.getText().toString(),
                        etAlamat.getText().toString(),
                        etPassword.getText().toString()
                );
            }
        });

        btn_halaman_login = findViewById(R.id.btn_halaman_login);
        btn_halaman_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityRegister.this, ActivityLogin.class);
                startActivity(intent);
            }
        });
    }
    public boolean isEmailValid (String email){
        boolean isValid = false;

        String expression = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr); // Mengubah dari Pattern.matcher(inputStr) menjadi pattern.matcher(inputStr)

        if (matcher.matches()){
            isValid=true;
        }
        return isValid;
    }
    void prosesSubmit (String vemail, String vnama, String valamat, String vPassword) {
        // Membuat instance Retrofit dengan menggunakan URL yang sudah ditentukan
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        if (!isEmailValid(vemail)) {
            AlertDialog.Builder msg = new AlertDialog.Builder(ActivityRegister.this);
            msg.setMessage("Email Tidak Valid").setNegativeButton("Coba Lagi", null).create().show();
            return;
        }

        api.register(vemail, vnama, valamat, vPassword).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string(); // Mengambil konten body sebagai string
                        JSONObject json = new JSONObject(responseData); // Membuat objek JSON dari string

                        int status = json.getInt("status");
                        int result = json.getInt("result");

                        if (status == 0 && result == 1) {
                            AlertDialog.Builder msg = new AlertDialog.Builder(ActivityRegister.this);
                            msg.setMessage("Register Berhasil").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Kembali ke MainActivity setelah berhasil mendaftar
                                    Intent intent = new Intent(ActivityRegister.this, ActivityLogin.class);
                                    startActivity(intent);
                                    finish(); // Menutup activity saat ini agar tidak kembali ke sini saat tombol back ditekan di MainActivity
                                }
                            }).create().show();
                            etNama.setText("");
                            etEmail.setText("");
                            etAlamat.setText("");
                            etPassword.setText("");
                        } else {
                            AlertDialog.Builder msg = new AlertDialog.Builder(ActivityRegister.this);
                            msg.setMessage("Simpan Gagal").setNegativeButton("Coba Lagi", null).create().show();
                        }
                    } else {
                        Log.e("API Response", "Response not successful: " + response.code());
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Info Register", "Register Gagal" + t.toString());
            }
        });
    }
}