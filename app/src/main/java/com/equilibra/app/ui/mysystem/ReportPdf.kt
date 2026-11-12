package com.equilibra.app.ui.mysystem

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.equilibra.app.R
import com.equilibra.app.ui.common.toDisplayString
import java.io.OutputStream
import java.time.LocalDate

/**
 * Draws the report (BalanceReport) as text on white A4 pages with Android's own
 * PdfDocument class: no library and no permission. Long text wraps (StaticLayout), a block
 * that doesn't fit goes to the next page, and every page gets its number at the bottom.
 */
class ReportPdf(
    context: Context,
) {
    private val day = BalanceReport.dayContext(context)
    private val regular = ResourcesCompat.getFont(context, R.font.nunito_regular) ?: Typeface.DEFAULT
    private val bold = ResourcesCompat.getFont(context, R.font.nunito_bold) ?: Typeface.DEFAULT_BOLD
    private val ink = ContextCompat.getColor(day, R.color.md_on_surface)
    private val muted = ContextCompat.getColor(day, R.color.md_on_surface_variant)
    private val primary = ContextCompat.getColor(day, R.color.md_primary)
    private val line = ContextCompat.getColor(day, R.color.md_outline_variant)
    private val title = context.getString(R.string.report_title)
    private val made = context.getString(R.string.report_made_format, LocalDate.now().toDisplayString())

    private lateinit var document: PdfDocument
    private var page: PdfDocument.Page? = null
    private var pageNumber = 0
    private var y = 0f

    fun write(
        blocks: List<ReportBlock>,
        output: OutputStream,
    ) {
        document = PdfDocument()
        try {
            newPage()
            drawTitle()
            blocks.forEachIndexed { index, block ->
                // A heading stays with the block after it, so it never ends a page alone.
                val next = blocks.getOrNull(index + 1)
                val needed = height(block) + if (block is ReportBlock.Heading && next != null) height(next) else 0f
                if (y + needed > PAGE_HEIGHT - MARGIN) newPage()
                draw(block)
            }
            finishPage()
            document.writeTo(output)
        } finally {
            document.close()
        }
    }

    private fun canvas(): Canvas = page!!.canvas

    private fun newPage() {
        finishPage()
        pageNumber++
        page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        y = MARGIN
    }

    /** Puts the page number at the bottom and closes the page. */
    private fun finishPage() {
        val current = page ?: return
        val paint = paint(regular, SMALL, muted).apply { textAlign = Paint.Align.CENTER }
        current.canvas.drawText("$title · $pageNumber", PAGE_WIDTH / 2f, PAGE_HEIGHT - MARGIN / 2, paint)
        document.finishPage(current)
        page = null
    }

    private fun drawTitle() {
        y += drawText("Equilibra", paint(bold, SMALL, primary), 0f)
        y += drawText(title, paint(bold, TITLE, ink), 0f)
        y += drawText(made, paint(regular, BODY, muted), 0f) + GAP
        canvas().drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, Paint().apply { color = line })
        y += GAP
    }

    private fun draw(block: ReportBlock) {
        when (block) {
            is ReportBlock.Heading -> {
                y += GAP
                y += drawText(block.text.uppercase(), paint(bold, HEADING, primary), 0f)
                canvas().drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, Paint().apply { color = line })
                y += GAP / 2
            }
            is ReportBlock.Subheading -> {
                y += GAP / 2
                y += drawText(block.text, paint(bold, BODY, block.color ?: ink), 0f)
            }
            is ReportBlock.Line ->
                y += drawText(block.text, lineDrawPaint(block), block.indent * INDENT)
            is ReportBlock.Row -> {
                val labelHeight = layout(block.label, paint(regular, BODY, muted), LABEL_WIDTH).height
                val valueHeight = layout(block.value, paint(bold, BODY, ink), TEXT_WIDTH - LABEL_WIDTH).height
                drawLayout(layout(block.label, paint(regular, BODY, muted), LABEL_WIDTH), 0f)
                drawLayout(layout(block.value, paint(bold, BODY, ink), TEXT_WIDTH - LABEL_WIDTH), LABEL_WIDTH)
                y += maxOf(labelHeight, valueHeight) + ROW_GAP
            }
        }
    }

    /** How tall a block will be, so it can move to the next page in one piece. */
    private fun height(block: ReportBlock): Float =
        when (block) {
            is ReportBlock.Heading -> GAP * 1.5f + layout(block.text.uppercase(), paint(bold, HEADING, primary), TEXT_WIDTH).height
            is ReportBlock.Subheading -> GAP / 2 + layout(block.text, paint(bold, BODY, ink), TEXT_WIDTH).height
            is ReportBlock.Line -> layout(block.text, lineDrawPaint(block), TEXT_WIDTH - block.indent * INDENT).height.toFloat()
            is ReportBlock.Row ->
                maxOf(
                    layout(block.label, paint(regular, BODY, muted), LABEL_WIDTH).height,
                    layout(block.value, paint(bold, BODY, ink), TEXT_WIDTH - LABEL_WIDTH).height,
                ) + ROW_GAP
        }

    private fun lineDrawPaint(block: ReportBlock.Line): TextPaint =
        paint(regular, BODY, block.color ?: if (block.italic) muted else ink).apply { textSkewX = if (block.italic) ITALIC_SKEW else 0f }

    /** Draws wrapped text at the current height, [left] points from the margin; returns its height. */
    private fun drawText(
        text: String,
        paint: TextPaint,
        left: Float,
    ): Float {
        val layout = layout(text, paint, (TEXT_WIDTH - left).toInt())
        drawLayout(layout, left)
        return layout.height.toFloat()
    }

    private fun drawLayout(
        layout: StaticLayout,
        left: Float,
    ) {
        val canvas = canvas()
        canvas.save()
        canvas.translate(MARGIN + left, y)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun layout(
        text: String,
        paint: TextPaint,
        width: Int,
    ): StaticLayout =
        StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(LINE_EXTRA, 1f)
            .build()

    private fun layout(
        text: String,
        paint: TextPaint,
        width: Float,
    ): StaticLayout = layout(text, paint, width.toInt())

    private fun paint(
        typeface: Typeface,
        size: Float,
        color: Int,
    ): TextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = size
            this.color = color
        }

    private companion object {
        /** A4 in PDF points (1/72 inch). */
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val MARGIN = 48f
        const val TEXT_WIDTH = PAGE_WIDTH - 2 * MARGIN
        const val LABEL_WIDTH = 170f
        const val INDENT = 14f
        const val GAP = 12f
        const val ROW_GAP = 3f
        const val LINE_EXTRA = 2f
        const val TITLE = 22f
        const val HEADING = 12f
        const val BODY = 10.5f
        const val SMALL = 8.5f
        const val ITALIC_SKEW = -0.2f
    }
}
