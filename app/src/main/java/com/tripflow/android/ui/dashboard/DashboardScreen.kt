package com.tripflow.android.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripflow.android.data.local.entity.TripEntity
import com.tripflow.android.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Módulo Dashboard & Estadísticas - Asignado a Integrante 5 (feature/dashboard)
 * Conectado exclusivamente a la base de datos local (Room).
 * Incluye:
 * - Estado de Bienvenida (sin viajes en la base de datos)
 * - Tarjeta de Viaje Activo con contador de días restantes
 * - Resumen de presupuesto y barra de progreso interactiva (Magenta / Rojo sobregiro)
 * - Gráfica de gastos interactiva con tabs (Semana / Mes) y tooltips
 * - Speed Dial FAB animado tipo Nequi (+ rota 45° y despliega "Nuevo Viaje" y "Nuevo Gasto")
 */
@Composable
fun DashboardScreen(
    onNavigateToNewTrip: () -> Unit,
    onNavigateToNewExpense: () -> Unit,
    viewModel: DashboardViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSpeedDialOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                // Speed Dial FAB tipo Nequi
                SpeedDialFab(
                    isOpen = isSpeedDialOpen,
                    onToggle = { isSpeedDialOpen = !isSpeedDialOpen },
                    onNewTrip = {
                        isSpeedDialOpen = false
                        onNavigateToNewTrip()
                    },
                    onNewExpense = {
                        isSpeedDialOpen = false
                        onNavigateToNewExpense()
                    }
                )
            }
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandPrimary)
                }
            } else if (uiState.activeTrip == null) {
                // Estado de bienvenida cuando no hay viajes registrados en Room
                DashboardEmptyState(
                    paddingValues = paddingValues,
                    onNewTripClick = onNavigateToNewTrip
                )
            } else {
                // Contenido principal del Dashboard con datos reales de Room
                DashboardContent(
                    paddingValues = paddingValues,
                    uiState = uiState,
                    onTimeRangeSelected = { viewModel.setTimeRange(it) }
                )
            }
        }

        // Overlay semitransparente cuando el Speed Dial está abierto
        if (isSpeedDialOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isSpeedDialOpen = false }
            )
        }
    }
}

/**
 * Vista de contenido activo del Dashboard con scroll
 */
@Composable
private fun DashboardContent(
    paddingValues: PaddingValues,
    uiState: DashboardUiState,
    onTimeRangeSelected: (TimeRangeFilter) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Encabezado
        DashboardHeader()

        // Tarjeta de Viaje Activo
        uiState.activeTrip?.let { trip ->
            ActiveTripCard(
                trip = trip,
                daysRemaining = uiState.daysRemaining
            )
        }

        // Tarjeta de Presupuesto Consumido
        BudgetCard(
            totalSpent = uiState.totalSpent,
            budgetLimit = uiState.budgetLimit,
            currency = uiState.currency
        )

        // Gráfica de Gastos
        ExpensesChartCard(
            dailyStats = uiState.dailyStats,
            selectedRange = uiState.selectedTimeRange,
            currency = uiState.currency,
            onRangeSelected = onTimeRangeSelected
        )

        Spacer(modifier = Modifier.height(72.dp))
    }
}

/**
 * Encabezado de la pantalla
 */
@Composable
private fun DashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Resumen de viaje activo y finanzas",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary
                )
            )
        }
    }
}

/**
 * Tarjeta del Viaje Activo
 */
