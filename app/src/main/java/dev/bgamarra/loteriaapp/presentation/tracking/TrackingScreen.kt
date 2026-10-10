package dev.bgamarra.loteriaapp.presentation.tracking

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.bgamarra.loteriaapp.remote.FirebaseAuthManager
import dev.bgamarra.loteriaapp.remote.LotteryFirestoreManager
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TrackingScreen(
    navController: NavController
) {

    val context = LocalContext.current
    val user = FirebaseAuthManager.getCurrentUser()

    var nombreSorteo by remember {
        mutableStateOf("")
    }

    var fechaSorteo by remember {
        mutableStateOf("")
    }

    var estadoSorteo by remember {
        mutableStateOf("abierto")
    }

    val jugada = remember {
        mutableStateListOf<Int>()
    }

    val bolillas = remember {
        mutableStateListOf<Int>()
    }


    // Cargar información inicial
    LaunchedEffect(Unit) {

        LotteryFirestoreManager
            .getSorteoInfo()
            .onSuccess { sorteo ->

                nombreSorteo = sorteo.nombre

                fechaSorteo = sorteo.fecha?.let { timestamp ->

                    val formato = SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                    )

                    formato.format(timestamp.toDate())

                } ?: ""

                estadoSorteo = sorteo.estado
            }


        val uid = user?.uid

        if (uid != null) {

            LotteryFirestoreManager
                .getJugada(uid)
                .onSuccess { numeros ->

                    jugada.clear()

                    if (numeros != null) {
                        jugada.addAll(numeros)
                    }
                }
        }
    }


    // LISTENERS EN TIEMPO REAL
    DisposableEffect(Unit) {

        val estadoListener =
            LotteryFirestoreManager
                .escucharEstadoSorteo(

                    onChange = { nuevoEstado ->

                        estadoSorteo = nuevoEstado
                    },

                    onError = { error ->

                        Toast.makeText(
                            context,
                            error.message ?: "Error al escuchar el sorteo",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )


        val bolillasListener =
            LotteryFirestoreManager
                .escucharBolillas(

                    onChange = { nuevasBolillas ->

                        bolillas.clear()
                        bolillas.addAll(nuevasBolillas)
                    },

                    onError = { error ->

                        Toast.makeText(
                            context,
                            error.message ?: "Error al escuchar bolillas",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )


        // Eliminar listeners al salir de la pantalla
        onDispose {

            estadoListener.remove()
            bolillasListener.remove()
        }
    }


    // CALCULAR ACIERTOS
    val aciertos = jugada.count { numero ->

        numero in bolillas
    }


    // CALCULAR RESULTADO
    val resultado = when {

        estadoSorteo == "abierto" -> {

            "Esperando que inicie el sorteo"
        }

        estadoSorteo == "en curso" -> {

            "Sorteo en curso"
        }

        estadoSorteo == "finalizado" &&
                bolillas.size < 6 -> {

            "Esperando resultados"
        }

        estadoSorteo == "finalizado" &&
                bolillas.size == 6 &&
                aciertos == 6 -> {

            "GANADOR"
        }

        estadoSorteo == "finalizado" &&
                bolillas.size == 6 -> {

            "SIGUE INTENTANDO"
        }

        else -> {

            "Esperando resultados"
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = nombreSorteo,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = fechaSorteo
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Usuario: ${user?.email ?: ""}"
        )

        Text(
            text = "Estado: $estadoSorteo"
        )


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        Text(
            text = "Bolillas publicadas",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            bolillas.forEach { numero ->

                NumeroBox(
                    numero = numero,
                    acertado = false
                )
            }
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        Text(
            text = "Tu jugada",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            jugada.forEach { numero ->

                NumeroBox(
                    numero = numero,
                    acertado = numero in bolillas
                )
            }
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Bolillas publicadas: ${bolillas.size} / 6"
                )

                Text(
                    text = "Aciertos: $aciertos / 6"
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = resultado,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        Button(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Volver")
        }
    }
}


@Composable
fun NumeroBox(
    numero: Int,
    acertado: Boolean
) {

    Surface(
        color = if (acertado) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        shape = MaterialTheme.shapes.medium
    ) {

        Text(
            text = if (acertado) {
                "✓ $numero"
            } else {
                numero.toString()
            },
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 8.dp
            )
        )
    }
}