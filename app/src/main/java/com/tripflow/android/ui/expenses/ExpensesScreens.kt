package com.tripflow.android.ui.expenses

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

import androidx.compose.material3.FdloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.tripflow.android.data.local.entity.TripEntity
import com.tripflow.android.data.model.Expense
import com.tripflow.android.ui.theme.BackgroundLight
import com.tripflow.android.ui.theme.BrandPrimary
import com.tripflow.android.ui.theme.CatEntertainmentBg
import com.tripflow.android.ui.theme.CatEntertainmentText
import com.tripflow.android.ui.theme.CatFlightsBg
import com.tripflow.android.ui.theme.CatFlightsText
import com.tripflow.android.ui.theme.CatFoodBg
import com.tripflow.android.ui.theme.CatFoodText
import com.tripflow.android.ui.theme.CatLodgingBg
import com.tripflow.android.ui.theme.CatLodgingText
import com.tripflow.android.ui.theme.CatOtherBg
import com.tripflow.android.ui.theme.CatOtherText
import com.tripflow.android.ui.theme.CatTransportBg
import com.tripflow.android.ui.theme.CatTransportText
import com.tripflow.android.ui.theme.DestructiveLight
import com.tripflow.android.ui.theme.DestructiveRed

private enum class ExpenseCategory(val label: String, val background: Color, val foreground: Color) {
    FOOD("Alimentación", CatFoodBg, CatFoodText),
    TRANSPORT("Transporte", CatTransportBg, CatTransportText),
    LODGING("Alojamiento", CatLodgingBg, CatLodgingText),
    ENTERTAINMENT("Entretenimiento", CatEntertainmentBg, CatEntertainmentText),
    FLIGHT("Vuelos", CatFlightsBg, CatFlightsText),
    OTHER("Otros", CatOtherBg, CatOtherText)
}

