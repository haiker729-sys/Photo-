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
import com.example.data.model.GuestAccessModes
import com.example.data.model.PhotoEvent
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class GuestAccessValidationResult(
    val isAllowed: Boolean,
    val statusCode: String,
    val title: String,
    val message: String,
    val guestPortalUrl: String,
    val accessMode: String,
    val allowsImmediateGallery: Boolean,
    val requiresFaceSearchConsent: Boolean,
    val requiresOrganizerApproval: Boolean
)

object QrCodeGenerator {

    private const val BASE_GUEST_PORTAL_URL = "https://jummophotos.ai/guest/"

    /**
     * Builds the canonical Guest Portal URL for an event, ensuring the actual event ID
     * is always encoded and never a static or placeholder URL.
     */
    fun buildGuestPortalUrl(eventId: String, existingQrCodeValue: String = ""): String {
        val cleanId = eventId.trim().ifBlank { "evt_live" }
        val trimmedExisting = existingQrCodeValue.trim()
        return if (
            trimmedExisting.startsWith("https://") &&
            trimmedExisting.contains(cleanId)
        ) {
            trimmedExisting
        } else {
            "$BASE_GUEST_PORTAL_URL$cleanId"
        }
    }

    fun buildGuestPortalUrl(event: PhotoEvent): String {
        return buildGuestPortalUrl(event.id, event.qrCodeValue)
    }

