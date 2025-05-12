package org.ip.addr.counter

import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths

fun getNumberOfWorkers(): Int {
    return Runtime.getRuntime().availableProcessors() / 2
}

fun parseCmd(): Pair<String, Boolean> {
    val file = System.getProperty("file") ?: throw IllegalArgumentException("File path is not specified. Example: file=/path/to/file")
    val isParallel = System.getProperty("isParallel")?.toBoolean() ?: false

    if (file.isBlank()) {
        throw IllegalArgumentException("Path to file is empty")
    }

    val path = Paths.get(file)

    if (!Files.exists(path)) {
        throw FileNotFoundException("File not found: $file")
    }

    return Pair(file, isParallel)
}

fun countTime(): Pair<() -> Unit, () -> Unit> {
    var startTime = 0L

    val start = {
        startTime = System.nanoTime()
    }

    val end = {
        val endTime = System.nanoTime()
        val elapsedMs = (endTime - startTime) / 1_000_000

        val minutes = elapsedMs / 60000
        val seconds = (elapsedMs % 60000) / 1000
        val millis = elapsedMs % 1000

        println("Working time: %02d:%02d.%03d".format(minutes, seconds, millis))
    }

    return Pair(start, end)
}
