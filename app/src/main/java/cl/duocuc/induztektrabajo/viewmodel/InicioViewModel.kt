package cl.duocuc.induztektrabajo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.induztektrabajo.data.local.dao.ProtocoloDao
import cl.duocuc.induztektrabajo.data.local.entity.ProtocoloEntity
import cl.duocuc.induztektrabajo.data.remote.ClimaActual
import cl.duocuc.induztektrabajo.data.remote.InduztekBackendClient
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
import cl.duocuc.induztektrabajo.data.remote.ProtocoloRemotoDto
import android.util.Log
import kotlinx.coroutines.flow.first
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

    fun sincronizarConBackend() {
        viewModelScope.launch {
            _uiState.update { it.copy(mensaje = "Sincronizando con Docker Gateway...") }
            try {
                Log.d("INDUZTEK_DEBUG", "Llamando a Gateway /api/protocolos...")
                val respuesta = InduztekBackendClient.servicio.obtenerProtocolos()

                if (respuesta.isSuccessful) {
                    val listaRemota = respuesta.body().orEmpty()
                    Log.d("INDUZTEK_DEBUG", "Recibidos ${listaRemota.size} protocolos desde backend")

                    val protocolosLocales = protocoloDao.obtenerTodosLosProtocolos().first()
                    val numerosLocales = protocolosLocales.map { it.numero }.toSet()

                    var nuevosInsertados = 0
                    for (remoto in listaRemota) {
                        val num = remoto.numero
                        if (!numerosLocales.contains(num)) {
                            val tag = when {
                                num.contains("0001") -> "TR-01"
                                num.contains("0002") -> "TR-02"
                                num.contains("0003") -> "TR-03"
                                num.contains("0004") -> "SW-01"
                                else -> "GEN-01"
                            }
                            val estadoStr = when (remoto.estado?.uppercase()) {
                                "FORMALIZADO" -> "FORMALIZADO"
                                "EN_PROCESO", "EN_TERRENO" -> "EN_PROCESO"
                                else -> "BORRADOR"
                            }
                            val nuevo = ProtocoloEntity(
                                numero = num,
                                tagEquipo = tag,
                                clienteNombre = "Enel Distribución",
                                estado = estadoStr,
                                pruebasAprobadas = if (estadoStr == "FORMALIZADO") 3 else 1,
                                pruebasTotales = 3,
                                latitud = -33.4489,
                                longitud = -70.6693
                            )
                            protocoloDao.insertarProtocolo(nuevo)
                            nuevosInsertados++
                        }
                    }

                    _uiState.update {
                        it.copy(
                            mensaje = if (nuevosInsertados > 0) {
                                "Sincronizado: $nuevosInsertados nuevos desde Gateway"
                            } else {
                                "Al día: Los ${listaRemota.size} protocolos ya están en el teléfono"
                            }
                        )
                    }
                } else {
                    val errCode = "Error Gateway HTTP ${respuesta.code()}"
                    Log.e("INDUZTEK_DEBUG", errCode)
                    _uiState.update { it.copy(mensaje = errCode) }
                }
            } catch (e: Exception) {
                Log.e("INDUZTEK_DEBUG", "Fallo al sincronizar", e)
                _uiState.update { it.copy(mensaje = "Error: ${e::class.simpleName}: ${e.message}") }
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
            _uiState.update { it.copy(dialogoNuevoVisible = false, mensaje = "Protocolo $numero creado") }

            try {
                val requestRemoto = ProtocoloRemotoDto(
                    codigoProtocolo = numero,
                    clienteId = 3L,
                    tecnicoId = 2L,
                    estado = "BORRADOR",
                    resultado = "PENDIENTE",
                    observaciones = "Creado desde la app móvil para ${equipo.tag}"
                )
                val respuesta = InduztekBackendClient.servicio.crearProtocolo(requestRemoto)
                if (respuesta.isSuccessful) {
                    _uiState.update { it.copy(mensaje = "Protocolo $numero sincronizado con Docker") }
                }
            } catch (e: Exception) {
                android.util.Log.w("INDUZTEK_BACKEND", "No se pudo sincronizar protocolo remoto: ${e.message}")
            }
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
