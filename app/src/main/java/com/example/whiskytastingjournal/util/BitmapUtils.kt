package com.example.whiskytastingjournal.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * Scales a JPEG photo down to [maxDimension] on the longest side and overwrites the file.
 * Uses a two-pass decode (inJustDecodeBounds + inSampleSize) to keep memory usage low.
 */
fun scaleDownPhoto(file: File, maxDimension: Int = 1024) {
    if (!file.exists() || file.length() == 0L) return

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)

    val opts = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
    }
    val bmp = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return

    val scaled = if (maxOf(bmp.width, bmp.height) > maxDimension) {
        val ratio = maxDimension.toFloat() / maxOf(bmp.width, bmp.height)
        Bitmap.createScaledBitmap(
            bmp,
            (bmp.width * ratio).toInt(),
            (bmp.height * ratio).toInt(),
            true
        )
    } else bmp

    file.outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, 85, it) }
    if (scaled !== bmp) scaled.recycle()
    bmp.recycle()
}

private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
    var sample = 1
    while (maxOf(width / sample, height / sample) > maxDimension * 2) {
        sample *= 2
    }
    return sample
}
