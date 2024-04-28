@file:OptIn(
    ExperimentalFoundationApi::class, ExperimentalResourceApi::class,
    ExperimentalMaterial3Api::class
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import cafe.adriel.voyager.navigator.LocalNavigator
import data.model.PlanEntity
import event.LifePlanEvent
import event.NoneLifePlanEvent
import event.OnPlanCreateSuccess
import kotlinx.datetime.LocalDate
import lifeplan.composeapp.generated.resources.Res
import lifeplan.composeapp.generated.resources.main_create_plan_success
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import screen.detail.DetailScreen
import theme.blue400
import theme.titleFontColor
import util.Direction
import util.HorizontalLoadMorePager
import util.forward
import util.getPriorityColor
import util.getPriorityName
import util.getProgressColor
import util.ifThen
import util.isEn
import util.today
import util.weekNames

class MainScreen : Screen {
    @Composable
    override fun Content() {
        val planCreatedSuccessTip = stringResource(Res.string.main_create_plan_success)

        val mainScreenModel = getScreenModel<MainScreenModel>()
        val navigator = LocalNavigator.current
        val snackBarHostState = LocalSnackBarHostState.current

        val planListState by mainScreenModel.planList.collectAsState()
        val pageListState by mainScreenModel.weekList.collectAsState()
        val selectedDayState by mainScreenModel.selectedDay.collectAsState()

        LaunchedEffect(Unit) {
            mainScreenModel.refreshPlanList()
        }

        LaunchedEffect(LifePlanEvent.event) {
            when (LifePlanEvent.event) {
                is OnPlanCreateSuccess -> {
                    snackBarHostState?.showSnackbar(
                        message = planCreatedSuccessTip,
                        withDismissAction = true
                    )
                }
            }

            if (LifePlanEvent.event !is NoneLifePlanEvent)
                LifePlanEvent.sendEvent(NoneLifePlanEvent)
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
            onNavigateDetailScreen = {
                navigator?.push(DetailScreen(it))
            }
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
    onNavigateDetailScreen: (Long) -> Unit = {},
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

        MainListContent(
            plans = planList,
            onNavigateDetailScreen = onNavigateDetailScreen
        )
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
    onNavigateDetailScreen: (Long) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(
            items = plans,
            key = { plan -> plan.id }
        ) { plan ->
            MainPlanCard(
                modifier = Modifier.animateItemPlacement(),
                plan = plan,
                onNavigateDetailScreen = onNavigateDetailScreen
            )
        }
    }
}

@Composable
private fun MainPlanCard(
    modifier: Modifier = Modifier,
    plan: PlanEntity,
    onNavigateDetailScreen: (Long) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth().height(68.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onNavigateDetailScreen(plan.id) }
            .background(color = Color.White.copy(0.85F))
            .border(
                width = 0.5.dp,
                color = Color.Gray.copy(0.1F),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
            .then(modifier),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(18.dp)
                    .border(2.dp, getPriorityColor(plan.priority).copy(alpha = 0.75F), CircleShape),
                imageVector = Icons.Filled.Circle,
                contentDescription = getPriorityName(plan.priority),
                tint = getPriorityColor(plan.priority).copy(alpha = 0.25F)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = plan.title,
                fontSize = 15.sp,
                color = titleFontColor,
                maxLines = 1,
            )
        }

        Spacer(Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (plan.remindDateTime.isNotBlank())
                    Icons.Outlined.Event
                else
                    Icons.Outlined.Flag,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = with((plan.remindDateTime.ifBlank { plan.endDateTime }).split(" ")) {
                    if (today().toString() == get(0) && size > 1)
                        get(1).substringBeforeLast(':')
                    else
                        get(0).substringAfter('-')
                },
                fontSize = 10.sp,
                lineHeight = 14.sp,
            )

            Slider(
                modifier = Modifier,
                value = plan.progress.toFloat(),
                onValueChange = {},
                enabled = false,
                valueRange = 0F..100F,
                colors = SliderDefaults.colors(
                    inactiveTrackColor = Color.Gray.copy(0.25F),
                    disabledActiveTrackColor = getProgressColor(plan.progress.toFloat())
                ),
                thumb = {},
            )
        }
    }
}