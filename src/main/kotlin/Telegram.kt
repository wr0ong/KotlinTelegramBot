fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var updateId = 0

    var chatId: Long
    val chatIdStringRegex: Regex = "\"chat\":\\{\"id\":(\\d+),".toRegex()
    val updateIdStringRegex: Regex = "\"update_id\":(.+?),".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()
    val trainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)

        updateId = updateIdStringRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull()?.plus(1) ?: continue
        val message = messageTextRegex.find(updates)?.groups?.get(1)?.value ?: continue
        chatId = chatIdStringRegex.find(updates)?.groups?.get(1)?.value?.toLongOrNull() ?: continue
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if ((message.lowercase() == START_OF_BOT) || (data?.lowercase() == START_OF_BOT)) {
            telegramBotService.sendMenu(chatId)
        }
        if (data?.lowercase() == STATISTICS_CLICKED) {
            telegramBotService.sendNextMessageJSON(createStatistic(trainer, chatId))
        } else if (data?.lowercase() == LEARN_WORDS_CLICKED) {
            telegramBotService.sendNextMessageJSON(checkNextQuestionAndCreateMessage(trainer, chatId))
        }
    }
}

fun checkNextQuestionAndCreateMessage(trainer: LearnWordsTrainer, chatId: Long): String {
    val question = trainer.getNextQuestion()
    val createQuestionBody: String
    if (question == null) {
        createQuestionBody = "Вы выучили все слова в базе"
        return createQuestionBody
    } else {
        val variantsString = question.variants
            .mapIndexed { index, word ->
                """
                {
                    "text": "${word.translate}",
                    "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${index}"
                }
                """.trimIndent()
            }
            .joinToString(separator = ",")

        createQuestionBody = """
            {
                "chat_id": $chatId,
                "text": "${question.correctAnswer.original}",
                "reply_markup": {
                    "inline_keyboard": [
                        [$variantsString
                        ],
                        [
                        {
                        "text": "0 - Вернуться в меню",
                        "callback_data": "$START_OF_BOT"
                        }
                        ]
                    ]
                }
            }
        """.trimIndent()
    }
    return createQuestionBody
}

fun createStatistic(trainer: LearnWordsTrainer, chatId: Long): String {
    val statisticOfTrainer = trainer.getStatistics()
    val createStatisticBody = """
            {
                "chat_id": $chatId,
                "text": "Выучено ${statisticOfTrainer.learnedCount} из ${statisticOfTrainer.totalCount}  слов | ${statisticOfTrainer.percent}%",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Вернуться в меню",
                                "callback_data": "$START_OF_BOT"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

    return createStatisticBody
}
