package com.tripflow.android.ui.expenses

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
 * Módulo de Gastos - Asignado a Integrante 4 (feature/expenses)
 * Lista de gastos por categoría, cámara para recibos y validación de sobregiro.
 */
@Composable
fun ExpensesScreen(
    onNavigateToNewExpense: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewExpense,
                containerColor = BrandPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Gasto")
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
                text = "Mis Gastos",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Historial y detalles de compras realizadas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // TODO Integrante 4: Lista LazyColumn de gastos con íconos por categoría
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExpenseScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Gasto") },
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
            // TODO Integrante 4: Selector de viaje, monto, categoría, cámara para foto del ticket
            Text(
                text = "Formulario de Registro de Gasto",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
