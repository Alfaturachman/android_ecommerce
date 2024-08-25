package com.example.lestarithriftshop.ui.product;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.RegisterAPI;
import com.example.lestarithriftshop.ServerAPI;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> implements Filterable {
    private Context context;
    private List<DataKatalog> results;
    List<DataKatalog> resultsFull;
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(DataKatalog item);
    }

    public ProductAdapter(Context context, List<DataKatalog> results, OnItemClickListener listener) {
        this.context = context;
        this.results = results;
        this.resultsFull = new ArrayList<>(results); // Initialize the full list
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataKatalog result = results.get(position);

        ServerAPI urlAPI = new ServerAPI();
        String URL = urlAPI.URL_FOTO;
        Glide.with(context)
                .load(URL + "/" + result.getFoto())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.imgKatalog);

        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        String formattedPrice = formatRupiah.format(result.getHargajual()).replace(",00", "");

        holder.tv_judul.setText(result.getMerk());
        holder.tv_harga.setText(formattedPrice);
        holder.tv_pengunjung.setText(("Dilihat : " + result.getPengunjung()));
        holder.tv_kategori.setText(result.getKategori());
        holder.tv_stok.setText("Stok : " + result.getStok());

        if (result.getStok() > 0) {
            holder.tv_stok.setText("Stok : " + result.getStok());
            holder.tv_stok.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green_200))); // Default background tint
            holder.tv_stok.setTextColor(ContextCompat.getColor(context, R.color.green_500)); // Default text color
        } else {
            holder.tv_stok.setText("Stok Habis");
            holder.tv_stok.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red_200))); // Red background tint
            holder.tv_stok.setTextColor(ContextCompat.getColor(context, R.color.red_500));
        }

        holder.cardView_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ActivityProductDetail.class);
                intent.putExtra("ID_PRODUK", result.getId());
                context.startActivity(intent);

                Log.d("Product Detail Product", "Product code: " + result.getId());
                tambahPengunjung(result.getId());
            }
        });
    }

    private void tambahPengunjung(int productID) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Call<ResponseBody> call = api.updatePengunjung(productID);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("IncrementPengunjung", "Successfully incremented pengunjung for product code: " + productID);
                } else {
                    Log.e("IncrementPengunjung", "Failed to increment pengunjung. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("IncrementPengunjung", "Failed to increment pengunjung. Error: " + t.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Log.d("Filter", "performFiltering: " + constraint); // Debug log
                List<DataKatalog> filteredList = new ArrayList<>();

                if (TextUtils.isEmpty(constraint)) {
                    filteredList.addAll(resultsFull); // Show all items if query is empty
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    Log.d("Filter", "Filter Pattern: " + filterPattern); // Debug log
                    for (DataKatalog item : resultsFull) {
                        if (item.getMerk().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                Log.d("Filter", "Filtered List Size: " + filteredList.size()); // Debug log
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                Log.d("Filter", "publishResults: " + results.values); // Debug log
                ProductAdapter.this.results.clear();
                if (results.values != null) {
                    ProductAdapter.this.results.addAll((List<DataKatalog>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imgKatalog;
        public TextView tv_judul, tv_harga, tv_kategori, tv_pengunjung, tv_stok;
        public RelativeLayout relativeLayout;
        public CardView cardView_product;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView_product = itemView.findViewById(R.id.cardView_product);
            imgKatalog = itemView.findViewById(R.id.imgKatalog);
            tv_judul = itemView.findViewById(R.id.tv_judul);
            tv_harga = itemView.findViewById(R.id.tv_harga);
            tv_kategori = itemView.findViewById(R.id.tv_kategori);
            tv_pengunjung = itemView.findViewById(R.id.tv_pengunjung);
            tv_stok = itemView.findViewById(R.id.tv_stok);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(results.get(position));
            }
        }
    }
}