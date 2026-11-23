package cl.duocuc.induztektrabajo.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ClimaApiService {
    @GET("v1/forecast")
    suspend fun obtenerClimaFaena(
        @Query("latitude") latitud: Double = -33.4569,
        @Query("longitude") longitud: Double = -70.6483,
        @Query("current") campos: String = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m"
    ): Response<ClimaRespuesta>
}
