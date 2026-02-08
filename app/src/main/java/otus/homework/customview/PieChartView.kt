package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnSectorClickListener {
        fun onSectorClick(category: String)
    }

    private var onSectorClickListener: OnSectorClickListener? = null

    fun setOnSectorClickListener(listener: OnSectorClickListener) {
        this.onSectorClickListener = listener
    }

    private var categoriesData = mutableListOf<CategoryData>()
    private val colors = arrayOf(
        Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN,
        Color.MAGENTA, Color.GRAY, Color.parseColor("#FFA500"), // Orange
        Color.parseColor("#800080"), // Purple
        Color.parseColor("#008080"), // Teal
        Color.parseColor("#FF1493"), // Deep Pink
        Color.parseColor("#00CED1") // Dark Turquoise
    )

    private var totalAmount = 0.0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var centerText = ""

    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var selectedSector = -1

    init {
        setupPaints()
    }

    private fun setupPaints() {
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 2f

        textPaint.color = Color.WHITE
        textPaint.textSize = 36f
        textPaint.textAlign = Paint.Align.CENTER

        selectedPaint.style = Paint.Style.FILL
        selectedPaint.color = Color.parseColor("#CCCCCC")
        selectedPaint.strokeWidth = 3f

        centerPaint.color = Color.WHITE
        centerPaint.style = Paint.Style.FILL
    }

    fun setData(categories: List<CategoryData>) {
        categoriesData.clear()
        categoriesData.addAll(categories)
        totalAmount = categoriesData.sumOf { it.amount }
        updateCenterText()
        invalidate()
        requestLayout()
    }

    private fun updateCenterText() {
        if (categoriesData.isNotEmpty()) {
            val topCategory = categoriesData.maxByOrNull { it.amount }
            centerText = topCategory?.category ?: ""
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minSize = 400

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(widthSize, minSize)
            else -> minSize
        }

        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(heightSize, minSize)
            else -> minSize
        }

        val size = minOf(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        centerX = w / 2f
        centerY = h / 2f
        radius = minOf(w, h) * 0.4f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (categoriesData.isEmpty() || totalAmount == 0.0) {
            drawEmptyState(canvas)
            return
        }

        var startAngle = 0f

        for ((index, category) in categoriesData.withIndex()) {
            val sweepAngle = (category.amount / totalAmount * 360).toFloat()

            paint.color = category.color

            if (index == selectedSector) {
                val offsetRadius = radius * 1.1f
                val offsetX =
                    (cos(Math.toRadians((startAngle + sweepAngle / 2).toDouble())) * offsetRadius * 0.1).toFloat()
                val offsetY =
                    (sin(Math.toRadians((startAngle + sweepAngle / 2).toDouble())) * offsetRadius * 0.1).toFloat()

                canvas.drawArc(
                    centerX - offsetRadius + offsetX,
                    centerY - offsetRadius + offsetY,
                    centerX + offsetRadius + offsetX,
                    centerY + offsetRadius + offsetY,
                    startAngle,
                    sweepAngle,
                    true,
                    paint
                )
            } else {
                canvas.drawArc(
                    centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius,
                    startAngle,
                    sweepAngle,
                    true,
                    paint
                )
            }

            if (sweepAngle > 15) {
                val angle = startAngle + sweepAngle / 2
                val textRadius = radius * 0.7f
                val x = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * textRadius
                val y = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * textRadius

                canvas.save()
                canvas.rotate(angle, x, y)
                canvas.drawText(
                    category.category,
                    x,
                    y,
                    textPaint
                )
                canvas.restore()
            }

            startAngle += sweepAngle
        }

        canvas.drawCircle(centerX, centerY, radius * 0.3f, centerPaint)

        canvas.drawText(
            centerText,
            centerX,
            centerY + textPaint.textSize / 3,
            textPaint.apply { color = Color.BLACK }
        )
    }

    private fun drawEmptyState(canvas: Canvas) {
        paint.color = Color.LTGRAY
        canvas.drawCircle(centerX, centerY, radius, paint)

        textPaint.color = Color.DKGRAY
        canvas.drawText(
            "Нет данных",
            centerX,
            centerY,
            textPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                val distance = sqrt((x - centerX).pow(2) + (y - centerY).pow(2))

                if (distance <= radius) {
                    var angle = (Math.toDegrees(
                        atan2(
                            (y - centerY).toDouble(),
                            (x - centerX).toDouble()
                        )
                    ) + 360) % 360

                    angle = (angle + 90) % 360

                    var startAngle = 0f
                    for ((index, category) in categoriesData.withIndex()) {
                        val sweepAngle = (category.amount / totalAmount * 360).toFloat()

                        if (angle >= startAngle && angle < startAngle + sweepAngle) {
                            selectedSector = index
                            onSectorClickListener?.onSectorClick(category.category)
                            invalidate()
                            return true
                        }
                        startAngle += sweepAngle
                    }
                }
                selectedSector = -1
                invalidate()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState).apply {
            this.selectedSector = this@PieChartView.selectedSector
            this.centerText = this@PieChartView.centerText
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? SavedState
        if (savedState != null) {
            super.onRestoreInstanceState(savedState.superState)
            selectedSector = savedState.selectedSector
            centerText = savedState.centerText
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private class SavedState : BaseSavedState {
        var selectedSector: Int = -1
        var centerText: String = ""

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            selectedSector = parcel.readInt()
            centerText = parcel.readString() ?: ""
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(selectedSector)
            out.writeString(centerText)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}