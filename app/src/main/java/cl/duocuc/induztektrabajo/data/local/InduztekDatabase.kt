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

                val hashComun = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92" // clave: 123456

                val usuarios = listOf(
                    UsuarioEntity(
                        correo = "carlos.sanchez@induztek.cl",
                        passwordHash = hashComun,
                        nombreCompleto = "Carlos Sánchez",
                        rol = "TECNICO"
                    ),
                    UsuarioEntity(
                        correo = "admin@induztek.cl",
                        passwordHash = hashComun,
                        nombreCompleto = "Administrador Induztek",
                        rol = "ADMINISTRADOR"
                    ),
                    UsuarioEntity(
                        correo = "cliente.minera@minera.cl",
                        passwordHash = hashComun,
                        nombreCompleto = "Juan Pérez (Minera Andes Norte)",
                        rol = "CLIENTE"
                    ),
                    UsuarioEntity(
                        correo = "cliente.celulosa@celulosa.cl",
                        passwordHash = hashComun,
                        nombreCompleto = "Roberto Díaz (Celulosa del Sur)",
                        rol = "CLIENTE"
                    ),
                    UsuarioEntity(
                        correo = "cliente.puerto@puertoaustral.cl",
                        passwordHash = hashComun,
                        nombreCompleto = "Pedro Morales (Puerto Austral)",
                        rol = "CLIENTE"
                    )
                )
                usuarios.forEach { usuarioDao.insertarUsuario(it) }

                val protocolos = listOf(
                    ProtocoloEntity(
                        numero = "PT-2026-0001",
                        tagEquipo = "TR-01",
                        clienteNombre = "Minera Andes Norte",
                        estado = "REGISTRADO_TERRENO",
                        pruebasAprobadas = 1,
                        pruebasTotales = 2,
                        latitud = -23.8634,
                        longitud = -69.1328
                    ),
                    ProtocoloEntity(
                        numero = "PT-2026-0002",
                        tagEquipo = "TR-07",
                        clienteNombre = "Celulosa del Sur",
                        estado = "FORMALIZADO",
                        pruebasAprobadas = 2,
                        pruebasTotales = 2,
                        latitud = -36.7842,
                        longitud = -72.9814
                    ),
                    ProtocoloEntity(
                        numero = "PT-2026-0003",
                        tagEquipo = "MOT-204",
                        clienteNombre = "Minera Andes Norte",
                        estado = "BORRADOR",
                        pruebasAprobadas = 0,
                        pruebasTotales = 2,
                        latitud = -23.8640,
                        longitud = -69.1335
                    ),
                    ProtocoloEntity(
                        numero = "PT-2026-0004",
                        tagEquipo = "GEN-02",
                        clienteNombre = "Celulosa del Sur",
                        estado = "REGISTRADO_TERRENO",
                        pruebasAprobadas = 1,
                        pruebasTotales = 2,
                        latitud = -36.7850,
                        longitud = -72.9820
                    ),
                    ProtocoloEntity(
                        numero = "PT-2026-0005",
                        tagEquipo = "TAB-11",
                        clienteNombre = "Puerto Austral",
                        estado = "REGISTRADO_TERRENO",
                        pruebasAprobadas = 1,
                        pruebasTotales = 2,
                        latitud = -53.1638,
                        longitud = -70.9171
                    )
                )
                protocolos.forEach { protocoloDao.insertarProtocolo(it) }

                val mediciones = listOf(
                    MedicionEntity(
                        protocoloNumero = "PT-2026-0001",
                        tipoPrueba = "AISLAMIENTO_IP",
                        valorA = 120.0,
                        valorB = 250.0,
                        resultado = "APROBADA",
                        motivo = "Índice de polarización conforme a norma IEEE 43"
                    ),

                    MedicionEntity(
                        protocoloNumero = "PT-2026-0002",
                        tipoPrueba = "AISLAMIENTO_IP",
                        valorA = 180.0,
                        valorB = 400.0,
                        resultado = "APROBADA",
                        motivo = "Aislamiento en devanados dentro de rango óptimo"
                    ),
                    MedicionEntity(
                        protocoloNumero = "PT-2026-0002",
                        tipoPrueba = "RELACION_TTR",
                        valorA = 13800.0,
                        valorB = 400.0,
                        resultado = "APROBADA",
                        motivo = "Desviación de relación de transformación inferior al 0.3%"
                    ),


                    MedicionEntity(
                        protocoloNumero = "PT-2026-0004",
                        tipoPrueba = "RELACION_TTR",
                        valorA = 4160.0,
                        valorB = 380.0,
                        resultado = "APROBADA",
                        motivo = "Prueba de campo completada satisfactoriamente"
                    ),

                    MedicionEntity(
                        protocoloNumero = "PT-2026-0005",
                        tipoPrueba = "AISLAMIENTO_IP",
                        valorA = 100.0,
                        valorB = 220.0,
                        resultado = "APROBADA",
                        motivo = "Aislamiento verificado en terreno"
                    )
                )

                mediciones.forEach { medicionDao.insertarMedicion(it) }
            }

        }
    }
}
