package co.touchlab.sessionize

import co.touchlab.sessionize.platform.backgroundTask
import platform.Foundation.NSError
import platform.Foundation.NSThread
import kotlin.native.concurrent.freeze

class CrashHook(block: (List<String>)->Unit): ReportUnhandledExceptionHook {
    private val crashBlock = block.freeze()
    override fun invoke(p1: Throwable) {
        val stackTrace = p1.getStackTrace().asList()
        val stackTraceString = stackTrace.joinToString(separator = "\n")

        // Print to Xcode console to report for debug builds
        println("\nKotlin stackTrace:\n\n$stackTraceString\n")

        // Pass to crash hook to report for release builds
        crashBlock(stackTrace)
    }
}

// Call from AppDelegate, ASAP after setting up Crashlytics
@Suppress("unused")
fun setCrashHook(block: (List<String>)->Unit){
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
