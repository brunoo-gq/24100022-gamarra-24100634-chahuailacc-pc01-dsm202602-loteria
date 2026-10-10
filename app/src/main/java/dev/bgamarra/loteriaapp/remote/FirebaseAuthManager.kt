package dev.bgamarra.loteriaapp.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun registerUser(
        email: String,
        password: String
    ): Result<Unit> {

        return try {

            val result = auth.createUserWithEmailAndPassword(
                email,
                password
            ).await()

            val uid = result.user?.uid
                ?: return Result.failure(
                    Exception("No se pudo obtener el usuario")
                )

            val userData = hashMapOf(
                "uid" to uid,
                "email" to email
            )

            firestore
                .collection("users")
                .document(uid)
                .set(userData)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun loginUser(
        email: String,
        password: String
    ): Result<Unit> {

        return try {

            auth.signInWithEmailAndPassword(
                email,
                password
            ).await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    fun getCurrentUser() =
        auth.currentUser

    fun logout() {
        auth.signOut()
    }
}