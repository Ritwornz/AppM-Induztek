package cl.duocuc.induztektrabajo.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cl.duocuc.induztektrabajo.model.RegistroPrueba
import cl.duocuc.induztektrabajo.model.ResultadoPrueba
import cl.duocuc.induztektrabajo.model.TipoPrueba
import cl.duocuc.induztektrabajo.model.calcularErrorTtr
import cl.duocuc.induztektrabajo.model.calcularIP
import cl.duocuc.induztektrabajo.ui.components.BotonVolverFlecha
import cl.duocuc.induztektrabajo.ui.theme.NaranjaInduztek
import cl.duocuc.induztektrabajo.viewmodel.RegistroUiState
import java.util.Locale

private fun colorResultado(r: ResultadoPrueba): Color = when (r) {
    ResultadoPrueba.APROBADA -> Color(0xFF258A4B)
    ResultadoPrueba.OBSERVADA -> Color(0xFF9A6700)
    ResultadoPrueba.RECHAZADA -> Color(0xFFB3261E)
}


private fun Double.f2() = String.format(Locale.US, "%.2f", this)

@Composable
fun RegistroPruebasScreen(
    estado: RegistroUiState,
    onTipo: (TipoPrueba) -> Unit,
    onValorA: (String) -> Unit,
    onValorB: (String) -> Unit,
    onResultado: (ResultadoPrueba) -> Unit,
    onMotivo: (String) -> Unit,
    onGuardarPrueba: () -> Unit,
    onFinalizar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val p = estado.protocolo

    if (p == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cargando protocolo de pruebas...", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onVolver) {
                    Text("Volver al inicio")
                }
            }
        }
        return
    }

    var valorALocal by rememberSaveable { mutableStateOf(estado.valorA) }
    var valorBLocal by rememberSaveable { mutableStateOf(estado.valorB) }
    var motivoLocal by rememberSaveable { mutableStateOf(estado.motivo) }

    LaunchedEffect(Unit) {
        if (valorALocal.isNotBlank() && estado.valorA.isBlank()) {
            onValorA(valorALocal)
        }
        if (valorBLocal.isNotBlank() && estado.valorB.isBlank()) {
            onValorB(valorBLocal)
        }
        if (motivoLocal.isNotBlank() && estado.motivo.isBlank()) {
            onMotivo(motivoLocal)
        }
    }



    var fotoPruebaLocal by remember { mutableStateOf<Bitmap?>(null) }
    var intentosPermisoCamara by rememberSaveable { mutableIntStateOf(0) }
    var mostrarDialogoPermisoExplicacion by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogoIrAjustes by rememberSaveable { mutableStateOf(false) }
    var mostrarAdvertenciaSinFoto by rememberSaveable { mutableStateOf(false) }

    fun actualizarA(nuevo: String) {
        valorALocal = nuevo
        onValorA(nuevo)
    }

    fun actualizarB(nuevo: String) {
        valorBLocal = nuevo
        onValorB(nuevo)
    }

    fun actualizarMot(nuevo: String) {
        motivoLocal = nuevo
        onMotivo(nuevo)
    }

    val launcherCamaraPrueba = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            fotoPruebaLocal = bitmap
        }
    }

    val launcherPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            intentosPermisoCamara = 0
            launcherCamaraPrueba.launch(null)
        } else {
            intentosPermisoCamara++
            val activity = context as? Activity
            val debeExplicar = activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA
            )

            if (debeExplicar || intentosPermisoCamara < 2) {
                mostrarDialogoPermisoExplicacion = true
            } else {
                mostrarDialogoIrAjustes = true
            }
        }
    }

    fun solicitarTomarFoto() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            launcherCamaraPrueba.launch(null)
        } else {
            launcherPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    val esIP = estado.tipo == TipoPrueba.AISLAMIENTO

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { BotonVolverFlecha(onClick = onVolver, descripcion = "Volver al inicio") }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(p.numero, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(
                        "${p.equipo.tag} · ${p.equipo.tipo.etiqueta}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${p.equipo.cliente.nombre} — ${p.equipo.cliente.instalacion}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("Pruebas registradas: ${estado.registros.size}")
                    estado.global?.let {
                        Text(
                            "Resultado global: ${it.etiqueta}",
                            color = colorResultado(it),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item { Text("Nueva prueba", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoPrueba.entries.forEach { t ->
                    FilterChip(
                        selected = estado.tipo == t,
                        onClick = { onTipo(t) },
                        label = { Text(if (t == TipoPrueba.AISLAMIENTO) "Aislamiento (IP)" else "TTR") }
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(estado.tipo.descripcion, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = valorALocal,
                    onValueChange = { actualizarA(it) },
                    singleLine = true,
                    label = { Text(if (esIP) "Lectura 1 min (MΩ)" else "Relación esperada (Placa)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = valorBLocal,
                    onValueChange = { actualizarB(it) },
                    singleLine = true,
                    label = { Text(if (esIP) "Lectura 10 min (MΩ)" else "Relación medida (Terreno)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                val a = valorALocal.replace(',', '.').toDoubleOrNull()
                val b = valorBLocal.replace(',', '.').toDoubleOrNull()
                if (a != null && b != null) {
                    val calc = if (esIP) calcularIP(a, b)?.let { "Índice de polarización: ${it.f2()}" }
                    else calcularErrorTtr(a, b)?.let { "% de error TTR: ${it.f2()} %" }
                    calc?.let { Text(it, fontWeight = FontWeight.SemiBold) }
                }


                Text("Resultado")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResultadoPrueba.entries.forEach { r ->
                        FilterChip(
                            selected = estado.resultado == r,
                            onClick = { onResultado(r) },
                            label = { Text(r.etiqueta) }
                        )
                    }
                }
                if (estado.resultado != null && estado.resultado != ResultadoPrueba.APROBADA) {
                    OutlinedTextField(
                        value = motivoLocal,
                        onValueChange = { actualizarMot(it) },
                        label = { Text("Motivo (obligatorio)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Evidencia fotográfica del instrumento",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (fotoPruebaLocal != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                bitmap = fotoPruebaLocal!!.asImageBitmap(),
                                contentDescription = "Foto capturada",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            )
                            Column {
                                Text(
                                    "✔ Fotografía adjunta a la prueba",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                TextButton(onClick = { solicitarTomarFoto() }) {
                                    Text("Repetir foto")
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { solicitarTomarFoto() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Tomar fotografía")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tomar foto al display del medidor")
                        }
                        Text(
                            text = "Aviso: Si no adjuntas foto, la app te solicitará confirmación al guardar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (fotoPruebaLocal == null) {
                        mostrarAdvertenciaSinFoto = true
                    } else {
                        onGuardarPrueba()
                        valorALocal = ""
                        valorBLocal = ""
                        motivoLocal = ""
                        fotoPruebaLocal = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NaranjaInduztek,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )

            ) {
                Text("Guardar prueba", fontWeight = FontWeight.Bold)

            }

        }

        if (estado.registros.isNotEmpty()) {
            item { Text("Pruebas registradas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            items(estado.registros) { TarjetaRegistro(it) }
            item {
                OutlinedButton(onClick = onFinalizar, modifier = Modifier.fillMaxWidth()) {
                    Text("Finalizar registro en terreno")
                }
            }
        }
    }

    if (mostrarAdvertenciaSinFoto) {
        AlertDialog(
            onDismissRequest = { mostrarAdvertenciaSinFoto = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Sin evidencia fotográfica") },
            text = { Text("No has adjuntado una fotografía del display o instrumento para esta prueba. ¿Deseas capturarla ahora o guardar la prueba de todas formas?") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarAdvertenciaSinFoto = false
                        solicitarTomarFoto()
                    }
                ) {
                    Text("Tomar foto")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarAdvertenciaSinFoto = false
                        onGuardarPrueba()
                    }
                ) {
                    Text("Guardar igual")
                }
            }
        )
    }

    if (mostrarDialogoPermisoExplicacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermisoExplicacion = false },
            title = { Text("Permiso de Cámara Requerido") },
            text = { Text("Induztek necesita acceder a la cámara para capturar la evidencia del instrumento en terreno para la auditoría eléctrica.") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoPermisoExplicacion = false
                        launcherPermisoCamara.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Reintentar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPermisoExplicacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoIrAjustes) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoIrAjustes = false },
            title = { Text("Permiso Denegado") },
            text = { Text("Has denegado el permiso de cámara. Para utilizar esta función debes activarlo manualmente desde los Ajustes de la aplicación.") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoIrAjustes = false
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Text("Abrir Ajustes")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoIrAjustes = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaRegistro(r: RegistroPrueba) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(r.tipo.etiqueta, fontWeight = FontWeight.SemiBold)
            val detalle = if (r.tipo == TipoPrueba.AISLAMIENTO)
                "1 min: ${r.valorA} MΩ · 10 min: ${r.valorB} MΩ · IP: ${calcularIP(r.valorA, r.valorB)?.f2()}"
            else
                "Esperada: ${r.valorA} · Medida: ${r.valorB} · Error: ${calcularErrorTtr(r.valorA, r.valorB)?.f2()} %"
            Text(detalle, style = MaterialTheme.typography.bodySmall)
            Text(r.resultado.etiqueta, color = colorResultado(r.resultado), fontWeight = FontWeight.Bold)
            if (r.motivo.isNotBlank()) Text("Motivo: ${r.motivo}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
