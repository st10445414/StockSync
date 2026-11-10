package com.stocksync.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.stocksync.app.BuildConfig
import com.stocksync.app.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val loginState by authViewModel.loginState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(loginState) {
        if (loginState is AuthViewModel.LoginState.Success) {
            onLoginSuccess()
            authViewModel.resetState()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF27AE60), Color(0xFF2ECC71))
                        )
                    )
                    .semantics { contentDescription = "StockSync" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "StockSync",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF27AE60)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.login_tagline),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            val isLoading = loginState is AuthViewModel.LoginState.Loading
            val errorMessage = (loginState as? AuthViewModel.LoginState.Error)?.message

            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        try {
                            if (BuildConfig.GOOGLE_SERVER_CLIENT_ID.isBlank()) {
                                throw IllegalStateException("Google sign-in is not configured for this build")
                            }

                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setServerClientId(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
                                .setFilterByAuthorizedAccounts(false)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val result = credentialManager.getCredential(context, request)
                            val credential = result.credential

                            if (credential is CustomCredential &&
                                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {
                                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                authViewModel.loginWithGoogle(googleCredential.idToken)
                            } else {
                                authViewModel.showError("Google sign-in did not return a valid account")
                            }
                        } catch (error: Exception) {
                            authViewModel.showError(error.message ?: "Google sign-in failed")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF27AE60)
                    )
                } else {
                    Text(
                        text = "\u25CF ",
                        color = Color(0xFF4285F4),
                        fontSize = 16.sp
                    )
                    Text(
                        text = stringResource(R.string.continue_with_google),
                        color = Color(0xFF555555),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = msg,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.sso_description),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
