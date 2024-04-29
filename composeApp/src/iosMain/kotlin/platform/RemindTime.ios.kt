package platform

private lateinit var remindTimeProtocol: IRemindTimeProtocol

fun setRemindTimeProtocol(protocol: IRemindTimeProtocol) {
    remindTimeProtocol = protocol
}

actual fun remindTime(title: String, description: String, reminderTime: Long) {
    remindTimeProtocol.remindTime(title, description, reminderTime)
}