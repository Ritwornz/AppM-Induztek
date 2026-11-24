package cl.duocuc.induztektrabajo.data.remote

import com.google.gson.annotations.SerializedName

data class UsuarioRemotoDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("rol") val rol: String? = null,
    @SerializedName("activo") val activo: Boolean = true,
    @SerializedName("empresa") val empresa: String? = null
)

data class CrearUsuarioRemotoRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("password") val password: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("empresa") val empresa: String? = "Induztek SpA"
)

data class ProtocoloRemotoDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("codigoProtocolo") val codigoProtocolo: String? = null,
    @SerializedName("clienteId") val clienteId: Long? = null,
    @SerializedName("tecnicoId") val tecnicoId: Long? = null,
    @SerializedName("estado") val estado: String? = null,
    @SerializedName("resultado") val resultado: String? = null,
    @SerializedName("observaciones") val observaciones: String? = null
) {
    val numero: String get() = codigoProtocolo ?: "PT-2026-0001"
}

data class MedicionRemotaDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("protocoloId") val protocoloId: Long? = null,
    @SerializedName("tipoPrueba") val tipoPrueba: String? = null,
    @SerializedName("parametro") val parametro: String? = null,
    @SerializedName("valorMedido") val valorMedido: Double? = 0.0,
    @SerializedName("unidadMedida") val unidadMedida: String? = null,
    @SerializedName("cumpleNorma") val cumpleNorma: Boolean = true
)
