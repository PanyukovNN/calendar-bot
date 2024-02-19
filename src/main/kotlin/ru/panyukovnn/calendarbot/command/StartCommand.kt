package ru.panyukovnn.calendarbot.command

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

@Service
class StartCommand : BotCommand("start", "Start command") {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun execute(absSender: AbsSender, user: User, chat: Chat, arguments: Array<out String>) {
        try {
            absSender.execute(
                SendMessage.builder()
                    .chatId(chat.id)
                    .text("Working")
                    .build()
            )
        } catch (e: Exception) {
            log.error("Exception at start command {}: {}", chat.id, e.message, e)
        }
    }
}