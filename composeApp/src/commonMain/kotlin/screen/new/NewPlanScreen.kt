@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalResourceApi::class,
)

package screen.new

import LocalSnackBarHostState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import event.LifePlanEvent
import event.OnPlanCreateSuccess
import lifeplan.composeapp.generated.resources.Res
import lifeplan.composeapp.generated.resources.new_text_create_plan
import lifeplan.composeapp.generated.resources.new_toast_end_date_not_null
import lifeplan.composeapp.generated.resources.new_toast_incorrect_date
import lifeplan.composeapp.generated.resources.new_toast_start_date_not_null
import lifeplan.composeapp.generated.resources.new_toast_title_not_null
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import theme.blue700
import util.DateTimeDialogShow
import util.PlanDetails
import util.launchWhenStart

class NewPlanScreen : Screen {
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
                        LifePlanEvent.sendEvent(OnPlanCreateSuccess)
                        navigator.pop()
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

    DateTimeDialogShow(
        showDatePickDialog = viewModel.showDatePickDialog,
        isShowStartDatePickDialog = isShowStartDatePickDialog,
        startDate = viewModel.startDate,
        endDate = viewModel.endDate,
        onStartDateUpdate = viewModel::updateStartDate,
        onEndDateUpdate = viewModel::updateEndDate,
        onDatePickDialogDismiss = { viewModel.updateShowDatePickDialog(false) },
        showTimePickDialog = viewModel.showTimePickDialog,
        startTime = viewModel.startTime,
        endTime = viewModel.endTime,
        onStartTimeUpdate = viewModel::updateStartTime,
        onEndTimeUpdate = viewModel::updateEndTime,
        onTimePickDialogDismiss = { viewModel.updateShowTimePickDialog(false) },
    )

    PlanDetails(
        title = viewModel.title,
        onTitleChange = viewModel::updatePlanTitle,
        isTitleError = viewModel.titleInputError,
        desc = viewModel.desc,
        onDescChange = viewModel::updatePlanDesc,
        priority = viewModel.priority,
        onPriorityChange = viewModel::updatePriority,
        startDate = viewModel.startDate,
        isStartDateError = viewModel.startDateError,
        onStartDateClick = {
            isShowStartDatePickDialog.value = true
            viewModel.updateShowDatePickDialog(true)
        },
        startTime = viewModel.startTime,
        onStartTimeClick = {
            isShowStartDatePickDialog.value = true
            viewModel.updateShowTimePickDialog(true)
        },
        endDate = viewModel.endDate,
        isEndDateError = viewModel.endDateError,
        onEndDateClick = {
            isShowStartDatePickDialog.value = false
            viewModel.updateShowDatePickDialog(true)
        },
        endTime = viewModel.endTime,
        onEndTimeClick = {
            isShowStartDatePickDialog.value = false
            viewModel.updateShowTimePickDialog(true)
        },
        progress = viewModel.progress,
        onProgressChange = viewModel::updateProgress,
        bottomButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    modifier = Modifier.width(it.maxWidth / 2),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = blue700
                    ),
                    onClick = { viewModel.createPlan() }
                ) {
                    Text(stringResource(Res.string.new_text_create_plan))
                }
            }
        }
    )
}