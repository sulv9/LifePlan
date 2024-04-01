package screen.main

sealed class MainEvent {
    data object ShowPlanCreateSuccess : MainEvent()
}