package bot.command

import bot.Bot.Actions
import bot.BotHelper
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class DeleteEventCommand : BotCommand(COMMAND_NAME, DESCRIPTION) {

    companion object {
        private const val COMMAND_NAME = "deleteevent"
        private const val DESCRIPTION = "Delete event"
        private const val CHOOSE_DAY = "Мероприятие на какой день нужно удалить?"
    }

    override fun execute(bot: AbsSender, user: User, chat: Chat, args: Array<out String>) {
        if (!BotHelper.isAdmin(chat.id)) {
            return
        }
        val buttons = DatabaseManager.getAllDates().map { date ->
            val monthPrefix = if (date.monthNumber < 10) "0" else ""
            val dayPrefix = if (date.dayOfMonth < 10) "0" else ""
            "${dayPrefix}${date.dayOfMonth}.${monthPrefix}${date.monthNumber}".let {
                InlineKeyboardButton
                    .builder()
                    .text(it)
                    .callbackData("$date ${Actions.ACTION_DELETE_EVENT.name}")
                    .build()
            }
        }.chunked(5)
        try {
            bot.execute(SendMessage.builder()
                .chatId(chat.id)
                .text(CHOOSE_DAY)
                .replyMarkup(InlineKeyboardMarkup.builder()
                    .keyboard(buttons)
                    .build())
                .build())
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}
