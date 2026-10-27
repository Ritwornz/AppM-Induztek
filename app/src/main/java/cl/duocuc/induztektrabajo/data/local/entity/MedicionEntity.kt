package cl.duocuc.induztektrabajo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mediciones")
data class MedicionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val protocoloNumero: String,
    val tipoPrueba: String,
    val valorA: Double,
    val valorB: Double,
    val resultado: String,
    val motivo: String = ""
)