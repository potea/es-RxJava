package study.rxjava.rxacidrain.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import study.rxjava.rxacidrain.data.WordData

/**
 * Created by CodeMaker on 2018-11-05.
 */
class RainView : View {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var wordList = listOf<WordData>()
    private val textPaint = Paint()

    init {
        textPaint.isAntiAlias = true
        textPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, context.resources.displayMetrics)
    }

    fun requestDraw(wordList: List<WordData>) {
        this.wordList = wordList
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            wordList.forEach {
                val width: Float = textPaint.measureText(it.word)
                val posY: Float =
                        if (it.point.x + width >= getWidth())
                            getWidth() - width * 2
                        else
                            it.point.x.toFloat()
                canvas.drawText(it.word, posY, it.point.y.toFloat(), textPaint)
            }
        }
    }
}