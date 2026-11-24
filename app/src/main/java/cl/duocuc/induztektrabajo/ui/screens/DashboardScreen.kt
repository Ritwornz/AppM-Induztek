package cl.duocuc.induztektrabajo.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duocuc.induztektrabajo.model.EstadoProtocolo
import cl.duocuc.induztektrabajo.model.ProtocoloResumen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    protocolos: List<ProtocoloResumen>,
    rolUsuario: String = "",
    onVolver: () -> Unit
) {
    val esCliente = rolUsuario.contains("Cliente", ignoreCase = true)


    val protocolosFiltrados = if (esCliente) {
        protocolos.filter {
            it.equipo.cliente.nombre.contains("Minera", ignoreCase = true) &&
                    it.estado != EstadoProtocolo.BORRADOR
        }
    } else {
        protocolos
    }

    val total = protocolosFiltrados.size
    val formalizados = protocolosFiltrados.count { it.estado == EstadoProtocolo.FORMALIZADO }
    val borradores = protocolosFiltrados.count { it.estado == EstadoProtocolo.BORRADOR }
    val enRevision = protocolosFiltrados.count {
        it.estado == EstadoProtocolo.OBSERVADO || it.estado == EstadoProtocolo.REGISTRADO_TERRENO
    }

    val pruebasTotales = protocolosFiltrados.sumOf { it.pruebasTotales }
    val pruebasAprobadas = protocolosFiltrados.sumOf { it.pruebasAprobadas }
    val porcentajeAprobacion = if (pruebasTotales > 0) {
        pruebasAprobadas.toFloat() / pruebasTotales.toFloat()
    } else 0f


    var animar by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animar = true
    }

    val progresoAnimado by animateFloatAsState(
        targetValue = if (animar) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "animacionDona"
    )

    val barraProgresoAnimada by animateFloatAsState(
        targetValue = if (animar) porcentajeAprobacion else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "animacionBarra"
    )

    val colorFormalizado = Color(0xFF2E7D32)
    val colorBorrador = Color(0xFFE65100)
    val colorRevision = Color(0xFF1976D2)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Métricas y Estadísticas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Distribución de Protocolos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(190.dp)
                    ) {
                        Canvas(modifier = Modifier.size(170.dp)) {
                            val strokeWidth = 28.dp.toPx()

                            if (total == 0) {
                                drawCircle(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    style = Stroke(strokeWidth)
                                )
                            } else {
                                var anguloInicio = -90f

                                val anguloFormalizados = (formalizados.toFloat() / total) * 360f * progresoAnimado
                                val anguloBorradores = (borradores.toFloat() / total) * 360f * progresoAnimado
                                val anguloRevision = (enRevision.toFloat() / total) * 360f * progresoAnimado

                                if (anguloFormalizados > 0) {
                                    drawArc(
                                        color = colorFormalizado,
                                        startAngle = anguloInicio,
                                        sweepAngle = anguloFormalizados,
                                        useCenter = false,
                                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                    )
                                    anguloInicio += (formalizados.toFloat() / total) * 360f
                                }

                                if (anguloBorradores > 0) {
                                    drawArc(
                                        color = colorBorrador,
                                        startAngle = anguloInicio,
                                        sweepAngle = anguloBorradores,
                                        useCenter = false,
                                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                    )
                                    anguloInicio += (borradores.toFloat() / total) * 360f
                                }

                                if (anguloRevision > 0) {
                                    drawArc(
                                        color = colorRevision,
                                        startAngle = anguloInicio,
                                        sweepAngle = anguloRevision,
                                        useCenter = false,
                                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$total",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ElementoLeyenda(color = colorFormalizado, etiqueta = "Formalizados", cantidad = formalizados)
                        ElementoLeyenda(color = colorBorrador, etiqueta = "Borradores", cantidad = borradores)
                        ElementoLeyenda(color = colorRevision, etiqueta = "Pendientes", cantidad = enRevision)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Efectividad de Ensayos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(barraProgresoAnimada * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorFormalizado
                        )
                    }

                    LinearProgressIndicator(
                        progress = { barraProgresoAnimada },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = colorFormalizado,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Text(
                        text = "$pruebasAprobadas de $pruebasTotales pruebas aprobadas conforme a norma IEEE / NCh.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$formalizados",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorFormalizado
                        )
                        Text(
                            text = "Actas listas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$borradores",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorBorrador
                        )
                        Text(
                            text = "En terreno",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ElementoLeyenda(
    color: Color,
    etiqueta: String,
    cantidad: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = CircleShape)
        )
        Column {
            Text(
                text = "$cantidad",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
