package cl.duocuc.induztektrabajo.model

// Pruebas eléctricas del Paso 4 (datos ficticios, en memoria)
enum class TipoPrueba(val etiqueta: String, val descripcion: String) {
    AISLAMIENTO("Resistencia de aislamiento (IP)", "Lecturas a 1 y 10 minutos, en MΩ"),
    TTR("Relación de transformación (TTR)", "Relación esperada y medida")
}

enum class ResultadoPrueba(val etiqueta: String) {
    APROBADA("Aprobada"),
    OBSERVADA("Observada"),
    RECHAZADA("Rechazada")
}

data class RegistroPrueba(
    val tipo: TipoPrueba,
    val valorA: Double,      // IP: lectura 1 min  | TTR: relación esperada
    val valorB: Double,      // IP: lectura 10 min | TTR: relación medida
    val resultado: ResultadoPrueba,
    val motivo: String = ""
)

// Índice de polarización = lectura 10 min / lectura 1 min
fun calcularIP(lectura1Min: Double, lectura10Min: Double): Double? =
    if (lectura1Min > 0) lectura10Min / lectura1Min else null

// % de error TTR = |medida - esperada| / esperada * 100
fun calcularErrorTtr(esperada: Double, medida: Double): Double? =
    if (esperada > 0) kotlin.math.abs(medida - esperada) / esperada * 100 else null

// Una prueba rechazada => protocolo rechazado; si hay observada => observado
fun resultadoGlobal(registros: List<RegistroPrueba>): ResultadoPrueba? = when {
    registros.isEmpty() -> null
    registros.any { it.resultado == ResultadoPrueba.RECHAZADA } -> ResultadoPrueba.RECHAZADA
    registros.any { it.resultado == ResultadoPrueba.OBSERVADA } -> ResultadoPrueba.OBSERVADA
    else -> ResultadoPrueba.APROBADA
}

// Devuelve el error a mostrar, o null si el registro es válido
fun validarRegistro(
    valorA: String,
    valorB: String,
    resultado: ResultadoPrueba?,
    motivo: String
): String? {
    val a = valorA.replace(',', '.').toDoubleOrNull()
    val b = valorB.replace(',', '.').toDoubleOrNull()
    return when {
        a == null || b == null -> "Ingresa ambos valores numéricos"
        a <= 0 || b <= 0 -> "Los valores deben ser mayores que 0"
        resultado == null -> "Selecciona el resultado de la prueba"
        resultado != ResultadoPrueba.APROBADA && motivo.isBlank() ->
            "El motivo es obligatorio si la prueba es Observada o Rechazada"
        else -> null
    }
}