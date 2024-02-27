package ru.panyukovnn.calendarbot.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import ru.panyukovnn.calendarbot.scheduler.SendingScheduler

@RestController
class CalendarController(val sendingScheduler: SendingScheduler) {

    @PostMapping("/")
    fun runSending() {
        sendingScheduler.sendEvents()
    }
}