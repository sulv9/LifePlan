package screen.new

sealed class NewPlanEvent {
    data object ShowTitleInputError : NewPlanEvent()

    data object ShowStartDateError : NewPlanEvent()

    data object ShowEndDateError : NewPlanEvent()

    data object IncorrectStartEndDate : NewPlanEvent()

    data object CreatePlanSuccess : NewPlanEvent()
}