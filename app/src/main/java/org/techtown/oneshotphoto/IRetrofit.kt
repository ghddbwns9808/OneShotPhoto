package org.techtown.oneshotphoto

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface IRetrofit {

    @Multipart
    @POST("training")
    fun train(
        @Part uid: MultipartBody.Part,
        @Part crop: MultipartBody.Part,
        @Part filterImage: MultipartBody.Part
    ):Call<TrainingResult>


    @Multipart
    @POST("inference")
    fun inference(
        @Part uid: MultipartBody.Part,
        @Part crop: MultipartBody.Part,
        @Part originalImage: MultipartBody.Part
    ):Call<InferenceResult>


    @FormUrlEncoded
    @POST("save")
    fun save(
        @Field("uid") uid:String,
        @Field("filterName") filterName: String
    ):Call<TrainingResult>

    @FormUrlEncoded
    @POST("getfilter")
    fun getFilterList(
        @Field("uid") uid: String
    ):Call<filterResult>

    @FormUrlEncoded
    @POST("usemyfilter")
    fun useMyFilter(
        @Field("uid") uid: String,
        @Field("filterName") filterName: String
    ):Call<InferenceResult>

    @FormUrlEncoded
    @POST("usebasicfilter")
    fun useBasicFilter(
        @Field("uid") uid: String,
        @Field("filterName") filterName: String
    ):Call<InferenceResult>

    @FormUrlEncoded
    @POST("getbasicfilter")
    fun getBasicFilterList(
        @Field("uid") uid: String
    ):Call<filterResult>

}