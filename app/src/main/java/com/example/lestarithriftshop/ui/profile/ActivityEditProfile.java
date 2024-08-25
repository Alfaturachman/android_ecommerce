package com.example.lestarithriftshop.ui.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.RegisterAPI;
import com.example.lestarithriftshop.ResponseUpload;
import com.example.lestarithriftshop.ServerAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ActivityEditProfile extends AppCompatActivity {
    EditText etNama, etAlamat, etNik, etKota, etProvinsi, etTelp, etKodepos;
    String email;
    Button btn_submit, btn_pilih;
    ImageButton imgbtn_kembali;
    ImageView imageView_profile;
    String pathImage;
    ProgressDialog pd;
    int REQUEST_GALLERY = 100, customer_id;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    SharedPreferences sharedPreferences;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);

        etNama = findViewById(R.id.etProfile_nama);
        etNik = findViewById(R.id.etProfile_nik);
        etAlamat = findViewById(R.id.etProfile_alamat);
        etKota = findViewById(R.id.etProfile_kota);
        etProvinsi = findViewById(R.id.etProfile_provinsi);
        etTelp = findViewById(R.id.etProfile_notelp);
        etKodepos = findViewById(R.id.etProfile_kodepos);

        imageView_profile = findViewById(R.id.imageView_profile);
        pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        btn_pilih = findViewById(R.id.btn_pilih);
        btn_pilih.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Open Gallery"), REQUEST_GALLERY);
            }
        });

        imgbtn_kembali = findViewById(R.id.btn_kembali);
        imgbtn_kembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        email = sharedPreferences.getString("email", null);
        customer_id = sharedPreferences.getInt("customer_id", -1);
        Log.i("Email", "Email from SharedPreferences: " + email);
        getProfile(email);

        btn_submit = findViewById(R.id.btnProfile_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    DataUser data = new DataUser();
                    data.setAlamat(etAlamat.getText().toString());
                    data.setNama(etNama.getText().toString());
                    data.setNik(etNik.getText().toString());
                    data.setKota(etKota.getText().toString());
                    data.setProvinsi(etProvinsi.getText().toString());
                    data.setTelp(etTelp.getText().toString());
                    data.setKodepos(etKodepos.getText().toString());
                    data.setEmail(email);
                    updateProfile(data);
                } else {
                    showAlert("Data Belum Diisi Semua!.");
                }
            }
        });
    }

    private void uploadImage(Bitmap bitmap) {
        // Direktori untuk menyimpan gambar yang diunggah
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Upload");
        dir.mkdirs();
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outfile = new File(dir, fileName);

        try (FileOutputStream outStream = new FileOutputStream(outfile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            pathImage = outfile.getAbsolutePath();
            Log.i("Info path upload:", pathImage);
        } catch (Exception e) {
            pd.dismiss();
            Log.i("Error save image:", e.toString());
        }

        File imageFile = new File(pathImage);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-file"), imageFile);
        MultipartBody.Part partImage = MultipartBody.Part.createFormData("imageupload", imageFile.getName(), requestBody);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);

        Call<ResponseUpload> upload = api.uploadImage(customer_id, partImage);
        upload.enqueue(new Callback<ResponseUpload>() {
            @Override
            public void onResponse(Call<ResponseUpload> call, Response<ResponseUpload> response) {
                pd.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getKode().equals("1")) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityEditProfile.this);
                        alertDialogBuilder.setMessage(response.body().getPesan());
                        alertDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                            // Refresh profil setelah upload berhasil
                            getProfile(email);
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } else {
                        Toast.makeText(ActivityEditProfile.this, "Upload failed: " + response.body().getPesan(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ActivityEditProfile.this, "Upload failed: Response is null or unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseUpload> call, Throwable t) {
                pd.dismiss();
                Toast.makeText(ActivityEditProfile.this, "Upload failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Metode untuk menangani hasil dari pemilihan gambar dari galeri
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        verifyStoragePermissions(ActivityEditProfile.this);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            imageView_profile.setImageURI(uri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                uploadImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Metode untuk memverifikasi izin penyimpanan
    public void verifyStoragePermissions(Activity activity) {
        int permissionRead = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWrite = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionRead != PackageManager.PERMISSION_GRANTED || permissionWrite != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private boolean validateFields() {
        return !etNama.getText().toString().isEmpty() &&
                !etAlamat.getText().toString().isEmpty() &&
                !etNik.getText().toString().isEmpty() &&
                !etKota.getText().toString().isEmpty() &&
                !etProvinsi.getText().toString().isEmpty() &&
                !etTelp.getText().toString().isEmpty() &&
                !etKodepos.getText().toString().isEmpty();
    }

    private void showAlert(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityEditProfile.this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("OK", null);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void getProfile(String v_email) {
        ServerAPI urlAPI = new ServerAPI();
        String URL = urlAPI.BASE_URL;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.getProfile(v_email).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    Log.d("JSON Response", jsonResponse.toString());
                    if (jsonResponse.getInt("result") == 1) {
                        JSONObject data = jsonResponse.getJSONObject("data");

                        etNama.setText(data.getString("nama"));
                        etNik.setText(data.getString("nik"));
                        etAlamat.setText(data.getString("alamat"));
                        etKota.setText(data.getString("kota"));
                        etProvinsi.setText(data.getString("provinsi"));
                        etTelp.setText(data.getString("telp"));
                        etKodepos.setText(data.getString("kodepos"));

                        // Load gambar profil menggunakan Glide
                        String imageUrl = ServerAPI.BASE_URL_PROFILE + data.getString("foto_profile");
                        Log.d("Image URL", "URL: " + imageUrl);
                        DataUser dataUser = new DataUser();
                        dataUser.setPhoto_profile(imageUrl);
                        if (!imageUrl.isEmpty()) {
                            Glide.with(ActivityEditProfile.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .into(imageView_profile);
                        }

                        Log.i("Info Profile", data.getString("nama"));
                    } else {

                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    void updateProfile(DataUser data) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Call<ResponseBody> call = api.updateProfile(data.getNama(),
                data.getAlamat(),
                data.getNik(),
                data.getKota(),
                data.getProvinsi(),
                data.getTelp(),
                data.getKodePos(),
                data.getEmail());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String message = jsonObject.getString("message");
                        Toast.makeText(ActivityEditProfile.this, message, Toast.LENGTH_SHORT).show();

                        updateNamaSharedPreferences(data.getNama());

                        getProfile(data.getEmail());
                    } else {
                        Toast.makeText(ActivityEditProfile.this, "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                AlertDialog.Builder msg = new AlertDialog.Builder(ActivityEditProfile.this);
                msg.setMessage("Simpan Gagal, Error:" + t.toString()).setNegativeButton("coba lagi", null).create().show();
            }
        });
    }

    private void updateNamaSharedPreferences(String newName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nama", newName);
        editor.apply();
    }
}