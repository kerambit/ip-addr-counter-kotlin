package org.ip.addr.counter

import java.io.File
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.atomic.AtomicIntegerArray

class ChunkProcessor(
    private val start: Long,
    private val end: Long,
    private val filePath: String,
    private val sharedBitmask: AtomicIntegerArray,
    private val maxBufferSize: Long,
    private val countProcessor: IpAddrCounterAbs
) {

    private var currentPart = 0u
    private var result = 0u
    private var parts = 0
    private var hasDigits = false

    fun process() {
        val channel = File(filePath).inputStream().channel

        var currentStart = start
        while (currentStart < end) {
            val mapSize = getMapSize(currentStart)

            val buffer = channel
                .map(FileChannel.MapMode.READ_ONLY, currentStart, mapSize)

            processBuffer(buffer)

            currentStart += mapSize

            if (
                currentStart == end
                && (hasDigits && parts == MAX_IP_PARTS)
            ) {
                val newResult = countProcessor.getResultValue(result, currentPart)
                countProcessor.addToBitmask(newResult, sharedBitmask)
            }
        }
    }

    private fun getMapSize(currentStart: Long): Long {
        val remaining = end - currentStart
        return minOf(remaining, maxBufferSize)
    }

    private fun processBuffer(buffer: MappedByteBuffer) {
        while (buffer.hasRemaining()) {
            val char = buffer.get().toInt().toChar()
            processChar(char)
        }
    }

    private fun processChar(char: Char) {
        when (char) {
            in '0'..'9' -> {
                hasDigits = true
                currentPart = currentPart * 10u + (char - '0').toUInt()
                if (currentPart > MAX_PART_VALUE) {
                    clearState()
                }
            }

            '.' -> {
                if (!hasDigits || parts >= MAX_IP_PARTS) {
                    clearState()
                } else {
                    result = countProcessor.getResultValue(result, currentPart)
                    currentPart = 0u
                    parts++
                    hasDigits = false
                }
            }

            '\n' -> {
                if (hasDigits && parts == MAX_IP_PARTS && currentPart <= MAX_PART_VALUE) {
                    val newResult = countProcessor.getResultValue(result, currentPart)
                    countProcessor.addToBitmask(newResult, sharedBitmask)
                }
                clearState()
            }

            else -> {
                clearState()
            }
        }
    }

    private fun clearState() {
        currentPart = 0u
        result = 0u
        parts = 0
        hasDigits = false
    }
}
