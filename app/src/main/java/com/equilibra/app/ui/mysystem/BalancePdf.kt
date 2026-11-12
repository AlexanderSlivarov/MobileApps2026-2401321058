package com.equilibra.app.ui.mysystem

import android.graphics.pdf.PdfDocument
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import java.io.OutputStream

/**
 * Turns the Balance page into a PDF file: the page's view is drawn onto A4 pages with
 * Android's own PdfDocument class (no library, no permission). The view is scaled to the
 * page width; a new page starts at the top of a card, so cards are not cut in two
 * (only a card taller than a whole page is).
 */
object BalancePdf {
    /** A4 in PDF points (1/72 inch). */
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    /**
     * Draws the cards of [content] (already laid out on screen) up to [contentHeight] pixels
     * on a [background] color and writes the PDF to [output].
     */
    fun write(
        content: ViewGroup,
        contentHeight: Int,
        background: Int,
        output: OutputStream,
    ) {
        val scale = PAGE_WIDTH.toFloat() / content.width
        val sliceHeight = (PAGE_HEIGHT / scale).toInt()
        val starts = pageStarts(content.children.toList(), contentHeight, sliceHeight)

        val document = PdfDocument()
        try {
            starts.forEachIndexed { index, start ->
                val end = starts.getOrElse(index + 1) { contentHeight }
                val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
                val page = document.startPage(info)
                val canvas = page.canvas
                canvas.drawColor(background)
                canvas.scale(scale, scale)
                // Show only this page's part of the view: move it up and cut off what comes next.
                canvas.clipRect(0, 0, content.width, end - start)
                canvas.translate(0f, -start.toFloat())
                content.draw(canvas)
                document.finishPage(page)
            }
            document.writeTo(output)
        } finally {
            document.close()
        }
    }

    /**
     * Where each page starts, in view pixels: a card that would not fit on the current page
     * moves to a new page. A card taller than a page is cut where the page is full.
     */
    private fun pageStarts(
        cards: List<View>,
        contentHeight: Int,
        sliceHeight: Int,
    ): List<Int> {
        val starts = mutableListOf(0)
        for (card in cards) {
            if (card.top >= contentHeight) break
            if (card.bottom - starts.last() > sliceHeight && card.top > starts.last()) starts.add(card.top)
            while (minOf(card.bottom, contentHeight) - starts.last() > sliceHeight) starts.add(starts.last() + sliceHeight)
        }
        return starts
    }
}
