package xcom.niteshray.apps.collegeapp.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import xcom.niteshray.apps.collegeapp.model.ImageUploadResponse

interface imgBBApi {
    @FormUrlEncoded
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Field("image") base64Image: String
    ): Response<ImageUploadResponse>
}