package okskygo.rex.timeseekbar.library

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class TimeSeekBar @JvmOverloads constructor(context: Context,
                                            attrs: AttributeSet? = null,
                                            defStyle: Int = 0)
  : View(context, attrs, defStyle) {

  private val defaultWidth = 250.toPx

  private val defaultHeight = 76.toPx

  private val barHeight = 40.toPx

  private val thumbWidth = 50.0f.toPx

  private lateinit var bar: TimeBar

  private lateinit var leftThumb: TimeThumbView

  private lateinit var rightThumb: TimeThumbView

  private lateinit var connectingRect: TimeConnectingRect

  private var diffX: Int = 0

  private var diffY: Int = 0

  private var lastX: Float = 0.0f

  private var lastY: Float = 0.0f

  var onChangeListener: ((TimeInterval) -> Unit)? = null

  /**
   * TimeInterval value in 0 ~ 1440 minutes
   */
  var timeInterval = listOf<TimeInterval>()
    set(value) {
      field = value
      invalidate()
    }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

    val measureWidthMode = MeasureSpec.getMode(widthMeasureSpec)
    val measureHeightMode = MeasureSpec.getMode(heightMeasureSpec)
    val measureWidth = MeasureSpec.getSize(widthMeasureSpec)
    val measureHeight = MeasureSpec.getSize(heightMeasureSpec)

    val width = when (measureWidthMode) {
      MeasureSpec.AT_MOST -> defaultWidth
      MeasureSpec.EXACTLY -> measureWidth
      MeasureSpec.UNSPECIFIED -> defaultWidth
      else -> defaultWidth
    }

    val height = when (measureHeightMode) {
      MeasureSpec.AT_MOST -> defaultHeight
      MeasureSpec.EXACTLY -> measureHeight
      MeasureSpec.UNSPECIFIED -> defaultHeight
      else -> defaultHeight
    }

    setMeasuredDimension(width, height)
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)

    val marginHorizontal = thumbWidth / 2

    leftThumb = TimeThumbView(context)
    leftThumb.thumbX = marginHorizontal
    leftThumb.thumbY = height - (barHeight / 2.0f)
    leftThumb.thumbHeight = barHeight
    leftThumb.minutes = 0

    rightThumb = TimeThumbView(context)
    rightThumb.thumbX = w.toFloat() - marginHorizontal
    rightThumb.thumbY = height - (barHeight / 2.0f)
    rightThumb.thumbHeight = barHeight
    rightThumb.minutes = 1440

    bar = TimeBar(h, (w - thumbWidth).toInt(), barHeight, marginHorizontal,
                  (w - marginHorizontal), marginHorizontal)
    bar.timeInterval = timeInterval

    connectingRect = TimeConnectingRect(context, height - barHeight, height)

  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    bar.draw(canvas)
    leftThumb.draw(canvas)
    rightThumb.draw(canvas)
    connectingRect.draw(canvas, leftThumb, rightThumb)
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (!isEnabled) {
      return false
    }
    when (event.action) {

      MotionEvent.ACTION_DOWN -> {
        diffX = 0
        diffY = 0

        lastX = event.x
        lastY = event.y

        onActionDown(event.x, event.y)
        return true
      }

      MotionEvent.ACTION_UP -> {
        this.parent.requestDisallowInterceptTouchEvent(false)
        onActionUp(event.x, event.y)
        return true
      }

      MotionEvent.ACTION_CANCEL -> {
        this.parent.requestDisallowInterceptTouchEvent(false)
        onActionUp(event.x, event.y)
        return true
      }

      MotionEvent.ACTION_MOVE -> {
        onActionMove(event.x)
        this.parent.requestDisallowInterceptTouchEvent(true)
        val curX = event.x
        val curY = event.y
        diffX += Math.abs(curX - lastX).toInt()
        diffY += Math.abs(curY - lastY).toInt()
        lastX = curX
        lastY = curY

        if (diffX < diffY) {
          //vertical touch
          parent.requestDisallowInterceptTouchEvent(false)
          return false
        }

        return true
      }

      else -> return false
    }

  }

  private fun onActionMove(x: Float) {
    if (leftThumb.isPressed) {
      moveThumb(leftThumb, x)
    } else if (rightThumb.isPressed) {
      moveThumb(rightThumb, x)
    }

    if (leftThumb.thumbX > rightThumb.thumbX) {
      val temp = leftThumb
      leftThumb = rightThumb
      rightThumb = temp
    }

    onChangeListener?.invoke(TimeInterval(leftThumb.minutes, rightThumb.minutes))
  }

  private fun moveThumb(thumb: TimeThumbView, x: Float) {
    when {
      x < bar.left -> {
        thumb.thumbX = bar.left
        thumb.minutes = 0
      }
      x > bar.right -> {
        thumb.thumbX = bar.right
        thumb.minutes = 1440
      }
      x >= bar.left && x <= bar.right -> {
        thumb.thumbX = x
        thumb.minutes = bar.getMinutes(x)
      }
    }
    invalidate()
  }

  private fun onActionUp(x: Float, y: Float) {
    if (leftThumb.isPressed) {
      releaseThumb(leftThumb, x)
    } else if (rightThumb.isPressed) {
      releaseThumb(rightThumb, x)
    }
  }

  private fun releaseThumb(thumb: TimeThumbView, x: Float) {
    val animator = ValueAnimator.ofFloat(1.0f, 0.0f)
    animator.addUpdateListener { animation ->
      thumb.setSize(animation.animatedValue as Float, (bar.barHeight / 2) + 6.0f.toPx)
      invalidate()
    }
    animator.start()
    thumb.release()
  }

  private fun onActionDown(x: Float, y: Float) {
    if (!rightThumb.isPressed && leftThumb.isInTargetZone(x, y)) {
      pressThumb(leftThumb)
    } else if (!leftThumb.isPressed && rightThumb.isInTargetZone(x, y)) {
      pressThumb(rightThumb)
    }
  }

  private fun pressThumb(thumb: TimeThumbView) {
    val animator = ValueAnimator.ofFloat(0.0f, 1.0f)
    animator.addUpdateListener { animation ->
      thumb.setSize(animation.animatedValue as Float, (bar.barHeight / 2) + 6.0f.toPx)
      invalidate()
    }
    animator.start()
    thumb.press()
  }
}