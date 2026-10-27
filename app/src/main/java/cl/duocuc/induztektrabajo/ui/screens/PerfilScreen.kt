package cl.duocuc.induztektrabajo.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.unit.dp
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
    onCerrarSesion: () -> Unit = {}
) {
    val context = LocalContext.current
    var mostrarDialogoOrigen by remember { mutableStateOf(false) }

    // Archivo persistente en almacenamiento interno según el correo del usuario
    val archivoFoto = remember(correoUsuario) {
        val nombreArchivo = "foto_perfil_" + correoUsuario.replace("@", "_").replace(".", "_") + ".jpg"
        File(context.filesDir, nombreArchivo)
    }

    // Si ya existe foto en disco la carga; si no, queda null para mostrar la silueta default
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmapTomado ->
        if (bitmapTomado != null) {
            guardarBitmapEnDisco(bitmapTomado)
        }
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uriSeleccionada ->
        if (uriSeleccionada != null) {
            try {
                val bitmapGaleria = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uriSeleccionada)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uriSeleccionada)
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
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
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
            // Contenedor Avatar Circular
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
                    // SILUETA POR DEFECTO (Idéntica a la imagen requerida)
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Avatar por defecto",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color(0xFF1E1E1E)
                    )
                }

                // Pequeña insignia de cámara para indicar que se puede cambiar
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
            Text(
                text = "Toca para cambiar foto",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = nombreUsuario,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = rolUsuario,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ItemDatoPerfil(
                        icono = Icons.Default.Email,
                        titulo = "Correo Corporativo",
                        valor = correoUsuario
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ItemDatoPerfil(
                        icono = Icons.Default.Badge,
                        titulo = "Rol en el Sistema",
                        valor = rolUsuario
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ItemDatoPerfil(
                        icono = Icons.Default.Business,
                        titulo = "Empresa / Organización",
                        valor = empresaUsuario
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cerrar Sesión", color = MaterialTheme.colorScheme.onError)
            }
        }
    }

    if (mostrarDialogoOrigen) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoOrigen = false },
            title = { Text("Actualizar foto de perfil") },
            text = { Text("Selecciona el origen para actualizar tu foto de perfil:") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoOrigen = false
                        lanzadorCamara.launch(null)
                    }
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cámara")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoOrigen = false
                        lanzadorGaleria.launch("image/*")
                    }
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Galería")
                }
            }
        )
    }
}

@Composable
private fun ItemDatoPerfil(
    icono: ImageVector,
    titulo: String,
    valor: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
