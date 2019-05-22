package co.touchlab.sessionize

import co.touchlab.sessionize.reaktive.asObservable
import co.touchlab.sessionize.reaktive.mainSubscribe
import co.touchlab.stately.ensureNeverFrozen
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.ioScheduler
import com.squareup.sqldelight.Query
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Sort of a "controller" in MVC thinking. Pass in the SQLDelight Query,
 * a method to actually extract data on updates. The View interface is
 * generically defined to take data extracted from the Query, and manage
 * registering and shutting down.
 */
abstract class BaseQueryModelView<Q : Any, VT>(
        query: Query<Q>,
        extractData: (Query<Q>) -> VT,
        mainContext: CoroutineContext) : BaseModel(mainContext) {

    private val queryDisposable: Disposable

    init {
        ensureNeverFrozen()

        val queryObservable = query.asObservable(ioScheduler)
        queryDisposable = queryObservable
                .mainSubscribe {
                    val vt = extractData(it)

                    view?.let {
                        launch {
                            it.update(vt)
                        }
                    }
                }
    }

    private var view: View<VT>? = null

    fun register(view: View<VT>) {
        this.view = view
    }

    fun shutDown() {
        view = null
        queryDisposable.dispose()
    }

    interface View<VT> {
        suspend fun update(data: VT)
    }
}