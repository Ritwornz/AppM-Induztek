package cl.duocuc.induztektrabajo.model

data class RecepcionActa(
    val nombreReceptor: String,
    val rutReceptor: String,
    val cargo: String,
    val fecha: String
)

object RutValidator {
    fun validarRut(rutCompleto: String): Boolean {
        if (rutCompleto.isBlank()) return false
        val limpio = rutCompleto.replace(".", "").replace("-", "").trim().uppercase()
        if (limpio.length < 2) return false

        val cuerpo = limpio.dropLast(1)
        val dv = limpio.takeLast(1)

        if (!cuerpo.all { it.isDigit() }) return false

        var suma = 0
        var multiplicador = 2
        for (i in cuerpo.length - 1 downTo 0) {
            suma += cuerpo[i].toString().toInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }

        val resto = suma % 11
        val dvCalculado = when (resto) {
            0 -> "0"
            1 -> "K"
            else -> (11 - resto).toString()
        }

        return dv == dvCalculado
    }
}

fun validarRecepcion(
    nombre: String,
    rut: String,
    cargo: String,
    fecha: String
): String? = when {
    nombre.isBlank() -> "El nombre del receptor es obligatorio"
    rut.isBlank() -> "El RUT es obligatorio"
    cargo.isBlank() -> "El cargo es obligatorio"
    fecha.isBlank() -> "La fecha es obligatoria"
    !RutValidator.validarRut(rut) -> "Ingresa un RUT chileno válido (módulo 11)"
    else -> null
}

fun EstadoProtocolo.puedeEditarPruebas(): Boolean =
    this == EstadoProtocolo.BORRADOR ||
        this == EstadoProtocolo.REGISTRADO_TERRENO ||
        this == EstadoProtocolo.OBSERVADO
