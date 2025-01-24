package com.example.swipeloacationcamera

import com.example.swipeloacationcamera.data.SupabaseConn
import com.example.swipeloacationcamera.domain.UserRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {


    @Test
    fun success_sign_in() = runTest {
        SupabaseConn.supabase.auth.clearSession()
        SupabaseConn.supabase.auth.signInWith(Email){
            email = "testuser@gmail.com"
            password = "User1235678!"
        }
        val result = SupabaseConn.supabase.auth.currentUserOrNull()
        assertEquals("testuser@gmail.com", result?.email)
    }
}