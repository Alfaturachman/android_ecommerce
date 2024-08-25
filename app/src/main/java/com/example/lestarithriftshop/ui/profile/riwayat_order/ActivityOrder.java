package com.example.lestarithriftshop.ui.profile.riwayat_order;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.RegisterAPI;
import com.example.lestarithriftshop.ServerAPI;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ActivityOrder extends AppCompatActivity implements OrderAdapter.OnItemClickListener {

    ImageButton imgbtn_kembali;
    TabLayout tabLayout;
    TableLayout tvNoOrders;
    RecyclerView recyclerView;
    SharedPreferences sharedPreferences;
    int customerId = 0;
    private List<DataOrder> orderList;
    private List<DataOrder> filteredOrderList;
    private OrderAdapter viewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_order);
        getSupportActionBar().hide();

        imgbtn_kembali = findViewById(R.id.btn_kembali);
        imgbtn_kembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);
        customerId = sharedPreferences.getInt("customer_id", -1);

        recyclerView = findViewById(R.id.recycler_view_riwayat_order);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        orderList = new ArrayList<>();
        filteredOrderList = new ArrayList<>();
        viewAdapter = new OrderAdapter(this, filteredOrderList);
        recyclerView.setAdapter(viewAdapter);
        viewAdapter.setOnItemClickListener(this);

        tvNoOrders = findViewById(R.id.tv_no_orders);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterOrders(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                filterOrders(tab.getText().toString());
            }
        });

        listOrder();
    }

    @Override
    public void onItemClick(DataOrder order) {
        Intent intent = new Intent(ActivityOrder.this, ActivityDetailOrder.class);
        intent.putExtra("INVOICE", order.getInvoice());
        Log.d("ItemClicked", "Item clicked: " + order.getInvoice());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listOrder();
    }

    private void listOrder() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI registerAPI = retrofit.create(RegisterAPI.class);
        Call<ValueOrder> call = registerAPI.getOrder(customerId);
        call.enqueue(new Callback<ValueOrder>() {
            @Override
            public void onResponse(Call<ValueOrder> call, Response<ValueOrder> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DataOrder> results = response.body().getResult();
                    if (results != null) {
                        orderList.clear();
                        orderList.addAll(results);
                        filterOrders(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString());
                        Log.d("LoadDataRiwayatOrder", "Data loaded: " + results.size());
                    } else {
                        Log.i("Info Load", "Results list is null");
                    }
                } else {
                    Log.i("Info Load", "Response body is null or not successful");
                }
            }

            @Override
            public void onFailure(Call<ValueOrder> call, Throwable t) {
                Log.e("API Call", "Error: " + t.getMessage());
            }
        });
    }

    private void filterOrders(String status) {
        filteredOrderList.clear();
        for (DataOrder order : orderList) {
            if (order.getStatus().equals(status)) {
                filteredOrderList.add(order);
            }
        }
        viewAdapter.notifyDataSetChanged();
        tvNoOrders.setVisibility(filteredOrderList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
