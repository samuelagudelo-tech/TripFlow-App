package com.tripflow.android.ui.trips

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tripflow.android.ui.theme.BrandPrimary

/**
 * Módulo de Viajes - Asignado a Integrante 3 (feature/trips)
 * Lista con filtros (Todos, Actuales, Próximos, Finalizados) y creación de viaje con OpenStreetMap.
 */
@Composable
fun TripsScreen(
    onNavigateToNewTrip: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewTrip,
                containerColor = BrandPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Viaje")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Mis Viajes",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Gestiona tus destinos y presupuestos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // TODO Integrante 3: Chips de filtro y LazyColumn con tarjetas de viaje
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTripScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Viaje") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TODO Integrante 3: Buscador Nominatim, selector de fechas, presupuesto y moneda
            Text(
                text = "Formulario de Creación de Viaje",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
