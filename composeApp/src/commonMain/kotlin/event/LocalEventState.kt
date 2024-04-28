package event

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object LifePlanEvent {
    var event by mutableStateOf<ILifePlanEvent>(NoneLifePlanEvent)
        private set

    fun sendEvent(event: ILifePlanEvent) {
        this.event = event
    }
}