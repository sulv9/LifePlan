package screen.main

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import data.model.PlanEntity
import data.repo.PlanRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import util.Direction
import util.format
import util.forwardWeekList
import util.getWeekOfDate
import util.today

private const val INIT_WEEK_LIST_COUNT = 5
private const val PRE_LOAD_CALENDAR_COUNT = 2
private const val PRE_LOAD_CALENDAR_THRESHOLD = 1

class MainScreenModel(
    private val planRepo: PlanRepository,
) : ScreenModel {
    private val _planList = MutableStateFlow<List<PlanEntity>>(emptyList())
    val planList = _planList.asStateFlow()

    private val _weekList = MutableStateFlow(initWeekList())
    val weekList = _weekList.asStateFlow()

    private val _selectedDay = MutableStateFlow(today())
    val selectedDay = _selectedDay.asStateFlow()

    private var eventChannel = Channel<MainEvent>()
    val eventFlow: Flow<MainEvent> = eventChannel.receiveAsFlow()

    init {
        _planList.update {
            planRepo.getPlanByDay(_selectedDay.value.format())
        }
    }

    fun refreshPlanList() {
        _planList.update {
            planRepo.getPlanByDay(_selectedDay.value.format())
        }
    }

    fun onCreatePlanSuccess() {
        screenModelScope.launch { eventChannel.send(MainEvent.ShowPlanCreateSuccess) }
    }

    fun loadMoreCalendarPages(direction: Direction, page: Int) {
        _weekList.update { prev ->
            val new = prev.toMutableList()
            val currentPageFirstDate = prev.getOrNull(page)?.getOrNull(0) ?: return@update prev
            for (i in (1..PRE_LOAD_CALENDAR_COUNT)) {
                when (direction) {
                    Direction.START -> {
                        new.add(
                            0,
                            getWeekOfDate(currentPageFirstDate.minus(DatePeriod(days = 7 * i)))
                                .forwardWeekList()
                        )
                    }

                    Direction.END -> {
                        new.add(
                            getWeekOfDate(currentPageFirstDate.plus(DatePeriod(days = 7 * i)))
                                .forwardWeekList()
                        )
                    }
                }
            }
            new
        }
    }

    fun getCalendarPageCount(): Int = _weekList.value.size

    fun getInitWeekPage(): Int = INIT_WEEK_LIST_COUNT / 2

    fun getCalendarLoadMoreThreshold() = PRE_LOAD_CALENDAR_THRESHOLD

    fun getCalendarLoadMoreCount() = PRE_LOAD_CALENDAR_COUNT

    private fun initWeekList(): List<List<LocalDate>> {
        val result = mutableListOf<List<LocalDate>>()
        val todayDate = today()
        for (i in (0 until INIT_WEEK_LIST_COUNT)) {
            result.add(
                getWeekOfDate(
                    if (i < INIT_WEEK_LIST_COUNT / 2)
                        todayDate.minus(DatePeriod(days = 7 * (INIT_WEEK_LIST_COUNT / 2 - i)))
                    else
                        todayDate.plus(DatePeriod(days = 7 * (i - INIT_WEEK_LIST_COUNT / 2)))
                ).forwardWeekList()
            )
        }
        return result
    }
}