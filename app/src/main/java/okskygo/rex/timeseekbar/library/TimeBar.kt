package okskygo.rex.timeseekbar.library

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Rect
import java.util.Collections
import kotlin.math.max
import kotlin.math.roundToInt

data class TimeBar(val height: Int,
                   val barWidth: Int,
                   val barHeight: Int,
                   val left: Float,
                   val right: Float,
                   val marginHorizontal: Float) {

  var timeInterval: List<TimeInterval> = listOf()
    set(value) {
      field = merge(value.toMutableList().map {
        TimeInterval(if (it.start < 0) 0 else it.start, if (it.end > 1440) 1440 else it.end)
      }.filter {
        it.start != it.end
      }.sortedBy {
        it.start
      })
    }

  private val dataPaint = Paint().apply {
    color = Color.parseColor("#4a4a4a")
    alpha = (255 * 0.5f).toInt()
    isAntiAlias = true
  }

  //minutes
  private val tickCount = 1440.0f

  private val minuteWidth: Float = barWidth / tickCount

  private val underLineWidth = 1.0f.toPx

  private val underLinePaint = Paint().apply {
    color = Color.parseColor("#DBDBDB")
    style = Style.STROKE
    strokeWidth = underLineWidth
    isAntiAlias = true
  }

  private val tickWidth = 1.0f.toPx

  private val tickPaint = Paint().apply {
    color = Color.parseColor("#EEEEEE")
    strokeWidth = tickWidth
    isAntiAlias = true
  }

  private fun merge(intervals: List<TimeInterval>): List<TimeInterval> {

    if (intervals.isEmpty() || intervals.size == 1)
      return intervals

    Collections.sort(intervals, IntervalComparator())

    val first = intervals[0]
    var start = first.start
    var end = first.end

    val result = mutableListOf<TimeInterval>()

    for (i in 1 until intervals.size) {
      val current = intervals[i]
      if (current.start <= end) {
        end = max(current.end, end)
      } else {
        result.add(TimeInterval(start, end))
        start = current.start
        end = current.end
      }
    }

    result.add(TimeInterval(start, end))
    return result
  }

  fun draw(canvas: Canvas) {
    drawUnderLine(canvas)
    drawTick(canvas)
    drawData(canvas)
  }

  fun getMinutes(x: Float): Int {
    return ((x - marginHorizontal) / minuteWidth).roundToInt()
  }

  private fun getX(minutes: Int): Float {
    return minuteWidth * minutes + marginHorizontal
  }

  private fun drawData(canvas: Canvas) {
    val top = height - barHeight
    val bottom = height
    timeInterval.forEach {
      val rect = Rect(getX(it.start).toInt(), top.toInt(), getX(it.end).toInt(), bottom)
      canvas.drawRect(rect, dataPaint)
    }
  }

  private fun drawUnderLine(canvas: Canvas) {
    canvas.drawLine(marginHorizontal,
                    (height - underLineWidth),
                    barWidth.toFloat(),
                    height.toFloat(),
                    underLinePaint)
  }

  private fun drawTick(canvas: Canvas) {

    val hourWidth = barWidth / 24.0f
    val halfHourWidth = hourWidth / 2.0f

    val tickerStartY = height - barHeight
    val halfTickerHeight = height - (barHeight / 2.0f)

    for (ticker in 0..48) {
      val i = ticker.rem(2)
      when (i) {
        0 -> {
          val x = (ticker / 2) * hourWidth
          canvas.drawLine(x + marginHorizontal,
                          tickerStartY.toFloat(),
                          x + marginHorizontal,
                          height.toFloat(), tickPaint)
        }
        1 -> {
          val x = (ticker / 2) * hourWidth + halfHourWidth
          canvas.drawLine(x + marginHorizontal,
                          halfTickerHeight,
                          x + marginHorizontal,
                          height.toFloat(),
                          tickPaint)
        }
      }
    }
  }

  private class IntervalComparator : Comparator<TimeInterval> {
    override fun compare(i1: TimeInterval, i2: TimeInterval): Int {
      return i1.start - i2.start
    }
  }
}
