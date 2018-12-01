package study.rxjava.rxacidrain.data

/**
 * Created by CodeMaker on 2018-11-10.
 */
class GameData {
    val wordMap = hashMapOf<String, WordData>()
    var life = 5
    var level = GameLevel.EASY
    var score = 0
    var isPaused = false

    fun resetData() {
        life = 5
        level = GameLevel.EASY
        score = 0
        isPaused = false
        wordMap.clear()
    }
}