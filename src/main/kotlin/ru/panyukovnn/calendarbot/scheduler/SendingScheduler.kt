package ru.panyukovnn.calendarbot.scheduler

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.panyukovnn.calendarbot.config.TgBotApi
import ru.panyukovnn.calendarbot.property.CalendarSenderProperties
import ru.panyukovnn.calendarbot.service.GoogleCalendarEventsFetcher
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class SendingScheduler(
    val eventsFetcher: GoogleCalendarEventsFetcher,
    val calendarSenderProperties: CalendarSenderProperties,
    val tgBotApi: TgBotApi
) {

    val FRONT_DT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Scheduled(cron = "\${calendar-sender.cron}")
    fun sendEvents() {
        val events = eventsFetcher.fetchDayEvens()

        val messageLines = formatMessage(events)

        tgBotApi.execute(SendMessage.builder()
            .text(messageLines.joinToString(separator = "\n"))
            .parseMode("html")
            .chatId(calendarSenderProperties.chatId!!)
            .build())
    }

    private fun formatMessage(events: List<Event>): MutableList<String> {
        val messageLines = mutableListOf<String>()
        messageLines.add("<b>" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "</b>")

        if (events.isEmpty()) {
            messageLines.add("")
            messageLines.add("Событий нет")
        } else {
            val noTimeEvents = events.filter {
                it.start.dateTime == null || it.end.dateTime == null
            }

            val hasTimeEvents = events.filter {
                it.start.dateTime != null && it.end.dateTime != null
            }

            if (!noTimeEvents.isEmpty()) {
                messageLines.add("")

                for (event in noTimeEvents) {
                    messageLines.add(event.summary ?: "Нет заголовка")
                }
            }

            for (event in hasTimeEvents) {
                messageLines.add("")
                messageLines.add(event.summary ?: "Нет заголовка")

                val start = formatGoogleDateTime(event.start.dateTime)
                val end = formatGoogleDateTime(event.end.dateTime)

                messageLines.add("$start - $end")
            }
        }
        return messageLines
    }

    fun formatGoogleDateTime(dateTime: DateTime?): String? {
        if (dateTime == null) {
            return null
        }

        return Instant.ofEpochMilli(dateTime.value)
            .atZone(ZoneId.of("Europe/Moscow"))
            .toLocalDateTime()
            .format(FRONT_DT_FORMATTER);
    }
}
