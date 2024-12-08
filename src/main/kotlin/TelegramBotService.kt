import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService(private val botToken: String) {

    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
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

    fun sendMenu(chatId: Long): String {
        val sendMessage = "$LINK_TO_TG_API_BOT$botToken/sendMessage"
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучить слова",
                                "callback_data": "$LEARN_WORDS_CLICKED"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "$STATISTICS_CLICKED"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun checkNextQuestionAndSend(
        trainer: LearnWordsTrainer,
        chatId: Long
    ) : String {
        if (trainer.getNextQuestion() == null) return ("Вы выучили все слова в базе")
        else {
            val nextQuestion = trainer.getNextQuestion()

            val sendMessage = "$LINK_TO_TG_API_BOT$botToken/sendMessage"
            val sendQuestionBody = """
            {
                "chat_id": $chatId,
                "text": "${nextQuestion!!.correctAnswer.translate}",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "${nextQuestion.variants[0].original}",
                                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + nextQuestion.variants.indexOf(nextQuestion.variants[0])}"
                            },
                            {
                                "text": "${nextQuestion.variants[1].original}",
                                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + nextQuestion.variants.indexOf(nextQuestion.variants[1])}"
                            }
                        ],
                        [
                            {
                                "text": "${nextQuestion.variants[2].original}",
                                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + nextQuestion.variants.indexOf(nextQuestion.variants[2])}"
                            },
                            {
                                "text": "${nextQuestion.variants[3].original}",
                                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + nextQuestion.variants.indexOf(nextQuestion.variants[3])}"
                            }
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

            val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            return response.body()

        }
    }

    fun sendStatistic (trainer: LearnWordsTrainer, chatId: Long): String {
        val statisticTrainer = trainer.getStatistics()
        val sendMessage = "$LINK_TO_TG_API_BOT$botToken/sendMessage"
        val sendStatisticBody = """
            {
                "chat_id": $chatId,
                "text": "Выучено ${statisticTrainer.learnedCount} из ${statisticTrainer.totalCount}  слов | ${statisticTrainer.percent}%",
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

        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendStatisticBody))
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
