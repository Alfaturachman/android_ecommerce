package com.example.lestarithriftshop.ui.cart;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lestarithriftshop.CustomToast;
import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.ServerAPI;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private ArrayList<DataCart> cartProduct;
    private SharedPreferences sharedPreferences;
    private boolean showCheckoutButton;

    public CartAdapter(ArrayList<DataCart> listProduct, SharedPreferences sharedPreferences, boolean showCheckoutButton) {
        this.cartProduct = listProduct;
        this.sharedPreferences = sharedPreferences;
        this.showCheckoutButton = showCheckoutButton;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.list_cart, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                deleteItem(position);
            }
        });

        return viewHolder;
    }

    public interface OnItemDeleteListener {
        void onItemDeleted();
    }

    private OnItemDeleteListener onItemDeleteListener;

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.onItemDeleteListener = listener;
    }

    private void deleteItem(int position) {
        cartProduct.remove(position);
        saveProductListToSharedPreferences();
        notifyDataSetChanged();
        notifyItemRemoved(position);
        if (onItemDeleteListener != null) {
            onItemDeleteListener.onItemDeleted();
        }
    }

    private void saveProductListToSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String jsonText = gson.toJson(cartProduct);
        editor.putString("listproduct", jsonText);
        editor.apply();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        DataCart product = cartProduct.get(position);

        // Load image using Glide
        ServerAPI urlAPI = new ServerAPI();
        String URL = urlAPI.URL_FOTO;
        String fullImageUrl = URL + "/" + product.getFotoUrl();
        Glide.with(holder.itemView.getContext())  // Use holder.itemView.getContext() to get the context
                .load(fullImageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.imgKatalog);

        holder.tvProduct.setText(product.getMerk());
        holder.tvUkuran.setText(product.getUkuran());
        holder.tvKategori.setText(product.getKategori());
        holder.tvJumlahProduk.setText(String.valueOf(product.getJumlah_produk()));

        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        double hargaProduk = product.getTotalHarga();
        String formattedPrice = formatRupiah.format(hargaProduk).replace(",00", "");

        if (showCheckoutButton) {
            holder.delete.setVisibility(View.VISIBLE);
            holder.btn_minplus.setVisibility(View.VISIBLE);
            holder.tvPrice.setText(formattedPrice);
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog(holder.itemView.getContext(), position);
                }
            });
        } else {
            // Display quantity and total price
            String totalPriceText = String.format("%dx %s", product.getJumlah_produk(), formattedPrice);
            holder.tvPrice.setText(totalPriceText);

            holder.delete.setVisibility(View.GONE);
            holder.btn_minplus.setVisibility(View.GONE);
        }

        holder.btn_tambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = Integer.parseInt(holder.tvJumlahProduk.getText().toString());
                int total_stock = product.getStok();
                if (currentQuantity < total_stock) {
                    currentQuantity++;
                    holder.tvJumlahProduk.setText(String.valueOf(currentQuantity));
                    product.setJumlah_produk(currentQuantity); // Mengatur jumlah_produk di objek DataCart
                    saveProductListToSharedPreferences();
                    holder.tvPrice.setText(formatRupiah.format(product.getTotalHarga()).replace(",00", ""));
                    if (onItemDeleteListener != null) {
                        onItemDeleteListener.onItemDeleted(); // Memperbarui totalBayar di CartFragment
                    }
                } else {
                    CustomToast.showToast(holder.itemView.getContext(), "Jumlah Sudah Maksimal", 1500);
                }
            }
        });

        holder.btn_kurang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = Integer.parseInt(holder.tvJumlahProduk.getText().toString());
                if (currentQuantity > 1) {
                    currentQuantity--;
                    holder.tvJumlahProduk.setText(String.valueOf(currentQuantity));
                    product.setJumlah_produk(currentQuantity); // Mengatur jumlah_produk di objek DataCart
                    saveProductListToSharedPreferences(); // Simpan perubahan ke SharedPreferences
                    holder.tvPrice.setText(formatRupiah.format(product.getTotalHarga()).replace(",00", ""));
                    if (onItemDeleteListener != null) {
                        onItemDeleteListener.onItemDeleted(); // Memperbarui totalBayar di CartFragment
                    }
                } else {
                    showDeleteConfirmationDialog(holder.itemView.getContext(), position);
                }
            }
        });
    }

    public double getTotalPrice() {
        double totalPrice = 0;
        for (DataCart cart : cartProduct) {
            totalPrice += cart.getTotalHarga();
        }
        return totalPrice;
    }

    public int getTotalQuantity() {
        int totalQuantity = 0;
        for (DataCart cart : cartProduct) {
            totalQuantity += cart.getJumlah_produk();
        }
        return totalQuantity;
    }

    private void showDeleteConfirmationDialog(Context context, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Apakah Anda yakin ingin menghapus produk ini?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem(position);
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return cartProduct.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProduct, tvUkuran, tvKategori, tvJumlahProduk, tvPrice;
        ImageView imgKatalog, delete;
        LinearLayout btn_minplus;
        ImageButton btn_kurang, btn_tambah;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProduct = itemView.findViewById(R.id.tv_merk);
            tvUkuran = itemView.findViewById(R.id.tv_ukuran);
            tvKategori = itemView.findViewById(R.id.tv_kategori);
            tvPrice = itemView.findViewById(R.id.tv_harga);
            tvJumlahProduk = itemView.findViewById(R.id.tv_jumlah);
            btn_kurang = itemView.findViewById(R.id.btn_kurang);
            btn_tambah = itemView.findViewById(R.id.btn_tambah);
            btn_minplus = itemView.findViewById(R.id.minplus);
            imgKatalog = itemView.findViewById(R.id.img_cart);
            delete = itemView.findViewById(R.id.btn_hapus);
        }
    }
}