@Composable
fun ExpensesScreen(
    onNavigateToNewExpense: () -> Unit,
    expenses: List<Expense> = emptyList(),
    trips: List<TripEntity> = emptyList()
) {
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }

    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewExpense,
                containerColor = BrandPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo gasto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Mis Gastos", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Historial y detalles de compras realizadas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (expenses.isEmpty()) {
                Text("Aún no tienes gastos registrados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseRow(expense, trips) { selectedExpense = expense }
                    }
                }
            }
        }
    }

    selectedExpense?.let { expense ->
        ExpenseDetailsSheet(expense, trips) { selectedExpense = null }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, trips: List<TripEntity>, onClick: () -> Unit) {
    val category = categoryFor(expense.category)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CategoryIcon(category)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(expense.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    tripName(expense, trips),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(expense.createdAt.ifBlank { "Sin fecha" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("-${expense.amount}", color = BrandPrimary, style = MaterialTheme.typography.titleSmall)
                if (expense.receiptUrl != null) {
                    Icon(Icons.Default.ReceiptLong, "Tiene recibo", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryIcon(category: ExpenseCategory) {
    val icon = when (category) {
        ExpenseCategory.FOOD -> Icons.Default.Restaurant
        ExpenseCategory.TRANSPORT -> Icons.Default.DirectionsBus
        ExpenseCategory.LODGING -> Icons.Default.Bed
        ExpenseCategory.ENTERTAINMENT -> Icons.Default.ConfirmationNumber
        ExpenseCategory.FLIGHT -> Icons.Default.Flight
        ExpenseCategory.OTHER -> Icons.Default.MoreHoriz
    }
    Box(Modifier.size(46.dp).clip(CircleShape).background(category.background), contentAlignment = Alignment.Center) {
        Icon(icon, category.label, tint = category.foreground)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDetailsSheet(expense: Expense, trips: List<TripEntity>, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text(expense.title, style = MaterialTheme.typography.titleLarge)
            Text(tripName(expense, trips), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Divider(Modifier.padding(vertical = 18.dp))
            DetailLine("Fecha", expense.createdAt.ifBlank { "Sin fecha" })
            DetailLine("Categoría", categoryFor(expense.category).label)
            DetailLine("Monto", "-${expense.amount}")
            if (expense.receiptUrl != null) {
                Text("Recibo adjunto", modifier = Modifier.padding(top = 18.dp), style = MaterialTheme.typography.titleSmall)
                Box(
                    Modifier.fillMaxWidth().height(100.dp).padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp)).background(BackgroundLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ReceiptLong, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, Modifier.weight(1f)) { Text("Editar") }
                Button(onClick = onDismiss, Modifier.weight(1f)) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExpenseScreen(
    onNavigateBack: () -> Unit,
    trips: List<TripEntity> = emptyList(),
    budgetRemaining: Double = 100.0,
    onSaveExpense: (Expense) -> Unit = {}
) {
    var selectedTrip by remember { mutableStateOf<TripEntity?>(null) }
    var tripExpanded by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(ExpenseCategory.LODGING) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showOverdraft by remember { mutableStateOf(false) }
    var receiptBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var receiptUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var cameraPermissionDenied by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        receiptBitmap = it
        receiptUri = null
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(null) else cameraPermissionDenied = true
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        receiptUri = it
        receiptBitmap = null
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("tripflow", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = BrandPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Registrar Nuevo Gasto", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 10.dp, bottom = 14.dp))
            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch(null)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CameraAlt, null)
                Spacer(Modifier.width(8.dp))
                Text("Escanear Recibo", style = MaterialTheme.typography.titleMedium)
            }
            if (receiptBitmap != null || receiptUri != null) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (receiptUri != null) {
                        AsyncImage(receiptUri, "Recibo seleccionado", Modifier.size(44.dp).clip(RoundedCornerShape(6.dp)))
                    } else {
                        Text("Recibo capturado", color = BrandPrimary)
                    }
                    TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.PhotoLibrary, null)
                        Text("Cambiar")
                    }
                }
            } else {
                TextButton(onClick = { galleryLauncher.launch("image/*") }, Modifier.align(Alignment.End)) {
                    Icon(Icons.Default.PhotoLibrary, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Elegir de galería")
                }
            }

            FieldLabel("Viaje")
            ExpenseDropdown(
                value = selectedTrip?.destination ?: "Selecciona un viaje",
                expanded = tripExpanded,
                onExpandedChange = { tripExpanded = !tripExpanded },
                options = trips.map(::tripLabel),
                modifier = Modifier.fillMaxWidth()
            ) { selectedLabel ->
                selectedTrip = trips.firstOrNull { tripLabel(it) == selectedLabel }
                tripExpanded = false
            }

            FieldLabel("Fecha")
            OutlinedButton(onClick = { showDatePicker = true }, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(8.dp)) {
                Text(if (date.isBlank()) "Fecha del Gasto" else date, Modifier.weight(1f), color = if (date.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Default.CalendarMonth, "Seleccionar fecha")
            }

            FieldLabel("Categoría")
            ExpenseDropdown(
                value = category.label,
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                options = ExpenseCategory.entries.map { it.label },
                modifier = Modifier.fillMaxWidth()
            ) { selectedLabel ->
                category = categoryFor(selectedLabel)
                categoryExpanded = false
            }

            FieldLabel("Descripción")
            OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), placeholder = { Text("Descripción") }, singleLine = true, shape = RoundedCornerShape(8.dp))

            FieldLabel("Monto")
            OutlinedTextField(
                amount,
                { amount = it },
                Modifier.fillMaxWidth(),
                placeholder = { Text("Monto") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    selectedTrip?.let { trip ->
                        val overdraft = amount.toDoubleOrNull()?.let { it > budgetRemaining } == true
                        if (overdraft) {
                            showOverdraft = true
                        } else {
                            onSaveExpense(createExpense(description, amount, category, date, receiptUri, trip.id))
                            onNavigateBack()
                        }
                    }
                },
                enabled = selectedTrip != null && description.isNotBlank() && amount.toDoubleOrNull() != null,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Registrar Gasto", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(it))
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }
    if (showOverdraft) {
        AlertDialog(
            onDismissRequest = { showOverdraft = false },
            title = { Text("Posible sobregiro") },
            text = { Text("Este gasto supera el presupuesto restante del viaje. ¿Deseas registrarlo de todas formas?") },
            confirmButton = {
                TextButton(onClick = {
                    selectedTrip?.let { trip ->
                        showOverdraft = false
                        onSaveExpense(createExpense(description, amount, category, date, receiptUri, trip.id))
                        onNavigateBack()
                    }
                }) { Text("Registrar", color = DestructiveRed) }
            },
            dismissButton = { TextButton(onClick = { showOverdraft = false }) { Text("Cancelar") } },
            containerColor = DestructiveLight
        )
    }
    if (cameraPermissionDenied) {
        AlertDialog(
            onDismissRequest = { cameraPermissionDenied = false },
            title = { Text("Permiso de cámara requerido") },
            text = { Text("Para escanear un recibo debes permitir el acceso a la cámara.") },
            confirmButton = { TextButton(onClick = { cameraPermissionDenied = false }) { Text("Entendido") } }
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = Color(0xFF604B53), modifier = Modifier.padding(top = 4.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDropdown(
    value: String,
    expanded: Boolean,
    onExpandedChange: () -> Unit,
    options: List<String>,
    modifier: Modifier
        = Modifier,
    onOptionSelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { onExpandedChange() }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(8.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = onExpandedChange
        ) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onOptionSelected(option) })
            }
        }
    }
}

private fun createExpense(
    description: String,
    amount: String,
    category: ExpenseCategory,
    date: String,
    receiptUri: android.net.Uri?,
    tripId: Long
) = Expense(
    tripId = tripId.toString(),
    title = description.trim(),
    amount = amount.toDoubleOrNull() ?: 0.0,
    category = category.label,
    receiptUrl = receiptUri?.toString(),
    createdAt = date
)

private fun categoryFor(label: String) =
    ExpenseCategory.entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: ExpenseCategory.OTHER

private fun tripLabel(trip: TripEntity) =
    "${trip.destination} (${trip.startDate} - ${trip.endDate})"

private fun tripName(expense: Expense, trips: List<TripEntity>) =
    trips.firstOrNull { it.id.toString() == expense.tripId }?.destination ?: "Viaje sin asignar"
