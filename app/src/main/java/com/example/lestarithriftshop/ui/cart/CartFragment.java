package com.example.lestarithriftshop.ui.cart;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lestarithriftshop.ui.cart.checkout.ActivityCheckout;
import com.example.lestarithriftshop.ui.auth.ActivityLogin;
import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.databinding.FragmentCartBinding;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class CartFragment extends Fragment implements CartAdapter.OnItemDeleteListener {
    private Button btnBayar;
    private TextView totalBayar;
    private FragmentCartBinding binding;
    private CartAdapter cartAdapter;
    private ArrayList<DataCart> listProduct;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ((AppCompatActivity) requireActivity()).getSupportActionBar().hide();

        btnBayar = root.findViewById(R.id.btn_bayar);
        totalBayar = root.findViewById(R.id.total_bayar);

        listProduct = new ArrayList<>();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCartData();
    }

    private void updateCartData() {
        listProduct.clear(); // Clear the current list
        if (sharedPreferences.contains("listproduct")) {
            Gson gson = new Gson();
            String jsonText = sharedPreferences.getString("listproduct", null);
            DataCart[] products = gson.fromJson(jsonText, DataCart[].class);
            if (products != null) {
                for (DataCart product : products) {
                    listProduct.add(product);
                }
            }
            Log.i("info pref", "" + listProduct.toString());
        }
        cartAdapter.notifyDataSetChanged(); // Notify adapter of data change
        updateTotalPrice(); // Update the total price
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireContext().getSharedPreferences("cartData", MODE_PRIVATE);
        if (sharedPreferences.contains("listproduct")) {
            Gson gson = new Gson();
            String jsonText = sharedPreferences.getString("listproduct", null);
            DataCart[] products = gson.fromJson(jsonText, DataCart[].class);
            if (products != null) {
                for (DataCart product : products) {
                    listProduct.add(product);
                }
            }
            Log.i("info pref", "" + listProduct.toString());
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        cartAdapter = new CartAdapter(listProduct, sharedPreferences, true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));
        recyclerView.setAdapter(cartAdapter);

        // Calculate and display total price
        updateTotalPrice();

        SharedPreferences userPreferences = requireContext().getSharedPreferences("userData", MODE_PRIVATE);
        String savedEmail = userPreferences.getString("email", "");

        btnBayar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listProduct.isEmpty()) {
                    // Display message if cart is empty
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Keranjang Kosong")
                            .setMessage("Keranjang Anda kosong, silakan tambahkan produk terlebih dahulu.")
                            .setPositiveButton("OK", null)
                            .show();
                } else if (savedEmail.isEmpty()) {
                    // Alert for login and redirect to MainActivityLogin
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Peringatan Login")
                            .setMessage("Login terlebih dahulu untuk melanjutkan Checkout!")
                            .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent loginIntent = new Intent(requireContext(), ActivityLogin.class);
                                    startActivity(loginIntent);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    double totalPrice = calculateTotalPrice();
                    Intent intent = new Intent(requireContext(), ActivityCheckout.class);
                    // Pass the total price as a double extra
                    intent.putExtra("total_bayar", totalPrice);
                    startActivity(intent);
                }
            }
        });

        updateCartData();
        cartAdapter.setOnItemDeleteListener(this);
    }

    private void updateTotalPrice() {
        double totalPrice = cartAdapter.getTotalPrice();

        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        String formattedPrice = formatRupiah.format(totalPrice);
        formattedPrice = formattedPrice.replace(",00", "");

        totalBayar.setText(formattedPrice);
    }

    private double calculateTotalPrice() {
        double totalPrice = 0;
        for (DataCart cart : listProduct) {
            totalPrice += cart.getHarga();
        }
        Log.i("totalBayar checkout", "totalBayar: " + totalPrice);
        return totalPrice;
    }

    @Override
    public void onItemDeleted() {
        // Update listProduct by retrieving it from SharedPreferences
        Gson gson = new Gson();
        String jsonText = sharedPreferences.getString("listproduct", null);
        DataCart[] products = gson.fromJson(jsonText, DataCart[].class);
        listProduct.clear();
        if (products != null) {
            for (DataCart product : products) {
                listProduct.add(product);
            }
        }

        // Recalculate total price and update RecyclerView
        updateTotalPrice();
        cartAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
