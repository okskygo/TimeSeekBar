package okskygo.rex.timeseekbar.library

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.support.v4.content.ContextCompat
import android.view.View
import okskygo.rex.timeseekbar.R
import java.text.DecimalFormat

class TimeThumbView constructor(context: Context) : View(context) {

  var thumbX: Float = 0.0f
  var thumbY: Float = 0.0f

  private val thumbWidth = 6.toPx
  var thumbHeight: Int = 0
  private val thumbRect: Rect = Rect()

  var minutes: Int = 0

  private val maxPinFont = 14.toSp
  private val textRect = Rect()
  private val textPaint = Paint().apply {
    color = Color.WHITE
    isAntiAlias = true
    textSize = maxPinFont
  }

  private val pinColor = ContextCompat.getColor(context, R.color.colorAccent)

  private val pinContentHeight = 20.toPx
  private val pinContentWidth = 50.toPx
  private val pinContentRect: RectF = RectF()
  private val pinContentPaint = Paint().apply {
    color = pinColor
    isAntiAlias = true
  }

  private val arrowPath = Path()
  private val pinArrowHeight = 10.toPx
  private val pinArrowWidth = 10.toPx
  private val pinArrowPaint = Paint().apply {
    color = pinColor
    isAntiAlias = true
    style = FILL
  }

  private var scaleRatio: Float = 0.0f
  private var pinPadding: Float = 0.0f

  private var thumbPressed = false

  private val rectPaint = Paint().apply {
    color = pinColor
    isAntiAlias = true
  }

  override fun onDraw(canvas: Canvas) {
    val halfHeight = thumbHeight / 2
    val halfWidth = thumbWidth / 2
    thumbRect.set((thumbX - halfWidth).toInt(),
                  (thumbY - halfHeight).toInt(),
                  (thumbX + halfWidth).toInt(),
                  (thumbY + halfHeight).toInt())
    canvas.drawRect(thumbRect, rectPaint)

    pinContentRect.set((thumbX - pinContentWidth / 2 * scaleRatio),
                       thumbY - pinPadding - ((pinContentHeight + pinArrowHeight) * scaleRatio),
                       (thumbX + pinContentWidth / 2 * scaleRatio),
                       (thumbY - pinPadding - (pinArrowHeight) * scaleRatio))
    canvas.drawRoundRect(pinContentRect, 4.0f.toPx, 4.0f.toPx, pinContentPaint)

    arrowPath.reset()
    arrowPath.moveTo(thumbX, thumbY - pinPadding)
    arrowPath.lineTo(thumbX + pinArrowWidth / 2 * scaleRatio,
                     thumbY - pinPadding - pinArrowHeight * scaleRatio)
    arrowPath.lineTo(thumbX - pinArrowWidth / 2 * scaleRatio,
                     thumbY - pinPadding - pinArrowHeight * scaleRatio)
    arrowPath.lineTo(thumbX, thumbY - pinPadding)
    arrowPath.close()
    canvas.drawPath(arrowPath, pinArrowPaint)

    val text = displayText()
    pinContentRect.round(textRect)
    calibrateTextSize(textPaint, text, pinContentRect.width())
    textPaint.getTextBounds(text, 0, text.length, textRect)
    textPaint.textAlign = Paint.Align.CENTER
    canvas.drawText(text,
                    thumbX,
                    thumbY - pinPadding - ((pinArrowHeight + pinContentHeight / 2) * scaleRatio) - ((textPaint.descent() + textPaint.ascent()) / 2),
                    textPaint)

    super.onDraw(canvas)
  }

  private fun displayText(): String {
    var hours = minutes / 60
    val minutesInDisplay = minutes % 60
    val formatter = DecimalFormat("00")
    return "${formatter.format(hours)} : ${formatter.format(minutesInDisplay)}"
  }

  fun setSize(scaleRatio: Float, pinPadding: Float) {
    this.scaleRatio = scaleRatio
    this.pinPadding = pinPadding
  }

  fun press() {
    thumbPressed = true
  }

  fun release() {
    thumbPressed = false
  }

  override fun isPressed(): Boolean {
    return thumbPressed
  }

  fun isInTargetZone(x: Float, y: Float): Boolean {
    return Math.abs(x - thumbX) <= (thumbWidth * 2) && Math.abs(y - thumbY) <= (thumbHeight / 2)
  }

  private fun calibrateTextSize(paint: Paint, text: String, boxWidth: Float) {
    val testTextSize = 48f

    paint.textSize = testTextSize
    paint.getTextBounds(text, 0, text.length, textRect)

    var desiredTextSize = testTextSize * boxWidth / textRect.width()
    if (desiredTextSize > maxPinFont) {
      desiredTextSize = maxPinFont
    }
    paint.textSize = desiredTextSize
  }
}