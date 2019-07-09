package co.touchlab.sessionize

import co.touchlab.sessionize.ServiceRegistry.clLogCallback
import co.touchlab.sessionize.ServiceRegistry.initServiceRegistry
import co.touchlab.sessionize.ServiceRegistry.staticFileLoader
import co.touchlab.sessionize.platform.backgroundSuspend
import io.ktor.http.ContentType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlin.Result.Companion.success
import kotlin.native.concurrent.ThreadLocal

class AboutModelSwiftUI {
    lateinit var aboutInfo: List<AboutInfo>

    private fun fetchAboutInfo(): List<AboutInfo> {
        val aboutJsonString = staticFileLoader("about", "json")!!
        return Json.nonstrict.parse(AboutInfo.serializer().list, aboutJsonString)
    }

    fun fetchAboutInfo(success: (List<AboutInfo>) -> Unit) {
        GlobalScope.launch (ServiceRegistry.coroutinesDispatcher) {
            success(fetchAboutInfo())
        }
    }
}

@ThreadLocal
object AboutModel : BaseModel(ServiceRegistry.coroutinesDispatcher) {
    lateinit var aboutData: List<AboutInfo>

    fun loadAboutInfo(proc: (aboutInfo: List<AboutInfo>) -> Unit) = launch {
        clLogCallback("loadAboutInfo AboutModel()")
        proc(backgroundSuspend { AboutProc.parseAbout() })
    }

    fun loadAboutInfo() = launch {
        clLogCallback("loadAboutInfo AboutModel()")
        backgroundSuspend { aboutData = AboutProc.parseAbout() }
    }
}

internal object AboutProc {
    fun parseAbout(): List<AboutInfo> {
        val aboutJsonString = staticFileLoader("about", "json")!!
        return Json.nonstrict.parse(AboutInfo.serializer().list, aboutJsonString)
    }
}

@Serializable
data class AboutInfo(val icon: String, val title: String, val detail: String)