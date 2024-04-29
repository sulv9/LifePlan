package util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import lifeplan.composeapp.generated.resources.Res
import lifeplan.composeapp.generated.resources.new_dialog_cancel
import lifeplan.composeapp.generated.resources.new_dialog_ok
import lifeplan.composeapp.generated.resources.new_label_plan_desc
import lifeplan.composeapp.generated.resources.new_label_plan_title
import lifeplan.composeapp.generated.resources.new_text_current_progress
import lifeplan.composeapp.generated.resources.new_text_priority
import lifeplan.composeapp.generated.resources.new_tip_end_date
import lifeplan.composeapp.generated.resources.new_tip_end_time
import lifeplan.composeapp.generated.resources.new_tip_remind_date_time
import lifeplan.composeapp.generated.resources.new_tip_start_date
import lifeplan.composeapp.generated.resources.new_tip_start_time
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import theme.blue100
import theme.blue200
import theme.blue50
import theme.blue700
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeDialogShow(
    showDatePickDialog: Boolean = false,
    isShowStartDatePickDialog: MutableState<Boolean> = mutableStateOf(false),
    startDate: Long = 0L,
    endDate: Long = 0L,
    onStartDateUpdate: (Long) -> Unit = {},
    onEndDateUpdate: (Long) -> Unit = {},
    onDatePickDialogDismiss: () -> Unit = {},
    showTimePickDialog: Boolean = false,
    startTime: Long = 0L,
    endTime: Long = 0L,
    onStartTimeUpdate: (Long) -> Unit = {},
    onEndTimeUpdate: (Long) -> Unit = {},
    onTimePickDialogDismiss: () -> Unit = {},
    showRemindDateDialog: Boolean = false,
    showRemindTimeDialog: Boolean = false,
    remindDateTime: String = "",
    onRemindTimeDialogConfirm: (String) -> Unit = {},
    onRemindDateDialogDismiss: () -> Unit = {},
    onRemindTimeDialogShow: () -> Unit = {},
    onRemindTimeDialogDismiss: () -> Unit = {},
) {
    if (showDatePickDialog) {
        val datePickerState = rememberDatePickerState()

        if (isShowStartDatePickDialog.value && startDate >= 0L) {
            datePickerState.selectedDateMillis = startDate
        } else if (isShowStartDatePickDialog.value.not() && endDate >= 0L) {
            datePickerState.selectedDateMillis = endDate
        }

        DateDialog(
            datePickerState = datePickerState,
            onDatePickDialogDismiss = onDatePickDialogDismiss,
            onConfirmClick = {
                datePickerState.selectedDateMillis?.let {
                    if (isShowStartDatePickDialog.value)
                        onStartDateUpdate(it)
                    else
                        onEndDateUpdate(it)
                }
                onDatePickDialogDismiss()
            }
        )
    }

    if (showTimePickDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = if (isShowStartDatePickDialog.value && startTime >= 0L) {
                millis2HourAndMin(startTime).first
            } else if (isShowStartDatePickDialog.value.not() && endTime >= 0L) {
                millis2HourAndMin(endTime).first
            } else 0,
            initialMinute = if (isShowStartDatePickDialog.value && startTime >= 0L) {
                millis2HourAndMin(startTime).second
            } else if (isShowStartDatePickDialog.value.not() && endTime >= 0L) {
                millis2HourAndMin(endTime).second
            } else 0,
        )

        TimeDialog(
            timePickerState = timePickerState,
            onTimePickDialogDismiss = onTimePickDialogDismiss,
            onConfirmClick = {
                val timeMillis = hour2Millis(timePickerState.hour) +
                        min2Millis(timePickerState.minute)
                if (isShowStartDatePickDialog.value)
                    onStartTimeUpdate(timeMillis)
                else
                    onEndTimeUpdate(timeMillis)
                onTimePickDialogDismiss()
            }
        )
    }

    if (showRemindDateDialog) {
        val datePickerState = rememberDatePickerState()
        if (remindDateTime.isNotBlank())
            datePickerState.selectedDateMillis = parseDate2Millis("$remindDateTime:00")
        DateDialog(
            datePickerState = datePickerState,
            onDatePickDialogDismiss = onRemindDateDialogDismiss,
            onConfirmClick = {
                datePickerState.selectedDateMillis?.let {
                    onRemindTimeDialogConfirm(dateFormat.format(millisToDateTime(it)))
                    onRemindTimeDialogShow()
                }
                onRemindDateDialogDismiss()
            }
        )
    }

    if (showRemindTimeDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = remindDateTime.split(" ")
                .run { if (size <= 1) 0 else get(1).split(":")[0].toInt() },
            initialMinute = remindDateTime.split(" ")
                .run { if (size <= 1) 0 else get(2).split(":")[0].toInt() },
        )
        TimeDialog(
            timePickerState = timePickerState,
            onTimePickDialogDismiss = {
                onRemindTimeDialogDismiss()
                onRemindTimeDialogConfirm("")
            },
            onConfirmClick = {
                val timeMillis = hour2Millis(timePickerState.hour) +
                        min2Millis(timePickerState.minute)
                onRemindTimeDialogConfirm(
                    remindDateTime + " " + timeFormat.format(millisToDateTime(timeMillis - zeroTime))
                )
                onRemindTimeDialogDismiss()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun DateDialog(
    datePickerState: DatePickerState,
    onConfirmClick: () -> Unit = {},
    onDatePickDialogDismiss: () -> Unit = {},
) {
    DatePickerDialog(
        onDismissRequest = onDatePickDialogDismiss,
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(
                    stringResource(Res.string.new_dialog_ok),
                    color = blue800
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDatePickDialogDismiss) {
                Text(
                    stringResource(Res.string.new_dialog_cancel),
                    color = blue800
                )
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = blue50
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                todayContentColor = blue700,
                todayDateBorderColor = blue800,
                selectedDayContainerColor = blue800,
            )
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
fun TimeDialog(
    timePickerState: TimePickerState,
    onTimePickDialogDismiss: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
) {
    DatePickerDialog(
        onDismissRequest = onTimePickDialogDismiss,
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(
                    stringResource(Res.string.new_dialog_ok),
                    color = blue800
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onTimePickDialogDismiss) {
                Text(
                    stringResource(Res.string.new_dialog_cancel),
                    color = blue800
                )
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = blue50
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = blue100,
                    selectorColor = blue800,
                    timeSelectorSelectedContainerColor = blue200,
                    timeSelectorUnselectedContainerColor = blue100,
                )
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlanDetails(
    title: String = "",
    onTitleChange: (String) -> Unit = {},
    isTitleError: Boolean = false,
    desc: String = "",
    onDescChange: (String) -> Unit = {},
    priority: Float = 0F,
    onPriorityChange: (Float) -> Unit = {},
    startDate: Long = -1L,
    isStartDateError: Boolean = false,
    onStartDateClick: () -> Unit = {},
    startTime: Long = -1L,
    onStartTimeClick: () -> Unit = {},
    endDate: Long = -1L,
    isEndDateError: Boolean = false,
    onEndDateClick: () -> Unit = {},
    endTime: Long = -1L,
    onEndTimeClick: () -> Unit = {},
    progress: Float = 0F,
    onProgressChange: (Float) -> Unit = {},
    remindDateTime: String = "",
    onRemindDateTimeClick: () -> Unit = {},
    bottomButton: @Composable (BoxWithConstraintsScope) -> Unit = {},
) {
    BoxWithConstraints {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 12.dp, horizontal = 12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { onTitleChange(it) },
                label = { Text(stringResource(Res.string.new_label_plan_title)) },
                isError = isTitleError,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = blue800,
                    focusedLabelColor = blue800,
                    focusedBorderColor = blue800,
                )
            )

            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = desc,
                onValueChange = { onDescChange(it) },
                label = { Text(stringResource(Res.string.new_label_plan_desc)) },
                modifier = Modifier.fillMaxWidth().height(144.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = blue800,
                    focusedLabelColor = blue800,
                    focusedBorderColor = blue800,
                )
            )

            Spacer(Modifier.height(18.dp))

            Column {
                Text(
                    text = stringResource(Res.string.new_text_priority) +
                            getPriorityName(priority.toInt())
                )

                Slider(
                    value = priority,
                    onValueChange = { onPriorityChange(it) },
                    colors = SliderDefaults.colors(
                        thumbColor = getPriorityColor(priority.toInt()),
                        activeTrackColor = getPriorityColor(priority.toInt())
                    ),
                    steps = 2,
                    valueRange = 1F..4F,
                )
            }

            Spacer(Modifier.height(4.dp))

            Column {
                Row {
                    OutlinedTextFieldButton(
                        value = validDateStr(startDate) {
                            dateFormat.format(millisToDateTime(it))
                        },
                        label = { Text(stringResource(Res.string.new_tip_start_date)) },
                        modifier = Modifier.weight(1F),
                        isError = isStartDateError,
                        onClick = onStartDateClick
                    )

                    Spacer(Modifier.width(12.dp))

                    OutlinedTextFieldButton(
                        value = validDateStr(startTime) {
                            timeFormat.format(millisToTime(it))
                        },
                        label = { Text(stringResource(Res.string.new_tip_start_time)) },
                        modifier = Modifier.weight(1F),
                        onClick = onStartTimeClick
                    )
                }

                Row {
                    OutlinedTextFieldButton(
                        value = validDateStr(endDate) {
                            dateFormat.format(millisToDateTime(it))
                        },
                        label = { Text(stringResource(Res.string.new_tip_end_date)) },
                        modifier = Modifier.weight(1F),
                        isError = isEndDateError,
                        onClick = onEndDateClick
                    )

                    Spacer(Modifier.width(12.dp))

                    OutlinedTextFieldButton(
                        value = validDateStr(endTime) {
                            timeFormat.format(millisToTime(it))
                        },
                        label = { Text(stringResource(Res.string.new_tip_end_time)) },
                        modifier = Modifier.weight(1F),
                        onClick = onEndTimeClick
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Column {
                Text(
                    text = stringResource(Res.string.new_text_current_progress) +
                            progress.toInt().toString() + "%"
                )

                Slider(
                    value = progress,
                    onValueChange = { onProgressChange(it) },
                    colors = SliderDefaults.colors(
                        thumbColor = getProgressColor(progress),
                        activeTrackColor = getProgressColor(progress),
                        inactiveTrackColor = Color.Gray.copy(0.25F)
                    ),
                    valueRange = 0F..100F,
                    thumb = {
                        Box(
                            Modifier.clip(CircleShape)
                                .size(20.dp)
                                .background(getProgressColor(progress)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getProgressIcon(progress),
                                contentDescription = stringResource(Res.string.new_text_current_progress) +
                                        progress.toInt().toString() + "%",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                )
            }

            Spacer(Modifier.height(4.dp))

            OutlinedTextFieldButton(
                value = remindDateTime,
                label = { Text(stringResource(Res.string.new_tip_remind_date_time)) },
                modifier = Modifier.fillMaxWidth(),
                onClick = onRemindDateTimeClick
            )

            Spacer(Modifier.height(24.dp))

            bottomButton(this@BoxWithConstraints)
        }
    }
}