package screen.new

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import data.repo.PlanRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import util.dateTimeFormat
import util.hour2Millis
import util.millisToDateTime
import util.min2Millis
import util.zeroTime

class NewPlanScreenModel(
    private val planRepo: PlanRepository,
) : ScreenModel {
    var title by mutableStateOf("")
        private set

    var desc by mutableStateOf("")
        private set

    var priority by mutableStateOf(2F)
        private set

    var startDate by mutableStateOf(0L)
        private set

    var startTime by mutableStateOf(0L)
        private set

    var endDate by mutableStateOf(0L)
        private set

    var endTime by mutableStateOf(0L)
        private set

    var titleInputError by mutableStateOf(false)
        private set

    private var eventChannel = Channel<NewPlanEvent>()
    val eventFlow: Flow<NewPlanEvent> = eventChannel.receiveAsFlow()

    var startDateError by mutableStateOf(false)
        private set

    var endDateError by mutableStateOf(false)
        private set

    var showDatePickDialog by mutableStateOf(false)
        private set

    var showTimePickDialog by mutableStateOf(false)
        private set

    fun updatePlanTitle(title: String) {
        this.title = title
        if (titleInputError) titleInputError = false
    }

    fun updatePlanDesc(desc: String) {
        this.desc = desc
    }

    fun updateStartDate(date: Long) {
        startDate = date
        if (startDateError) startDateError = false
    }

    fun updateStartTime(time: Long) {
        startTime = time
    }

    fun updateEndDate(date: Long) {
        endDate = date
        if (endDateError) endDateError = false
    }

    fun updateEndTime(time: Long) {
        endTime = time
    }

    fun updatePriority(priority: Float) {
        this.priority = priority.toInt().toFloat()
    }

    fun updateShowDatePickDialog(show: Boolean) {
        showDatePickDialog = show
    }

    fun updateShowTimePickDialog(show: Boolean) {
        showTimePickDialog = show
    }

    fun createPlan() {
        if (title.isBlank()) {
            titleInputError = true
            screenModelScope.launch {
                eventChannel.send(NewPlanEvent.ShowTitleInputError)
            }
            return
        }
        if (startDate == 0L) {
            startDateError = true
            screenModelScope.launch {
                eventChannel.send(NewPlanEvent.ShowStartDateError)
            }
            return
        }
        if (endDate == 0L) {
            endDateError = true
            screenModelScope.launch {
                eventChannel.send(NewPlanEvent.ShowEndDateError)
            }
            return
        }
        if (startDate > endDate || (startDate == endDate && startTime > endTime)) {
            screenModelScope.launch {
                eventChannel.send(NewPlanEvent.IncorrectStartEndDate)
            }
            return
        }
        if (endTime == 0L) {
            endTime = hour2Millis(23) + min2Millis(59)
        }
        planRepo.createPlan(
            title,
            desc,
            dateTimeFormat.format(millisToDateTime(startDate + startTime - zeroTime)),
            dateTimeFormat.format(millisToDateTime(endDate + endTime - zeroTime)),
            priority.toLong()
        )
        screenModelScope.launch {
            eventChannel.send(NewPlanEvent.CreatePlanSuccess)
        }
    }
}