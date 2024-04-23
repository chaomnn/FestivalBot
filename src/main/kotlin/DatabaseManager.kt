import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.time
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.update

object DatabaseManager {

    object ButtonTexts : Table() {
        val id = text("id")
        val description = text("description")
        override val primaryKey = PrimaryKey(id)
    }

    object Events : Table() {
        val id = integer("id").autoIncrement()
        val date = date("date")
        val time = time("time")
        val info = text("info")
        override val primaryKey = PrimaryKey(id)
    }

    fun addEvent(eventDate: LocalDate,
                 eventTime: LocalTime,
                 eventInfo: String): Boolean {
        try {
            transaction {
                SchemaUtils.create(Events)
                Events.insert {
                    it[date] = eventDate
                    it[time] = eventTime
                    it[info] = eventInfo
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getAllDates(): List<LocalDate> {
        return try {
            transaction {
                Events.select(Events.date)
                    .withDistinct()
                    .map {
                        it[Events.date]
                    }.sortedBy {
                        it
                    }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            listOf()
        }
    }

    fun getEventsForDate(date: LocalDate): List<Pair<Int, String>> {
        return try {
            transaction {
                Events.select(Events.id, Events.time, Events.info)
                    .where {
                        Events.date.eq(date)
                    }.sortedBy {
                        it[Events.time]
                    }.mapIndexed { ind, event ->
                        event[Events.id] to "${ind+1}. ${event[Events.time]}\n${event[Events.info]}\n\n"
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            listOf()
        }
    }

    fun deleteEvent(eventId: Int): Boolean {
        try {
            transaction {
                Events.deleteWhere {
                    id eq eventId
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun editButtonText(buttonName: String, text: String): Boolean {
        try {
            transaction {
                SchemaUtils.create(ButtonTexts)
                ButtonTexts.insertIgnore {
                    it[id] = buttonName
                    it[description] = text
                }
                ButtonTexts.update({ ButtonTexts.id eq buttonName }) {
                    it[description] = text
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getButtonText(buttonName: String): String {
        return try {
            transaction {
                val query = ButtonTexts.select(ButtonTexts.description).where {
                    ButtonTexts.id eq buttonName
                }
                if (query.count() > 0) {
                    query.first()[ButtonTexts.description]
                } else {
                    "Тут еще ничего нет. ¯\\_(ツ)_/¯ "
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
