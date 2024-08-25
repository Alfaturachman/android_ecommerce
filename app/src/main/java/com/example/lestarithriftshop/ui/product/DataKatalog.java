package com.example.lestarithriftshop.ui.product;

public class DataKatalog {
    String merk, kategori, satuan, foto, deskripsi;
    Double hargajual;
    Integer id, stok, pengunjung;

    // Getter methods
    public int getId() {
        return id;
    }

    public String getMerk() {
        return merk;
    }

    public String getKategori() {
        return kategori;
    }

    public String getSatuan() {
        return satuan;
    }


    public double getHargajual() {
        return hargajual;
    }

    public Integer getStok() {
        return stok;
    }

    public Integer getPengunjung() {
        return pengunjung;
    }

    public String getFoto() {
        return foto;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    // Setter methods
    public void setId(int id) {
        this.id = id;
    }

    public void setMerk(String merk) {
        this.merk = merk;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public void setSatuan(String satuan) {
        this.satuan = satuan;
    }

    public void setHargajual(double hargajual) {
        this.hargajual = hargajual;
    }

    public void setStok(Integer stok) {
        this.stok = stok;
    }

    public void setPengunjung(Integer pengunjung) {
        this.pengunjung = pengunjung;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }
}