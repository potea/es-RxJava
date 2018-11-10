package study.rxjava.rxacidrain

import android.graphics.Point
import android.view.View
import android.view.inputmethod.EditorInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.observables.ConnectableObservable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import study.rxjava.rxacidrain.data.GameData
import study.rxjava.rxacidrain.data.GameLevel
import study.rxjava.rxacidrain.data.WordData
import study.rxjava.rxacidrain.event.Name.*
import study.rxjava.rxacidrain.event.RxEventBus
import study.rxjava.rxacidrain.event.RxEventBus.sendEvent
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by CodeMaker on 2018-10-31.
 */
class GameManager(private val mainActivity: MainActivity) {

    val RAIN_MOVEMENT = 10
    val TIC_INTERVAL = 250L

    private val gameData = GameData()

    private var rainViewHeight = 0
    private var rainViewWidth = 0

    private val compositeDisposable = CompositeDisposable()

    fun initUi() {
        rainViewHeight = mainActivity.rain_view.height
        rainViewWidth = mainActivity.rain_view.width

        mainActivity.button_start.setOnClickListener {
            it.visibility = View.GONE
            gameData.resetData()
            startGame()
        }

        mainActivity.edit_text.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_UNSPECIFIED, EditorInfo.IME_ACTION_GO -> {
                    val userInput = v.text.toString()
                    if (userInput.isNotEmpty()) {
                        sendEvent(UserInputChanged, userInput)
                        v.text = ""
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun startGame() {
        //시간 흐름 생성
        val ticObservable = ConnectableObservable.interval(TIC_INTERVAL, TimeUnit.MILLISECONDS)
                .filter { !gameData.isPaused }
                .publish()
        compositeDisposable.add(ticObservable.connect())

        //시간에 따라 단어 생성
        compositeDisposable.add(Observable.zip(
                getWordObservable(),
                ticObservable.filter { tic: Long -> tic % gameData.level.wordGenInterval == 0L },
                BiFunction { word: String, tic: Long ->
                    WordData(word, speed = Random().nextInt(5) + 1,
                            point = Point(Random().nextInt(rainViewWidth), 0))
                })
                .subscribe { gameData.wordMap[it.word] = it })

        //일정 시간마다 단어 위치 변화
        compositeDisposable.add(ticObservable
                .filter { tic: Long -> tic % gameData.level.rainDropInterval == 0L }
                .subscribe {
                    val removeList = mutableListOf<String>()
                    gameData.wordMap.values.forEach {
                        it.point.y += it.speed * RAIN_MOVEMENT

                        //땅에 닿으면 제거
                        if (it.point.y > rainViewHeight) {
                            removeList.add(it.word)
                            gameData.life -= 1
                            sendEvent(LifeChanged)
                        }

                        //end
                        if (gameData.life == 0) {
                            sendEvent(GameOver)
                        }
                    }

                    removeList.forEach {
                        gameData.wordMap.remove(it)
                    }

                    sendEvent(WordDataChanged)
                })

        //점수 변화마다 레벨 변경 체크
        compositeDisposable.add(RxEventBus.getEventSubject()
                .filter { it.name == ScoreChanged }
                .subscribe {
                    if (gameData.score > gameData.level.levelScoreInterval) {
                        gameData.level = when (gameData.level) {
                            GameLevel.EASY -> GameLevel.NORMAL
                            GameLevel.NORMAL -> GameLevel.HARD
                            else -> GameLevel.HARD
                        }
                    }
                })

        //입력된 단어처리
        compositeDisposable.add(RxEventBus.getEventSubject()
                .filter { it.name == UserInputChanged && it.data is String }
                .subscribe {
                    val userInput = it.data
                    val wordData = gameData.wordMap[userInput]
                    if (wordData != null) {
                        gameData.score += wordData.score
                        gameData.wordMap.remove(wordData.word)
                        sendEvent(ScoreChanged)
                        sendEvent(WordDataChanged)
                    }
                })

        //단어 변화마다 산성비 다시 그리기
        compositeDisposable.add(RxEventBus.getEventSubject()
                .filter { it.name == WordDataChanged }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mainActivity.rain_view.requestDraw(gameData.wordMap.values.toList())
                })

        //라이프 변화마다 다시 그리기
        compositeDisposable.add(RxEventBus.getEventSubject()
                .filter { it.name == LifeChanged }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mainActivity.text_view_life?.text = "${gameData.life}"
                })

        //점수 변화마다 다시 그리기
        compositeDisposable.add(RxEventBus.getEventSubject()
                .filter { it.name == ScoreChanged }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mainActivity.text_view_score?.text = "${gameData.score}"
                })

        //게임 끝
        compositeDisposable.add(RxEventBus.getEventSubject()
                .filter { it.name == GameOver }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    compositeDisposable.clear()
                    mainActivity.button_start?.visibility = View.VISIBLE
                })

        sendEvent(WordDataChanged)
        sendEvent(ScoreChanged)
        sendEvent(LifeChanged)
    }

    private fun getWordObservable(): Observable<String> {
        return mainActivity.resources.assets.open("word.csv")
                .bufferedReader()
                .lineSequence()
                .map { line -> line.split(",") }
                .flatten()
                .toList()
                .shuffled()
                .toObservable()
                .subscribeOn(Schedulers.io())
    }

    fun onCreate() {

    }

    fun onDestroy() {
        compositeDisposable.clear()
    }

    fun onResume() {
        if (gameData.isPaused) {
            gameData.isPaused = false
        }
    }

    fun onPause() {
        if (!gameData.isPaused) {
            gameData.isPaused = true
        }
    }
}