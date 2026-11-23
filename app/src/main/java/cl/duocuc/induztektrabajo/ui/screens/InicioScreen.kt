package cl.duocuc.induztektrabajo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duocuc.induztektrabajo.model.Equipo
import cl.duocuc.induztektrabajo.model.EstadoProtocolo
import cl.duocuc.induztektrabajo.model.ProtocoloResumen
import cl.duocuc.induztektrabajo.ui.components.BuscadorTag
import cl.duocuc.induztektrabajo.ui.components.TarjetaMetrica
import cl.duocuc.induztektrabajo.ui.components.TarjetaProtocolo
import cl.duocuc.induztektrabajo.viewmodel.InicioUiState
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.width
import cl.duocuc.induztektrabajo.ui.theme.NaranjaInduztek
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    estado: InicioUiState,
    nombreUsuario: String = "Carlos Sánchez",
    rolUsuario: String = "Técnico de terreno",
    correoUsuario: String = "carlos.sanchez@induztek.cl",
    onBusquedaCambia: (String) -> Unit,
    onNuevoProtocolo: () -> Unit,
    onCerrarDialogo: () -> Unit,
    onEquipoElegido: (Equipo) -> Unit,
    onCerrarSesion: () -> Unit,
    onProtocoloElegido: (ProtocoloResumen) -> Unit,
    onIrAGestionUsuarios: () -> Unit = {},
    modifier: Modifier = Modifier,
    onNavegarPerfil: () -> Unit = {},
    onNavegarDashboard: () -> Unit = {},
    onReintentarClima: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var mostrarBandejaNotificaciones by remember { mutableStateOf(false) }

    val empresaUsuario = if (nombreUsuario.contains("(") && nombreUsuario.contains(")")) {
        nombreUsuario.substringAfter("(").substringBefore(")").trim()
    } else {
        nombreUsuario
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val iniciales = nombreUsuario
                            .split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .joinToString("")

                        Text(
                            text = if (iniciales.isNotBlank()) iniciales else "U",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = nombreUsuario,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = correoUsuario,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = rolUsuario,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                    label = { Text("Protocolos de prueba") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),


                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Mi Perfil") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onNavegarPerfil()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                    label = { Text("Métricas y Estadísticas") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onNavegarDashboard()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                if (rolUsuario.equals("Administrador", ignoreCase = true)) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Gestión de Usuarios (Admin)") },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                onIrAGestionUsuarios()
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }


                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onCerrarSesion()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Induztek",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menú principal"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    actions = {

                        val esCliente = rolUsuario == "Cliente supervisor"
                        if (esCliente) {

                            val formalizadosCliente = estado.todos.filter {
                                (it.equipo.cliente.nombre.contains(empresaUsuario, ignoreCase = true) ||
                                        empresaUsuario.contains(it.equipo.cliente.nombre, ignoreCase = true)) &&
                                        it.estado == EstadoProtocolo.FORMALIZADO
                            }


                            IconButton(onClick = { mostrarBandejaNotificaciones = true }) {
                                BadgedBox(
                                    badge = {
                                        if (formalizadosCliente.isNotEmpty()) {
                                            Badge { Text("${formalizadosCliente.size}") }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Bandeja de notificaciones"
                                    )
                                }
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                if (rolUsuario != "Cliente supervisor") {
                    ExtendedFloatingActionButton(
                        onClick = onNuevoProtocolo,
                        containerColor = NaranjaInduztek,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("+  Nuevo protocolo", fontWeight = FontWeight.Bold)
                    }
                }
            },
            modifier = modifier
        ) { innerPadding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val esCliente = rolUsuario == "Cliente supervisor" || rolUsuario.contains("CLIENTE", ignoreCase = true)
                val listaAMostrar = if (esCliente) {
                    estado.visibles.filter {
                        (it.equipo.cliente.nombre.contains(empresaUsuario, ignoreCase = true) ||
                                empresaUsuario.contains(it.equipo.cliente.nombre, ignoreCase = true)) &&
                                it.estado != EstadoProtocolo.BORRADOR
                    }
                } else {
                    estado.visibles
                }

                val totalMostrar = if (esCliente) listaAMostrar.size else estado.todos.size
                val formalizadosMostrar = if (esCliente) {
                    listaAMostrar.count { it.estado == EstadoProtocolo.FORMALIZADO }
                } else {
                    estado.formalizados
                }
                val secundariosMostrar = if (esCliente) {
                    totalMostrar - formalizadosMostrar
                } else {
                    estado.borradores
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!esCliente) {
                        item {
                            TarjetaClimaFaena(
                                estado = estado.clima,
                                onReintentar = onReintentarClima
                            )
                        }
                    }

                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TarjetaMetrica("Total", totalMostrar, Modifier.weight(1f))
                            TarjetaMetrica(if (esCliente) "Pendientes" else "Borradores", secundariosMostrar, Modifier.weight(1f))
                            TarjetaMetrica("Formalizados", formalizadosMostrar, Modifier.weight(1f))
                        }
                    }

                    item { BuscadorTag(estado.busqueda, onBusquedaCambia) }

                    item {
                        Text(
                            "Protocolos recientes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (listaAMostrar.isEmpty()) {
                        item {
                            Text(
                                if (esCliente) "No hay protocolos registrados para su empresa aún"
                                else "No hay protocolos que coincidan con \"${estado.busqueda}\"",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(listaAMostrar, key = { it.numero }) { p ->
                        TarjetaProtocolo(p, Modifier.clickable { onProtocoloElegido(p) })
                    }
                }
            }
        }
    }
    if (mostrarBandejaNotificaciones) {

        val formalizadosCliente = estado.todos.filter {
            (it.equipo.cliente.nombre.contains(empresaUsuario, ignoreCase = true) ||
                    empresaUsuario.contains(it.equipo.cliente.nombre, ignoreCase = true)) &&
                    it.estado == EstadoProtocolo.FORMALIZADO
        }


        AlertDialog(
            onDismissRequest = { mostrarBandejaNotificaciones = false },
            title = { Text("Bandeja de Notificaciones") },
            text = {
                if (formalizadosCliente.isEmpty()) {
                    Text("No tienes notificaciones pendientes.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(formalizadosCliente, key = { "notif_" + it.numero }) { p ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        mostrarBandejaNotificaciones = false
                                        onProtocoloElegido(p)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "⚡ Protocolo Formalizado en Terreno",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "${p.numero} · ${p.equipo.tag}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "El técnico formalizó el protocolo para ${p.equipo.cliente.nombre}. Toca aquí para ver el resumen.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarBandejaNotificaciones = false }) {
                    Text("Cerrar")
                }
            }
        )
    }


    if (estado.dialogoNuevoVisible) {
        DialogoNuevoProtocolo(estado.equipos, onEquipoElegido, onCerrarDialogo)
    }
}

@Composable
fun DialogoNuevoProtocolo(
    equipos: List<Equipo>,
    onEquipoElegido: (Equipo) -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Selecciona el equipo") },
        text = {
            LazyColumn {
                items(equipos, key = { it.id }) { equipo ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onEquipoElegido(equipo) }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            equipo.tag,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${equipo.tipo.etiqueta} · ${equipo.cliente.nombre}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}

@Composable
fun TarjetaClimaFaena(
    estado: cl.duocuc.induztektrabajo.viewmodel.ClimaUiState,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Clima",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Condiciones Ambientales en Faena",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (estado) {
                is cl.duocuc.induztektrabajo.viewmodel.ClimaUiState.Cargando -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Consultando estación meteorológica...")
                    }
                }
                is cl.duocuc.induztektrabajo.viewmodel.ClimaUiState.Exito -> {
                    val clima = estado.clima
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Temperatura", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "${clima.temperatura} °C",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Humedad Relativa", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "${clima.humedad} %",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Viento", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "${clima.velocidadViento} km/h",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Open-Meteo API en vivo • Base para corrección de aislamiento IEEE",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is cl.duocuc.induztektrabajo.viewmodel.ClimaUiState.Error -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "No se pudo obtener el clima en faena",
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = onReintentar) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}
