package cl.duocuc.induztektrabajo.model

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val mostrarContrasena: Boolean = false,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null
)
