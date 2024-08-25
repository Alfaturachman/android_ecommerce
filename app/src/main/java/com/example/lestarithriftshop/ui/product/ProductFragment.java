package com.example.lestarithriftshop.ui.product;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.RegisterAPI;
import com.example.lestarithriftshop.ServerAPI;
import com.example.lestarithriftshop.databinding.FragmentProductBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductFragment extends Fragment implements ProductAdapter.OnItemClickListener {
    private List<DataKatalog> results = new ArrayList<>();
    private List<DataKatalog> filteredResults = new ArrayList<>();
    private ProductAdapter viewAdapter;
    RecyclerView recyclerView;
    private FragmentProductBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ((AppCompatActivity) requireActivity()).getSupportActionBar().hide();

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        viewAdapter = new ProductAdapter(requireContext(), filteredResults, this);
        recyclerView.setAdapter(viewAdapter);

        SearchView searchView = root.findViewById(R.id.searchView);
        try {
            Field searchPlateField = searchView.getClass().getDeclaredField("mSearchPlate");
            searchPlateField.setAccessible(true);
            View searchPlate = (View) searchPlateField.get(searchView);
            searchPlate.setBackgroundColor(Color.TRANSPARENT);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("SearchView", "QueryTextChange: " + newText);
                viewAdapter.getFilter().filter(newText);
                return true;
            }
        });

        loadDataKatalog();

        return root;
    }

    @Override
    public void onItemClick(DataKatalog item) {
        Log.d("ItemClicked", "Item clicked: " + item.getId());
        Intent intent = new Intent(requireContext(), ActivityProductDetail.class);
        intent.putExtra("ID_PRODUK", item.getId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDataKatalog();
    }

    private void loadDataKatalog() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Call<Value> call = api.view();

        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Log the raw response body for debugging
                    Log.d("Raw Response", response.raw().toString());

                    // Clear and update 'results' with API response
                    results.clear();
                    results.addAll(response.body().getResult());

                    // Update 'filteredResults' for initial display
                    filteredResults.clear();
                    filteredResults.addAll(results);

                    // Update 'resultsFull' in the adapter with the full dataset
                    viewAdapter.resultsFull.clear();
                    viewAdapter.resultsFull.addAll(response.body().getResult());

                    // Notify adapter of data change
                    viewAdapter.notifyDataSetChanged();

                    // Optionally log the updated data size for debugging
                    Log.d("Info Load", "Results updated, total items: " + results.size());
                } else {
                    Log.i("Info Load", "Response unsuccessful or body null");
                }
            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                Log.i("Info Load", "Load failed: " + t.toString());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}