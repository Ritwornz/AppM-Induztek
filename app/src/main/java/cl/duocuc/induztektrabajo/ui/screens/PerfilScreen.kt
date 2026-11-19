package cl.duocuc.induztektrabajo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    nombreUsuario: String = "Carlos Sánchez",
    correoUsuario: String = "carlos.sanchez@induztek.cl",
    rolUsuario: String = "Técnico de terreno",
    empresaUsuario: String = "Induztek SpA",
    onVolver: () -> Unit,
    onCerrarSesion: () -> Unit = {},
    onGuardarNombre: (String) -> Unit = {},
    onCambiarPassword: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var mostrarDialogoOrigen by remember { mutableStateOf(false) }
    var mostrarDialogoNombre by remember { mutableStateOf(false) }
    var mostrarDialogoPassword by remember { mutableStateOf(false) }

    var nombreEstado by remember(nombreUsuario) { mutableStateOf(nombreUsuario) }
    var campoNuevoNombre by remember { mutableStateOf("") }
    var campoNuevaClave by remember { mutableStateOf("") }
    var campoConfirmarClave by remember { mutableStateOf("") }

    // Archivo de foto persistente según correo
    val archivoFoto = remember(correoUsuario) {
        val nombreArchivo = "foto_perfil_" + correoUsuario.replace("@", "_").replace(".", "_") + ".jpg"
        File(context.filesDir, nombreArchivo)
    }

    var fotoBitmap by remember(correoUsuario) {
        mutableStateOf(
            if (archivoFoto.exists()) {
                BitmapFactory.decodeFile(archivoFoto.absolutePath)
            } else null
        )
    }

    fun guardarBitmapEnDisco(bitmap: Bitmap) {
        try {
            FileOutputStream(archivoFoto).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
            }
            fotoBitmap = bitmap
            Toast.makeText(context, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al guardar foto", Toast.LENGTH_SHORT).show()
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) guardarBitmapEnDisco(bitmap)
    }

    val lanzadorPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            lanzadorCamara.launch(null)
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara para tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    fun solicitarTomarFoto() {
        val permiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            lanzadorCamara.launch(null)
        } else {
            lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmapGaleria = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                guardarBitmapEnDisco(bitmapGaleria)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Circular con botón de cámara
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .clickable { mostrarDialogoOrigen = true },
                contentAlignment = Alignment.Center
            ) {
                if (fotoBitmap != null) {
                    Image(
                        bitmap = fotoBitmap!!.asImageBitmap(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Avatar por defecto",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color(0xFF1E1E1E)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Toca para cambiar foto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = nombreEstado, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = rolUsuario, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta de Datos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ItemDatoPerfil(icono = Icons.Default.Email, titulo = "Correo Corporativo", valor = correoUsuario)
                    Spacer(modifier = Modifier.height(14.dp))
                    ItemDatoPerfil(icono = Icons.Default.Badge, titulo = "Rol en el Sistema", valor = rolUsuario)
                    Spacer(modifier = Modifier.height(14.dp))
                    ItemDatoPerfil(icono = Icons.Default.Business, titulo = "Empresa / Organización", valor = empresaUsuario)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de Personalización
            OutlinedButton(
                onClick = {
                    campoNuevoNombre = nombreEstado
                    mostrarDialogoNombre = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editar Nombre")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    campoNuevaClave = ""
                    campoConfirmarClave = ""
                    mostrarDialogoPassword = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cambiar Contraseña")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cerrar Sesión", color = MaterialTheme.colorScheme.onError)
            }
        }
    }

    // Diálogo Origen Foto
    if (mostrarDialogoOrigen) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoOrigen = false },
            title = { Text("Actualizar foto de perfil") },
            text = { Text("Selecciona el origen:") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoOrigen = false
                    solicitarTomarFoto()
                }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cámara")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoOrigen = false
                    lanzadorGaleria.launch("image/*")
                }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Galería")
                }
            }
        )
    }

    // Diálogo Editar Nombre
    if (mostrarDialogoNombre) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoNombre = false },
            title = { Text("Editar Nombre Completo") },
            text = {
                Column {
                    Text("Ingresa tu nombre para mostrar en los protocolos:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = campoNuevoNombre,
                        onValueChange = { campoNuevoNombre = it },
                        singleLine = true,
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limpio = campoNuevoNombre.trim()
                        if (limpio.length >= 3) {
                            nombreEstado = limpio
                            onGuardarNombre(limpio)
                            mostrarDialogoNombre = false
                            Toast.makeText(context, "Nombre actualizado con éxito", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNombre = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo Cambiar Contraseña
    if (mostrarDialogoPassword) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPassword = false },
            title = { Text("Cambiar Contraseña") },
            text = {
                Column {
                    OutlinedTextField(
                        value = campoNuevaClave,
                        onValueChange = { campoNuevaClave = it },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("Nueva contraseña") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = campoConfirmarClave,
                        onValueChange = { campoConfirmarClave = it },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("Confirmar contraseña") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (campoNuevaClave.length < 6) {
                            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                        } else if (campoNuevaClave != campoConfirmarClave) {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                        } else {
                            onCambiarPassword(campoNuevaClave)
                            mostrarDialogoPassword = false
                            Toast.makeText(context, "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Actualizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPassword = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ItemDatoPerfil(icono: ImageVector, titulo: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(imageVector = icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text(text = valor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}
