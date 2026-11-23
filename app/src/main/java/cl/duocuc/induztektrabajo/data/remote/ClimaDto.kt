package cl.duocuc.induztektrabajo.data.remote

import com.google.gson.annotations.SerializedName

data class ClimaRespuesta(
    @SerializedName("current")
    val actual: ClimaActual? = null
)

data class ClimaActual(
    @SerializedName("temperature_2m")
    val temperatura: Double = 0.0,
    @SerializedName("relative_humidity_2m")
    val humedad: Int = 0,
    @SerializedName("weather_code")
    val codigoClima: Int = 0,
    @SerializedName("wind_speed_10m")
    val velocidadViento: Double = 0.0
)
