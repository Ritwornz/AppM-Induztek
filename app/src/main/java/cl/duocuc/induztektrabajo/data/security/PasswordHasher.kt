package cl.duocuc.induztektrabajo.data.security

import java.security.MessageDigest

object PasswordHasher {

    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verificarPassword(passwordPlana: String, hashGuardado: String): Boolean {
        val hashIngresado = hashPassword(passwordPlana)
        return hashIngresado.equals(hashGuardado, ignoreCase = true)
    }
}