package com.example.lestarithriftshop.ui.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import static android.content.Context.MODE_PRIVATE;

import com.bumptech.glide.Glide;
import com.example.lestarithriftshop.MainActivity;
import com.example.lestarithriftshop.ui.auth.ActivityLogin;
import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.RegisterAPI;
import com.example.lestarithriftshop.ServerAPI;
import com.example.lestarithriftshop.databinding.FragmentProfileBinding;
import com.example.lestarithriftshop.ui.profile.riwayat_order.ActivityOrder;

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

public class ProfileFragment extends Fragment {
    TextView tv_email, tv_nama;
    ImageView imageView_profile;
    private FragmentProfileBinding binding;
    SharedPreferences sharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        sharedPreferences = requireContext().getSharedPreferences("userData", MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("email", "");
        if (savedEmail.isEmpty()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Login Required")
                    .setMessage("Login terlebih dahulu untuk mengakses Profile Kamu!")
                    .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent loginIntent = new Intent(requireContext(), ActivityLogin.class);
                            startActivity(loginIntent);
                            requireActivity().finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(requireContext(), MainActivity.class);
                            startActivity(intent);
                            requireActivity().finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }

        tv_email = root.findViewById(R.id.tv_profile_email);
        tv_nama = root.findViewById(R.id.tv_profile_nama);
        imageView_profile = root.findViewById(R.id.imageView_profile);

        String email = sharedPreferences.getString("email", "");
        String nama = sharedPreferences.getString("nama", "");
        getProfile(email);

        tv_email.setText(email);
        tv_nama.setText(nama);

        // Edit Profile
        CardView cardView_profile = root.findViewById(R.id.profile);
        cardView_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), ActivityEditProfile.class);
                startActivity(intent);
            }
        });

        // Riwayat Order
        CardView cardViewRiwayatOrder = root.findViewById(R.id.riwayat_order);
        cardViewRiwayatOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), ActivityOrder.class);
                startActivity(intent);
            }
        });

        // Ganti Password
        CardView cardViewGantiPassword = root.findViewById(R.id.ganti_password);
        cardViewGantiPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), ActivityGantiPassword.class);
                startActivity(intent);
            }
        });

        // Kontak Kami
        CardView cardViewContact = root.findViewById(R.id.kontak_kami);
        cardViewContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), ActivityContact.class);
                startActivity(intent);
            }
        });

        // Logout
        CardView cardViewLogout = root.findViewById(R.id.logout);
        cardViewLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogoutDialog();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        String email = sharedPreferences.getString("email", "");
        String nama = sharedPreferences.getString("nama", "");
        tv_email.setText(email);
        tv_nama.setText(nama);
        getProfile(email);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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

    private void LogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void logout() {
        Intent intent = new Intent(requireContext(), ActivityLogin.class);
        startActivity(intent);
        requireActivity().finish();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Toast.makeText(requireContext(), "Logout Berhasil", Toast.LENGTH_SHORT).show();
    }
}