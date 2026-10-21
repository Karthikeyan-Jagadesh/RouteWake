package com.routewake.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.routewake.app.data.repository.AuthRepository
import com.routewake.app.theme.*
import com.routewake.app.ui.components.RouteWakeButton
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    authRepository: AuthRepository,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    RegisterContent(
        isLoading = isLoading,
        errorMessage = errorMessage,
        onRegisterSubmit = { fullName, email, password, confirmPassword ->
            if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                errorMessage = "All fields are required"
                return@RegisterContent
            }
            if (password != confirmPassword) {
                errorMessage = "Passwords do not match"
                return@RegisterContent
            }
            if (password.length < 6) {
                errorMessage = "Password must be at least 6 characters"
                return@RegisterContent
            }

            isLoading = true
            errorMessage = null
            scope.launch {
                val result = authRepository.register(fullName, email, password)
                isLoading = false
                result.onSuccess {
                    onRegisterSuccess()
                }.onFailure { ex ->
                    errorMessage = ex.message ?: "Registration failed"
                }
            }
        },
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
fun RegisterContent(
    isLoading: Boolean,
    errorMessage: String?,
    onRegisterSubmit: (String, String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Join RouteWake to configure personal travel alarm preferences",
            style = MaterialTheme.typography.bodyMedium,
            color = RouteWakeTextSecondaryDark
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (errorMessage != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = RouteWakeErrorRed.copy(alpha = 0.15f),
                border = null
            ) {
                Text(
                    text = errorMessage!!,
                    color = RouteWakeErrorRed,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = RouteWakeAccentLime) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = RouteWakeAccentLime) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RouteWakeAccentLime) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RouteWakeAccentLime) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RouteWakeAccentLime)
            }
        } else {
            RouteWakeButton(
                text = "Register",
                onClick = {
                    onRegisterSubmit(fullName, email, password, confirmPassword)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium,
                color = RouteWakeTextSecondaryDark
            )
            Text(
                text = "Log In",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = RouteWakeAccentLime,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Register Screen (Dark)",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun RegisterScreenDarkPreview() {
    RouteWakeTheme(darkTheme = true) {
        RegisterContent(
            isLoading = false,
            errorMessage = null,
            onRegisterSubmit = { _, _, _, _ -> },
            onNavigateToLogin = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Register Screen - Error State (Light)",
    showBackground = true
)
@Composable
private fun RegisterScreenErrorLightPreview() {
    RouteWakeTheme(darkTheme = false) {
        RegisterContent(
            isLoading = false,
            errorMessage = "Passwords do not match. Please verify your entries.",
            onRegisterSubmit = { _, _, _, _ -> },
            onNavigateToLogin = {}
        )
    }
}

