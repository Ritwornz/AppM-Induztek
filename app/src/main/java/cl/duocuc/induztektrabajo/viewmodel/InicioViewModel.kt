package cl.duocuc.induztektrabajo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.induztektrabajo.data.local.dao.ProtocoloDao
import cl.duocuc.induztektrabajo.data.local.entity.ProtocoloEntity
import cl.duocuc.induztektrabajo.data.remote.ClimaActual
import cl.duocuc.induztektrabajo.data.remote.RetrofitClimaClient
import cl.duocuc.induztektrabajo.model.Equipo
import cl.duocuc.induztektrabajo.model.EstadoProtocolo
import cl.duocuc.induztektrabajo.model.ProtocoloResumen
import cl.duocuc.induztektrabajo.model.RepositorioFicticio
import cl.duocuc.induztektrabajo.model.filtrarProtocolos
import cl.duocuc.induztektrabajo.model.siguienteNumeroProtocolo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface ClimaUiState {
    object Cargando : ClimaUiState
    data class Exito(val clima: ClimaActual) : ClimaUiState
    object Error : ClimaUiState
}

data class InicioUiState(
    val busqueda: String = "",
    val todos: List<ProtocoloResumen> = emptyList(),
    val equipos: List<Equipo> = emptyList(),
    val dialogoNuevoVisible: Boolean = false,
    val mensaje: String? = null,
    val clima: ClimaUiState = ClimaUiState.Cargando
) {
    val visibles: List<ProtocoloResumen> get() = filtrarProtocolos(todos, busqueda)
    val borradores: Int get() = todos.count { it.estado == EstadoProtocolo.BORRADOR }
    val formalizados: Int get() = todos.count { it.estado == EstadoProtocolo.FORMALIZADO }
}

class InicioViewModel(
    private val protocoloDao: ProtocoloDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        InicioUiState(
            equipos = RepositorioFicticio.equipos
        )
    )
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    private var jobObservador: Job? = null

    init {
        // Carga inicial por defecto
        cargarProtocolos(rol = null, nombreCompleto = null)
        cargarClimaFaena()
    }

    fun cargarClimaFaena() {
        viewModelScope.launch {
            _uiState.update { it.copy(clima = ClimaUiState.Cargando) }
            try {
                val respuesta = RetrofitClimaClient.servicio.obtenerClimaFaena()
                if (respuesta.isSuccessful && respuesta.body()?.actual != null) {
                    _uiState.update { it.copy(clima = ClimaUiState.Exito(respuesta.body()!!.actual!!)) }
                } else {
                    _uiState.update { it.copy(clima = ClimaUiState.Error) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(clima = ClimaUiState.Error) }
            }
        }
    }

    fun cargarProtocolos(rol: String?, nombreCompleto: String?) {
        jobObservador?.cancel()
        jobObservador = viewModelScope.launch {
            val flujo = if (rol == "CLIENTE" && !nombreCompleto.isNullOrBlank()) {
                val empresa = if (nombreCompleto.contains("(") && nombreCompleto.contains(")")) {
                    nombreCompleto.substringAfter("(").substringBefore(")").trim()
                } else {
                    nombreCompleto.trim()
                }
                protocoloDao.obtenerProtocolosPorCliente(empresa)
            } else {
                protocoloDao.obtenerTodosLosProtocolos()
            }

            flujo.collectLatest { entidades ->
                val listaResumenes = entidades.map { entidad ->
                    val fechaStr = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")).format(Date(entidad.fechaRegistro))
                    val equipoAsociado = RepositorioFicticio.equipos.find { it.tag == entidad.tagEquipo }
                        ?: RepositorioFicticio.equipos.first()

                    val estadoEnum = try {
                        EstadoProtocolo.valueOf(entidad.estado)
                    } catch (e: Exception) {
                        EstadoProtocolo.BORRADOR
                    }

                    ProtocoloResumen(
                        numero = entidad.numero,
                        equipo = equipoAsociado,
                        fecha = fechaStr,
                        estado = estadoEnum,
                        pruebasAprobadas = entidad.pruebasAprobadas,
                        pruebasTotales = entidad.pruebasTotales
                    )
                }
                _uiState.update { it.copy(todos = listaResumenes) }
            }
        }
    }

    fun actualizarBusqueda(texto: String) = _uiState.update { it.copy(busqueda = texto) }

    fun abrirDialogoNuevo() = _uiState.update { it.copy(dialogoNuevoVisible = true) }

    fun cerrarDialogoNuevo() = _uiState.update { it.copy(dialogoNuevoVisible = false) }

    fun limpiarMensaje() = _uiState.update { it.copy(mensaje = null) }

    fun crearProtocolo(equipo: Equipo) {
        viewModelScope.launch {
            val anio = Calendar.getInstance().get(Calendar.YEAR)
            val numero = siguienteNumeroProtocolo(_uiState.value.todos, anio)
            val nuevaEntidad = ProtocoloEntity(
                numero = numero,
                tagEquipo = equipo.tag,
                clienteNombre = equipo.cliente.nombre,
                estado = EstadoProtocolo.BORRADOR.name,
                pruebasAprobadas = 0,
                pruebasTotales = 3,
                fechaRegistro = System.currentTimeMillis()
            )
            protocoloDao.insertarProtocolo(nuevaEntidad)
            _uiState.update { it.copy(dialogoNuevoVisible = false, mensaje = "Protocolo $numero creado para ${equipo.tag}") }
        }
    }

    fun actualizarAvance(numero: String, aprobadas: Int, total: Int, nuevoEstado: EstadoProtocolo) {
        viewModelScope.launch {
            val protocoloActual = protocoloDao.obtenerProtocoloPorNumero(numero)
            if (protocoloActual != null) {
                val protocoloActualizado = protocoloActual.copy(
                    pruebasAprobadas = aprobadas,
                    pruebasTotales = total,
                    estado = nuevoEstado.name
                )
                protocoloDao.actualizarProtocolo(protocoloActualizado)
                _uiState.update { it.copy(mensaje = "Registro de $numero guardado") }
            }
        }
    }
}
