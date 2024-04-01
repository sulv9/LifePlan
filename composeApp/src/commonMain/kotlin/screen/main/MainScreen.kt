@file:OptIn(
    ExperimentalFoundationApi::class, ExperimentalResourceApi::class
)

package screen.main

import LocalSnackBarHostState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import data.model.PlanEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import lifeplan.composeapp.generated.resources.Res
import lifeplan.composeapp.generated.resources.main_create_plan_success
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import theme.blackBg
import theme.grayBorder
import theme.subTitleFontColor
import theme.titleFontColor
import theme.whiteBg
import util.Direction
import util.HorizontalLoadMorePager
import util.colorWithDark
import util.forward
import util.getPriorityColor
import util.isEn
import util.weekNames

class MainScreen : Screen {
    var onCreatePlanSuccessCallback: (() -> Unit)? = null

    @Composable
    override fun Content() {
        val planCreatedSuccessTip = stringResource(Res.string.main_create_plan_success)

        val mainScreenModel = getScreenModel<MainScreenModel>()
        val snackBarHostState = LocalSnackBarHostState.current
        val coroutineScope = rememberCoroutineScope()
        var eventFlowJob: Job? = null

        val planListState by mainScreenModel.planList.collectAsState()
        val pageListState by mainScreenModel.weekList.collectAsState()

        onCreatePlanSuccessCallback = {
            mainScreenModel.refreshPlanList()
            mainScreenModel.onCreatePlanSuccess()
        }

        LifecycleEffect(
            onStarted = {
                eventFlowJob = coroutineScope.launch {
                    mainScreenModel.eventFlow.collect {
                        when (it) {
                            is MainEvent.ShowPlanCreateSuccess -> {
                                snackBarHostState?.showSnackbar(
                                    message = planCreatedSuccessTip,
                                    withDismissAction = true
                                )
                            }
                        }
                    }
                }
            },
            onDisposed = {
                eventFlowJob?.cancel()
            }
        )

        MainScreenContent(
            planList = planListState,
            calendarInitPage = mainScreenModel.getInitWeekPage(),
            calendarPageList = pageListState,
            onCalendarPageCount = { mainScreenModel.getCalendarPageCount() },
            calendarLoadMoreThreshold = mainScreenModel.getCalendarLoadMoreThreshold(),
            calendarLoadMoreCount = mainScreenModel.getCalendarLoadMoreCount(),
            calendarLoadMorePage = { dir, page ->
                mainScreenModel.loadMoreCalendarPages(dir, page)
            },
        )
    }
}

@Composable
private fun MainScreenContent(
    planList: List<PlanEntity> = emptyList(),
    calendarInitPage: Int = 0,
    calendarPageList: List<List<LocalDate>> = emptyList(),
    onCalendarPageCount: () -> Int = { 0 },
    calendarLoadMoreThreshold: Int = 0,
    calendarLoadMoreCount: Int = 0,
    calendarLoadMorePage: (Direction, Int) -> Unit = { _, _ -> },
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MainCalendarHeader(
            calendarInitPage = calendarInitPage,
            calendarPageList = calendarPageList,
            onCalendarPageCount = onCalendarPageCount,
            calendarLoadMoreThreshold = calendarLoadMoreThreshold,
            calendarLoadMoreCount = calendarLoadMoreCount,
            calendarLoadMorePage = calendarLoadMorePage,
        )

        Spacer(Modifier.height(12.dp))

        MainListContent(planList)
    }
}

@Composable
private fun MainCalendarHeader(
    calendarInitPage: Int = 0,
    calendarPageList: List<List<LocalDate>> = emptyList(),
    onCalendarPageCount: () -> Int = { 0 },
    calendarLoadMoreThreshold: Int = 0,
    calendarLoadMoreCount: Int = 0,
    calendarLoadMorePage: (Direction, Int) -> Unit = { _, _ -> },
) {
    Column(
        Modifier.fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            weekNames().mapIndexed { index, s ->
                if (isEn())
                    if (index == 1 || index == 3) s.substring(0, 4) else s.substring(0, 3)
                else
                    s.split("")[2]
            }.toMutableList().forward(1).forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.fillMaxWidth().height(12.dp))

        HorizontalLoadMorePager(
            initPage = calendarInitPage,
            pageCount = onCalendarPageCount,
            loadMoreThreshold = calendarLoadMoreThreshold,
            loadMoreCount = calendarLoadMoreCount,
            loadMore = calendarLoadMorePage,
        ) { index ->
            calendarPageList.getOrNull(index)?.let { date ->
                Row {
                    date.map { it.dayOfMonth.toString() }.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainListContent(
    plans: List<PlanEntity>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(
            items = plans,
            key = { plan -> plan.id }
        ) { plan ->
            MainPlanCard(plan)
        }
    }
}

@Composable
private fun MainPlanCard(plan: PlanEntity) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorWithDark(whiteBg, blackBg)
        ),
        border = BorderStroke(1.dp, grayBorder),
        onClick = { /* TODO navigate to detailScreen */ }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Circle,
                contentDescription = "",
                tint = getPriorityColor(plan.priority),
                modifier = Modifier.size(36.dp).padding(horizontal = 4.dp)
            )

            Spacer(Modifier.width(8.dp))

            Column {
                Text(
                    text = plan.title,
                    fontSize = 16.sp,
                    color = titleFontColor,
                )

                if (plan.description.isNotBlank()) {
                    Text(
                        text = plan.description,
                        fontSize = 14.sp,
                        color = subTitleFontColor,
                    )
                }
            }
        }
    }
}