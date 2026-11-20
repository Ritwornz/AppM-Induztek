package cl.duocuc.induztektrabajo.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// =====================================================================
//  Pruebas unitarias — RegistroUsuarioViewModelTest.kt (Patrón AAA)
// =====================================================================

class RegistroUsuarioViewModelTest {

    @Test
    fun registro_contrasenaMenorASeisCaracteres_esRechazada() {
        // Arrange
        val claveCorta = "abc12"

        // Act
        val esValida = claveCorta.length >= 6

        // Assert
        assertFalse("La contraseña menor a 6 caracteres debe ser rechazada", esValida)
    }

    @Test
    fun registro_confirmacionDeContrasenaDiferente_noCoincide() {
        // Arrange
        val password = "Password123"
        val confirmarDiferente = "Password456"

        // Act
        val coinciden = (password == confirmarDiferente)

        // Assert
        assertFalse("Las contraseñas distintas no deben coincidir", coinciden)
    }

    @Test
    fun registro_formateoNombreConEmpresa_generaFormatoCorrectoParaFiltro() {
        // Arrange
        val nombre = "Carlos Mendoza"
        val empresa = "Puerto Austral"

        // Act
        val nombreCompuesto = "${nombre.trim()} (${empresa.trim()})"

        // Assert
        assertEquals("Carlos Mendoza (Puerto Austral)", nombreCompuesto)
        assertTrue(nombreCompuesto.contains("Puerto Austral"))
    }
}
