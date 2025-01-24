package com.example.swipeloacationcamera.domain

import com.example.swipeloacationcamera.data.SupabaseConn
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlin.system.measureTimeMillis

class UserRepository {
    suspend fun signIn(_email: String, _password: String):UserInfo?{
        try {
            SupabaseConn.supabase.auth.awaitInitialization()
            SupabaseConn.supabase.auth.clearSession()
            SupabaseConn.supabase.auth.signInWith(Email){
                email = _email
                password = _password
            }
            return SupabaseConn.supabase.auth.currentUserOrNull()
        }catch (ex : Exception){
            return null
        }
    }
}