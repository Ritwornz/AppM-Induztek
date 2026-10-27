package cl.duocuc.induztektrabajo.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sesion_usuario")

data class DatosSesion(
    val id: Int,
    val correo: String,
    val nombre: String,
    val rol: String
)

class SesionDataStore(private val context: Context) {

    companion object {
        private val USER_ID = intPreferencesKey("user_id")
        private val USER_CORREO = stringPreferencesKey("user_correo")
        private val USER_NOMBRE = stringPreferencesKey("user_nombre")
        private val USER_ROL = stringPreferencesKey("user_rol")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val estaLogueado: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val obtenerUsuario: Flow<DatosSesion?> = context.dataStore.data.map { preferences ->
        val id = preferences[USER_ID]
        val correo = preferences[USER_CORREO]
        val nombre = preferences[USER_NOMBRE]
        val rol = preferences[USER_ROL]

        if (id != null && correo != null && nombre != null && rol != null) {
            DatosSesion(id = id, correo = correo, nombre = nombre, rol = rol)
        } else {
            null
        }
    }

    suspend fun guardarSesion(id: Int, correo: String, nombre: String, rol: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = id
            preferences[USER_CORREO] = correo
            preferences[USER_NOMBRE] = nombre
            preferences[USER_ROL] = rol
            preferences[IS_LOGGED_IN] = true
        }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}