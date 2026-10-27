package cl.duocuc.induztektrabajo.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

/**
 * Ubicación sin Google Play Services, con LocationManager.
 * La comprobación de permiso sigue el mismo criterio que Permisos.kt
 * del curso: ContextCompat + PackageManager, y la solicitud ocurre
 * en tiempo de ejecución desde la pantalla.
 */
object Ubicacion {

    const val PERMISO_FINO = Manifest.permission.ACCESS_FINE_LOCATION

    fun permisoConcedido(contexto: Context): Boolean =
        ContextCompat.checkSelfPermission(contexto, PERMISO_FINO) ==
            PackageManager.PERMISSION_GRANTED

    fun ultimaConocida(contexto: Context): Location? {
        if (!permisoConcedido(contexto)) return null
        val gestor = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val proveedores = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        return proveedores.mapNotNull { proveedor ->
            try {
                gestor.getLastKnownLocation(proveedor)
            } catch (_: SecurityException) {
                null
            }
        }.maxByOrNull { it.time }
    }
}
