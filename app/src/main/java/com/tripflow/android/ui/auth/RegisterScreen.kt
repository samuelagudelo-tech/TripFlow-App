package com.tripflow.android.ui.auth

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripflow.android.ui.theme.BackgroundLight
import com.tripflow.android.ui.theme.BrandPrimary
import com.tripflow.android.ui.theme.DestructiveRed
import com.tripflow.android.ui.theme.TextPrimary
import com.tripflow.android.ui.theme.TextSecondary
import com.tripflow.android.ui.theme.TripFlowTheme

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

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

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Título y Subtítulo
            Text(
                text = "Crea tu cuenta",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Empieza a planear tus viajes y disfruta de cada destino sin salirte del presupuesto.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Campo Nombre Completo
            TripFlowTextField(
                label = "Nombre completo",
                value = fullName,
                onValueChange = {
                    fullName = it
                    if (uiState is AuthUiState.Error) viewModel.resetState()
                },
                placeholder = "Tu nombre y apellido",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Campo Correo electrónico
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

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Campo Contraseña
            TripFlowPasswordField(
                label = "Contraseña",
                value = password,
                onValueChange = {
                    password = it
                    if (uiState is AuthUiState.Error) viewModel.resetState()
                },
                placeholder = "Crea una contraseña",
                helperText = "Usa al menos 8 caracteres, una letra y un número.",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Campo Confirmar Contraseña
            TripFlowPasswordField(
                label = "Confirmar contraseña",
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (uiState is AuthUiState.Error) viewModel.resetState()
                },
                placeholder = "Repite tu contraseña",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.signUp(
                            fullName,
                            email,
                            password,
                            confirmPassword,
                            termsAccepted,
                            onRegisterSuccess
                        )
                    }
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 7. Checkbox Términos y Condiciones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = {
                        termsAccepted = it
                        if (uiState is AuthUiState.Error) viewModel.resetState()
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = BrandPrimary,
                        uncheckedColor = Color(0xFFD1D5DB)
                    )
                )

                val termsText = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF374151), fontSize = 12.sp)) {
                        append("Acepto los ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = BrandPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    ) {
                        append("Términos y condiciones")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFF374151), fontSize = 12.sp)) {
                        append(" y la ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = BrandPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    ) {
                        append("Política de privacidad")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFF374151), fontSize = 12.sp)) {
                        append(" de Tripflow.")
                    }
                }

                Text(
                    text = termsText,
                    lineHeight = 16.sp,
                    modifier = Modifier.clickable {
                        termsAccepted = !termsAccepted
                        if (uiState is AuthUiState.Error) viewModel.resetState()
                    }
                )
            }

            // Mensaje de error si la validación o registro fallan
            if (uiState is AuthUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = DestructiveRed,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 8. Botón Crear cuenta
            TripFlowPrimaryButton(
                text = "Crear cuenta",
                isLoading = uiState is AuthUiState.Loading,
                onClick = {
                    focusManager.clearFocus()
                    viewModel.signUp(
                        fullName,
                        email,
                        password,
                        confirmPassword,
                        termsAccepted,
                        onRegisterSuccess
                    )
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 9. Enlace a Iniciar Sesión
            val annotatedLoginText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextSecondary, fontSize = 14.sp)) {
                    append("¿Ya tienes una cuenta? ")
                }
                withStyle(
                    style = SpanStyle(
                        color = BrandPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("Inicia sesión")
                }
            }

            Text(
                text = annotatedLoginText,
                modifier = Modifier.clickable { onNavigateToLogin() },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    TripFlowTheme {
        RegisterScreen(
            onNavigateToLogin = {},
            onRegisterSuccess = {}
        )
    }
}
