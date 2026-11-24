package cl.duocuc.induztektrabajo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.induztektrabajo.data.local.dao.UsuarioDao
import cl.duocuc.induztektrabajo.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminUsuariosUiState(
    val usuarios: List<UsuarioEntity> = emptyList(),
    val busqueda: String = "",
    val mensajeFeedback: String? = null,
    val esErrorFeedback: Boolean = false
)

class AdminUsuariosViewModel(
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    private val _mensajeFeedback = MutableStateFlow<String?>(null)
    private val _esErrorFeedback = MutableStateFlow(false)

    val uiState: StateFlow<AdminUsuariosUiState> = combine(
        usuarioDao.observarTodosLosUsuarios(),
        _busqueda,
        _mensajeFeedback,
        _esErrorFeedback
    ) { listaUsuarios, query, mensaje, esError ->
        val filtrados = if (query.isBlank()) {
            listaUsuarios
        } else {
            listaUsuarios.filter { usuario ->
                usuario.nombreCompleto.contains(query, ignoreCase = true) ||
                        usuario.correo.contains(query, ignoreCase = true) ||
                        usuario.rol.contains(query, ignoreCase = true)
            }
        }
        AdminUsuariosUiState(
            usuarios = filtrados,
            busqueda = query,
            mensajeFeedback = mensaje,
            esErrorFeedback = esError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdminUsuariosUiState()
    )

    fun onBusquedaCambia(nuevoTexto: String) {
        _busqueda.value = nuevoTexto
    }

    fun limpiarMensaje() {
        _mensajeFeedback.value = null
    }

    /**
     * Cambia el rol de un usuario en Room (ADMINISTRADOR, TECNICO, CLIENTE)
     */
    fun cambiarRol(usuario: UsuarioEntity, nuevoRol: String, idUsuarioLogueado: Int) {
        if (usuario.id == idUsuarioLogueado && nuevoRol != "ADMINISTRADOR") {
            _mensajeFeedback.value = "No puedes quitarte el rol de Administrador a ti mismo."
            _esErrorFeedback.value = true
            return
        }

        viewModelScope.launch {
            try {
                usuarioDao.actualizarRol(usuario.id, nuevoRol)
                _mensajeFeedback.value = "Rol de ${usuario.nombreCompleto} actualizado a $nuevoRol."
                _esErrorFeedback.value = false
            } catch (e: Exception) {
                _mensajeFeedback.value = "Error al actualizar rol: ${e.message}"
                _esErrorFeedback.value = true
            }
        }
    }

    /**
     * Elimina un usuario de Room con validación de seguridad
     */
    fun eliminarUsuario(usuario: UsuarioEntity, idUsuarioLogueado: Int) {
        if (usuario.id == idUsuarioLogueado) {
            _mensajeFeedback.value = "No puedes eliminar tu propia cuenta en sesión."
            _esErrorFeedback.value = true
            return
        }

        viewModelScope.launch {
            try {
                usuarioDao.eliminarUsuarioPorId(usuario.id)
                _mensajeFeedback.value = "Usuario ${usuario.nombreCompleto} eliminado exitosamente."
                _esErrorFeedback.value = false
            } catch (e: Exception) {
                _mensajeFeedback.value = "Error al eliminar usuario: ${e.message}"
                _esErrorFeedback.value = true
            }
        }
    }
}
