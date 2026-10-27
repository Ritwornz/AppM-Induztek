package cl.duocuc.induztektrabajo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.induztektrabajo.data.local.dao.MedicionDao
import cl.duocuc.induztektrabajo.data.local.entity.MedicionEntity
import cl.duocuc.induztektrabajo.model.ProtocoloResumen
import cl.duocuc.induztektrabajo.model.RegistroPrueba
import cl.duocuc.induztektrabajo.model.ResultadoPrueba
import cl.duocuc.induztektrabajo.model.TipoPrueba
import cl.duocuc.induztektrabajo.model.resultadoGlobal
import cl.duocuc.induztektrabajo.model.validarRegistro
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroUiState(
    val protocolo: ProtocoloResumen? = null,
    val tipo: TipoPrueba = TipoPrueba.AISLAMIENTO,
    val valorA: String = "",
    val valorB: String = "",
    val resultado: ResultadoPrueba? = null,
    val motivo: String = "",
    val error: String? = null,
    val registros: List<RegistroPrueba> = emptyList()
) {
    val global: ResultadoPrueba?
        get() = resultadoGlobal(registros)
}

class RegistroViewModel(
    private val medicionDao: MedicionDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    private var jobObservadorMediciones: Job? = null

    fun abrir(protocolo: ProtocoloResumen) {
        // Si ya estamos en este protocolo y tiene datos cargados, no reiniciamos los campos
        if (_uiState.value.protocolo?.numero == protocolo.numero) {
            return
        }

        jobObservadorMediciones?.cancel()

        _uiState.update {
            it.copy(
                protocolo = protocolo,
                registros = emptyList(),
                tipo = TipoPrueba.AISLAMIENTO,
                valorA = "",
                valorB = "",
                resultado = null,
                motivo = "",
                error = null
            )
        }

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
                _uiState.update { it.copy(registros = listaPruebas) }
            }
        }
    }


    fun cambiarTipo(tipo: TipoPrueba) {
        _uiState.update {
            it.copy(
                tipo = tipo,
                valorA = "",
                valorB = "",
                resultado = null,
                motivo = "",
                error = null
            )
        }
    }

    fun cambiarValorA(valor: String) {
        _uiState.update {
            val nuevo = it.copy(valorA = valor, error = null)
            nuevo.copy(resultado = calcularResultadoAutomatico(nuevo.tipo, valor, nuevo.valorB))
        }
    }

    fun cambiarValorB(valor: String) {
        _uiState.update {
            val nuevo = it.copy(valorB = valor, error = null)
            nuevo.copy(resultado = calcularResultadoAutomatico(nuevo.tipo, nuevo.valorA, valor))
        }
    }

    fun cambiarResultado(resultado: ResultadoPrueba) =
        _uiState.update { it.copy(resultado = resultado, error = null) }

    fun cambiarMotivo(motivo: String) =
        _uiState.update { it.copy(motivo = motivo, error = null) }

    private fun calcularResultadoAutomatico(
        tipo: TipoPrueba,
        vA: String,
        vB: String
    ): ResultadoPrueba? {
        val a = vA.trim().replace(',', '.').toDoubleOrNull() ?: return null
        val b = vB.trim().replace(',', '.').toDoubleOrNull() ?: return null
        if (a <= 0 || b <= 0) return null

        // LOG EN ROJO PARA VERLO DE INMEDIATO EN LOGCAT:
        android.util.Log.e("PRUEBA_DEBUG", "Tipo: $tipo | Campo1(1min): $a | Campo2(10min): $b")

        return when (tipo) {
            TipoPrueba.AISLAMIENTO -> {
                val ip = cl.duocuc.induztektrabajo.model.calcularIP(lectura1Min = a, lectura10Min = b) ?: return null
                android.util.Log.e("PRUEBA_DEBUG", "IP calculado: $ip")

                when {
                    ip >= 2.0 -> ResultadoPrueba.APROBADA
                    ip >= 1.2 -> ResultadoPrueba.OBSERVADA
                    else -> ResultadoPrueba.RECHAZADA
                }
            }
            TipoPrueba.TTR -> {
                val error = cl.duocuc.induztektrabajo.model.calcularErrorTtr(esperada = a, medida = b) ?: return null
                android.util.Log.e("PRUEBA_DEBUG", "Error TTR: $error%")

                when {
                    error <= 0.5 -> ResultadoPrueba.APROBADA
                    error <= 1.0 -> ResultadoPrueba.OBSERVADA
                    else -> ResultadoPrueba.RECHAZADA
                }
            }
        }
    }



    fun guardarPrueba() {
        val estado = _uiState.value
        val protocolo = estado.protocolo ?: return

        val valorADouble = estado.valorA.replace(',', '.').toDoubleOrNull()
        val valorBDouble = estado.valorB.replace(',', '.').toDoubleOrNull()

        if (valorADouble == null || valorBDouble == null) {
            _uiState.update { it.copy(error = "Debe ingresar ambos valores numéricos") }
            return
        }

        if (estado.resultado == null) {
            _uiState.update { it.copy(error = "Debe seleccionar o calcular un resultado") }
            return
        }

        if (estado.resultado == ResultadoPrueba.OBSERVADA && estado.motivo.isBlank()) {
            _uiState.update { it.copy(error = "El motivo es obligatorio para pruebas observadas") }
            return
        }

        viewModelScope.launch {
            val nuevaMedicion = MedicionEntity(
                protocoloNumero = protocolo.numero,
                tipoPrueba = estado.tipo.name,
                valorA = valorADouble,
                valorB = valorBDouble,
                resultado = estado.resultado.name,
                motivo = estado.motivo.trim()
            )

            medicionDao.insertarMedicion(nuevaMedicion)

            _uiState.update {
                it.copy(
                    valorA = "",
                    valorB = "",
                    resultado = null,
                    motivo = "",
                    error = null
                )
            }
        }
    }
}
