package com.tripflow.android.ui.trips

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.android.data.local.TripFlowDatabase
import com.tripflow.android.data.local.entity.TripEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class TripStatus {
    ACTIVE,    // En curso
    UPCOMING,  // Próximo
    FINISHED   // Finalizado
}

enum class TripFilter(val displayName: String) {
    ALL("Todos"),
    ACTIVE("Actuales"),
    UPCOMING("Próximos"),
    FINISHED("Finalizados")
}

data class TripItemUiModel(
    val trip: TripEntity,
    val status: TripStatus,
    val spentAmount: Double,
    val progress: Float,
    val isOverBudget: Boolean,
    val formattedDates: String,
    val daysCount: Long
)

sealed interface NewTripSaveState {
    object Idle : NewTripSaveState
    object Loading : NewTripSaveState
    object Success : NewTripSaveState
    data class Error(val message: String) : NewTripSaveState
}

class TripsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TripFlowDatabase.getDatabase(application)
    private val tripDao = database.tripDao()
    private val expenseDao = database.expenseDao()

    private val _userId = MutableStateFlow<Long?>(null)
    val userId: StateFlow<Long?> = _userId.asStateFlow()

    private val _selectedFilter = MutableStateFlow(TripFilter.ALL)
    val selectedFilter: StateFlow<TripFilter> = _selectedFilter.asStateFlow()

    // Estado del buscador de destino con Nominatim
    private val _destinationQuery = MutableStateFlow("")
    val destinationQuery: StateFlow<String> = _destinationQuery.asStateFlow()

    private val _destinationSuggestions = MutableStateFlow<List<DestinationSuggestion>>(emptyList())
    val destinationSuggestions: StateFlow<List<DestinationSuggestion>> = _destinationSuggestions.asStateFlow()

    private val _isSearchingDestinations = MutableStateFlow(false)
    val isSearchingDestinations: StateFlow<Boolean> = _isSearchingDestinations.asStateFlow()

    // Estado de guardado de nuevo viaje
    private val _saveState = MutableStateFlow<NewTripSaveState>(NewTripSaveState.Idle)
    val saveState: StateFlow<NewTripSaveState> = _saveState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val id = TripSessionManager.getActiveUserId(application)
            _userId.value = id
        }
    }

    /**
     * Flujo de viajes del usuario activo junto con cálculo reactivo de gastos y estados
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val trips: StateFlow<List<TripItemUiModel>> = _userId
        .filterNotNull()
        .flatMapLatest { uid ->
            tripDao.getTripsByUser(uid).flatMapLatest { tripsList ->
                if (tripsList.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    // Para cada viaje, combinamos sus gastos en tiempo real
                    val flows = tripsList.map { trip ->
                        expenseDao.getExpensesByTrip(trip.id).map { expenses ->
                            val spent = expenses.sumOf { it.amount }
                            mapToUiModel(trip, spent)
                        }
                    }
                    combine(flows) { array -> array.toList() }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Lista filtrada de viajes según el chip seleccionado
     */
    val filteredTrips: StateFlow<List<TripItemUiModel>> = combine(trips, _selectedFilter) { list, filter ->
        when (filter) {
            TripFilter.ALL -> list
            TripFilter.ACTIVE -> list.filter { it.status == TripStatus.ACTIVE }
            TripFilter.UPCOMING -> list.filter { it.status == TripStatus.UPCOMING }
            TripFilter.FINISHED -> list.filter { it.status == TripStatus.FINISHED }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: TripFilter) {
        _selectedFilter.value = filter
    }

    /**
     * Búsqueda en vivo de ciudades con autocompletado en Nominatim y debouncing
     */
    fun onDestinationQueryChanged(query: String) {
        _destinationQuery.value = query
        searchJob?.cancel()

        if (query.trim().length < 2) {
            _destinationSuggestions.value = emptyList()
            _isSearchingDestinations.value = false
            return
        }

        searchJob = viewModelScope.launch {
            _isSearchingDestinations.value = true
            delay(350) // Debounce para no saturar la API
            val results = NominatimService.searchDestinations(query)
            _destinationSuggestions.value = results
            _isSearchingDestinations.value = false
        }
    }

    fun clearSuggestions() {
        _destinationSuggestions.value = emptyList()
        _isSearchingDestinations.value = false
    }

    fun resetSaveState() {
        _saveState.value = NewTripSaveState.Idle
    }

    /**
     * Valida solapamiento de fechas con los viajes ya existentes del usuario
     */
    fun findOverlappingTrip(startDateStr: String, endDateStr: String): TripEntity? {
        val newStart = parseDate(startDateStr) ?: return null
        val newEnd = parseDate(endDateStr) ?: return null

        val currentList = trips.value.map { it.trip }
        return currentList.firstOrNull { trip ->
            val tripStart = parseDate(trip.startDate)
            val tripEnd = parseDate(trip.endDate)
            if (tripStart != null && tripEnd != null) {
                !newStart.isAfter(tripEnd) && !newEnd.isBefore(tripStart)
            } else {
                false
            }
        }
    }

    /**
     * Guarda un nuevo viaje en la base de datos local Room
     */
    fun createTrip(
        destination: String,
        startDate: String,
        endDate: String,
        budgetLimit: Double,
        currency: String,
        onSuccess: () -> Unit
    ) {
        val trimmedDest = destination.trim()
        if (trimmedDest.isEmpty()) {
            _saveState.value = NewTripSaveState.Error("Ingresa un destino para tu viaje")
            return
        }

        val start = parseDate(startDate)
        val end = parseDate(endDate)
        if (start == null || end == null) {
            _saveState.value = NewTripSaveState.Error("Selecciona el rango completo de fechas")
            return
        }
        if (end.isBefore(start)) {
            _saveState.value = NewTripSaveState.Error("La fecha de fin no puede ser anterior a la de inicio")
            return
        }

        if (budgetLimit <= 0.0) {
            _saveState.value = NewTripSaveState.Error("El presupuesto debe ser mayor a 0")
            return
        }

        // Validación de solapamiento
        val overlap = findOverlappingTrip(startDate, endDate)
        if (overlap != null) {
            _saveState.value = NewTripSaveState.Error(
                "Ya tienes un viaje a \"${overlap.destination}\" del ${overlap.startDate} al ${overlap.endDate} que coincide con estas fechas."
            )
            return
        }

        viewModelScope.launch {
            _saveState.value = NewTripSaveState.Loading
            try {
                val currentUid = _userId.value ?: TripSessionManager.getActiveUserId(getApplication())
                val newTrip = TripEntity(
                    userId = currentUid,
                    destination = trimmedDest,
                    startDate = startDate,
                    endDate = endDate,
                    budgetLimit = budgetLimit,
                    currency = currency
                )
                val id = tripDao.insertTrip(newTrip)
                if (id > 0) {
                    _saveState.value = NewTripSaveState.Success
                    onSuccess()
                } else {
                    _saveState.value = NewTripSaveState.Error("No se pudo registrar el viaje.")
                }
            } catch (t: Throwable) {
                _saveState.value = NewTripSaveState.Error(t.localizedMessage ?: "Error al guardar el viaje")
            }
        }
    }

    /**
     * Elimina un viaje de la base de datos local
     */
    fun deleteTrip(trip: TripEntity) {
        viewModelScope.launch {
            try {
                tripDao.deleteTrip(trip)
            } catch (_: Exception) {
                // Manejo silencioso o notificación
            }
        }
    }

    private fun mapToUiModel(trip: TripEntity, spent: Double): TripItemUiModel {
        val status = computeStatus(trip.startDate, trip.endDate)
        val days = computeDays(trip.startDate, trip.endDate)
        val progress = if (trip.budgetLimit > 0.0) {
            (spent / trip.budgetLimit).coerceIn(0.0, 1.0).toFloat()
        } else {
            0f
        }
        val isOverBudget = spent > trip.budgetLimit

        return TripItemUiModel(
            trip = trip,
            status = status,
            spentAmount = spent,
            progress = progress,
            isOverBudget = isOverBudget,
            formattedDates = formatDatesRange(trip.startDate, trip.endDate),
            daysCount = days
        )
    }

    companion object {
        fun parseDate(dateStr: String): LocalDate? {
            return try {
                LocalDate.parse(dateStr)
            } catch (_: Exception) {
                null
            }
        }

        fun computeStatus(startDateStr: String, endDateStr: String): TripStatus {
            val today = LocalDate.now()
            val start = parseDate(startDateStr) ?: return TripStatus.UPCOMING
            val end = parseDate(endDateStr) ?: return TripStatus.UPCOMING

            return when {
                today.isBefore(start) -> TripStatus.UPCOMING
                today.isAfter(end) -> TripStatus.FINISHED
                else -> TripStatus.ACTIVE
            }
        }

        fun computeDays(startDateStr: String, endDateStr: String): Long {
            val start = parseDate(startDateStr) ?: return 1
            val end = parseDate(endDateStr) ?: return 1
            val days = ChronoUnit.DAYS.between(start, end) + 1
            return if (days > 0) days else 1
        }

        fun formatDatesRange(startStr: String, endStr: String): String {
            val start = parseDate(startStr)
            val end = parseDate(endStr)
            if (start == null || end == null) return "$startStr - $endStr"

            val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale("es", "ES"))
            val startMonth = start.format(monthFormatter).replace(".", "")
            val endMonth = end.format(monthFormatter).replace(".", "")

            return if (start.year == end.year) {
                if (start.monthValue == end.monthValue) {
                    "${start.dayOfMonth} - ${end.dayOfMonth} $endMonth ${start.year}"
                } else {
                    "${start.dayOfMonth} $startMonth - ${end.dayOfMonth} $endMonth ${start.year}"
                }
            } else {
                "${start.dayOfMonth} $startMonth ${start.year} - ${end.dayOfMonth} $endMonth ${end.year}"
            }
        }
    }
}
