

sealed class Model {
    object WordModel : Model() {

        val file = ResourceHelper.convertFile("dictionary.txt")
        private val words: List<String>
        private val textsObservable: Observable<String>

        private val INTERVAL_TIME: Long = 1 // seconds
        val trigger = Observable.interval(INTERVAL_TIME, TimeUnit.MICROSECONDS)

        init {
            words = file.readLines(Charsets.UTF_8)

//            textsObservable = Observable.fromArray(words).flatMapIterable { it -> it }.sorted().filter { it.length > 2 }.concatMap { it->
//                Observable.just(it).zipWith(trigger).map { it.first }
//            }


            textsObservable = Observable
                    .just(words)
                    .observeOn(Schedulers.io())
                    .flatMapIterable { it -> it }
                    .sorted()
                    .filter { it.length > 2 }
                    .repeat()
        }

        fun sampleWord(): String {
            return textsObservable.sample(1, TimeUnit.MICROSECONDS).blockingFirst()
        }

    }


}
