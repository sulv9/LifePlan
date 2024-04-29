@file:OptIn(ExperimentalResourceApi::class)

package screen.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import lifeplan.composeapp.generated.resources.Res
import lifeplan.composeapp.generated.resources.detail_delete_plan
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import org.koin.core.parameter.parametersOf
import util.DateTimeDialogShow
import util.PlanDetails

class DetailScreen(private val id: Long) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val detailScreenModel = getScreenModel<DetailScreenModel> { parametersOf(id) }

        DetailScreenContent(
            viewModel = detailScreenModel,
            onPlanDeleted = {
                detailScreenModel.deletePlan()
                navigator.pop()
            }
        )
    }
}

@Composable
private fun DetailScreenContent(
    viewModel: DetailScreenModel,
    onPlanDeleted: () -> Unit = {},
) {
    val isShowStartDatePickDialog = remember { mutableStateOf(false) }

    DateTimeDialogShow(
        showDatePickDialog = viewModel.showDatePickDialog,
        isShowStartDatePickDialog = isShowStartDatePickDialog,
        startDate = viewModel.startDate,
        endDate = viewModel.endDate,
        onStartDateUpdate = viewModel::updatePlanStartDate,
        onEndDateUpdate = viewModel::updatePlanEndDate,
        onDatePickDialogDismiss = { viewModel.updateShowDatePickDialog(false) },
        showTimePickDialog = viewModel.showTimePickDialog,
        startTime = viewModel.startTime.let { if (it < 0) 0 else it },
        endTime = viewModel.endTime.let { if (it < 0) 0 else it },
        onStartTimeUpdate = viewModel::updatePlanStartTime,
        onEndTimeUpdate = viewModel::updatePlanEndTime,
        onTimePickDialogDismiss = { viewModel.updateShowTimePickDialog(false) },
        showRemindDateDialog = viewModel.showRemindDatePickDialog,
        showRemindTimeDialog = viewModel.showRemindTimePickDialog,
        remindDateTime = viewModel.remindDateTime.substringBefore(":00"),
        onRemindTimeDialogConfirm = viewModel::updatePlanRemindDateTime,
        onRemindDateDialogDismiss = { viewModel.updateShowRemindDatePickDialog(false) },
        onRemindTimeDialogShow = { viewModel.updateShowRemindTimePickDialog(true) },
        onRemindTimeDialogDismiss = { viewModel.updateShowRemindTimePickDialog(false) },
    )

    PlanDetails(
        title = viewModel.title,
        onTitleChange = viewModel::updatePlanTitle,
        desc = viewModel.desc,
        onDescChange = viewModel::updatePlanDesc,
        priority = viewModel.priority.toFloat(),
        onPriorityChange = viewModel::updatePlanPriority,
        startDate = viewModel.startDate,
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
        onEndDateClick = {
            isShowStartDatePickDialog.value = false
            viewModel.updateShowDatePickDialog(true)
        },
        endTime = viewModel.endTime,
        onEndTimeClick = {
            isShowStartDatePickDialog.value = false
            viewModel.updateShowTimePickDialog(true)
        },
        progress = viewModel.progress.toFloat(),
        onProgressChange = viewModel::updatePlanProgress,
        remindDateTime = viewModel.remindDateTime,
        onRemindDateTimeClick = { viewModel.updateShowRemindDatePickDialog(true) },
        bottomButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    modifier = Modifier.width(it.maxWidth / 2),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.75f),
                    ),
                    onClick = onPlanDeleted,
                ) {
                    Text(stringResource(Res.string.detail_delete_plan))
                }
            }
        }
    )
}