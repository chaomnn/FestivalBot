package bot

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

object BotHelper {

    private const val FILE_NAME = "bot.json"
    private const val TOKEN = "token"
    private const val ADMIN = "admin"

    private val mapper: ObjectMapper = ObjectMapper()
    private val file: File = File(FILE_NAME)

    fun getBotToken(): String {
        return mapper.readTree(file).get(TOKEN).asText()
    }

    fun isAdmin(id: Long): Boolean {
        val admins = mapper.readTree(file).get(ADMIN)
        admins.forEach { admin ->
            if (id == admin.asLong()) {
                return true
            }
        }
        return false
    }
}
