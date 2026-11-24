package cl.duocuc.induztektrabajo.viewmodel

import cl.duocuc.induztektrabajo.model.RegistroPrueba
import cl.duocuc.induztektrabajo.model.ResultadoPrueba
import cl.duocuc.induztektrabajo.model.TipoPrueba
import cl.duocuc.induztektrabajo.model.calcularErrorTtr
import cl.duocuc.induztektrabajo.model.calcularIP
import cl.duocuc.induztektrabajo.model.resultadoGlobal
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicionReglasTest {

    private fun reg(r: ResultadoPrueba) = RegistroPrueba(TipoPrueba.TTR, 10.0, 10.0, r, "x")

    @Test
    fun ipEsDiezMinutosSobreUnMinuto() {
        assertEquals(2.5, calcularIP(400.0, 1000.0)!!, 0.0001)
    }

    @Test
    fun errorTtrEnPorcentaje() {
        assertEquals(1.0, calcularErrorTtr(100.0, 101.0)!!, 0.0001)
    }

    @Test
    fun unaRechazadaDejaProtocoloRechazado() {
        val lista = listOf(reg(ResultadoPrueba.APROBADA), reg(ResultadoPrueba.RECHAZADA))
        assertEquals(ResultadoPrueba.RECHAZADA, resultadoGlobal(lista))
    }
}
