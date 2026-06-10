package com.xixikitchen.jetpack.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream

data class ProcessedImage(val bytes: ByteArray, val extension: String)

fun readAndCompressImage(context: Context, uri: Uri): ProcessedImage? {
    val tag = "ImageReader"
    Log.d(tag, "Reading image for URI: $uri")
    try {
        val resolver = context.contentResolver
        val rawBytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        
        // Smell the original extension from raw bytes
        val originalExtension = getExtensionFromHeader(rawBytes, resolver, uri)
        
        // If it's <= 1MB, return raw bytes directly with original extension
        val maxOriginalSize = 1 * 1024 * 1024
        if (rawBytes.size <= maxOriginalSize) {
            Log.d(tag, "Original size (${rawBytes.size} bytes) is <= 1MB, uploading raw bytes.")
            return ProcessedImage(rawBytes, originalExtension)
        }
        
        Log.d(tag, "Original size (${rawBytes.size} bytes) exceeds 1MB, compressing to JPEG under 800KB...")
        
        // Decode bitmap dimensions
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, options)
        var width = options.outWidth
        var height = options.outHeight
        if (width <= 0 || height <= 0) {
            return ProcessedImage(rawBytes, originalExtension)
        }
        
        // Calculate sample size to prevent OOM
        var sampleSize = 1
        val maxDim = 2000
        while (width / sampleSize > maxDim || height / sampleSize > maxDim) {
            sampleSize *= 2
        }
        
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        var bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOptions) 
            ?: return ProcessedImage(rawBytes, originalExtension)
            
        // Correct EXIF orientation
        var orientation = ExifInterface.ORIENTATION_NORMAL
        try {
            resolver.openInputStream(uri)?.use { inputStream ->
                orientation = ExifInterface(inputStream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        } catch (ignored: Exception) {}
        
        if (orientation != ExifInterface.ORIENTATION_NORMAL) {
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) {
                bitmap.recycle()
                bitmap = rotated
            }
        }
        
        val targetSize = 800 * 1024 // 800KB
        var quality = 80
        var compressedBytes: ByteArray
        
        do {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedBytes = outputStream.toByteArray()
            Log.d(tag, "Compressed to JPEG with quality=$quality, size=${compressedBytes.size} bytes")
            
            if (compressedBytes.size <= targetSize) {
                break
            }
            quality -= 15
            if (quality < 30) {
                val scaledWidth = (bitmap.width * 0.75).toInt()
                val scaledHeight = (bitmap.height * 0.75).toInt()
                if (scaledWidth < 200 || scaledHeight < 200) {
                    break
                }
                val scaled = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
                if (scaled != bitmap) {
                    bitmap.recycle()
                    bitmap = scaled
                }
                quality = 80
            }
        } while (compressedBytes.size > targetSize)
        
        bitmap.recycle()
        // Since it's compressed to JPEG, the extension is always jpg
        return ProcessedImage(compressedBytes, "jpg")
    } catch (e: Exception) {
        Log.e(tag, "Error reading or compressing image: ${e.message}", e)
        return null
    }
}

private fun getExtensionFromHeader(header: ByteArray, resolver: android.content.ContentResolver, uri: Uri): String {
    val len = header.size
    return if (len >= 3 && (header[0].toInt() and 0xFF) == 0xFF && (header[1].toInt() and 0xFF) == 0xD8 && (header[2].toInt() and 0xFF) == 0xFF) {
        "jpg"
    } else if (len >= 8 && (header[0].toInt() and 0xFF) == 0x89 && header[1].toInt() == 0x50 && header[2].toInt() == 0x4E && header[3].toInt() == 0x47) {
        "png"
    } else if (len >= 6 && header[0].toInt() == 0x47 && header[1].toInt() == 0x49 && header[2].toInt() == 0x46 && header[3].toInt() == 0x38) {
        "gif"
    } else if (len >= 12 && header[0].toInt() == 0x52 && header[1].toInt() == 0x49 && header[2].toInt() == 0x46 && header[3].toInt() == 0x46
        && header[8].toInt() == 0x57 && header[9].toInt() == 0x45 && header[10].toInt() == 0x42 && header[11].toInt() == 0x50) {
        "webp"
    } else {
        when (resolver.getType(uri)?.lowercase()) {
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> "jpg"
        }
    }
}
