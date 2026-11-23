package cl.duocuc.induztektrabajo.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClimaClient {
    private const val BASE_URL = "https://api.open-meteo.com/"

    val servicio: ClimaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClimaApiService::class.java)
    }
}
