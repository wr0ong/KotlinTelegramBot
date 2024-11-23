package org.example

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswerCount: Int = 0,
)

fun loadDictionary(wordFile: File): List<Word> {
    val dictionary: MutableList<Word> = mutableListOf()
    wordFile.forEachLine {
        val lines = it.split("|")
        val word: Word =
            Word(
                original = lines[0],
                translate = lines[1],
                correctAnswerCount = lines.getOrNull(2)?.toIntOrNull() ?: 0
            );
        dictionary.add(word)
    }
    return dictionary
}

fun main() {
    val wordFile: File = File("words.txt")
    val dictionary = loadDictionary(wordFile)
    println(dictionary)

    fun saveDictionary(dictionary: List<Word>) {
        var newText = dictionary.joinToString {"${it.original}|${it.translate}|${it.correctAnswerCount}"}
        newText = newText.replace(", ", "\n")
        wordFile.writeText(newText)
    }

    while (true) {
        println("Меню:\n1 - Учить слова\n2 - Статистика\n0 - Выход")
        try {
            val choice: Int = readln().toInt()
            when (choice) {
                1 -> {

                    while (true) {
                        var choiceOfUser: Int
                        val notLearnedList = dictionary.filter { it.correctAnswerCount < CORRECT_ANSWERS_TO_LEARN }
                        if (notLearnedList.isEmpty()) println("Все слова в словаре выучены!")
                        else {
                            val questionWords = notLearnedList.shuffled().take(ANSWER_OPTIONS)
                            val originalWord = questionWords.random()
                            println(questionWords.mapIndexed { index, i -> "${index + 1} - ${i.translate}" }
                                .joinToString("\n", "${originalWord.original}: \n", "\n----------- \n0 - Меню"))
                            try {
                                choiceOfUser = readln().toInt()
                                if (choiceOfUser !in 0..ANSWER_OPTIONS) {
                                    println("Выберите ответ 1, 2, 3 или 4")
                                } else {

                                    when (choiceOfUser) {
                                        0 -> break
                                        1 -> if (questionWords.indexOf(originalWord) == questionWords.indexOf(questionWords[0])) {
                                            println("Верно!")
                                            questionWords[0].correctAnswerCount++
                                            saveDictionary(dictionary)
                                        } else println("Неверно! ${originalWord.original} - это ${originalWord.translate}")

                                        2 -> if (questionWords.indexOf(originalWord) == questionWords.indexOf(questionWords[1])) {
                                            println("Верно!")
                                            questionWords[1].correctAnswerCount++
                                            saveDictionary(dictionary)
                                        } else println("Неверно! ${originalWord.original} - это ${originalWord.translate}")

                                        3 -> if (questionWords.indexOf(originalWord) == questionWords.indexOf(questionWords[2])) {
                                            println("Верно!")
                                            questionWords[2].correctAnswerCount++
                                            saveDictionary(dictionary)
                                        } else println("Неверно! ${originalWord.original} - это ${originalWord.translate}")

                                        4 -> if (questionWords.indexOf(originalWord) == questionWords.indexOf(questionWords[3])) {
                                            println("Верно!")
                                            questionWords[3].correctAnswerCount++
                                            saveDictionary(dictionary)
                                        } else println("Неверно! ${originalWord.original} - это ${originalWord.translate}")
                                    }

                                }
                            } catch (e: Exception) {
                                println("Выберите ответ 1, 2, 3 или 4")
                            }
                        }
                    }

                }

                2 -> {
                    val learnedCount = dictionary.filter { it.correctAnswerCount >= CORRECT_ANSWERS_TO_LEARN }.size
                    val totalCount = dictionary.size
                    val percent = learnedCount.toDouble() / totalCount.toDouble() * 100
                    println(String.format("Выучено %d из %d слов | %.0f%%\n", learnedCount, totalCount, percent))
                }

                0 -> break
                else -> println("Введите число 0, 1 или 2")
            }
        } catch (e: Exception) {
            println("Введите число 0, 1 или 2")
        }
    }

}

const val ANSWER_OPTIONS = 4
const val CORRECT_ANSWERS_TO_LEARN = 3