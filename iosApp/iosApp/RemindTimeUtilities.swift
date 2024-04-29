//
//  RemindTimeUtilities.swift
//  iosApp
//
//  Created by sulv on 2024/4/29.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import ComposeApp
import EventKit

class RemindTimeUtilities {
    func initRemindProtocol() {
        let remindTime = RemindTime()
        RemindTime_iosKt.setRemindTimeProtocol(protocol: remindTime)
    }
    
    class RemindTime : IRemindTimeProtocol {
        func remindTime(title: String, description: String, reminderTime: Int64) {
            let eventStore = EKEventStore()
            eventStore.requestAccess(to: .event) { (granted, error) in
                if (granted) && (error == nil) {
                    // 新建一个事件
                    let event:EKEvent = EKEvent(eventStore: eventStore)
                    event.title = title
                    let date = Date(timeIntervalSince1970: TimeInterval(reminderTime / 1000))
                    event.startDate = date
                    event.endDate = date
                    event.notes = description
                    event.calendar = eventStore.defaultCalendarForNewEvents
                    do {
                        try eventStore.save(event, span: .thisEvent)
                    } catch {}
                }
            }
        }
    }
}
