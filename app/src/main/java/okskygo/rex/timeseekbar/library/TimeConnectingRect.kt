package okskygo.rex.timeseekbar.library

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import okskygo.rex.timeseekbar.R
import kotlin.math.max
import kotlin.math.min

class TimeConnectingRect(private val context: Context,
                         private val startY: Int,
                         private val endY: Int) {

  private val paint = Paint().apply {
    color = ContextCompat.getColor(context, R.color.colorAccent)
    alpha = (255 * 0.5f).toInt()
    isAntiAlias = true
  }

  fun draw(canvas: Canvas, leftThumbView: TimeThumbView, rightThumbView: TimeThumbView) {
    val left = min(leftThumbView.thumbX, rightThumbView.thumbX)
    val right = max(leftThumbView.thumbX, rightThumbView.thumbX)
    val rect = Rect(left.toInt(), startY, right.toInt(), endY)
    canvas.drawRect(rect, paint)
  }

}