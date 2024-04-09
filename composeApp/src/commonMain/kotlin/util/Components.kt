package util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
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
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = label,
        readOnly = true,
        modifier = Modifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(pass = PointerEventPass.Initial)
                val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                if (upEvent != null) {
                    onClick()
                }
            }
        }.then(modifier),
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = blue800,
            focusedLabelColor = blue800,
            focusedBorderColor = blue800,
        ),
    )
}