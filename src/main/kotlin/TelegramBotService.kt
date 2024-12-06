import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(private val botToken: String) {

    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$LINK_TO_TG_API_BOT$botToken/getUpdates?offset=$updateId"

        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: Int, text: String): String {

        val urlGetMessage = "$LINK_TO_TG_API_BOT$botToken/sendMessage?chat_id=$chatId&text=$text"
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetMessage)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }
}

const val LINK_TO_TG_API_BOT = "https://api.telegram.org/bot"