package com.example.lestarithriftshop.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.lestarithriftshop.ui.auth.ActivityLogin;
import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.RegisterAPI;
import com.example.lestarithriftshop.ServerAPI;
import com.example.lestarithriftshop.ui.product.ActivityProductDetail;
import com.example.lestarithriftshop.ui.product.DataKatalog;
import com.example.lestarithriftshop.ui.product.Value;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class HomeFragment extends Fragment implements HomeAdapter.OnItemClickListener {

    private TextView tv_welcome_text, tv_nama;
    private Button btn_login;
    ImageView imageView_profile;
    private SharedPreferences sharedPreferences;
    private RecyclerView recyclerView_terbaru, recyclerView_rekomendasi;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().hide();

        // Initialize views
        tv_welcome_text = view.findViewById(R.id.tv_welcome_text);
        tv_nama = view.findViewById(R.id.tv_nama);
        btn_login = view.findViewById(R.id.btn_login);
        imageView_profile = view.findViewById(R.id.imageView_profile);

        // Initialize RecyclerView Terbaru
        recyclerView_terbaru = view.findViewById(R.id.recyclerview_home_terbaru);
        RecyclerView.LayoutManager mLayoutManagerTerbaru = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView_terbaru.setLayoutManager(mLayoutManagerTerbaru);
        recyclerView_terbaru.setItemAnimator(new DefaultItemAnimator());

        // Initialize RecyclerView Rekomendasi
        recyclerView_rekomendasi = view.findViewById(R.id.recyclerview_home_rekomendasi);
        RecyclerView.LayoutManager mLayoutManagerRekomendasi = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView_rekomendasi.setLayoutManager(mLayoutManagerRekomendasi);
        recyclerView_rekomendasi.setItemAnimator(new DefaultItemAnimator());

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("userData", Context.MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("email", "");
        getProfile(savedEmail);
        if (savedEmail.isEmpty()) {
            // User is not logged in
            tv_welcome_text.setText("Anda belum login");
            tv_nama.setText("Silahkan login terlebih dahulu");
            btn_login.setVisibility(View.VISIBLE);
            CardView photo_profile = view.findViewById(R.id.photo_profile);
            photo_profile.setVisibility(View.GONE);
        } else {
            // User is logged in
            String nama = sharedPreferences.getString("nama", "");
            tv_nama.setText(nama);
            btn_login.setVisibility(View.GONE);
        }

        // Handle login button click
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), ActivityLogin.class);
                startActivity(intent);
            }
        });

        // Setup image slider
        ImageSlider imageSlider = view.findViewById(R.id.image_slider);
        ArrayList<SlideModel> slideModel = new ArrayList<>();
        slideModel.add(new SlideModel(R.drawable.banner_01, ScaleTypes.FIT));
        slideModel.add(new SlideModel(R.drawable.banner_02, ScaleTypes.FIT));
        slideModel.add(new SlideModel(R.drawable.banner_03, ScaleTypes.FIT));
        imageSlider.setImageList(slideModel, ScaleTypes.FIT);

        // Load data katalog
        loadDataKatalogTerbaru();
        loadDataKatalogRekomendasi();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDataKatalogTerbaru();
        loadDataKatalogRekomendasi();
    }

    private void loadDataKatalogTerbaru() {
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Call<Value> call = api.get_katalog_terbaru();

        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DataKatalog> results = response.body().getResult();

                    // Initialize and set adapter to RecyclerView
                    HomeAdapter terbaruAdapter = new HomeAdapter(requireContext(), results, HomeFragment.this);
                    recyclerView_terbaru.setAdapter(terbaruAdapter);
                    Log.d("LoadDataKatalogTerbaru", "Data loaded: " + results.size()); // Debug log
                } else {
                    Log.i("Info Load", "Respon Body Null atau tidak sukses");
                }
            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                Log.i("Info Load", "Load Gagal" + t.toString());
            }
        });
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

                        // Load gambar profil menggunakan Glide
                        String imageUrl = ServerAPI.BASE_URL_PROFILE + data.getString("foto_profile");
                        Log.d("Image URL", "URL: " + imageUrl);
                        if (!imageUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .into(imageView_profile);
                        }

                        Log.i("Info Profile", data.getString("nama"));
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

    private void loadDataKatalogRekomendasi() {
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Call<Value> call = api.get_katalog_rekomendasi();

        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DataKatalog> results = response.body().getResult();

                    // Initialize and set adapter to RecyclerView
                    HomeAdapter rekomendasiAdapter = new HomeAdapter(requireContext(), results, HomeFragment.this);
                    recyclerView_rekomendasi.setAdapter(rekomendasiAdapter);
                    Log.d("LoadDataKatalogRekomendasi", "Data loaded: " + results.size()); // Debug log
                } else {
                    Log.i("Info Load", "Respon Body Null atau tidak sukses");
                }
            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                Log.i("Info Load", "Load Gagal" + t.toString());
            }
        });
    }

    @Override
    public void onItemClick(DataKatalog item) {
        Log.d("HomeFragment", "Clicked item: " + item.getId());
        Intent intent = new Intent(requireContext(), ActivityProductDetail.class);
        intent.putExtra("ID_PRODUK", item.getId());
        requireContext().startActivity(intent);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}