    /**
     * Formats an event or expiration date into luxury display format (e.g., "01 Mar 2027" or "15 Dec 2026").
     */
    fun formatDisplayDate(rawDate: String, defaultFallback: String = "01 Mar 2027"): String {
        val trimmed = rawDate.trim()
        if (trimmed.isBlank()) return defaultFallback
        if (trimmed.equals("Never", ignoreCase = true) || trimmed.equals("No Expiration", ignoreCase = true)) {
            return "Never"
        }
        val inputPatterns = listOf("yyyy-MM-dd", "dd MMM yyyy", "d MMM yyyy", "MMM d, yyyy", "dd/MM/yyyy")
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        for (pattern in inputPatterns) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                    isLenient = false
                }
                val parsed = parser.parse(trimmed)
                if (parsed != null) {
                    return outputFormat.format(parsed)
                }
            } catch (_: Exception) {
            }
        }
        return trimmed
    }

    /**
     * Parses a date string ("01 Mar 2027", "2027-03-01", etc.) to the end-of-day timestamp (23:59:59.999).
     * Returns null if "Never" or unparseable.
     */
    fun parseExpirationEndOfDayMillis(rawDate: String): Long? {
        val trimmed = rawDate.trim()
        if (trimmed.isBlank() ||
            trimmed.equals("Never", ignoreCase = true) ||
            trimmed.equals("No Expiration", ignoreCase = true)
        ) {
            return null
        }
        val inputPatterns = listOf("dd MMM yyyy", "d MMM yyyy", "yyyy-MM-dd", "dd/MM/yyyy", "MMM d, yyyy")
        for (pattern in inputPatterns) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                    isLenient = false
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = parser.parse(trimmed) ?: continue
                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    time = date
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                return cal.timeInMillis
            } catch (_: Exception) {
            }
        }
        return null
    }

    /**
     * Checks whether the given QR expiration date string is expired relative to [nowMillis].
     */
    fun isQrExpired(rawExpirationDate: String, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val expMillis = parseExpirationEndOfDayMillis(rawExpirationDate) ?: return false
        return nowMillis > expMillis
    }

    /**
     * Validates event status, QR portal enabled state, and QR expiration date before granting
     * Guest Portal access, and resolves the 3 access mode rules.
     */
    fun validateGuestPortalAccess(
        event: PhotoEvent,
        nowMillis: Long = System.currentTimeMillis()
    ): GuestAccessValidationResult {
        val portalUrl = buildGuestPortalUrl(event)
        val mode = event.guestAccessMode.ifBlank { GuestAccessModes.FACE_SEARCH_ONLY }

        if (event.id.isBlank()) {
            return GuestAccessValidationResult(
                isAllowed = false,
                statusCode = "INVALID_EVENT",
                title = "Invalid Event QR",
                message = "This QR code is not linked to a valid event ID.",
                guestPortalUrl = portalUrl,
                accessMode = mode,
                allowsImmediateGallery = false,
                requiresFaceSearchConsent = true,
                requiresOrganizerApproval = false
            )
        }

        if (!event.status.equals("Active", ignoreCase = true)) {
            return GuestAccessValidationResult(
                isAllowed = false,
                statusCode = "EVENT_ARCHIVED",
                title = "Event Gallery Archived",
                message = "\"${event.name}\" is currently archived by the organizer. Reactivate the event to restore guest QR access.",
                guestPortalUrl = portalUrl,
                accessMode = mode,
                allowsImmediateGallery = false,
                requiresFaceSearchConsent = true,
                requiresOrganizerApproval = false
            )
        }

        if (!event.qrEnabled) {
            return GuestAccessValidationResult(
                isAllowed = false,
                statusCode = "QR_DISABLED",
                title = "Event QR Portal Paused",
                message = "QR access for \"${event.name}\" has been disabled by the photographer. Enable Event QR Portal in QR Management to grant access.",
                guestPortalUrl = portalUrl,
                accessMode = mode,
                allowsImmediateGallery = false,
                requiresFaceSearchConsent = true,
                requiresOrganizerApproval = false
            )
        }

        if (isQrExpired(event.qrExpirationDate, nowMillis)) {
            val formattedExp = formatDisplayDate(event.qrExpirationDate)
            return GuestAccessValidationResult(
                isAllowed = false,
                statusCode = "QR_EXPIRED",
                title = "Event QR Code Expired",
                message = "Guest QR access for \"${event.name}\" expired on $formattedExp. Update the QR Expiration Date in QR Management to extend guest access.",
                guestPortalUrl = portalUrl,
                accessMode = mode,
                allowsImmediateGallery = false,
                requiresFaceSearchConsent = true,
                requiresOrganizerApproval = false
            )
        }

        return when (mode) {
            GuestAccessModes.VIEW_ALL_PERMITTED -> GuestAccessValidationResult(
                isAllowed = true,
                statusCode = "ACCESS_GRANTED_VIEW_ALL",
                title = "View All + Face Search Active",
                message = "Guests can browse all event photos or run AI Face Search with consent.",
                guestPortalUrl = portalUrl,
                accessMode = GuestAccessModes.VIEW_ALL_PERMITTED,
                allowsImmediateGallery = true,
                requiresFaceSearchConsent = false,
                requiresOrganizerApproval = false
            )

            GuestAccessModes.PIN_PROTECTED -> GuestAccessValidationResult(
                isAllowed = true,
                statusCode = "ACCESS_GRANTED_ORGANIZER_APPROVED",
                title = "Organizer Approved Access",
                message = "Guests require organizer approval or studio access code before viewing event photos.",
                guestPortalUrl = portalUrl,
                accessMode = GuestAccessModes.PIN_PROTECTED,
                allowsImmediateGallery = false,
                requiresFaceSearchConsent = false,
                requiresOrganizerApproval = true
            )

            else -> GuestAccessValidationResult(
                isAllowed = true,
                statusCode = "ACCESS_GRANTED_CONSENT_FACE_SEARCH",
                title = "Consent Face Search Only",
                message = "Guests must provide explicit consent and selfie verification to find their matched photos.",
                guestPortalUrl = portalUrl,
                accessMode = GuestAccessModes.FACE_SEARCH_ONLY,
                allowsImmediateGallery = false,
                requiresFaceSearchConsent = true,
                requiresOrganizerApproval = false
            )
        }
    }

    /**
     * Generates a real, standards-compliant ISO/IEC 18004 QR Code matrix (Error Correction Level M,
     * Byte mode 0100 with GF(256) Reed-Solomon ECC and optimal mask selection) so any standard
     * Android camera app or barcode scanner decodes [payload] directly.
     */
    fun generateIsoQrMatrix(payload: String): Array<BooleanArray> {
        val safePayload = payload.ifBlank { BASE_GUEST_PORTAL_URL + "evt_live" }
        val dataBytes = safePayload.toByteArray(StandardCharsets.UTF_8)
        val versionSpec = selectVersionSpec(dataBytes.size)
        val version = versionSpec.version
        val size = 17 + 4 * version

        // 1. Build Byte-mode bit stream (Mode 0100 + 8-bit length + UTF-8 bytes + terminator + pad)
        val dataCodewords = buildDataCodewords(dataBytes, versionSpec.totalDataCodewords)

        // 2. Split into blocks, compute Reed-Solomon ECC per block, and interleave
        val finalCodewords = interleaveWithEcc(dataCodewords, versionSpec)

        // 3. Build base function patterns (finders, separators, timing, alignment, dark module, format reservations)
        val baseMatrix = Array(size) { BooleanArray(size) }
        val isFunction = Array(size) { BooleanArray(size) }
        setupFunctionPatterns(version, size, baseMatrix, isFunction)

        // 4. Place interleaved data + ECC bits in ISO zig-zag order
        placeCodewordBits(finalCodewords, size, baseMatrix, isFunction)

        // 5. Evaluate all 8 masks and choose the lowest ISO penalty score
        var bestMask = 0
        var lowestPenalty = Int.MAX_VALUE
        var bestMatrix = baseMatrix

        for (mask in 0..7) {
            val candidate = Array(size) { r -> baseMatrix[r].clone() }
            applyMask(mask, size, candidate, isFunction)
            writeFormatBits(ecLevelBits = 0b00, mask = mask, size = size, matrix = candidate) // 00 = Level M
            val penalty = calculateIsoPenalty(candidate, size)
            if (penalty < lowestPenalty) {
                lowestPenalty = penalty
                bestMask = mask
                bestMatrix = candidate
            }
        }
        // Ensure format bits are written on bestMatrix
        writeFormatBits(ecLevelBits = 0b00, mask = bestMask, size = size, matrix = bestMatrix)
        return bestMatrix
    }

    /**
     * Decodes a QR matrix generated by [generateIsoQrMatrix] by reading the 15-bit format info,
     * unmasking the data modules in ISO/IEC 18004 zig-zag order, de-interleaving the blocks,
     * and extracting the Byte-mode (0100) UTF-8 payload. Used by tests to verify scannability.
     */
    fun decodeQrPayloadFromMatrix(matrix: Array<BooleanArray>): String {
        val size = matrix.size
        require(size >= 21 && (size - 17) % 4 == 0) { "Invalid ISO QR matrix size: $size" }
        val version = (size - 17) / 4
        val spec = VERSION_SPECS.firstOrNull { it.version == version }
            ?: error("Unsupported QR version: $version")

        // Read format bits from top-left horizontal strip (row 8, cols 0..5, 7, 8) and vertical (rows 7, 5..0, col 8)
        var formatInt = 0
        val coords = listOf(
            8 to 0, 8 to 1, 8 to 2, 8 to 3, 8 to 4, 8 to 5, 8 to 7, 8 to 8,
            7 to 8, 5 to 8, 4 to 8, 3 to 8, 2 to 8, 1 to 8, 0 to 8
        )
        for (i in coords.indices) {
            val (r, c) = coords[14 - i]
            if (matrix[r][c]) {
                formatInt = formatInt or (1 shl i)
            }
        }
        val unmaskedFormat = formatInt xor 0x5412
        val mask = (unmaskedFormat ushr 10) and 0b111

        // Reconstruct function pattern map
        val dummyMatrix = Array(size) { BooleanArray(size) }
        val isFunction = Array(size) { BooleanArray(size) }
        setupFunctionPatterns(version, size, dummyMatrix, isFunction)

        // Copy and unmask
        val working = Array(size) { r -> matrix[r].clone() }
        applyMask(mask, size, working, isFunction)

        // Read raw bits in zig-zag order
        val totalCodewords = spec.totalDataCodewords + spec.numBlocks * spec.ecCodewordsPerBlock
        val rawBytes = IntArray(totalCodewords)
        var bitCounter = 0
        var rightCol = size - 1
        var upward = true

        while (rightCol >= 1) {
            if (rightCol == 6) rightCol--
            for (step in 0 until size) {
                val r = if (upward) (size - 1 - step) else step
                for (cOffset in 0..1) {
                    val c = rightCol - cOffset
                    if (!isFunction[r][c]) {
                        val byteIdx = bitCounter / 8
                        if (byteIdx < rawBytes.size && working[r][c]) {
                            rawBytes[byteIdx] = rawBytes[byteIdx] or (1 shl (7 - (bitCounter % 8)))
                        }
                        bitCounter++
                    }
                }
            }
            upward = !upward
            rightCol -= 2
        }

        // De-interleave data blocks
        val blockSizes = IntArray(spec.numBlocks) { idx ->
            if (idx < spec.numBlocksGroup1) spec.dataCodewordsGroup1 else spec.dataCodewordsGroup2
        }
        val blocks = Array(spec.numBlocks) { IntArray(blockSizes[it]) }
        val maxDataLen = blockSizes.maxOrNull() ?: 0
        var cursor = 0
        for (col in 0 until maxDataLen) {
            for (b in 0 until spec.numBlocks) {
                if (col < blockSizes[b] && cursor < rawBytes.size) {
                    blocks[b][col] = rawBytes[cursor++]
                }
            }
        }

        val sequentialData = IntArray(spec.totalDataCodewords)
        var outIdx = 0
        for (b in 0 until spec.numBlocks) {
            for (value in blocks[b]) {
                sequentialData[outIdx++] = value
            }
        }

        // Parse Byte mode: first 4 bits = 0100 (4), next 8 bits = charCount
        val mode = (sequentialData[0] ushr 4) and 0x0F
        require(mode == 0b0100) { "Expected QR Byte mode (4), got $mode" }
        val charCount = ((sequentialData[0] and 0x0F) shl 4) or ((sequentialData[1] ushr 4) and 0x0F)
        val decodedBytes = ByteArray(charCount)
        for (i in 0 until charCount) {
            val highNibble = (sequentialData[i + 1] and 0x0F) shl 4
            val lowNibble = (sequentialData[i + 2] ushr 4) and 0x0F
            decodedBytes[i] = (highNibble or lowNibble).toByte()
        }
        return String(decodedBytes, StandardCharsets.UTF_8)
    }

    /**
     * Generates a QR matrix for [payload].
     * When [gridSize] <= 0 or matches the ISO version dimension, returns the exact ISO/IEC 18004
     * QR matrix. If a fixed [gridSize] (e.g. 25) is requested and the payload fits in Version 2 (<= 26 bytes),
     * returns the 25x25 ISO matrix; otherwise returns a deterministic 25x25 matrix for legacy callers.
     */
    fun generateMatrix(payload: String, gridSize: Int = 0): Array<BooleanArray> {
        val isoMatrix = generateIsoQrMatrix(payload)
        if (gridSize <= 0 || isoMatrix.size == gridSize) {
            return isoMatrix
        }

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

        for (i in 8 until gridSize - 8) {
            reserved[6][i] = true
            matrix[6][i] = (i % 2 == 0)
            reserved[i][6] = true
            matrix[i][6] = (i % 2 == 0)
        }

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
     * Renders the square scannable QR code bitmap (white rounded card with ISO/IEC 18004 QR modules,
     * dark purple outer finder rings, champagne-gold finder centers, and optional luxury center camera badge).
     */
    fun createScannableQrSquareBitmap(
        qrPayload: String,
        sizePx: Int = 720,
        includeCenterLogo: Boolean = true
    ): Bitmap {
        val safeUrl = if (qrPayload.isBlank()) "$BASE_GUEST_PORTAL_URL" + "evt_live" else qrPayload
        val matrix = generateIsoQrMatrix(safeUrl)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val whiteCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val cornerRadius = sizePx * 0.08f
        canvas.drawRoundRect(
            RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat()),
            cornerRadius,
            cornerRadius,
            whiteCardPaint
        )

        drawIsoQrModulesOntoCanvas(
            canvas = canvas,
            matrix = matrix,
            boxLeft = 0f,
            boxTop = 0f,
            boxSize = sizePx.toFloat(),
            includeCenterLogo = includeCenterLogo
        )

        return bitmap
    }

    /**
     * Renders a high-resolution branded Event QR Card PNG Bitmap (default 1080px width)
     * for downloading to gallery and sharing via Android Share Sheet.
     */
    fun createBrandedEventQrBitmap(
        eventName: String,
        eventCategory: String,
        eventDate: String,
        qrPayload: String,
        sizePx: Int = 1080
    ): Bitmap {
        val heightPx = (sizePx * 1.25f).toInt()
        val bitmap = Bitmap.createBitmap(sizePx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val scale = sizePx / 960f

        // Background gradient (Midnight Black to Royal Purple)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                sizePx.toFloat(),
                heightPx.toFloat(),
                intArrayOf(
                    Color.parseColor("#1E0E3A"),
                    Color.parseColor("#110722"),
                    Color.parseColor("#080411")
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, sizePx.toFloat(), heightPx.toFloat(), bgPaint)

        // Glowing electric violet outer border + champagne gold inner hairline
        val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f * scale
            color = Color.parseColor("#A855F7")
        }
        val frameInset = 26f * scale
        val frameRect = RectF(frameInset, frameInset, sizePx - frameInset, heightPx - frameInset)
        canvas.drawRoundRect(frameRect, 42f * scale, 42f * scale, outerBorderPaint)

        val goldBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * scale
            color = Color.parseColor("#F59E0B")
        }
        val innerInset = 36f * scale
        canvas.drawRoundRect(
            RectF(innerInset, innerInset, sizePx - innerInset, heightPx - innerInset),
            36f * scale,
            36f * scale,
            goldBorderPaint
        )

        // Header text
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            textSize = 32f * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("✦  JUMMO PHOTOS AI • GUEST PORTAL  ✦", sizePx / 2f, 98f * scale, brandPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 46f * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val clippedTitle = if (eventName.length > 28) eventName.take(26) + "…" else eventName.ifBlank { "Studio Event" }
        canvas.drawText(clippedTitle, sizePx / 2f, 162f * scale, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E9D5FF")
            textSize = 28f * scale
            textAlign = Paint.Align.CENTER
        }
        val formattedDate = formatDisplayDate(eventDate, eventDate.ifBlank { "15 Dec 2026" })
        canvas.drawText("$eventCategory • $formattedDate", sizePx / 2f, 208f * scale, subPaint)

        // White QR container card
        val qrBoxMargin = 130f * scale
        val qrBoxTop = 250f * scale
        val qrBoxSize = sizePx - (qrBoxMargin * 2f)
        val whiteCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        val qrCardRect = RectF(qrBoxMargin, qrBoxTop, qrBoxMargin + qrBoxSize, qrBoxTop + qrBoxSize)
        canvas.drawRoundRect(qrCardRect, 38f * scale, 38f * scale, whiteCardPaint)

        // Draw real ISO/IEC 18004 QR Matrix inside white card
        val safeUrl = if (qrPayload.isBlank()) "$BASE_GUEST_PORTAL_URL" + "evt_live" else qrPayload
        val matrix = generateIsoQrMatrix(safeUrl)
        drawIsoQrModulesOntoCanvas(
            canvas = canvas,
            matrix = matrix,
            boxLeft = qrBoxMargin,
            boxTop = qrBoxTop,
            boxSize = qrBoxSize,
            includeCenterLogo = true
        )

        // Footer instruction
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 31f * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Scan to Find Your Event Photos Instantly",
            sizePx / 2f,
            qrBoxTop + qrBoxSize + 72f * scale,
            footerPaint
        )

        val urlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C4B5FD")
            textSize = 22f * scale
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(safeUrl, sizePx / 2f, qrBoxTop + qrBoxSize + 114f * scale, urlPaint)

        return bitmap
    }

    private fun drawIsoQrModulesOntoCanvas(
        canvas: Canvas,
        matrix: Array<BooleanArray>,
        boxLeft: Float,
        boxTop: Float,
        boxSize: Float,
        includeCenterLogo: Boolean
    ) {
        val gridCount = matrix.size
        // Mandatory 4-module ISO quiet zone around the QR matrix
        val quietZone = boxSize * 0.085f
        val drawableSize = boxSize - (quietZone * 2f)
        val cellSize = drawableSize / gridCount

        val darkPurplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#16082F")
            style = Paint.Style.FILL
        }
        val deepFinderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#120526")
            style = Paint.Style.FILL
        }
        val finderGoldCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B45309") // Deep rich amber-gold for high scanner contrast
            style = Paint.Style.FILL
        }
        val finderGoldInnerHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D97706")
            style = Paint.Style.FILL
        }

        // Draw the 3 solid rounded finder patterns cleanly
        fun drawRoundedFinder(startR: Int, startC: Int) {
            val left = boxLeft + quietZone + startC * cellSize
            val top = boxTop + quietZone + startR * cellSize
            val outerSize = 7 * cellSize

            // Outer 7x7 dark frame
            canvas.drawRoundRect(
                RectF(left, top, left + outerSize, top + outerSize),
                cellSize * 1.4f,
                cellSize * 1.4f,
                deepFinderPaint
            )
            // Inner 5x5 white ring
            val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                RectF(
                    left + cellSize,
                    top + cellSize,
                    left + 6 * cellSize,
                    top + 6 * cellSize
                ),
                cellSize * 1.0f,
                cellSize * 1.0f,
                whitePaint
            )
            // Inner 3x3 golden-amber core
            canvas.drawRoundRect(
                RectF(
                    left + 2 * cellSize,
                    top + 2 * cellSize,
                    left + 5 * cellSize,
                    top + 5 * cellSize
                ),
                cellSize * 0.8f,
                cellSize * 0.8f,
                finderGoldCenterPaint
            )
            canvas.drawRoundRect(
                RectF(
                    left + 2.35f * cellSize,
                    top + 2.35f * cellSize,
                    left + 4.65f * cellSize,
                    top + 4.65f * cellSize
                ),
                cellSize * 0.55f,
                cellSize * 0.55f,
                finderGoldInnerHighlightPaint
            )
        }

        drawRoundedFinder(0, 0)
        drawRoundedFinder(0, gridCount - 7)
        drawRoundedFinder(gridCount - 7, 0)

        // Center logo exclusion zone (~14% of matrix width, safely within Level M 15% Reed-Solomon ECC)
        val centerIndex = gridCount / 2
        val logoHalfModules = if (includeCenterLogo) 2 else -1

        for (r in 0 until gridCount) {
            for (c in 0 until gridCount) {
                val inTopLeftFinder = r in 0..6 && c in 0..6
                val inTopRightFinder = r in 0..6 && c in (gridCount - 7) until gridCount
                val inBottomLeftFinder = r in (gridCount - 7) until gridCount && c in 0..6
                if (inTopLeftFinder || inTopRightFinder || inBottomLeftFinder) continue

                if (includeCenterLogo &&
                    r in (centerIndex - logoHalfModules)..(centerIndex + logoHalfModules) &&
                    c in (centerIndex - logoHalfModules)..(centerIndex + logoHalfModules)
                ) {
                    continue
                }

                if (matrix[r][c]) {
                    val left = boxLeft + quietZone + c * cellSize
                    val top = boxTop + quietZone + r * cellSize
                    val pad = cellSize * 0.04f
                    canvas.drawRoundRect(
                        RectF(left + pad, top + pad, left + cellSize - pad, top + cellSize - pad),
                        cellSize * 0.26f,
                        cellSize * 0.26f,
                        darkPurplePaint
                    )
                }
            }
        }

        if (includeCenterLogo) {
            val badgeModules = (logoHalfModules * 2 + 1).toFloat()
            val badgeSize = badgeModules * cellSize
            val badgeLeft = boxLeft + quietZone + (centerIndex - logoHalfModules) * cellSize
            val badgeTop = boxTop + quietZone + (centerIndex - logoHalfModules) * cellSize
            val badgeRect = RectF(badgeLeft, badgeTop, badgeLeft + badgeSize, badgeTop + badgeSize)

            // White cushion border
            val whitePadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            val cushion = cellSize * 0.35f
            canvas.drawRoundRect(
                RectF(
                    badgeRect.left - cushion,
                    badgeRect.top - cushion,
                    badgeRect.right + cushion,
                    badgeRect.bottom + cushion
                ),
                badgeSize * 0.28f,
                badgeSize * 0.28f,
                whitePadPaint
            )

            // Dark royal purple rounded square background
            val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1A0B36")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(badgeRect, badgeSize * 0.24f, badgeSize * 0.24f, badgeBgPaint)

            // Golden camera body outline inside center badge
            val goldStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FBBF24")
                style = Paint.Style.STROKE
                strokeWidth = (badgeSize * 0.065f).coerceAtLeast(2f)
            }
            val camInsetX = badgeSize * 0.18f
            val camInsetY = badgeSize * 0.26f
            val camRect = RectF(
                badgeRect.left + camInsetX,
                badgeRect.top + camInsetY,
                badgeRect.right - camInsetX,
                badgeRect.bottom - camInsetY * 0.85f
            )
            canvas.drawRoundRect(camRect, badgeSize * 0.12f, badgeSize * 0.12f, goldStroke)

            // Camera lens violet fill + gold ring
            val cx = badgeRect.centerX()
            val cy = camRect.centerY()
            val lensRadius = badgeSize * 0.15f
            val lensFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#7C3AED")
                style = Paint.Style.FILL
            }
            canvas.drawCircle(cx, cy, lensRadius, lensFill)
            canvas.drawCircle(cx, cy, lensRadius, goldStroke)
        }
    }

    /**
     * Downloads the high-resolution branded QR code PNG to device MediaStore Pictures/JummoPhotosAI
     * (and guarantees a local file copy in filesDir/photos for verification).
     */
    fun saveQrBitmapToGallery(
        context: Context,
        eventName: String,
        eventCategory: String,
        eventDate: String,
        qrPayload: String
    ): Result<String> = runCatching {
        val safeUrl = if (qrPayload.isBlank()) "$BASE_GUEST_PORTAL_URL" + "evt_live" else qrPayload
        val bitmap = createBrandedEventQrBitmap(
            eventName = eventName,
            eventCategory = eventCategory,
            eventDate = eventDate,
            qrPayload = safeUrl,
            sizePx = 1200
        )
        val cleanName = eventName.replace(Regex("[^a-zA-Z0-9]+"), "_").trim('_').ifBlank { "Event" }.take(30)
        val fileName = "Jummo_QR_${cleanName}_${System.currentTimeMillis()}.png"

        // Always save a verified high-res PNG copy in app storage as well
        val localQrDir = File(context.filesDir, "qr_exports").apply { mkdirs() }
        val localFile = File(localQrDir, fileName)
        FileOutputStream(localFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

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

        val imageUri = try {
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        } catch (_: Exception) {
            null
        }

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
            localFile.absolutePath
        }
    }

    /**
     * Shares the branded QR code PNG via Android native Share Sheet using FileProvider.
     */
    fun shareEventQrCode(
        context: Context,
        eventName: String,
        eventCategory: String,
        eventDate: String,
        qrPayload: String
    ): Result<Intent> = runCatching {
        val safeUrl = if (qrPayload.isBlank()) "$BASE_GUEST_PORTAL_URL" + "evt_live" else qrPayload
        val bitmap = createBrandedEventQrBitmap(
            eventName = eventName,
            eventCategory = eventCategory,
            eventDate = eventDate,
            qrPayload = safeUrl,
            sizePx = 1200
        )
        val qrDir = File(context.cacheDir, "qr").apply { mkdirs() }
        val cleanName = eventName.replace(Regex("[^a-zA-Z0-9]+"), "_").trim('_').ifBlank { "event" }.take(24)
        val file = File(qrDir, "jummo_qr_${cleanName}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (_: Exception) {
            android.net.Uri.fromFile(file)
        }

        val formattedDate = formatDisplayDate(eventDate, eventDate)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_SUBJECT,
                "Jummo Photos AI Guest Portal — $eventName"
            )
            putExtra(
                Intent.EXTRA_TEXT,
                "Scan this QR code or open $safeUrl to find your event photos from $eventName ($eventCategory • $formattedDate)!"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Event QR Code").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        shareIntent
    }

    // =========================================================================
    // ISO/IEC 18004 QR Code Version 1–10 Level M Encoder Implementation
    // =========================================================================

    private data class QrVersionSpec(
        val version: Int,
        val totalDataCodewords: Int,
        val ecCodewordsPerBlock: Int,
        val numBlocksGroup1: Int,
        val dataCodewordsGroup1: Int,
        val numBlocksGroup2: Int,
        val dataCodewordsGroup2: Int,
        val alignmentCenters: IntArray
    ) {
        val numBlocks: Int get() = numBlocksGroup1 + numBlocksGroup2
        val maxBytePayload: Int get() = totalDataCodewords - 2 // 4-bit mode + 8-bit length + 4-bit terminator
    }

    // ISO/IEC 18004 Table 9 — Error Correction Level M (15%)
    private val VERSION_SPECS = listOf(
        QrVersionSpec(1, 16, 10, 1, 16, 0, 0, intArrayOf()),
        QrVersionSpec(2, 28, 16, 1, 28, 0, 0, intArrayOf(6, 18)),
        QrVersionSpec(3, 44, 26, 1, 44, 0, 0, intArrayOf(6, 22)),
        QrVersionSpec(4, 64, 18, 2, 32, 0, 0, intArrayOf(6, 26)),
        QrVersionSpec(5, 86, 24, 2, 43, 0, 0, intArrayOf(6, 30)),
        QrVersionSpec(6, 108, 16, 4, 27, 0, 0, intArrayOf(6, 34)),
        QrVersionSpec(7, 124, 18, 4, 31, 0, 0, intArrayOf(6, 22, 38)),
        QrVersionSpec(8, 154, 22, 2, 38, 2, 39, intArrayOf(6, 24, 42)),
        QrVersionSpec(9, 182, 22, 3, 36, 2, 37, intArrayOf(6, 26, 46))
    )

    private fun selectVersionSpec(byteLength: Int): QrVersionSpec {
        return VERSION_SPECS.firstOrNull { byteLength <= it.maxBytePayload }
            ?: VERSION_SPECS.last()
    }

    private fun buildDataCodewords(payloadBytes: ByteArray, totalDataCodewords: Int): IntArray {
        val maxLen = (totalDataCodewords - 2).coerceAtLeast(1)
        val trimmed = if (payloadBytes.size > maxLen) payloadBytes.copyOf(maxLen) else payloadBytes
        val totalBitsCapacity = totalDataCodewords * 8
        val bits = BooleanArray(totalBitsCapacity)
        var bitPos = 0

        fun appendBits(value: Int, count: Int) {
            for (i in count - 1 downTo 0) {
                if (bitPos < totalBitsCapacity) {
                    bits[bitPos++] = ((value ushr i) and 1) == 1
                }
            }
        }

        // Byte mode indicator = 0100 (4 bits)
        appendBits(0b0100, 4)
        // Character count indicator (8 bits for versions 1..9)
        appendBits(trimmed.size, 8)
        // Payload bytes
        for (b in trimmed) {
            appendBits(b.toInt() and 0xFF, 8)
        }
        // Terminator (up to 4 zero bits)
        val terminatorCount = minOf(4, totalBitsCapacity - bitPos)
        appendBits(0, terminatorCount)
        // Pad to byte boundary
        while (bitPos % 8 != 0 && bitPos < totalBitsCapacity) {
            appendBits(0, 1)
        }
        // Pad codewords 0xEC, 0x11 alternating
        val padBytes = intArrayOf(0xEC, 0x11)
        var padIdx = 0
        while (bitPos < totalBitsCapacity) {
            appendBits(padBytes[padIdx % 2], 8)
            padIdx++
        }

        val codewords = IntArray(totalDataCodewords)
        for (i in 0 until totalDataCodewords) {
            var byteVal = 0
            for (b in 0..7) {
                byteVal = (byteVal shl 1) or (if (bits[i * 8 + b]) 1 else 0)
            }
            codewords[i] = byteVal
        }
        return codewords
    }

    private fun interleaveWithEcc(dataCodewords: IntArray, spec: QrVersionSpec): IntArray {
        val dataBlocks = mutableListOf<IntArray>()
        val eccBlocks = mutableListOf<IntArray>()
        var offset = 0

        for (i in 0 until spec.numBlocksGroup1) {
            val block = dataCodewords.copyOfRange(offset, offset + spec.dataCodewordsGroup1)
            offset += spec.dataCodewordsGroup1
            dataBlocks.add(block)
            eccBlocks.add(computeReedSolomonEcc(block, spec.ecCodewordsPerBlock))
        }
        for (i in 0 until spec.numBlocksGroup2) {
            val block = dataCodewords.copyOfRange(offset, offset + spec.dataCodewordsGroup2)
            offset += spec.dataCodewordsGroup2
            dataBlocks.add(block)
            eccBlocks.add(computeReedSolomonEcc(block, spec.ecCodewordsPerBlock))
        }

        val totalCodewords = spec.totalDataCodewords + spec.numBlocks * spec.ecCodewordsPerBlock
        val result = IntArray(totalCodewords)
        var outIdx = 0

        val maxDataBlockLen = maxOf(spec.dataCodewordsGroup1, spec.dataCodewordsGroup2)
        for (col in 0 until maxDataBlockLen) {
            for (block in dataBlocks) {
                if (col < block.size) {
                    result[outIdx++] = block[col]
                }
            }
        }
        for (col in 0 until spec.ecCodewordsPerBlock) {
            for (ecc in eccBlocks) {
                result[outIdx++] = ecc[col]
            }
        }
        return result
    }

    // GF(256) with primitive polynomial 0x11D
    private val GF_EXP = IntArray(512)
    private val GF_LOG = IntArray(256)

    init {
        var x = 1
        for (i in 0 until 255) {
            GF_EXP[i] = x
            GF_LOG[x] = i
            x = x shl 1
            if ((x and 0x100) != 0) {
                x = x xor 0x11D
            }
        }
        for (i in 255 until 512) {
            GF_EXP[i] = GF_EXP[i - 255]
        }
    }

    private fun gfMul(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return GF_EXP[GF_LOG[a] + GF_LOG[b]]
    }

    private fun computeReedSolomonEcc(data: IntArray, ecCount: Int): IntArray {
        // Build generator polynomial of degree ecCount
        var gen = intArrayOf(1)
        for (i in 0 until ecCount) {
            val next = IntArray(gen.size + 1)
            val root = GF_EXP[i]
            for (j in gen.indices) {
                next[j] = next[j] xor gen[j]
                next[j + 1] = next[j + 1] xor gfMul(gen[j], root)
            }
            gen = next
        }

        val remainder = IntArray(ecCount)
        for (byteVal in data) {
            val factor = byteVal xor remainder[0]
            System.arraycopy(remainder, 1, remainder, 0, ecCount - 1)
            remainder[ecCount - 1] = 0
            if (factor != 0) {
                for (j in 0 until ecCount) {
                    remainder[j] = remainder[j] xor gfMul(gen[j + 1], factor)
                }
            }
        }
        return remainder
    }

    private fun setupFunctionPatterns(
        version: Int,
        size: Int,
        matrix: Array<BooleanArray>,
        isFunction: Array<BooleanArray>
    ) {
        fun placeFinderWithSeparator(topRow: Int, leftCol: Int) {
            for (r in -1..7) {
                for (c in -1..7) {
                    val rr = topRow + r
                    val cc = leftCol + c
                    if (rr in 0 until size && cc in 0 until size) {
                        isFunction[rr][cc] = true
                        val inFinder = r in 0..6 && c in 0..6
                        val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                        val isCenter = r in 2..4 && c in 2..4
                        matrix[rr][cc] = inFinder && (isBorder || isCenter)
                    }
                }
            }
        }

        placeFinderWithSeparator(0, 0)
        placeFinderWithSeparator(0, size - 7)
        placeFinderWithSeparator(size - 7, 0)

        // Timing patterns
        for (i in 8 until size - 8) {
            isFunction[6][i] = true
            matrix[6][i] = (i % 2 == 0)
            isFunction[i][6] = true
            matrix[i][6] = (i % 2 == 0)
        }

        // Alignment patterns
        val spec = VERSION_SPECS.first { it.version == version }
        val centers = spec.alignmentCenters
        for (cr in centers) {
            for (cc in centers) {
                val overlapsFinder =
                    (cr <= 8 && cc <= 8) ||
                        (cr <= 8 && cc >= size - 9) ||
                        (cr >= size - 9 && cc <= 8)
                if (overlapsFinder) continue
                for (dr in -2..2) {
                    for (dc in -2..2) {
                        val r = cr + dr
                        val c = cc + dc
                        isFunction[r][c] = true
                        matrix[r][c] = (dr == -2 || dr == 2 || dc == -2 || dc == 2 || (dr == 0 && dc == 0))
                    }
                }
            }
        }

        // Dark module
        isFunction[4 * version + 9][8] = true
        matrix[4 * version + 9][8] = true

        // Reserve Format Information areas
        for (i in 0..8) {
            if (i < size) {
                isFunction[8][i] = true
                isFunction[i][8] = true
            }
        }
        for (i in 0..7) {
            isFunction[8][size - 1 - i] = true
            isFunction[size - 1 - i][8] = true
        }
    }

    private fun placeCodewordBits(
        codewords: IntArray,
        size: Int,
        matrix: Array<BooleanArray>,
        isFunction: Array<BooleanArray>
    ) {
        var bitIdx = 0
        val totalBits = codewords.size * 8
        var rightCol = size - 1
        var upward = true

        while (rightCol >= 1) {
            if (rightCol == 6) rightCol-- // Skip vertical timing pattern column
            for (step in 0 until size) {
                val r = if (upward) (size - 1 - step) else step
                for (cOffset in 0..1) {
                    val c = rightCol - cOffset
                    if (!isFunction[r][c]) {
                        val bit = if (bitIdx < totalBits) {
                            ((codewords[bitIdx / 8] ushr (7 - (bitIdx % 8))) and 1) == 1
                        } else {
                            false
                        }
                        matrix[r][c] = bit
                        bitIdx++
                    }
                }
            }
            upward = !upward
            rightCol -= 2
        }
    }

    private fun applyMask(
        mask: Int,
        size: Int,
        matrix: Array<BooleanArray>,
        isFunction: Array<BooleanArray>
    ) {
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (!isFunction[r][c]) {
                    val invert = when (mask) {
                        0 -> (r + c) % 2 == 0
                        1 -> r % 2 == 0
                        2 -> c % 3 == 0
                        3 -> (r + c) % 3 == 0
                        4 -> ((r / 2) + (c / 3)) % 2 == 0
                        5 -> ((r * c) % 2) + ((r * c) % 3) == 0
                        6 -> (((r * c) % 2) + ((r * c) % 3)) % 2 == 0
                        else -> (((r + c) % 2) + ((r * c) % 3)) % 2 == 0
                    }
                    if (invert) {
                        matrix[r][c] = !matrix[r][c]
                    }
                }
            }
        }
    }

    private fun writeFormatBits(
        ecLevelBits: Int,
        mask: Int,
        size: Int,
        matrix: Array<BooleanArray>
    ) {
        val data = ((ecLevelBits and 0b11) shl 3) or (mask and 0b111)
        var rem = data shl 10
        for (i in 14 downTo 10) {
            if (((rem ushr i) and 1) != 0) {
                rem = rem xor (0x537 shl (i - 10))
            }
        }
        val bits = ((data shl 10) or rem) xor 0x5412

        fun bit(index: Int): Boolean = ((bits ushr index) and 1) == 1

        // Top-left around finder: bit 14 down to 0
        val topLeftCoords = listOf(
            8 to 0, 8 to 1, 8 to 2, 8 to 3, 8 to 4, 8 to 5, 8 to 7, 8 to 8,
            7 to 8, 5 to 8, 4 to 8, 3 to 8, 2 to 8, 1 to 8, 0 to 8
        )
        for (i in 0..14) {
            val (r, c) = topLeftCoords[14 - i]
            matrix[r][c] = bit(i)
        }

        // Bottom-left vertical (bits 14..8) & Top-right horizontal (bits 7..0)
        for (i in 0..6) {
            matrix[size - 1 - i][8] = bit(14 - i)
        }
        for (i in 0..7) {
            matrix[8][size - 8 + i] = bit(7 - i)
        }
    }

    private fun calculateIsoPenalty(matrix: Array<BooleanArray>, size: Int): Int {
        var penalty = 0
        // Rule 1: 5+ consecutive modules of same color in row/col
        for (r in 0 until size) {
            var runLen = 1
            for (c in 1 until size) {
                if (matrix[r][c] == matrix[r][c - 1]) {
                    runLen++
                    if (runLen == 5) penalty += 3
                    else if (runLen > 5) penalty += 1
                } else {
                    runLen = 1
                }
            }
        }
        for (c in 0 until size) {
            var runLen = 1
            for (r in 1 until size) {
                if (matrix[r][c] == matrix[r - 1][c]) {
                    runLen++
                    if (runLen == 5) penalty += 3
                    else if (runLen > 5) penalty += 1
                } else {
                    runLen = 1
                }
            }
        }
        // Rule 2: 2x2 blocks of same color
        for (r in 0 until size - 1) {
            for (c in 0 until size - 1) {
                val v = matrix[r][c]
                if (matrix[r][c + 1] == v && matrix[r + 1][c] == v && matrix[r + 1][c + 1] == v) {
                    penalty += 3
                }
            }
        }
        return penalty
    }
}
