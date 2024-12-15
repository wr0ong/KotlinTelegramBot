import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var lastUpdateId = 0L

    val json = Json {
        ignoreUnknownKeys = true
    }
    val trainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)
        val responseString: String = telegramBotService.getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = json.decodeFromString(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val message = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id
        val data = firstUpdate.callbackQuery?.data

        if (((message?.lowercase() == START_OF_BOT) || (data?.lowercase() == START_OF_BOT)) && chatId != null) {
            telegramBotService.sendMenu(json, chatId)
        }
        if ((data?.lowercase() == STATISTICS_CLICKED) && chatId != null) {
            telegramBotService.sendNextMessageJSON(createStatistic(json, trainer, chatId))
        } else if ((data?.lowercase() == LEARN_WORDS_CLICKED) && chatId != null) {
            telegramBotService.sendNextMessageJSON(checkNextQuestionAndCreateMessage(json, trainer, chatId))
        }
        if ((data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) && chatId != null) {
            val indexOfAnswer = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(indexOfAnswer)) {
                telegramBotService.sendMessage(chatId, "Правильно!")
            } else telegramBotService.sendMessage(chatId, "Неправильно! ${trainer.getCorrectAnswer()}")
            telegramBotService.sendNextMessageJSON(checkNextQuestionAndCreateMessage(json, trainer, chatId))
        }
    }
}

fun checkNextQuestionAndCreateMessage(json: Json, trainer: LearnWordsTrainer, chatId: Long): String {
    val question = trainer.getNextQuestion()
    val createQuestionBody: String
    if (question == null) {
        createQuestionBody = "Вы выучили все слова в базе"
        return createQuestionBody
    } else {
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                listOf(question.variants.mapIndexed { index, word ->
                    InlineKeyboard(
                        text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                    )
                }, listOf(InlineKeyboard(text = "Вернуться в меню", callbackData = START_OF_BOT)))
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        return requestBodyString
    }
}

fun createStatistic(json: Json, trainer: LearnWordsTrainer, chatId: Long): String {
    val statisticOfTrainer = trainer.getStatistics()
    val requestBody = SendMessageRequest(
        chatId = chatId,
        text = "Выучено ${statisticOfTrainer.learnedCount} из ${statisticOfTrainer.totalCount} cлов | ${statisticOfTrainer.percent}%",
        replyMarkup = ReplyMarkup(
            listOf(
                listOf(
                    InlineKeyboard(text = "Вернуться в меню", callbackData = START_OF_BOT)
                )
            )
        )
    )

    val requestBodyString = json.encodeToString(requestBody)

    return requestBodyString
}