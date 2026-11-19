package cl.duocuc.induztektrabajo.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duocuc.induztektrabajo.model.RegistroPrueba
import cl.duocuc.induztektrabajo.model.ResultadoPrueba
import cl.duocuc.induztektrabajo.model.TipoPrueba
import cl.duocuc.induztektrabajo.model.calcularErrorTtr
import cl.duocuc.induztektrabajo.model.calcularIP
import cl.duocuc.induztektrabajo.model.puedeEditarPruebas
import cl.duocuc.induztektrabajo.ui.components.BotonVolverFlecha
import cl.duocuc.induztektrabajo.util.Ubicacion
import cl.duocuc.induztektrabajo.viewmodel.ResumenUiState
import cl.duocuc.induztektrabajo.model.EstadoProtocolo
import cl.duocuc.induztektrabajo.ui.theme.Azul
import cl.duocuc.induztektrabajo.ui.theme.NaranjaInduztek
import java.util.Locale

private fun colorResultado(r: ResultadoPrueba): Color = when (r) {
    ResultadoPrueba.APROBADA -> Color(0xFF258A4B)
    ResultadoPrueba.OBSERVADA -> Color(0xFF9A6700)
    ResultadoPrueba.RECHAZADA -> Color(0xFFB3261E)
}

private fun Double.f2() = String.format(Locale.US, "%.2f", this)

@Composable
fun ResumenProtocoloScreen(
    estado: ResumenUiState,
    onVolverPruebas: () -> Unit,
    onRegistrarRecepcion: () -> Unit,
    onVolverInicio: () -> Unit,
    onUbicacionObtenida: (Double, Double) -> Unit,
    onMensajeUbicacion: (String) -> Unit,
    esCliente: Boolean = false,
    modifier: Modifier = Modifier
) {

    val p = estado.protocolo
    if (p == null) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            onVolverInicio()
        }
        return
    }

    val contexto = LocalContext.current
    val actividad = contexto as? Activity

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (!concedido) {
            onMensajeUbicacion(
                "Permiso de ubicación denegado. El protocolo se puede cerrar igual: la ubicación no es obligatoria."
            )
            return@rememberLauncherForActivityResult
        }
        val loc = Ubicacion.ultimaConocida(contexto)
        if (loc == null) {
            onMensajeUbicacion(
                "No hay señal GPS o aún no hay una ubicación conocida. Puedes continuar sin registrarla."
            )
        } else {
            onUbicacionObtenida(loc.latitude, loc.longitude)
        }
    }


    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { BotonVolverFlecha(onClick = onVolverInicio, descripcion = "Volver al inicio") }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Resumen del protocolo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(p.numero, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("TAG: ${p.equipo.tag}")
                    Text("Cliente: ${p.equipo.cliente.nombre}")
                    Text("Equipo: ${p.equipo.tipo.etiqueta}")
                    Text("Fecha: ${p.fecha}")
                    Text("Técnico responsable: ${estado.tecnico}")
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Resultado eléctrico global", fontWeight = FontWeight.SemiBold)
                    val global = estado.global
                    if (global == null) {
                        Text("Sin pruebas registradas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(global.etiqueta, color = colorResultado(global), fontWeight = FontWeight.Bold)
                    }
                    Text("Estado del documento", fontWeight = FontWeight.SemiBold)
                    Text(p.estado.etiqueta, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(
                        "El resultado de las pruebas no es lo mismo que la formalización del documento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text("Pruebas registradas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        if (estado.pruebas.isEmpty()) {
            item {
                Text(
                    "Aún no hay pruebas en esta sesión para este protocolo.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(estado.pruebas) { prueba -> TarjetaResumenPrueba(prueba) }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Recepción del acta",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Comprueba si está en memoria o si ya fue FORMALIZADO en Room
                    val yaFormalizado = estado.recepcion != null || estado.protocolo?.estado == EstadoProtocolo.FORMALIZADO

                    if (yaFormalizado) {
                        val receptorTexto = estado.recepcion?.let { "Acta recibida por ${it.nombreReceptor}" }
                            ?: "Acta formalizada y archivada en terreno"
                        val detallesTexto = estado.recepcion?.let { "${it.cargo} · ${it.rutReceptor} · ${it.fecha}" }
                            ?: "Protocolo formalizado · Conforme con la supervisión de cliente"

                        Text(receptorTexto, fontWeight = FontWeight.Medium)
                        Text(
                            detallesTexto,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "✔ Evidencia fotográfica de acta firmada adjunta",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text("Pendiente de recepción", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)

                        if (!esCliente) {
                            Button(
                                onClick = onRegistrarRecepcion,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NaranjaInduztek,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )

                            ) {
                                Text("Registrar recepción del acta", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                "Pendiente de formalización en terreno por el técnico responsable.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Text(
                        "Recibir el acta no equivale a la aprobación técnica del ingeniero.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }



        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ubicación GPS (opcional)", fontWeight = FontWeight.SemiBold)
                    if (estado.latitud != null && estado.longitud != null) {
                        Text("Latitud: ${estado.latitud}")
                        Text("Longitud: ${estado.longitud}")
                    }
                    estado.mensajeUbicacion?.let {
                        Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Solo el técnico en terreno puede registrar la ubicación GPS
                    if (!esCliente) {
                        OutlinedButton(
                            onClick = {
                                if (Ubicacion.permisoConcedido(contexto)) {
                                    val loc = Ubicacion.ultimaConocida(contexto)
                                    if (loc == null) {
                                        onMensajeUbicacion(
                                            "No hay señal GPS o aún no hay una ubicación conocida. Puedes continuar sin registrarla."
                                        )
                                    } else {
                                        onUbicacionObtenida(loc.latitude, loc.longitude)
                                    }
                                } else {
                                    if (actividad != null) {
                                        lanzadorPermiso.launch(Ubicacion.PERMISO_FINO)
                                    } else {
                                        onMensajeUbicacion("No se pudo solicitar el permiso de ubicación.")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NaranjaInduztek,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )

                        ) {
                            Text("Registrar ubicación de la visita")
                        }
                    }
                }
            }
        }

        if (p.estado.puedeEditarPruebas() && !esCliente) {
            item {
                OutlinedButton(onClick = onVolverPruebas,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NaranjaInduztek,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )

                ) {
                    Text("Volver a pruebas")
                }
            }
        }

    }
}


@Composable
private fun TarjetaResumenPrueba(r: RegistroPrueba) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(r.tipo.etiqueta, fontWeight = FontWeight.SemiBold)
            val calculado = if (r.tipo == TipoPrueba.AISLAMIENTO) {
                "IP: ${calcularIP(r.valorA, r.valorB)?.f2() ?: "—"}"
            } else {
                "% de error TTR: ${calcularErrorTtr(r.valorA, r.valorB)?.f2() ?: "—"} %"
            }
            Text(calculado)
            Text(r.resultado.etiqueta, color = colorResultado(r.resultado), fontWeight = FontWeight.Bold)
            if (r.motivo.isNotBlank()) {
                Text("Motivo: ${r.motivo}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
