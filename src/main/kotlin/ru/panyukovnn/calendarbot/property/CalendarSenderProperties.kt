package ru.panyukovnn.calendarbot.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "calendar-sender")
data class CalendarSenderProperties (
    var chatId: Long? = null
)