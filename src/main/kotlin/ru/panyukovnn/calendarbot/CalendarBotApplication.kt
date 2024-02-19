package ru.panyukovnn.calendarbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CalendarBotApplication

fun main(args: Array<String>) {
    runApplication<CalendarBotApplication>(*args)
}