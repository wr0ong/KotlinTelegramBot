fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var updateId = 0
    var chatId: Int
    val chatIdStringRegex: Regex = "\"id\":(.+?),".toRegex()
    val updateIdStringRegex: Regex = "\"update_id\":(.+?),".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)

        val matchResultUpdateId: MatchResult? = updateIdStringRegex.find(updates)
        val updateIdGroups = matchResultUpdateId?.groups
        updateId = updateIdGroups?.get(1)?.value?.toIntOrNull()?.plus(1) ?: continue

        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value ?: continue

        val matchResultChatId: MatchResult? = chatIdStringRegex.find(updates)
        val chatIdGroup = matchResultChatId?.groups
        chatId = chatIdGroup?.get(1)?.value?.toIntOrNull() ?: continue

        telegramBotService.sendMessage(chatId, text)
    }

}