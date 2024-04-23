package bot.command

import bot.BotHelper
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class AddEventCommand : BotCommand(COMMAND_NAME, DESCRIPTION) {

    companion object {
        private const val COMMAND_NAME = "addevent"
        private const val DESCRIPTION = "Add event"
        private const val EVENT_ADDED = "Мероприятие добавлено."
    }

    enum class Error(val errorText: String) {
        NOT_ENOUGH_PARAMS("Недостаточно параметров."),
        DATE_INVALID("Невалидная дата."),
        TIME_INVALID("Невалидное время.")
    }

    override fun execute(bot: AbsSender, user: User, chat: Chat, args: Array<out String>) {}

    override fun processMessage(bot: AbsSender, message: Message, arguments: Array<out String>) {
        super.processMessage(bot, message, arguments)
        if (!BotHelper.isAdmin(message.chatId)) {
            return
        }
        if (addEvent(message.text.removePrefix("/$COMMAND_NAME\n"), bot, message.chatId)) {
            try {
                bot.execute(
                    SendMessage.builder()
                        .chatId(message.chatId)
                        .text(EVENT_ADDED)
                        .build()
                )
            } catch (e: TelegramApiException) {
                e.printStackTrace()
            }
        }
    }

    private fun addEvent(eventDescription: String, bot: AbsSender, chatId: Long): Boolean {
        eventDescription.split("\n", limit = 3).apply {
            if (this.size < 3) {
                sendErrorMessage(Error.NOT_ENOUGH_PARAMS, bot, chatId)
                return false
            }
            if (!this.first().matches(Regex("^\\d\\d\\d\\d\\.\\d\\d\\.\\d\\d$"))) {
                sendErrorMessage(Error.DATE_INVALID, bot, chatId)
                return false
            }
            val date = this.first().split(".").map {
                it.toInt()
            }
            val localDate = LocalDate(date[0], date[1], date[2])
            if (!this[1].matches(Regex("^\\d\\d:\\d\\d$"))) {
                sendErrorMessage(Error.TIME_INVALID, bot, chatId)
                return false
            }
            val time = this[1].split(":").map {
                it.toInt()
            }
            val localTime = LocalTime(time[0], time[1])
            Logger.getRootLogger().log(Level.INFO, "Adding new event")
            return DatabaseManager.addEvent(localDate, localTime, this.last())
        }
    }

    private fun sendErrorMessage(e: Error, bot: AbsSender, chatId: Long) {
        try {
            bot.execute(SendMessage.builder()
                .chatId(chatId)
                .text(e.errorText)
                .build())
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}
