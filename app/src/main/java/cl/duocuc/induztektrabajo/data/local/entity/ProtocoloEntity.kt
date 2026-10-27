package cl.duocuc.induztektrabajo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protocolos")
data class ProtocoloEntity(
    @PrimaryKey
    val numero: String,
    val tagEquipo: String,
    val clienteNombre: String,
    val estado: String,
    val pruebasAprobadas: Int = 0,
    val pruebasTotales: Int = 0,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fechaRegistro: Long = System.currentTimeMillis()
)