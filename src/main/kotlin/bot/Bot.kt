package bot

import DatabaseManager
import bot.command.AddEventCommand
import bot.command.DeleteEventCommand
import bot.command.EditTextCommand
import bot.command.StartCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.jetbrains.exposed.sql.Database
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage.SendMessageBuilder
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class Bot(token: String) : TelegramLongPollingCommandBot(token) {

    companion object {
        private const val EVENTS_MESSAGE = "События:\n"
        private const val REGISTRATION_MESSAGE = "<a href=\"https://artweeknd.com/skazschedule\">Регистрация</a>"
        private const val DELETE_SUCCESS = "Мероприятие удалено."
        private const val EDIT_SUCCESS = "Текст отредактирован."
        private const val ENTER_NEW_TEXT = "Ответь на это сообщение новым текстом."

        private const val ID = "id:"

        private const val URL = "jdbc:sqlite:"
        private const val DRIVER = "org.sqlite.JDBC"
    }

    enum class Actions {
        ACTION_DELETE_EVENT,
        ACTION_EDIT_TEXT
    }

    private var username: String
    private lateinit var editTextButton: Buttons

    init {
        register(StartCommand())
        register(AddEventCommand())
        register(DeleteEventCommand())
        register(EditTextCommand())
        runBlocking {
            username = withContext(Dispatchers.Default) {
                me.userName
            }
        }
        Database.connect("${URL}$botUsername.db", DRIVER)
        Logger.getRootLogger().log(Level.INFO, "Connected to DB")
    }

    override fun getBotUsername(): String {
        return username
    }

    override fun processNonCommandUpdate(update: Update) {
        try {
            if (update.hasCallbackQuery()) {
                val messageBuilder = SendMessage
                    .builder()
                    .chatId(update.callbackQuery.message.chatId)
                    .disableWebPagePreview(true)
                    .parseMode("HTML")
                val message = when (val callbackData = update.callbackQuery.data) {
                    Buttons.MAIN_PROGRAM.name -> {
                        getMultipleDatesMessage(callbackData, messageBuilder)
                    }
                    Buttons.MAIN_MEMBERS.name -> {
                        val buttons = Buttons.entries
                            .filter { it.name.contains("MEMBER_") }
                            .map { button ->
                            listOf(
                                InlineKeyboardButton
                                    .builder()
                                    .text(button.text)
                                    .callbackData(button.name)
                                    .build()
                            )
                        }
                        messageBuilder
                            .text(DatabaseManager.getButtonText(Buttons.MAIN_MEMBERS.name))
                            .replyMarkup(InlineKeyboardMarkup
                                .builder()
                                .keyboard(buttons)
                                .build())
                    }
                    else -> {
                        if (callbackData.contains(Regex("^\\d\\d\\d\\d-\\d\\d-\\d\\d"))) {
                            // It's a date
                            getSingleDateMessage(callbackData, messageBuilder)
                        } else if (callbackData.contains(ID) && callbackData.contains(Actions.ACTION_DELETE_EVENT.name)) {
                            // It's an event deletion request
                            val eventId = callbackData.split(" ").first().split(ID).last().toInt()
                            Logger.getRootLogger().log(Level.INFO, "Deleting event with ID $eventId")
                            if (DatabaseManager.deleteEvent(eventId)) {
                                messageBuilder
                                    .text(DELETE_SUCCESS)
                            } else null
                        } else if (callbackData.contains(Actions.ACTION_EDIT_TEXT.name)) {
                            // It's a request to edit text
                            editTextButton = Buttons.valueOf(callbackData.split(Actions.ACTION_EDIT_TEXT.name).last())
                            messageBuilder
                                .text(ENTER_NEW_TEXT)
                        } else {
                            // It's just a button click
                            messageBuilder
                                .text(DatabaseManager.getButtonText(callbackData))
                        }
                    }
                }
                execute(message!!.build())
            } else if (update.message != null && BotHelper.isAdmin(update.message.chatId) && update.message.isReply &&
                update.message.replyToMessage.text.contains(ENTER_NEW_TEXT)) {
                // Edit button text
                if (DatabaseManager.editButtonText(editTextButton.name,
                    MarkupHelper.getMessageTextWithMarkup(update.message))) {
                    execute(SendMessage.builder()
                            .chatId(update.message.chatId)
                            .text(EDIT_SUCCESS)
                            .build())
                }
            }
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    private fun getMultipleDatesMessage(callbackData: String, messageBuilder: SendMessageBuilder): SendMessageBuilder {
        val buttons = DatabaseManager.getAllDates().map { date ->
            val monthPrefix = if (date.monthNumber < 10) "0" else ""
            val dayPrefix = if (date.dayOfMonth < 10) "0" else ""
            "${dayPrefix}${date.dayOfMonth}.${monthPrefix}${date.monthNumber}".let {
                InlineKeyboardButton
                    .builder()
                    .text(it)
                    .callbackData("$date $callbackData")
                    .build()
            }
        }.chunked(5)
        return messageBuilder
            .text(DatabaseManager.getButtonText(Buttons.MAIN_PROGRAM.name))
            .replyMarkup(InlineKeyboardMarkup
                .builder()
                .keyboard(buttons)
                .build())
    }

    private fun getSingleDateMessage(callbackData: String, messageBuilder: SendMessageBuilder): SendMessageBuilder {
        val events = callbackData.split(" ").first().split("-").map {
            it.toInt()
        }.let {
            DatabaseManager.getEventsForDate(LocalDate(it[0], it[1], it[2]))
        }
        val text = events.map {
            it.second
        }.reduce {
                acc, string -> acc + string
        }
        if (callbackData.contains(Actions.ACTION_DELETE_EVENT.name)) {
            val buttons = events.mapIndexed { ind, pair ->
                // Place event id and action in callback
                InlineKeyboardButton.builder()
                    .text("${ind + 1}")
                    .callbackData(ID + pair.first.toString() + " " + Actions.ACTION_DELETE_EVENT.name)
                    .build()
            }
            return messageBuilder
                .text(text)
                .replyMarkup(InlineKeyboardMarkup
                    .builder()
                    .keyboard(listOf(buttons))
                    .build())
        } else {
            return messageBuilder
                .text(EVENTS_MESSAGE + text + REGISTRATION_MESSAGE)
        }
    }
}
