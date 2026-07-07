package com.lenaralabs.cardsreminder.feature.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lenaralabs.cardsreminder.CardsReminderApp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.util.AppLinks
import com.lenaralabs.cardsreminder.ui.components.AppLoadingIndicator
import com.lenaralabs.cardsreminder.ui.components.AuthGradientBackground
import com.lenaralabs.cardsreminder.core.analytics.AnalyticsScreens
import com.lenaralabs.cardsreminder.core.analytics.TrackScreen
import com.lenaralabs.cardsreminder.ui.components.PoweredByLenaraFooter
import com.lenaralabs.cardsreminder.ui.animation.pressScaleEffect
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder
import com.lenaralabs.cardsreminder.ui.theme.isDarkTheme
import kotlinx.coroutines.delay

@Composable
fun PostLoginSetupScreen(
    modifier: Modifier = Modifier,
    viewModel: PostLoginSetupViewModel = viewModel(
        factory = PostLoginSetupViewModel.Factory(
            (LocalContext.current.applicationContext as CardsReminderApp).userRepository,
        ),
    ),
) {
    TrackScreen(AnalyticsScreens.POST_LOGIN_SETUP)
    val colors = MaterialTheme.cardsReminder
    val darkTheme = isDarkTheme()
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

    var messageIndex by remember { mutableIntStateOf(0) }
    var messageVisible by remember { mutableStateOf(false) }
    var actionsVisible by remember { mutableStateOf(false) }

    val messageAlpha by animateFloatAsState(
        targetValue = if (messageVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "messageAlpha",
    )
    val messageOffset by animateFloatAsState(
        targetValue = if (messageVisible) 0f else 12f,
        animationSpec = tween(durationMillis = 600),
        label = "messageOffset",
    )
    val actionsAlpha by animateFloatAsState(
        targetValue = if (actionsVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 680),
        label = "actionsAlpha",
    )
    val actionsOffset by animateFloatAsState(
        targetValue = if (actionsVisible) 0f else 16f,
        animationSpec = tween(durationMillis = 680),
        label = "actionsOffset",
    )
    val loadingAlpha by animateFloatAsState(
        targetValue = if (sessionState.isLoading) 1f else 0f,
        animationSpec = tween(250),
        label = "loadingAlpha",
    )
    val continueInteraction = remember { MutableInteractionSource() }

    LaunchedEffect(Unit) {
        messageVisible = true
        delay(350)
        actionsVisible = true
        delay(3_200)
        messageIndex = 1
    }

    val welcomeMessages = listOf(
        stringResource(R.string.post_login_welcome_excited),
        stringResource(R.string.post_login_welcome_ready),
    )

    AuthGradientBackground(isDarkTheme = darkTheme, modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = welcomeMessages[messageIndex],
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                        .offset(y = messageOffset.dp)
                        .padding(vertical = 8.dp)
                        .alpha(messageAlpha),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = MaterialTheme.typography.headlineLarge.lineHeight,
                )

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .offset(y = actionsOffset.dp)
                        .alpha(actionsAlpha),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Button(
                        onClick = viewModel::acceptTerms,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .pressScaleEffect(continueInteraction),
                        enabled = !sessionState.isLoading,
                        interactionSource = continueInteraction,
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryAction,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.action_continue),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    sessionState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.redStateForeground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Text(
                        text = rememberTermsFooterText(linkColor = colors.primaryAction),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                PoweredByLenaraFooter(
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 24.dp)
                        .alpha(actionsAlpha),
                )
            }

            if (sessionState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(loadingAlpha)
                        .background(Color.Black.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    AppLoadingIndicator(color = colors.primaryAction)
                }
            }
        }
    }
}

@Composable
private fun rememberTermsFooterText(linkColor: Color) = buildAnnotatedString {
    append(stringResource(R.string.post_login_terms_prefix))
    withLink(
        LinkAnnotation.Url(
            url = AppLinks.TERMS,
            styles = TextLinkStyles(
                style = SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline,
                ),
            ),
        ),
    ) {
        append(stringResource(R.string.post_login_terms_link))
    }
    append(stringResource(R.string.post_login_terms_middle))
    withLink(
        LinkAnnotation.Url(
            url = AppLinks.PRIVACY,
            styles = TextLinkStyles(
                style = SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline,
                ),
            ),
        ),
    ) {
        append(stringResource(R.string.post_login_privacy_link))
    }
    append(stringResource(R.string.post_login_terms_suffix))
}
