package ru.panyukovnn.calendarbot.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "bot")
data class TgBotProperties (
    var name: String = "",
    var token: String = ""
)