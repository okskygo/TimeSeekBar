package okskygo.rex.timeseekbar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.seekBar
import okskygo.rex.timeseekbar.library.TimeInterval

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    seekBar.timeInterval = listOf(TimeInterval(650, 1000))
    seekBar.onChangeListener = {
      println(">>>>>>>>>>> start = ${it.start}, end = ${it.end}")
    }
  }
}
