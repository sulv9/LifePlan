@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalResourceApi::class,
    ExperimentalResourceApi::class
)

package screen.new

import LocalSnackBarHostState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import lifeplan.composeapp.generated.resources.Res
import lifeplan.composeapp.generated.resources.new_dialog_cancel
import lifeplan.composeapp.generated.resources.new_dialog_ok
import lifeplan.composeapp.generated.resources.new_label_plan_desc
import lifeplan.composeapp.generated.resources.new_label_plan_title
import lifeplan.composeapp.generated.resources.new_text_create_plan
import lifeplan.composeapp.generated.resources.new_text_current_progress
import lifeplan.composeapp.generated.resources.new_text_priority
import lifeplan.composeapp.generated.resources.new_tip_end_date
import lifeplan.composeapp.generated.resources.new_tip_end_time
import lifeplan.composeapp.generated.resources.new_tip_start_date
import lifeplan.composeapp.generated.resources.new_tip_start_time
import lifeplan.composeapp.generated.resources.new_toast_end_date_not_null
import lifeplan.composeapp.generated.resources.new_toast_incorrect_date
import lifeplan.composeapp.generated.resources.new_toast_start_date_not_null
import lifeplan.composeapp.generated.resources.new_toast_title_not_null
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import theme.blue100
import theme.blue200
import theme.blue50
import theme.blue700
import theme.blue800
import util.OutlinedTextFieldButton
import util.dateFormat
import util.getPriorityColor
import util.getPriorityName
import util.getProgressColor
import util.getProgressIcon
import util.hour2Millis
import util.launchWhenStart
import util.millis2HourAndMin
import util.millisToDateTime
import util.millisToTime
import util.min2Millis
import util.timeFormat
import util.validDateStr

class NewPlanScreen(
    private val onCreatePlanSuccess: (() -> Unit)? = {},
) : Screen {
    @Composable
    override fun Content() {
        val titleNotNullTip = stringResource(Res.string.new_toast_title_not_null)
        val startDateNotNullTip = stringResource(Res.string.new_toast_start_date_not_null)
        val endDateNotNullTip = stringResource(Res.string.new_toast_end_date_not_null)
        val incorrectDateTip = stringResource(Res.string.new_toast_incorrect_date)

        val navigator = LocalNavigator.currentOrThrow
        val newScreenModel = getScreenModel<NewPlanScreenModel>()
        val snackBarHostState = LocalSnackBarHostState.current

        launchWhenStart {
            newScreenModel.eventFlow.collect {
                when (it) {
                    is NewPlanEvent.ShowTitleInputError -> snackBarHostState?.showSnackbar(
                        message = titleNotNullTip,
                        withDismissAction = true
                    )

                    is NewPlanEvent.ShowStartDateError -> snackBarHostState?.showSnackbar(
                        message = startDateNotNullTip,
                        withDismissAction = true
                    )

                    is NewPlanEvent.ShowEndDateError -> snackBarHostState?.showSnackbar(
                        message = endDateNotNullTip,
                        withDismissAction = true
                    )

                    is NewPlanEvent.IncorrectStartEndDate -> snackBarHostState?.showSnackbar(
                        message = incorrectDateTip,
                        withDismissAction = true
                    )

                    is NewPlanEvent.CreatePlanSuccess -> {
                        navigator.pop()
                        onCreatePlanSuccess?.invoke()
                    }
                }
            }
        }

        NewPlanScreenContent(
            viewModel = newScreenModel,
        )
    }
}

