package cl.duocuc.induztektrabajo.viewmodel

import cl.duocuc.induztektrabajo.model.LoginValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// =====================================================================
//  Pruebas unitarias — LoginViewModelTest.kt (Patrón AAA)
// =====================================================================

class LoginViewModelTest {

    @Test
    fun validar_correoCorporativoValido_devuelveTrue() {
        // Arrange
        val correo = "tecnico@induztek.cl"

        // Act
        val resultado = LoginValidator.correoEsValido(correo)

        // Assert
        assertTrue(resultado)
    }

    @Test
    fun validar_correoSinArrobaOSinDominio_devuelveFalse() {
        // Arrange
        val sinArroba = "tecnicoinduztek.cl"
        val sinPunto = "tecnico@induztek"

        // Act & Assert
        assertFalse(LoginValidator.correoEsValido(sinArroba))
        assertFalse(LoginValidator.correoEsValido(sinPunto))
    }

    @Test
    fun validar_contrasenaMinimaSeisCaracteres_cumpleRegla() {
        // Arrange
        val claveValida = "123456"
        val claveCorta = "12345"

        // Act & Assert
        assertTrue(LoginValidator.contrasenaEsValida(claveValida))
        assertFalse(LoginValidator.contrasenaEsValida(claveCorta))
    }
}
