package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JummoAccent
import com.example.ui.theme.JummoBackground
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoButtonText
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoErrorRed
import com.example.ui.theme.JummoPrimary
import com.example.ui.theme.JummoSecondary
import com.example.ui.theme.JummoSuccessGreen
import com.example.ui.theme.JummoSurface
import com.example.ui.theme.JummoTextMuted
import com.example.ui.theme.JummoTextPrimary
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import com.example.ui.theme.JummoVioletPrimary
import com.example.ui.theme.JummoWarningAmber

/**
 * Clean Purple Premium Camera Emblem for Jummo Photos AI.
 */
@Composable
fun JummoLuxuryCameraLogo(
    size: Dp = 44.dp,
    showCrown: Boolean = true,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    JummoAccent.copy(alpha = 0.28f),
                    Color.Transparent
                )
            ),
            radius = w * 0.54f,
            center = Offset(w * 0.5f, h * 0.52f)
        )

        if (showCrown) {
            val crownPath = Path().apply {
                moveTo(w * 0.34f, h * 0.23f)
                lineTo(w * 0.30f, h * 0.11f)
                lineTo(w * 0.42f, h * 0.17f)
                lineTo(w * 0.50f, h * 0.07f)
                lineTo(w * 0.58f, h * 0.17f)
                lineTo(w * 0.70f, h * 0.11f)
                lineTo(w * 0.66f, h * 0.23f)
                close()
            }
            drawPath(
                path = crownPath,
                brush = Brush.verticalGradient(
                    colors = listOf(JummoAccent, JummoPrimary)
                )
            )
        }

        val bodyTop = if (showCrown) h * 0.27f else h * 0.18f
        val bodyHeight = h * 0.62f

        // Camera top viewfinder hump
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(JummoPrimary, JummoAccent, JummoPrimary)
            ),
            topLeft = Offset(w * 0.33f, bodyTop - h * 0.06f),
            size = Size(w * 0.34f, h * 0.10f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
        )

        // Camera body (#5B21B6 -> #8B5CF6)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    JummoAccent,
                    JummoPrimary
                )
            ),
            topLeft = Offset(w * 0.10f, bodyTop),
            size = Size(w * 0.80f, bodyHeight),
            cornerRadius = CornerRadius(w * 0.18f, w * 0.18f)
        )

        // Soft lavender inner body inset (#EDE9FE)
        val insetPad = w * 0.055f
        drawRoundRect(
            color = JummoSurface,
            topLeft = Offset(w * 0.10f + insetPad, bodyTop + insetPad),
            size = Size(w * 0.80f - insetPad * 2, bodyHeight - insetPad * 2),
            cornerRadius = CornerRadius(w * 0.13f, w * 0.13f)
        )

        val lensCenter = Offset(w * 0.50f, bodyTop + bodyHeight * 0.52f)
        val outerLensRadius = w * 0.26f

        // Outer lens ring
        drawCircle(
            color = JummoPrimary,
            radius = outerLensRadius,
            center = lensCenter
        )

        // Optical glass center
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    JummoSecondary,
                    JummoAccent,
                    JummoPrimary
                ),
                center = Offset(lensCenter.x - outerLensRadius * 0.2f, lensCenter.y - outerLensRadius * 0.2f),
                radius = outerLensRadius * 0.9f
            ),
            radius = outerLensRadius * 0.78f,
            center = lensCenter
        )

        // Lens reflection dot
        drawCircle(
            color = JummoButtonText.copy(alpha = 0.85f),
            radius = outerLensRadius * 0.18f,
            center = Offset(lensCenter.x - outerLensRadius * 0.25f, lensCenter.y - outerLensRadius * 0.25f)
        )

        // Top-right indicator dot
        drawCircle(
            color = JummoAccent,
            radius = w * 0.035f,
            center = Offset(w * 0.76f, bodyTop + bodyHeight * 0.22f)
        )
    }
}

/**
 * Clean Bottom Navigation Bar with 5 tabs: Home, Events, Camera, Uploads, Profile.
 */
