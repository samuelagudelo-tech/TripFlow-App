package com.tripflow.android.data.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    // Configurar con las credenciales de tu proyecto Supabase
    private const val SUPABASE_URL = "https://vaqggjxwlpofxnizlqyc.supabase.co"
    private const val SUPABASE_KEY = "TRIPFLOWANDROID"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}
