package cl.duocuc.induztektrabajo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.induztektrabajo.data.local.dao.UsuarioDao
import cl.duocuc.induztektrabajo.data.local.entity.UsuarioEntity
import cl.duocuc.induztektrabajo.data.security.PasswordHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroUsuarioUiState(
    val nombre: String = "",
    val empresa: String = "",
    val correo: String = "",
    val rol: String = "TECNICO", // "TECNICO" o "CLIENTE"
    val password: String = "",
    val confirmarPassword: String = "",
    val passwordVisible: Boolean = false,
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val registroExitoso: Boolean = false
)

class RegistroUsuarioViewModel(
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUsuarioUiState())
    val uiState: StateFlow<RegistroUsuarioUiState> = _uiState.asStateFlow()

    fun onNombreChange(nuevo: String) = _uiState.update { it.copy(nombre = nuevo, mensajeError = null) }
    fun onEmpresaChange(nuevo: String) = _uiState.update { it.copy(empresa = nuevo, mensajeError = null) }
    fun onCorreoChange(nuevo: String) = _uiState.update { it.copy(correo = nuevo.trim(), mensajeError = null) }
    fun onRolChange(nuevo: String) = _uiState.update { it.copy(rol = nuevo) }
    fun onPasswordChange(nuevo: String) = _uiState.update { it.copy(password = nuevo, mensajeError = null) }
    fun onConfirmarPasswordChange(nuevo: String) = _uiState.update { it.copy(confirmarPassword = nuevo, mensajeError = null) }
    fun togglePasswordVisible() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun registrarUsuario(onExito: () -> Unit) {
        val s = _uiState.value

        // 1. Validaciones
        if (s.nombre.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Ingresa tu nombre completo") }
            return
        }
        if (s.empresa.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Ingresa tu empresa o faena") }
            return
        }
        if (!s.correo.contains("@") || !s.correo.contains(".")) {
            _uiState.update { it.copy(mensajeError = "Ingresa un correo corporativo válido") }
            return
        }
        if (s.password.length < 6) {
            _uiState.update { it.copy(mensajeError = "La contraseña debe tener al menos 6 caracteres") }
            return
        }
        if (s.password != s.confirmarPassword) {
            _uiState.update { it.copy(mensajeError = "Las contraseñas no coinciden") }
            return
        }

        // 2. Proceso en SQLite
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, mensajeError = null) }
            try {
                val existe = usuarioDao.buscarPorCorreo(s.correo)
                if (existe != null) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            mensajeError = "El correo ya está registrado en el sistema"
                        )
                    }
                    return@launch
                }

                // Convención de nombre: "Nombre (Empresa)" para el filtro dinámico
                val nombreConEmpresa = "${s.nombre.trim()} (${s.empresa.trim()})"
                val hash = PasswordHasher.hashPassword(s.password)

                val nuevoUsuario = UsuarioEntity(
                    correo = s.correo,
                    passwordHash = hash,
                    nombreCompleto = nombreConEmpresa,
                    rol = s.rol
                )

                usuarioDao.insertarUsuario(nuevoUsuario)

                _uiState.update { it.copy(cargando = false, registroExitoso = true) }
                onExito()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeError = "Error al registrar: ${e.localizedMessage ?: "Intente nuevamente"}"
                    )
                }
            }
        }
    }
}
