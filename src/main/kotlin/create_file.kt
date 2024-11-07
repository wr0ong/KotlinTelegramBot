package org.example

import java.io.File

fun main() {
    val wordFile: File = File("words.txt")
    wordFile.delete()
    wordFile.appendText("hello привет\ndog собака\ncat кошка")

    //wordFile.readLines().forEach{ println(it) }
    for (i in wordFile.readLines()) println(i)
}