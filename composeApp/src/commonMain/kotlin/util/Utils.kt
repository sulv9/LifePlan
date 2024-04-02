package util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import lifeplan.composeapp.generated.resources.Res
import lifeplan.composeapp.generated.resources.new_text_blue_priority
import lifeplan.composeapp.generated.resources.new_text_green_priority
import lifeplan.composeapp.generated.resources.new_text_red_priority
import lifeplan.composeapp.generated.resources.new_text_yellow_priority
import lifeplan.composeapp.generated.resources.week_name_friday
import lifeplan.composeapp.generated.resources.week_name_monday
import lifeplan.composeapp.generated.resources.week_name_saturday
import lifeplan.composeapp.generated.resources.week_name_sunday
import lifeplan.composeapp.generated.resources.week_name_thursday
import lifeplan.composeapp.generated.resources.week_name_tuesday
import lifeplan.composeapp.generated.resources.week_name_wednesday
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import theme.bluePriority
import theme.greenPriority
import theme.redPriority
import theme.yellowPriority

// region Screen

@Composable
fun Screen.launchWhenStart(block: suspend () -> Unit) {
    var job: Job? = null
    val coroutineScope = rememberCoroutineScope()
    LifecycleEffect(
        onStarted = {
            job = coroutineScope.launch { block() }
        },
        onDisposed = {
            job?.cancel()
        },
    )
}

// endregion

// region 时间

const val zeroTime: Long = 8 * 60 * 60 * 1000L

val dateFormat = LocalDateTime.Format {
    year(); char('-'); monthNumber(); char('-'); dayOfMonth()
}

val timeFormat = LocalDateTime.Format {
    hour(); char(':'); minute()
}

val dateTimeFormat = LocalDateTime.Format {
    year(); char('-'); monthNumber(); char('-'); dayOfMonth()
    char(' ')
    hour(); char(':'); minute(); char(':'); second()
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun weekNames() = listOf(
    stringResource(Res.string.week_name_monday),
    stringResource(Res.string.week_name_tuesday),
    stringResource(Res.string.week_name_wednesday),
    stringResource(Res.string.week_name_thursday),
    stringResource(Res.string.week_name_friday),
    stringResource(Res.string.week_name_saturday),
    stringResource(Res.string.week_name_sunday),
)

fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

fun LocalDate.format(): String = LocalDate.Format {
    year(); char('-'); monthNumber(); char('-'); dayOfMonth()
}.format(this)

fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun getWeekOfDate(date: LocalDate): List<LocalDate> {
    val result = mutableListOf<LocalDate>()
    val dayOfWeek = date.dayOfWeek.isoDayNumber
    for (day in (1..7)) {
        result.add(
            if (day < dayOfWeek)
                date.minus(DatePeriod(days = dayOfWeek - day))
            else
                date.plus(DatePeriod(days = day - dayOfWeek))
        )
    }
    return result
}

fun List<LocalDate>.forwardWeekList(): List<LocalDate> {
    val result = this.toMutableList()
    result.removeLastOrNull()
    result.add(0, (this.firstOrNull() ?: return result).minus(DatePeriod(days = 1)))
    return result
}

fun millisToDateTime(millis: Long): LocalDateTime =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())

fun millisToTime(millis: Long): LocalDateTime =
    Instant.fromEpochMilliseconds(millis - zeroTime)
        .toLocalDateTime(TimeZone.currentSystemDefault())

fun validDateStr(dateMillis: Long, action: (Long) -> String) =
    if (dateMillis > 0) action(dateMillis) else ""

fun hour2Millis(hour: Int): Long = hour * 60 * 60 * 1000L

fun min2Millis(min: Int): Long = min * 60 * 1000L

fun millis2HourAndMin(millis: Long): Pair<Int, Int> {
    val hour = (millis / (60 * 60 * 1000)).toInt()
    val min = (millis - hour * (60 * 60 * 1000)) / (60 * 1000)
    return Pair(hour, min.toInt())
}

// endregion


// region 系统

fun isEn() = Locale.current.language == "en"

// endregion


// region 列表

/**
 * 列表前移 n 个元素
 */
fun <T> MutableList<T>.forward(n: Int): List<T> {
    val list = mutableListOf<T>()
    repeat(n) {
        val elem = this.removeLast()
        list.add(0, elem)
    }
    list.addAll(this)
    return list
}

// endregion


// region Theme

@Composable
inline fun colorWithDark(lightColor: Color, darkColor: Color): Color =
    if (isSystemInDarkTheme()) darkColor else lightColor

fun Modifier.ifThen(bool: Boolean, modifier: Modifier): Modifier =
    this.then(if (bool) modifier else Modifier)

// endregion


// region Entity

fun getPriorityColor(priority: Int): Color = when (priority) {
    1 -> bluePriority
    2 -> greenPriority
    3 -> yellowPriority
    4 -> redPriority
    else -> Color(0xFFFFFF)
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun getPriorityName(priority: Int): String = when (priority) {
    1 -> stringResource(Res.string.new_text_blue_priority)
    2 -> stringResource(Res.string.new_text_green_priority)
    3 -> stringResource(Res.string.new_text_yellow_priority)
    4 -> stringResource(Res.string.new_text_red_priority)
    else -> ""
}

// endregion
