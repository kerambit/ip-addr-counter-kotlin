package org.ip.addr.counter

import java.util.concurrent.atomic.AtomicIntegerArray
import kotlin.experimental.or

const val READ_MODE = "r"
const val MAX_IP_PARTS = 3
const val MAX_PART_VALUE = 255u

abstract class IpAddrCounterAbs {
    abstract suspend fun processFile(): UInt

    protected val maxBufferSize = Int.MAX_VALUE.toLong()

    protected fun getBitmaskSize(): Int {
        val totalIps = 1L shl 32
        return (totalIps / 8).toInt()
    }

    fun getResultValue(parsedData: UInt, currentPart: UInt): UInt {
        return (parsedData shl 8) or currentPart
    }

    fun addToBitmask(result: UInt, sharedBitmask: AtomicIntegerArray) {
        val index = (result / 8u).toInt()
        val mask = 1 shl (result % 8u).toInt()

        if (sharedBitmask[index] and mask == 0) {
            sharedBitmask.getAndUpdate(index) { it or mask }
        }
    }

    fun addToBitmask(result: UInt, sharedBitmask: ByteArray): Boolean {
        val index = (result / 8u).toInt()
        val mask = (1 shl (result % 8u).toInt()).toByte()

        if ((sharedBitmask[index].toInt() and mask.toInt()) == 0) {
            sharedBitmask[index] = sharedBitmask[index] or mask
            return true
        }

        return false
    }
}
