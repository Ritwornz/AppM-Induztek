package cl.duocuc.induztektrabajo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.induztektrabajo.data.local.dao.MedicionDao
import cl.duocuc.induztektrabajo.data.local.dao.ProtocoloDao
import cl.duocuc.induztektrabajo.model.EstadoProtocolo
import cl.duocuc.induztektrabajo.model.ProtocoloResumen
import cl.duocuc.induztektrabajo.model.RecepcionActa
import cl.duocuc.induztektrabajo.model.RegistroPrueba
import cl.duocuc.induztektrabajo.model.ResultadoPrueba
import cl.duocuc.induztektrabajo.model.TipoPrueba
import cl.duocuc.induztektrabajo.model.resultadoGlobal
import cl.duocuc.induztektrabajo.model.validarRecepcion
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.Bitmap
import cl.duocuc.induztektrabajo.data.local.entity.ProtocoloEntity

data class ResumenUiState(
    val protocolo: ProtocoloResumen? = null,
    val pruebas: List<RegistroPrueba> = emptyList(),
    val tecnico: String = "Carlos Sánchez",
    val recepcion: RecepcionActa? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val mensajeUbicacion: String? = null,
    val nombreReceptor: String = "",
    val rutReceptor: String = "",
    val cargo: String = "",
    val fechaRecepcion: String = fechaHoy(),
    val errorRecepcion: String? = null,
    val fotoEvidencia: Bitmap? = null
) {
    val global: ResultadoPrueba? get() = resultadoGlobal(pruebas)
}

private fun fechaHoy(): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")).format(Date())

