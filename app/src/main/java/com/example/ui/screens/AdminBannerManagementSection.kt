package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.model.BannerPresets
import com.example.data.model.HomeBanner
import com.example.ui.components.StatusBadgePill
import com.example.ui.theme.JummoBorderGlow
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoElevatedPurple
import com.example.ui.theme.JummoErrorRed
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoSuccessGreen
import com.example.ui.theme.JummoTextMuted
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import com.example.ui.theme.JummoVioletPrimary
import kotlin.math.roundToInt

/**
 * Dedicated Admin Banner Management Section inside the Admin Dashboard.
 * Supports:
 * - Multiple banner uploads from mobile/gallery (PickMultipleVisualMedia)
 * - 1-tap studio photography banner addition
 * - Live image-only carousel preview (strictly zero text over banner image)
 * - Drag-and-drop & up/down display order reordering
 * - Individual banner Activate / Deactivate toggle
 * - Replace banner image from gallery or preset
 * - Delete individual banners
 * - Automatic slide duration configuration (2s, 3s, 4s, 5s, 7s)
 */
@Composable
fun AdminBannerManagementSection(
    banners: List<HomeBanner>,
    onUploadMultipleBanners: (List<Uri>) -> Unit,
    onAddPresetBanner: (String) -> Unit,
    onToggleBannerActive: (HomeBanner) -> Unit,
    onReplaceBannerFromUri: (HomeBanner, Uri) -> Unit,
    onReplaceBannerWithPreset: (HomeBanner, String) -> Unit,
    onReorderBanners: (List<HomeBanner>) -> Unit,
    onUpdateSlideDuration: (Long) -> Unit,
    onDeleteBanner: (HomeBanner) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedBanners = remember(banners) {
        banners.sortedBy { it.displayOrder }
    }
    val activeBanners = remember(sortedBanners) {
        sortedBanners.filter { it.isActive }
    }
    val currentDurationSec = remember(sortedBanners) {
        (sortedBanners.firstOrNull()?.slideDurationSeconds ?: 4L).coerceIn(1L, 30L)
    }

    var bannerPendingReplace by remember { mutableStateOf<HomeBanner?>(null) }

    // Multiple banner upload picker from mobile gallery
    val multipleBannersLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onUploadMultipleBanners(uris)
        }
    }

    // Single banner image replacement picker
    val replaceBannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val target = bannerPendingReplace
        if (uri != null && target != null) {
            onReplaceBannerFromUri(target, uri)
        }
        bannerPendingReplace = null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_banner_management_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(1.dp, JummoGoldBright.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section Title & Active Count Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JummoGoldBright.copy(alpha = 0.16f))
                            .border(0.75.dp, JummoGoldBright.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewCarousel,
                            contentDescription = "Banner Management",
                            tint = JummoGoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Home Banner Management",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Image-only slider • Drag or tap arrows to reorder",
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoTextSecondary
                        )
                    }
                }

                StatusBadgePill(
                    text = "${activeBanners.size}/${sortedBanners.size} ACTIVE",
                    color = if (activeBanners.isNotEmpty()) JummoSuccessGreen else JummoGoldBright
                )
            }

            // Live Image-Only Carousel Preview
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "LIVE HOME SLIDER PREVIEW (IMAGE-ONLY)",
                    style = MaterialTheme.typography.labelSmall,
                    color = JummoGoldBright,
                    fontWeight = FontWeight.Bold
                )
                HomeImageOnlyBannerCarousel(
                    activeBanners = activeBanners,
                    modifier = Modifier.testTag("admin_banner_live_preview")
                )
            }

            // Upload Banner Actions (Gallery Multi-Select + Studio Preset Upload)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        multipleBannersLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoGoldBright,
                        contentColor = JummoObsidian
                    ),
                    shape = RoundedCornerShape(11.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("admin_upload_banners_gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Upload Banners",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = {
                        val nextPreset = BannerPresets.ALL[sortedBanners.size % BannerPresets.ALL.size]
                        onAddPresetBanner(nextPreset)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoVioletPrimary,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(0.85.dp, JummoBorderGlow),
                    shape = RoundedCornerShape(11.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("admin_add_studio_banner_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Studio Shot",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Automatic Slide Duration Selector Row
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = JummoDeepPurple.copy(alpha = 0.85f),
                border = BorderStroke(0.75.dp, JummoBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Slide Duration",
                            tint = JummoGoldBright,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Auto Slide Duration",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf(2L, 3L, 4L, 5L, 7L).forEach { sec ->
                            val selected = currentDurationSec == sec
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onUpdateSlideDuration(sec) }
                                    .testTag("admin_banner_duration_${sec}s"),
                                color = if (selected) JummoGoldBright else JummoCardPurple,
                                border = BorderStroke(
                                    0.75.dp,
                                    if (selected) JummoGoldBright else JummoBorderSubtle
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${sec}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) JummoObsidian else JummoTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = JummoBorderSubtle.copy(alpha = 0.6f))

            // Banner List with Drag-and-Drop Reordering, Activate Toggle, Replace & Delete
            if (sortedBanners.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = JummoDeepPurple.copy(alpha = 0.6f),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_banners_empty_state")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "No Home Banners Configured",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Upload banner photos from your device gallery or add a studio shot above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = JummoTextSecondary
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("admin_banner_items_column")
                ) {
                    sortedBanners.forEachIndexed { index, banner ->
                        AdminBannerRowItem(
                            banner = banner,
                            index = index,
                            totalCount = sortedBanners.size,
                            onToggleActive = { onToggleBannerActive(banner) },
                            onMoveUp = {
                                if (index > 0) {
                                    val mutable = sortedBanners.toMutableList()
                                    val item = mutable.removeAt(index)
                                    mutable.add(index - 1, item)
                                    onReorderBanners(mutable)
                                }
                            },
                            onMoveDown = {
                                if (index < sortedBanners.lastIndex) {
                                    val mutable = sortedBanners.toMutableList()
                                    val item = mutable.removeAt(index)
                                    mutable.add(index + 1, item)
                                    onReorderBanners(mutable)
                                }
                            },
                            onReplaceGallery = {
                                bannerPendingReplace = banner
                                replaceBannerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onReplaceStudioPreset = {
                                val currentIdx = BannerPresets.ALL.indexOf(banner.imageUri)
                                val nextPreset = BannerPresets.ALL[(currentIdx + 1).coerceAtLeast(0) % BannerPresets.ALL.size]
                                onReplaceBannerWithPreset(banner, nextPreset)
                            },
                            onDelete = { onDeleteBanner(banner) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminBannerRowItem(
    banner: HomeBanner,
    index: Int,
    totalCount: Int,
    onToggleActive: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onReplaceGallery: () -> Unit,
    onReplaceStudioPreset: () -> Unit,
    onDelete: () -> Unit
) {
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, dragOffsetY.roundToInt()) }
            .pointerInput(banner.id, index, totalCount) {
                detectDragGesturesAfterLongPress(
                    onDragEnd = {
                        if (dragOffsetY < -48f && index > 0) {
                            onMoveUp()
                        } else if (dragOffsetY > 48f && index < totalCount - 1) {
                            onMoveDown()
                        }
                        dragOffsetY = 0f
                    },
                    onDragCancel = {
                        dragOffsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetY = (dragOffsetY + dragAmount.y).coerceIn(-120f, 120f)
                    }
                )
            }
            .testTag("admin_banner_row_${banner.id}"),
        shape = RoundedCornerShape(14.dp),
        color = JummoDeepPurple.copy(alpha = 0.92f),
        border = BorderStroke(
            0.85.dp,
            if (banner.isActive) JummoGoldBright.copy(alpha = 0.45f) else JummoBorderSubtle
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Drag Handle + Image-Only Thumbnail Preview + Order Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DragIndicator,
                        contentDescription = "Drag to reorder",
                        tint = JummoTextMuted,
                        modifier = Modifier.size(18.dp)
                    )

                    // Strictly Image-Only Banner Thumbnail
                    Box(
                        modifier = Modifier
                            .width(96.dp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                0.75.dp,
                                if (banner.isActive) JummoGoldBright else JummoBorderSubtle,
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        BannerImageSurface(
                            imageUri = banner.imageUri,
                            contentDescription = "Banner Preview ${index + 1}",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = JummoGoldBright.copy(alpha = 0.18f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JummoGoldBright,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = if (banner.isActive) "Active" else "Inactive",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (banner.isActive) JummoSuccessGreen else JummoTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Slide ${ banner.slideDurationSeconds }s • Order ${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Active / Inactive Switch
                Switch(
                    checked = banner.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JummoObsidian,
                        checkedTrackColor = JummoGoldBright,
                        uncheckedThumbColor = JummoTextSecondary,
                        uncheckedTrackColor = JummoCardPurple
                    ),
                    modifier = Modifier.testTag("admin_banner_toggle_${banner.id}")
                )
            }

            // Compact Action Bar: Move Up, Move Down, Replace (Gallery / Preset), Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(JummoCardPurple)
                            .border(0.75.dp, JummoBorderSubtle, RoundedCornerShape(8.dp))
                            .testTag("admin_banner_move_up_${banner.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Move Banner Up",
                            tint = if (index > 0) Color.White else JummoTextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(JummoCardPurple)
                            .border(0.75.dp, JummoBorderSubtle, RoundedCornerShape(8.dp))
                            .testTag("admin_banner_move_down_${banner.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Move Banner Down",
                            tint = if (index < totalCount - 1) Color.White else JummoTextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onReplaceGallery() }
                            .testTag("admin_banner_replace_gallery_${banner.id}"),
                        color = JummoCardPurple,
                        border = BorderStroke(0.75.dp, JummoBorderSubtle),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FindReplace,
                                contentDescription = "Replace from Gallery",
                                tint = JummoVioletBright,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Replace",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onReplaceStudioPreset() }
                            .testTag("admin_banner_replace_${banner.id}"),
                        color = JummoElevatedPurple,
                        border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Swap Shot",
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoGoldBright,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(JummoErrorRed.copy(alpha = 0.16f))
                            .border(0.75.dp, JummoErrorRed.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .testTag("admin_banner_delete_${banner.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Banner",
                            tint = JummoErrorRed,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
