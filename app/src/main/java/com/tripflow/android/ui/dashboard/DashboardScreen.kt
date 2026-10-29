package com.tripflow.android.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tripflow.android.ui.theme.BrandPrimary

/**
 * Módulo Dashboard & Estadísticas - Asignado a Integrante 5 (feature/dashboard)
 * Incluye tarjeta de presupuesto, gráfica de gastos y Speed Dial FAB tipo Nequi.
 */
@Composable
fun DashboardScreen(
    onNavigateToNewTrip: () -> Unit,
    onNavigateToNewExpense: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            // TODO Integrante 5: Expandable Speed Dial FAB
            FloatingActionButton(
                onClick = onNavigateToNewExpense,
                containerColor = BrandPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Gasto / Viaje")
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
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Resumen del viaje activo y finanzas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // TODO Integrante 5: Tarjeta de Presupuesto con progreso y gráfica
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Presupuesto Consumido", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Aquí irá la barra de progreso y la gráfica de gastos", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
