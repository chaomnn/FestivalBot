package bot

import org.telegram.telegrambots.meta.api.objects.Message

object MarkupHelper {

    private const val ENTITY_TYPE_LINK = "text_link"
    private const val ENTITY_TYPE_BOLD = "bold"
    private const val ENTITY_TYPE_ITALIC = "italic"

    fun getMessageTextWithMarkup(message: Message): String {
        var editedText = message.text
        val entities = message.entities
        if (entities != null && entities.isNotEmpty()) {
            for (e in entities) {
                val text = e.text.trim()
                when (e.type) {
                    ENTITY_TYPE_LINK -> {
                        editedText = editedText.replaceFirst(text.toRegex(), "<a href=\"" + e.url + "\">" + text + "</a>")
                        continue
                    }
                    ENTITY_TYPE_BOLD -> {
                        editedText = editedText.replace(text, "<b>$text</b>")
                        continue
                    }
                    ENTITY_TYPE_ITALIC -> {
                        editedText = editedText.replace(text, "<i>$text</i>")
                        continue
                    }
                    else -> {}
                }
            }
        }
        return editedText
    }
}
