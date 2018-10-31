package views

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.beans.property.*
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import utils.GraphicUtils
import java.util.concurrent.TimeUnit

class VerticalMoveLabel(text: String = "") : Label(text), VerticalMoveable {

    override val positionY = ReadOnlyDoubleWrapper()
    override val speed = SimpleIntegerProperty()
    val correct = SimpleStringProperty()
    private lateinit var moveObservable: Disposable
    private var MAX_HEGIHT: Double = GraphicUtils.getDisplay().y * 0.6

    constructor(text: String, speed: Int) : this() {
        this.text = text
        this.speed.value = speed
        correct.bindBidirectional(textProperty())
        translateYProperty().bindBidirectional(positionY)
    }

    fun move() {
        moveObservable = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(JavaFxScheduler.platform())
                .subscribe {
                    if (MAX_HEGIHT >= (positionY.value + this.height)) {
                        positionY.value += this.speed.value
                    } else {
                        positionY.value = 0.0
                    }
                }
    }

    fun dispose() {
        moveObservable.dispose()
    }

    fun invalidate() {
        this.speed.value = 0
        this.positionY.value = 0.0
        this.text = ""
    }

    fun invalidate(spped: Int) {
        this.speed.value = spped
        this.positionY.value = 0.0
        this.text = ""
    }

    fun isCorrect(word: String): Boolean {
        return text == word
    }

}
