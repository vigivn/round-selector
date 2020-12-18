package ru.vigivn.roundselector


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class RoundSelector @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        private const val DEFAULT_SIZE = 120
        private const val DEFAULT_FILL_COLOR = Color.LTGRAY
        private const val DEFAULT_FOREGROUND_COLOR = Color.GRAY
        private const val DEFAULT_BORDER_WIDTH = 8f
        private const val DEFAULT_BORDER_COLOR = Color.DKGRAY
    }

    private var radius = 0.0f
    private var innerRadius = 0.0f
    //central point
    private val cp = PointF(0.0f, 0.0f)
    //left top point
    private val ltp = PointF(0.0f, 0.0f)
    //right bottom point
    private val rbp = PointF(0.0f, 0.0f)
    private val rect = RectF()
    private val innerRect = RectF()

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    @ColorInt
    private var fillColor = DEFAULT_FILL_COLOR
    @ColorInt
    private var foregroundColor = DEFAULT_FOREGROUND_COLOR
    @ColorInt
    private var borderColor = DEFAULT_BORDER_COLOR

    private var borderWidth = DEFAULT_BORDER_WIDTH

    init {
        context?.withStyledAttributes(attrs, R.styleable.RoundSelector) {
            fillColor = getColor(R.styleable.RoundSelector_rc_backgroundColor, DEFAULT_FILL_COLOR)
            foregroundColor = getColor(R.styleable.RoundSelector_rc_foregroundColor, DEFAULT_FOREGROUND_COLOR)
            borderColor = getColor(R.styleable.RoundSelector_rc_borderColor, DEFAULT_BORDER_COLOR)
            borderWidth = getDimension(R.styleable.RoundSelector_rc_borderWidth, DEFAULT_BORDER_WIDTH)
        }

        setup()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2 * 0.95f
        innerRadius = radius / 6
        cp.x = w / 2f
        cp.y = h / 2f

        rect.set(cp.x - radius, cp.y - radius, cp.x+radius, cp.y+radius)
        innerRect.set(cp.x - innerRadius, cp.y - innerRadius, cp.x+innerRadius, cp.y+innerRadius)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val initSize = resolveDefaultSize(min(widthMeasureSpec, heightMeasureSpec))
        setMeasuredDimension(initSize, initSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(cp.x, cp.y, radius, foregroundPaint)
        canvas.drawCircle(cp.x, cp.y, radius*0.9f, backgroundPaint)
        canvas.drawArc(innerRect, 178f, 184f, false, foregroundPaint)
        canvas.drawArc(rect, 0f, 180f, false, foregroundPaint)

        canvas.drawCircle(cp.x, cp.y, radius, borderPaint)
        canvas.drawLine(cp.x - radius*0.9f, cp.y, cp.x - innerRadius, cp.y, borderPaint)
        canvas.drawArc(innerRect, 180f, 180f, false, borderPaint)
        canvas.drawLine(cp.x + innerRadius, cp.y, cp.x + radius*0.9f, cp.y, borderPaint)

        with(rect) {
            left = cp.x - radius * 0.9f
            right = cp.x + radius * 0.9f
            top = cp.y - radius * 0.9f
            bottom = cp.y + radius * 0.9f
        }
        canvas.drawArc(rect, 180f, 180f, false, borderPaint)
        rect.set(cp.x - radius, cp.y - radius, cp.x+radius, cp.y+radius)

        ltp.calculateXY(60f + 180f, radius * 0.9f)
        rbp.calculateXY(60f + 180f, innerRadius)
        canvas.drawLine(ltp.x, ltp.y, rbp.x, rbp.y, borderPaint)

        ltp.calculateXY(120f + 180f, radius * 0.9f)
        rbp.calculateXY(120f + 180f, innerRadius)
        canvas.drawLine(ltp.x, ltp.y, rbp.x, rbp.y, borderPaint)
    }

    private fun setup() {
        with(backgroundPaint) {
            style = Paint.Style.FILL
            color = fillColor
        }

        with(foregroundPaint) {
            style = Paint.Style.FILL
            color = foregroundColor
        }

        with(borderPaint) {
            style = Paint.Style.STROKE
            color = borderColor
            strokeWidth = borderWidth
        }
    }

    private fun resolveDefaultSize(spec: Int): Int {
        return when (MeasureSpec.getMode(spec)) {
            MeasureSpec.UNSPECIFIED -> DEFAULT_SIZE
            MeasureSpec.AT_MOST -> MeasureSpec.getSize(spec)
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec)
            else -> MeasureSpec.getSize(spec)
        }
    }

    private fun PointF.calculateXY(angle: Float, radius: Float) {
        x = cp.x + (radius * cos(angle * Math.PI.toFloat() / 180))
        y = cp.y + (radius * sin(angle * Math.PI.toFloat() / 180))
    }
}