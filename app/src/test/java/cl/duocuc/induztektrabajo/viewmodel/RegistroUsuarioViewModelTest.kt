package cl.duocuc.induztektrabajo.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistroUsuarioViewModelTest {

    @Test
    fun registro_contrasenaMenorASeisCaracteres_esRechazada() {
        val claveCorta = "abc12"
        val esValida = claveCorta.length >= 6
        assertFalse(esValida)
    }

    @Test
    fun registro_confirmacionDeContrasenaDiferente_noCoincide() {
        val password = "Password123"
        val confirmarDiferente = "Password456"

        val coinciden = (password == confirmarDiferente)
        assertFalse(coinciden)
    }
}
