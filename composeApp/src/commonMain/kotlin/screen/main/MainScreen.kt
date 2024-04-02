@file:OptIn(
    ExperimentalFoundationApi::class, ExperimentalResourceApi::class
)

package screen.main

import LocalSnackBarHostState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import data.model.PlanEntity
import kotlinx.datetime.LocalDate
import lifeplan.composeapp.generated.resources.Res
import lifeplan.composeapp.generated.resources.main_create_plan_success
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import theme.blue400
import theme.subTitleFontColor
import theme.titleFontColor
import util.Direction
import util.HorizontalLoadMorePager
import util.forward
import util.getPriorityColor
import util.getPriorityName
import util.ifThen
import util.isEn
import util.launchWhenStart
import util.today
import util.weekNames

class MainScreen : Screen {
    var onCreatePlanSuccessCallback: (() -> Unit)? = null

    @Composable
    override fun Content() {
        val planCreatedSuccessTip = stringResource(Res.string.main_create_plan_success)

        val mainScreenModel = getScreenModel<MainScreenModel>()
        val snackBarHostState = LocalSnackBarHostState.current

        val planListState by mainScreenModel.planList.collectAsState()
        val pageListState by mainScreenModel.weekList.collectAsState()
        val selectedDayState by mainScreenModel.selectedDay.collectAsState()

        onCreatePlanSuccessCallback = {
            mainScreenModel.refreshPlanList()
            mainScreenModel.onCreatePlanSuccess()
        }

        launchWhenStart {
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

        MainScreenContent(
            planList = planListState,
            calendarInitPage = mainScreenModel.getInitWeekPage(),
            calendarPageList = pageListState,
            onCalendarPageCount = { mainScreenModel.getCalendarPageCount() },
            calendarLoadMoreThreshold = mainScreenModel.getCalendarLoadMoreThreshold(),
            calendarLoadMorePage = { dir, page ->
                mainScreenModel.loadMoreCalendarPages(dir, page)
            },
            updateSelectedDay = { mainScreenModel.updateSelectedDay(it) },
            selectedDay = selectedDayState,
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
    calendarLoadMorePage: (Direction, Int) -> Int = { _, _ -> 0 },
    updateSelectedDay: (LocalDate) -> Unit = {},
    selectedDay: LocalDate = today(),
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MainCalendarHeader(
            calendarInitPage = calendarInitPage,
            calendarPageList = calendarPageList,
            onCalendarPageCount = onCalendarPageCount,
            calendarLoadMoreThreshold = calendarLoadMoreThreshold,
            calendarLoadMorePage = calendarLoadMorePage,
            updateSelectedDay = updateSelectedDay,
            selectedDay = selectedDay,
        )

        Spacer(Modifier.height(20.dp))

        MainListContent(planList)
    }
}

@Composable
private fun MainCalendarHeader(
    calendarInitPage: Int = 0,
    calendarPageList: List<List<LocalDate>> = emptyList(),
    onCalendarPageCount: () -> Int = { 0 },
    calendarLoadMoreThreshold: Int = 0,
    calendarLoadMorePage: (Direction, Int) -> Int = { _, _ -> 0 },
    updateSelectedDay: (LocalDate) -> Unit = {},
    selectedDay: LocalDate = today(),
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
            loadMore = calendarLoadMorePage,
        ) { index ->
            calendarPageList.getOrNull(index)?.let { date ->
                Row {
                    date.forEach { day ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = day.dayOfMonth.toString(),
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (day.toString() == selectedDay.toString())
                                            blue400
                                        else
                                            Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ).clickable {
                                        updateSelectedDay(day)
                                    }.ifThen(
                                        day.toString() == today().toString(),
                                        Modifier.border(2.dp, blue400, RoundedCornerShape(8.dp))
                                    ).padding(horizontal = 10.dp, vertical = 4.dp),
                                textAlign = TextAlign.Center,
                                color = if (day.toString() == selectedDay.toString())
                                    Color.White
                                else
                                    Color.Black
                            )
                        }
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
        verticalArrangement = Arrangement.spacedBy(20.dp),
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
    Row(
        modifier = Modifier.fillMaxWidth().height(66.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { /* TODO navigate to detailScreen */ },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Circle,
            contentDescription = getPriorityName(plan.priority),
            tint = getPriorityColor(plan.priority),
            modifier = Modifier.padding(horizontal = 12.dp)
        )

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