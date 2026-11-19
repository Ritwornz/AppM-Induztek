package cl.duocuc.induztektrabajo

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duocuc.induztektrabajo.model.EstadoProtocolo
import cl.duocuc.induztektrabajo.model.ResultadoPrueba
import cl.duocuc.induztektrabajo.ui.screens.DashboardScreen
import cl.duocuc.induztektrabajo.ui.screens.InicioScreen
import cl.duocuc.induztektrabajo.ui.screens.LoginScreen
import cl.duocuc.induztektrabajo.ui.screens.PerfilScreen
import cl.duocuc.induztektrabajo.ui.screens.RecepcionActaScreen
import cl.duocuc.induztektrabajo.ui.screens.RegistroPruebasScreen
import cl.duocuc.induztektrabajo.ui.screens.ResumenProtocoloScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.induztektrabajo.viewmodel.RegistroViewModel
import kotlinx.coroutines.launch

private enum class Destino {
    LOGIN,
    INICIO,
    REGISTRO,
    REGISTRO_USUARIO,
    RESUMEN,
    RECEPCION,
    PERFIL,
    DASHBOARD
}

@Composable
fun InduztekApp() {
    val context = androidx.compose.ui.platform.LocalContext.current

    val db = remember { cl.duocuc.induztektrabajo.data.local.InduztekDatabase.getDatabase(context) }
    val sesionStore = remember { cl.duocuc.induztektrabajo.data.preferences.SesionDataStore(context) }

    val loginViewModel = remember {
        cl.duocuc.induztektrabajo.viewmodel.LoginViewModel(
            usuarioDao = db.usuarioDao(),
            sesionDataStore = sesionStore
        )
    }

    val inicioViewModel = remember {
        cl.duocuc.induztektrabajo.viewmodel.InicioViewModel(
            protocoloDao = db.protocoloDao()
        )
    }

    val registroViewModel: RegistroViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RegistroViewModel(medicionDao = db.medicionDao()) as T
            }
        }
    )

    val resumenViewModel = remember {
        cl.duocuc.induztektrabajo.viewmodel.ResumenViewModel(
            medicionDao = db.medicionDao(),
            protocoloDao = db.protocoloDao()
        )
    }
    val registroUsuarioViewModel = remember {
        cl.duocuc.induztektrabajo.viewmodel.RegistroUsuarioViewModel(db.usuarioDao())
    }



    var destino by rememberSaveable { mutableStateOf(Destino.LOGIN) }
    var protocoloActualNumero by rememberSaveable { mutableStateOf<String?>(null) }

    val estadoLogin by loginViewModel.uiState.collectAsStateWithLifecycle()
    val estadoInicio by inicioViewModel.uiState.collectAsStateWithLifecycle()
    val estadoRegistro by registroViewModel.uiState.collectAsStateWithLifecycle()
    val estadoResumen by resumenViewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sesionActiva by sesionStore.estaLogueado.collectAsStateWithLifecycle(initialValue = false)
    val datosUsuario by sesionStore.obtenerUsuario.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(sesionActiva) {
        if (sesionActiva && destino == Destino.LOGIN) {
            destino = Destino.INICIO
        }
    }

    LaunchedEffect(datosUsuario) {
        inicioViewModel.cargarProtocolos(
            rol = datosUsuario?.rol,
            nombreCompleto = datosUsuario?.nombre
        )
    }


    // Sincroniza el protocolo actual si cambia de número
    LaunchedEffect(protocoloActualNumero, estadoInicio.todos, destino) {
        val numero = protocoloActualNumero ?: return@LaunchedEffect

        estadoInicio.todos.find { it.numero == numero }?.let { protocolo ->
            when (destino) {
                Destino.REGISTRO -> {
                    if (estadoRegistro.protocolo?.numero != protocolo.numero) {
                        registroViewModel.abrir(protocolo)
                    }
                }

                Destino.RESUMEN,
                Destino.RECEPCION -> {
                    if (estadoResumen.protocolo?.numero != protocolo.numero) {
                        resumenViewModel.abrir(protocolo)
                    }
                }

                else -> Unit
            }
        }
    }

    LaunchedEffect(estadoInicio.mensaje) {
        estadoInicio.mensaje?.let {
            snackbar.showSnackbar(it)
            inicioViewModel.limpiarMensaje()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { innerPadding ->
        when (destino) {
            Destino.LOGIN -> LoginScreen(
                estado = estadoLogin,
                onCorreoCambia = loginViewModel::actualizarCorreo,
                onContrasenaCambia = loginViewModel::actualizarContrasena,
                onAlternarContrasena = loginViewModel::alternarContrasenaVisible,
                onIngresar = {
                    loginViewModel.iniciarSesion(
                        onSuccess = { destino = Destino.INICIO }
                    )
                },
                onRecuperarContrasena = { },
                onConfirmarRecuperacion = { correo, nuevaClave, onResultado ->
                    scope.launch {
                        val resultado = loginViewModel.restablecerContrasena(correo, nuevaClave)
                        if (resultado.isSuccess) {
                            snackbar.showSnackbar("Contraseña restablecida con éxito. Inicia sesión.")
                            onResultado(true, null)
                        } else {
                            onResultado(false, resultado.exceptionOrNull()?.message)
                        }
                    }
                },
                onIrARegistro = { destino = Destino.REGISTRO_USUARIO },
                modifier = Modifier.padding(innerPadding)
            )



            Destino.INICIO -> InicioScreen(
                estado = estadoInicio,
                nombreUsuario = datosUsuario?.nombre ?: "Carlos Sánchez",
                rolUsuario = if (datosUsuario?.rol == "CLIENTE") "Cliente supervisor" else "Técnico de terreno",
                correoUsuario = datosUsuario?.correo ?: "carlos.sanchez@induztek.cl",
                onBusquedaCambia = inicioViewModel::actualizarBusqueda,
                onNavegarPerfil = { destino = Destino.PERFIL },
                onNavegarDashboard = { destino = Destino.DASHBOARD },
                onNuevoProtocolo = inicioViewModel::abrirDialogoNuevo,
                onCerrarDialogo = inicioViewModel::cerrarDialogoNuevo,
                onEquipoElegido = inicioViewModel::crearProtocolo,
                onCerrarSesion = {
                    scope.launch {
                        sesionStore.cerrarSesion()
                        protocoloActualNumero = null
                        destino = Destino.LOGIN
                    }
                },
                onProtocoloElegido = { protocolo ->
                    protocoloActualNumero = protocolo.numero
                    registroViewModel.abrir(protocolo)
                    resumenViewModel.abrir(protocolo)
                    if (protocolo.estado == EstadoProtocolo.BORRADOR) {
                        destino = Destino.REGISTRO
                    } else {
                        destino = Destino.RESUMEN
                    }
                },
                modifier = Modifier.padding(innerPadding)
            )

            Destino.REGISTRO -> RegistroPruebasScreen(
                estado = estadoRegistro,
                onTipo = registroViewModel::cambiarTipo,
                onValorA = registroViewModel::cambiarValorA,
                onValorB = registroViewModel::cambiarValorB,
                onResultado = registroViewModel::cambiarResultado,
                onMotivo = registroViewModel::cambiarMotivo,
                onGuardarPrueba = registroViewModel::guardarPrueba,
                onFinalizar = {
                    val p = estadoRegistro.protocolo
                    if (p != null) {
                        val nuevoEstado = when (estadoRegistro.global) {
                            ResultadoPrueba.OBSERVADA, ResultadoPrueba.RECHAZADA -> EstadoProtocolo.OBSERVADO
                            else -> EstadoProtocolo.REGISTRADO_TERRENO
                        }
                        protocoloActualNumero = p.numero
                        inicioViewModel.actualizarAvance(
                            p.numero,
                            estadoRegistro.registros.count { it.resultado == ResultadoPrueba.APROBADA },
                            estadoRegistro.registros.size,
                            nuevoEstado
                        )
                        val actualizado = p.copy(
                            estado = nuevoEstado,
                            pruebasAprobadas = estadoRegistro.registros.count { it.resultado == ResultadoPrueba.APROBADA },
                            pruebasTotales = estadoRegistro.registros.size
                        )
                        resumenViewModel.abrir(actualizado)
                        // No re-insertamos aquí porque guardarPrueba() ya lo hizo en Room
                    }
                    destino = Destino.RESUMEN
                },
                onVolver = { destino = Destino.INICIO },
                modifier = Modifier.padding(innerPadding)
            )

            Destino.RESUMEN -> ResumenProtocoloScreen(
                estado = estadoResumen,
                onVolverPruebas = {
                    estadoResumen.protocolo?.let { registroViewModel.abrir(it) }
                    destino = Destino.REGISTRO
                },
                onRegistrarRecepcion = {
                    resumenViewModel.prepararRecepcion()
                    destino = Destino.RECEPCION
                },
                onVolverInicio = {
                    destino = Destino.INICIO
                },
                onUbicacionObtenida = resumenViewModel::guardarUbicacion,
                onMensajeUbicacion = resumenViewModel::informarUbicacion,
                esCliente = datosUsuario?.rol == "CLIENTE",
                modifier = Modifier.padding(innerPadding)
            )

            Destino.RECEPCION -> RecepcionActaScreen(
                estado = estadoResumen,
                onNombre = resumenViewModel::cambiarNombreReceptor,
                onRut = resumenViewModel::cambiarRutReceptor,
                onCargo = resumenViewModel::cambiarCargo,
                onFecha = resumenViewModel::cambiarFechaRecepcion,
                onFotoCapturada = resumenViewModel::guardarFotoEvidencia,
                onGuardar = {
                    if (resumenViewModel.guardarRecepcion()) {
                        destino = Destino.RESUMEN
                    }
                },
                onVolver = { destino = Destino.RESUMEN },
                modifier = Modifier.padding(innerPadding)
            )

            Destino.PERFIL -> {
                val usuarioActual = datosUsuario
                val nombre = usuarioActual?.nombre ?: "Carlos Sánchez"
                val empresa = if (nombre.contains("(") && nombre.contains(")")) {
                    nombre.substringAfter("(").substringBefore(")").trim()
                } else {
                    "Induztek Ingeniería"
                }

                PerfilScreen(
                    nombreUsuario = nombre,
                    correoUsuario = usuarioActual?.correo ?: "carlos.sanchez@induztek.cl",
                    rolUsuario = when (usuarioActual?.rol) {
                        "ADMINISTRADOR" -> "Administrador"
                        "CLIENTE" -> "Cliente supervisor"
                        else -> "Técnico de terreno"
                    },
                    empresaUsuario = empresa,
                    onVolver = { destino = Destino.INICIO },
                    onCerrarSesion = {
                        scope.launch { sesionStore.cerrarSesion() }
                        protocoloActualNumero = null
                        destino = Destino.LOGIN
                    },
                    onGuardarNombre = { nuevoNombre ->
                        val correo = usuarioActual?.correo
                        val idUsuario = usuarioActual?.id ?: 1
                        val rolUsuario = usuarioActual?.rol ?: "TECNICO"
                        if (correo != null) {
                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                db.usuarioDao().actualizarNombrePorCorreo(correo, nuevoNombre)
                                sesionStore.guardarSesion(
                                    id = idUsuario,
                                    correo = correo,
                                    nombre = nuevoNombre,
                                    rol = rolUsuario
                                )
                            }
                        }
                    },
                    onCambiarPassword = { nuevaClave ->
                        val correo = usuarioActual?.correo
                        if (correo != null) {
                            val nuevoHash = cl.duocuc.induztektrabajo.data.security.PasswordHasher.hashPassword(nuevaClave)
                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                db.usuarioDao().actualizarPasswordPorCorreo(correo, nuevoHash)
                            }
                        }
                    }
                )
            }
                    Destino.REGISTRO_USUARIO -> {
                    cl.duocuc.induztektrabajo.ui.screens.RegistroUsuarioScreen(
                        viewModel = registroUsuarioViewModel,
                        onVolverAlLogin = { destino = Destino.LOGIN },
                        onRegistroExitoso = {
                            destino = Destino.LOGIN
                            scope.launch {
                                snackbar.showSnackbar("Cuenta creada con éxito. Inicia sesión.")
                            }
                        }
                    )
                }

                Destino.DASHBOARD -> {
                    DashboardScreen(
                        protocolos = estadoInicio.todos,
                        rolUsuario = datosUsuario?.rol ?: "",
                        onVolver = { destino = Destino.INICIO }
                    )
                }
        }
    }
}
