package dev.bgamarra.loteriaapp.presentation.auth

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.bgamarra.loteriaapp.remote.FirebaseAuthManager
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    val scope = rememberCoroutineScope()

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña (mínimo 6 caracteres)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmacion,
            onValueChange = { confirmacion = it },
            label = { Text("Confirmar contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        mensajeError?.let {
            Spacer(Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val correoLimpio = correo.trim()
                when {
                    correoLimpio.isEmpty() || contrasena.isEmpty() || confirmacion.isEmpty() ->
                        mensajeError = "Completa todos los campos."

                    !Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches() ->
                        mensajeError = "Ingresa un correo válido."

                    contrasena.length < 6 ->
                        mensajeError = "La contraseña debe tener al menos 6 caracteres."

                    contrasena != confirmacion ->
                        mensajeError = "Las contraseñas no coinciden."

                    else -> {
                        mensajeError = null
                        cargando = true
                        scope.launch {
                            val resultado = FirebaseAuthManager.registerUser(correoLimpio, contrasena)
                            cargando = false
                            resultado
                                .onSuccess {
                                    navController.navigate("play") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                                .onFailure { mensajeError = traducirErrorAuth(it) }
                        }
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Crear cuenta")
            }
        }

        TextButton(onClick = { navController.popBackStack() }, enabled = !cargando) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}