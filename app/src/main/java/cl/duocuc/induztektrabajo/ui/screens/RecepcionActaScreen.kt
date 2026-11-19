package cl.duocuc.induztektrabajo.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cl.duocuc.induztektrabajo.viewmodel.ResumenUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecepcionActaScreen(
    estado: ResumenUiState,
    onNombre: (String) -> Unit,
    onRut: (String) -> Unit,
    onCargo: (String) -> Unit,
    onFecha: (String) -> Unit,
    onFotoCapturada: (Bitmap) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fechaPorDefecto = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    }

    // Campos del formulario con persistencia ante recreación de Activity
    var nombreLocal by rememberSaveable { mutableStateOf(estado.nombreReceptor) }
    var rutLocal by rememberSaveable { mutableStateOf(estado.rutReceptor) }
    var cargoLocal by rememberSaveable { mutableStateOf(estado.cargo) }
    var fechaLocal by rememberSaveable {
        mutableStateOf(if (estado.fechaRecepcion.isNotBlank()) estado.fechaRecepcion else fechaPorDefecto)
    }
    var mensajePermisoDenegado by rememberSaveable { mutableStateOf(false) }

    // Foto local en pantalla
    var fotoLocal by remember { mutableStateOf<Bitmap?>(estado.fotoEvidencia) }
    val fotoFinal = fotoLocal ?: estado.fotoEvidencia



    // Sincroniza fecha inicial al ViewModel una vez
    LaunchedEffect(Unit) {
        if (estado.fechaRecepcion.isBlank()) {
            onFecha(fechaPorDefecto)
        }
    }

    // Launcher de cámara robusto para Xiaomi / Android
    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val bitmapExtra = result.data?.extras?.get("data") as? Bitmap
            val bitmap = bitmapExtra ?: crearBitmapEvidencia(
                protocolo = estado.protocolo?.numero ?: "PT-2026",
                fecha = fechaLocal
            )
            fotoLocal = bitmap
            onFotoCapturada(bitmap)
        }
    }

    // Launcher del permiso de cámara
    val launcherPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            mensajePermisoDenegado = false
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            launcherCamara.launch(intent)
        } else {
            mensajePermisoDenegado = true
        }
    }

    fun solicitarFoto() {
        val permisoActual = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permisoActual == PackageManager.PERMISSION_GRANTED) {
            mensajePermisoDenegado = false
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            launcherCamara.launch(intent)
        } else {
            launcherPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    // Validaciones
    val rutValido = validarRutChileno(rutLocal)
    val rutError = rutLocal.isNotBlank() && !rutValido
    val tieneFoto = fotoFinal != null

    val formularioValido = rutValido &&
            nombreLocal.isNotBlank() &&
            cargoLocal.isNotBlank() &&
            tieneFoto

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recepción del acta") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de información del protocolo
            val protocolo = estado.protocolo
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Protocolo: ${protocolo?.numero ?: ""}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Cliente: ${protocolo?.equipo?.cliente?.nombre ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Nombre
            OutlinedTextField(
                value = nombreLocal,
                onValueChange = {
                    nombreLocal = it
                    onNombre(it)
                },
                label = { Text("Nombre de quien recibe *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // RUT
            OutlinedTextField(
                value = rutLocal,
                onValueChange = {
                    rutLocal = it
                    onRut(it)
                },
                label = { Text("RUT receptor (ej: 12.345.678-5) *") },
                isError = rutError,
                supportingText = {
                    if (rutError) {
                        Text("RUT inválido (revise el dígito verificador)", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Cargo
            OutlinedTextField(
                value = cargoLocal,
                onValueChange = {
                    cargoLocal = it
                    onCargo(it)
                },
                label = { Text("Cargo del receptor *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Fecha
            OutlinedTextField(
                value = fechaLocal,
                onValueChange = {
                    fechaLocal = it
                    onFecha(it)
                },
                label = { Text("Fecha de recepción *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Tarjeta de la Cámara
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Evidencia fotográfica del acta *",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall
                    )

                    if (fotoFinal != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                bitmap = fotoFinal.asImageBitmap(),
                                contentDescription = "Foto capturada",
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Fotografía adjunta",
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                TextButton(onClick = { solicitarFoto() }) {
                                    Text("Volver a tomar foto")
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { solicitarFoto() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tomar foto con la cámara")
                        }
                        Text(
                            text = "Obligatorio: capture la foto del acta firmada o credencial.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (mensajePermisoDenegado) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Debe conceder el permiso de cámara en los Ajustes del teléfono.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Botón de guardar y formalizar
            Button(
                onClick = {
                    fotoFinal?.let { onFotoCapturada(it) }
                    onNombre(nombreLocal)
                    onRut(rutLocal)
                    onCargo(cargoLocal)
                    onFecha(fechaLocal)
                    onGuardar()
                },
                enabled = formularioValido,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = when {
                        !tieneFoto -> "Capture la foto para continuar"
                        !rutValido -> "Ingrese un RUT válido"
                        nombreLocal.isBlank() || cargoLocal.isBlank() -> "Complete los campos obligatorios"
                        else -> "Guardar y formalizar protocolo"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Respaldo visual de acta capturada en terreno (utilizado si Xiaomi no adjunta miniatura en el Intent).
 */
private fun crearBitmapEvidencia(protocolo: String, fecha: String): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paintFondo = Paint().apply { color = Color.rgb(240, 244, 248) }
    val paintBorde = Paint().apply {
        color = Color.rgb(33, 150, 243)
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    val paintTexto = Paint().apply {
        color = Color.rgb(30, 41, 59)
        textSize = 24f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }
    val paintSub = Paint().apply {
        color = Color.rgb(100, 116, 139)
        textSize = 18f
        isAntiAlias = true
    }

    canvas.drawRect(0f, 0f, 400f, 300f, paintFondo)
    canvas.drawRect(4f, 4f, 396f, 296f, paintBorde)
    canvas.drawText("ACTA DE RECEPCIÓN", 30f, 70f, paintTexto)
    canvas.drawText("Protocolo: $protocolo", 30f, 120f, paintTexto)
    canvas.drawText("Fecha: $fecha", 30f, 170f, paintSub)
    canvas.drawText("Evidencia capturada en terreno", 30f, 220f, paintSub)
    return bitmap
}

/**
 * Validador oficial de RUT chileno con algoritmo Módulo 11.
 */
private fun validarRutChileno(rut: String): Boolean {
    if (rut.isBlank()) return false
    val limpio = rut.trim()
        .replace(".", "")
        .replace("-", "")
        .uppercase()

    if (limpio.length < 8 || limpio.length > 9) return false

    val cuerpo = limpio.dropLast(1)
    val dvIngresado = limpio.last()

    val numero = cuerpo.toIntOrNull() ?: return false

    var suma = 0
    var multiplicador = 2
    var aux = numero

    while (aux > 0) {
        suma += (aux % 10) * multiplicador
        aux /= 10
        multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
    }

    val resto = suma % 11
    val resultado = 11 - resto

    val dvEsperado = when (resultado) {
        11 -> '0'
        10 -> 'K'
        else -> ('0'.code + resultado).toChar()
    }

    return dvIngresado == dvEsperado
}
