package ru.vigivn.roundselector


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.toBitmap
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
        private const val DEFAULT_LABELS_COLOR = Color.BLACK
        private const val DEFAULT_LABELS_TEXT_SIZE = 44f
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
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var bitmap: Bitmap
    private var bitmapMatrix = Matrix()

    @ColorInt
    private var fillColor = DEFAULT_FILL_COLOR

    @ColorInt
    private var foregroundColor = DEFAULT_FOREGROUND_COLOR

    @ColorInt
    private var borderColor = DEFAULT_BORDER_COLOR

    private var borderWidth = DEFAULT_BORDER_WIDTH

    @ColorInt
    private var labelsColor = DEFAULT_LABELS_COLOR
    private var labelsTextSize = DEFAULT_LABELS_TEXT_SIZE

    var items: List<IRoundSelectorItem> = emptyList()
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                invalidate()
            }
        }

    var currIndex = 0
        set(value) {
            assert(value >= 0 && value < items.size)
            field = value
            if (items.isNotEmpty()) {
                invalidate()
            }
        }

    var isLooped = false
        set(value) {
            field = value
            invalidate()
        }

    var showLabels = true
        set(value) {
            field = value
            invalidate()
        }

    init {
        context?.withStyledAttributes(attrs, R.styleable.RoundSelector) {
            fillColor = getColor(R.styleable.RoundSelector_rc_backgroundColor, DEFAULT_FILL_COLOR)
            foregroundColor =
                getColor(R.styleable.RoundSelector_rc_foregroundColor, DEFAULT_FOREGROUND_COLOR)
            borderColor = getColor(R.styleable.RoundSelector_rc_borderColor, DEFAULT_BORDER_COLOR)
            borderWidth =
                getDimension(R.styleable.RoundSelector_rc_borderWidth, DEFAULT_BORDER_WIDTH)
            isLooped = getBoolean(R.styleable.RoundSelector_rc_isLooped, false)
            showLabels = getBoolean(R.styleable.RoundSelector_rc_showLabels, true)
            labelsColor = getColor(R.styleable.RoundSelector_rc_labelsColor, DEFAULT_LABELS_COLOR)
            labelsTextSize = getFloat(R.styleable.RoundSelector_rc_labelsTextSize, DEFAULT_LABELS_TEXT_SIZE)
        }

        setup()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2 * 0.95f
        innerRadius = radius / 6
        cp.x = w / 2f
        cp.y = h / 2f

        rect.set(cp.x - radius, cp.y - radius, cp.x + radius, cp.y + radius)
        innerRect.set(
            cp.x - innerRadius,
            cp.y - innerRadius,
            cp.x + innerRadius,
            cp.y + innerRadius
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val initSize = resolveDefaultSize(min(widthMeasureSpec, heightMeasureSpec))
        setMeasuredDimension(initSize, initSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(cp.x, cp.y, radius, foregroundPaint)
        canvas.drawCircle(cp.x, cp.y, radius * 0.9f, backgroundPaint)
        canvas.drawArc(innerRect, 178f, 184f, false, foregroundPaint)
        canvas.drawArc(rect, 0f, 180f, false, foregroundPaint)

        if (items.isNotEmpty()) {
            drawItems(canvas)
        }

        drawBorder(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.x < width / 2)
            prev()
        else
            next()
        return super.onTouchEvent(event)
    }

    fun next() {
        if (currIndex + 1 < items.size) {
            currIndex++
            invalidate()
        } else if (isLooped) {
            currIndex = 0
            invalidate()
        }
    }

    fun prev() {
        if (currIndex - 1 >= 0) {
            currIndex--
            invalidate()
        } else if (isLooped) {
            currIndex = items.lastIndex
            invalidate()
        }
    }

    private fun drawItems(canvas: Canvas) {
        //center (current item)
        prepareBitmap(currIndex)
        canvas.drawBitmap(
            bitmap, cp.x - bitmap.width / 2,
            cp.y - radius * 3 / 5 - bitmap.height / 2, foregroundPaint
        )
        if (showLabels)
            canvas.drawText(
                items[currIndex].getLabel(), cp.x,
                cp.y - radius * 2 / 5, textPaint
            )

        //left
        if (currIndex - 1 >= 0)
            drawLeft(canvas, currIndex - 1)
        else if (isLooped)
            drawLeft(canvas, items.lastIndex)

        //right
        if (currIndex + 1 < items.size)
            drawRight(canvas, currIndex + 1)
        else if (isLooped)
            drawRight(canvas, 0)
    }

    private fun drawLeft(canvas: Canvas, index: Int) {
        prepareBitmap(index)
        prepareBitmap(currIndex - 1)
        bitmap.rotate(-60f, cp.x, cp.y)
        ltp.calculateXY(180f + 30f, radius * 3 / 5)
        canvas.drawBitmap(
            bitmap, ltp.x - bitmap.width / 2,
            ltp.y - bitmap.height / 2, foregroundPaint
        )
        if (showLabels) {
            canvas.rotate(-60f, cp.x, cp.y)
            canvas.drawText(
                items[currIndex].getLabel(), cp.x,
                cp.y - radius * 2 / 5, textPaint
            )
            canvas.rotate(60f, cp.x, cp.y)
        }
    }

    private fun drawRight(canvas: Canvas, index: Int) {
        prepareBitmap(index)
        bitmap.rotate(60f, cp.x, cp.y)
        ltp.calculateXY(360f - 30f, radius * 3 / 5)
        canvas.drawBitmap(
            bitmap, ltp.x - bitmap.width / 2,
            ltp.y - bitmap.height / 2, foregroundPaint
        )
        if (showLabels) {
            canvas.rotate(60f, cp.x, cp.y)
            canvas.drawText(
                items[currIndex].getLabel(), cp.x,
                cp.y - radius * 2 / 5, textPaint
            )
            canvas.rotate(-60f, cp.x, cp.y)
        }
    }

    private fun drawBorder(canvas: Canvas) {
        canvas.drawCircle(cp.x, cp.y, radius, borderPaint)
        canvas.drawLine(cp.x - radius * 0.9f, cp.y, cp.x - innerRadius, cp.y, borderPaint)
        canvas.drawArc(innerRect, 180f, 180f, false, borderPaint)
        canvas.drawLine(cp.x + innerRadius, cp.y, cp.x + radius * 0.9f, cp.y, borderPaint)

        with(rect) {
            left = cp.x - radius * 0.9f
            right = cp.x + radius * 0.9f
            top = cp.y - radius * 0.9f
            bottom = cp.y + radius * 0.9f
        }
        canvas.drawArc(rect, 180f, 180f, false, borderPaint)
        rect.set(cp.x - radius, cp.y - radius, cp.x + radius, cp.y + radius)

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

        with(textPaint) {
            textSize = labelsTextSize
            color = labelsColor
            style = Paint.Style.STROKE
            textAlign = Paint.Align.CENTER
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

    private fun Bitmap.rotate(degrees: Float, px: Float, py: Float) {
        bitmapMatrix.reset()
        bitmapMatrix.setRotate(degrees, px, py)
        bitmap = Bitmap.createBitmap(this, 0, 0, width, height, bitmapMatrix, true)
    }

    private fun prepareBitmap(index: Int) {
        bitmap =
            AppCompatResources.getDrawable(context, items[index].getDrawable())?.toBitmap()
                ?: AppCompatResources.getDrawable(context, R.drawable.ic_android)!!
                    .toBitmap()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.currIndex = currIndex
        savedState.isLooped = isLooped
        savedState.showLabels = showLabels
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state is SavedState) {
            currIndex = state.currIndex
            isLooped = state.isLooped
            showLabels = state.showLabels
        }
    }

    private class SavedState : BaseSavedState, Parcelable {
        var currIndex = 0
        var isLooped = false
        var showLabels = true

        constructor(superState: Parcelable?) : super(superState)
        constructor(src: Parcel) : super(src) {
            currIndex = src.readInt()
            isLooped = src.readInt() == 1
            showLabels = src.readInt() == 1
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeInt(currIndex)
            parcel.writeInt(if (isLooped) 1 else 0)
            parcel.writeInt(if (showLabels) 1 else 0)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}