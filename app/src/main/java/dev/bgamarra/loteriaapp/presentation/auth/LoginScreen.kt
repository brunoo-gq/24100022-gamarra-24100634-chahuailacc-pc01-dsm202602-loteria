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
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dev.bgamarra.loteriaapp.remote.FirebaseAuthManager
import kotlinx.coroutines.launch
import androidx.navigation.NavController

// Convierte la excepción de Firebase en un mensaje en español.
// Es internal para reutilizarla desde RegisterScreen.
internal fun traducirErrorAuth(e: Throwable): String = when (e) {
    is FirebaseAuthWeakPasswordException -> "La contraseña debe tener al menos 6 caracteres."
    is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo."
    is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo."
    is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos."
    is FirebaseNetworkException -> "Sin conexión. Revisa tu internet."
    else -> "Ocurrió un error: ${e.localizedMessage ?: "desconocido"}"
}

@Composable
fun LoginScreen(navController: NavController) {
    val scope = rememberCoroutineScope()

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
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
            text = "Iniciar sesión",
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
            label = { Text("Contraseña") },
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
                    correoLimpio.isEmpty() || contrasena.isEmpty() ->
                        mensajeError = "Completa el correo y la contraseña."

                    !Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches() ->
                        mensajeError = "Ingresa un correo válido."

                    else -> {
                        mensajeError = null
                        cargando = true
                        scope.launch {
                            val resultado = FirebaseAuthManager.loginUser(correoLimpio, contrasena)
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
                Text("Ingresar")
            }
        }

        TextButton(onClick = { navController.navigate("register") }, enabled = !cargando) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}