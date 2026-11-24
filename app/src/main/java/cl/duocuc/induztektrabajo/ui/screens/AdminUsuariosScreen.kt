package cl.duocuc.induztektrabajo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duocuc.induztektrabajo.data.local.entity.UsuarioEntity
import cl.duocuc.induztektrabajo.viewmodel.AdminUsuariosUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsuariosScreen(
    estado: AdminUsuariosUiState,
    idUsuarioLogueado: Int,
    onBusquedaCambia: (String) -> Unit,
    onCambiarRol: (UsuarioEntity, String) -> Unit,
    onEliminarUsuario: (UsuarioEntity) -> Unit,
    onLimpiarMensaje: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var usuarioParaEditarRol by remember { mutableStateOf<UsuarioEntity?>(null) }
    var usuarioParaEliminar by remember { mutableStateOf<UsuarioEntity?>(null) }

    LaunchedEffect(estado.mensajeFeedback) {
        estado.mensajeFeedback?.let { mensaje ->
            snackbarHostState.showSnackbar(
                message = mensaje,
                duration = SnackbarDuration.Short
            )
            onLimpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gestión de Usuarios", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "${estado.usuarios.size} usuario(s) registrado(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = onBusquedaCambia,
                label = { Text("Buscar por nombre, correo o rol...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (estado.usuarios.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = if (estado.busqueda.isBlank()) "No hay usuarios registrados." else "No se encontraron coincidencias.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(estado.usuarios, key = { it.id }) { usuario ->
                        TarjetaUsuarioAdmin(
                            usuario = usuario,
                            esUsuarioActual = (usuario.id == idUsuarioLogueado),
                            onEditarRolClick = { usuarioParaEditarRol = usuario },
                            onEliminarClick = { usuarioParaEliminar = usuario }
                        )
                    }
                }
            }
        }
    }

    usuarioParaEditarRol?.let { usuario ->
        DialogoCambiarRol(
            usuario = usuario,
            esUsuarioActual = (usuario.id == idUsuarioLogueado),
            onDismiss = { usuarioParaEditarRol = null },
            onConfirmar = { nuevoRol ->
                onCambiarRol(usuario, nuevoRol)
                usuarioParaEditarRol = null
            }
        )
    }

    usuarioParaEliminar?.let { usuario ->
        AlertDialog(
            onDismissRequest = { usuarioParaEliminar = null },
            title = { Text("Confirmar Eliminación") },
            text = {
                Text("¿Estás seguro de que deseas eliminar permanentemente a ${usuario.nombreCompleto} (${usuario.correo}) de la base de datos?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEliminarUsuario(usuario)
                        usuarioParaEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { usuarioParaEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaUsuarioAdmin(
    usuario: UsuarioEntity,
    esUsuarioActual: Boolean,
    onEditarRolClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = usuario.nombreCompleto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (esUsuarioActual) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(Tú)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = usuario.correo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))
                BadgeRol(rol = usuario.rol)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEditarRolClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Cambiar Rol",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onEliminarClick,
                    enabled = !esUsuarioActual
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar Usuario",
                        tint = if (esUsuarioActual) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeRol(rol: String) {
    val (colorFondo, colorTexto) = when (rol.uppercase()) {
        "ADMINISTRADOR" -> Color(0xFFE8DEF8) to Color(0xFF6750A4)
        "TECNICO" -> Color(0xFFFFDBCF) to Color(0xFF8B2D00)
        else -> Color(0xFFD0E4FF) to Color(0xFF004881)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colorFondo)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = rol.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colorTexto
        )
    }
}

@Composable
private fun DialogoCambiarRol(
    usuario: UsuarioEntity,
    esUsuarioActual: Boolean,
    onDismiss: () -> Unit,
    onConfirmar: (String) -> Unit
) {
    val rolesDisponibles = listOf("ADMINISTRADOR", "TECNICO", "CLIENTE")
    var rolSeleccionado by remember { mutableStateOf(usuario.rol.uppercase()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Rol") },
        text = {
            Column {
                Text(
                    text = "Selecciona el nuevo rol para ${usuario.nombreCompleto}:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                rolesDisponibles.forEach { rol ->
                    val deshabilitado = (esUsuarioActual && rol != "ADMINISTRADOR")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (rolSeleccionado == rol),
                            onClick = { if (!deshabilitado) rolSeleccionado = rol },
                            enabled = !deshabilitado
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rol + if (deshabilitado) " (No permitido a ti mismo)" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (deshabilitado) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(rolSeleccionado) },
                enabled = rolSeleccionado != usuario.rol.uppercase()
            ) {
                Text("Guardar Rol")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
