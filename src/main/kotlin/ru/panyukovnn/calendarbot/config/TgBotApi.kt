package ru.panyukovnn.calendarbot.config

import org.springframework.context.ApplicationEventPublisher
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
import org.telegram.telegrambots.meta.api.objects.Update

class TgBotApi(
    val username: String,
    val token: String,
    val eventPublisher: ApplicationEventPublisher
) : TelegramLongPollingCommandBot() {

    override fun getBotUsername() = username

    override fun getBotToken() = token

    override fun processNonCommandUpdate(update: Update) {
        println(update)
    }
}