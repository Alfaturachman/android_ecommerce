package com.example.lestarithriftshop;

import com.google.gson.annotations.SerializedName;

public class ResponseUpload {
    @SerializedName("kode")
    private String kode;

    @SerializedName("pesan")
    private String pesan;

    @SerializedName("newImageUrl")
    private String newImageUrl;

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public String getPesan() {
        return pesan;
    }

    public void setPesan(String pesan) {
        this.pesan = pesan;
    }

    public String getNewImageUrl() {
        return newImageUrl;
    }

    public void setNewImageUrl(String newImageUrl) {
        this.newImageUrl = newImageUrl;
    }
}
