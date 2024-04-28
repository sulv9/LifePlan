package screen.main

import cafe.adriel.voyager.core.model.ScreenModel
import data.model.PlanEntity
import data.repo.PlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    fun refreshPlanList() {
        _planList.update {
            planRepo.getPlanByDate(_selectedDay.value.format())
        }
    }

    fun loadMoreCalendarPages(direction: Direction, page: Int): Int {
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
        return PRE_LOAD_CALENDAR_COUNT
    }

    fun updateSelectedDay(day: LocalDate) {
        _selectedDay.value = day
        refreshPlanList()
    }

    fun getCalendarPageCount(): Int = _weekList.value.size

    fun getInitWeekPage(): Int = INIT_WEEK_LIST_COUNT / 2

    fun getCalendarLoadMoreThreshold() = PRE_LOAD_CALENDAR_THRESHOLD

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