package dev.bgamarra.loteriaapp.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

data class SorteoInfo(
    val nombre: String = "",
    val fecha: Timestamp? = null,
    val estado: String = "abierto"
)

object LotteryFirestoreManager {

    private val firestore = FirebaseFirestore.getInstance()

    const val SORTEO_ID = "sorteo-prueba"


    suspend fun getSorteoInfo(): Result<SorteoInfo> {

        return try {

            val document = firestore
                .collection("sorteos")
                .document(SORTEO_ID)
                .get()
                .await()

            val sorteo = SorteoInfo(
                nombre = document.getString("nombre") ?: "",
                fecha = document.getTimestamp("fecha"),
                estado = document.getString("estado") ?: "abierto"
            )

            Result.success(sorteo)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    suspend fun guardarJugada(
        uid: String,
        numeros: List<Int>
    ): Result<Unit> {

        return try {

            if (numeros.size != 6) {
                return Result.failure(
                    Exception("Debes ingresar exactamente 6 números")
                )
            }

            if (numeros.any { it !in 1..45 }) {
                return Result.failure(
                    Exception("Los números deben estar entre 1 y 45")
                )
            }

            if (numeros.distinct().size != 6) {
                return Result.failure(
                    Exception("Los números no pueden repetirse")
                )
            }

            val sorteoDocument = firestore
                .collection("sorteos")
                .document(SORTEO_ID)
                .get()
                .await()

            val estado =
                sorteoDocument.getString("estado") ?: "abierto"

            if (estado != "abierto") {

                return Result.failure(
                    Exception("El sorteo ya comenzó")
                )
            }

            val jugada = hashMapOf(
                "sorteoId" to SORTEO_ID,
                "uid" to uid,
                "numeros" to numeros
            )

            firestore
                .collection("sorteos")
                .document(SORTEO_ID)
                .collection("jugadas")
                .document(uid)
                .set(jugada)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    suspend fun getJugada(
        uid: String
    ): Result<List<Int>?> {

        return try {

            val document = firestore
                .collection("sorteos")
                .document(SORTEO_ID)
                .collection("jugadas")
                .document(uid)
                .get()
                .await()

            if (!document.exists()) {

                Result.success(null)

            } else {

                val numeros = (
                        document.get("numeros") as? List<*>
                        )
                    ?.mapNotNull {
                        (it as? Number)?.toInt()
                    }

                Result.success(numeros)
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    fun escucharEstadoSorteo(
        onChange: (String) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {

        return firestore
            .collection("sorteos")
            .document(SORTEO_ID)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {

                    onError(error)
                    return@addSnapshotListener
                }

                val estado =
                    snapshot?.getString("estado") ?: "abierto"

                onChange(estado)
            }
    }


    fun escucharBolillas(
        onChange: (List<Int>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {

        return firestore
            .collection("sorteos")
            .document(SORTEO_ID)
            .collection("bolillas")
            .orderBy("orden")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {

                    onError(error)
                    return@addSnapshotListener
                }

                val bolillas = snapshot
                    ?.documents
                    ?.mapNotNull {
                        it.getLong("numero")?.toInt()
                    }
                    ?: emptyList()

                onChange(bolillas)
            }
    }
}