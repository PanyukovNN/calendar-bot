package ru.panyukovnn.calendarbot.scheduler

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.panyukovnn.calendarbot.config.TgBotApi
import ru.panyukovnn.calendarbot.property.CalendarSenderProperties
import ru.panyukovnn.calendarbot.service.GoogleCalendarEventsFetcher
import java.time.*
import java.time.format.DateTimeFormatter

@Service
class SendingScheduler(
    val eventsFetcher: GoogleCalendarEventsFetcher,
    val calendarSenderProperties: CalendarSenderProperties,
    val tgBotApi: TgBotApi
) {

    val FRONT_DT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

    @Scheduled(cron = "\${calendar-sender.cron}")
    fun sendEvents() {
        val now = LocalDateTime.now();

        val events = eventsFetcher.fetchDayEvens(now)

        val messageLines = formatMessage(events, now)

        tgBotApi.execute(
            SendMessage.builder()
                .text(messageLines.joinToString(separator = "\n"))
                .parseMode("html")
                .chatId(calendarSenderProperties.chatId!!)
                .build()
        )
    }

    private fun formatMessage(events: List<Event>, now: LocalDateTime): MutableList<String> {
        val messageLines = mutableListOf<String>()

        if (events.isEmpty()) {
            messageLines.add("<b>" + now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "</b>")
            messageLines.add("")
            messageLines.add("Событий нет")
        } else {
            val tomorrowMidnight = LocalDateTime.of(now.toLocalDate().plusDays(1), LocalTime.MIN)
                .toInstant(ZoneOffset.of("+03:00"))
                .toEpochMilli()

            val todaysEvents = events.filter { isTodayEvent(it, tomorrowMidnight) }
            val specificTimeTodayEvents = filterSpecificTimeEvents(todaysEvents)

            if (!specificTimeTodayEvents.isEmpty()) {
                messageLines.add("<b>" + now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "</b>")

                enrichMessageWithSpecificTimeEvents(messageLines, specificTimeTodayEvents)
            }

            val tomorrowEvents = events.filter { !isTodayEvent(it, tomorrowMidnight) }

            if (!tomorrowEvents.isEmpty()) {
                messageLines.add("")
                messageLines.add("<b>" + now.plusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "</b>")

                enrichMessageWithDateEvents(messageLines, tomorrowEvents)
                enrichMessageWithSpecificTimeEvents(messageLines, tomorrowEvents)
            }
        }

        return messageLines
    }

    fun isTodayEvent(event: Event, tomorrowMidnight: Long): Boolean {
        return (event.start.dateTime != null && event.start.dateTime.value < tomorrowMidnight) ||
                (event.start.date != null && event.start.date.value < tomorrowMidnight)
    }

    fun enrichMessageWithDateEvents(messageLines: MutableList<String>, dayEvents: List<Event>) {
        val dateEvents = filterDateEvents(dayEvents)

        if (!dateEvents.isEmpty()) {
            messageLines.add("")

            for (event in dateEvents) {
                messageLines.add(event.summary ?: "Нет заголовка")
            }
        }
    }

    fun enrichMessageWithSpecificTimeEvents(messageLines: MutableList<String>, dayEvents: List<Event>) {
        val specificTimeEvents = filterSpecificTimeEvents(dayEvents)

        for (event in specificTimeEvents) {
            messageLines.add("")

            val start = formatGoogleDateTime(event.start.dateTime)
            val end = formatGoogleDateTime(event.end.dateTime)

            messageLines.add("$start - $end ${event.summary ?: "Нет заголовка"}")
        }
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

    /**
     * Извлекает события целого дня (например праздники)
     */
    fun filterDateEvents(dayEvents: List<Event>) = dayEvents
            .filter { it.start.dateTime == null || it.end.dateTime == null }

    /**
     * Излвекает события с конкретным временем
     */
    fun filterSpecificTimeEvents(dayEvents: List<Event>) = dayEvents
        .filter { it.start.dateTime != null && it.end.dateTime != null }

}
