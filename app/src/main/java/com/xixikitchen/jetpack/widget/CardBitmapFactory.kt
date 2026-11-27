package com.xixikitchen.jetpack.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import java.io.File

/**
 * 桌面小组件的绘制。
 *
 * 只有两种：纪念日（大数字倒计时）和票根（等比还原票根造型）。
 * 统一画成位图，这样在任意 widget 尺寸下都能按实际宽高重新排版。
 */
object CardBitmapFactory {

    private const val ACCENT = 0xFFD97724.toInt()
    private const val ACCENT_DARK = 0xFFB35D15.toInt()
    private const val INK = 0xFF2C2824.toInt()
    private const val MUTED = 0xFF8A8078.toInt()

    fun create(
        context: Context,
        type: WidgetType,
        card: WidgetCard,
        widthPx: Int? = null,
        heightPx: Int? = null
    ): Bitmap {
        val w = (widthPx ?: WidgetSize.fallbackWidth(context)).coerceIn(160, 1400)
        val h = (heightPx ?: WidgetSize.fallbackHeight(context)).coerceIn(160, 1400)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(0x00000000)
        when (type) {
            WidgetType.Anniversary -> drawAnniversary(context, canvas, card, w, h)
            WidgetType.Diary -> drawTicket(context, canvas, card, w, h)
        }
        return bmp
    }

    // ───────────────────────── 纪念日：大数字 ─────────────────────────

    private fun drawAnniversary(context: Context, canvas: Canvas, card: WidgetCard, w: Int, h: Int) {
        val density = context.resources.displayMetrics.density
        val radius = 20f * density
        canvas.drawRoundRect(
            RectF(0f, 0f, w.toFloat(), h.toFloat()),
            radius, radius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFCFAF5.toInt() }
        )
        // 顶部主题色细条
        canvas.drawPath(
            Path().apply {
                addRoundRect(
                    RectF(0f, 0f, w.toFloat(), 6f * density),
                    floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f),
                    Path.Direction.CW
                )
            },
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ACCENT }
        )

        val pad = 14f * density
        val available = w - pad * 2

        // 标题（如有）
        var y = pad + 6f * density
        if (card.title.isNotBlank()) {
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = INK
                textSize = 14f * density
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            y += titlePaint.textSize
            drawEllipsized(canvas, card.title, titlePaint, pad, y, w - pad)
            y += 8f * density
        }

        // 主要大字：数字 + 单位；小数在上方
        val prefixPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = MUTED
            textSize = 13f * density
        }
        val bigNumber = card.bigNumber.ifBlank { "0" }
        val unit = card.unit
        val numberPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ACCENT_DARK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val unitPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ACCENT_DARK
            textSize = 15f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // 让数字占满剩余高度的约 60%
        val remaining = h - y - pad
        numberPaint.textSize = (remaining * 0.62f).coerceIn(28f * density, 120f * density)

        val numberWidth = numberPaint.measureText(bigNumber)
        val unitWidth = unitPaint.measureText(unit)
        val gap = 4f * density
        val totalWidth = numberWidth + unitWidth + gap
        val startX = pad + (available - totalWidth).coerceAtLeast(0f) / 2f

        val baseline = y + numberPaint.textSize * 0.86f
        if (card.prefix.isNotBlank()) {
            canvas.drawText(card.prefix, startX, y + prefixPaint.textSize, prefixPaint)
            canvas.drawText(bigNumber, startX, y + prefixPaint.textSize + 6f * density + numberPaint.textSize * 0.86f, numberPaint)
            canvas.drawText(
                unit,
                startX + numberWidth + gap,
                y + prefixPaint.textSize + 6f * density + numberPaint.textSize * 0.86f - (numberPaint.textSize - unitPaint.textSize) * 0.15f,
                unitPaint
            )
        } else {
            canvas.drawText(bigNumber, startX, baseline, numberPaint)
            canvas.drawText(unit, startX + numberWidth + gap, baseline, unitPaint)
        }
    }

    // ───────────────────────── 票根：等比还原 ─────────────────────────

    private fun drawTicket(context: Context, canvas: Canvas, card: WidgetCard, w: Int, h: Int) {
        val density = context.resources.displayMetrics.density
        val tempBitmap = TicketWidgetRenderer.render(
            context = context,
            card = card,
            width = w,
            height = h,
            density = density
        )
        if (tempBitmap != null) {
            canvas.drawBitmap(tempBitmap, 0f, 0f, null)
            tempBitmap.recycle()
        }
    }

    private fun drawEllipsized(
        canvas: Canvas,
        text: String,
        paint: TextPaint,
        left: Float,
        baseline: Float,
        maxRight: Float
    ) {
        if (text.isBlank()) return
        var out = text
        if (paint.measureText(out) > maxRight - left) {
            var cut = out.length
            while (cut > 1 && paint.measureText(out.substring(0, cut) + "…") > maxRight - left) cut--
            out = out.substring(0, cut) + "…"
        }
        canvas.drawText(out, left, baseline, paint)
    }
}

/** 尺寸兜底：系统没给 options 时按 2x2（约 155dp）画。 */
object WidgetSize {
    fun fallbackWidth(context: Context): Int =
        (155f * context.resources.displayMetrics.density).toInt()

    fun fallbackHeight(context: Context): Int =
        (155f * context.resources.displayMetrics.density).toInt()
}

/** 把要展示的封面图提前缓存到文件，widget 刷新时不必联网。 */
object WidgetImageCache {
    fun file(context: Context, type: WidgetType, url: String?): File? {
        if (url.isNullOrBlank()) return null
        val dir = File(context.filesDir, "widget_images").apply { mkdirs() }
        val name = "cover_" + type.key + "_" + url.hashCode() + ".png"
        return File(dir, name)
    }

    fun load(context: Context, type: WidgetType, url: String?): Bitmap? {
        val f = file(context, type, url) ?: return null
        if (!f.exists()) return null
        return runCatching { BitmapFactory.decodeFile(f.absolutePath) }.getOrNull()
    }
}
