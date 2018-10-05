
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

    val correctString = ReadOnlyStringWrapper()

    lateinit var enterEvents: Observable<ActionEvent>
    val containerObservable = FXCollections.observableArrayList<HBox>((1..MAX_CONTAINER).map { HBox() })
    val stageObservable = Observable.just(containerObservable).flatMapIterable { it -> it }.map {
        it.alignment = Pos.TOP_CENTER
        it
    }.repeat()
    val speed = Observable.just((5..100).step(5)).flatMapIterable { it -> it }.repeat()
    val labelObservable = Observable.just(Label(worModel.sampleWord()))
    val labelY = ReadOnlyDoubleWrapper()
    val speedWrapper = ReadOnlyIntegerWrapper()


    val paneWrapper = ReadOnlyObjectWrapper<HBox>()

    private val INTERVAL_TIME: Long = 1 // seconds
    val trigger = Observable.interval(INTERVAL_TIME, TimeUnit.SECONDS)

    init {
        countString.bind(count.asString())

        var stage = stageObservable.filter { it.children.isEmpty() }.blockingFirst()
        speedWrapper.value = speedSample()

        paneWrapper.value = stage

        labelObservable
                .map {
                    labelY.bindBidirectional(it.translateYProperty())
                    it.translateY += speedWrapper.readOnlyProperty.value
                    it
                }
                .zipWith(trigger)
                .repeat()
                .observeOn(JavaFxScheduler.platform())
                .subscribe {
                    //                    val stage = stageSample()
                    val label = it.first
                    correctString.bindBidirectional(label.textProperty())
                    stage.children.clear()
                    if (stage.height >= label.translateY) {
                        stage.children.add(label)
                    } else {
                        speedWrapper.value = speedSample()
                        label.text = worModel.sampleWord()
                        label.translateY = 0.0
                        stage = stageSample()
                    }
                }
    }

    fun bindEnterEvent(node: Node) {
        enterEvents = JavaFxObservable.actionEventsOf(node)
        enterEvents.subscribe {

            if (correctString.value == currentInput.value) {
                count.value += correctString.value.length
                correctString.value = ""
                labelY.value = 0.0
                correctString.value = worModel.sampleWord()
                speedWrapper.value = speedSample()
                paneWrapper.value = stageSample()
            }
            currentInput.value = ""

        }
    }

    fun bindContainer(container: SplitPane) {
        container.items.addAll(containerObservable)
        val increment = (1 / containerObservable.size.toDouble())
        container.setDividerPositions(*increment.rangeTo(1.0).step(increment).map { Math.round(it * 100) / 100.0 }.toDoubleArray())
    }

    fun stageSample(): HBox {
        return stageObservable.filter { it.children.isEmpty() }.sample(1, TimeUnit.MICROSECONDS).blockingFirst()
    }

    fun speedSample(): Int {
        return speed.sample(1, TimeUnit.MICROSECONDS).blockingFirst()
    }

}
