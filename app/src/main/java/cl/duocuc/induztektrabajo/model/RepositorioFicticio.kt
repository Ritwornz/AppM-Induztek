package cl.duocuc.induztektrabajo.model
object RepositorioFicticio {

    private val minera = Cliente(1, "Minera Andes Norte", "Planta Concentradora")
    private val celulosa = Cliente(2, "Celulosa del Sur", "Subestación Principal")
    private val puerto = Cliente(3, "Puerto Austral", "Sala Eléctrica 2")

    val equipos = listOf(
        Equipo(1, "TR-01", TipoEquipo.TRANSFORMADOR, minera),
        Equipo(2, "MOT-204", TipoEquipo.MOTOR, minera),
        Equipo(3, "TR-07", TipoEquipo.TRANSFORMADOR, celulosa),
        Equipo(4, "GEN-02", TipoEquipo.GENERADOR, celulosa),
        Equipo(5, "TAB-11", TipoEquipo.TABLERO, puerto)
    )

    val protocolos = listOf(
        ProtocoloResumen("PT-2026-0004", equipos[0], "12/10/2026", EstadoProtocolo.BORRADOR, 1, 3),
        ProtocoloResumen("PT-2026-0003", equipos[2], "10/10/2026", EstadoProtocolo.REGISTRADO_TERRENO, 3, 3),
        ProtocoloResumen("PT-2026-0002", equipos[1], "08/10/2026", EstadoProtocolo.OBSERVADO, 2, 3),
        ProtocoloResumen("PT-2026-0001", equipos[3], "02/10/2026", EstadoProtocolo.FORMALIZADO, 3, 3)
    )
}
