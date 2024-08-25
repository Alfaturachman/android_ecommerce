package com.example.lestarithriftshop.ui.profile.riwayat_order;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.RegisterAPI;
import com.example.lestarithriftshop.ServerAPI;
import com.example.lestarithriftshop.ResponseUpload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

public class ActivityDetailOrder extends AppCompatActivity {

    TextView tv_invoice, tv_tanggal, tv_status, tv_kurir, tv_layanan, tv_nama_penerima, tv_alamat, tv_total_harga, tv_ongkos_kirim, tv_total_belanja;
    TextView tv_metode, tv_tanggal_pembayaran, tv_alert_pembayaran, tv_latitude, tv_longitude;
    ImageView imageView_bukti;
    ImageButton btn_kembali;
    RecyclerView recyclerViewDetailOrder;
    Button btn_upload_bukti, btn_konfirmasi, btn_batal;
    CardView card_bca;
    LinearLayout layout_bukti_pembayaran, layout_tanggal_pembayaran, layout_button;
    double totalSubTotal;
    String pathImage, valueInvoice, confirm_tanggal, confirm_jam, buktiPembayaran, metodePembayaran;
    private List<DataDetailOrder> productList = new ArrayList<>();
    private DetailOrderAdapter adapter;
    ProgressDialog pd;
    int REQUEST_GALLERY = 100, valueOrderId = 0;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_order);
        getSupportActionBar().hide();

        btn_kembali = findViewById(R.id.btn_kembali);
        btn_kembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityDetailOrder.this, ActivityOrder.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        tv_invoice = findViewById(R.id.invoice);
        tv_tanggal = findViewById(R.id.tanggal);
        tv_status = findViewById(R.id.status);
        tv_kurir = findViewById(R.id.kurir);
        tv_layanan = findViewById(R.id.layanan);
        tv_nama_penerima = findViewById(R.id.nama_penerima);
        tv_alamat = findViewById(R.id.alamat);
        tv_total_harga = findViewById(R.id.total_harga);
        tv_ongkos_kirim = findViewById(R.id.ongkos_kirim);
        tv_total_belanja = findViewById(R.id.total_belanja);
        imageView_bukti = findViewById(R.id.imageView_bukti);
        tv_metode = findViewById(R.id.tv_metode);
        tv_latitude = findViewById(R.id.tv_latitude);
        tv_longitude = findViewById(R.id.tv_longitude);

        tv_tanggal_pembayaran = findViewById(R.id.tv_tanggal_pembayaran);
        tv_alert_pembayaran = findViewById(R.id.tv_alert_pembayaran);

        card_bca = findViewById(R.id.card_bca);
        layout_bukti_pembayaran = findViewById(R.id.layout_bukti_pembayaran);
        layout_tanggal_pembayaran = findViewById(R.id.layout_tanggal_pembayaran);
        layout_button = findViewById(R.id.layout_button);

        String invoice = getIntent().getStringExtra("INVOICE");
        Log.d("Kode Produk", "Kode Produk dari intent: " + invoice);

        recyclerViewDetailOrder = findViewById(R.id.recycler_view_detail_order);
        recyclerViewDetailOrder.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DetailOrderAdapter(this, productList);
        recyclerViewDetailOrder.setAdapter(adapter);

        getDetailOrder(invoice);
        getOrderConfirm(valueOrderId);

        btn_upload_bukti = findViewById(R.id.btn_upload_bukti);
        btn_upload_bukti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Open Gallery"), REQUEST_GALLERY);
            }
        });

        btn_konfirmasi = findViewById(R.id.btn_konfirmasi);
        btn_konfirmasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("foto.png".equals(buktiPembayaran)) {
                    showUploadPaymentDialog();
                } else {
                    new AlertDialog.Builder(ActivityDetailOrder.this)
                            .setTitle("Konfirmasi")
                            .setMessage("Apakah Anda yakin ingin melanjutkan?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Melanjutkan dengan konfirmasi pembayaran dan navigasi ke ActivityTransaksi
                                    konfirmasiPembayaran(valueOrderId);
                                    Intent intent = new Intent(ActivityDetailOrder.this, ActivityTransaksi.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("INVOICE", valueInvoice);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("Tidak", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });

        btn_batal = findViewById(R.id.btn_batal);
        btn_batal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDetailOrder.this);
                builder.setTitle("Konfirmasi Pembatalan");
                builder.setMessage("Anda yakin ingin membatalkan order ini?");
                builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateOrderStatusDibatalkan(valueInvoice);
                        updateStockProduct(valueOrderId);
                        Intent intent = new Intent(ActivityDetailOrder.this, ActivityDetailOrder.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String invoice = getIntent().getStringExtra("INVOICE");
        getDetailOrder(invoice);
    }

    private void showUploadPaymentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDetailOrder.this);
        builder.setTitle("Upload Bukti Pembayaran");
        builder.setMessage("Anda belum mengunggah bukti pembayaran. Harap unggah bukti pembayaran untuk melanjutkan.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Implementasi navigasi atau tindakan yang diperlukan saat pengguna menekan OK
            }
        });
        builder.setCancelable(false); // Biarkan dialog tidak bisa di-cancel dengan tap di luar dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    void konfirmasiPembayaran(int idOrder) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Call<ResponseBody> call = api.postOrderConfirm(idOrder);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String message = jsonObject.getString("message");
                        Toast.makeText(ActivityDetailOrder.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ActivityDetailOrder.this, "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                AlertDialog.Builder msg = new AlertDialog.Builder(ActivityDetailOrder.this);
                msg.setMessage("Simpan Gagal, Error:" + t.toString()).setNegativeButton("coba lagi", null).create().show();
            }
        });
    }

    void getOrderConfirm(int v_order_id) {
        ServerAPI urlAPI = new ServerAPI();
        String URL = urlAPI.BASE_URL;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.getOrderConfirm(v_order_id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        Log.d("JSON Response", jsonResponse.toString());
                        int result = jsonResponse.getInt("result");
                        if (result == 1) {
                            JSONArray dataArray = jsonResponse.getJSONArray("data");

                            if (dataArray.length() > 0) {
                                JSONObject data = dataArray.getJSONObject(0);

                                // Ambil informasi bukti pembayaran
                                confirm_tanggal = data.getString("confirm_tanggal");
                                confirm_jam = data.getString("confirm_jam");
                                buktiPembayaran = data.getString("bukti_pembayaran");
                                metodePembayaran = data.getString("metode_pembayaran");

                                String formattedDate = formatDate(confirm_tanggal);
                                tv_tanggal_pembayaran.setText(formattedDate + ", " + confirm_jam + " WIB");
                                tv_metode.setText(metodePembayaran);

                                if ("Transfer Bank".equals(metodePembayaran)) {
                                    String imageUrl = ServerAPI.BASE_URL_PEMBAYARAN + buktiPembayaran;
                                    if (!imageUrl.isEmpty()) {
                                        Glide.with(ActivityDetailOrder.this)
                                                .load(imageUrl)
                                                .placeholder(R.drawable.ic_launcher_background)
                                                .error(R.drawable.ic_launcher_foreground)
                                                .into(imageView_bukti);
                                    }
                                } else {
                                    layout_tanggal_pembayaran.setVisibility(View.GONE);
                                    imageView_bukti.setVisibility(View.GONE);
                                }
                            } else {
                                Log.e("Error", "No data found in dataArray");
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.e("Error", "Result is not 1");
                        }
                    } else {
                        Log.e("Error", "Response unsuccessful");
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

    void getDetailOrder(String v_invoice) {
        ServerAPI urlAPI = new ServerAPI();
        String URL = urlAPI.BASE_URL;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.getDetailOrder(v_invoice).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    Log.d("JSON Response", jsonResponse.toString());

                    int result = jsonResponse.getInt("result");
                    if (result == 1) {
                        productList.clear();
                        totalSubTotal = 0;
                        boolean isCanceled = false;

                        JSONArray dataArray = jsonResponse.getJSONArray("data");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject data = dataArray.getJSONObject(i);

                            int order_id = data.getInt("order_id");
                            String invoice = data.getString("invoice");
                            String tanggal = data.getString("tanggal");
                            String jam = data.getString("jam");
                            double total_harga = data.getDouble("total_harga");
                            String nama_penerima = data.getString("nama");
                            String alamat = data.getString("alamat");
                            String kota = data.getString("kota");
                            String provinsi = data.getString("provinsi");
                            String kurir = data.getString("kurir");
                            String nama_layanan = data.getString("nama_layanan");
                            String deskripsi_kurir = data.getString("deskripsi_kurir");
                            double ongkos_kirim = data.getDouble("ongkos_kirim");
                            String status = data.getString("status");
                            String latitude = data.getString("latitude");
                            String longitude = data.getString("longitude");
                            double sub_total = data.getDouble("sub_total");
                            String product_merk = data.getString("product_merk");
                            int quantity = data.getInt("quantity");
                            double harga = data.getDouble("product_hargajual");
                            String FotoUrl = data.getString("product_foto");

                            productList.add(new DataDetailOrder(product_merk, FotoUrl, quantity, harga));

                            String formattedDate = formatDate(tanggal);
                            Locale localeID = new Locale("in", "ID");
                            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

                            String formattedOngkosKirim = formatRupiah.format(ongkos_kirim).replace(",00", "");
                            String formattedTotalHarga = formatRupiah.format(total_harga).replace(",00", "");

                            // Set values to TextViews
                            valueOrderId = order_id;
                            valueInvoice = invoice;
                            tv_invoice.setText("#" + invoice);
                            tv_tanggal.setText(formattedDate + ", " + jam + " WIB");
                            tv_status.setText(status);
                            tv_kurir.setText(kurir);
                            tv_layanan.setText(nama_layanan + " - " + deskripsi_kurir);
                            tv_nama_penerima.setText(nama_penerima);
                            tv_alamat.setText(alamat + ", " + kota + ", " + provinsi);
                            tv_ongkos_kirim.setText(formattedOngkosKirim);
                            tv_total_belanja.setText(formattedTotalHarga);
                            totalSubTotal = total_harga - ongkos_kirim;
                            tv_latitude.setText("Latitude = " + latitude);
                            tv_longitude.setText("Longitude = " + longitude);

                            // Set background tint based on status
                            getOrderConfirm(valueOrderId);
                            handleBadgeStatus(status);
                            handleStatusOrder(status);

                            if ("Dibatalkan".equals(status)) {
                                isCanceled = true;
                            }
                        }

                        Locale localeID = new Locale("in", "ID");
                        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
                        String formattedTotalSubTotal = formatRupiah.format(totalSubTotal).replace(",00", "");
                        tv_total_harga.setText(formattedTotalSubTotal);

                        adapter.notifyDataSetChanged();

                        if (isCanceled) {
                            updateOrderStatusDibatalkan(valueInvoice);
                        }
                    } else {
                        Log.e("Error", "Result is not 1");
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

    private void handleStatusOrder(String status) {
        if ("Belum Bayar".equals(status)) {
            layout_button.setVisibility(View.VISIBLE);
            card_bca.setVisibility(View.VISIBLE);
            layout_tanggal_pembayaran.setVisibility(View.GONE);
        } else if ("Dibatalkan".equals(status)) {
            layout_bukti_pembayaran.setVisibility(View.GONE);
        } else {
            layout_button.setVisibility(View.GONE);
            card_bca.setVisibility(View.GONE);
            tv_alert_pembayaran.setVisibility(View.GONE);
            tv_tanggal_pembayaran.setVisibility(View.VISIBLE);
        }
    }

    private void handleBadgeStatus(String status) {
        switch (status) {
            case "Belum Bayar":
                tv_status.setBackgroundTintList(ContextCompat.getColorStateList(
                        tv_status.getContext(), android.R.color.darker_gray));
                break;
            case "Diproses":
                tv_status.setBackgroundTintList(ContextCompat.getColorStateList(
                        tv_status.getContext(), android.R.color.holo_orange_dark));
                break;
            case "Dikirim":
                tv_status.setBackgroundTintList(ContextCompat.getColorStateList(
                        tv_status.getContext(), android.R.color.holo_blue_dark));
                break;
            case "Selesai":
                tv_status.setBackgroundTintList(ContextCompat.getColorStateList(
                        tv_status.getContext(), android.R.color.holo_green_dark));
                break;
            case "Dibatalkan":
                tv_status.setBackgroundTintList(ContextCompat.getColorStateList(
                        tv_status.getContext(), android.R.color.holo_red_dark));
                break;
            default:
                tv_status.setBackgroundTintList(ContextCompat.getColorStateList(
                        tv_status.getContext(), android.R.color.darker_gray));
                break;
        }
    }

    private void updateOrderStatusDibatalkan(String invoice) {
        ServerAPI urlAPI = new ServerAPI();
        String URL = urlAPI.BASE_URL;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.updateOrderStatus(invoice, "Dibatalkan").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("Update Status", "Berhasil mengubah status menjadi Dibatalkan");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.e("Update Status", "Gagal mengubah status");
            }
        });
    }

    private void updateStockProduct(int order_id) {
        ServerAPI urlAPI = new ServerAPI();
        String URL = urlAPI.BASE_URL;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.updateStock(order_id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("Stock Product Dibatalkan", "Berhasil mengembalikan stok produk");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.e("Update Status", "Gagal mengubah status");
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
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            Log.i("Error save image:", e.toString());
        }

        File imageFile = new File(pathImage);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-file"), imageFile);
        MultipartBody.Part partImage = MultipartBody.Part.createFormData("imageupload", imageFile.getName(), requestBody);

        // Initialize the ProgressDialog
        pd = new ProgressDialog(this);
        pd.setMessage("Uploading...");
        pd.setCancelable(false);
        pd.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);

        Call<ResponseUpload> upload = api.uploadBukti(valueOrderId, partImage);
        upload.enqueue(new Callback<ResponseUpload>() {
            @Override
            public void onResponse(Call<ResponseUpload> call, Response<ResponseUpload> response) {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getKode().equals("1")) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityDetailOrder.this);
                        alertDialogBuilder.setMessage(response.body().getPesan());
                        alertDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                            // Refresh Detail Order setelah upload berhasil
                            getDetailOrder(valueInvoice);
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } else {
                        Toast.makeText(ActivityDetailOrder.this, "Upload failed: " + response.body().getPesan(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ActivityDetailOrder.this, "Upload failed: Response is null or unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseUpload> call, Throwable t) {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                Toast.makeText(ActivityDetailOrder.this, "Upload failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        verifyStoragePermissions(ActivityDetailOrder.this);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            imageView_bukti.setImageURI(uri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                uploadImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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

    private String formatDate(String date) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat targetFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("in", "ID"));
            return targetFormat.format(originalFormat.parse(date));
        } catch (Exception e) {
            e.printStackTrace();
            return date;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}