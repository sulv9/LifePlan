package screen.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import data.model.PlanEntity
import data.repo.PlanRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import screen.new.NewPlanEvent
import util.dateFormat
import util.dateTimeFormat
import util.millisToDateTime
import util.parseDate2Millis
import util.parseTime2Millis
import util.zeroTime

class DetailScreenModel(
    id: Long,
    private val planRepo: PlanRepository,
) : ScreenModel {
    private val initialPlan: PlanEntity = planRepo.getPlan(id)

    var title by mutableStateOf(initialPlan.title)
        private set

    var desc by mutableStateOf(initialPlan.description)
        private set

    var priority by mutableStateOf(initialPlan.priority)
        private set

    var progress by mutableStateOf(initialPlan.progress)
        private set

    var startDate by mutableStateOf(parseDate2Millis(initialPlan.startDateTime))
        private set

    var startTime by mutableStateOf(parseTime2Millis(initialPlan.startDateTime))
        private set

    var endDate by mutableStateOf(parseDate2Millis(initialPlan.endDateTime))
        private set

    var endTime by mutableStateOf(parseTime2Millis(initialPlan.endDateTime))
        private set

    var remindDateTime by mutableStateOf(initialPlan.remindDateTime)
        private set

    private var eventChannel = Channel<NewPlanEvent>()
    val eventFlow: Flow<NewPlanEvent> = eventChannel.receiveAsFlow()

    var showDatePickDialog by mutableStateOf(false)
        private set

    var showTimePickDialog by mutableStateOf(false)
        private set

    var showRemindDatePickDialog by mutableStateOf(false)
        private set

    var showRemindTimePickDialog by mutableStateOf(false)
        private set

    fun deletePlan() {
        planRepo.deletePlan(initialPlan.id)
    }

    fun updatePlanTitle(title: String) {
        if (title.isEmpty()) {
            return
        }
        this.title = title
        planRepo.updateTitle(initialPlan.id, title)
    }

    fun updatePlanDesc(desc: String) {
        this.desc = desc
        planRepo.updateDescription(initialPlan.id, desc)
    }

    fun updatePlanPriority(priority: Float) {
        this.priority = priority.toInt()
        planRepo.updatePriority(initialPlan.id, priority.toLong())
    }

    fun updatePlanProgress(progress: Float) {
        this.progress = progress.toInt()
        planRepo.updateProgress(initialPlan.id, progress.toLong())
    }

    fun updatePlanStartDate(startDate: Long) {
        if (startDate > endDate) return
        this.startDate = startDate
        val startDateTime = (if (startTime < 0) dateFormat else dateTimeFormat)
            .format(millisToDateTime(startDate + startTime - if (startTime < 0) 0 else zeroTime))
        planRepo.updateStartDateTime(initialPlan.id, startDateTime)
    }

    fun updatePlanStartTime(startTime: Long) {
        this.startTime = startTime
        val startDateTime = (if (startTime < 0) dateFormat else dateTimeFormat)
            .format(millisToDateTime(startDate + startTime - if (startTime < 0) 0 else zeroTime))
        planRepo.updateStartDateTime(initialPlan.id, startDateTime)
    }

    fun updatePlanEndDate(endDate: Long) {
        if (endDate < startDate) return
        this.endDate = endDate
        val endDateTime = (if (endTime < 0) dateFormat else dateTimeFormat)
            .format(millisToDateTime(endDate + endTime - if (endTime < 0) 0 else zeroTime))
        planRepo.updateEndDateTime(initialPlan.id, endDateTime)
    }

    fun updatePlanEndTime(endTime: Long) {
        this.endTime = endTime
        val endDateTime = (if (endTime < 0) dateFormat else dateTimeFormat)
            .format(millisToDateTime(endDate + endTime - if (endTime < 0) 0 else zeroTime))
        planRepo.updateEndDateTime(initialPlan.id, endDateTime)
    }

    fun updatePlanRemindDateTime(remindDateTime: String) {
        this.remindDateTime = "$remindDateTime:00"
        planRepo.updateRemindDateTime(initialPlan.id, "$remindDateTime:00")
    }

    fun updateShowDatePickDialog(show: Boolean) {
        showDatePickDialog = show
    }

    fun updateShowTimePickDialog(show: Boolean) {
        showTimePickDialog = show
    }

    fun updateShowRemindDatePickDialog(show: Boolean) {
        showRemindDatePickDialog = show
    }

    fun updateShowRemindTimePickDialog(show: Boolean) {
        showRemindTimePickDialog = show
    }
}