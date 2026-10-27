package cl.duocuc.induztektrabajo.model

object LoginValidator {
    fun correoEsValido(correo: String): Boolean =
        correo.contains("@") && correo.substringAfter("@", "").contains(".")

    fun contrasenaEsValida(contrasena: String): Boolean = contrasena.length >= 6
}