fun main(args: Array<String>) {

    val telegramBotService = TelegramBotService()
    val botToken = args[0]
    var updateId: Int? = 0
    var chatId: Int?
    val chatIdStringRegex: Regex = "\"id\":(.+?),".toRegex()
    val updateIdStringRegex: Regex = "\"update_id\":(.+?),".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(botToken, updateId)
        println(updates)

        val matchResultUpdateId: MatchResult? = updateIdStringRegex.find(updates)
        val updateIdGroups = matchResultUpdateId?.groups
        updateId = updateIdGroups?.get(1)?.value?.toIntOrNull()?.plus(1) ?: continue

        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value

        val matchResultChatId: MatchResult? = chatIdStringRegex.find(updates)
        val chatIdGroup = matchResultChatId?.groups
        chatId = chatIdGroup?.get(1)?.value?.toIntOrNull() ?: continue

        telegramBotService.getMessage(botToken, chatId, text)
    }

}

