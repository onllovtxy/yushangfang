package com.xixikitchen.jetpack.ui.components

import android.content.ContentValues
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
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextPaint
import com.xixikitchen.jetpack.data.CoupleMeetup
import com.xixikitchen.jetpack.data.FoodDiary
import java.io.File
import java.io.FileOutputStream

/**
 * 把卡片导出成图片保存到相册。
 *
 * 用 Canvas 直接按卡片的版式重绘，而不是给 Compose 页面截图：
 * 截图依赖窗口和首帧，在后台/弹窗关闭后经常拿到空白图；
 * 重绘版式稳定、可指定任意分辨率，且能复用同样的配色与排版。
 */
object CardImageExporter {

    private const val ACCENT = 0xFFD97724.toInt()
    private const val ACCENT_DARK = 0xFFB35D15.toInt()
    private const val INK = 0xFF2C2824.toInt()
    private const val MUTED = 0xFF8A8078.toInt()
    private const val PAPER = 0xFFFCFAF5.toInt()

    // ─────────────────────── 见面日记卡片 ───────────────────────

    fun renderMeetup(
        context: Context,
        meetup: CoupleMeetup,
        photos: List<Bitmap>
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val width = 1080
        val photoAreaHeight = if (photos.isEmpty()) 0f else photos.size * 380f * density
        val headerHeight = 620f * density
        val height = (headerHeight + photoAreaHeight + 40f * density).toInt()
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(PAPER)

        val pad = 44f * density
        var y = pad + 20f * density

        // 序号圆圈 + 标题
        val circleR = 26f * density
        canvas.drawCircle(
            pad + circleR, y + circleR, circleR,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ACCENT }
        )
        val idxPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val idx = meetup.meetIndex.toString()
        canvas.drawText(
            idx,
            pad + circleR - idxPaint.measureText(idx) / 2f,
            y + circleR + idxPaint.textSize * 0.35f,
            idxPaint
        )
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = INK
            textSize = 42f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(
            "第 ${meetup.meetIndex} 次见面",
            pad + circleR * 2 + 18f * density,
            y + circleR + titlePaint.textSize * 0.35f,
            titlePaint
        )
        y += circleR * 2 + 40f * density

        // 日期行
        val bigDatePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = INK
            textSize = 52f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val arrive = meetup.arriveDate.replace('-', '.')
        val leave = meetup.leaveDate.replace('-', '.')
        canvas.drawText(arrive, pad, y + bigDatePaint.textSize, bigDatePaint)
        val leaveWidth = bigDatePaint.measureText(leave)
        canvas.drawText(leave, width - pad - leaveWidth, y + bigDatePaint.textSize, bigDatePaint)

