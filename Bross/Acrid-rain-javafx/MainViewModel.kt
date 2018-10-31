import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxjavafx.observables.JavaFxObservable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.SplitPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import views.VerticalMoveLabel
import java.util.concurrent.TimeUnit


infix fun ClosedRange<Double>.step(step: Double): Iterable<Double> {
    require(start.isFinite())
    require(endInclusive.isFinite())
    require(step > 0.0) { "Step must be positive, was: $step." }
    val sequence = generateSequence(start) { previous ->
        if (previous == Double.POSITIVE_INFINITY) return@generateSequence null
        val next = previous + step
        if (next > endInclusive) null else next
    }
    return sequence.asIterable()
}


class MainViewModel {

    private val worModel = Model.WordModel

    val MAX_CONTAINER = 5
    val count = SimpleIntegerProperty(0)
    val currentInput = SimpleStringProperty()
    val countString = ReadOnlyStringWrapper()

    lateinit var enterEvents: Observable<ActionEvent>
    val containerObservable = FXCollections.observableArrayList<HBox>((1..MAX_CONTAINER).map { HBox() })
    val stageObservable = Observable.just(containerObservable).flatMapIterable { it -> it }.map {
        it.alignment = Pos.TOP_CENTER
        it
    }

    val speed = ReadOnlyIntegerWrapper(5)
    val labels = mutableListOf<VerticalMoveLabel>()

    init {
        countString.bind(count.asString())

        stageObservable
                .filter { it.children.isEmpty() }
                .subscribe {
                    val label = VerticalMoveLabel(worModel.sampleWord(), speed.value)
                    it.children.add(label)
                    label.move()
                    labels.add(label)
                }
    }

    fun bindEnterEvent(node: Node) {
        enterEvents = JavaFxObservable.actionEventsOf(node)
        enterEvents.subscribe { _ ->

            labels.asSequence()
                    .filter { it.isCorrect(currentInput.value) }
                    .maxBy { it.positionY.value }
                    .let {
                        if (it != null) {
                            count.value += it.text.length
                            speed.value += 1
                            it.invalidate(spped = speed.value)
                            it.text = worModel.sampleWord()
                        }
                    }
            currentInput.value = ""
        }
    }

    fun bindContainer(container: SplitPane) {
        container.items.addAll(containerObservable)
        val increment = (1 / containerObservable.size.toDouble())
        container.setDividerPositions(*increment.rangeTo(1.0).step(increment).map { Math.round(it * 100) / 100.0 }.toDoubleArray())
    }
}
