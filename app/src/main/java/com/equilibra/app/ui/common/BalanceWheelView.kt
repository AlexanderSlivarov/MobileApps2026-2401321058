package com.equilibra.app.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.domain.BalanceWheel
import com.google.android.material.color.MaterialColors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import com.google.android.material.R as MaterialR

/**
 * Draws the balance wheel as a radar chart: one spoke per sector, rings for the scores 1-5,
 * and my scores as a filled shape. Drawn with plain Canvas calls, no chart library.
 */
class BalanceWheelView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : View(context, attrs) {
        private var current: Map<WheelSector, Int>? = null

        private val density = resources.displayMetrics.density
        private val primary = MaterialColors.getColor(this, MaterialR.attr.colorPrimary)
        private val sectorColors = WheelSector.entries.map { ContextCompat.getColor(context, it.colorRes) }

        // Paints are created once here, not in onDraw, because onDraw runs on every frame.
        private val gridPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1 * density
                color = MaterialColors.getColor(this@BalanceWheelView, MaterialR.attr.colorOutlineVariant)
            }
        private val spokePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2 * density
            }
        private val fillPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = ColorUtils.setAlphaComponent(primary, 70)
            }
        private val outlinePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3 * density
                strokeJoin = Paint.Join.ROUND
                color = primary
            }
        private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val path = Path()

        /** [current] is the filled shape (null = empty wheel). */
        fun setScores(current: Map<WheelSector, Int>?) {
            this.current = current
            invalidate() // Ask Android to call onDraw again.
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val centerX = width / 2f
            val centerY = height / 2f
            val radius = min(width, height) / 2f - 12 * density
            val sectors = WheelSector.entries

            // Rings for the scores 1 to 5.
            for (score in BalanceWheel.MIN_SCORE..BalanceWheel.MAX_SCORE) {
                buildShape(centerX, centerY, radius) { score }
                canvas.drawPath(path, gridPaint)
            }

            // One colored spoke per sector.
            sectors.forEachIndexed { index, _ ->
                spokePaint.color = sectorColors[index]
                canvas.drawLine(
                    centerX,
                    centerY,
                    pointX(centerX, radius, index),
                    pointY(centerY, radius, index),
                    spokePaint,
                )
            }

            // My rating as a filled shape with a dot on each spoke.
            current?.let { scores ->
                buildShape(centerX, centerY, radius) { scores[it] ?: BalanceWheel.DEFAULT_SCORE }
                canvas.drawPath(path, fillPaint)
                canvas.drawPath(path, outlinePaint)
                sectors.forEachIndexed { index, sector ->
                    val r = radius * (scores[sector] ?: BalanceWheel.DEFAULT_SCORE) / BalanceWheel.MAX_SCORE
                    dotPaint.color = sectorColors[index]
                    canvas.drawCircle(pointX(centerX, r, index), pointY(centerY, r, index), 5 * density, dotPaint)
                }
            }
        }

        /** Fills [path] with a closed shape that has, on each spoke, the point for that sector's score. */
        private fun buildShape(
            centerX: Float,
            centerY: Float,
            radius: Float,
            scoreOf: (WheelSector) -> Int,
        ) {
            path.reset()
            WheelSector.entries.forEachIndexed { index, sector ->
                val r = radius * scoreOf(sector) / BalanceWheel.MAX_SCORE
                val x = pointX(centerX, r, index)
                val y = pointY(centerY, r, index)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
        }

        // The first spoke points straight up, the others follow clockwise at equal angles.
        private fun angle(index: Int): Double = Math.toRadians(-90.0 + index * 360.0 / WheelSector.entries.size)

        private fun pointX(
            centerX: Float,
            r: Float,
            index: Int,
        ): Float = centerX + r * cos(angle(index)).toFloat()

        private fun pointY(
            centerY: Float,
            r: Float,
            index: Int,
        ): Float = centerY + r * sin(angle(index)).toFloat()
    }
