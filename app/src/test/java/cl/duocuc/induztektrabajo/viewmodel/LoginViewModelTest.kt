package cl.duocuc.induztektrabajo.viewmodel

import cl.duocuc.induztektrabajo.model.LoginValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class LoginViewModelTest {

    @Test
    fun validar_correoCorporativoValido_devuelveTrue() {

        val correo = "tecnico@induztek.cl"

        val resultado = LoginValidator.correoEsValido(correo)

        assertTrue(resultado)
    }

    @Test
    fun validar_correoSinArrobaOSinDominio_devuelveFalse() {
        val sinArroba = "tecnicoinduztek.cl"
        val sinPunto = "tecnico@induztek"

        assertFalse(LoginValidator.correoEsValido(sinArroba))
        assertFalse(LoginValidator.correoEsValido(sinPunto))
    }

    @Test
    fun validar_contrasenaMinimaSeisCaracteres_cumpleRegla() {
        val claveValida = "123456"
        val claveCorta = "12345"

        assertTrue(LoginValidator.contrasenaEsValida(claveValida))
        assertFalse(LoginValidator.contrasenaEsValida(claveCorta))
    }
}
