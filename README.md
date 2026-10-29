# ✈️ TripFlow Android

Aplicación móvil nativa para la gestión inteligente de presupuestos y gastos de viaje en tiempo real, desarrollada en **Kotlin** y **Jetpack Compose** para Android Studio, conectada al backend de **Supabase**.

Este proyecto es la evolución nativa de TripFlow Web, implementado bajo una arquitectura modular y colaborativa.

---

## 🛠️ Stack Tecnológico
* **Lenguaje:** Kotlin 2.0+
* **UI Toolkit:** Jetpack Compose + Material 3
* **Arquitectura:** MVVM + Clean Architecture modular
* **Navegación:** Navigation Compose (Bottom Navigation Bar)
* **Backend & Autenticación:** Supabase Kotlin SDK (Auth, PostgREST, Storage)
* **Carga de Imágenes:** Coil Compose
* **API Externa:** OpenStreetMap Nominatim (Búsqueda de destinos)

---

## 📂 Estructura del Proyecto

```text
TripFlow-Android/
├── AGENTS.md                  # Contexto y directrices para Antigravity (AGY)
├── docs/
│   └── UI_SPECIFICATION.md    # Especificación visual completa extraída de TripFlow Web
├── app/
│   └── src/main/java/com/tripflow/android/
│       ├── data/              # Modelos de datos y cliente de Supabase
│       │   ├── model/
│       │   └── supabase/
│       └── ui/                # Capa de presentación modular
│           ├── theme/         # Colores magenta, tipografía y tema Material 3
│           ├── navigation/    # Rutas y barra de navegación inferior
│           ├── auth/          # Módulo de Login y Registro (Figma)
│           ├── dashboard/     # Métricas, gráfica y Speed Dial FAB
│           ├── trips/         # Listado y creación de viajes
│           └── expenses/      # Registro de tickets, fotos y categorías
```

---

## 🚀 Flujo de Trabajo en Git

1. Clonar el repositorio.
2. Crear una rama según la tarea asignada:
   * `feature/auth`
   * `feature/trips`
   * `feature/expenses`
   * `feature/dashboard`
3. Realizar commits siguiendo **Conventional Commits** (`feat:`, `fix:`, `ui:`).
4. Abrir un Pull Request hacia la rama `develop`.