        // 中间：相伴天数
        val pillPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ACCENT_DARK
            textSize = 26f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val pillText = "相伴 ${meetup.stayDays} 天"
        val pillW = pillPaint.measureText(pillText) + 36f * density
        val pillH = 52f * density
        val pillLeft = (width - pillW) / 2f
        canvas.drawRoundRect(
            RectF(pillLeft, y + 6f * density, pillLeft + pillW, y + 6f * density + pillH),
            pillH / 2f, pillH / 2f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x1AD97724 }
        )
        canvas.drawText(
            pillText,
            pillLeft + 18f * density,
            y + 6f * density + pillH * 0.68f,
            pillPaint
        )
        y += bigDatePaint.textSize + 50f * density

        // 城市
        if (meetup.city.isNotBlank()) {
            val cityPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ACCENT_DARK
                textSize = 30f * density
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("📍 ${meetup.city}", pad, y + cityPaint.textSize, cityPaint)
            y += cityPaint.textSize + 26f * density
        }

        // 文字小记
        if (meetup.note.isNotBlank()) {
            val notePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = INK
                textSize = 32f * density
            }
            y = drawWrapped(canvas, meetup.note, notePaint, pad, y + notePaint.textSize, width - pad)
            y += 26f * density
        }

        // 分割线
        canvas.drawLine(
            pad, y, width - pad, y,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x33D97724
                strokeWidth = 2f * density
            }
        )
        y += 24f * density

        // 照片
        if (photos.isNotEmpty()) {
            val photoW = (width - pad * 2 - 16f * density * (photos.size - 1)) / photos.size
            val photoH = 380f * density
            photos.forEachIndexed { i, photo ->
                val left = pad + i * (photoW + 16f * density)
                val rect = RectF(left, y, left + photoW, y + photoH)
                canvas.save()
                canvas.clipPath(
                    Path().apply {
                        addRoundRect(rect, 20f * density, 20f * density, Path.Direction.CW)
                    }
                )
                canvas.drawBitmap(
                    photo, null,
                    fitCenter(rect, photo.width, photo.height),
                    Paint(Paint.ANTI_ALIAS_FLAG)
                )
                canvas.restore()
            }
        }

        return bmp
    }

    // ─────────────────────── 票根卡片 ───────────────────────

    fun renderDiary(
        context: Context,
        diary: FoodDiary,
        photo: Bitmap?
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val width = 1080
        val height = 810
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)

        val headerH = 130f * density
        canvas.drawRect(
            0f, 0f, width.toFloat(), headerH,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1D5C8B.toInt() }
        )
        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 34f * density
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        canvas.drawText("GOURMET BOARDING PASS", 44f * density, headerH * 0.66f, headerPaint)

        val pad = 44f * density
        val photoRect = RectF(pad, headerH + pad, width * 0.56f, height - pad)
        canvas.save()
        canvas.clipPath(
            Path().apply {
                addRoundRect(photoRect, 20f * density, 20f * density, Path.Direction.CW)
            }
        )
        if (photo != null) {
            canvas.drawBitmap(
                photo, null,
                fitCenter(photoRect, photo.width, photo.height),
                Paint(Paint.ANTI_ALIAS_FLAG)
            )
        } else {
            canvas.drawRect(photoRect, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFECEAE4.toInt() })
        }
        canvas.restore()

        val infoLeft = photoRect.right + pad
        var y = headerH + pad + 40f * density
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF1E293B.toInt()
            textSize = 56f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(
            diary.title.ifBlank { "今日份美味" },
            infoLeft, y + titlePaint.textSize, titlePaint
        )
        y += titlePaint.textSize + 40f * density

        val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF64748B.toInt()
            textSize = 32f * density
        }
        canvas.drawText(diary.location.ifBlank { "熙熙的小厨房" }, infoLeft, y + subPaint.textSize, subPaint)
        y += subPaint.textSize + 30f * density
        canvas.drawText(
            diary.createdAt?.take(10)?.replace('-', '.') ?: "",
            infoLeft, y + subPaint.textSize, subPaint
        )
        y += subPaint.textSize + 40f * density

        val tagPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ACCENT_DARK
            textSize = 30f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val tag = diary.moodTag.ifBlank { "超满足" }
        val tagW = tagPaint.measureText(tag) + 40f * density
        val tagH = 56f * density
        canvas.drawRoundRect(
            RectF(infoLeft, y, infoLeft + tagW, y + tagH),
            tagH / 2f, tagH / 2f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x1AD97724 }
        )
        canvas.drawText(tag, infoLeft + 20f * density, y + tagH * 0.68f, tagPaint)

        val noPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF94A3B8.toInt()
            textSize = 26f * density
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        canvas.drawText(
            diary.ticketNo.ifBlank { "NO.2026-0520" },
            infoLeft, height - pad,
            noPaint
        )

        return bmp
    }

    // ─────────────────────── 保存到相册 ───────────────────────

    fun saveToGallery(context: Context, bitmap: Bitmap, displayName: String): Boolean {
        return runCatching {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/御膳房"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return false
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            } ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            true
        }.getOrDefault(false)
    }

    /** API 24~28 需要写外部存储权限；29+ 走 MediaStore 不需要。 */
    fun needsLegacyStoragePermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    private fun fitCenter(rect: RectF, bw: Int, bh: Int): RectF {
        val scale = maxOf(rect.width() / bw, rect.height() / bh)
        val dw = bw * scale
        val dh = bh * scale
        val dx = rect.left - (dw - rect.width()) / 2f
        val dy = rect.top - (dh - rect.height()) / 2f
        return RectF(dx, dy, dx + dw, dy + dh)
    }

    private fun drawWrapped(
        canvas: Canvas,
        text: String,
        paint: TextPaint,
        left: Float,
        firstBaseline: Float,
        maxRight: Float
    ): Float {
        val maxWidth = maxRight - left
        val lines = mutableListOf<String>()
        var current = ""
        text.forEach { ch ->
            val candidate = current + ch
            if (paint.measureText(candidate) > maxWidth && current.isNotEmpty()) {
                lines.add(current)
                current = ch.toString()
            } else {
                current = candidate
            }
        }
        if (current.isNotEmpty()) lines.add(current)
        var baseline = firstBaseline
        val lineHeight = paint.textSize * 1.35f
        lines.take(6).forEach { line ->
            canvas.drawText(line, left, baseline, paint)
            baseline += lineHeight
        }
        return baseline - lineHeight + paint.textSize * 0.4f
    }
}
