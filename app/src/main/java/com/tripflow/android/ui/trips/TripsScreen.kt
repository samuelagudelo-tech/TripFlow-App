package com.tripflow.android.ui.trips

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripflow.android.data.local.entity.TripEntity
import com.tripflow.android.ui.theme.*
import java.util.Locale

@Composable
fun TripsScreen(
    onNavigateToNewTrip: () -> Unit,
    viewModel: TripsViewModel = viewModel()
) {
    val trips by viewModel.filteredTrips.collectAsState()
    val allTrips by viewModel.trips.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var tripToDelete by remember { mutableStateOf<TripEntity?>(null) }

    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewTrip,
                containerColor = BrandPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Viaje")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Cabecera de la pantalla
            Text(
                text = "Mis Viajes",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Gestiona tus destinos, fechas y presupuestos",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filtros horizontales (Horizontal Chips)
            TripFilterChipsRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { viewModel.setFilter(it) },
                allCount = allTrips.size,
                activeCount = allTrips.count { it.status == TripStatus.ACTIVE },
                upcomingCount = allTrips.count { it.status == TripStatus.UPCOMING },
                finishedCount = allTrips.count { it.status == TripStatus.FINISHED }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista o Estado vacío
            if (trips.isEmpty()) {
                EmptyTripsState(
                    filter = selectedFilter,
                    hasAnyTrip = allTrips.isNotEmpty(),
                    onCreateTripClick = onNavigateToNewTrip
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(trips, key = { it.trip.id }) { item ->
                        TripCardItem(
                            item = item,
                            onDeleteClick = { tripToDelete = item.trip }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar viaje
    tripToDelete?.let { trip ->
        AlertDialog(
            onDismissRequest = { tripToDelete = null },
            icon = {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = DestructiveRed
                )
            },
            title = {
                Text(
                    text = "Eliminar viaje",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "¿Deseas eliminar el viaje a \"${trip.destination}\"? Esta acción no se puede deshacer y también eliminará sus gastos asociados.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTrip(trip)
                        tripToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DestructiveRed)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { tripToDelete = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Fila de filtros rápidos (Chips horizontales)
 */
@Composable
private fun TripFilterChipsRow(
    selectedFilter: TripFilter,
    onFilterSelected: (TripFilter) -> Unit,
    allCount: Int,
    activeCount: Int,
    upcomingCount: Int,
    finishedCount: Int
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(TripFilter.values()) { filter ->
            val isSelected = filter == selectedFilter
            val count = when (filter) {
                TripFilter.ALL -> allCount
                TripFilter.ACTIVE -> activeCount
                TripFilter.UPCOMING -> upcomingCount
                TripFilter.FINISHED -> finishedCount
            }

            val containerColor = if (isSelected) BrandPrimary else SurfaceWhite
            val contentColor = if (isSelected) Color.White else TextSecondary
            val borderColor = if (isSelected) BrandPrimary else BorderMedium

            Surface(
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(20.dp),
                color = containerColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                shadowElevation = if (isSelected) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = filter.displayName,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = contentColor
                    )
                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White.copy(alpha = 0.25f) else BorderSubtle)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta individual de viaje con estado, progreso y diseño Material 3
 */
@Composable
fun TripCardItem(
    item: TripItemUiModel,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Fila superior: Destino, Badge de Estado y Botón de Opciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Destino",
                        tint = BrandPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.trip.destination,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.daysCount} días de aventura",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Badge de estado
                TripStatusBadge(status = item.status)

                // Botón para eliminar viaje
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar viaje",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

            // Rango de fechas
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Fechas",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = item.formattedDates,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Barra de presupuesto y montos consumidos
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Presupuesto",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    val formattedSpent = formatCurrency(item.spentAmount)
                    val formattedLimit = formatCurrency(item.trip.budgetLimit)
                    Text(
                        text = "$formattedSpent / $formattedLimit ${item.trip.currency}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isOverBudget) DestructiveRed else TextPrimary
                    )
                }

                // Barra de progreso
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (item.isOverBudget) DestructiveRed else BrandPrimary,
                    trackColor = if (item.isOverBudget) DestructiveLight else BrandLight
                )

                // Saldo restante o aviso de sobregiro
                val remaining = item.trip.budgetLimit - item.spentAmount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (item.isOverBudget) {
                        Text(
                            text = "Sobregiro de ${formatCurrency(-remaining)} ${item.trip.currency}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DestructiveRed
                        )
                    } else {
                        val percentage = if (item.trip.budgetLimit > 0) {
                            ((item.spentAmount / item.trip.budgetLimit) * 100).toInt()
                        } else 0
                        Text(
                            text = "$percentage% consumido",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Disponible: ${formatCurrency(remaining)} ${item.trip.currency}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Insignia visual del estado de viaje según UI Specification
 */
@Composable
fun TripStatusBadge(status: TripStatus) {
    val (label, bg, text) = when (status) {
        TripStatus.ACTIVE -> Triple("En curso", StatusActiveBg, StatusActiveText)
        TripStatus.UPCOMING -> Triple("Próximo", StatusUpcomingBg, StatusUpcomingText)
        TripStatus.FINISHED -> Triple("Finalizado", StatusFinishedBg, StatusFinishedText)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = text
        )
    }
}

/**
 * Estado vacío cuando no hay viajes o la categoría seleccionada no tiene resultados
 */
@Composable
private fun EmptyTripsState(
    filter: TripFilter,
    hasAnyTrip: Boolean,
    onCreateTripClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(BrandLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FlightTakeoff,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            val title = if (!hasAnyTrip) {
                "Aún no tienes viajes registrados"
            } else {
                "No hay viajes en la categoría \"${filter.displayName}\""
            }

            val subtitle = if (!hasAnyTrip) {
                "Comienza planificando tu próxima aventura. Define tu destino turístico y presupuesto."
            } else {
                "Cambia el filtro seleccionado o agrega un nuevo viaje para verlo reflejado aquí."
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onCreateTripClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear viaje", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    return String.format(Locale.US, "$%,.2f", amount)
}
