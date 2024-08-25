package com.example.lestarithriftshop.ui.profile.riwayat_order;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.ServerAPI;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DetailOrderAdapter extends RecyclerView.Adapter<DetailOrderAdapter.ViewHolder> {

    private Context context;
    private List<DataDetailOrder> productList;

    public DetailOrderAdapter(Context context, List<DataDetailOrder> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_cart_checkout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataDetailOrder product = productList.get(position);

        // Formatting the price
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        String formattedPrice = formatRupiah.format(product.getHarga()).replace(",00", "");

        holder.tv_product_name.setText(product.getProduct_merk());
        holder.tv_harga.setText(product.getQuantity() + " x " + formattedPrice);

        ServerAPI urlAPI = new ServerAPI();
        String URL = urlAPI.URL_FOTO;
        String fullImageUrl = URL + "/" + product.getFotoUrl();
        Glide.with(context)
                .load(fullImageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.img_detail_order);

        Log.d("Photo URL Checkout", product.getFotoUrl());
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_product_name, tv_harga;
        ImageView img_detail_order;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_product_name = itemView.findViewById(R.id.tv_merk);
            tv_harga = itemView.findViewById(R.id.tv_harga);
            img_detail_order = itemView.findViewById(R.id.img_detail_order);
        }
    }
}