@Composable
private fun ActiveTripCard(
    trip: TripEntity,
    daysRemaining: Long
) {
    val statusInfo = getTripStatusInfo(trip.startDate, trip.endDate)
    val formattedDates = formatTripDates(trip.startDate, trip.endDate)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Fila superior: Destino + Badge de Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = "Ubicación",
                        tint = BrandPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = trip.destination,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Badge de estado
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusInfo.bgColor
                ) {
                    Text(
                        text = statusInfo.label,
                        color = statusInfo.textColor,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

            // Fila inferior: Fechas y Contador de Días Restantes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = "Fechas",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formattedDates,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = "Días restantes",
                            tint = BrandPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (daysRemaining > 0) "$daysRemaining días rest." else "Último día",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = BrandPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de Presupuesto Consumido con Barra de Progreso
 */
@Composable
private fun BudgetCard(
    totalSpent: Double,
    budgetLimit: Double,
    currency: String
) {
    val progress = if (budgetLimit > 0) {
        (totalSpent / budgetLimit).toFloat().coerceIn(0f, 1f)
    } else 0f

    val progressPercent = if (budgetLimit > 0) {
        ((totalSpent / budgetLimit) * 100).toInt()
    } else 0

    val remaining = budgetLimit - totalSpent
    val isOver = (totalSpent > budgetLimit) && (budgetLimit > 0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Presupuesto Consumido",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                // Porcentaje
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isOver) DestructiveRed else BrandPrimary
                    )
                )
            }

            // Barra de progreso interactiva
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (isOver) DestructiveRed else BrandPrimary,
                trackColor = if (isOver) DestructiveLight else BrandLight
            )

            // Valores de dinero: Gastado / Límite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Gastado",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = String.format(Locale.US, "$%,.2f %s", totalSpent, currency),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isOver) DestructiveRed else TextPrimary
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Límite Total",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = String.format(Locale.US, "$%,.2f %s", budgetLimit, currency),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    )
                }
            }

            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

            // Indicador de disponible o sobregiro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOver) "Monto Excedido" else "Saldo Disponible",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isOver) DestructiveRed else TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = if (isOver) {
                        String.format(Locale.US, "+$%,.2f %s", totalSpent - budgetLimit, currency)
                    } else {
                        String.format(Locale.US, "$%,.2f %s", remaining.coerceAtLeast(0.0), currency)
                    },
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isOver) DestructiveRed else StatusActiveText
                    )
                )
            }
        }
    }
}

/**
 * Gráfica de Gastos Interactiva con Tabs (Semana / Mes) y Tooltip al tocar
 */
