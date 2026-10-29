# Reglas y Contexto del Proyecto para Antigravity (AGY)

Bienvenido a **TripFlow Android**. Eres un asistente experto en desarrollo móvil con **Kotlin**, **Jetpack Compose** y **Material 3**. 
Este proyecto es la versión nativa de la aplicación web TripFlow, conectada al backend de **Supabase**.

---

## 1. Reglas Generales de Arquitectura

1. **Lenguaje y UI:** Todo el código de interfaz debe estar escrito en **Kotlin** utilizando **Jetpack Compose** con componentes de **Material 3**. No uses vistas XML tradicionales para la interfaz.
2. **Modularidad estricta:** Este proyecto es desarrollado por un equipo de 5 personas. **Cada integrante trabaja en una rama y paquete específico**. NUNCA modifiques archivos fuera del módulo asignado por el usuario sin su autorización expresa.
3. **Manejo de Estado:** Utiliza arquitecturas recomendadas por Android (StateFlow / ViewModel / Compose State).
4. **Backend:** Todas las operaciones de red, autenticación y base de datos se realizan a través de la librería oficial de Supabase para Kotlin (`data/supabase/SupabaseClient.kt`).

---

## 2. Mapa de Responsabilidades por Integrante

* **Integrante 1 (Arquitectura y Navegación):**
  * `app/src/main/java/com/tripflow/android/ui/theme/` (Colores, Tipografía, Formas).
  * `app/src/main/java/com/tripflow/android/ui/navigation/` (Scaffold central, BottomNav, NavHost).
  * `app/src/main/java/com/tripflow/android/data/supabase/` (Configuración del cliente).
* **Integrante 2 (Módulo Auth):**
  * Rama: `feature/auth`
  * Paquete: `app/src/main/java/com/tripflow/android/ui/auth/`
  * Responsabilidad: Pantallas de Login y Registro (basado en Figma) y gestión de sesión con Supabase Auth.
* **Integrante 3 (Módulo Viajes):**
  * Rama: `feature/trips`
  * Paquete: `app/src/main/java/com/tripflow/android/ui/trips/`
  * Responsabilidad: Listado de viajes con filtros, tarjetas de viaje y formulario de nuevo viaje con API Nominatim y selector de fechas.
* **Integrante 4 (Módulo Gastos):**
  * Rama: `feature/expenses`
  * Paquete: `app/src/main/java/com/tripflow/android/ui/expenses/`
  * Responsabilidad: Lista de gastos con categorías, formulario de nuevo gasto, captura de fotos con la cámara nativa para tickets y alerta modal de sobregiro.
* **Integrante 5 (Módulo Dashboard & Gráficas):**
  * Rama: `feature/dashboard`
  * Paquete: `app/src/main/java/com/tripflow/android/ui/dashboard/`
  * Responsabilidad: Pantalla principal de Dashboard, barra de progreso del presupuesto consumido, gráfica de gastos y botón flotante interactivo (*Speed Dial FAB* tipo Nequi).

---

## 3. Especificación de Diseño (UI Design Tokens)

Antes de codificar cualquier pantalla, consulta obligatoriamente el archivo:
`docs/UI_SPECIFICATION.md`

* **Color Primario:** Magenta `Color(0xFFFF3482)`
* **Color Primario Claro:** `Color(0xFFFFEAEE)`
* **Fondo de Pantalla:** `Color(0xFFF9FAFB)`
* **Tarjetas:** Superficie blanca `Color.White` con elevación sutil (`1.dp` o `2.dp`) y esquinas redondeadas (`16.dp`).

---

## 4. Reglas de Git y Commits

Al ayudar al desarrollador a guardar cambios o hacer commits, genera siempre mensajes bajo el estándar **Conventional Commits**:
* `feat(<módulo>): descripción concisa de la nueva funcionalidad`
* `fix(<módulo>): corrección de un bug o cálculo`
* `ui(<módulo>): ajuste visual o de estilos`
* `refactor(<módulo>): mejora de código sin cambiar comportamiento`
* `docs(<tema>): adición o actualización de documentación`
