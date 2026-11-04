package com.tripflow.android.ui.trips

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Representa una sugerencia de destino turístico obtenida de Nominatim (OpenStreetMap)
 * o de las sugerencias populares offline.
 */
data class DestinationSuggestion(
    val cityName: String,
    val countryName: String,
    val fullFormatted: String, // ej: "París, Francia"
    val subtitle: String       // ej: "Isla de Francia, Francia"
)

/**
 * Servicio para consultar la API de OpenStreetMap Nominatim con soporte de autocompletado en vivo,
 * extracción de ciudad y país, y catálogo de respaldo offline.
 */
object NominatimService {

    private const val BASE_URL = "https://nominatim.openstreetmap.org/search"
    private const val USER_AGENT = "TripFlow-Android-App/1.0"

    // Destinos populares turísticos como respaldo offline y sugerencias instantáneas
    private val POPULAR_DESTINATIONS = listOf(
        DestinationSuggestion("París", "Francia", "París, Francia", "Isla de Francia"),
        DestinationSuggestion("Madrid", "España", "Madrid, España", "Comunidad de Madrid"),
        DestinationSuggestion("Barcelona", "España", "Barcelona, España", "Cataluña"),
        DestinationSuggestion("Roma", "Italia", "Roma, Italia", "Lazio"),
        DestinationSuggestion("Londres", "Reino Unido", "Londres, Reino Unido", "Inglaterra"),
        DestinationSuggestion("Nueva York", "Estados Unidos", "Nueva York, Estados Unidos", "Nueva York"),
        DestinationSuggestion("Miami", "Estados Unidos", "Miami, Estados Unidos", "Florida"),
        DestinationSuggestion("Cancún", "México", "Cancún, México", "Quintana Roo"),
        DestinationSuggestion("Ciudad de México", "México", "Ciudad de México, México", "CDMX"),
        DestinationSuggestion("Cartagena", "Colombia", "Cartagena, Colombia", "Bolívar"),
        DestinationSuggestion("Bogotá", "Colombia", "Bogotá, Colombia", "Cundinamarca"),
        DestinationSuggestion("Medellín", "Colombia", "Medellín, Colombia", "Antioquia"),
        DestinationSuggestion("Buenos Aires", "Argentina", "Buenos Aires, Argentina", "Capital Federal"),
        DestinationSuggestion("Río de Janeiro", "Brasil", "Río de Janeiro, Brasil", "Río de Janeiro"),
        DestinationSuggestion("Santiago", "Chile", "Santiago, Chile", "Región Metropolitana"),
        DestinationSuggestion("Lima", "Perú", "Lima, Perú", "Lima"),
        DestinationSuggestion("Tokio", "Japón", "Tokio, Japón", "Kanto"),
        DestinationSuggestion("Ámsterdam", "Países Bajos", "Ámsterdam, Países Bajos", "Holanda Septentrional"),
        DestinationSuggestion("Berlín", "Alemania", "Berlín, Alemania", "Berlín"),
        DestinationSuggestion("Sídney", "Australia", "Sídney, Australia", "Nueva Gales del Sur")
    )

    /**
     * Busca destinos que coincidan con la consulta. Prioriza la API en vivo de Nominatim
     * y utiliza la lista precargada si no hay conexión o la API no responde.
     */
    suspend fun searchDestinations(query: String): List<DestinationSuggestion> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()

        val results = mutableListOf<DestinationSuggestion>()

        try {
            val encodedQuery = URLEncoder.encode(trimmed, "UTF-8")
            val urlString = "$BASE_URL?q=$encodedQuery&format=json&addressdetails=1&limit=6&accept-language=es"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "application/json")
                connectTimeout = 3000
                readTimeout = 3000
            }

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(responseText)

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val address = item.optJSONObject("address")
                    val name = item.optString("name")
                    val displayName = item.optString("display_name")

                    val city = address?.optString("city")?.takeIf { it.isNotBlank() }
                        ?: address?.optString("town")?.takeIf { it.isNotBlank() }
                        ?: address?.optString("village")?.takeIf { it.isNotBlank() }
                        ?: address?.optString("municipality")?.takeIf { it.isNotBlank() }
                        ?: address?.optString("state_district")?.takeIf { it.isNotBlank() }
                        ?: name

                    val country = address?.optString("country")?.trim() ?: ""
                    val state = address?.optString("state")?.trim() ?: ""

                    // Formatear: "Ciudad, País"
                    val fullFormatted = if (country.isNotBlank() && !city.contains(country, ignoreCase = true)) {
                        "$city, $country"
                    } else {
                        city
                    }

                    val subtitle = if (state.isNotBlank() && country.isNotBlank() && !state.equals(city, ignoreCase = true)) {
                        "$state, $country"
                    } else {
                        displayName
                    }

                    if (results.none { it.fullFormatted.equals(fullFormatted, ignoreCase = true) }) {
                        results.add(
                            DestinationSuggestion(
                                cityName = city,
                                countryName = country,
                                fullFormatted = fullFormatted,
                                subtitle = subtitle
                            )
                        )
                    }
                }
            }
        } catch (_: Throwable) {
            // Si hay fallo de red o timeout, se recurre a la lista offline
        }

        // Si la API no devolvió resultados o falló, filtrar catálogo popular offline
        if (results.isEmpty()) {
            val matchingFallbacks = POPULAR_DESTINATIONS.filter {
                it.fullFormatted.contains(trimmed, ignoreCase = true) ||
                it.cityName.contains(trimmed, ignoreCase = true) ||
                it.countryName.contains(trimmed, ignoreCase = true)
            }
            results.addAll(matchingFallbacks)
        }

        results
    }
}
