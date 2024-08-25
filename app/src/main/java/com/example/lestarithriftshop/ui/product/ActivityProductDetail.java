package com.example.lestarithriftshop.ui.product;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.lestarithriftshop.CustomToast;
import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.RegisterAPI;
import com.example.lestarithriftshop.ServerAPI;
import com.example.lestarithriftshop.ui.cart.DataCart;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ActivityProductDetail extends AppCompatActivity {
    TextView tv_merek, tv_harga, tv_kategori, tv_pengunjung, tv_stok, tv_deskripsi;
    ImageView imageView_product;
    ImageButton btn_kembali;
    Button btn_cart;
    ArrayList<DataCart> listProduct;
    SharedPreferences sharedPreferences;
    int total_stock, id_product;
    String hasil_ukuran;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        getSupportActionBar().hide();

        sharedPreferences = getSharedPreferences("cartData", MODE_PRIVATE);
        if (sharedPreferences.contains("listproduct")) {
            Gson getgson = new Gson();
            String getjsonText = sharedPreferences.getString("listproduct", null);
            DataCart[] product = getgson.fromJson(getjsonText, DataCart[].class);
            listProduct = new ArrayList<>();
            for (DataCart cart : product) {
                listProduct.add(cart);
            }
        } else {
            listProduct = new ArrayList<>();
        }

        imageView_product = findViewById(R.id.imageView_product);
        tv_merek = findViewById(R.id.tv_judul);
        tv_harga = findViewById(R.id.tv_harga);
        tv_kategori = findViewById(R.id.tv_kategori);
        tv_pengunjung = findViewById(R.id.tv_pengunjung);
        tv_stok = findViewById(R.id.tv_stok);
        tv_deskripsi = findViewById(R.id.tv_deskripsi);
        btn_kembali = findViewById(R.id.btn_kembali);
        btn_cart = findViewById(R.id.btn_cart);

        // Mendapatkan kode produk dari intent
        int idProduk = getIntent().getIntExtra("ID_PRODUK", -1);
        Log.d("Kode Produk", "ID Produk dari intent: " + idProduk);
        getProduct(idProduk);

        btn_kembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    void getProduct(int v_idProduk) {
        ServerAPI urlAPI = new ServerAPI();
        String URL = urlAPI.BASE_URL;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.getKodeKatalog(v_idProduk).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    Log.d("JSON Response", jsonResponse.toString());
                    if (jsonResponse.getInt("result") == 1) {
                        ServerAPI urlAPI = new ServerAPI();
                        String URL_FOTO = urlAPI.URL_FOTO;

                        JSONObject data = jsonResponse.getJSONObject("data");
                        String id = data.getString("id");
                        String merk = data.getString("merk");
                        String kategori = data.getString("kategori");
                        String pengunjung = data.getString("pengunjung");
                        String harga = data.getString("hargajual");
                        String stok = data.getString("stok");
                        String ukuran = data.getString("ukuran");
                        String deskripsi = data.getString("deskripsi");

                        Locale localeID = new Locale("in", "ID");
                        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

                        try {
                            String foto = data.getString("foto");  // Get the filename directly
                            String imageURL = URL_FOTO + "/" + foto;
                            Glide.with(ActivityProductDetail.this)
                                    .load(imageURL)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .into(imageView_product);

                            double hargaDouble = Double.parseDouble(harga);
                            String formattedPrice = formatRupiah.format(hargaDouble).replace(",00", "");

                            total_stock = Integer.parseInt(stok);
                            hasil_ukuran = (ukuran);

                            imageView_product.setTag(foto);
                            id_product = Integer.parseInt(id);
                            tv_merek.setText(merk);
                            tv_pengunjung.setText("Produk ini telah dilihat " + pengunjung + " kali");
                            tv_harga.setText(formattedPrice);
                            tv_kategori.setText(kategori);
                            tv_deskripsi.setText(deskripsi);

                            if (total_stock > 0) {
                                tv_stok.setText("STOK : " + stok);
                                tv_stok.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ActivityProductDetail.this, R.color.green_500)));
                                tv_stok.setTextColor(ContextCompat.getColor(ActivityProductDetail.this, R.color.white));
                            } else {
                                tv_stok.setText("STOK HABIS");
                                tv_stok.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ActivityProductDetail.this, R.color.red_500)));
                                tv_stok.setTextColor(ContextCompat.getColor(ActivityProductDetail.this, R.color.white));
                            }

                            setupCartButton(total_stock);

                            switch (ukuran) {
                                case "S":
                                    TextView smallTextView = findViewById(R.id.small);
                                    smallTextView.setBackground(ContextCompat.getDrawable(ActivityProductDetail.this, R.drawable.badge_terpilih));
                                    smallTextView.setTextColor(Color.WHITE);
                                    smallTextView.setPadding(dpToPx(15), dpToPx(5), dpToPx(15), dpToPx(5));
                                    break;
                                case "M":
                                    TextView mediumTextView = findViewById(R.id.medium);
                                    mediumTextView.setBackground(ContextCompat.getDrawable(ActivityProductDetail.this, R.drawable.badge_terpilih));
                                    mediumTextView.setTextColor(Color.WHITE);
                                    mediumTextView.setPadding(dpToPx(15), dpToPx(5), dpToPx(15), dpToPx(5));
                                    break;
                                case "L":
                                    TextView largeTextView = findViewById(R.id.large);
                                    largeTextView.setBackground(ContextCompat.getDrawable(ActivityProductDetail.this, R.drawable.badge_terpilih));
                                    largeTextView.setTextColor(Color.WHITE);
                                    largeTextView.setPadding(dpToPx(15), dpToPx(5), dpToPx(15), dpToPx(5));
                                    break;
                                case "XL":
                                    TextView extraLargeTextView = findViewById(R.id.extra_large);
                                    extraLargeTextView.setBackground(ContextCompat.getDrawable(ActivityProductDetail.this, R.drawable.badge_terpilih));
                                    extraLargeTextView.setTextColor(Color.WHITE);
                                    extraLargeTextView.setPadding(dpToPx(15), dpToPx(5), dpToPx(15), dpToPx(5));
                                    break;
                                default:
                                    break;
                            }

                        } catch (NumberFormatException | JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("Error", "Kode Product not found or response not valid");
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

    private void setupCartButton(int stock) {
        btn_cart.setEnabled(stock > 0);

        if (stock > 0) {
            btn_cart.setOnClickListener(v -> {
                // Logika untuk menambahkan produk ke keranjang
                String hargaText = tv_harga.getText().toString().replace("Rp", "").replace(".", "");
                double harga = Double.parseDouble(hargaText);
                String foto = imageView_product.getTag().toString();

                DataCart product = new DataCart(
                        id_product,
                        tv_merek.getText().toString(),
                        harga,
                        1,
                        stock,
                        hasil_ukuran,
                        tv_kategori.getText().toString(),
                        foto
                );

                boolean merkExists = false;
                for (DataCart cart : listProduct) {
                    if (cart.getMerk().equals(product.getMerk())) {
                        merkExists = true;
                        int currentQuantity = cart.getJumlah_produk();
                        int newQuantity = currentQuantity + 1;
                        if (newQuantity > stock) {
                            CustomToast.showToast(getApplicationContext(), "Sudah ditambahkan di keranjang", 1500);
                        } else {
                            cart.setJumlah_produk(newQuantity);
                            CustomToast.showToast(getApplicationContext(), "Berhasil ditambahkan di keranjang", 1500);
                        }
                        break;
                    }
                }

                if (!merkExists) {
                    listProduct.add(product);
                    CustomToast.showToast(getApplicationContext(), "Berhasil ditambahkan di keranjang", 1500);
                }

                Gson gson = new Gson();
                String jsonText = gson.toJson(listProduct);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("listproduct", jsonText);
                editor.apply();
            });
        } else {
            btn_cart.setOnClickListener(null);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}