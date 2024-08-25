package com.example.lestarithriftshop.ui.cart;

public class DataCart {
    String merk, ukuran, kategori, fotoUrl;
    int id_product, jumlah_produk, stok;
    double harga;

    public DataCart(int id_product, String merk, double harga, int jumlah_produk, int stok, String ukuran, String kategori, String fotoUrl) {
        this.id_product = id_product;
        this.merk = merk;
        this.harga = harga;
        this.jumlah_produk = jumlah_produk;
        this.stok = stok;
        this.ukuran = ukuran;
        this.kategori = kategori;
        this.fotoUrl = fotoUrl;
    }

    public int getId_product() {
        return id_product;
    }

    public void setId_product(int id_product) {
        this.id_product = id_product;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getMerk() {
        return merk;
    }

    public void setMerk(String merk) {
        this.merk = merk;
    }

    public int getJumlah_produk() {
        return jumlah_produk;
    }

    public void setJumlah_produk(int jumlah) {
        this.jumlah_produk = jumlah;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public String getUkuran() {
        return ukuran;
    }

    public void setUkuran(String ukuran) {
        this.ukuran = ukuran;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public double getTotalHarga() {
        return this.harga * this.jumlah_produk;
    }
}