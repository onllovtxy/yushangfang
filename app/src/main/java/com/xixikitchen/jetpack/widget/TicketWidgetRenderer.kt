package com.xixikitchen.jetpack.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint

/**
 * 票根小组件：按票根模板等比还原造型（不是截图，是按同样的结构和配色重绘）。
 *
 * 宽度铺满 widget，高度按模板比例算，居中绘制，四周留 transparent，
 * 所以 widget 本身的尺寸可以是任意形状，票根始终等比。
 */
object TicketWidgetRenderer {

    fun render(
        context: Context,
        card: WidgetCard,
        width: Int,
        height: Int,
        density: Float
    ): Bitmap? {
        // 票根基本比例是 4:3 横向（和模板一致），这里按 widget 宽度铺满
        val ratio = 4f / 3f
        var ticketW = width.toFloat()
        var ticketH = ticketW / ratio
        if (ticketH > height) {
            ticketH = height.toFloat()
            ticketW = ticketH * ratio
        }
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val left = (width - ticketW) / 2f
        val top = (height - ticketH) / 2f
        val body = RectF(left, top, left + ticketW, top + ticketH)

        drawBoardingPass(context, canvas, body, card, density)
        return bmp
    }

    private fun drawBoardingPass(
        context: Context,
        canvas: Canvas,
        body: RectF,
        card: WidgetCard,
        density: Float
    ) {
        val radius = 16f * density
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(body, radius, radius, cardPaint)

        val headerH = body.height() * 0.16f
        val headerRect = RectF(body.left, body.top, body.right, body.top + headerH)
        canvas.save()
        canvas.clipPath(Path().apply { addRoundRect(body, radius, radius, Path.Direction.CW) })
        canvas.drawRect(headerRect, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1D5C8B.toInt() })
        canvas.restore()

        // 航司名
        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = headerH * 0.44f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        canvas.drawText(
            "GOURMET BOARDING PASS",
            body.left + headerH * 0.5f,
            body.top + headerH * 0.68f,
            headerPaint
        )
        val tailPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFBAE6FD.toInt()
            textSize = headerH * 0.42f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        canvas.drawText(
            "XX-520",
            body.right - headerH * 1.6f,
            body.top + headerH * 0.68f,
            tailPaint
        )

        // 照片区（左侧 55%，下方）
        val pad = body.width() * 0.035f
        val photoRect = RectF(
            body.left + pad,
            body.top + headerH + pad,
            body.left + body.width() * 0.56f,
            body.bottom - pad
        )
        val photoClip = Path().apply {
            addRoundRect(photoRect, 10f * density, 10f * density, Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(photoClip)
        val bitmap = WidgetImageCache.load(context, WidgetType.Diary, card.imageUrl)
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, null, fitCenter(photoRect, bitmap.width, bitmap.height), Paint(Paint.ANTI_ALIAS_FLAG))
            bitmap.recycle()
        } else {
            canvas.drawRect(photoRect, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFECEAE4.toInt() })
        }
        canvas.restore()

        // 右侧信息区
        val infoLeft = photoRect.right + pad * 1.4f
        val infoRight = body.right - pad
        var y = body.top + headerH + pad + body.height() * 0.10f

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF1E293B.toInt()
            textSize = body.height() * 0.13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(ellipsize(card.title, titlePaint, infoRight - infoLeft), infoLeft, y, titlePaint)
        y += titlePaint.textSize * 0.9f

        val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF64748B.toInt()
            textSize = body.height() * 0.085f
        }
        canvas.drawText(ellipsize(card.location, subPaint, infoRight - infoLeft), infoLeft, y, subPaint)
        y += subPaint.textSize * 1.25f

        if (card.dateLabel.isNotBlank()) {
            canvas.drawText(card.dateLabel, infoLeft, y, subPaint)
            y += subPaint.textSize * 1.25f
        }

        // 心情标签
        if (card.moodTag.isNotBlank()) {
            val tagPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFB35D15.toInt()
                textSize = body.height() * 0.082f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val tagW = tagPaint.measureText(card.moodTag) + body.width() * 0.045f
            val tagH = tagPaint.textSize * 1.75f
            canvas.drawRoundRect(
                RectF(infoLeft, y, infoLeft + tagW, y + tagH),
                tagH / 2f, tagH / 2f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x1AD97724 }
            )
            canvas.drawText(card.moodTag, infoLeft + body.width() * 0.022f, y + tagH * 0.68f, tagPaint)
        }

        // 底部票号
        val noPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF94A3B8.toInt()
            textSize = body.height() * 0.07f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        canvas.drawText(card.ticketNo, infoLeft, body.bottom - pad * 1.2f, noPaint)
    }

    private fun fitCenter(rect: RectF, bw: Int, bh: Int): RectF {
        val scale = maxOf(rect.width() / bw, rect.height() / bh)
        val dw = bw * scale
        val dh = bh * scale
        val dx = rect.left - (dw - rect.width()) / 2f
        val dy = rect.top - (dh - rect.height()) / 2f
        return RectF(dx, dy, dx + dw, dy + dh)
    }

    private fun ellipsize(text: String, paint: TextPaint, maxWidth: Float): String {
        if (text.isBlank()) return ""
        if (paint.measureText(text) <= maxWidth) return text
        var cut = text.length
        while (cut > 1 && paint.measureText(text.substring(0, cut) + "…") > maxWidth) cut--
        return text.substring(0, cut) + "…"
    }
}
