@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import lifeplan.composeapp.generated.resources.Res
import lifeplan.composeapp.generated.resources.detail_screen_title
import lifeplan.composeapp.generated.resources.main_add_new_plan_content_desc
import lifeplan.composeapp.generated.resources.main_top_bar_title
import lifeplan.composeapp.generated.resources.new_close_screen_content_desc
import lifeplan.composeapp.generated.resources.new_top_bar_title
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import screen.detail.DetailScreen
import screen.main.MainScreen
import screen.new.NewPlanScreen
import theme.LifePlanTheme
import theme.blue50

@Composable
fun App() {
    LifePlanTheme { LifePlanContent() }
}

val LocalSnackBarHostState: ProvidableCompositionLocal<SnackbarHostState?> =
    staticCompositionLocalOf { null }

@Composable
private fun LifePlanContent() {
    var navigator by remember { mutableStateOf<Navigator?>(null) }
    val topAppBarState = rememberTopAppBarState()
    val snackBarHostState = remember { SnackbarHostState() }

    val topBarScrollBehavior = topBarScrollBehavior(navigator, topAppBarState)

    Scaffold(
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { TopBarTitle(navigator) },
                actions = {
                    IconButton(onClick = { onTopBarActionClick(navigator) }) {
                        TopBarActionIcon(navigator)
                    }
                },
                scrollBehavior = topBarScrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = blue50.copy(alpha = 0.75F)
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
    ) { paddingValues ->
        CompositionLocalProvider(
            LocalSnackBarHostState provides snackBarHostState
        ) {
            Box(
                Modifier.padding(paddingValues)
                    .fillMaxSize()
                    .background(blue50.copy(alpha = 0.75F))
            ) {
                Navigator(screen = MainScreen()) { nav ->
                    navigator = nav
                    LifePlanScreenTransition(nav)
                }
            }
        }
    }
}

@Composable
private fun TopBarTitle(navigator: Navigator?) {
    navigator?.let { nav ->
        AnimatedContent(
            nav.lastItem,
            transitionSpec = {
                fadeIn(animationSpec = tween(150, 100))
                    .togetherWith(fadeOut(tween(150))).using(SizeTransform())
            }
        ) {
            Text(
                text = when (it) {
                    is MainScreen -> stringResource(Res.string.main_top_bar_title)
                    is NewPlanScreen -> stringResource(Res.string.new_top_bar_title)
                    is DetailScreen -> stringResource(Res.string.detail_screen_title)
                    else -> ""
                }
            )
        }
    }
}

@Composable
private fun TopBarActionIcon(navigator: Navigator?) {
    navigator?.let { nav ->
        val rotation = remember { Animatable(0F) }

        LaunchedEffect(nav.lastItem) {
            if (nav.lastItem is MainScreen) { // 进入 Main
                rotation.animateTo(
                    targetValue = 0F,
                    animationSpec = tween(150)
                )
            } else if (nav.lastItem !is MainScreen) { // 退出 Main
                rotation.animateTo(
                    targetValue = -45F,
                    animationSpec = tween(150)
                )
            }
        }

        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = if (nav.lastItem is MainScreen)
                stringResource(Res.string.main_add_new_plan_content_desc)
            else stringResource(Res.string.new_close_screen_content_desc),
            modifier = Modifier.graphicsLayer {
                rotationZ = rotation.value
            }
        )
    }
}

private fun onTopBarActionClick(navigator: Navigator?) {
    navigator?.let {
        when (it.lastItem) {
            is MainScreen -> it.push(NewPlanScreen())

            else -> it.pop()
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
private fun LifePlanScreenTransition(navigator: Navigator) {
    val planCardHeight = LocalDensity.current.run { 68.dp.roundToPx() }
    ScreenTransition(
        navigator = navigator,
        transition = {
            when (navigator.lastItem) {
                is NewPlanScreen, is MainScreen -> fadeIn(
                    animationSpec = tween(250, delayMillis = 150)
                ) togetherWith fadeOut(animationSpec = tween(250))

                is DetailScreen -> expandVertically(
                    animationSpec = tween(350),
                    expandFrom = Alignment.CenterVertically,
                    clip = true,
                    initialHeight = { planCardHeight },
                ) + fadeIn(
                    animationSpec = tween(250, 150)
                ) togetherWith fadeOut(
                    animationSpec = tween(250)
                )

                else -> throw Exception("Unknown screen")
            }
        }
    )
}