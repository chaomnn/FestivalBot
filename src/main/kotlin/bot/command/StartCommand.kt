package bot.command

import bot.Buttons
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class StartCommand : BotCommand(COMMAND_NAME, DESCRIPTION) {

    companion object {
        private const val COMMAND_NAME = "start"
        private const val DESCRIPTION = "Start the bot"
        private const val TEXT_CHOOSE = "Выбери из списка, что ты бы хотел узнать в этом чат-боте:"
    }

    override fun execute(bot: AbsSender, user: User, chat: Chat, args: Array<out String>) {
        Logger.getRootLogger().log(Level.INFO, "Start command from user ${user.userName} with id ${user.id}")
        val buttons = Buttons.entries
            .filter { it.name.contains("MAIN") }
            .map { button ->
            listOf(
                InlineKeyboardButton
                    .builder()
                    .text(button.text)
                    .callbackData(button.name)
                    .build()
            )
        }
        val markup = InlineKeyboardMarkup
            .builder()
            .keyboard(buttons)
            .build()
        // test code
        val message = SendMessage
            .builder()
            .chatId(chat.id)
            .text(TEXT_CHOOSE)
            .replyMarkup(markup)
//            .replyMarkup(ReplyKeyboardMarkup.builder().keyboardRow(kbRow).build())
            .build()
        try {
            bot.execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}
