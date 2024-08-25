package com.example.lestarithriftshop.ui.profile;

public class DataUser {
    private int id;
    private String email;
    private String nama;
    private String password;
    private String nik;
    private String alamat;
    private String kota;
    private String provinsi;
    private String kodepos;
    private String telp;
    private static String photo_profile;

    //setter
    public void setId (int id) { this.id=id; }
    public void setEmail (String email) { this.email=email; }
    public void setNama (String nama) { this.nama=nama; }
    public void setPassword (String password) { this.password=password; }
    public void setNik (String nik) { this.nik=nik; }
    public void setAlamat (String alamat) { this.alamat=alamat; }
    public void setKota (String kota) { this.kota=kota; }
    public void setProvinsi (String provinsi) { this.provinsi=provinsi; }
    public void setKodepos (String kodepos) { this.kodepos=kodepos; }
    public void setTelp(String telp) { this.telp=telp; }
    public void setPhoto_profile(String photo_profile) { this.photo_profile=photo_profile; }

    //getter
    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getNama() { return nama; }
    public String getPassword() { return password; }
    public String getNik() { return nik; }
    public String getAlamat() { return alamat; }
    public String getKota() { return kota; }
    public String getProvinsi() { return provinsi; }
    public String getKodePos() { return kodepos; }
    public String getTelp() { return telp; }
    public static String getPhoto_profile() { return photo_profile; }
}