package cl.duocuc.induztektrabajo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duocuc.induztektrabajo.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observarUsuarioPorId(id: Int): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios LIMIT 2")
    suspend fun obtenerUsuarios(): List<UsuarioEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity)

    @Query("UPDATE usuarios SET fotoBlob = :foto WHERE id = :id")
    suspend fun actualizarFoto(id: Int, foto: ByteArray?)

    @Query("UPDATE usuarios SET nombreCompleto = :nuevoNombre WHERE correo = :correo")
    suspend fun actualizarNombrePorCorreo(correo: String, nuevoNombre: String)

    @Query("UPDATE usuarios SET passwordHash = :nuevoHash WHERE correo = :correo")
    suspend fun actualizarPasswordPorCorreo(correo: String, nuevoHash: String)

}