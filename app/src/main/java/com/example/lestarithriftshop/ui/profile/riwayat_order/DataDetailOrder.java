package com.example.lestarithriftshop.ui.profile.riwayat_order;

public class DataDetailOrder {
    private String product_merk, FotoUrl, order_status;
    private int quantity;
    private double harga;

    public DataDetailOrder(String product_merk, String FotoUrl, int quantity, double harga) {
        this.product_merk = product_merk;
        this.FotoUrl = FotoUrl;
        this.quantity = quantity;
        this.harga = harga;
    }

    public String getProduct_merk() {
        return product_merk;
    }

    public void setProduct_merk(String product_merk) {
        this.product_merk = product_merk;
    }

    public String getFotoUrl() {
        return FotoUrl;
    }

    public void setFotoUrl(String FotoUrl) {
        this.FotoUrl = FotoUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }
}