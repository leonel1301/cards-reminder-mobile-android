package com.lenaralabs.cardsreminder.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lenaralabs.cardsreminder.CardsReminderApp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.auth.AuthRepository
import com.lenaralabs.cardsreminder.ui.components.AuthGradientBackground
import com.lenaralabs.cardsreminder.ui.components.PoweredByLenaraFooter
import com.lenaralabs.cardsreminder.ui.theme.CardsreminderTheme
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = viewModel(
        factory = SignInViewModel.Factory(
            (LocalContext.current.applicationContext as CardsReminderApp).authRepository,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.cardsReminder
    val isDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val webClientId = stringResource(R.string.default_web_client_id)
    val noGoogleAccountMessage = stringResource(R.string.error_no_google_account)
    val signInFailedMessage = stringResource(R.string.error_sign_in_failed)
    val credentialManager = remember { CredentialManager.create(context) }

    AuthGradientBackground(
        isDarkTheme = isDarkTheme,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!isDarkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = colors.headerSurface,
                        tonalElevation = 2.dp,
                    ) {
                        SignInBrandContent()
                    }
                } else {
                    SignInBrandContent(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.sign_in_prompt),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                            )

                            Text(
                                text = stringResource(R.string.sign_in_choose_method),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.secondaryText,
                                textAlign = TextAlign.Center,
                            )
                        }

                        uiState.errorMessage?.let { message ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = colors.redStateBackground.copy(alpha = 0.65f),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = null,
                                        tint = colors.redStateForeground,
                                    )
                                    Text(
                                        text = message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.redStateForeground,
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.signInWithGoogle(
                                    context = context,
                                    credentialManager = credentialManager,
                                    webClientId = webClientId,
                                    noGoogleAccountMessage = noGoogleAccountMessage,
                                    signInFailedMessage = signInFailedMessage,
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !uiState.isSigningIn,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.cardSurface,
                                contentColor = if (isDarkTheme) Color.White else Color.Black,
                            ),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.google_g),
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Text(
                                text = stringResource(R.string.sign_in_continue_google),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                }
            }

            PoweredByLenaraFooter(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
            )
        }
    }

    if (uiState.isSigningIn) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        ) {
            ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    CircularProgressIndicator(color = colors.primaryAction)
                    Text(
                        text = stringResource(R.string.sign_in_loading),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun SignInBrandContent(
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val titleColor = if (isDarkTheme) MaterialTheme.colorScheme.onBackground else Color.White
    val subtitleColor = if (isDarkTheme) {
        MaterialTheme.cardsReminder.secondaryText
    } else {
        Color.White.copy(alpha = 0.88f)
    }
    val iconContainerColor = if (isDarkTheme) {
        MaterialTheme.cardsReminder.primaryAction.copy(alpha = 0.14f)
    } else {
        Color.White.copy(alpha = 0.12f)
    }
    val iconTint = if (isDarkTheme) MaterialTheme.cardsReminder.primaryAction else Color.White

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = iconContainerColor,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.CreditCard,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = iconTint,
                )
            }
        }

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = titleColor,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(R.string.sign_in_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = subtitleColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenPreview() {
    CardsreminderTheme {
        SignInScreen(
            viewModel = remember {
                SignInViewModel(AuthRepository())
            },
        )
    }
}
