package study.rxjava.rxacidrain.data

import android.graphics.Point

/**
 * Created by CodeMaker on 2018-11-09.
 */
data class WordData(val word: String, val speed: Int, var point: Point) {
    val score: Int
        get() {
            return word.length
        }
}