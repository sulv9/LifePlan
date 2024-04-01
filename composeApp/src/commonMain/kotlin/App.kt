@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import lifeplan.composeapp.generated.resources.Res
import lifeplan.composeapp.generated.resources.main_add_new_plan_content_desc
import lifeplan.composeapp.generated.resources.main_top_bar_title
import lifeplan.composeapp.generated.resources.new_close_screen_content_desc
import lifeplan.composeapp.generated.resources.new_top_bar_title
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import screen.main.MainScreen
import screen.new.NewPlanScreen
import theme.LifePlanTheme

@Composable
fun App() {
    LifePlanTheme { LifePlanContent() }
}

val LocalSnackBarHostState: ProvidableCompositionLocal<SnackbarHostState?> =
    staticCompositionLocalOf { null }

@Composable
private fun LifePlanContent() {
    val navigator = remember { mutableStateOf<Navigator?>(null) }
    val topAppBarState = rememberTopAppBarState()
    val snackBarHostState = remember { SnackbarHostState() }

    val topBarScrollBehavior = topBarScrollBehavior(navigator.value, topAppBarState)

    Scaffold(
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { TopBarTitle(navigator.value) },
                actions = {
                    IconButton(onClick = { onTopBarActionClick(navigator.value) }) {
                        TopBarActionIcon(navigator.value)
                    }
                },
                scrollBehavior = topBarScrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
    ) { paddingValues ->
        CompositionLocalProvider(
            LocalSnackBarHostState provides snackBarHostState
        ) {
            Box(
                Modifier.padding(paddingValues).fillMaxSize()
            ) {
                Navigator(screen = MainScreen()) { nav ->
                    navigator.value = nav
                    LifePlanScreenTransition(nav)
                }
            }
        }
    }
}

@Composable
private fun TopBarTitle(navigator: Navigator?) {
    val title = navigator?.let {
        when (it.lastItem) {
            is MainScreen -> stringResource(Res.string.main_top_bar_title)
            is NewPlanScreen -> stringResource(Res.string.new_top_bar_title)
            else -> ""
        }
    } ?: ""
    Text(
        text = title
    )
}

@Composable
private fun TopBarActionIcon(navigator: Navigator?) {
    val (imageVector, contentDescription) = navigator?.let {
        when (it.lastItem) {
            is MainScreen -> Icons.Rounded.Add to stringResource(Res.string.main_add_new_plan_content_desc)
            is NewPlanScreen -> Icons.Rounded.Close to stringResource(Res.string.new_close_screen_content_desc)
            else -> null to ""
        }
    } ?: (null to "")
    if (imageVector != null) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
        )
    }
}

private fun onTopBarActionClick(navigator: Navigator?) {
    navigator?.let {
        when (it.lastItem) {
            is MainScreen -> it.push(
                NewPlanScreen((it.lastItem as MainScreen).onCreatePlanSuccessCallback)
            )
            is NewPlanScreen -> it.pop()
            else -> Unit
        }
    }
}

@Composable
private fun topBarScrollBehavior(
    navigator: Navigator?,
    topAppBarState: TopAppBarState
): TopAppBarScrollBehavior {
    val enterAlways = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    val pinned = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
    return navigator?.let {
        when (it.lastItem) {
            is MainScreen -> enterAlways
            is NewPlanScreen -> pinned
            else -> pinned
        }
    } ?: pinned
}

@Composable
private fun LifePlanScreenTransition(
    navigator: Navigator,
) {
    val enterAnim: FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )
    val exitAnim: FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    ScreenTransition(
        navigator = navigator,
        modifier = Modifier,
        content = { it.Content() },
        transition = {
            println("Qiutian - ${navigator.lastEvent} ${navigator.lastItem} ${navigator.parent}")
            val (initialScale, targetScale) = when (navigator.lastEvent) {
                StackEvent.Pop -> 1.0f to 0.8f
                else -> 0.8f to 1.0f
            }

            fadeIn(animationSpec = enterAnim) +
                    scaleIn(animationSpec = enterAnim, initialScale = initialScale) togetherWith
                    fadeOut(animationSpec = exitAnim) + scaleOut(
                animationSpec = exitAnim,
                targetScale = targetScale
            )
        }
    )
}