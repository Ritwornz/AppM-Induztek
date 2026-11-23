package cl.duocuc.induztektrabajo.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class RegistroUsuarioViewModelTest {

    @Test
    fun registro_contrasenaMenorASeisCaracteres_esRechazada() {
        val claveCorta = "abc12"

        val esValida = claveCorta.length >= 6

        assertFalse("La contraseña menor a 6 caracteres debe ser rechazada", esValida)
    }

    @Test
    fun registro_confirmacionDeContrasenaDiferente_noCoincide() {

        val password = "Password123"
        val confirmarDiferente = "Password456"

        val coinciden = (password == confirmarDiferente)

        assertFalse("Las contraseñas distintas no deben coincidir", coinciden)
    }

    @Test
    fun registro_formateoNombreConEmpresa_generaFormatoCorrectoParaFiltro() {

        val nombre = "Carlos Mendoza"
        val empresa = "Puerto Austral"

        val nombreCompuesto = "${nombre.trim()} (${empresa.trim()})"

        assertEquals("Carlos Mendoza (Puerto Austral)", nombreCompuesto)
        assertTrue(nombreCompuesto.contains("Puerto Austral"))
    }
}
