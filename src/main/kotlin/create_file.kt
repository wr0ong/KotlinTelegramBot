package org.example

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswerCount: Int? = 0,
)

fun main() {
    val wordFile: File = File("words.txt")

    val dictionary: MutableList<Word> = mutableListOf()

    wordFile.forEachLine {
        val lines = it.split("|", "+");
        val word: Word =
            Word(original = lines[0], translate = lines[1], correctAnswerCount = lines[2].toIntOrNull() ?: 0);
        dictionary.add(word)
    }
    println(dictionary)
}