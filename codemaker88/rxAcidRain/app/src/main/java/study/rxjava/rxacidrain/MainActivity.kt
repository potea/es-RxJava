package study.rxjava.rxacidrain

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by CodeMaker on 2018-10-31.
 */
class MainActivity : AppCompatActivity() {

    lateinit var gameManager: GameManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameManager = GameManager(this)
        gameManager.onCreate()

        rain_view.post {
            gameManager.initUi()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gameManager.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        gameManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        gameManager.onPause()
    }
}
