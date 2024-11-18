package org.example

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswerCount: Int = 0,
)

fun loadDictionary(wordFile: File): List<Word> {
    val dictionary: MutableList<Word> = mutableListOf()
    wordFile.forEachLine {
        val lines = it.split("|");
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
    val realizationLoadDictionary = loadDictionary(wordFile)
    println(realizationLoadDictionary)

    var choice: Int
    while (true) {
        println("Меню:\n1 - Учить слова\n2 - Статистика\n0 - Выход")
        choice = readln().toInt()
        when (choice) {
            1 -> println("Выбран пункт \"Учить слова\"")
            2 -> println("Выбран пункт \"Статистика\"")
            0 -> break
            else -> println("Введите число 0, 1 или 2")
        }
    }
}