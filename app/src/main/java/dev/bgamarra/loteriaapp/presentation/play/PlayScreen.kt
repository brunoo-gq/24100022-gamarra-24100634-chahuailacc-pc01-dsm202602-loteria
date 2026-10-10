package dev.bgamarra.loteriaapp.presentation.play

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.bgamarra.loteriaapp.remote.FirebaseAuthManager
import dev.bgamarra.loteriaapp.remote.LotteryFirestoreManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PlayScreen(
    navController: NavController
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val user =
        FirebaseAuthManager.getCurrentUser()

    val numeros = remember {
        mutableStateListOf("", "", "", "", "", "")
    }

    var nombreSorteo by remember {
        mutableStateOf("")
    }

    var fechaSorteo by remember {
        mutableStateOf("")
    }

    var estadoSorteo by remember {
        mutableStateOf("abierto")
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    var tieneJugada by remember {
        mutableStateOf(false)
    }


    LaunchedEffect(Unit) {

        val sorteoResult =
            LotteryFirestoreManager.getSorteoInfo()

        sorteoResult.onSuccess { sorteo ->

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

            val jugadaResult =
                LotteryFirestoreManager.getJugada(uid)

            jugadaResult.onSuccess { jugada ->

                if (jugada != null) {

                    tieneJugada = true

                    jugada.forEachIndexed { index, numero ->

                        if (index < 6) {
                            numeros[index] = numero.toString()
                        }
                    }
                }
            }
        }

        cargando = false
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Registrar jugada",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = nombreSorteo
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
            modifier = Modifier.height(20.dp)
        )


        numeros.forEachIndexed { index, valor ->

            OutlinedTextField(
                value = valor,

                onValueChange = { nuevoValor ->

                    if (
                        nuevoValor.length <= 2 &&
                        nuevoValor.all { it.isDigit() }
                    ) {

                        numeros[index] = nuevoValor
                    }
                },

                label = {
                    Text("Número ${index + 1}")
                },

                enabled = estadoSorteo == "abierto",

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        Button(
            modifier = Modifier.fillMaxWidth(),

            enabled =
                !cargando &&
                        estadoSorteo == "abierto",

            onClick = {

                val numerosConvertidos =
                    numeros.mapNotNull {
                        it.toIntOrNull()
                    }


                if (numerosConvertidos.size != 6) {

                    Toast.makeText(
                        context,
                        "Debes ingresar los 6 números",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }


                if (
                    numerosConvertidos.any {
                        it !in 1..45
                    }
                ) {

                    Toast.makeText(
                        context,
                        "Los números deben estar entre 1 y 45",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }


                if (
                    numerosConvertidos.distinct().size != 6
                ) {

                    Toast.makeText(
                        context,
                        "No puedes repetir números",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }


                val uid = user?.uid

                if (uid == null) {

                    Toast.makeText(
                        context,
                        "No hay usuario autenticado",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }


                scope.launch {

                    val result =
                        LotteryFirestoreManager.guardarJugada(
                            uid = uid,
                            numeros = numerosConvertidos
                        )

                    result.onSuccess {

                        Toast.makeText(
                            context,
                            "Jugada guardada correctamente",
                            Toast.LENGTH_SHORT
                        ).show()

                        navController.navigate("tracking")
                    }


                    result.onFailure {

                        Toast.makeText(
                            context,
                            it.message ?: "Error al guardar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        ) {

            Text(
                if (tieneJugada)
                    "Actualizar jugada"
                else
                    "Registrar jugada"
            )
        }


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Button(
            onClick = {
                navController.navigate("tracking")
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Ver sorteo")
        }
    }
}