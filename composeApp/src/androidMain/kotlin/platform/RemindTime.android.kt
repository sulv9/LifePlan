package platform

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import org.koin.mp.KoinPlatform
import java.util.Calendar

private const val CALENDAR_URL = "content://com.android.calendar/calendars/"
private const val CALENDER_EVENT_URL = "content://com.android.calendar/events"
private const val CALENDER_REMINDER_URL = "content://com.android.calendar/reminders"

var remindTimeCalendarRequest: ActivityResultLauncher<Array<String>>? = null
private var calendarTitle: String = ""
private var calendarDesc: String = ""
private var calendarTime: Long = 0L
actual fun remindTime(title: String, description: String, reminderTime: Long) {
    val context: Context = KoinPlatform.getKoin().get()
    calendarTitle = title
    calendarDesc = description
    calendarTime = reminderTime
    try {
        if (
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            addCalendarEvent(context)
        } else {
            // 申请权限
            remindTimeCalendarRequest?.launch(
                arrayOf(
                    Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.READ_CALENDAR
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun addCalendarEvent(context: Context) {
    val calId: Int = checkAccount(context) // 获取日历账户的id
    if (calId < 0) { // 获取账户id失败直接返回，添加日历事件失败
        return
    }
    // 添加日历事件
    val newEvent = with(ContentValues()) {
        put("title", calendarTitle)
        put("description", calendarDesc)
        put("calendar_id", calId)
        val calendar = Calendar.getInstance().apply { timeInMillis = calendarTime }
        put(CalendarContract.Events.DTSTART, calendar.time.time)
        put(CalendarContract.Events.DTEND, calendar.time.time)
        put(CalendarContract.Events.HAS_ALARM, 1) //设置有闹钟提醒
        put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Shanghai") //这个是时区，必须有
        context.contentResolver.insert(Uri.parse(CALENDER_EVENT_URL), this) //添加事件
    } ?: return
    // 事件提醒的设定
    ContentValues().apply {
        put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(newEvent))
        put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        context.contentResolver.insert(Uri.parse(CALENDER_REMINDER_URL), this)
    }
}

@SuppressLint("Range")
private fun checkAccount(context: Context): Int {
    val userCursor = context.contentResolver.query(Uri.parse(CALENDAR_URL), null, null, null, null)
    return userCursor.use { cursor ->
        if (cursor == null) { //查询返回空值
            return -1
        }
        if (cursor.count > 0) { // 存在现有账户，取第一个账户的id返回
            cursor.moveToFirst()
            cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars._ID))
        } else {
            -1
        }
    }
}