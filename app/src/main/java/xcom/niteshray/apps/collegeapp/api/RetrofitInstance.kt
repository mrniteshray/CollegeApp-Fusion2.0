package xcom.niteshray.apps.collegeapp.api

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitInstance {
    private const val BASE_URL = "https://api.brevo.com/"

    fun brevoapi(context: Context): Brevoapi {
        val api: Brevoapi by lazy {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("api-key","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")
                        .build()
                    chain.proceed(request)
                }
                .build()

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Brevoapi::class.java)
        }
        return api
    }

    fun imgbbApi(): imgBBApi {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(imgBBApi::class.java)
    }

}