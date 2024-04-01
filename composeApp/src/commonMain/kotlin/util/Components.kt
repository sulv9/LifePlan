package util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.launch
import lifeplan.composeapp.generated.resources.Res
import lifeplan.composeapp.generated.resources.new_tip_start_date
import org.jetbrains.compose.resources.stringResource
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
    loadMoreCount: Int = 0,
    modifier: Modifier = Modifier,
    loadMore: (Direction, Int) -> Unit = { _, _ -> },
    content: @Composable PagerScope.(page: Int) -> Unit = {},
) {
    val pagerState = rememberPagerState(
        initialPage = initPage,
        pageCount = pageCount,
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (page <= loadMoreThreshold) {
                loadMore(Direction.START, page)
                launch { pagerState.scrollToPage(page + loadMoreCount) }
            } else if (page >= pagerState.pageCount - 1 - loadMoreThreshold) {
                loadMore(Direction.END, page)
            }
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