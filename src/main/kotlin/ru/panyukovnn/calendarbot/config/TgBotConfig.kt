package ru.panyukovnn.calendarbot.config

import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import ru.panyukovnn.calendarbot.property.TgBotProperties


@Configuration
class TgBotConfig {

    @Bean
    fun botApi(eventPublisher: ApplicationEventPublisher, botProperties: TgBotProperties, commands: List<BotCommand>) : TgBotApi {
        val botApi = TgBotApi(botProperties.name, botProperties.token, eventPublisher)

        TelegramBotsApi(DefaultBotSession::class.java).registerBot(botApi)

        commands.forEach { botCommand: BotCommand? -> botApi.register(botCommand) }

        return botApi
    }
}