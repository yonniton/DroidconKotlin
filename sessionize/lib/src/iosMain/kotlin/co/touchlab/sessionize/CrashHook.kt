package co.touchlab.sessionize

import co.touchlab.sessionize.platform.backgroundTask
import kotlin.native.concurrent.freeze

class CrashHook(block: (String)->Unit): ReportUnhandledExceptionHook {
    private val crashBlock = block.freeze()
    override fun invoke(p1: Throwable) {
        val stack = p1.getStackTrace().joinToString(separator = "\n")
        crashBlock(stack)
    }
}

//Calling from Swift
@Suppress("unused")
fun setCrashHook(block: (String)->Unit){
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
        throw IllegalStateException("Hello darkness, my old friend")
    }
}
