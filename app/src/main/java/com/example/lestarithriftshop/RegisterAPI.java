package com.example.lestarithriftshop;

import com.example.lestarithriftshop.ui.product.Value;
import com.example.lestarithriftshop.ui.profile.riwayat_order.ValueOrder;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RegisterAPI {

    @FormUrlEncoded
    @POST("post_register.php")
    Call<ResponseBody> register(
            @Field("email") String email,
            @Field("nama") String nama,
            @Field("alamat") String alamat,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("get_login.php")
    Call<ResponseBody> login (
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("get_profile.php")
    Call<ResponseBody> getProfile(
            @Field("email") String email
    );

    @Multipart
    @POST("upload_image.php")
    Call<ResponseUpload> uploadImage(
            @Part("customer_id") int customer_id,
            @Part MultipartBody.Part image
    );

    @FormUrlEncoded
    @POST("update_profile.php")
    Call<ResponseBody> updateProfile (
            @Field("nama") String nama,
            @Field("alamat") String alamat,
            @Field("nik") String nik,
            @Field("kota") String kota,
            @Field("provinsi") String provinsi,
            @Field("telp") String telp,
            @Field("kodepos") String kodepos,
            @Field("email") String email
    );

    @FormUrlEncoded
    @POST("update_password.php")
    Call<ResponseBody> updatePassword (
            @Field("password") String password,
            @Field("email") String email
    );

    @GET("get_katalog.php")
    Call<Value> view();

    @GET("get_katalog_terbaru.php")
    Call<Value> get_katalog_terbaru();

    @GET("get_katalog_rekomendasi.php")
    Call<Value> get_katalog_rekomendasi();

    @FormUrlEncoded
    @POST("get_kode_katalog.php")
    Call<ResponseBody> getKodeKatalog(
            @Field("idProduk") int idProduk
    );

    @FormUrlEncoded
    @POST("update_pengunjung.php")
    Call<ResponseBody> updatePengunjung(@Field("productID") int productID);

    @GET("get_order.php")
    Call<ValueOrder> getOrder(
            @Query("customer_id") int customerId
    );

    @FormUrlEncoded
    @POST("get_detail_order.php")
    Call<ResponseBody> getDetailOrder(
            @Field("invoice") String invoice
    );

    @FormUrlEncoded
    @POST("get_order_confirm.php")
    Call<ResponseBody> getOrderConfirm(
            @Field("id_order") int order_id
    );

    @FormUrlEncoded
    @POST("post_order_confirm.php")
    Call<ResponseBody> postOrderConfirm(
            @Field("id_order") int order_id
    );

    @FormUrlEncoded
    @POST("cost")
    @Headers({
            "content-type: application/x-www-form-urlencoded",
            "key: 9726ddc76f155c51de2fcf51ba84bf90"
    })
    Call<ResponseBody> cekOngkir(@Field("origin") String origin,
                                 @Field("destination") String destination,
                                 @Field("weight") int weight,
                                 @Field("courier") String courier);
    @GET("province")
    @Headers("key: 9726ddc76f155c51de2fcf51ba84bf90")
    Call<ResponseBody> getProvince();

    @GET("city")
    @Headers("key: 9726ddc76f155c51de2fcf51ba84bf90")
    Call<ResponseBody> getCity(@Query("province") int provinceId);

    @FormUrlEncoded
    @POST("update_order_status.php")
    Call<ResponseBody> updateOrderStatus(
            @Field("invoice") String invoice,
            @Field("status") String status
    );

    @POST("post_checkout.php")
    Call<ResponseBody> checkoutJson(@Body RequestBody body);

    @Multipart
    @POST("upload_bukti.php")
    Call<ResponseUpload> uploadBukti(
            @Part("order_id") int order_id,
            @Part MultipartBody.Part image
    );

    @FormUrlEncoded
    @POST("update_product_stock.php")
    Call<ResponseBody> updateStock(
            @Field("id_order") int order_id
    );
}
