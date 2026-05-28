package com.bpo.gasapp.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromoCodeRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    sealed class Result {
        data object Success : Result()
        data object NotLoggedIn : Result()
        data object InvalidCode : Result()
        data object AlreadyUsed : Result()
        data class Error(val message: String) : Result()
    }

    /** Canjea un código promocional. La transacción es atómica en Firestore. */
    suspend fun redeem(rawCode: String): Result {
        val code = rawCode.trim().uppercase()
        if (code.isEmpty()) return Result.InvalidCode
        val user = auth.currentUser ?: return Result.NotLoggedIn

        return try {
            firestore.runTransaction { tx ->
                val ref = firestore.collection("promoCodes").document(code)
                val snap = tx.get(ref)
                if (!snap.exists()) {
                    throw IllegalStateException("INVALID")
                }
                if (snap.getBoolean("used") == true) {
                    throw IllegalStateException("USED")
                }
                tx.update(
                    ref,
                    mapOf(
                        "used" to true,
                        "usedByUid" to user.uid,
                        "redeemedAt" to System.currentTimeMillis()
                    )
                )
            }.await()
            Result.Success
        } catch (e: IllegalStateException) {
            when (e.message) {
                "INVALID" -> Result.InvalidCode
                "USED" -> Result.AlreadyUsed
                else -> Result.Error(e.message ?: "Error")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        }
    }
}