@Composable
fun JummoLuxuryBottomBar(
    currentTab: Int,
    onSelectTab: (Int) -> Unit
) {
    Surface(
        color = JummoBackground,
        border = BorderStroke(1.dp, JummoBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LuxuryNavTabPill(
                label = "Home",
                selected = currentTab == 0,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                testTag = "nav_tab_home",
                onClick = { onSelectTab(0) }
            )

            LuxuryNavTabPill(
                label = "Events",
                selected = currentTab == 1,
                selectedIcon = Icons.Filled.Event,
                unselectedIcon = Icons.Outlined.Event,
                testTag = "nav_tab_events",
                onClick = { onSelectTab(1) }
            )

            // Center Circular Camera Button (#5B21B6 Primary + #8B5CF6 Accent)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                JummoAccent,
                                JummoPrimary
                            )
                        )
                    )
                    .border(
                        width = if (currentTab == 2) 2.5.dp else 1.5.dp,
                        color = if (currentTab == 2) JummoPrimary else JummoSecondary,
                        shape = CircleShape
                    )
                    .clickable { onSelectTab(2) }
                    .testTag("nav_tab_camera"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Camera",
                    tint = JummoButtonText,
                    modifier = Modifier.size(22.dp)
                )
            }

            LuxuryNavTabPill(
                label = "Uploads",
                selected = currentTab == 3,
                selectedIcon = Icons.Filled.CloudUpload,
                unselectedIcon = Icons.Outlined.CloudUpload,
                testTag = "nav_tab_uploads",
                onClick = { onSelectTab(3) }
            )

            LuxuryNavTabPill(
                label = "Profile",
                selected = currentTab == 4,
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                testTag = "nav_tab_profile",
                onClick = { onSelectTab(4) }
            )
        }
    }
}

@Composable
private fun LuxuryNavTabPill(
    label: String,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    val bgBrush = if (selected) {
        Brush.verticalGradient(
            colors = listOf(JummoPrimary, JummoAccent)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgBrush)
            .then(
                if (selected) {
                    Modifier.border(0.75.dp, JummoSecondary, RoundedCornerShape(14.dp))
                } else {
                    Modifier
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 5.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = if (selected) JummoButtonText else JummoTextSecondary,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) JummoButtonText else JummoTextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

/**
 * Compact statistic card using #EDE9FE surface and #24133F primary text.
 */
@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String = "",
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = JummoSurface),
        border = BorderStroke(0.85.dp, JummoBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.14f))
                    .border(0.5.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = JummoTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = JummoTextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = JummoTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Single-line status badge pill.
 */
@Composable
fun StatusBadgePill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(0.75.dp, color.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SectionHeaderRow(
    title: String,
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = JummoTextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = JummoTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    message: String,
    actionButton: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(0.85.dp, JummoBorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(JummoPrimary, JummoAccent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = JummoButtonText,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = JummoTextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = JummoTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
            if (actionButton != null) {
                Spacer(modifier = Modifier.height(10.dp))
                actionButton()
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    accentColor: Color = JummoVioletBright,
    trailingBadge: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.14f))
                    .border(0.5.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = JummoTextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (trailingBadge != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingBadge()
        }
    }
}

@Composable
fun SettingsInfoRow(
    label: String,
    value: String,
    valueColor: Color = JummoTextPrimary,
    showDivider: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = JummoTextSecondary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = valueColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (showDivider) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = JummoBorderSubtle.copy(alpha = 0.7f)
            )
        }
    }
}

fun statusColorForUpload(status: String): Color = when (status) {
    "Uploaded" -> JummoSuccessGreen
    "Uploading" -> JummoAccent
    "Pending" -> JummoWarningAmber
    "Failed" -> JummoErrorRed
    else -> JummoPrimary
}

@Composable
fun JummoGoldPrimaryButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        color = if (enabled) JummoPrimary else JummoPrimary.copy(alpha = 0.45f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = JummoButtonText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = JummoButtonText,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun JummoPurpleSecondaryButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        color = JummoAccent,
        border = BorderStroke(0.75.dp, JummoSecondary),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = JummoButtonText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = JummoButtonText,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
