package co.touchlab.sessionize.reaktive

import co.touchlab.stately.concurrency.AtomicBoolean
import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.ThreadLocalRef
import co.touchlab.stately.concurrency.value
import co.touchlab.stately.freeze
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableEmitter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.observable
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.scheduler.mainScheduler
import com.squareup.sqldelight.Query

fun <T : Any> Query<T>.asObservable(scheduler: Scheduler = mainScheduler): Observable<Query<T>> = observable<Query<T>> { emitter ->
    val listenerAndDisposable = QueryListenerAndDisposable<T>(emitter, this)
    emitter.setDisposable(listenerAndDisposable)
    this.addListener(listenerAndDisposable)
    emitter.onNext(this)
}.observeOn(scheduler)


private class QueryListenerAndDisposable<T : Any>(
        private val emitter: ObservableEmitter<Query<T>>,
        private val query: Query<T>
) : Query.Listener, Disposable {
    private val ab = AtomicBoolean(false)
    override fun queryResultsChanged() {
        emitter.onNext(query)
    }

    override val isDisposed = ab.value

    override fun dispose() {
        if (ab.compareAndSet(false, true)) {
            query.removeListener(this)
        }
    }
}

fun <T : Any> Observable<Query<T>>.mapToOne(): Observable<T> {
    return map { it.executeAsOne() }
}

fun <T : Any> Observable<Query<T>>.mapToOneOrDefault(defaultValue: T): Observable<T> {
    return map { it.executeAsOneOrNull() ?: defaultValue }
}

fun <T : Any> Observable<Query<T>>.mapToList(): Observable<List<T>> {
    return map { it.executeAsList() }
}

/*
fun <T : Any> Observable<Query<T>>.mapToOneNonNull(): Observable<T> {
    return flatMap {
        val result = it.executeAsOneOrNull()
        if (result == null) Observable.empty() else Observable.just(result)
    }
}*/

internal fun <T> Observable<T>.mainSubscribe(onNext: (T) -> Unit): Disposable {
    val mtDisposable = MainThreadDisposable(onNext).freeze()

    mtDisposable.delegate.value = observeOn(mainScheduler)
            .subscribe {
                mtDisposable.call(it)
            }

    return mtDisposable
}

internal class MainThreadDisposable<T>(onNext: (T) -> Unit) : Disposable {
    internal val delegate = AtomicReference<Disposable?>(null)

    private val onNextRef: ThreadLocalRef<(T) -> Unit> = ThreadLocalRef()

    init {
        onNextRef.value = onNext
    }

    override val isDisposed: Boolean
        get() = delegate.value?.isDisposed ?: true

    fun call(arg:T){
        onNextRef.value?.invoke(arg)
    }

    override fun dispose() {
        delegate.value?.dispose()
        onNextRef.remove()
    }
}
