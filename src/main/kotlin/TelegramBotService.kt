import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService(private val botToken: String) {

    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$LINK_TO_TG_API_BOT$botToken/getUpdates?offset=$updateId"
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }


    fun sendMessage(chatId: Long, text: String): String {
        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )
        println(encoded)
        val urlGetMessage = "$LINK_TO_TG_API_BOT$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetMessage)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(json: Json, chatId: Long): String {
        val sendMessage = "$LINK_TO_TG_API_BOT$botToken/sendMessage"
        val requestBody = SendMessageRequest (
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(listOf(
                    InlineKeyboard(text = "Изучить слова", callbackData = LEARN_WORDS_CLICKED),
                    InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED)
                ))
            )
        )
        val sendBodyString = json.encodeToString(requestBody)
        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendBodyString))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendNextMessageJSON(
        textMessage: String
    ): String {
        val sendMessage = "$LINK_TO_TG_API_BOT$botToken/sendMessage"
        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(textMessage))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}

const val LINK_TO_TG_API_BOT = "https://api.telegram.org/bot"
const val START_OF_BOT = "/start"
const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"