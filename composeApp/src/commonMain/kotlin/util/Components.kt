package util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import theme.blue800

enum class Direction {
    START,
    END
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalLoadMorePager(
    initPage: Int = 0,
    pageCount: () -> Int = { 0 },
    loadMoreThreshold: Int = 1,
    modifier: Modifier = Modifier,
    loadMore: (Direction, Int) -> Int = { _, _ -> 0 },
    content: @Composable PagerScope.(page: Int) -> Unit = {},
) {
    suspend fun PagerState.updatePage(page: Int) {
        scroll {
            updateCurrentPage(page)
        }
    }

    val pagerState = rememberPagerState(
        initialPage = initPage,
        pageCount = pageCount,
    )

    val currentSettledPage by derivedStateOf { pagerState.settledPage }

    LaunchedEffect(currentSettledPage) {
        if (currentSettledPage <= loadMoreThreshold) {
            val loadMoreCount = loadMore(Direction.START, currentSettledPage)
            pagerState.updatePage(currentSettledPage + loadMoreCount)
        } else if (currentSettledPage >= pagerState.pageCount - 1 - loadMoreThreshold) {
            loadMore(Direction.END, currentSettledPage)
        }
    }


    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        pageContent = content,
    )
}

@Composable
fun OutlinedTextFieldButton(
    value: String,
    label: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onClick: () -> Unit = {},
) {
    val source = remember { MutableInteractionSource() }
    val pressedState = source.interactions.collectAsState(
        initial = PressInteraction.Cancel(PressInteraction.Press(Offset.Zero))
    )

    if (pressedState.value is PressInteraction.Release) {
        onClick.invoke()
        source.tryEmit(PressInteraction.Cancel(PressInteraction.Press(Offset.Zero)))
    }

    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = label,
        readOnly = true,
        modifier = Modifier.then(modifier),
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = blue800,
            focusedLabelColor = blue800,
            focusedBorderColor = blue800,
        ),
        interactionSource = source,
    )
}