@Composable
private fun NewPlanScreenContent(
    viewModel: NewPlanScreenModel,
) {
    val isShowStartDatePickDialog = remember { mutableStateOf(false) }

    if (viewModel.showDatePickDialog) {
        val datePickerState = rememberDatePickerState()

        if (isShowStartDatePickDialog.value && viewModel.startDate >= 0L) {
            datePickerState.selectedDateMillis = viewModel.startDate
        } else if (isShowStartDatePickDialog.value.not() && viewModel.endDate >= 0L) {
            datePickerState.selectedDateMillis = viewModel.endDate
        }

        DatePickerDialog(
            onDismissRequest = { viewModel.updateShowDatePickDialog(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        if (isShowStartDatePickDialog.value)
                            viewModel.updateStartDate(it)
                        else
                            viewModel.updateEndDate(it)
                    }
                    viewModel.updateShowDatePickDialog(false)
                }) {
                    Text(
                        stringResource(Res.string.new_dialog_ok),
                        color = blue800
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.updateShowDatePickDialog(false) }) {
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

    if (viewModel.showTimePickDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = if (isShowStartDatePickDialog.value && viewModel.startTime >= 0L) {
                millis2HourAndMin(viewModel.startTime).first
            } else if (isShowStartDatePickDialog.value.not() && viewModel.endTime >= 0L) {
                millis2HourAndMin(viewModel.endTime).first
            } else 0,
            initialMinute = if (isShowStartDatePickDialog.value && viewModel.startTime >= 0L) {
                millis2HourAndMin(viewModel.startTime).second
            } else if (isShowStartDatePickDialog.value.not() && viewModel.endTime >= 0L) {
                millis2HourAndMin(viewModel.endTime).second
            } else 0,
        )

        DatePickerDialog(
            onDismissRequest = { viewModel.updateShowTimePickDialog(false) },
            confirmButton = {
                TextButton(onClick = {
                    val timeMillis = hour2Millis(timePickerState.hour) +
                            min2Millis(timePickerState.minute)
                    if (isShowStartDatePickDialog.value)
                        viewModel.updateStartTime(timeMillis)
                    else
                        viewModel.updateEndTime(timeMillis)
                    viewModel.updateShowTimePickDialog(false)
                }) {
                    Text(
                        stringResource(Res.string.new_dialog_ok),
                        color = blue800
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.updateShowTimePickDialog(false) }) {
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

    BoxWithConstraints {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 12.dp, horizontal = 12.dp)
        ) {
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.updatePlanTitle(it) },
                label = { Text(stringResource(Res.string.new_label_plan_title)) },
                isError = viewModel.titleInputError,
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
                value = viewModel.desc,
                onValueChange = { viewModel.updatePlanDesc(it) },
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
                            getPriorityName(viewModel.priority.toInt())
                )

                Slider(
                    value = viewModel.priority,
                    onValueChange = { viewModel.updatePriority(it) },
                    colors = SliderDefaults.colors(
                        thumbColor = getPriorityColor(viewModel.priority.toInt()),
                        activeTrackColor = getPriorityColor(viewModel.priority.toInt())
                    ),
                    steps = 2,
                    valueRange = 1F..4F,
                )
            }

            Spacer(Modifier.height(4.dp))

            Column {
                Row {
                    OutlinedTextFieldButton(
                        value = validDateStr(viewModel.startDate) {
                            dateFormat.format(millisToDateTime(it))
                        },
                        label = { Text(stringResource(Res.string.new_tip_start_date)) },
                        modifier = Modifier.weight(1F),
                        isError = viewModel.startDateError,
                        onClick = {
                            isShowStartDatePickDialog.value = true
                            viewModel.updateShowDatePickDialog(true)
                        }
                    )

                    Spacer(Modifier.width(12.dp))

                    OutlinedTextFieldButton(
                        value = validDateStr(viewModel.startTime) {
                            timeFormat.format(millisToTime(it))
                        },
                        label = { Text(stringResource(Res.string.new_tip_start_time)) },
                        modifier = Modifier.weight(1F),
                        onClick = {
                            isShowStartDatePickDialog.value = true
                            viewModel.updateShowTimePickDialog(true)
                        }
                    )
                }

                Row {
                    OutlinedTextFieldButton(
                        value = validDateStr(viewModel.endDate) {
                            dateFormat.format(millisToDateTime(it))
                        },
                        label = { Text(stringResource(Res.string.new_tip_end_date)) },
                        modifier = Modifier.weight(1F),
                        isError = viewModel.endDateError,
                        onClick = {
                            isShowStartDatePickDialog.value = false
                            viewModel.updateShowDatePickDialog(true)
                        }
                    )

                    Spacer(Modifier.width(12.dp))

                    OutlinedTextFieldButton(
                        value = validDateStr(viewModel.endTime) {
                            timeFormat.format(millisToTime(it))
                        },
                        label = { Text(stringResource(Res.string.new_tip_end_time)) },
                        modifier = Modifier.weight(1F),
                        onClick = {
                            isShowStartDatePickDialog.value = false
                            viewModel.updateShowTimePickDialog(true)
                        }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Column {
                Text(
                    text = stringResource(Res.string.new_text_current_progress) +
                            viewModel.progress.toInt().toString() + "%"
                )

                Slider(
                    value = viewModel.progress,
                    onValueChange = { viewModel.updateProgress(it) },
                    colors = SliderDefaults.colors(
                        thumbColor = getProgressColor(viewModel.progress),
                        activeTrackColor = getProgressColor(viewModel.progress),
                        inactiveTrackColor = Color.Gray.copy(0.25F)
                    ),
                    valueRange = 0F..100F,
                    thumb = {
                        Box(
                            Modifier.clip(CircleShape)
                                .size(20.dp)
                                .background(getProgressColor(viewModel.progress)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getProgressIcon(viewModel.progress),
                                contentDescription = stringResource(Res.string.new_text_current_progress) +
                                        viewModel.progress.toInt().toString() + "%",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                )
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    modifier = Modifier.width(this@BoxWithConstraints.maxWidth / 2),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = blue700
                    ),
                    onClick = { viewModel.createPlan() }
                ) {
                    Text(stringResource(Res.string.new_text_create_plan))
                }
            }
        }
    }
}