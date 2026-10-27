# Induztek - Sistema Móvil de Formalización de Pruebas Eléctricas en Terreno

**Asignatura:** DSY1105 - Desarrollo de Aplicaciones Móviles  
**Evaluación:** Evaluación Parcial 2 (Encargo)  
**Estudiante:** Angel Galdames  
**Institución:** Duoc UC  
**Fecha:** Octubre 2026  

---

## Descripción del Proyecto

Aplicación móvil nativa en Android desarrollada para **Induztek Ingeniería SpA**, orientada a técnicos e ingenieros en terreno para la inspección, ejecución y formalización inmediata de protocolos de prueba eléctrica en subestaciones y transformadores de potencia.

La solución opera con enfoque **offline-first**, asegurando el registro de mediciones críticas (Índice de Polarización IP y Relación de Transformación TTR), captura de evidencia fotográfica del instrumento y formalización in situ del acta de recepción mediante firma y validación de RUT.

---

## Funcionalidades Principales

1. **Autenticación y Seguridad (RBAC):**
   - Control de acceso por roles: **Técnico de Terreno** (gestión y toma de pruebas) vs. **Cliente Supervisor** (consulta filtrada por empresa).
   - Encriptación de contraseñas mediante algoritmo **SHA-256**.
   - Persistencia de sesión activa mediante **Jetpack DataStore Preferences**.
   - Animaciones fluidas en Compose (`AnimatedVisibility`, `animateContentSize`) en retroalimentación de credenciales y errores.

2. **Persistencia Local (Room SQLite):**
   - Arquitectura de persistencia con **Room 2.7**: tablas para Usuarios, Protocolos y Mediciones.
   - Datos semilla precargados para pruebas automáticas sin conexión.

3. **Recursos Nativos del Dispositivo (Hardware):**
   - **Cámara:** Captura de evidencia fotográfica del instrumento de medición y fotografía del acta de recepción formalizada.
   - **GPS / Localización:** Integración con `FusedLocationProviderClient` para georreferenciación de la faena en el resumen del protocolo.

4. **Reglas de Negocio Eléctricas y Validaciones:**
   - Cálculo automático del Índice de Polarización (IP = R10 / R1).
   - Cálculo de error porcentual TTR y clasificación de resultado global (Aprobado / Observado / Rechazado).
   - Validación algorítmica de RUT chileno mediante algoritmo **Módulo 11**.

5. **Métricas y Monitoreo:**
   - Dashboard interactivo con métricas estadísticas segregadas por rol y estado de protocolos.
   - Centro de notificaciones para clientes sobre protocolos formalizados.

---

## Tecnologías y Arquitectura

- **Lenguaje:** Kotlin 2.x
- **Framework UI:** Jetpack Compose + Material Design 3
- **Patrón Arquitectónico:** MVVM (Model - View - ViewModel)
- **Base de Datos Local:** Room Database + SQLite
- **Almacenamiento de Preferencias:** Jetpack DataStore
- **Servicios de Ubicación:** Google Play Services Location (`FusedLocationProviderClient`)
- **Control de Versiones:** Git & GitHub

---

## Estructura del Proyecto

```text
cl.duocuc.induztektrabajo/
├── data/
│   ├── local/          # Base de datos Room, Entidades y DAOs
│   ├── preferences/    # SesionDataStore (sesión de usuario)
│   └── security/       # PasswordHasher (hashing SHA-256)
├── model/              # Clases de dominio y modelos de datos
├── ui/
│   ├── components/     # Componentes visuales reutilizables
│   ├── screens/        # Pantallas (Login, Inicio, Registro, Resumen, Acta, etc.)
│   ├── styles/         # Modificadores y estilos Material 3
│   └── theme/          # Paleta de colores, tipografía y tema Induztek
├── util/               # Algoritmo RutValidator y gestor de Ubicación GPS
└── viewmodel/          # ViewModels (gestión de estado desacoplada)
