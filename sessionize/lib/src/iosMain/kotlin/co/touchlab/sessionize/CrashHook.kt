package co.touchlab.sessionize

import co.touchlab.sessionize.platform.backgroundTask
import io.ktor.util.InternalAPI
import io.ktor.util.toCharArray
import kotlin.native.concurrent.freeze

class CrashHook(block: (List<KMPStackFrame>)->Unit): ReportUnhandledExceptionHook {
    private val crashBlock = block.freeze()
    override fun invoke(p1: Throwable) {
        val stackTrace = p1.getStackTrace()
        val stringStackTrace = stackTrace.asList()
        val kotlinStackTrace = stringStackTrace.joinToString(separator = "\n")

        // Print to Xcode console to report for debug builds
        println("\nKotlin stackTrace:\n\n$kotlinStackTrace\n")

        val kmpStackTrace = stringStackTrace.map {
            kmpStackFrameFrom(it)
        }

        // Pass to crash hook to report for release builds
        crashBlock(kmpStackTrace)
    }
}

fun kmpStackFrameFrom(stackTraceString: String): KMPStackFrame {
    var string = stackTraceString

    val frameNumber = nextStringComponent(string)
    string = string.removePrefix(frameNumber).trim()

    val library = nextStringComponent(string)
    string = string.removePrefix(library).trim()

    val memoryAddress = nextStringComponent(string)
    string = string.removePrefix(memoryAddress).trim()

    val function = nextStringComponent(string)
    string = string.removePrefix(function).trim()

    val lineNumber = string

    println("\nframeNumber: $frameNumber \nlibrary: $library \nmemoryAddress: $memoryAddress \nfunction: $function \nlineNumber: $lineNumber\n")

    return KMPStackFrame(
            frameNumber = frameNumber,
            rawSymbol = function,
            library = library,
            fileName = null,
            lineNumber = lineNumber,
            offset = null,
            address = memoryAddress
    )
}

data class KMPStackFrame(
        val frameNumber: String,
        val rawSymbol: String,
        val library: String,
        val fileName: String?,
        val lineNumber: String,
        val offset: String?,
        val address: String
)

fun nextStringComponent(string: String): String {
    return string.substringBefore(" ")

}

// Call from AppDelegate, ASAP after setting up Crashlytics
@Suppress("unused")
fun setCrashHook(block: (List<KMPStackFrame>)->Unit){
    setUnhandledExceptionHook(CrashHook(block).freeze())
}

class CrashViewModel {
    @Suppress("unused")

    fun backgroundCrash() {
        backgroundTask({
            forceCrash()
        }) {
            println("Too Far!")
        }
    }

    fun forceCrash() {
        loopdyLoop()
    }

    fun loopdyLoop() {
        for (i in 0 until 10){
            println("$i")
            if(i > 5)
                helloDarkness()
        }
    }

    fun helloDarkness() {
        throw RuntimeException("Hello darkness, my old friend")
    }
}