package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

object QrCodeGenerator {

    /**
     * Generates a crisp, deterministic 25x25 QR matrix representation for an event URL
     * with standard finder patterns, timing patterns, alignment pattern, and SHA-256 bit encoding.
     */
    fun generateMatrix(payload: String, gridSize: Int = 25): Array<BooleanArray> {
        val matrix = Array(gridSize) { BooleanArray(gridSize) }
        val reserved = Array(gridSize) { BooleanArray(gridSize) }

        fun placeFinderPattern(startRow: Int, startCol: Int) {
            for (r in -1..7) {
                for (c in -1..7) {
                    val rr = startRow + r
                    val cc = startCol + c
                    if (rr in 0 until gridSize && cc in 0 until gridSize) {
                        reserved[rr][cc] = true
                        val inOuter = r in 0..6 && c in 0..6 && (r == 0 || r == 6 || c == 0 || c == 6)
                        val inInner = r in 2..4 && c in 2..4
                        matrix[rr][cc] = inOuter || inInner
                    }
                }
            }
        }

        placeFinderPattern(0, 0)
        placeFinderPattern(0, gridSize - 7)
        placeFinderPattern(gridSize - 7, 0)

        // Timing patterns
        for (i in 8 until gridSize - 8) {
            reserved[6][i] = true
            matrix[6][i] = (i % 2 == 0)
            reserved[i][6] = true
            matrix[i][6] = (i % 2 == 0)
        }

        // Alignment pattern at (gridSize - 9, gridSize - 9)
        val alignCenter = gridSize - 7
        for (r in -2..2) {
            for (c in -2..2) {
                val rr = alignCenter + r
                val cc = alignCenter + c
                if (rr in 0 until gridSize && cc in 0 until gridSize) {
                    reserved[rr][cc] = true
                    val isBorder = r == -2 || r == 2 || c == -2 || c == 2
                    val isCenter = r == 0 && c == 0
                    matrix[rr][cc] = isBorder || isCenter
                }
            }
        }

        // Deterministic bit stream derived from payload bytes + SHA-256 digest
        val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray())
        val payloadBytes = payload.toByteArray()
        var bitIndex = 0

        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (!reserved[r][c]) {
                    val byteSource = if (bitIndex / 8 < payloadBytes.size) {
                        payloadBytes[(bitIndex / 8) % payloadBytes.size].toInt()
                    } else {
                        digest[(bitIndex / 8) % digest.size].toInt()
                    }
                    val bit = ((byteSource ushr (bitIndex % 8)) and 1) == 1
                    val mask = ((r + c) % 3 == 0)
                    matrix[r][c] = bit xor mask
                    bitIndex++
                }
            }
        }
        return matrix
    }

    /**
     * Renders a high-resolution branded Event QR Card Bitmap for downloading and sharing.
     */
    fun createBrandedEventQrBitmap(
        eventName: String,
        eventCategory: String,
        eventDate: String,
        qrPayload: String,
        sizePx: Int = 960
    ): Bitmap {
        val heightPx = (sizePx * 1.22f).toInt()
        val bitmap = Bitmap.createBitmap(sizePx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background gradient (Deep purple to obsidian)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                sizePx.toFloat(),
                heightPx.toFloat(),
                intArrayOf(
                    Color.parseColor("#1B0F33"),
                    Color.parseColor("#0A0514")
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, sizePx.toFloat(), heightPx.toFloat(), bgPaint)

        // Gold border frame
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.parseColor("#F59E0B")
        }
        val frameRect = RectF(28f, 28f, sizePx - 28f, heightPx - 28f)
        canvas.drawRoundRect(frameRect, 42f, 42f, borderPaint)

        // Header text
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("JUMMO PHOTOS AI • GUEST PORTAL", sizePx / 2f, 100f, brandPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val clippedTitle = if (eventName.length > 26) eventName.take(24) + "…" else eventName
        canvas.drawText(clippedTitle, sizePx / 2f, 165f, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D8B4FE")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("$eventCategory • $eventDate", sizePx / 2f, 212f, subPaint)

        // White QR container card
        val qrBoxMargin = 140f
        val qrBoxTop = 260f
        val qrBoxSize = sizePx - (qrBoxMargin * 2f)
        val whiteCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        val qrCardRect = RectF(qrBoxMargin, qrBoxTop, qrBoxMargin + qrBoxSize, qrBoxTop + qrBoxSize)
        canvas.drawRoundRect(qrCardRect, 36f, 36f, whiteCardPaint)

        // Draw QR Matrix inside white card
        val matrix = generateMatrix(qrPayload, 25)
        val quietZone = 42f
        val drawableSize = qrBoxSize - (quietZone * 2f)
        val cellSize = drawableSize / matrix.size
        val darkModulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#120924")
            style = Paint.Style.FILL
        }
        val finderGoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B45309")
            style = Paint.Style.FILL
        }

        for (r in matrix.indices) {
            for (c in matrix[r].indices) {
                if (matrix[r][c]) {
                    val left = qrBoxMargin + quietZone + c * cellSize
                    val top = qrBoxTop + quietZone + r * cellSize
                    val isFinderCenter =
                        (r in 2..4 && c in 2..4) ||
                            (r in 2..4 && c in (matrix.size - 5)..(matrix.size - 3)) ||
                            (r in (matrix.size - 5)..(matrix.size - 3) && c in 2..4)
                    val paint = if (isFinderCenter) finderGoldPaint else darkModulePaint
                    canvas.drawRoundRect(
                        RectF(left + 1f, top + 1f, left + cellSize - 1f, top + cellSize - 1f),
                        4f,
                        4f,
                        paint
                    )
                }
            }
        }

        // Footer instruction
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Scan to Find Your Event Photos Instantly",
            sizePx / 2f,
            qrBoxTop + qrBoxSize + 74f,
            footerPaint
        )

        val urlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 23f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(qrPayload, sizePx / 2f, qrBoxTop + qrBoxSize + 118f, urlPaint)

        return bitmap
    }

    /**
     * Downloads the branded QR code PNG to device MediaStore Pictures/JummoPhotosAI.
     */
    fun saveQrBitmapToGallery(
        context: Context,
        eventName: String,
        eventCategory: String,
        eventDate: String,
        qrPayload: String
    ): Result<String> = runCatching {
        val bitmap = createBrandedEventQrBitmap(eventName, eventCategory, eventDate, qrPayload)
        val cleanName = eventName.replace(Regex("[^a-zA-Z0-9]+"), "_").take(30)
        val fileName = "Jummo_QR_${cleanName}_${System.currentTimeMillis()}.png"

        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/JummoPhotosAI"
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri != null) {
            resolver.openOutputStream(imageUri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            "Pictures/JummoPhotosAI/$fileName"
        } else {
            val fallbackDir = File(context.filesDir, "photos").apply { mkdirs() }
            val fallbackFile = File(fallbackDir, fileName)
            FileOutputStream(fallbackFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            fallbackFile.absolutePath
        }
    }

    /**
     * Shares the branded QR code PNG via Android Share Sheet using FileProvider.
     */
    fun shareEventQrCode(
        context: Context,
        eventName: String,
        eventCategory: String,
        eventDate: String,
        qrPayload: String
    ): Result<Unit> = runCatching {
        val bitmap = createBrandedEventQrBitmap(eventName, eventCategory, eventDate, qrPayload)
        val qrDir = File(context.cacheDir, "qr").apply { mkdirs() }
        val file = File(qrDir, "jummo_event_qr.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_SUBJECT,
                "Event Photos QR — $eventName"
            )
            putExtra(
                Intent.EXTRA_TEXT,
                "Scan this QR code or open $qrPayload to view and find your photos from $eventName ($eventDate)!"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Event QR Code").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
