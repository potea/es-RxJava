package study.rxjava.rxacidrain.data

/**
 * Created by CodeMaker on 2018-11-10.
 */
enum class GameLevel(val levelScoreInterval: Int, val wordGenInterval: Long, val rainDropInterval: Int) {
    EASY(20, 16, 6),
    NORMAL(50, 8, 4),
    HARD(Int.MAX_VALUE, 4, 2)
}