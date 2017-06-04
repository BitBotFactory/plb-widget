package com.artem.lendingwidget.extensions

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.util.*

private const val ERROR_LOG_FILE = "errors.txt"
private const val DEFAULT_MAX_LINES = 1000
private const val SEPERATOR = "======SEPERATOR:com.artem.lendingwidget======"

fun Context.logError(e: Throwable) {
    val errorLog = File(this.filesDir, ERROR_LOG_FILE)

    val writer = PrintWriter(FileWriter(errorLog, true))
    writer.appendln(SEPERATOR)
    writer.appendln(Date(System.currentTimeMillis()).toString())
    e.printStackTrace(writer)
    writer.close()

    cleanLog(errorLog)
}

private fun cleanLog(file: File, maxLines: Int = DEFAULT_MAX_LINES) {
    var lines = file.readLines()
    if (lines.size <= maxLines) {
        return
    }

    val fileName = file.name
    lines = lines.subList(lines.size - maxLines / 2, lines.size - 1)
    val newLog = File(file.parentFile, "${fileName}_copy")
    if (newLog.exists()) {
        newLog.delete()
    }
    val writer = newLog.bufferedWriter()

    lines.forEach {
        writer.append(it)
    }
    writer.close()
    newLog.copyTo(file, true)
    newLog.delete()
}

fun Context.loadErrorLog(): List<String> {
    val errorLog = File(this.filesDir, ERROR_LOG_FILE)
    if (!errorLog.exists()) {
        return List(1, {"Nothing logged yet."})
    }
    val logString = errorLog.readText()
    return logString.split(SEPERATOR).reversed()
}