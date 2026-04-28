package com.sathix.app.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun compressImage(uri: Uri, maxDim: Int = 1280, quality: Int = 80): File =
        withContext(Dispatchers.IO) {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            val sample = calcSample(bounds.outWidth, bounds.outHeight, maxDim)
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val bmp = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            } ?: error("Decode failed")

            val out = File.createTempFile("img_", ".jpg", context.cacheDir)
            FileOutputStream(out).use { os ->
                bmp.compress(Bitmap.CompressFormat.JPEG, quality, os)
            }
            bmp.recycle()
            out
        }

    private fun calcSample(w: Int, h: Int, max: Int): Int {
        var sample = 1
        var halfW = w / 2; var halfH = h / 2
        while (halfW / sample >= max && halfH / sample >= max) sample *= 2
        return sample.coerceAtLeast(1)
    }
}
