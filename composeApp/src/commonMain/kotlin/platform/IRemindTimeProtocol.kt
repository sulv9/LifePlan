package platform

interface IRemindTimeProtocol {
    fun remindTime(title: String, description: String, reminderTime: Long)
}