class ResumenViewModel(
    private val medicionDao: MedicionDao,
    private val protocoloDao: ProtocoloDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResumenUiState())
    val uiState: StateFlow<ResumenUiState> = _uiState.asStateFlow()

    private val recepciones = mutableMapOf<String, RecepcionActa>()
    private var jobObservadorMediciones: Job? = null

    fun abrir(protocolo: ProtocoloResumen) {
        val recepcion = recepciones[protocolo.numero]
        val actual = _uiState.value

        // PRESERVA la foto y los campos del formulario si la app se suspende por la cámara:
        _uiState.value = ResumenUiState(
            protocolo = protocolo,
            pruebas = emptyList(),
            recepcion = recepcion,
            fechaRecepcion = actual.fechaRecepcion.ifBlank { fechaHoy() },
            nombreReceptor = actual.nombreReceptor,
            rutReceptor = actual.rutReceptor,
            cargo = actual.cargo,
            fotoEvidencia = actual.fotoEvidencia // 👈 No borra la foto capturada
        )

        // 1. Cargar coordenadas GPS persistidas en Room para este protocolo
        viewModelScope.launch {
            val entidad = protocoloDao.obtenerProtocoloPorNumero(protocolo.numero)
            if (entidad != null && entidad.latitud != null && entidad.longitud != null) {
                _uiState.update {
                    it.copy(
                        latitud = entidad.latitud,
                        longitud = entidad.longitud,
                        mensajeUbicacion = "Ubicación recuperada de la base de datos"
                    )
                }
            }
        }

        // 2. Observar mediciones reactivamente desde Room
        jobObservadorMediciones?.cancel()
        jobObservadorMediciones = viewModelScope.launch {
            medicionDao.obtenerMedicionesPorProtocolo(protocolo.numero).collectLatest { entidades ->
                val listaPruebas = entidades.map { entidad ->
                    val tipoEnum = try {
                        TipoPrueba.valueOf(entidad.tipoPrueba)
                    } catch (e: Exception) {
                        TipoPrueba.AISLAMIENTO
                    }
                    val resultadoEnum = try {
                        ResultadoPrueba.valueOf(entidad.resultado)
                    } catch (e: Exception) {
                        ResultadoPrueba.APROBADA
                    }
                    RegistroPrueba(
                        tipo = tipoEnum,
                        valorA = entidad.valorA,
                        valorB = entidad.valorB,
                        resultado = resultadoEnum,
                        motivo = entidad.motivo
                    )
                }
                _uiState.update { it.copy(pruebas = listaPruebas) }
            }
        }
    }


    fun actualizarEstadoDocumento(estado: EstadoProtocolo) {
        val p = _uiState.value.protocolo ?: return
        viewModelScope.launch {
            val actual = protocoloDao.obtenerProtocoloPorNumero(p.numero)
            if (actual != null) {
                protocoloDao.actualizarProtocolo(actual.copy(estado = estado.name))
            }
            _uiState.update { actualState ->
                actualState.copy(protocolo = p.copy(estado = estado))
            }
        }
    }

    fun guardarUbicacion(latitud: Double, longitud: Double) {
        val numero = _uiState.value.protocolo?.numero ?: return
        viewModelScope.launch {
            val actual = protocoloDao.obtenerProtocoloPorNumero(numero)
            if (actual != null) {
                protocoloDao.actualizarProtocolo(actual.copy(latitud = latitud, longitud = longitud))
            }
            _uiState.update {
                it.copy(
                    latitud = latitud,
                    longitud = longitud,
                    mensajeUbicacion = "Ubicación guardada en Room"
                )
            }
        }
    }

    fun informarUbicacion(mensaje: String) {
        _uiState.update { it.copy(mensajeUbicacion = mensaje) }
    }

    fun cambiarNombreReceptor(valor: String) =
        _uiState.update { it.copy(nombreReceptor = valor, errorRecepcion = null) }

    fun cambiarRutReceptor(valor: String) =
        _uiState.update { it.copy(rutReceptor = valor, errorRecepcion = null) }

    fun cambiarCargo(valor: String) =
        _uiState.update { it.copy(cargo = valor, errorRecepcion = null) }

    fun cambiarFechaRecepcion(valor: String) =
        _uiState.update { it.copy(fechaRecepcion = valor, errorRecepcion = null) }

    fun prepararRecepcion() {
        _uiState.update {
            it.copy(
                errorRecepcion = null,
                fechaRecepcion = it.fechaRecepcion.ifBlank { fechaHoy() }
            )
        }
    }

    fun guardarFotoEvidencia(bitmap: Bitmap) {
        _uiState.update { it.copy(fotoEvidencia = bitmap) }
    }

    fun guardarRecepcion(): Boolean {
        val estado = _uiState.value
        val protocolo = estado.protocolo ?: return false

        val error = validarRecepcion(
            estado.nombreReceptor,
            estado.rutReceptor,
            estado.cargo,
            estado.fechaRecepcion
        )
        if (error != null) {
            _uiState.update { it.copy(errorRecepcion = error) }
            return false
        }

        val acta = RecepcionActa(
            nombreReceptor = estado.nombreReceptor.trim(),
            rutReceptor = estado.rutReceptor.trim(),
            cargo = estado.cargo.trim(),
            fecha = estado.fechaRecepcion.trim()
        )
        recepciones[protocolo.numero] = acta

        // Persiste en Room SQLite cambiando el estado a FORMALIZADO
        viewModelScope.launch {
            protocoloDao.actualizarProtocolo(
                ProtocoloEntity(
                    numero = protocolo.numero,
                    tagEquipo = protocolo.equipo.tag,
                    clienteNombre = protocolo.equipo.cliente.nombre,
                    estado = "FORMALIZADO",
                    pruebasAprobadas = protocolo.pruebasAprobadas,
                    pruebasTotales = protocolo.pruebasTotales,
                    latitud = estado.latitud,
                    longitud = estado.longitud,
                    fechaRegistro = System.currentTimeMillis()
                )
            )
        }

        _uiState.update {
            it.copy(
                protocolo = protocolo.copy(estado = EstadoProtocolo.FORMALIZADO), // 👈 AGREGA ESTA LÍNEA
                recepcion = acta,
                errorRecepcion = null,
                nombreReceptor = "",
                rutReceptor = "",
                cargo = ""
            )
        }
        return true
    }

}
