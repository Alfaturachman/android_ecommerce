package com.example.lestarithriftshop;

public class ServerAPI {
    private static final String IP_ADDRESS = "192.168.1.39";
    private static final String BASE_PATH = "http://" + IP_ADDRESS + "/android_ecommerce/";

    public static final String BASE_URL = BASE_PATH;
    public static final String URL_FOTO = BASE_PATH + "produk/";
    public static final String BASE_URL_PROFILE = BASE_PATH + "foto_profile/";
    public static final String BASE_URL_PEMBAYARAN = BASE_PATH + "bukti_pembayaran/";
}
