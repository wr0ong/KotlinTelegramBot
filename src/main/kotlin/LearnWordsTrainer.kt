import kotlinx.serialization.Serializable
import java.io.File

data class Statistics(
    val learnedCount: Int,
    val totalCount: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(
    private val fileName: String = "words.txt",
    private val learnedAnswerCount: Int = LEARNED_ANSWER_COUNT,
    private val countOfQuestionWord: Int = COUNT_OF_QUESTION_WORD,
) {

    private var question: Question? = null
    private val dictionary: List<Word> = loadDictionary()

    fun getCorrectAnswer(): String {
        return "${question?.correctAnswer?.translate} - это ${question?.correctAnswer?.original}"
    }

    fun getStatistics(): Statistics {
        val learnedCount: Int = dictionary.filter { it.correctAnswerCount >= learnedAnswerCount }.size
        val totalCount: Int = dictionary.size
        val percent = learnedCount * 100 / totalCount
        return Statistics(learnedCount, totalCount, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswerCount < learnedAnswerCount }
        if (notLearnedList.isEmpty()) return null
        val questionWords = if (notLearnedList.size < countOfQuestionWord) {
            val learnedList = dictionary.filter { it.correctAnswerCount >= learnedAnswerCount }.shuffled()
            notLearnedList.shuffled()
                .take(countOfQuestionWord) + learnedList.take(countOfQuestionWord - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(countOfQuestionWord)
        }.shuffled()

        val correctAnswer = questionWords.random()

        question = Question(
            variants = questionWords,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(answerOfUser: Int?): Boolean {
        return question?.let {
            val indexOfCorrectAnswer = it.variants.indexOf(it.correctAnswer)
            if (indexOfCorrectAnswer == answerOfUser) {
                it.correctAnswer.correctAnswerCount++
                saveDictionary()
                true
            } else false
        } ?: false
    }

    private fun saveDictionary() {
        var newText = dictionary.joinToString { "${it.original}|${it.translate}|${it.correctAnswerCount}\n" }
        newText = newText.replace(", ", "")
        val wordFile = File(fileName)
        wordFile.writeText(newText)
    }

    private fun loadDictionary(): List<Word> {
        try {
            val wordFile = File(fileName)
            if (!wordFile.exists()) {
                File("words.txt").copyTo(wordFile)
            }

            val dictionary = mutableListOf<Word>()
            wordFile.forEachLine {
                val lines = it.split("|")
                val word = Word(
                    original = lines[0],
                    translate = lines[1],
                    correctAnswerCount = lines.getOrNull(2)?.toIntOrNull() ?: 0
                )
                dictionary.add(word)
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Некорректный файл")
        }
    }

    fun resetProgress() {
        dictionary.forEach { it.correctAnswerCount = 0 }
        saveDictionary()
    }

}

@Serializable
data class Word(
    val original: String,
    val translate: String,
    var correctAnswerCount: Int = 0,
)

const val LEARNED_ANSWER_COUNT = 3
const val COUNT_OF_QUESTION_WORD = 4