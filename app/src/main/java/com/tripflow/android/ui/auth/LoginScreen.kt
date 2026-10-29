package com.tripflow.android.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripflow.android.ui.theme.BackgroundLight
import com.tripflow.android.ui.theme.BrandPrimary
import com.tripflow.android.ui.theme.DestructiveRed
import com.tripflow.android.ui.theme.TextPrimary
import com.tripflow.android.ui.theme.TextSecondary

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    Scaffold(
        containerColor = BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Logotipo de TripFlow
            TripFlowLogoHeader()

            Spacer(modifier = Modifier.height(28.dp))

            // 2. Título y Subtítulo
            Text(
                text = "Bienvenido de nuevo",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tus próximos viajes te esperan. Inicia sesión y mantén tu presupuesto bajo control.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Campo Correo electrónico
            TripFlowTextField(
                label = "Correo electrónico",
                value = email,
                onValueChange = {
                    email = it
                    if (uiState is AuthUiState.Error) viewModel.resetState()
                },
                placeholder = "nombre@correo.com",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Campo Contraseña
            TripFlowPasswordField(
                label = "Contraseña",
                value = password,
                onValueChange = {
                    password = it
                    if (uiState is AuthUiState.Error) viewModel.resetState()
                },
                placeholder = "Ingresa tu contraseña",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.signIn(email, password, onLoginSuccess)
                    }
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Enlace ¿Olvidaste tu contraseña?
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrandPrimary,
                    modifier = Modifier.clickable {
                        // Acción de recuperación de contraseña
                    }
                )
            }

            // Mensaje de error si la autenticación falla
            if (uiState is AuthUiState.Error) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = DestructiveRed,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Botón Iniciar sesión
            TripFlowPrimaryButton(
                text = "Iniciar sesión",
                isLoading = uiState is AuthUiState.Loading,
                onClick = {
                    focusManager.clearFocus()
                    viewModel.signIn(email, password, onLoginSuccess)
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 7. Enlace a Registro
            val annotatedRegisterText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextSecondary, fontSize = 14.sp)) {
                    append("¿No tienes una cuenta? ")
                }
                withStyle(
                    style = SpanStyle(
                        color = BrandPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("Regístrate")
                }
            }

            Text(
                text = annotatedRegisterText,
                modifier = Modifier.clickable { onNavigateToRegister() },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // 8. Tarjeta promocional inferior
            TripFlowPromoCard()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
