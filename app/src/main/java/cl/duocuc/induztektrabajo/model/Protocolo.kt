package cl.duocuc.induztektrabajo.model

enum class EstadoProtocolo(val etiqueta: String) {
    BORRADOR("Borrador"),
    REGISTRADO_TERRENO("Registrado en terreno"),
    OBSERVADO("Observado"),
    FORMALIZADO("Formalizado"),
    ANULADO("Anulado")
}

enum class TipoEquipo(val etiqueta: String) {
    TRANSFORMADOR("Transformador de poder"),
    MOTOR("Motor de alta tensión"),
    GENERADOR("Generador"),
    TABLERO("Tablero eléctrico")
}

data class Cliente(
    val id: Int,
    val nombre: String,
    val instalacion: String
)

data class Equipo(
    val id: Int,
    val tag: String,
    val tipo: TipoEquipo,
    val cliente: Cliente
)

data class ProtocoloResumen(
    val numero: String,          // Formato PT-YYYY-XXXX
    val equipo: Equipo,
    val fecha: String,
    val estado: EstadoProtocolo,
    val pruebasAprobadas: Int,
    val pruebasTotales: Int
)
fun filtrarProtocolos(lista: List<ProtocoloResumen>, texto: String): List<ProtocoloResumen> {
    val q = texto.trim()
    if (q.isEmpty()) return lista
    return lista.filter {
        it.equipo.tag.contains(q, ignoreCase = true) ||
                it.equipo.cliente.nombre.contains(q, ignoreCase = true) ||
                it.numero.contains(q, ignoreCase = true)
    }
}

fun siguienteNumeroProtocolo(lista: List<ProtocoloResumen>, anio: Int): String {
    val prefijo = "PT-$anio-"
    val mayor = lista.mapNotNull {
        if (it.numero.startsWith(prefijo)) it.numero.removePrefix(prefijo).toIntOrNull() else null
    }.maxOrNull() ?: 0
    return prefijo + (mayor + 1).toString().padStart(4, '0')
}