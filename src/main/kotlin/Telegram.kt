fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var updateId = 0
    var chatId: Int
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
        chatId = chatIdStringRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (message.lowercase() == "/start") {
            telegramBotService.sendMenu(chatId)
        }
        if (data?.lowercase() == "statistics_clicked") {
            telegramBotService.sendMessage(chatId, "Выучено 10 слов из 10 | 100%")
        }
    }

}