package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.GuestAccessModes
import com.example.data.model.PhotoEvent
import com.example.ui.components.JummoLuxuryCameraLogo
import com.example.ui.theme.JummoBorderGlow
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoErrorRed
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoTextPrimary
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import com.example.ui.theme.JummoVioletPrimary
import com.example.util.QrCodeGenerator

/**
 * Ultra-Luxury Jummo Photos AI QR Management Modal designed from the approved reference image.
 *
 * Implements:
 * 1. Dynamic, standards-compliant ISO/IEC 18004 QR code generation per event.
 * 2. Encodes the actual Guest Portal URL containing the event ID (`https://jummophotos.ai/guest/<eventId>`).
 * 3. High-resolution PNG download to gallery & local storage.
 * 4. Native Android Share Sheet integration via FileProvider.
 * 5. Enable / Disable Event QR Portal toggle.
 * 6. QR Expiration Date selector & custom date picker with live expiration enforcement.
 * 7. Three Guest Access Modes:
 *    - Consent Face Search Only
 *    - View All + Face Search
 *    - Organizer Approved Access
 * 8. Direct "Open Guest Portal" launcher with live settings & validation.
 * 9. Fixed bottom action bar ("Close" and "Save QR Settings").
 */
@Composable
fun EventQrSystemDialog(
    event: PhotoEvent,
    onUpdateEvent: (PhotoEvent, String) -> Unit,
    onOpenGuestPortal: (PhotoEvent) -> Unit,
    onShowMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val canonicalQrUrl = remember(event.id, event.qrCodeValue) {
        QrCodeGenerator.buildGuestPortalUrl(event.id, event.qrCodeValue)
    }

    var qrEnabled by remember(event.id, event.qrEnabled) {
        mutableStateOf(event.qrEnabled)
    }
    var expirationDate by remember(event.id, event.qrExpirationDate) {
        mutableStateOf(
            if (event.qrExpirationDate.isBlank() || event.qrExpirationDate == "Never") {
                event.qrExpirationDate.ifBlank { "01 Mar 2027" }
            } else {
                QrCodeGenerator.formatDisplayDate(event.qrExpirationDate, "01 Mar 2027")
            }
        )
    }
    var guestAccessMode by remember(event.id, event.guestAccessMode) {
        mutableStateOf(event.guestAccessMode.ifBlank { GuestAccessModes.FACE_SEARCH_ONLY })
    }
    var showExpirationPicker by remember { mutableStateOf(false) }

    val qrSquareBitmap: Bitmap = remember(canonicalQrUrl) {
        QrCodeGenerator.createScannableQrSquareBitmap(
            qrPayload = canonicalQrUrl,
            sizePx = 720,
            includeCenterLogo = true
        )
    }

    val isExpiredNow = remember(expirationDate) {
        QrCodeGenerator.isQrExpired(expirationDate)
    }

    fun buildUpdatedEvent(): PhotoEvent {
        return event.copy(
            qrCodeValue = canonicalQrUrl,
            qrEnabled = qrEnabled,
            qrExpirationDate = expirationDate.trim().ifBlank { "01 Mar 2027" },
            guestAccessMode = guestAccessMode
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 430.dp)
                .padding(vertical = 10.dp)
                .testTag("qr_management_modal"),
            shape = RoundedCornerShape(26.dp),
            color = JummoObsidian,
            border = BorderStroke(
                width = 1.3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        JummoVioletPrimary,
                        JummoVioletBright,
                        JummoBorderSubtle
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                JummoObsidian,
                                JummoDeepPurple,
                                JummoObsidian
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // =============================================================
                // 1. TOP HEADER BAR
                // =============================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(JummoCardPurple, JummoBorderSubtle)
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = JummoBorderGlow,
                                    shape = RoundedCornerShape(11.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QR Management Icon",
                                tint = JummoVioletPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "QR Management",
                                color = JummoTextPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Create & manage guest access for your event",
                                color = JummoTextSecondary,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("qr_modal_close_top")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close QR Management",
                            tint = JummoTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // =============================================================
                // SCROLLABLE BODY (Hero QR Card + Controls)
                // =============================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .testTag("qr_modal_scroll_body"),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    // ---------------------------------------------------------
                    // 2. LUXURY BRANDED HERO QR PREVIEW CARD
                    // ---------------------------------------------------------
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = JummoCardPurple,
                        border = BorderStroke(
                            width = 1.25.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    JummoVioletPrimary,
                                    JummoVioletBright,
                                    JummoBorderSubtle
                                )
                            )
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            JummoCardPurple,
                                            JummoObsidian,
                                            JummoCardPurple
                                        )
                                    )
                                )
                        ) {
                            // Left & Right Cinematic Photography Vignettes
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(125.dp)
                                        .height(195.dp)
                                        .offset(x = (-6).dp, y = 18.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                ) {
                                    if (event.coverUri.isNotBlank()) {
                                        AsyncImage(
                                            model = event.coverUri,
                                            contentDescription = null,
                                            placeholder = painterResource(id = R.drawable.img_wedding_cover),
                                            error = painterResource(id = R.drawable.img_wedding_cover),
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            alpha = 0.58f
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_wedding_cover),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            alpha = 0.58f
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        JummoCardPurple.copy(alpha = 0.85f),
                                                        JummoCardPurple
                                                    )
                                                )
                                            )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(185.dp)
                                        .offset(x = 8.dp, y = 16.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_festival_cover),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        alpha = 0.45f
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        JummoCardPurple,
                                                        JummoCardPurple.copy(alpha = 0.78f),
                                                        Color.Transparent
                                                    )
                                                )
                                            )
                                    )
                                }
                            }

                            // Hero Foreground Content
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Studio Laurel Wreath + Camera Emblem + "Jummo Photos AI / Guest Portal"
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    GoldLaurelBranch(isLeft = true)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    JummoLuxuryCameraLogo(
                                        size = 26.dp,
                                        showCrown = false
                                    )
                                    Spacer(modifier = Modifier.width(7.dp))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Jummo Photos AI",
                                            color = JummoTextPrimary,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Guest Portal",
                                            color = JummoTextSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    GoldLaurelBranch(isLeft = false)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Event Title
                                Text(
                                    text = event.name.ifBlank { "Rahul weds Pooja" },
                                    color = JummoTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.testTag("qr_hero_event_name")
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                // Category • Formatted Date
                                val formattedEventDate = remember(event.eventDate) {
                                    QrCodeGenerator.formatDisplayDate(
                                        event.eventDate,
                                        event.eventDate.ifBlank { "15 Dec 2026" }
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = JummoVioletPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${event.category} • $formattedEventDate",
                                        color = JummoTextSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(7.dp))

                                // White Rounded QR Square + Glowing Purple Ring Pedestal
                                Box(
                                    contentAlignment = Alignment.BottomCenter,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Glowing Violet Pedestal Rings below QR card
                                    Canvas(
                                        modifier = Modifier
                                            .width(204.dp)
                                            .height(30.dp)
                                            .offset(y = 6.dp)
                                    ) {
                                        drawOval(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFFA855F7).copy(alpha = 0.42f),
                                                    Color.Transparent
                                                )
                                            ),
                                            topLeft = Offset(0f, 0f),
                                            size = Size(size.width, size.height)
                                        )
                                        drawOval(
                                            color = Color(0xFFD946EF).copy(alpha = 0.65f),
                                            topLeft = Offset(size.width * 0.06f, size.height * 0.18f),
                                            size = Size(size.width * 0.88f, size.height * 0.64f),
                                            style = Stroke(width = 1.5.dp.toPx())
                                        )
                                        drawOval(
                                            color = Color(0xFFA855F7).copy(alpha = 0.4f),
                                            topLeft = Offset(size.width * 0.15f, size.height * 0.30f),
                                            size = Size(size.width * 0.70f, size.height * 0.42f),
                                            style = Stroke(width = 1.dp.toPx())
                                        )
                                    }

                                    // Crisp White Rounded QR Code Card
                                    Surface(
                                        modifier = Modifier
                                            .size(152.dp)
                                            .padding(bottom = 4.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White,
                                        shadowElevation = 10.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Image(
                                                bitmap = qrSquareBitmap.asImageBitmap(),
                                                contentDescription = "Scannable Event QR Code for ${event.name}",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(4.dp)
                                                    .testTag("qr_code_preview_image")
                                            )

                                            if (!qrEnabled || isExpiredNow) {
                                                Surface(
                                                    color = JummoErrorRed.copy(alpha = 0.92f),
                                                    shape = RoundedCornerShape(50),
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(6.dp)
                                                ) {
                                                    Text(
                                                        text = if (!qrEnabled) "PAUSED" else "EXPIRED",
                                                        color = Color.White,
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(5.dp))

                                // Scan Instruction Text
                                Text(
                                    text = buildAnnotatedString {
                                        append("Scan to Find ")
                                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = JummoTextPrimary)) {
                                            append("Your Event Photos")
                                        }
                                    },
                                    color = JummoTextSecondary,
                                    fontSize = 11.5.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 3 Compact Pill Buttons: Download QR | Share QR | Open Guest Portal
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Download QR
                                    QrHeroActionButton(
                                        icon = Icons.Default.Download,
                                        iconTint = Color.White,
                                        label = "Download QR",
                                        isGoldHighlighted = true,
                                        onClick = {
                                            QrCodeGenerator.saveQrBitmapToGallery(
                                                context = context,
                                                eventName = event.name,
                                                eventCategory = event.category,
                                                eventDate = event.eventDate,
                                                qrPayload = canonicalQrUrl
                                            ).onSuccess { savedPath ->
                                                onShowMessage("High-res QR PNG saved to $savedPath")
                                            }.onFailure { err ->
                                                onShowMessage("QR download failed: ${err.localizedMessage}")
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("qr_download_button")
                                    )

                                    // 2. Share QR
                                    QrHeroActionButton(
                                        icon = Icons.Default.Share,
                                        iconTint = Color.White,
                                        label = "Share QR",
                                        isGoldHighlighted = false,
                                        onClick = {
                                            QrCodeGenerator.shareEventQrCode(
                                                context = context,
                                                eventName = event.name,
                                                eventCategory = event.category,
                                                eventDate = event.eventDate,
                                                qrPayload = canonicalQrUrl
                                            ).onSuccess {
                                                onShowMessage("Opening share sheet for ${event.name} QR...")
                                            }.onFailure { err ->
                                                onShowMessage("Unable to share QR: ${err.localizedMessage}")
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(0.88f)
                                            .testTag("qr_share_button")
                                    )

                                    // 3. Open Guest Portal
                                    QrHeroActionButton(
                                        icon = Icons.AutoMirrored.Filled.OpenInNew,
                                        iconTint = Color.White,
                                        label = "Open Guest Portal",
                                        isGoldHighlighted = false,
                                        onClick = {
                                            val updated = buildUpdatedEvent()
                                            onUpdateEvent(
                                                updated,
                                                "Opening Guest Portal for ${event.name}..."
                                            )
                                            onOpenGuestPortal(updated)
                                        },
                                        modifier = Modifier
                                            .weight(1.18f)
                                            .testTag("qr_open_guest_portal_button")
                                    )
                                }
                            }
                        }
                    }

                    // ---------------------------------------------------------
                    // 3. ENABLE EVENT QR PORTAL ROW CARD
                    // ---------------------------------------------------------
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        color = JummoCardPurple,
                        border = BorderStroke(0.9.dp, JummoBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(JummoBorderSubtle)
                                        .border(0.75.dp, JummoBorderGlow, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = JummoVioletPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Enable Event QR Portal",
                                        color = JummoTextPrimary,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (qrEnabled) {
                                            "Guests can scan and search photos"
                                        } else {
                                            "QR access is paused for guests"
                                        },
                                        color = JummoTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            LuxuryGoldSwitch(
                                checked = qrEnabled,
                                onCheckedChange = { checked ->
                                    qrEnabled = checked
                                    onUpdateEvent(
                                        buildUpdatedEvent().copy(qrEnabled = checked),
                                        if (checked) {
                                            "Event QR Portal enabled for ${event.name}."
                                        } else {
                                            "Event QR Portal disabled for ${event.name}."
                                        }
                                    )
                                },
                                modifier = Modifier.testTag("qr_enable_switch")
                            )
                        }
                    }

                    // ---------------------------------------------------------
                    // 4. QR EXPIRATION DATE ROW CARD
                    // ---------------------------------------------------------
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        color = JummoCardPurple,
                        border = BorderStroke(
                            0.9.dp,
                            if (isExpiredNow) JummoErrorRed.copy(alpha = 0.8f) else JummoBorderSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = JummoVioletPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "QR Expiration Date",
                                        color = JummoTextPrimary,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isExpiredNow) {
                                        Text(
                                            text = "Expired — guest access blocked",
                                            color = JummoErrorRed,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showExpirationPicker = true }
                                    .testTag("qr_expiration_selector"),
                                shape = RoundedCornerShape(10.dp),
                                color = JummoObsidian,
                                border = BorderStroke(0.9.dp, JummoBorderGlow)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = expirationDate.ifBlank { "01 Mar 2027" },
                                        color = JummoTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.testTag("qr_expiration_value_text")
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select Expiration Date",
                                        tint = JummoVioletPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ---------------------------------------------------------
                    // 5. GUEST ACCESS MODE CARD (3-COLUMN SELECTOR)
                    // ---------------------------------------------------------
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = JummoCardPurple,
                        border = BorderStroke(0.9.dp, JummoBorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = JummoVioletPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Guest Access Mode",
                                    color = JummoTextPrimary,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                GuestAccessModeOptionCard(
                                    topLine = "Consent",
                                    bottomLine = "Face Search Only",
                                    selected = guestAccessMode == GuestAccessModes.FACE_SEARCH_ONLY,
                                    onClick = {
                                        guestAccessMode = GuestAccessModes.FACE_SEARCH_ONLY
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("qr_mode_face_search_only")
                                )

                                GuestAccessModeOptionCard(
                                    topLine = "View All +",
                                    bottomLine = "Face Search",
                                    selected = guestAccessMode == GuestAccessModes.VIEW_ALL_PERMITTED,
                                    onClick = {
                                        guestAccessMode = GuestAccessModes.VIEW_ALL_PERMITTED
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("qr_mode_view_all")
                                )

                                GuestAccessModeOptionCard(
                                    topLine = "Organizer",
                                    bottomLine = "Approved Access",
                                    selected = guestAccessMode == GuestAccessModes.PIN_PROTECTED,
                                    onClick = {
                                        guestAccessMode = GuestAccessModes.PIN_PROTECTED
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("qr_mode_organizer_approved")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // =============================================================
                // 6. FIXED BOTTOM ACTION BAR ("Close" + "✓ Save QR Settings")
                // =============================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close Pill Button
                    Surface(
                        modifier = Modifier
                            .weight(0.42f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { onDismiss() }
                            .testTag("qr_close_button"),
                        shape = RoundedCornerShape(50),
                        color = JummoCardPurple,
                        border = BorderStroke(1.dp, JummoBorderSubtle)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Close",
                                color = JummoTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Save QR Settings Purple Pill Button
                    Surface(
                        modifier = Modifier
                            .weight(0.58f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                val updated = buildUpdatedEvent()
                                onUpdateEvent(
                                    updated,
                                    "QR settings saved for ${event.name} (${GuestAccessModes.displayLabel(guestAccessMode)})."
                                )
                                onDismiss()
                            }
                            .testTag("qr_save_settings_button"),
                        shape = RoundedCornerShape(50),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, JummoBorderGlow)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            JummoVioletPrimary,
                                            JummoVioletBright
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Save QR Settings",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Expiration Date Selection & Custom Date Dialog
    if (showExpirationPicker) {
        QrExpirationPickerDialog(
            currentValue = expirationDate,
            onSelectDate = { selected ->
                expirationDate = selected
                showExpirationPicker = false
            },
            onDismiss = { showExpirationPicker = false }
        )
    }
}

@Composable
private fun QrHeroActionButton(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    isGoldHighlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(50))
            .clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        border = BorderStroke(
            width = if (isGoldHighlighted) 1.1.dp else 0.85.dp,
            color = if (isGoldHighlighted) JummoVioletPrimary else JummoBorderGlow
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isGoldHighlighted) {
                        Brush.verticalGradient(
                            colors = listOf(
                                JummoVioletPrimary,
                                JummoVioletBright
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                JummoVioletBright,
                                JummoVioletPrimary
                            )
                        )
                    }
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 10.5.sp,
                    fontWeight = if (isGoldHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LuxuryGoldSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(48.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (checked) {
                    Brush.horizontalGradient(
                        colors = listOf(JummoVioletPrimary, JummoVioletBright)
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(JummoBorderSubtle, JummoCardPurple)
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (checked) JummoVioletPrimary else JummoBorderSubtle,
                shape = RoundedCornerShape(50)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (checked) Color.White else JummoTextSecondary)
                .border(
                    width = 0.8.dp,
                    color = if (checked) JummoVioletPrimary else Color.Transparent,
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun GuestAccessModeOptionCard(
    topLine: String,
    bottomLine: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = BorderStroke(
            width = if (selected) 1.3.dp else 0.85.dp,
            color = if (selected) JummoVioletPrimary else JummoBorderSubtle
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (selected) {
                        Brush.verticalGradient(
                            colors = listOf(
                                JummoVioletPrimary,
                                JummoVioletBright
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                JummoObsidian,
                                JummoCardPurple
                            )
                        )
                    }
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = topLine,
                    color = if (selected) Color.White else JummoTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = bottomLine,
                    color = if (selected) Color.White else JummoTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected Mode",
                        tint = JummoVioletPrimary,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GoldLaurelBranch(isLeft: Boolean) {
    Canvas(modifier = Modifier.size(width = 16.dp, height = 24.dp)) {
        val gold = Color(0xFF5B21B6)
        val path = Path().apply {
            if (isLeft) {
                moveTo(size.width * 0.75f, size.height * 0.90f)
                quadraticTo(
                    size.width * 0.15f,
                    size.height * 0.50f,
                    size.width * 0.70f,
                    size.height * 0.10f
                )
            } else {
                moveTo(size.width * 0.25f, size.height * 0.90f)
                quadraticTo(
                    size.width * 0.85f,
                    size.height * 0.50f,
                    size.width * 0.30f,
                    size.height * 0.10f
                )
            }
        }
        drawPath(path = path, color = gold, style = Stroke(width = 1.5.dp.toPx()))
        val leafY = listOf(0.22f, 0.42f, 0.62f, 0.80f)
        for (fy in leafY) {
            val cx = if (isLeft) size.width * 0.42f else size.width * 0.58f
            drawCircle(
                color = gold,
                radius = 1.8.dp.toPx(),
                center = Offset(cx, size.height * fy)
            )
        }
    }
}

@Composable
private fun QrExpirationPickerDialog(
    currentValue: String,
    onSelectDate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var customInput by remember(currentValue) { mutableStateOf(currentValue) }
    val presets = listOf(
        "01 Mar 2027",
        "15 Jun 2027",
        "31 Dec 2027",
        "Never",
        "01 Jan 2020" // Expired preset for verifying expiration enforcement
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JummoCardPurple,
        title = {
            Text(
                text = "Set QR Expiration Date",
                color = JummoTextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select an expiration date or enter a custom date:",
                    style = MaterialTheme.typography.bodySmall,
                    color = JummoTextSecondary
                )

                presets.forEach { preset ->
                    val isSelected = customInput.equals(preset, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                customInput = preset
                                onSelectDate(preset)
                            }
                            .testTag("qr_exp_preset_${preset.replace(" ", "_")}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) JummoVioletPrimary else JummoObsidian,
                        border = BorderStroke(
                            width = 0.9.dp,
                            color = if (isSelected) JummoVioletPrimary else JummoBorderSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset,
                                color = if (isSelected) Color.White else JummoTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            if (QrCodeGenerator.isQrExpired(preset)) {
                                Text(
                                    text = "Expired",
                                    color = JummoErrorRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = customInput,
                    onValueChange = { customInput = it },
                    label = { Text("Custom Expiration Date") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qr_custom_expiration_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val formatted = QrCodeGenerator.formatDisplayDate(
                        customInput.trim().ifBlank { "01 Mar 2027" },
                        "01 Mar 2027"
                    )
                    onSelectDate(formatted)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = JummoVioletPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("qr_apply_expiration_button")
            ) {
                Text("Apply Date", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = JummoTextSecondary)
            }
        }
    )
}
