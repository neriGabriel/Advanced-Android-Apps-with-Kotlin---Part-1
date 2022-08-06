package com.udacity.load

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.withStyledAttributes
import com.udacity.R
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.properties.Delegates

/**
 * LoadingButton
 *
 * Custom view for the download button which has specific animations for each state.
 *
 * @param: Context
 * @param: AttributeSet?
 * @param: defStyleAttr
 * */
class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    /**
     * Custom attributes.
     * */
    private var loadingDefaultBackgroundColor = 0
    private var loadingBackgroundColor = 0
    private var loadingDefaultText = ""
    private var loadingText = ""
    private var loadingTextColor = 0
    private var progressCircleBackgroundColor = 0

    /**
     * Paint object for the button itself, it has the fill style.
     * Geometry and text drawn with this style will be filled, ignoring all
     * stroke-related settings in the paint.
     *
     * @param: Int Flag
     *
     * [Paint.ANTI_ALIAS_FLAG:  Paint flag that enables antialiasing when drawing]
     * */
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    /**
     * String resource for the button.
     * */
    private var buttonText = ""

    /**
     * Paint object for the button itself,  it has the fill style, such as center text alignment,
     * text size of 50 and the default typeface.
     *
     * @param: Int Flag
     *  */
    private val buttonTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50f
        typeface = Typeface.DEFAULT
    }

    /**
     * Rect resource for the button text bounds.
     * */
    private lateinit var buttonTextBounds: Rect

    /**
     * Rect resource for the circle progress.
     * */
    private val progressCircleRect = RectF()

    /**
     * Float resource for the circle progress size.
     * */
    private var progressCircleSize = 0f

    /**
     * AnimatorSet object responsible for tracking the animation properties such as duration
     * and trigger events onStart and onEnd
     * */
    private val animatorSet = AnimatorSet().apply{
        duration = ANIMATOR_TIME
        doOnStart { this@LoadingButton.isEnabled = false }
        doOnEnd { this@LoadingButton.isEnabled = true }
    }

    /**
     *  Float resource for the current circle animation progress.
     * */
    private var currentProgressCircleAnimation = 0f

    /**
     * ValueAnimator for the circle animation progress.
     * */
    private val progressCircleAnimator = ValueAnimator.ofFloat(0f, FULL_ANGLE).apply {
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            currentProgressCircleAnimation = it.animatedValue as Float
            invalidate()
        }
    }

    /**
     * Float resource for the button background animation
     * */
    private var currentButtonBackgroundAnimation = 0f

    /**
     * ValueAnimator for button background
     * */
    private lateinit var buttonBackgroundAnimator: ValueAnimator

    /**
     * Observable button state, responsible for change the animation.
     * */
    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        Timber.d("The state of button has changed: ${new}")
        when(new) {
            ButtonState.Loading -> {
                buttonText = loadingText
                if(!::buttonTextBounds.isInitialized) {
                    //Initializing the bounds of button text such as it Paint object
                    buttonTextBounds = Rect()
                    buttonTextPaint.getTextBounds(buttonText, 0, buttonText.length, buttonTextBounds)

                    //Computing progress circle values
                    val horizontal = (buttonTextBounds.right + buttonTextBounds.width() + 16f)
                    val vertical = (heightSize / HALF_VALUE)
                    progressCircleRect.set(
                        horizontal - progressCircleSize,
                        vertical - progressCircleSize,
                        horizontal + progressCircleSize,
                        vertical + progressCircleSize
                    )
                }
                animatorSet.start()
            } else -> {
                buttonText = loadingDefaultText
                new.takeIf { it == ButtonState.Completed }?.run { animatorSet.cancel() }
            }
        }
    }

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            loadingDefaultBackgroundColor = getColor(
                R.styleable.LoadingButton_loadingDefaultBackgroundColor, 0)
            loadingBackgroundColor = getColor(
                R.styleable.LoadingButton_loadingBackgroundColor, 0)
            loadingDefaultText = getText(R.styleable.LoadingButton_loadingDefaultText).toString()
            loadingTextColor = getColor(R.styleable.LoadingButton_loadingTextColor, 0)
            loadingText = getText(R.styleable.LoadingButton_loadingText).toString()
        }.also {
            buttonText = loadingDefaultText
            progressCircleBackgroundColor = context.getColor(R.color.colorAccent)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        progressCircleSize = (min(w,h) / HALF_VALUE) * PROGRESS_BAR_CIRCLE_SIZE
        ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                currentButtonBackgroundAnimation = it.animatedValue as Float
                invalidate()
            }
        }.also {
            buttonBackgroundAnimator = it
            animatorSet.apply {
                playTogether(progressCircleAnimator, buttonBackgroundAnimator)
            }
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed) {
            buttonState = ButtonState.Clicked
            invalidate()
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { buttonCanvas ->
            buttonCanvas.apply {
                drawBackgroundColor()
                drawButtonText()
                drawProgressCircleIfLoading(buttonCanvas)
            }
        }
    }

    /**
     * Canvas extension functions responsible for changing the text of background
     * */
    private fun Canvas.drawButtonText() {
        buttonTextPaint.color = loadingTextColor
        drawText(buttonText,
            widthSize / HALF_VALUE,
            (heightSize / HALF_VALUE) + buttonTextPaint.computeTextOffset(),
            buttonTextPaint
        )
    }

    private fun TextPaint.computeTextOffset() = ((descent() - ascent()) / 2) - descent()

    private fun Canvas.drawBackgroundColor() {
        when (buttonState) {
            ButtonState.Loading -> {
                drawBackgroundWhenLoading()
                drawDefaultBackground()
            }
            else -> drawColor(loadingDefaultBackgroundColor)
        }
    }

    private fun Canvas.drawBackgroundWhenLoading() = buttonPaint.apply {
        color = loadingBackgroundColor
    }.run {
        drawRect(
            0f,
            0f,
            currentButtonBackgroundAnimation,
            heightSize.toFloat(),
            buttonPaint
        )
    }

    private fun Canvas.drawDefaultBackground() = buttonPaint.apply {
        color = loadingDefaultBackgroundColor
    }.run {
        drawRect(
            currentButtonBackgroundAnimation,
            0f,
            widthSize.toFloat(),
            heightSize.toFloat(),
            buttonPaint
        )
    }

    private fun drawProgressCircleIfLoading(buttonCanvas: Canvas) =
        buttonState.takeIf { it == ButtonState.Loading }?.let {
            buttonPaint.color = progressCircleBackgroundColor
            buttonCanvas.drawArc(
                progressCircleRect,
                0f,
                currentProgressCircleAnimation,
                true,
                buttonPaint
            )
        }

    /**
     * Update button state
     * */
    fun updateButtonState(state: ButtonState) {
        if(state != buttonState) {
            buttonState = state
            invalidate()
        }
    }

    companion object {
        private val ANIMATOR_TIME = TimeUnit.SECONDS.toMillis(3)
        private const val HALF_VALUE = 2f
        private const val PROGRESS_BAR_CIRCLE_SIZE = 0.4F
        private const val FULL_ANGLE = 360f
    }

}