# Especificación de Diseño y UI - TripFlow Android

Este documento contiene la especificación visual y de interacción extraída del proyecto web TripFlow para que el equipo de desarrollo y los asistentes de IA (**Antigravity**) implementen la versión nativa en **Kotlin + Jetpack Compose** con total fidelidad.

---

## 1. Identidad de Marca y Sistema de Color

* **Color Primario (Brand):** `#FF3482` (Magenta TripFlow)
  * Hover / Dark Variant: `#E02B71`
  * Contenedor Claro (Badges / Fondos suaves): `#FFEAEE`
* **Colores de Fondo y Superficie:**
  * Fondo General de la App: `#F9FAFB` (Gris claro)
  * Tarjetas y Contenedores: `#FFFFFF` (Blanco puro)
  * Bordes: `#F3F4F6` (sutil) y `#E5E7EB` (inputs)
* **Texto:**
  * Primario / Títulos: `#111827` (Gris 900)
  * Secundario / Subtítulos: `#6B7280` (Gris 500)
* **Estados de Viaje:**
  * *En curso*: Fondo `#DCFCE7`, Texto `#15803D` (Verde)
  * *Próximo a iniciar*: Fondo `#DBEAFE`, Texto `#1D4ED8` (Azul)
  * *Finalizado*: Fondo `#F3F4F6`, Texto `#4B5563` (Gris)
* **Insignias por Categoría de Gasto:**
  * **Alimentación / Comida:** Ícono de cubiertos (Utensils), Fondo `#FFEDD5`, Ícono/Texto `#EA580C` (Naranja)
  * **Transporte:** Ícono de autobús (Bus), Fondo `#DBEAFE`, Ícono/Texto `#2563EB` (Azul)
  * **Alojamiento:** Ícono de cama (Bed), Fondo `#F3E8FF`, Ícono/Texto `#9333EA` (Morado)
  * **Entretenimiento:** Ícono de ticket (Ticket), Fondo `#FCE7F3`, Ícono/Texto `#DB2777` (Rosa)
  * **Vuelos:** Ícono de avión (Plane), Fondo `#CFFAFE`, Ícono/Texto `#0891B2` (Cian)
  * **Otros:** Ícono de tres puntos (MoreHorizontal), Fondo `#F3F4F6`, Ícono/Texto `#6B7280` (Gris)

---

## 2. Pantallas y Componentes

### A. Módulo de Autenticación (`ui/auth/`)
* **Login:**
  * Campo de correo y contraseña con validaciones.
  * Botón primario en Magenta `#FF3482` con esquinas redondeadas (`16dp`).
  * Enlace "¿No tienes cuenta? Regístrate aquí".
* **Registro (según Figma):**
  * Campos diseñados en Figma (Nombre completo, correo, contraseña, confirmación).
  * Validación en tiempo real (mínimo de caracteres, contraseñas coincidentes).

### B. Dashboard (`ui/dashboard/`)
* **Estado de Bienvenida (Sin viajes creados):**
  * Logo de TripFlow centrado, mensaje amigable y botón destacado para "Crear mi primer viaje".
* **Tarjeta de Viaje Activo:**
  * Nombre del destino y badge con el estado actual (*En curso* / *Próximo*).
  * Fechas formateadas (ej: "28 ago - 05 sep 2026") y contador de días restantes.
  * Presupuesto consumido:
    * Monto gastado vs. límite total en formato de moneda (ej: `$1,250.00 / $3,000.00 USD`).
    * Barra de progreso (`LinearProgressIndicator`): Magenta si está dentro del presupuesto; Rojo `#EF4444` si hay sobregiro.
    * Indicador de saldo disponible o monto excedido.
* **Gráfica de Gastos:**
  * Filtro por pestañas o selector: "Esta Semana" / "Este Mes".
  * Gráfico de barras o área con tooltips en color magenta al tocar cada día.
* **Botón Flotante (Speed Dial FAB):**
  * Botón circular en magenta con el ícono `+` en la esquina inferior derecha.
  * Al presionarlo, el ícono rota 45° y se despliegan dos opciones flotantes:
    1. *Nuevo Viaje* (ícono de mapa)
    2. *Nuevo Gasto* (ícono de ticket)

### C. Mis Viajes (`ui/trips/`)
* **Filtros rápidos (Horizontal Chips):** "Todos", "Actuales", "Próximos", "Finalizados".
* **Tarjetas de Viaje:**
  * Destino (ej: "Madrid, España") con ícono de ubicación (`MapPin`).
  * Rango de fechas y presupuesto configurado.
  * Barra de progreso en miniatura del viaje.
* **Formulario de Nuevo Viaje (`NewTripScreen`):**
  * Campo de destino con autocompletado en vivo llamando a la API de Nominatim (OpenStreetMap).
  * Selector de rango de fechas con calendario nativo (`DateRangePicker`).
  * Entrada formateada de presupuesto monetario.
  * Selector de divisa (USD, EUR, COP, etc.).
  * Validación para evitar solapamiento con viajes ya existentes.

### D. Mis Gastos (`ui/expenses/`)
* **Lista de Gastos:**
  * Cada fila/tarjeta muestra:
    * Ícono de categoría con su respectivo color de fondo.
    * Título del gasto y nombre del viaje asociado.
    * Fecha del gasto.
    * Monto formateado con signo negativo en magenta (ej: `-$45.00 USD`).
    * Ícono sutil de recibo si tiene foto adjunta.
* **Detalle del Gasto (`ModalBottomSheet`):**
  * Al tocar un gasto, se despliega una hoja inferior con los detalles completos.
  * Si contiene recibo fotográfico, miniatura con opción de zoom a pantalla completa.
  * Botones de acción: Editar y Eliminar.
* **Formulario de Nuevo Gasto (`NewExpenseScreen`):**
  * Selector del viaje al que pertenece.
  * Campo de concepto / título.
  * Entrada formateada del monto gastado.
  * Selector de categoría con íconos visuales.
  * Selector de fecha del gasto (valida que esté dentro de las fechas del viaje).
  * **Captura de Recibo:** Botón para tomar foto con la cámara nativa o seleccionar de la galería, con previsualización.
  * **Alerta de Sobregiro:** Si el gasto supera el presupuesto restante del viaje, se lanza un diálogo modal de confirmación advirtiendo al usuario antes de guardar.
