package org.ip.addr.counter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicIntegerArray

class MultiThreadCounter(
    private val filePath: String,
    private val workerCount: Int
) : IpAddrCounterAbs() {

    override suspend fun processFile(): UInt = coroutineScope {
        println("Process file in parallel mode using $workerCount workers")

        val chunks = splitFileIntoChunks(filePath, workerCount)
        val sharedBitmask = AtomicIntegerArray(getBitmaskSize())

        val jobs = chunks.map { (start, end) ->
            async(Dispatchers.IO) { processChunk(start, end, sharedBitmask) }
        }

        jobs.awaitAll()

        return@coroutineScope countBits(sharedBitmask)
    }

    private fun splitFileIntoChunks(filePath: String, parts: Int): List<Pair<Long, Long>> {
        val file = File(filePath)
        val fileSize = file.length()
        val chunkSize = fileSize / parts
        val boundaries = mutableListOf<Pair<Long, Long>>()
        val raf = RandomAccessFile(file, READ_MODE)

        var start = 0L

        for (i in 0 until parts) {
            var end = if (i == parts - 1) fileSize else (start + chunkSize)

            if (end < fileSize) {
                raf.seek(end)
                while (raf.filePointer < fileSize && raf.read().toChar() != '\n') {
                    end++
                }
                end++
            }

            boundaries.add(start to end)

            start = end
        }

        return boundaries
    }

    private fun processChunk(start: Long, end: Long, sharedBitmask: AtomicIntegerArray) {
        val processor = ChunkProcessor(start, end, filePath, sharedBitmask, maxBufferSize, this)
        processor.process()
    }

    private fun countBits(bitmask: AtomicIntegerArray): UInt {
        var count = 0u
        for (i in 0 until bitmask.length()) {
            count += bitmask[i].countOneBits().toUInt()
        }

        return count
    }
}
