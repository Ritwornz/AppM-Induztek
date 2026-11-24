package cl.duocuc.induztektrabajo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duocuc.induztektrabajo.R
import androidx.compose.material3.Surface
import cl.duocuc.induztektrabajo.viewmodel.LoginUiState

@Composable
fun LoginScreen(
    estado: LoginUiState,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onAlternarContrasena: () -> Unit,
    onIngresar: () -> Unit,
    onRecuperarContrasena: () -> Unit,
    onIrARegistro: () -> Unit = {},
    onConfirmarRecuperacion: (correo: String, nuevaClave: String, onResultado: (Boolean, String?) -> Unit) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var mostrarDialogoRecuperacion by remember { mutableStateOf(false) }
    var correoRecuperacion by remember { mutableStateOf("") }
    var codigoGenerado by remember { mutableStateOf("") }
    var codigoIngresado by remember { mutableStateOf("") }
    var nuevaClaveRecuperacion by remember { mutableStateOf("") }
    var confirmarClaveRecuperacion by remember { mutableStateOf("") }
    var pasoRecuperacion by remember { mutableStateOf(1) }
    var mensajeFeedback by remember { mutableStateOf<String?>(null) }



    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MarcaInduztek()
            Spacer(Modifier.height(34.dp))

            Text(
                text = "Consorcio de Operaciones",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Ingresa tus credenciales para acceder",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = estado.correo,
                onValueChange = onCorreoCambia,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Correo corporativo") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_correo),
                        contentDescription = null
                    )
                },
                singleLine = true,
                isError = estado.errorCorreo != null,
                supportingText = {
                    estado.errorCorreo?.let { Text(it) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(6.dp)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = estado.contrasena,
                onValueChange = onContrasenaCambia,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_candado),
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onAlternarContrasena) {
                        Icon(
                            painter = painterResource(
                                if (estado.mostrarContrasena) R.drawable.ic_ojo_tachado
                                else R.drawable.ic_ojo
                            ),
                            contentDescription = if (estado.mostrarContrasena) {
                                "Ocultar contraseña"
                            } else {
                                "Mostrar contraseña"
                            }
                        )
                    }
                },
                visualTransformation = if (estado.mostrarContrasena) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                singleLine = true,
                isError = estado.errorContrasena != null,
                supportingText = {
                    estado.errorContrasena?.let { Text(it) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(6.dp)
            )

            TextButton(
                onClick = {
                    correoRecuperacion = estado.correo
                    mensajeFeedback = null
                    mostrarDialogoRecuperacion = true
                    onRecuperarContrasena()
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("¿Olvidaste tu contraseña?")
            }

            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(
                visible = estado.errorGeneral != null,
                enter = fadeIn(animationSpec = tween(300)) + slideInVertically { -it / 2 },
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically { -it / 2 }
            ) {
                estado.errorGeneral?.let { mensajeError ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = mensajeError,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onIngresar,
                enabled = !estado.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .animateContentSize(),
                shape = RoundedCornerShape(6.dp)
            ) {
                if (estado.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("INGRESAR")
                }
            }
            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = onIrARegistro
            ) {
                Text(
                    text = "¿No tienes cuenta? Regístrate aquí",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (mostrarDialogoRecuperacion) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoRecuperacion = false
                pasoRecuperacion = 1
                codigoIngresado = ""
                nuevaClaveRecuperacion = ""
                confirmarClaveRecuperacion = ""
                mensajeFeedback = null
            },
            title = {
                Text(
                    when (pasoRecuperacion) {
                        1 -> "Recuperar contraseña"
                        2 -> "Verificación de seguridad"
                        else -> "Nueva contraseña"
                    }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (pasoRecuperacion) {
                        1 -> {
                            Text(
                                "Ingresa tu correo institucional registrado para validar tu identidad:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = correoRecuperacion,
                                onValueChange = {
                                    correoRecuperacion = it
                                    mensajeFeedback = null
                                },
                                label = { Text("Correo institucional") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                        2 -> {
                            Text(
                                "Hemos generado un código de seguridad para tu cuenta ($correoRecuperacion):",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Código de seguridad OTP:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = codigoGenerado,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Simulador corporativo Induztek",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = codigoIngresado,
                                onValueChange = {
                                    if (it.length <= 6) codigoIngresado = it
                                    mensajeFeedback = null
                                },
                                label = { Text("Ingresa los 6 dígitos") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                        3 -> {
                            Text(
                                "Identidad verificada exitosamente. Ingresa tu nueva contraseña para $correoRecuperacion:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = nuevaClaveRecuperacion,
                                onValueChange = { nuevaClaveRecuperacion = it },
                                label = { Text("Nueva contraseña") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp)
                            )
                            OutlinedTextField(
                                value = confirmarClaveRecuperacion,
                                onValueChange = { confirmarClaveRecuperacion = it },
                                label = { Text("Confirmar nueva contraseña") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }

                    if (mensajeFeedback != null) {
                        Text(
                            text = mensajeFeedback!!,
                            color = if (mensajeFeedback!!.startsWith("Error") || mensajeFeedback!!.startsWith("No existe")) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (pasoRecuperacion) {
                            1 -> {
                                if (correoRecuperacion.isBlank() || !correoRecuperacion.contains("@")) {
                                    mensajeFeedback = "Error: Ingrese un correo electrónico válido."
                                } else {
                                    val randomOtp = (100000..999999).random().toString()
                                    codigoGenerado = randomOtp
                                    codigoIngresado = ""
                                    mensajeFeedback = null
                                    pasoRecuperacion = 2
                                }
                            }
                            2 -> {
                                if (codigoIngresado.trim() != codigoGenerado) {
                                    mensajeFeedback = "Error: Código de seguridad incorrecto."
                                } else {
                                    mensajeFeedback = null
                                    pasoRecuperacion = 3
                                }
                            }
                            3 -> {
                                if (nuevaClaveRecuperacion.length < 6) {
                                    mensajeFeedback = "Error: La contraseña debe tener al menos 6 caracteres."
                                } else if (nuevaClaveRecuperacion != confirmarClaveRecuperacion) {
                                    mensajeFeedback = "Error: Las contraseñas no coinciden."
                                } else {
                                    onConfirmarRecuperacion(correoRecuperacion, nuevaClaveRecuperacion) { exito, error ->
                                        if (exito) {
                                            mostrarDialogoRecuperacion = false
                                            pasoRecuperacion = 1
                                            codigoIngresado = ""
                                            nuevaClaveRecuperacion = ""
                                            confirmarClaveRecuperacion = ""
                                        } else {
                                            mensajeFeedback = error ?: "Error al actualizar contraseña"
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text(
                        when (pasoRecuperacion) {
                            1 -> "Enviar código"
                            2 -> "Verificar código"
                            else -> "Restablecer"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoRecuperacion = false
                        pasoRecuperacion = 1
                        codigoIngresado = ""
                        mensajeFeedback = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

}

@Composable
private fun MarcaInduztek() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ϟ",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "INDUZTEK",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "INGENIERÍA",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
