package org.ip.addr.counter

import kotlinx.coroutines.*
import java.io.FileNotFoundException

fun main() = runBlocking {
//    val file = "/Users/kerambit/kotlinprojects/ip-addr-counter/src/main/kotlin/test-file"
//    val file = "/Users/kerambit/kotlinprojects/ip-addr-counter/src/main/kotlin/ips_txt" //70000000
//    val file = "/Users/kerambit/kotlinprojects/ip-addr-counter/src/main/kotlin/ips_txt_head" //4949015
//    val file = "/Users/kerambit/go_test/ip_addresses" // 1000000000

    val file: String; val isParallel: Boolean

    try {
        val result = parseCmd()
        file = result.first
        isParallel = result.second
    } catch (e: IllegalArgumentException) {
        println(e.message)
        return@runBlocking
    } catch (e: FileNotFoundException) {
        println(e.message)
        return@runBlocking
    }

    val workers = getNumberOfWorkers()
    val (startTimer, endTimer) = countTime()

    val singleHandler = if (workers == 1 || !isParallel) {
        SingleThreadCounter(file)
    } else {
        MultiThreadCounter(file, workers)
    }

    startTimer()
    val uniqueIps = singleHandler.processFile()
    endTimer()

    println("Unique IPs: $uniqueIps")
}
