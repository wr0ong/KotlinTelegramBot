import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val text: String? = null,
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
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val responseString: String = telegramBotService.getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = telegramBotService.getJson().decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, trainers, telegramBotService) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun checkNextQuestionAndCreateMessage(trainer: LearnWordsTrainer, chatId: Long): SendMessageRequest {
    val question = trainer.getNextQuestion()
    val requestBody: SendMessageRequest
    if (question == null) {
        requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Вы выучили все слова",
            replyMarkup = ReplyMarkup(
                listOf(listOf(InlineKeyboard(text = "Вернуться в меню", callbackData = START_OF_BOT)))
            )
        )
        return requestBody
    } else {
        requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original.replaceFirstChar { it.uppercaseChar() },
            replyMarkup = ReplyMarkup(question.variants.mapIndexed { index, word ->
                listOf(
                    InlineKeyboard(
                        text = word.translate.replaceFirstChar { it.uppercaseChar() }, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                    ))} + listOf(listOf(InlineKeyboard(text = "Вернуться в меню", callbackData = START_OF_BOT)))

            )
        )
        return requestBody
    }
}

fun createStatistic(trainer: LearnWordsTrainer, chatId: Long): SendMessageRequest {
    val statisticOfTrainer = trainer.getStatistics()
    val requestBody = SendMessageRequest(
        chatId = chatId,
        text = "Выучено ${statisticOfTrainer.learnedCount} из ${statisticOfTrainer.totalCount} cлов | ${statisticOfTrainer.percent}%",
        replyMarkup = ReplyMarkup(
            listOf(
                listOf(
                    InlineKeyboard(text = "Вернуться в меню", callbackData = START_OF_BOT)
                ), listOf(InlineKeyboard(text = "Сбросить статистику", callbackData = RESET_CLICKED))
            )
        )
    )
    return requestBody
}

fun handleUpdate(
    update: Update,
    trainers: HashMap<Long, LearnWordsTrainer>,
    telegramBotService: TelegramBotService
) {

    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data
    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    if (((message?.lowercase() == START_OF_BOT) || (data?.lowercase() == START_OF_BOT))) {
        telegramBotService.sendMenu(chatId)
    }
    if (data?.lowercase() == STATISTICS_CLICKED) {
        telegramBotService.sendNextMessageJSON(createStatistic(trainer, chatId))
    } else if (data?.lowercase() == LEARN_WORDS_CLICKED) {
        telegramBotService.sendNextMessageJSON(checkNextQuestionAndCreateMessage(trainer, chatId))
    }
    if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
        val indexOfAnswer = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
        if (trainer.checkAnswer(indexOfAnswer)) {
            telegramBotService.sendMessage(chatId, "Правильно!")
        } else telegramBotService.sendMessage(chatId, "Неправильно! ${trainer.getCorrectAnswer().replaceFirstChar { it.uppercaseChar() }}")
        telegramBotService.sendNextMessageJSON(checkNextQuestionAndCreateMessage(trainer, chatId))
    }
    if (data?.lowercase() == RESET_CLICKED) {
        trainer.resetProgress()
        telegramBotService.sendMessage(chatId, "Статистика сброшена!")
    }
}