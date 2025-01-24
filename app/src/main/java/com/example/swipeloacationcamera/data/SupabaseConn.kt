package com.example.swipeloacationcamera.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConn {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://itlugemtbukmerysxuma.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0bHVnZW10YnVrbWVyeXN4dW1hIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzc3MTMxOTcsImV4cCI6MjA1MzI4OTE5N30.jJBDdAo0wJW9V4u7bhBdN-MG-XwFJ1B_cMReuqOsSjs"
    ) {
        install(Auth)
        install(Postgrest)

        //install other modules
    }
}