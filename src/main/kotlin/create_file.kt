package org.example

import java.io.File

fun main() {
    val wordFile: File = File("words.txt")

    wordFile.forEachLine { println(it) }
}