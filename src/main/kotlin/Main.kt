package org.example

import LearnWordsTrainer
import Question

data class Word(
    val original: String,
    val translate: String,
    var correctAnswerCount: Int = 0,
)

fun Question.asConsoleString(): String {
    val variants = this.variants.mapIndexed { index, i -> "${index + 1} - ${i.translate}" }
        .joinToString("\n", "${this.correctAnswer.original}: \n", "\n----------- \n0 - Меню")
    return variants
}

fun main() {

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {
        println("Меню:\n1 - Учить слова\n2 - Статистика\n0 - Выход")
        try {
            val choice: Int = readln().toInt()
            when (choice) {
                1 -> {
                    while (true) {
                        val question = trainer.getNextQuestion()
                        if (question == null) {
                            println("Все слова в словаре выучены!")
                            break
                        } else {
                            println(question.asConsoleString())
                            val choiceOfUser: Int? = readln().toIntOrNull()
                            if (choiceOfUser == 0) break

                            if (trainer.checkAnswer(choiceOfUser?.minus(1))) {
                                println("Верно!")
                            } else println("Неверно! ${question.correctAnswer.original} - это ${question.correctAnswer.translate}")
                        }
                    }
                }

                2 -> {
                    val statistics = trainer.getStatistics()
                    println("Выучено ${statistics.learnedCount} из ${statistics.totalCount}  слов | ${statistics.percent}%")
                }

                0 -> break
                else -> println("Введите число 0, 1 или 2")
            }
        } catch (e: Exception) {
            println("Введите число 0, 1 или 2")
        }
    }

}