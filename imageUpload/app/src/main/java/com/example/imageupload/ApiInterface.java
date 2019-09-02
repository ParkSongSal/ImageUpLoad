package com.example.imageupload;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiInterface {


    @Multipart
    @POST("upload2.php")
    Call<ResponseBody>uploadImage(@Part("title") RequestBody title,
                                  @Part MultipartBody.Part image);
    /*Call<ImageClass> uploadImage(@Field("title") String title,
                                  @Part MultipartBody.Part image);
                                  */
}
