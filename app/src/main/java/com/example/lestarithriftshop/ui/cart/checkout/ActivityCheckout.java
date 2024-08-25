package com.example.lestarithriftshop.ui.cart.checkout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.RegisterAPI;
import com.example.lestarithriftshop.ServerAPI;
import com.example.lestarithriftshop.ui.cart.CartAdapter;
import com.example.lestarithriftshop.ui.cart.DataCart;
import com.example.lestarithriftshop.ui.profile.riwayat_order.ActivityOrder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ActivityCheckout extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private ArrayList<DataCart> listProductCheckout = new ArrayList<>();
    EditText et_alamat, et_nama;
    ImageButton btn_kembali;
    Button btn_checkout;
    RadioGroup radioGroupKurir;
    TextView tv_jasa_kurir, tv_jasa_service, tv_jasa_description, tv_jasa_estimasi, tvongkir, tv_total_harga, tv_ongkos_kirim, tv_total_belanja, total_belanja_checkout;
    Spinner spinprovinsi, spinKota;
    RadioGroup radioGroupMetode;
    RadioButton rb_cod, rb_transfer;
    LinearLayout jasaPengirimanLayout, section_kurir;
    ArrayList<String> province_name = new ArrayList<>();
    ArrayList<String> city_name = new ArrayList<>();
    ArrayList<Integer> province_id = new ArrayList<>();
    ArrayList<Integer> city_id = new ArrayList<>();
    SharedPreferences sharedPreferences;
    String email, valueOngkir, v_courierName, v_service, v_description, v_etd, metodePembayaran, status;
    int id_kota_tujuan, totalQuantity;
    double totalHarga, ongkosKirim, totalBelanja = 0.0, longitude, latitude;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        getSupportActionBar().hide();

        et_nama = findViewById(R.id.nama_penerima);
        et_alamat = findViewById(R.id.alamat);
        tv_jasa_kurir = findViewById(R.id.jasa_kurir);
        tv_jasa_service = findViewById(R.id.jasa_service);
        tv_jasa_description = findViewById(R.id.jasa_description);
        tv_jasa_estimasi = findViewById(R.id.jasa_estimasi);
        tvongkir = findViewById(R.id.jasa_ongkos_kirim);
        spinprovinsi = findViewById(R.id.spinner_provinsi);
        spinKota = findViewById(R.id.spinner_kota);
        tv_ongkos_kirim = findViewById(R.id.ongkos_kirim);
        tv_total_belanja = findViewById(R.id.total_belanja);
        total_belanja_checkout = findViewById(R.id.total_belanja_checkout);
        jasaPengirimanLayout = findViewById(R.id.jasa_pengiriman);
        section_kurir = findViewById(R.id.section_kurir);

        rb_cod = findViewById(R.id.rb_cod);
        rb_transfer = findViewById(R.id.rb_transfer);

        radioGroupMetode = findViewById(R.id.rg_metode);
        radioGroupMetode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_cod) {
                    metodePembayaran = "Bayar di Tempat (COD)";
                    status = "Diproses";
                } else if (checkedId == R.id.rb_transfer) {
                    metodePembayaran = "Transfer Bank";
                    status = "Belum Bayar";
                }
                Log.d("metodePembayaran", "Selected method: " + metodePembayaran);
            }
        });

        btn_kembali = findViewById(R.id.btn_kembali);
        btn_kembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_checkout = findViewById(R.id.btn_checkout);
        btn_checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String namaPenerima = et_nama.getText().toString().trim();
                String alamat = et_alamat.getText().toString().trim();
                String kota = spinKota.getSelectedItem().toString();
                String provinsi = spinprovinsi.getSelectedItem().toString();

                int selectedKurirId = radioGroupKurir.getCheckedRadioButtonId();
                String kurir = null;
                if (selectedKurirId != -1) {
                    RadioButton selectedRadioButton = findViewById(selectedKurirId);
                    kurir = selectedRadioButton.getText().toString();
                }

                int selectedMetodeId = radioGroupMetode.getCheckedRadioButtonId();
                String metode = null;
                if (selectedMetodeId != -1) {
                    RadioButton selectedRadioButton = findViewById(selectedMetodeId);
                    metode = selectedRadioButton.getText().toString();
                }

                // Check if latitude and longitude are available
                if (latitude == 0.0 || longitude == 0.0) {
                    Toast.makeText(ActivityCheckout.this, "Lokasi tidak tersedia. Harap periksa pengaturan lokasi Anda.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (namaPenerima.isEmpty() || alamat.isEmpty() || provinsi.equals("Pilih Provinsi") || kota.equals("Pilih Kota") || kurir == null || metode == null) {
                    showAlertDialog("Peringatan", "Harap lengkapi semua informasi sebelum melakukan checkout.");
                } else {
                    int id_user = sharedPreferences.getInt("customer_id", -1);

                    ArrayList<Integer> idProductList = new ArrayList<>();
                    ArrayList<Integer> quantityList = new ArrayList<>();
                    ArrayList<Double> subTotalList = new ArrayList<>();

                    for (DataCart cart : listProductCheckout) {
                        idProductList.add(cart.getId_product());
                        quantityList.add(cart.getJumlah_produk());
                        subTotalList.add(cart.getTotalHarga());
                    }

                    // Call the confirmation dialog with latitude and longitude
                    showConfirmationDialog(id_user, totalBelanja, namaPenerima, alamat, kota, provinsi, v_courierName, v_service, v_description, ongkosKirim, idProductList, quantityList, subTotalList, metodePembayaran, status, latitude, longitude);
                }
            }
        });

        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);
        email = sharedPreferences.getString("email",null);
        Log.i("Email", "Email from SharedPreferences: " + email);
        getProfile(email);

        recyclerView = findViewById(R.id.recyclerView_checkout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cartAdapter = new CartAdapter(listProductCheckout, getSharedPreferences("cartData", MODE_PRIVATE), false);
        recyclerView.setAdapter(cartAdapter);

        loadCartDataFromSharedPreferences();
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        double totalPrice = cartAdapter.getTotalPrice();
        totalQuantity = cartAdapter.getTotalQuantity();
        String formattedTotalHarga = formatRupiah.format(totalPrice).replace(",00", "");
        tv_total_harga = findViewById(R.id.total_harga);
        tv_total_harga.setText(formattedTotalHarga);

        calculateTotalHarga();

        radioGroupKurir = findViewById(R.id.rg_kurir);
        jasaPengirimanLayout.setVisibility(View.GONE);
        section_kurir.setVisibility(View.GONE);
        radioGroupKurir.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selectedRadioButton = findViewById(checkedId);
                String selectedKurir = selectedRadioButton.getText().toString().toLowerCase();
                Log.i("Selected Courier", selectedKurir);

                jasaPengirimanLayout.setVisibility(View.VISIBLE);

                cekOngkir("399", "" + id_kota_tujuan, totalQuantity * 200, selectedKurir);
            }
        });

        // Add default options
        province_name.add("Pilih Provinsi");
        province_id.add(0);
        city_name.add("Pilih Kota");
        city_id.add(0);

        // Load provinces and set up spinner
        load_provinsi();
        spinprovinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) { // Ensure default option is not selected
                    int selectedProvinceId = province_id.get(position);
                    String selectedProvinceName = province_name.get(position);
                    Log.i("Selected Province", "ID: " + selectedProvinceId + ", Name: " + selectedProvinceName);
                    // Load cities based on selected province
                    load_kota(selectedProvinceId);
                } else {
                    city_name.clear();
                    city_id.clear();
                    city_name.add("Pilih Kota");
                    city_id.add(0);
                    ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(ActivityCheckout.this,
                            android.R.layout.simple_spinner_dropdown_item, city_name);
                    spinKota.setAdapter(cityAdapter);

                    section_kurir.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinKota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) { // Ensure default option is not selected
                    id_kota_tujuan = city_id.get(position);
                    section_kurir.setVisibility(View.VISIBLE);
                } else {
                    section_kurir.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
        createLocationCallback();
    }

    private void loadCartDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("cartData", MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonText = sharedPreferences.getString("listproduct", null);

        if (jsonText != null) {
            DataCart[] products = gson.fromJson(jsonText, DataCart[].class);
            listProductCheckout.addAll(Arrays.asList(products));
            cartAdapter.notifyDataSetChanged(); // Memberitahu adapter bahwa data telah berubah
        }
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

                        et_nama.setText(data.getString("nama"));
                        et_alamat.setText(data.getString("alamat"));

                    } else {
                        // Tangani ketika data tidak ditemukan atau respons tidak sesuai
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

    public void load_provinsi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.rajaongkir.com/starter/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Call<ResponseBody> call = api.getProvince();
        Log.i("Load Province", "Preparing to enqueue");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONObject rajaongkirObject = json.getJSONObject("rajaongkir");
                    JSONArray resultsArray = rajaongkirObject.getJSONArray("results");

                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject provinceObject = resultsArray.getJSONObject(i);
                        String provinceName = provinceObject.getString("province");
                        String provinceId = provinceObject.getString("province_id");

                        Log.i("Province ID", provinceId);
                        Log.i("Province Name", provinceName);

                        // Add province names and IDs to lists
                        province_name.add(provinceName);
                        province_id.add(Integer.parseInt(provinceId));
                    }

                    ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(ActivityCheckout.this,
                            android.R.layout.simple_spinner_dropdown_item, province_name);
                    spinprovinsi.setAdapter(provinceAdapter);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("Load Province Failed", "Failure: " + t.getMessage());
            }
        });
    }

    public void load_kota(int province_id) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.rajaongkir.com/starter/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Log.i("Load City", "Preparing to enqueue with province_id: " + province_id);
        Call<ResponseBody> call = api.getCity(province_id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONObject rajaongkirObject = json.getJSONObject("rajaongkir");
                    JSONArray resultsArray = rajaongkirObject.getJSONArray("results");

                    city_name.clear();
                    city_id.clear();

                    city_name.add("Pilih Kota");
                    city_id.add(0);

                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject cityObject = resultsArray.getJSONObject(i);
                        int cityId = cityObject.getInt("city_id");
                        String cityName = cityObject.getString("city_name");

                        Log.i("City ID", String.valueOf(cityId));
                        Log.i("City Name", cityName);

                        // Menambahkan nama dan ID kota ke dalam list
                        city_name.add(cityName);
                        city_id.add(cityId);
                    }

                    ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(ActivityCheckout.this,
                            android.R.layout.simple_spinner_dropdown_item, city_name);
                    spinKota.setAdapter(cityAdapter); // Menetapkan adapter baru ke Spinner Kota
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("Load City Failed", "Failure: " + t.getMessage());
            }
        });
    }

    private void calculateTotalHarga() {
        String totalHargaStr = String.valueOf(cartAdapter.getTotalPrice());
        totalHarga = totalHargaStr.isEmpty() ? 0 : Double.parseDouble(totalHargaStr);
        ongkosKirim = valueOngkir == null || valueOngkir.isEmpty() ? 0 : Double.parseDouble(valueOngkir);

        totalBelanja = totalHarga + ongkosKirim;
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        String formattedTotalBelanja = formatRupiah.format(totalBelanja).replace(",00", "");
        tv_total_belanja.setText(formattedTotalBelanja);
        total_belanja_checkout.setText(formattedTotalBelanja);
    }

    public void cekOngkir(String asal, String tujuan, int berat, String kurir) {
        ProgressDialog progressDialog = new ProgressDialog(ActivityCheckout.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Menghitung Ongkos Kirim");
        progressDialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.rajaongkir.com/starter/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.cekOngkir(asal, tujuan, berat, kurir).enqueue(new Callback<ResponseBody>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Locale localeID = new Locale("in", "ID");
                    NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
                    try {
                        String jsonString = response.body().string();
                        JSONObject json = new JSONObject(jsonString);
                        Log.i("Response", json.toString());

                        JSONObject rajaongkirObject = json.getJSONObject("rajaongkir");
                        JSONArray resultsArray = rajaongkirObject.getJSONArray("results");

                        if (resultsArray.length() > 0) {
                            JSONObject resultObject = resultsArray.getJSONObject(0);
                            String courierName = resultObject.getString("name");
                            Log.d("Courier Name", courierName);

                            JSONArray costsArray = resultObject.getJSONArray("costs");

                            if (costsArray.length() > 0) {
                                JSONObject costsObject = costsArray.getJSONObject(0);
                                String service = costsObject.getString("service");
                                String description = costsObject.getString("description");

                                JSONArray costArray = costsObject.getJSONArray("cost");
                                if (costArray.length() > 0) {
                                    JSONObject costObject = costArray.getJSONObject(0);

                                    String value = costObject.getString("value");
                                    String etd = costObject.getString("etd");
                                    double ongkosKirim = Double.parseDouble(value);

                                    // Set the TextViews with the first service information
                                    v_courierName = courierName;
                                    v_service = service;
                                    v_description = description;
                                    v_etd = etd;
                                    tv_jasa_service.setText(service);
                                    tv_jasa_description.setText(description);
                                    tv_jasa_kurir.setText(courierName);
                                    tv_jasa_service.setText(service);
                                    tv_jasa_description.setText(description);
                                    if (kurir.equalsIgnoreCase("pos") && etd.toUpperCase().contains("HARI")) {
                                        etd = etd.toUpperCase().replace("HARI", "").trim();
                                    }
                                    tv_jasa_estimasi.setText("Estimasi : " + etd + " Hari");
                                    valueOngkir = value;

                                    String formattedOnkosKirim = formatRupiah.format(ongkosKirim).replace(",00", "");
                                    tvongkir.setText(formattedOnkosKirim);
                                    tv_ongkos_kirim.setText(formattedOnkosKirim);

                                    // Calculate and update the total payment
                                    calculateTotalHarga();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ActivityCheckout.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("API Call Failed", "Error: " + response.message());
                    Toast.makeText(ActivityCheckout.this, "API Call Failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("API Call Failed", "Error: " + t.getMessage());
                Toast.makeText(ActivityCheckout.this, "API Call Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Checkout(int id_user, double totalBelanja, String namaPenerima, String alamat, String kota, String provinsi, String kurir, String nama_layanan, String deskripsi_kurir, double ongkosKirim, ArrayList<Integer> id_productList, ArrayList<Integer> quantityList, ArrayList<Double> subTotalList, String metodePembayaran, String status, double latitude, double longitude) {
        // Create a JSON object for the post data
        JSONObject postData = new JSONObject();
        try {
            postData.put("id_user", id_user);
            postData.put("total_harga", totalBelanja);
            postData.put("nama", namaPenerima);
            postData.put("alamat", alamat);
            postData.put("kota", kota);
            postData.put("provinsi", provinsi);
            postData.put("kurir", kurir);
            postData.put("nama_layanan", nama_layanan);
            postData.put("deskripsi_kurir", deskripsi_kurir);
            postData.put("ongkos_kirim", ongkosKirim);
            postData.put("id_product", new JSONArray(id_productList));
            postData.put("quantity", new JSONArray(quantityList));
            postData.put("sub_total", new JSONArray(subTotalList));
            postData.put("status", status);
            postData.put("metode_pembayaran", metodePembayaran);
            postData.put("latitude", latitude);
            postData.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("Checkout Parameters", postData.toString());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                postData.toString()
        );
        Call<ResponseBody> call = api.checkoutJson(requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d("Response Debug", "Response: " + jsonResponse);
                        JSONObject jsonObject = new JSONObject(jsonResponse);

                        int status = jsonObject.getInt("status");
                        int result = jsonObject.getInt("result");
                        String message = jsonObject.getString("message");

                        if (status == 1 && result == 1) {
                            clearCartData();
                            Toast.makeText(ActivityCheckout.this, message, Toast.LENGTH_SHORT).show();

                            Intent intent;
                            if (metodePembayaran.equals("Bayar di Tempat (COD)")) {
                                intent = new Intent(ActivityCheckout.this, ActivityOrder.class);
                            } else if (metodePembayaran.equals("Transfer Bank")) {
                                intent = new Intent(ActivityCheckout.this, ActivitySuccess.class);
                                intent.putExtra("JUMLAH_BAYAR", totalBelanja);
                            } else {
                                intent = new Intent(ActivityCheckout.this, ActivityOrder.class); // Default action
                            }

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ActivityCheckout.this, message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        Log.e("Checkout Error", "Error: " + e.getMessage());
                        Toast.makeText(ActivityCheckout.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ActivityCheckout.this, "Error during checkout", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ActivityCheckout.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityCheckout.this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showConfirmationDialog(int id_user, double totalBelanja, String namaPenerima, String alamat, String kota, String provinsi, String v_courierName, String v_service, String v_description, double ongkosKirim, ArrayList<Integer> idProductList, ArrayList<Integer> quantityList, ArrayList<Double> subTotalList, String metodePembayaran, String status, double latitude, double longitude) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityCheckout.this);
        builder.setTitle("Konfirmasi Checkout")
                .setMessage("Apakah Anda yakin ingin melanjutkan ke Checkout Pemabayaran?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Checkout(id_user, totalBelanja, namaPenerima, alamat, kota, provinsi, v_courierName, v_service, v_description, ongkosKirim, idProductList, quantityList, subTotalList, metodePembayaran, status, latitude, longitude);
                    }
                })
                .setNegativeButton("Tidak", null)
                .show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates();
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) { // Correct the parameter type to LocationResult
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        latitude = location.getLatitude(); // Corrected variable assignment
                        longitude = location.getLongitude(); // Corrected variable assignment
                        Log.d("Location Update", "Lat: " + latitude + ", Lon: " + longitude); // Add logging for debugging
                    }
                }
            }
        };
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        latitude = location.getLongitude();
                        longitude = location.getLatitude();
//                        et_koordinat.setText(locationStr);

                    } else {
//                        longitude.setText("Location not available. Requesting updates...");
                        requestLocationUpdates();
                    }
                }
            });
        } else {
            checkLocationPermission();
        }
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            checkLocationPermission();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void clearCartData() {
        SharedPreferences.Editor editor = getSharedPreferences("cartData", MODE_PRIVATE).edit();
        editor.remove("listproduct");
        editor.apply();
        listProductCheckout.clear();
        cartAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        // Do nothing to disable the back button
        super.onBackPressed();
    }
}
