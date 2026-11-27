package com.xixikitchen.jetpack.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.noRipplePressable
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

enum class CropAspectRatio(val code: String, val label: String, val ratio: Float) {
    RATIO_4_3("4:3", "4:3 横幅", 4f / 3f),
    RATIO_1_1("1:1", "1:1 方图", 1f),
    RATIO_3_4("3:4", "3:4 竖版", 3f / 4f),
    RATIO_16_9("16:9", "16:9 电影", 16f / 9f);

    companion object {
        fun fromCode(code: String?): CropAspectRatio =
            entries.firstOrNull { it.code == code } ?: RATIO_4_3
    }
}

fun decodeBitmapWithExif(context: Context, uri: Uri, maxDim: Int = 2048): Bitmap? {
    return runCatching {
        val resolver = context.contentResolver
        val rawBytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > maxDim || bounds.outHeight / sampleSize > maxDim) {
            sampleSize *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOpts) ?: return null

        val orientation = runCatching {
            resolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return@runCatching decoded
        }
        Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    }.getOrNull()
}

fun compressBitmapToJpegBytes(bitmap: Bitmap, targetBytes: Int = 800 * 1024): ByteArray {
    var working = bitmap
    val maxEdge = 1600
    if (working.width > maxEdge || working.height > maxEdge) {
        val scale = maxEdge.toFloat() / maxOf(working.width, working.height).toFloat()
        working = Bitmap.createScaledBitmap(
            working,
            (working.width * scale).roundToInt().coerceAtLeast(1),
            (working.height * scale).roundToInt().coerceAtLeast(1),
            true
        )
    }

    var quality = 88
    var result: ByteArray
    while (true) {
        val out = ByteArrayOutputStream()
        working.compress(Bitmap.CompressFormat.JPEG, quality, out)
        result = out.toByteArray()
        if (result.size <= targetBytes) break
        quality -= 12
        if (quality < 36) {
            val nextW = (working.width * 0.75f).roundToInt()
            val nextH = (working.height * 0.75f).roundToInt()
            if (nextW < 240 || nextH < 240) break
            working = Bitmap.createScaledBitmap(working, nextW, nextH, true)
            quality = 82
        }
    }
    return result
}

