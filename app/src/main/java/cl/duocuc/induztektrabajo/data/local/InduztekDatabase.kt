package cl.duocuc.induztektrabajo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duocuc.induztektrabajo.data.local.dao.MedicionDao
import cl.duocuc.induztektrabajo.data.local.dao.ProtocoloDao
import cl.duocuc.induztektrabajo.data.local.dao.UsuarioDao
import cl.duocuc.induztektrabajo.data.local.entity.MedicionEntity
import cl.duocuc.induztektrabajo.data.local.entity.ProtocoloEntity
import cl.duocuc.induztektrabajo.data.local.entity.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UsuarioEntity::class, ProtocoloEntity::class, MedicionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class InduztekDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun protocoloDao(): ProtocoloDao
    abstract fun medicionDao(): MedicionDao

    companion object {
        @Volatile
        private var INSTANCE: InduztekDatabase? = null

        fun getDatabase(context: Context): InduztekDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InduztekDatabase::class.java,
                    "induztek_database"
                )
                    .addCallback(InduztekDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class InduztekDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        poblarDatosIniciales(database)
                    }
                }
            }

            private suspend fun poblarDatosIniciales(database: InduztekDatabase) {
                val usuarioDao = database.usuarioDao()
                val protocoloDao = database.protocoloDao()
                val medicionDao = database.medicionDao()

                usuarioDao.insertarUsuario(
                    UsuarioEntity(
                        correo = "carlos.sanchez@induztek.cl",
                        passwordHash = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                        nombreCompleto = "Carlos Sánchez",
                        rol = "TECNICO"
                    )
                )

                usuarioDao.insertarUsuario(
                    UsuarioEntity(
                        correo = "cliente@minera.cl",
                        passwordHash = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                        nombreCompleto = "Juan Pérez (Minera Escondida)",
                        rol = "CLIENTE"
                    )
                )

                // Protocolos de ejemplo
                protocoloDao.insertarProtocolo(
                    ProtocoloEntity(
                        numero = "PT-2026-0001",
                        tagEquipo = "TR-SUBESTACION-01",
                        clienteNombre = "Minera Escondida",
                        estado = "REGISTRADO_TERRENO",
                        pruebasAprobadas = 3,
                        pruebasTotales = 3,
                        latitud = -33.4489,
                        longitud = -70.6693
                    )
                )

                protocoloDao.insertarProtocolo(
                    ProtocoloEntity(
                        numero = "PT-2026-0002",
                        tagEquipo = "MQ-EXTRACCION-04",
                        clienteNombre = "Codelco Ventanas",
                        estado = "OBSERVADO",
                        pruebasAprobadas = 1,
                        pruebasTotales = 2,
                        latitud = -32.7542,
                        longitud = -71.4851
                    )
                )

                medicionDao.insertarMedicion(
                    MedicionEntity(
                        protocoloNumero = "PT-2026-0001",
                        tipoPrueba = "AISLAMIENTO",
                        valorA = 5000.0,
                        valorB = 2400.0,
                        resultado = "APROBADA",
                        motivo = "Índice de polarización conforme (IP 2.08)"
                    )
                )
                medicionDao.insertarMedicion(
                    MedicionEntity(
                        protocoloNumero = "PT-2026-0001",
                        tipoPrueba = "RELACION_TRANSFORMACION",
                        valorA = 100.0,
                        valorB = 100.2,
                        resultado = "APROBADA",
                        motivo = "Desviación TTR 0.20% dentro de norma (< 0.5%)"
                    )
                )
                medicionDao.insertarMedicion(
                    MedicionEntity(
                        protocoloNumero = "PT-2026-0001",
                        tipoPrueba = "AISLAMIENTO",
                        valorA = 4800.0,
                        valorB = 2200.0,
                        resultado = "APROBADA",
                        motivo = "Aislamiento secundario conforme (IP 2.18)"
                    )
                )

                // Mediciones precargadas para PT-2026-0002 (1 aprobada, 1 observada)
                medicionDao.insertarMedicion(
                    MedicionEntity(
                        protocoloNumero = "PT-2026-0002",
                        tipoPrueba = "AISLAMIENTO",
                        valorA = 3200.0,
                        valorB = 2100.0,
                        resultado = "APROBADA",
                        motivo = "Aislamiento fase-tierra en rango aceptable"
                    )
                )
                medicionDao.insertarMedicion(
                    MedicionEntity(
                        protocoloNumero = "PT-2026-0002",
                        tipoPrueba = "RELACION_TRANSFORMACION",
                        valorA = 100.0,
                        valorB = 98.2,
                        resultado = "OBSERVADA",
                        motivo = "Error TTR 1.80% supera la tolerancia de 0.5%"
                    )
                )
            }
        }
    }
}
