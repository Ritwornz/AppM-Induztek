package cl.duocuc.induztektrabajo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duocuc.induztektrabajo.data.local.entity.ProtocoloEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtocoloDao {
    @Query("SELECT * FROM protocolos ORDER BY fechaRegistro DESC")
    fun obtenerTodosLosProtocolos(): Flow<List<ProtocoloEntity>>

    @Query("SELECT * FROM protocolos WHERE clienteNombre LIKE '%' || :cliente || '%' ORDER BY fechaRegistro DESC")
    fun obtenerProtocolosPorCliente(cliente: String): Flow<List<ProtocoloEntity>>

    @Query("SELECT * FROM protocolos WHERE numero = :numero LIMIT 1")
    suspend fun obtenerProtocoloPorNumero(numero: String): ProtocoloEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProtocolo(protocolo: ProtocoloEntity)

    @Update
    suspend fun actualizarProtocolo(protocolo: ProtocoloEntity)

    @Query("SELECT COUNT(*) FROM protocolos WHERE estado = :estado")
    suspend fun contarPorEstado(estado: String): Int
}