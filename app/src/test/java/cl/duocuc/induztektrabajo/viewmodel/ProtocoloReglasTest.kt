package cl.duocuc.induztektrabajo.viewmodel

import cl.duocuc.induztektrabajo.model.RepositorioFicticio
import cl.duocuc.induztektrabajo.model.filtrarProtocolos
import cl.duocuc.induztektrabajo.model.siguienteNumeroProtocolo
import org.junit.Assert.assertEquals
import org.junit.Test

class ProtocoloReglasTest {

    private val lista = RepositorioFicticio.protocolos

    @Test
    fun buscarPorTagIgnoraMayusculas() {
        val r = filtrarProtocolos(lista, "tr-01")
        assertEquals(1, r.size)
        assertEquals("TR-01", r[0].equipo.tag)
    }

    @Test
    fun buscarVacioDevuelveTodos() {
        assertEquals(lista.size, filtrarProtocolos(lista, "  ").size)
    }

    @Test
    fun siguienteNumeroUsaFormatoPT() {
        assertEquals("PT-2026-0005", siguienteNumeroProtocolo(lista, 2026))
        assertEquals("PT-2027-0001", siguienteNumeroProtocolo(lista, 2027))
    }
}
