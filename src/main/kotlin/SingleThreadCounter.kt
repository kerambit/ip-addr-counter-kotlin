package org.ip.addr.counter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader

class SingleThreadCounter(
    private val filePath: String
) : IpAddrCounterAbs() {

    override suspend fun processFile(): UInt {
        println("Process file in single thread mode")
        val bitmask = ByteArray(getBitmaskSize())
        var counter = 0u

        withContext(Dispatchers.IO) {
            val reader = BufferedReader(FileReader(filePath))
            var line = reader.readLine()

            while (line != null) {
                val processed = processLine(line, bitmask)
                if (processed) counter++
                line = reader.readLine()
            }
        }

        return counter
    }

    private fun processLine(line: String, bitmask: ByteArray): Boolean {
        val ipStr = line.trim()
        val ipUint32 = ipToUint32(ipStr)

        if (ipUint32 != null) {
            return addToBitmask(ipUint32, bitmask)
        }

        return false
    }

    private fun ipToUint32(ipStr: String): UInt? {
        var result = 0u
        var currentPart = 0u
        var parts = 0
        var hasDigits = false

        for (char in ipStr) {
            when (char) {
                in '0'..'9' -> {
                    hasDigits = true
                    currentPart = currentPart * 10u + (char - '0').toUInt()
                    if (currentPart > MAX_PART_VALUE) return null
                }
                '.' -> {
                    if (!hasDigits || parts >= MAX_IP_PARTS) return null
                    result = (result shl 8) or currentPart
                    currentPart = 0u
                    parts++
                    hasDigits = false
                }
                else -> return null
            }
        }

        if (!hasDigits || parts != MAX_IP_PARTS) return null

        return getResultValue(result, currentPart)
    }
}
