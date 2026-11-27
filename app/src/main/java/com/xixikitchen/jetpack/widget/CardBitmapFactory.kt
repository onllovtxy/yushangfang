package com.xixikitchen.jetpack.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import java.io.File

/**
 * 用一张位图绘制整个卡片。
 *
 * 选位图而不是 RemoteViews 布局树，是为了让小组件在任意尺寸（2x2 / 4x2 / 4x4…）
 * 下都能等比排布，不依赖预置的几套固定布局；桌面缩放时系统会重新回调 onAppWidgetOptionsChanged，
 * 我们按实际宽高重画即可。
 */
object CardBitmapFactory {

    fun create(
        context: Context,
        type: WidgetType,
        card: WidgetCard,
        widthPx: Int? = null,
        heightPx: Int? = null
    ): Bitmap {
        val w = (widthPx ?: WidgetSize.fallbackWidth(context)).coerceIn(180, 1400)
        val h = (heightPx ?: WidgetSize.fallbackHeight(context)).coerceIn(180, 1400)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val density = context.resources.displayMetrics.density
        val pad = 14f * density

        // 卡片底：暖白圆角 + 顶部主题色细条
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFCFAF5.toInt() }
        val radius = 20f * density
        canvas.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), radius, radius, bg)

        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFD97724.toInt() }
        val stripe = Path().apply {
            addRoundRect(
                RectF(0f, 0f, w.toFloat(), 6f * density),
                floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f),
                Path.Direction.CW
            )
        }
        canvas.drawPath(stripe, accent)

        // 有照片时上半部分铺图，下半部分留白写字
        var textTop = pad
        val imageFile = WidgetImageCache.file(context, type, card.imageUrl)
        if (imageFile != null && imageFile.exists() && h > 150 * density) {
            val bmpImg = BitmapFactory.decodeFile(imageFile.absolutePath)
            if (bmpImg != null) {
                val imgH = h * 0.52f
                val rect = RectF(pad, pad, w - pad, imgH)
                canvas.save()
                val clip = Path().apply {
                    addRoundRect(rect, 14f * density, 14f * density, Path.Direction.CW)
                }
                canvas.clipPath(clip)
                canvas.drawBitmap(
                    bmpImg,
                    null,
                    fitCenter(rect, bmpImg.width, bmpImg.height),
                    Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
                )
                canvas.restore()
                textTop = imgH + pad * 0.7f
                bmpImg.recycle()
            }
        }

        val availH = h - textTop - pad
        val badgePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFB35D15.toInt()
            textSize = 11f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF2C2824.toInt()
            textSize = 16f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF8A8078.toInt()
            textSize = 12f * density
        }

        var y = textTop + badgePaint.textSize + 4f * density
        if (card.badge.isNotBlank()) {
            // 徽章背景
            val badgeW = badgePaint.measureText(card.badge) + 16f * density
            val badgeH = badgePaint.textSize + 8f * density
            val badgeBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x1AD97724 }
            canvas.drawRoundRect(
                RectF(pad, textTop, pad + badgeW, textTop + badgeH),
                badgeH / 2f, badgeH / 2f, badgeBg
            )
            canvas.drawText(card.badge, pad + 8f * density, y, badgePaint)
            y += badgeH + 8f * density - 4f * density
        }

        y += titlePaint.textSize
        y = drawEllipsized(canvas, card.title, titlePaint, pad, y, w - pad)
        if (availH > 90f * density || imageFile == null) {
            // 小尺寸下只保留标题，副标题放不下
            y += subPaint.textSize + 6f * density
            drawEllipsized(canvas, card.subtitle, subPaint, pad, y, w - pad)
        }
        if (card.date.isNotBlank() && y + subPaint.textSize + 6f * density < h - pad) {
            y += subPaint.textSize + 4f * density
            canvas.drawText(card.date, pad, y, subPaint)
        }

        return bmp
    }

    private fun drawEllipsized(
        canvas: Canvas,
        text: String,
        paint: TextPaint,
        left: Float,
        baseline: Float,
        maxRight: Float
    ): Float {
        if (text.isBlank()) return baseline
        val avail = maxRight - left
        var out = text
        if (paint.measureText(out) > avail) {
            var cut = out.length
            while (cut > 1 && paint.measureText(out.substring(0, cut) + "…") > avail) cut--
            out = out.substring(0, cut) + "…"
        }
        canvas.drawText(out, left, baseline, paint)
        return baseline
    }

    private fun fitCenter(rect: RectF, bw: Int, bh: Int): RectF {
        val scale = maxOf(rect.width() / bw, rect.height() / bh)
        val dw = bw * scale
        val dh = bh * scale
        val dx = rect.left - (dw - rect.width()) / 2f
        val dy = rect.top - (dh - rect.height()) / 2f
        return RectF(dx, dy, dx + dw, dy + dh)
    }
}

/** 尺寸兜底：系统没给 options 时按 2x2（约 110dp）画。 */
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
}