@Composable
private fun ExpensesChartCard(
    dailyStats: List<DailyExpenseStat>,
    selectedRange: TimeRangeFilter,
    currency: String,
    onRangeSelected: (TimeRangeFilter) -> Unit
) {
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }
    val maxAmount = (dailyStats.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(10.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selector de Rango (Esta Semana / Este Mes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gráfica de Gastos",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                // Segmented Buttons / Tabs
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BorderSubtle
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        ChartTabButton(
                            title = "Esta Semana",
                            isSelected = selectedRange == TimeRangeFilter.WEEK,
                            onClick = {
                                selectedBarIndex = null
                                onRangeSelected(TimeRangeFilter.WEEK)
                            }
                        )
                        ChartTabButton(
                            title = "Este Mes",
                            isSelected = selectedRange == TimeRangeFilter.MONTH,
                            onClick = {
                                selectedBarIndex = null
                                onRangeSelected(TimeRangeFilter.MONTH)
                            }
                        )
                    }
                }
            }

            // Tooltip si hay una barra seleccionada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedBarIndex != null && selectedBarIndex!! in dailyStats.indices) {
                    val stat = dailyStats[selectedBarIndex!!]
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandPrimary
                    ) {
                        Text(
                            text = "${stat.dateString}: " + String.format(Locale.US, "$%,.2f %s", stat.amount, currency),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Toca una barra para ver detalles",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Área de Barras
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .then(
                        if (selectedRange == TimeRangeFilter.MONTH) Modifier.horizontalScroll(scrollState)
                        else Modifier
                    ),
                horizontalArrangement = if (selectedRange == TimeRangeFilter.WEEK) Arrangement.SpaceBetween else Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                dailyStats.forEachIndexed { index, stat ->
                    val isSelected = selectedBarIndex == index
                    val heightFraction = (stat.amount / maxAmount).toFloat().coerceIn(0.04f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(if (selectedRange == TimeRangeFilter.WEEK) 36.dp else 24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedBarIndex = if (selectedBarIndex == index) null else index
                            }
                    ) {
                        // Barra
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (isSelected) 0.85f else 0.7f)
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        when {
                                            isSelected -> BrandPrimary
                                            stat.amount > 0 -> BrandPrimary.copy(alpha = 0.75f)
                                            else -> BorderMedium
                                        }
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Etiqueta del día
                        Text(
                            text = stat.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BrandPrimary else TextSecondary
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) SurfaceWhite else Color.Transparent,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) BrandPrimary else TextSecondary
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

/**
 * Estado Vacío (Sin viajes creados en Room)
 */
@Composable
private fun DashboardEmptyState(
    paddingValues: PaddingValues,
    onNewTripClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo o ícono destacado
        Surface(
            shape = CircleShape,
            color = BrandLight,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.FlightTakeoff,
                    contentDescription = "Logo TripFlow",
                    tint = BrandPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¡Bienvenido a TripFlow!",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Aún no tienes ningún viaje registrado. Crea tu primer viaje para comenzar a registrar gastos y controlar tu presupuesto en tiempo real.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondary,
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNewTripClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Crear mi primer viaje",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

/**
 * Speed Dial FAB estilo Nequi
 */
@Composable
private fun SpeedDialFab(
    isOpen: Boolean,
    onToggle: () -> Unit,
    onNewTrip: () -> Unit,
    onNewExpense: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isOpen) 45f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "fab_rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(bottom = 12.dp, end = 4.dp)
    ) {
        // Elementos desplegables
        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn(tween(200)) + slideInVertically(
                animationSpec = tween(200),
                initialOffsetY = { it / 2 }
            ),
            exit = fadeOut(tween(150)) + slideOutVertically(
                animationSpec = tween(150),
                targetOffsetY = { it / 2 }
            )
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Opción 1: Nuevo Viaje (ícono de mapa)
                SpeedDialItem(
                    label = "Nuevo Viaje",
                    icon = Icons.Rounded.Map,
                    onClick = onNewTrip
                )

                // Opción 2: Nuevo Gasto (ícono de ticket)
                SpeedDialItem(
                    label = "Nuevo Gasto",
                    icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                    onClick = onNewExpense
                )
            }
        }

        // Botón FAB principal
        FloatingActionButton(
            onClick = onToggle,
            shape = CircleShape,
            containerColor = BrandPrimary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isOpen) "Cerrar opciones" else "Acciones rápidas",
                modifier = Modifier
                    .size(28.dp)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
private fun SpeedDialItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        // Etiqueta flotante con sombra
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SurfaceWhite,
            shadowElevation = 3.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        // Mini FAB circular
        SmallFloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = SurfaceWhite,
            contentColor = BrandPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// Utilidades de Fechas y Estados
// -------------------------------------------------------------

private data class TripStatusInfo(
    val label: String,
    val bgColor: Color,
    val textColor: Color
)

private fun getTripStatusInfo(startDateStr: String, endDateStr: String): TripStatusInfo {
    return try {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_DATE
        val start = LocalDate.parse(startDateStr.take(10), formatter)
        val end = LocalDate.parse(endDateStr.take(10), formatter)

        when {
            today.isBefore(start) -> TripStatusInfo("Próximo a iniciar", StatusUpcomingBg, StatusUpcomingText)
            today.isAfter(end) -> TripStatusInfo("Finalizado", StatusFinishedBg, StatusFinishedText)
            else -> TripStatusInfo("En curso", StatusActiveBg, StatusActiveText)
        }
    } catch (_: Exception) {
        TripStatusInfo("En curso", StatusActiveBg, StatusActiveText)
    }
}

private fun formatTripDates(startDateStr: String, endDateStr: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_DATE
        val outFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale("es", "ES"))
        val outYearFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es", "ES"))

        val start = LocalDate.parse(startDateStr.take(10), formatter)
        val end = LocalDate.parse(endDateStr.take(10), formatter)

        "${start.format(outFormatter)} - ${end.format(outYearFormatter)}"
    } catch (_: Exception) {
        "$startDateStr - $endDateStr"
    }
}
