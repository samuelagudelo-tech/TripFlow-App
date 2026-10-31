package com.tripflow.android.ui.trips

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripflow.android.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTripScreen(
    onNavigateBack: () -> Unit,
    viewModel: TripsViewModel = viewModel()
) {
    var destination by remember { mutableStateOf("") }
    var startDateStr by remember { mutableStateOf<String?>(null) }
    var endDateStr by remember { mutableStateOf<String?>(null) }
    var budgetInput by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()

    val suggestions by viewModel.destinationSuggestions.collectAsState()
    val isSearchingDestinations by viewModel.isSearchingDestinations.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    var showSuggestionsDropdown by remember { mutableStateOf(false) }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }

    // Detección en tiempo real de solapamiento de fechas
    val overlappingTrip = remember(startDateStr, endDateStr) {
        if (startDateStr != null && endDateStr != null) {
            viewModel.findOverlappingTrip(startDateStr!!, endDateStr!!)
        } else {
            null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.resetSaveState()
        viewModel.clearSuggestions()
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nuevo Viaje",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Cabecera descriptiva
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Planifica tu destino",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Ingresa la ciudad a la que viajarás. El buscador autocompletará la ciudad y su país correspondiente.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                )
            }

            // 1. Campo de Destino Turístico con Autocompletado en vivo (Nominatim)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Destino turístico",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { query ->
                            destination = query
                            localErrorMessage = null
                            showSuggestionsDropdown = true
                            viewModel.onDestinationQueryChanged(query)
                        },
                        placeholder = {
                            Text("Ej: París, Madrid, Cancún, Bogotá...", color = TextSecondary)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = BrandPrimary
                            )
                        },
                        trailingIcon = {
                            if (isSearchingDestinations) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = BrandPrimary
                                )
                            } else if (destination.isNotEmpty()) {
                                IconButton(onClick = {
                                    destination = ""
                                    viewModel.clearSuggestions()
                                    showSuggestionsDropdown = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Borrar",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = BorderMedium,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Menú de sugerencias autocompletadas (Nominatim API)
                    DropdownMenu(
                        expanded = showSuggestionsDropdown && suggestions.isNotEmpty(),
                        onDismissRequest = { showSuggestionsDropdown = false },
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(SurfaceWhite)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    ) {
                        suggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = suggestion.fullFormatted,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = suggestion.subtitle,
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BrandLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    // Al seleccionarla, se autocompleta con su país
                                    destination = suggestion.fullFormatted
                                    showSuggestionsDropdown = false
                                    viewModel.clearSuggestions()
                                }
                            )
                        }
                    }
                }
            }

            // 2. Selector de Rango de Fechas (DateRangePicker)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Fechas del viaje",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                val datesText = if (startDateStr != null && endDateStr != null) {
                    val formatted = TripsViewModel.formatDatesRange(startDateStr!!, endDateStr!!)
                    val days = TripsViewModel.computeDays(startDateStr!!, endDateStr!!)
                    "$formatted ($days días)"
                } else {
                    "Seleccionar rango de fechas"
                }

                Surface(
                    onClick = { showDatePickerDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = BrandPrimary
                            )
                            Text(
                                text = datesText,
                                fontSize = 14.sp,
                                color = if (startDateStr != null) TextPrimary else TextSecondary,
                                fontWeight = if (startDateStr != null) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }

                        Text(
                            text = if (startDateStr != null) "Cambiar" else "Elegir",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary
                        )
                    }
                }
            }

            // Advertencia visual de solapamiento de fechas
            AnimatedVisibility(visible = overlappingTrip != null) {
                overlappingTrip?.let { overlap ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Alerta de solapamiento",
                                tint = Color(0xFFD97706)
                            )
                            Column {
                                Text(
                                    text = "Solapamiento de fechas detectado",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFB45309)
                                )
                                Text(
                                    text = "Ya tienes un viaje a \"${overlap.destination}\" (${overlap.startDate} al ${overlap.endDate}) que se cruza con estas fechas.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF92400E),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. Presupuesto y Divisa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Campo de Presupuesto
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Presupuesto límite",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { input ->
                            // Permitir solo números y punto decimal
                            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                budgetInput = input
                                localErrorMessage = null
                            }
                        },
                        placeholder = { Text("0.00", color = TextSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = BrandPrimary
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = BorderMedium,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Selector de Divisa
                Column(modifier = Modifier.width(130.dp)) {
                    Text(
                        text = "Moneda",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    val currencies = listOf("USD", "EUR", "COP", "MXN", "GBP")

                    Box {
                        Surface(
                            onClick = { currencyDropdownExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedCurrency,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = if (currencyDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = currencyDropdownExpanded,
                            onDismissRequest = { currencyDropdownExpanded = false },
                            modifier = Modifier.background(SurfaceWhite)
                        ) {
                            currencies.forEach { curr ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = curr,
                                            fontWeight = if (curr == selectedCurrency) FontWeight.Bold else FontWeight.Normal,
                                            color = if (curr == selectedCurrency) BrandPrimary else TextPrimary
                                        )
                                    },
                                    onClick = {
                                        selectedCurrency = curr
                                        currencyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Mensajes de error (validación local o del ViewModel)
            val activeError = localErrorMessage ?: (saveState as? NewTripSaveState.Error)?.message
            if (activeError != null) {
                Text(
                    text = activeError,
                    color = DestructiveRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón Primario para Crear Viaje
            Button(
                onClick = {
                    val budget = budgetInput.toDoubleOrNull() ?: 0.0

                    if (destination.isBlank()) {
                        localErrorMessage = "Por favor ingresa un destino turístico"
                        return@Button
                    }
                    if (startDateStr == null || endDateStr == null) {
                        localErrorMessage = "Por favor selecciona el rango de fechas de tu viaje"
                        return@Button
                    }
                    if (budget <= 0.0) {
                        localErrorMessage = "Ingresa un presupuesto válido mayor a 0"
                        return@Button
                    }
                    if (overlappingTrip != null) {
                        localErrorMessage = "No puedes registrar un viaje que se solape con otro existente."
                        return@Button
                    }

                    viewModel.createTrip(
                        destination = destination.trim(),
                        startDate = startDateStr!!,
                        endDate = endDateStr!!,
                        budgetLimit = budget,
                        currency = selectedCurrency,
                        onSuccess = onNavigateBack
                    )
                },
                enabled = saveState !is NewTripSaveState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary,
                    disabledContainerColor = BrandPrimary.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (saveState is NewTripSaveState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Crear Viaje",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Selector nativo de fechas con DateRangePicker
    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val startMillis = dateRangePickerState.selectedStartDateMillis
                        val endMillis = dateRangePickerState.selectedEndDateMillis

                        if (startMillis != null && endMillis != null) {
                            val start = Instant.ofEpochMilli(startMillis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            val end = Instant.ofEpochMilli(endMillis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()

                            startDateStr = start.toString()
                            endDateStr = end.toString()
                            localErrorMessage = null
                        }
                        showDatePickerDialog = false
                    },
                    enabled = dateRangePickerState.selectedStartDateMillis != null &&
                              dateRangePickerState.selectedEndDateMillis != null
                ) {
                    Text(
                        text = "Confirmar",
                        color = BrandPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = SurfaceWhite
            )
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        text = "Rango de fechas de tu viaje",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
                    )
                },
                headline = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    val text = if (start != null && end != null) {
                        val s = Instant.ofEpochMilli(start).atZone(ZoneId.of("UTC")).toLocalDate()
                        val e = Instant.ofEpochMilli(end).atZone(ZoneId.of("UTC")).toLocalDate()
                        val days = ChronoUnit.DAYS.between(s, e) + 1
                        "$s - $e ($days días)"
                    } else if (start != null) {
                        val s = Instant.ofEpochMilli(start).atZone(ZoneId.of("UTC")).toLocalDate()
                        "Inicio: $s (Selecciona fin)"
                    } else {
                        "Selecciona inicio y fin"
                    }
                    Text(
                        text = text,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
                    )
                },
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = BrandPrimary,
                    selectedDayContentColor = Color.White,
                    dayInSelectionRangeContainerColor = BrandLight,
                    dayInSelectionRangeContentColor = BrandPrimary,
                    todayDateBorderColor = BrandPrimary
                )
            )
        }
    }
}
