package cl.duocuc.induztektrabajo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.induztektrabajo.data.local.dao.UsuarioDao
import cl.duocuc.induztektrabajo.data.preferences.SesionDataStore
import cl.duocuc.induztektrabajo.data.security.PasswordHasher
import cl.duocuc.induztektrabajo.model.LoginValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val mostrarContrasena: Boolean = false,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false
)

class LoginViewModel(
    private val usuarioDao: UsuarioDao,
    private val sesionDataStore: SesionDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun actualizarCorreo(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null, errorGeneral = null) }
    }

    fun actualizarContrasena(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, errorGeneral = null) }
    }

    fun alternarContrasenaVisible() {
        _uiState.update { it.copy(mostrarContrasena = !it.mostrarContrasena) }
    }

    fun iniciarSesion(onSuccess: () -> Unit) {
        val estado = _uiState.value
        val correoValido = LoginValidator.correoEsValido(estado.correo)
        val contrasenaValida = LoginValidator.contrasenaEsValida(estado.contrasena)

        if (!correoValido || !contrasenaValida) {
            _uiState.update {
                it.copy(
                    errorCorreo = if (correoValido) null else "Ingresa un correo válido",
                    errorContrasena = if (contrasenaValida) null else "Usa al menos 6 caracteres"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null) }
            try {
                val usuario = usuarioDao.buscarPorCorreo(estado.correo.trim())
                if (usuario != null && PasswordHasher.verificarPassword(estado.contrasena, usuario.passwordHash)) {
                    sesionDataStore.guardarSesion(
                        id = usuario.id,
                        correo = usuario.correo,
                        nombre = usuario.nombreCompleto,
                        rol = usuario.rol
                    )
                    _uiState.update { it.copy(cargando = false) }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            errorGeneral = "Correo o contraseña incorrectos"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorGeneral = "Error al iniciar sesión: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
    suspend fun restablecerContrasena(
        correo: String,
        nuevaClave: String
    ): Result<Unit> {
        return try {
            val usuario = usuarioDao.buscarPorCorreo(correo.trim())
            if (usuario == null) {
                Result.failure(Exception("No existe ninguna cuenta asociada a este correo."))
            } else {
                val nuevoHash = cl.duocuc.induztektrabajo.data.security.PasswordHasher.hashPassword(nuevaClave)
                usuarioDao.actualizarPasswordPorCorreo(correo.trim(), nuevoHash)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar la contraseña: ${e.message}"))
        }
    }

}