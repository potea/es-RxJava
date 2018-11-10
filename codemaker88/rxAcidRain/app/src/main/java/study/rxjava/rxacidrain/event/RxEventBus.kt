package study.rxjava.rxacidrain.event

import io.reactivex.subjects.PublishSubject

/**
 * Created by CodeMaker on 2018-11-08.
 */
data class Event(val name: Name, val data: Any)

object RxEventBus {
    private val eventSubject = PublishSubject.create<Event>()

    fun getEventSubject(): PublishSubject<Event> {
        return eventSubject
    }

    fun sendEvent(event: Name, data: Any = Unit) {
        eventSubject.onNext(Event(event, data))
    }
}