package cl.duocuc.induztektrabajo.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RutValidatorTest {

    @Test
    fun aceptaRutConPuntosYGuion() {
        assertTrue(RutValidator.validarRut("11.111.111-1"))
        assertTrue(RutValidator.validarRut("12.345.678-5"))
    }

    @Test
    fun aceptaRutSinFormato() {
        assertTrue(RutValidator.validarRut("111111111"))
        assertTrue(RutValidator.validarRut("123456785"))
    }

    @Test
    fun aceptaDigitoVerificadorK() {
        assertTrue(RutValidator.validarRut("71-K"))
        assertTrue(RutValidator.validarRut("71k"))
    }

    @Test
    fun rechazaDigitoVerificadorIncorrecto() {
        assertFalse(RutValidator.validarRut("11.111.111-2"))
        assertFalse(RutValidator.validarRut("12.345.678-9"))
    }

    @Test
    fun rechazaVacioOIncompleto() {
        assertFalse(RutValidator.validarRut(""))
        assertFalse(RutValidator.validarRut("   "))
        assertFalse(RutValidator.validarRut("1"))
        assertFalse(RutValidator.validarRut("ABC-K"))
    }
}
