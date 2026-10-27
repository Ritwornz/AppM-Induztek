package cl.duocuc.induztektrabajo.util

object RutValidator {
    /**
     * Valida un RUT chileno aplicando el algoritmo oficial de Módulo 11.
     * Acepta formatos: "12345678-5", "12.345.678-5", "12345678-K", etc.
     */
    fun validar(rut: String): Boolean {
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
}
