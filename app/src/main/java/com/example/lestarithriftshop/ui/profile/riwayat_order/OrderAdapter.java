package com.example.lestarithriftshop.ui.profile.riwayat_order;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lestarithriftshop.R;
import com.example.lestarithriftshop.ServerAPI;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private Context context;
    private List<DataOrder> orderList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(DataOrder order);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public OrderAdapter(Context context, List<DataOrder> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataOrder order = orderList.get(position);

        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        String formattedPrice = formatRupiah.format(order.getTotal_harga()).replace(",00", "");

        // Format the date
        String originalDate = order.getTanggal();
        String formattedDate = formatDate(originalDate);

        holder.tv_invoice.setText("#" + order.getInvoice());
        holder.tv_merk.setText(order.getMerk());
        holder.tv_tanggal.setText(formattedDate);
        holder.tv_total_harga.setText(formattedPrice);
        holder.total_quantity.setText(order.getTotal_quantity() + " produk");
        holder.tv_status.setText(order.getStatus());

        // Set latitude and longitude
        holder.latitude.setText("Latitude: " + order.getLatitude());
        holder.longitude.setText("Longitude: " + order.getLongitude());

        String status = order.getStatus();

        // Set background tint based on status
        switch (status) {
            case "Belum Bayar":
                holder.tv_status.setBackgroundTintList(ContextCompat.getColorStateList(holder.tv_status.getContext(), android.R.color.darker_gray));
                break;
            case "Diproses":
                holder.tv_status.setBackgroundTintList(ContextCompat.getColorStateList(holder.tv_status.getContext(), android.R.color.holo_orange_dark));
                break;
            case "Dikirim":
                holder.tv_status.setBackgroundTintList(ContextCompat.getColorStateList(holder.tv_status.getContext(), android.R.color.holo_blue_dark));
                break;
            case "Selesai":
                holder.tv_status.setBackgroundTintList(ContextCompat.getColorStateList(holder.tv_status.getContext(), android.R.color.holo_green_dark));
                break;
            case "Dibatalkan":
                holder.tv_status.setBackgroundTintList(ContextCompat.getColorStateList(holder.tv_status.getContext(), android.R.color.holo_red_dark));
                break;
            default:
                // Optionally handle other statuses or set a default color
                holder.tv_status.setBackgroundTintList(ContextCompat.getColorStateList(holder.tv_status.getContext(), android.R.color.darker_gray));
                break;
        }

        ServerAPI urlAPI = new ServerAPI();
        String URL = urlAPI.URL_FOTO;
        String fullImageUrl = URL + "/" + order.getFoto();
        Glide.with(context)
                .load(fullImageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.image_order);

        holder.cardView_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(order);
                }
            }
        });
    }

    private String formatDate(String date) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat targetFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("in", "ID"));
            return targetFormat.format(originalFormat.parse(date));
        } catch (Exception e) {
            e.printStackTrace();
            return date; // Return original date in case of an error
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_invoice, tv_tanggal, tv_total_harga, total_quantity, tv_status, tv_merk;
        TextView latitude, longitude;
        CardView cardView_order;
        ImageView image_order;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_invoice = itemView.findViewById(R.id.invoice);
            tv_tanggal = itemView.findViewById(R.id.tanggal);
            tv_total_harga = itemView.findViewById(R.id.total_harga);
            total_quantity = itemView.findViewById(R.id.total_quantity);
            tv_status = itemView.findViewById(R.id.status);
            tv_merk = itemView.findViewById(R.id.tv_merk);
            image_order = itemView.findViewById(R.id.image_order);
            cardView_order = itemView.findViewById(R.id.cardView_order);
            latitude = itemView.findViewById(R.id.tv_latitude);
            longitude = itemView.findViewById(R.id.tv_longitude);
        }
    }
}
