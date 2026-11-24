package cl.duocuc.induztektrabajo.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface InduztekBackendApiService {

    @GET("api/protocolos")
    suspend fun obtenerProtocolos(): Response<List<ProtocoloRemotoDto>>

    @GET("api/mediciones/protocolo/{numero}")
    suspend fun obtenerMedicionesPorProtocolo(
        @Path("numero") numero: String
    ): Response<List<MedicionRemotaDto>>

    @GET("api/usuarios")
    suspend fun obtenerUsuarios(): Response<List<UsuarioRemotoDto>>

    @POST("api/usuarios")
    suspend fun crearUsuario(
        @Body request: CrearUsuarioRemotoRequest
    ): Response<UsuarioRemotoDto>

    @POST("api/protocolos")
    suspend fun crearProtocolo(
        @Body protocolo: ProtocoloRemotoDto
    ): Response<ProtocoloRemotoDto>

    @POST("api/mediciones")
    suspend fun crearMedicion(
        @Body medicion: MedicionRemotaDto
    ): Response<MedicionRemotaDto>

}

object InduztekBackendClient {

    private const val PREFS_NAME = "induztek_backend_config"
    private const val KEY_IP = "backend_ip_host"
    private const val IP_DEFECTO = "localhost"

    private var ipActual: String = IP_DEFECTO
    private var servicioActual: InduztekBackendApiService? = null

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun obtenerIp(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        ipActual = prefs.getString(KEY_IP, IP_DEFECTO) ?: IP_DEFECTO
        return ipActual
    }

    /**
     * Guarda la nueva IP ingresada y reconstruye el cliente Retrofit de inmediato.
     */
    fun actualizarIp(context: Context, nuevaIp: String) {
        val ipLimpia = nuevaIp.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .substringBefore(":")
            .removeSuffix("/")

        ipActual = if (ipLimpia.isBlank()) IP_DEFECTO else ipLimpia

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_IP, ipActual).apply()

        reconstruirServicio()
    }

    private fun reconstruirServicio() {
        val baseUrl = "http://$ipActual:8081/"
        servicioActual = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InduztekBackendApiService::class.java)
    }

    val servicio: InduztekBackendApiService
        get() {
            if (servicioActual == null) {
                reconstruirServicio()
            }
            return servicioActual!!
        }
}
