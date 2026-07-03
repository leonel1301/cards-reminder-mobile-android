package com.lenaralabs.cardsreminder.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lenaralabs.cardsreminder.CardsReminderApp
import com.lenaralabs.cardsreminder.core.data.OnboardingPreferences
import com.lenaralabs.cardsreminder.feature.auth.PostLoginSetupScreen
import com.lenaralabs.cardsreminder.feature.auth.SignInScreen
import com.lenaralabs.cardsreminder.feature.onboarding.OnboardingScreen
import com.lenaralabs.cardsreminder.feature.onboarding.SplashScreen
import com.lenaralabs.cardsreminder.ui.animation.AppMotion
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder
import java.util.UUID

private enum class RootDestination {
    Splash,
    Onboarding,
    SignIn,
    SessionLoading,
    PostLoginSetup,
    Main,
}

@Composable
fun RootContent(
    modifier: Modifier = Modifier,
) {
    val application = LocalContext.current.applicationContext as CardsReminderApp
    val onboardingPreferences = remember {
        OnboardingPreferences(application)
    }
    val viewModel: RootViewModel = viewModel(
        factory = RootViewModel.Factory(
            onboardingPreferences = onboardingPreferences,
            authRepository = application.authRepository,
            userRepository = application.userRepository,
        ),
    )

    val showSplash by viewModel.showSplash.collectAsStateWithLifecycle()
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
    val isSignedIn by viewModel.isSignedIn.collectAsStateWithLifecycle()
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val currentUser by application.authRepository.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(isSignedIn, currentUser?.uid) {
        val firebaseUid = currentUser?.uid
        if (isSignedIn && firebaseUid != null) {
            viewModel.refreshUserSession(firebaseUid)
        }
    }

    val destination = when {
        showSplash -> RootDestination.Splash
        !hasCompletedOnboarding -> RootDestination.Onboarding
        !isSignedIn -> RootDestination.SignIn
        !sessionState.hasResolvedTermsStatus -> RootDestination.SessionLoading
        sessionState.needsTermsAcceptance -> RootDestination.PostLoginSetup
        else -> RootDestination.Main
    }

    val appSessionKey = remember(destination) {
        if (destination == RootDestination.Main) UUID.randomUUID().toString() else null
    }

    val colors = MaterialTheme.cardsReminder

    AnimatedContent(
        targetState = destination,
        modifier = modifier
            .fillMaxSize()
            .background(colors.appBackground),
        transitionSpec = { AppMotion.navFadeIn() togetherWith AppMotion.navFadeOut() },
        label = "rootContent",
    ) { target ->
        when (target) {
            RootDestination.Splash -> SplashScreen(
                onFinish = viewModel::onSplashFinished,
            )

            RootDestination.Onboarding -> OnboardingScreen(
                onComplete = viewModel::completeOnboarding,
            )

            RootDestination.SignIn -> SignInScreen()

            RootDestination.SessionLoading -> SessionLoadingScreen()

            RootDestination.PostLoginSetup -> PostLoginSetupScreen()

            RootDestination.Main -> {
                val firebaseUid = currentUser?.uid
                val activeSessionKey = appSessionKey
                if (firebaseUid != null && activeSessionKey != null) {
                    var previousFirebaseUid by remember { mutableStateOf<String?>(null) }
                    LaunchedEffect(firebaseUid) {
                        val userSwitched = previousFirebaseUid != null && previousFirebaseUid != firebaseUid
                        application.pushNotificationManager.handleUserSessionChange(
                            userSwitched = userSwitched,
                        )
                        previousFirebaseUid = firebaseUid
                    }
                    key(activeSessionKey) {
                        val sessionViewModelStoreOwner = remember(activeSessionKey) {
                            object : ViewModelStoreOwner {
                                override val viewModelStore = ViewModelStore()
                            }
                        }
                        DisposableEffect(activeSessionKey) {
                            onDispose {
                                sessionViewModelStoreOwner.viewModelStore.clear()
                            }
                        }
                        CompositionLocalProvider(
                            LocalViewModelStoreOwner provides sessionViewModelStoreOwner,
                        ) {
                            MainAppContent()
                        }
                    }
                }
            }
        }
    }
}
