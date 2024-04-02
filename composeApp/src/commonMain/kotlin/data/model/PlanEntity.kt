package data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlanEntity(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("createDateTime")
    val createDateTime: String,
    @SerialName("startDateTime")
    val startDateTime: String,
    @SerialName("endDateTime")
    val endDateTime: String,
    @SerialName("priority")
    val priority: Int,
    @SerialName("progress")
    val progress: Int,
    @SerialName("remindDateTime")
    val remindDateTime: String,
)