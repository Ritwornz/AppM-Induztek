package cl.duocuc.induztektrabajo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duocuc.induztektrabajo.data.local.entity.MedicionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicionDao {
    @Query("SELECT * FROM mediciones WHERE protocoloNumero = :protocoloNumero")
    fun obtenerMedicionesPorProtocolo(protocoloNumero: String): Flow<List<MedicionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMedicion(medicion: MedicionEntity): Long

    @Update
    suspend fun actualizarMedicion(medicion: MedicionEntity)

    @Query("DELETE FROM mediciones WHERE id = :id")
    suspend fun eliminarMedicion(id: Int)
}