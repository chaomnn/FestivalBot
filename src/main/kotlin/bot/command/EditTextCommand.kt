package bot.command

import bot.Bot.Actions
import bot.Buttons
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class EditTextCommand : BotCommand(COMMAND_NAME, DESCRIPTION) {

    companion object {
        private const val COMMAND_NAME = "edittext"
        private const val DESCRIPTION = "Edit text"
        private const val EDIT_TEXT_CHOOSE = "Для какого раздела необходимо отредактировать текст?"
    }

    override fun execute(bot: AbsSender, user: User, chat: Chat, args: Array<out String>) {
        val buttons = Buttons.entries.map { button ->
            listOf(
                InlineKeyboardButton
                    .builder()
                    .text(button.text)
                    .callbackData(Actions.ACTION_EDIT_TEXT.name + button.name)
                    .build()
            )
        }
        val markup = InlineKeyboardMarkup
            .builder()
            .keyboard(buttons)
            .build()
        val message = SendMessage
            .builder()
            .chatId(chat.id)
            .text(EDIT_TEXT_CHOOSE)
            .replyMarkup(markup)
            .build()
        try {
            bot.execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}
