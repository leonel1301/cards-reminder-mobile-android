package com.lenaralabs.cardsreminder.feature.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.ui.components.AuthGradientBackground
import com.lenaralabs.cardsreminder.ui.components.PoweredByLenaraFooter
import com.lenaralabs.cardsreminder.ui.theme.CardsReminderColors
import com.lenaralabs.cardsreminder.ui.theme.CardsreminderTheme
import com.lenaralabs.cardsreminder.core.analytics.AnalyticsScreens
import com.lenaralabs.cardsreminder.core.analytics.TrackScreen
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TrackScreen(AnalyticsScreens.ONBOARDING)
    val pages = OnboardingPages.all
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.cardsReminder
    val isDarkTheme = isSystemInDarkTheme()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    AuthGradientBackground(isDarkTheme = isDarkTheme, modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
            TopAppBar(
                title = {},
                actions = {
                    if (!isLastPage) {
                        TextButton(onClick = onComplete) {
                            Text(
                                text = stringResource(R.string.action_skip),
                                color = colors.secondaryText,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OnboardingPageIndicator(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage,
                    colors = colors,
                )

                Button(
                    onClick = {
                        if (isLastPage) {
                            onComplete()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryAction,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = stringResource(
                            if (isLastPage) {
                                R.string.action_get_started
                            } else {
                                R.string.action_continue
                            },
                        ),
                    )
                }

                PoweredByLenaraFooter()
            }
        },
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) { pageIndex ->
            OnboardingPageContent(
                page = pages[pageIndex],
                colors = colors,
            )
        }
    }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    colors: CardsReminderColors,
    modifier: Modifier = Modifier,
) {
    val (containerColor, iconTint) = page.style.colors(colors)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = containerColor,
            tonalElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = iconTint,
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(page.subtitleRes),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
        )
    }
}

@Composable
private fun OnboardingPageIndicator(
    pageCount: Int,
    currentPage: Int,
    colors: CardsReminderColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = tween(durationMillis = 250),
                label = "indicatorWidth",
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) colors.primaryAction else colors.defaultBorder,
                    ),
            )
        }
    }
}

private fun OnboardingPageStyle.colors(colors: CardsReminderColors): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> =
    when (this) {
        OnboardingPageStyle.Primary -> colors.primaryAction.copy(alpha = 0.12f) to colors.primaryAction
        OnboardingPageStyle.Amber -> colors.amberStateBackground to colors.amberStateForeground
        OnboardingPageStyle.Violet -> colors.violetStateBackground to colors.violetStateForeground
        OnboardingPageStyle.Emerald -> colors.emeraldStateBackground to colors.emeraldStateForeground
    }

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    CardsreminderTheme {
        OnboardingScreen(onComplete = {})
    }
}
