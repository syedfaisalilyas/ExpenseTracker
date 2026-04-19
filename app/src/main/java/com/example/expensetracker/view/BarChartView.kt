package com.example.expensetracker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#99744A")
    }
    private val overBudgetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C0392B")
    }
    private val targetLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#414A37")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(12f, 8f), 0f)
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CCCCCC")
        strokeWidth = 1.5f
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5A5A5A")
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#414A37")
        textSize = 24f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private var values: FloatArray = FloatArray(7) { 0f }
    private var labels: Array<String> = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private var weeklyTarget: Float = 0f
    private var dailyTarget: Float = 0f

    fun setData(dailyValues: FloatArray, dayLabels: Array<String>, target: Float) {
        values = dailyValues
        labels = dayLabels
        weeklyTarget = target
        dailyTarget = if (target > 0) target / 7f else 0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val paddingLeft = 60f
        val paddingRight = 16f
        val paddingTop = 32f
        val paddingBottom = 48f

        val chartW = w - paddingLeft - paddingRight
        val chartH = h - paddingTop - paddingBottom

        // Y-axis
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, h - paddingBottom, axisPaint)
        // X-axis
        canvas.drawLine(paddingLeft, h - paddingBottom, w - paddingRight, h - paddingBottom, axisPaint)

        val maxVal = max(values.maxOrNull() ?: 0f, dailyTarget * 1.1f).let {
            if (it == 0f) 1000f else it
        }

        val barCount = values.size
        val totalBarWidth = chartW / barCount
        val barWidth = totalBarWidth * 0.55f
        val barSpacing = totalBarWidth * 0.45f

        // Draw Y labels
        val ySteps = 4
        for (i in 0..ySteps) {
            val value = maxVal * i / ySteps
            val y = h - paddingBottom - (chartH * i / ySteps)
            val yLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#AAAAAA")
                textSize = 22f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(formatShort(value), paddingLeft - 6f, y + 8f, yLabelPaint)
            canvas.drawLine(paddingLeft, y, w - paddingRight, y, Paint.apply {  }.let {
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#EEEEEE")
                    strokeWidth = 1f
                }
            })
        }

        // Draw target line
        if (dailyTarget > 0) {
            val targetY = h - paddingBottom - (chartH * dailyTarget / maxVal)
            canvas.drawLine(paddingLeft, targetY, w - paddingRight, targetY, targetLinePaint)
        }

        // Draw bars
        for (i in 0 until barCount) {
            val barH = if (maxVal > 0) chartH * values[i] / maxVal else 0f
            val left = paddingLeft + i * totalBarWidth + barSpacing / 2
            val right = left + barWidth
            val top = h - paddingBottom - barH
            val bottom = h - paddingBottom

            val paint = if (dailyTarget > 0 && values[i] > dailyTarget) overBudgetPaint else barPaint

            if (barH > 0) {
                val rect = RectF(left, top, right, bottom)
                canvas.drawRoundRect(rect, 8f, 8f, paint)

                // Value label above bar
                if (values[i] > 0) {
                    canvas.drawText(
                        formatShort(values[i]),
                        left + barWidth / 2,
                        top - 6f,
                        valuePaint
                    )
                }
            }

            // Day label below bar
            canvas.drawText(labels[i], left + barWidth / 2, h - paddingBottom + 32f, labelPaint)
        }
    }

    private fun formatShort(value: Float): String {
        return when {
            value >= 1000 -> "${(value / 1000).toInt()}k"
            else -> value.toInt().toString()
        }
    }
}