@Composable
fun ImageCropperDialog(
    sourceBitmap: Bitmap,
    initialRatio: CropAspectRatio = CropAspectRatio.RATIO_4_3,
    onDismiss: () -> Unit,
    onCropConfirmed: (croppedBitmap: Bitmap, chosenRatio: CropAspectRatio) -> Unit
) {
    var workingBitmap by remember(sourceBitmap) { mutableStateOf(sourceBitmap) }
    var chosenRatio by remember(sourceBitmap) { mutableStateOf(initialRatio) }
    var zoom by remember(workingBitmap, chosenRatio) { mutableFloatStateOf(1f) }
    var offsetX by remember(workingBitmap, chosenRatio) { mutableFloatStateOf(0f) }
    var offsetY by remember(workingBitmap, chosenRatio) { mutableFloatStateOf(0f) }

    val imageBitmap = remember(workingBitmap) { workingBitmap.asImageBitmap() }
    val density = LocalDensity.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121214))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "取消裁切",
                        tint = Color.White
                    )
                }
                Text(
                    text = "裁切票根画面",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(
                    onClick = {
                        val matrix = Matrix().apply { postRotate(90f) }
                        workingBitmap = Bitmap.createBitmap(
                            workingBitmap,
                            0,
                            0,
                            workingBitmap.width,
                            workingBitmap.height,
                            matrix,
                            true
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = "旋转90度",
                        tint = Color.White
                    )
                }
            }

            var latestCropCalc by remember { mutableStateOf<(() -> Bitmap)?>(null) }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val canvasW = constraints.maxWidth.toFloat()
                val canvasH = constraints.maxHeight.toFloat()
                val padPx = with(density) { 28.dp.toPx() }
                val maxW = (canvasW - padPx * 2f).coerceAtLeast(120f)
                val maxH = (canvasH - padPx * 2f).coerceAtLeast(120f)

                val ratio = chosenRatio.ratio
                val cropW: Float
                val cropH: Float
                if (maxW / maxH > ratio) {
                    cropH = maxH
                    cropW = maxH * ratio
                } else {
                    cropW = maxW
                    cropH = maxW / ratio
                }

                val bmpW = workingBitmap.width.toFloat()
                val bmpH = workingBitmap.height.toFloat()
                val baseScale = max(cropW / bmpW, cropH / bmpH)
                val totalScale = baseScale * zoom
                val maxOffX = ((bmpW * totalScale - cropW) / 2f).coerceAtLeast(0f)
                val maxOffY = ((bmpH * totalScale - cropH) / 2f).coerceAtLeast(0f)
                val clampedX = offsetX.coerceIn(-maxOffX, maxOffX)
                val clampedY = offsetY.coerceIn(-maxOffY, maxOffY)

                latestCropCalc = {
                    val drawW = bmpW * totalScale
                    val drawH = bmpH * totalScale
                    val imgLeft = canvasW / 2f + clampedX - drawW / 2f
                    val imgTop = canvasH / 2f + clampedY - drawH / 2f
                    val cropLeft = (canvasW - cropW) / 2f
                    val cropTop = (canvasH - cropH) / 2f

                    val srcX = ((cropLeft - imgLeft) / totalScale).roundToInt()
                        .coerceIn(0, workingBitmap.width - 1)
                    val srcY = ((cropTop - imgTop) / totalScale).roundToInt()
                        .coerceIn(0, workingBitmap.height - 1)
                    val srcW = (cropW / totalScale).roundToInt()
                        .coerceIn(1, workingBitmap.width - srcX)
                    val srcH = (cropH / totalScale).roundToInt()
                        .coerceIn(1, workingBitmap.height - srcY)

                    Bitmap.createBitmap(workingBitmap, srcX, srcY, srcW, srcH)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(workingBitmap, chosenRatio, cropW, cropH) {
                            detectTransformGestures { _, pan, gestureZoom, _ ->
                                val nextZoom = (zoom * gestureZoom).coerceIn(1f, 4f)
                                val nextScale = baseScale * nextZoom
                                val nextMaxX = ((bmpW * nextScale - cropW) / 2f).coerceAtLeast(0f)
                                val nextMaxY = ((bmpH * nextScale - cropH) / 2f).coerceAtLeast(0f)
                                zoom = nextZoom
                                offsetX = (offsetX + pan.x).coerceIn(-nextMaxX, nextMaxX)
                                offsetY = (offsetY + pan.y).coerceIn(-nextMaxY, nextMaxY)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "待裁切图片",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .width(with(density) { (bmpW * baseScale).toDp() })
                            .height(with(density) { (bmpH * baseScale).toDp() })
                            .graphicsLayer {
                                scaleX = zoom
                                scaleY = zoom
                                translationX = clampedX
                                translationY = clampedY
                            }
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val left = (size.width - cropW) / 2f
                        val top = (size.height - cropH) / 2f
                        val cropRect = Rect(left, top, left + cropW, top + cropH)

                        val maskPath = Path().apply {
                            fillType = PathFillType.EvenOdd
                            addRect(Rect(0f, 0f, size.width, size.height))
                            addRect(cropRect)
                        }
                        drawPath(maskPath, color = Color.Black.copy(alpha = 0.62f))

                        // Crop border
                        drawRect(
                            color = Color.White.copy(alpha = 0.85f),
                            topLeft = Offset(left, top),
                            size = androidx.compose.ui.geometry.Size(cropW, cropH),
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // Rule of thirds grid
                        val gridColor = Color.White.copy(alpha = 0.28f)
                        val strokeThin = 0.8.dp.toPx()
                        for (i in 1..2) {
                            val gx = left + cropW * (i / 3f)
                            drawLine(gridColor, Offset(gx, top), Offset(gx, top + cropH), strokeThin)
                            val gy = top + cropH * (i / 3f)
                            drawLine(gridColor, Offset(left, gy), Offset(left + cropW, gy), strokeThin)
                        }

                        // Corner handles
                        val cornerLen = 18.dp.toPx()
                        val cornerW = 3.dp.toPx()
                        val right = left + cropW
                        val bottom = top + cropH
                        drawLine(Color.White, Offset(left, top), Offset(left + cornerLen, top), cornerW)
                        drawLine(Color.White, Offset(left, top), Offset(left, top + cornerLen), cornerW)
                        drawLine(Color.White, Offset(right - cornerLen, top), Offset(right, top), cornerW)
                        drawLine(Color.White, Offset(right, top), Offset(right, top + cornerLen), cornerW)
                        drawLine(Color.White, Offset(left, bottom), Offset(left + cornerLen, bottom), cornerW)
                        drawLine(Color.White, Offset(left, bottom - cornerLen), Offset(left, bottom), cornerW)
                        drawLine(Color.White, Offset(right - cornerLen, bottom), Offset(right, bottom), cornerW)
                        drawLine(Color.White, Offset(right, bottom - cornerLen), Offset(right, bottom), cornerW)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1C1C20))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CropAspectRatio.entries) { item ->
                        val active = item == chosenRatio
                        Box(
                            modifier = Modifier
                                .noRipplePressable(pressedScale = 0.95f) { chosenRatio = item }
                                .background(
                                    color = if (active) GlassAccent.primary else Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.label,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val cropped = latestCropCalc?.invoke() ?: workingBitmap
                        onCropConfirmed(cropped, chosenRatio)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("确认裁切", fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }
    }
}
