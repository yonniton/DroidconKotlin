package co.touchlab.sessionize

import co.touchlab.sessionize.db.SponsorGroupDbItem
import co.touchlab.sessionize.platform.backgroundTask

class SponsorViewModel {
    val sponsorModel = SponsorModel()

    fun registerForChanges(proc:(data: List<SponsorGroupDbItem>)->Unit){
        sponsorModel.register(object : SponsorModel.SponsorView {
            override suspend fun update(data: List<SponsorGroupDbItem>) {
                proc(data)
            }
        })
    }

    fun unregister(){
        sponsorModel.shutDown()
    }

    @Throws
    fun forceCrash() {
        backgroundTask({
            loopdyLoop()
        }) {
            println("Too Far!")
        }
    }

    @Throws
    fun loopdyLoop() {
        for (i in 0 until 10){
            println("$i")
            if(i > 5)
                helloDarkness()
        }
    }

    @Throws
    fun helloDarkness() {
        throw IllegalStateException("Hello darkness, my old friend")
    }
}