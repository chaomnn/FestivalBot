import bot.Bot
import bot.BotHelper
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

fun main() {
    val botsApi: TelegramBotsApi
    try {
        botsApi = TelegramBotsApi(DefaultBotSession::class.java)
    } catch (e: TelegramApiException) {
        throw RuntimeException(e)
    }
    try {
        botsApi.registerBot(Bot(BotHelper.getBotToken()))
    } catch (e: TelegramApiException) {
        throw RuntimeException(e)
    }
}
