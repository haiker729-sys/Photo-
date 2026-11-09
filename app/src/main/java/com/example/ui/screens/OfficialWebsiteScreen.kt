package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.components.JummoLuxuryCameraLogo
import com.example.ui.components.StatusBadgePill
import com.example.ui.theme.JummoBorderGlow
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoElevatedPurple
import com.example.ui.theme.JummoErrorRed
import com.example.ui.theme.JummoGold
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoGoldSoft
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoSuccessGreen
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import com.example.ui.theme.JummoVioletPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class VerifiedGitHubReleaseInfo(
    val checked: Boolean = false,
    val available: Boolean = false,
    val versionTag: String = "Checking GitHub Releases…",
    val fileSizeLabel: String = "Verifying asset…",
    val updatedDateLabel: String = "Checking date…",
    val downloadUrl: String? = null,
    val releasePageUrl: String = "https://github.com/haiker729-sys/Photo-/releases",
    val repoUrl: String = "https://github.com/haiker729-sys/Photo-",
    val diagnosticMessage: String? = null
)

private data class WebsitePreviewScreenItem(
    val number: String,
    val title: String,
    val badge: String,
    val description: String,
    val drawableRes: Int
)

private data class WebsiteFeatureSpec(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private data class WebsiteFaqItem(
    val question: String,
    val answer: String
)

suspend fun fetchVerifiedGitHubRelease(): VerifiedGitHubReleaseInfo = withContext(Dispatchers.IO) {
    val repoUrl = "https://github.com/haiker729-sys/Photo-"
    val releasesPage = "$repoUrl/releases"
    val latestApi = "https://api.github.com/repos/haiker729-sys/Photo-/releases/latest"
    val allReleasesApi = "https://api.github.com/repos/haiker729-sys/Photo-/releases"

    fun parseReleaseObject(obj: JSONObject): VerifiedGitHubReleaseInfo? {
        val assets = obj.optJSONArray("assets") ?: return null
        for (i in 0 until assets.length()) {
            val asset = assets.optJSONObject(i) ?: continue
            val name = asset.optString("name", "")
            val downloadUrl = asset.optString("browser_download_url", "")
            if (name.lowercase(Locale.US).endsWith(".apk") && downloadUrl.isNotBlank()) {
                val sizeBytes = asset.optLong("size", 0L)
                val sizeMb = if (sizeBytes > 0L) {
                    String.format(Locale.US, "%.2f MB", sizeBytes / (1024.0 * 1024.0))
                } else {
                    "Verified APK"
                }
                val rawDate = asset.optString("updated_at", obj.optString("published_at", ""))
                val shortDate = if (rawDate.length >= 10) rawDate.take(10) else "Published"
                val tag = obj.optString("tag_name", "v1.0.0").ifBlank { "v1.0.0" }
                return VerifiedGitHubReleaseInfo(
                    checked = true,
                    available = true,
                    versionTag = tag,
                    fileSizeLabel = sizeMb,
                    updatedDateLabel = shortDate,
                    downloadUrl = downloadUrl,
                    releasePageUrl = obj.optString("html_url", releasesPage),
                    repoUrl = repoUrl,
                    diagnosticMessage = null
                )
            }
        }
        return null
    }

    try {
        // 1. Try /releases/latest first
        val latestConn = (URL(latestApi).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 6000
            readTimeout = 6000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "JummoPhotosAI-OfficialWebsite")
        }
        if (latestConn.responseCode in 200..299) {
            val body = latestConn.inputStream.bufferedReader().use { it.readText() }
            val parsed = parseReleaseObject(JSONObject(body))
            if (parsed != null) return@withContext parsed
        }

        // 2. Fallback to /releases list
        val listConn = (URL(allReleasesApi).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 6000
            readTimeout = 6000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "JummoPhotosAI-OfficialWebsite")
        }
        if (listConn.responseCode in 200..299) {
            val body = listConn.inputStream.bufferedReader().use { it.readText() }
            val arr = JSONArray(body)
            for (i in 0 until arr.length()) {
                val rel = arr.optJSONObject(i) ?: continue
                val parsed = parseReleaseObject(rel)
                if (parsed != null) return@withContext parsed
            }
        }

        VerifiedGitHubReleaseInfo(
            checked = true,
            available = false,
            versionTag = "APK release is being prepared.",
            fileSizeLabel = "Pending release asset",
            updatedDateLabel = "Pending release run",
            downloadUrl = null,
            releasePageUrl = releasesPage,
            repoUrl = repoUrl,
            diagnosticMessage = "APK release is being prepared. Push to main on https://github.com/haiker729-sys/Photo- to automatically build and publish the latest signed APK to GitHub Releases."
        )
    } catch (e: Exception) {
        VerifiedGitHubReleaseInfo(
            checked = true,
            available = false,
            versionTag = "APK release is being prepared.",
            fileSizeLabel = "Check GitHub Releases",
            updatedDateLabel = "Network / Rate Limit",
            downloadUrl = null,
            releasePageUrl = releasesPage,
            repoUrl = repoUrl,
            diagnosticMessage = "APK release is being prepared. Open https://github.com/haiker729-sys/Photo-/releases directly to inspect published APK assets."
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OfficialWebsiteScreen(
    onClose: () -> Unit,
    enableLiveNetworkCheck: Boolean = true
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var releaseInfo by remember {
        mutableStateOf(
            if (enableLiveNetworkCheck) {
                VerifiedGitHubReleaseInfo()
            } else {
                VerifiedGitHubReleaseInfo(
                    checked = true,
                    available = false,
                    versionTag = "APK release is being prepared.",
                    fileSizeLabel = "Pending release asset",
                    updatedDateLabel = "Pending release run",
                    diagnosticMessage = "APK release is being prepared."
                )
            }
        )
    }
    var showDownloadErrorAlert by remember { mutableStateOf(false) }
    var useEmbeddedHtmlMode by remember { mutableStateOf(false) }
    var selectedScreenPreviewIndex by remember { mutableIntStateOf(0) }
    var selectedWorkloadTier by remember { mutableStateOf("Standard Studio") }
    var expandedFaqIndex by remember { mutableIntStateOf(0) }
    var activeLegalModal by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(enableLiveNetworkCheck) {
        if (enableLiveNetworkCheck) {
            releaseInfo = fetchVerifiedGitHubRelease()
        }
    }

    val previewScreens = remember {
        listOf(
            WebsitePreviewScreenItem(
                number = "Screen 01",
                title = "Login Screen",
                badge = "Google Auth + 5 Categories + 4 Feature Rows",
                description = "Cinematic DSLR & bokeh hero, golden camera lens emblem, 5 event categories (Weddings, Festivals, Corporate, Schools, Live Events), 4 purple glass feature rows, and official Google Sign-In.",
                drawableRes = R.drawable.img_hero_studio
            ),
            WebsitePreviewScreenItem(
                number = "Screen 02",
                title = "Home Dashboard",
                badge = "Live 3+2 Stats Grid • Drive Status • Recent Events",
                description = "Displays real-time counts for Events, Photos, Uploaded, Pending, and Failed items, side-by-side + Create Event and Camera actions, Google Drive sync indicator, and Recent Events.",
                drawableRes = R.drawable.img_wedding_cover
            ),
            WebsitePreviewScreenItem(
                number = "Screen 03",
                title = "Events",
                badge = "My Events • Category Pills • Active/Archived",
                description = "Manage Wedding, Birthday, Festival, School, and Corporate events with instant search, category filter pills, Active/Archived tabs, and 1-tap Gallery, QR, Edit, Archive, and Delete actions.",
                drawableRes = R.drawable.img_wedding_cover
            ),
            WebsitePreviewScreenItem(
                number = "Screen 04",
                title = "Camera",
                badge = "CameraX Pro Viewfinder • HDR • 4K • Event Pill",
                description = "Full-screen CameraX viewfinder with Flash, HDR, 4K, and rule-of-thirds grid controls, floating active event selector pill, front/rear lens switching, and local original preservation.",
                drawableRes = R.drawable.img_wedding_cover
            ),
            WebsitePreviewScreenItem(
                number = "Screen 05",
                title = "Upload Manager",
                badge = "WorkManager Queue • Wi-Fi & Battery Guard • DSLR Import",
                description = "Monitor Queue, Uploaded, and Failed transfers with percentage progress bars, toggle Automatic Upload with Wi-Fi Only and Battery > 20% protection, or import DSLR RAW/JPG files.",
                drawableRes = R.drawable.img_festival_cover
            ),
            WebsitePreviewScreenItem(
                number = "Screen 06",
                title = "Profile",
                badge = "Photographer Pro • Drive Bridge • Privacy & Storage",
                description = "Configure Studio Display Name, Google Drive root folder, Upload Preferences, Local Original Storage metrics, Guest Face-Search Consent privacy sessions, and Camera permissions.",
                drawableRes = R.drawable.img_hero_studio
            ),
            WebsitePreviewScreenItem(
                number = "Screen 07",
                title = "Photo Gallery",
                badge = "All Photos • People (AI) • Highlights • Multi-Select",
                description = "3-column event photo gallery with All Photos, People (AI), and Highlights tabs, multi-select batch actions, QR sharing, and consent-first Guest AI Face Search.",
                drawableRes = R.drawable.img_wedding_cover
            ),
            WebsitePreviewScreenItem(
                number = "Screen 08",
                title = "Admin Dashboard",
                badge = "2x2 KPIs • 7-Day Upload Chart • 14 Admin Modules",
                description = "Role-based command center (Super Admin, Event Admin, Support Admin, Finance/Storage Admin) with 2x2 KPI cards, dual-tone storage bar, 7-day upload chart, and 14 governance modules.",
                drawableRes = R.drawable.img_festival_cover
            )
        )
    }

    val implementedFeatures = remember {
        listOf(
            WebsiteFeatureSpec(
                icon = Icons.Default.CameraAlt,
                title = "Professional Camera",
                description = "CameraX viewfinder with Flash, HDR, 4K framing, rule-of-thirds grid, front/rear lens switch, and active event binding."
            ),
            WebsiteFeatureSpec(
                icon = Icons.Default.SdCard,
                title = "DSLR / RAW Import",
                description = "Zero-permission Photo Picker & SAF to import RAW, DNG, CR2, NEF, ARW, PNG, and JPG from SD cards or camera storage."
            ),
            WebsiteFeatureSpec(
                icon = Icons.Default.CloudUpload,
                title = "Automatic Photo Upload",
                description = "Resilient WorkManager background queue with configurable Wi-Fi Only and Battery > 20% protection."
            ),
            WebsiteFeatureSpec(
                icon = Icons.Default.CloudDone,
                title = "Google Drive Integration",
                description = "Connect your studio root folder (Jummo_Events_Cloud) via a server-side OAuth bridge with zero hardcoded APK secrets."
            ),
            WebsiteFeatureSpec(
                icon = Icons.Default.QrCode2,
                title = "Event QR Generation",
                description = "Deterministic branded QR code generator with gallery download, native Share sheet, expiration dates, and pause toggle."
            ),
            WebsiteFeatureSpec(
                icon = Icons.Default.Face,
                title = "AI Face Search",
                description = "Consent-first guest selfie matching with strict per-event isolation, mismatch reporting, and instant face-data purge."
            ),
            WebsiteFeatureSpec(
                icon = Icons.Default.PhotoLibrary,
                title = "Guest Photo Gallery",
                description = "3-column event gallery with All Photos, People (AI), and Highlights tabs plus configurable guest access modes."
            ),
            WebsiteFeatureSpec(
                icon = Icons.Default.Lock,
                title = "Secure Photo Download",
                description = "Original photos are preserved locally in filesDir/photos and never auto-deleted; guest downloads obey studio rules."
            )
        )
    }

    val faqItems = remember {
        listOf(
            WebsiteFaqItem(
                question = "How to install the APK?",
                answer = "Tap DOWNLOAD JUMMO PHOTOS AI to download the verified .apk asset from the official GitHub Releases page (github.com/haiker729-sys/Photo-/releases). Open the APK on Android 7.0+, allow installation from your browser if prompted, and launch the app."
            ),
            WebsiteFaqItem(
                question = "How to create an event?",
                answer = "After signing in with Google, tap + Create Event on the Home Dashboard or + New Event on the My Events tab. Enter the event name, choose a category (Wedding, Birthday, Festival, School, Corporate, Other), set the date, venue, cover photo, and guest access mode."
            ),
            WebsiteFaqItem(
                question = "How does QR sharing work?",
                answer = "Every event generates a deterministic branded QR code card. You can download it to your device gallery, share it via WhatsApp or email, set an expiration date, or pause/resume guest QR access at any time."
            ),
            WebsiteFaqItem(
                question = "How does Google Drive work?",
                answer = "In Profile & Settings -> Google Drive, enable Drive sync and set your root folder name (default: Jummo_Events_Cloud). It uses a server-side OAuth bridge so no sensitive OAuth client secret is embedded in the APK."
            ),
            WebsiteFaqItem(
                question = "How does AI Face Search work?",
                answer = "Guests scanning an event QR code must check and submit an explicit biometric consent agreement before running a selfie face search. Results are isolated per Event ID, and face sessions can be purged at any time."
            ),
            WebsiteFaqItem(
                question = "Are original photos preserved?",
                answer = "Yes. Every photo captured with CameraX or imported into an event is stored in local app storage (filesDir/photos) and is never automatically deleted after cloud upload."
            )
        )
    }

    fun openExternalUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun handleApkDownloadClick() {
        val verifiedUrl = releaseInfo.downloadUrl
        if (releaseInfo.available && !verifiedUrl.isNullOrBlank()) {
            openExternalUrl(verifiedUrl)
        } else {
            showDownloadErrorAlert = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JummoObsidian)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("official_website_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sticky Website Top Navigation Bar
            Surface(
                color = JummoObsidian.copy(alpha = 0.94f),
                border = BorderStroke(0.75.dp, JummoBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("close_official_website_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to App",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        JummoLuxuryCameraLogo(size = 32.dp, showCrown = false)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Jummo Photos AI",
                                style = MaterialTheme.typography.titleSmall,
                                color = JummoGoldBright,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Official Website & APK Release Portal",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { useEmbeddedHtmlMode = !useEmbeddedHtmlMode }
                                .testTag("toggle_web_engine_button"),
                            color = JummoCardPurple,
                            border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.55f)),
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = JummoGoldBright,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (useEmbeddedHtmlMode) "Native View" else "HTML5 View",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = { openExternalUrl(releaseInfo.repoUrl) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open GitHub Repository",
                                tint = JummoGoldBright,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (useEmbeddedHtmlMode) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = true
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString().orEmpty()
                                    if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("mailto:")) {
                                        openExternalUrl(url)
                                        return true
                                    }
                                    return false
                                }
                            }
                            loadUrl("file:///android_asset/website/index.html")
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .widthIn(max = 900.dp)
                        .align(Alignment.CenterHorizontally)
                        .testTag("official_website_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // 1. HERO SECTION
                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                            border = BorderStroke(1.2.dp, JummoGoldBright.copy(alpha = 0.55f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_hero_studio),
                                    contentDescription = "Jummo Photos AI DSLR & Wedding Photography Hero",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    JummoObsidian.copy(alpha = 0.35f),
                                                    JummoDeepPurple.copy(alpha = 0.78f),
                                                    JummoCardPurple
                                                )
                                            )
                                        )
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = JummoObsidian.copy(alpha = 0.76f),
                                        border = BorderStroke(0.85.dp, JummoGoldBright.copy(alpha = 0.65f)),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = JummoGoldBright,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "SMART EVENT PHOTOGRAPHY SUITE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = JummoGoldBright,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(64.dp))

                                    Text(
                                        text = "Jummo Photos AI",
                                        style = MaterialTheme.typography.displayMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Luxury Android Event Photography, DSLR RAW/JPG Import, WorkManager Cloud Queue, Google Drive Sync, Branded Event QR Codes, and Consent-First AI Face Search.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = JummoTextSecondary
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { handleApkDownloadClick() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = JummoGoldBright,
                                                contentColor = JummoObsidian
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .height(46.dp)
                                                .testTag("hero_download_apk_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = null,
                                                modifier = Modifier.size(17.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Download APK",
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = { openExternalUrl(releaseInfo.repoUrl) },
                                            border = BorderStroke(1.dp, JummoBorderGlow),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(46.dp)
                                        ) {
                                            Text(
                                                text = "Explore Features",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. APK DOWNLOAD SYSTEM
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                            border = BorderStroke(1.2.dp, JummoGoldBright.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("website_apk_download_section")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusBadgePill(
                                        text = if (releaseInfo.available) {
                                            "VERIFIED GITHUB RELEASE (${releaseInfo.versionTag})"
                                        } else {
                                            "RELEASE VERIFICATION STATUS"
                                        },
                                        color = if (releaseInfo.available) JummoSuccessGreen else JummoGoldBright
                                    )

                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                releaseInfo = fetchVerifiedGitHubRelease()
                                            }
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Re-verify GitHub Release",
                                            tint = JummoGoldBright,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Official Android APK Release Hub",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Official Repository: https://github.com/haiker729-sys/Photo-",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JummoGoldSoft
                                )

                                // 2x2 Metadata Grid: Version, Verified Size, Last Updated, Compatibility
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    WebsiteMetaBox(
                                        label = "APK Version",
                                        value = releaseInfo.versionTag,
                                        modifier = Modifier.weight(1f)
                                    )
                                    WebsiteMetaBox(
                                        label = "Verified File Size",
                                        value = releaseInfo.fileSizeLabel,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    WebsiteMetaBox(
                                        label = "Last Updated",
                                        value = releaseInfo.updatedDateLabel,
                                        modifier = Modifier.weight(1f)
                                    )
                                    WebsiteMetaBox(
                                        label = "Compatibility",
                                        value = "Android 7.0+ (API 24–36)",
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Prominent Button: "DOWNLOAD JUMMO PHOTOS AI"
                                Button(
                                    onClick = { handleApkDownloadClick() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = JummoGoldBright,
                                        contentColor = JummoObsidian
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("download_jummo_photos_ai_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "DOWNLOAD JUMMO PHOTOS AI",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    if (showDownloadErrorAlert || (!releaseInfo.available && releaseInfo.checked)) {
                        item {
                            Surface(
                                color = JummoErrorRed.copy(alpha = 0.16f),
                                border = BorderStroke(0.85.dp, JummoErrorRed.copy(alpha = 0.65f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("apk_unavailable_notice")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = JummoErrorRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "APK Release Status Notice",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = releaseInfo.diagnosticMessage
                                                ?: "No published .apk asset was found yet in https://github.com/haiker729-sys/Photo-/releases.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFFECACA)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Installation Instructions
                    item {
                        Surface(
                            color = JummoDeepPurple.copy(alpha = 0.85f),
                            border = BorderStroke(0.75.dp, JummoBorderSubtle),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Android Installation Instructions",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = JummoGoldBright,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "1. Tap DOWNLOAD JUMMO PHOTOS AI to fetch the signed APK from GitHub Releases.\n2. Open the downloaded .apk from your browser or Downloads folder.\n3. Enable 'Install unknown apps' if prompted by Android.\n4. Launch Jummo Photos AI and sign in with your Google account.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JummoTextSecondary
                                )
                            }
                        }
                    }

                    // 3. FEATURES SECTION (8 Implemented Features)
                    item {
                        Text(
                            text = "Implemented Studio Features",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    implementedFeatures.chunked(2).forEach { pair ->
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { feat ->
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                                        border = BorderStroke(0.85.dp, JummoBorderSubtle),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(JummoElevatedPurple)
                                                    .border(0.75.dp, JummoBorderGlow, RoundedCornerShape(10.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = feat.icon,
                                                    contentDescription = feat.title,
                                                    tint = JummoGoldBright,
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }
                                            Text(
                                                text = feat.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = feat.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = JummoTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. APP PREVIEW GALLERY (8 Screens)
                    item {
                        val activePreview = previewScreens[selectedScreenPreviewIndex]
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                            border = BorderStroke(0.9.dp, JummoBorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "App Preview Gallery (8 Screens)",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    previewScreens.forEachIndexed { idx, item ->
                                        FilterChip(
                                            selected = selectedScreenPreviewIndex == idx,
                                            onClick = { selectedScreenPreviewIndex = idx },
                                            label = { Text(item.title) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = JummoGoldBright,
                                                selectedLabelColor = JummoObsidian
                                            )
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(1.dp, JummoGoldBright.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = activePreview.drawableRes),
                                        contentDescription = activePreview.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        JummoObsidian.copy(alpha = 0.90f)
                                                    )
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "${activePreview.number} • ${activePreview.title}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = activePreview.badge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = JummoGoldBright,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text(
                                    text = activePreview.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JummoTextSecondary
                                )
                            }
                        }
                    }

                    // 5. HOW IT WORKS (6 Steps)
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                            border = BorderStroke(0.85.dp, JummoBorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "How It Works",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                val steps = listOf(
                                    "1. Download the Android App" to "Install the signed APK from the official GitHub Releases page.",
                                    "2. Sign in with Google" to "Authenticate your studio workspace via Firebase Authentication.",
                                    "3. Create an Event" to "Configure event name, category, date, venue, cover photo, and QR rules.",
                                    "4. Capture or Import Photos" to "Shoot with CameraX or import RAW/JPG photos from DSLR SD cards.",
                                    "5. Generate Event QR" to "Download or share your branded Event QR card with expiration controls.",
                                    "6. Let Guests Find Authorized Photos" to "Guests scan the QR code and find photos with explicit face-search consent."
                                )
                                steps.forEach { (stepTitle, stepDesc) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = JummoGoldBright,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Column {
                                            Text(
                                                text = stepTitle,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = stepDesc,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = JummoTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. PRICING (Configurable Studio Plans — No Fake Prices)
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                            border = BorderStroke(0.85.dp, JummoBorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Configurable Studio Plans",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Connects directly to your own Firebase and Google Drive workspace. No in-app payment gateway or artificial paywall.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JummoTextSecondary
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Personal Photographer", "Pro Studio Suite", "Multi-Photographer Agency").forEach { tier ->
                                        FilterChip(
                                            selected = selectedWorkloadTier == tier,
                                            onClick = { selectedWorkloadTier = tier },
                                            label = { Text(tier) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = JummoVioletPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    color = JummoDeepPurple,
                                    border = BorderStroke(1.dp, JummoGoldBright.copy(alpha = 0.55f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = selectedWorkloadTier,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = JummoGoldBright,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = when (selectedWorkloadTier) {
                                                "Personal Photographer" -> "Single photographer workspace • CameraX + SAF Import • Branded Event QR Codes • Local Originals Kept Safe"
                                                "Multi-Photographer Agency" -> "Multi-role Admin Dashboard (Super Admin, Event Admin, Support Admin, Storage Admin) • 14 Governance Modules • 7-Day Telemetry"
                                                else -> "Unlimited Active Events • Full DSLR RAW/JPG Queue • Google Drive Folder Sync • Consent-First Guest AI Face Search"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 7. FAQ SECTION
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                            border = BorderStroke(0.85.dp, JummoBorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Frequently Asked Questions",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                faqItems.forEachIndexed { idx, faq ->
                                    val expanded = expandedFaqIndex == idx
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                expandedFaqIndex = if (expanded) -1 else idx
                                            },
                                        color = JummoDeepPurple,
                                        border = BorderStroke(0.75.dp, JummoBorderSubtle),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = faq.question,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Icon(
                                                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = null,
                                                    tint = JummoGoldBright
                                                )
                                            }
                                            if (expanded) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = faq.answer,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = JummoTextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 8. FOOTER
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = JummoDeepPurple),
                            border = BorderStroke(0.85.dp, JummoBorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("website_footer_section")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    JummoLuxuryCameraLogo(size = 28.dp, showCrown = false)
                                    Text(
                                        text = "Jummo Photos AI — Official Website",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = JummoGoldBright,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = { activeLegalModal = "About" }) {
                                        Text("About", color = Color.White)
                                    }
                                    TextButton(onClick = { openExternalUrl("mailto:merajalam906090@gmail.com") }) {
                                        Text("Contact", color = Color.White)
                                    }
                                    TextButton(onClick = { activeLegalModal = "Privacy Policy" }) {
                                        Text("Privacy Policy", color = Color.White)
                                    }
                                    TextButton(onClick = { activeLegalModal = "Terms of Service" }) {
                                        Text("Terms of Service", color = Color.White)
                                    }
                                }

                                HorizontalDivider(color = JummoBorderSubtle)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { handleApkDownloadClick() },
                                        border = BorderStroke(0.75.dp, JummoGoldBright),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            tint = JummoGoldBright,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Download App", color = JummoGoldBright, maxLines = 1)
                                    }

                                    OutlinedButton(
                                        onClick = { openExternalUrl(releaseInfo.repoUrl) },
                                        border = BorderStroke(0.75.dp, JummoVioletBright),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Code,
                                            contentDescription = null,
                                            tint = JummoVioletBright,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Official GitHub Repo", color = Color.White, maxLines = 1)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = JummoGoldBright,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "github.com/haiker729-sys/Photo-",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JummoTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeLegalModal != null) {
        val modalTitle = activeLegalModal!!
        AlertDialog(
            onDismissRequest = { activeLegalModal = null },
            containerColor = JummoCardPurple,
            title = {
                Text(
                    text = "$modalTitle — Jummo Photos AI",
                    color = JummoGoldBright,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = when (modalTitle) {
                        "Privacy Policy" -> "1. Explicit Guest Consent is required before any AI Face Search session is created.\n2. All events, photos, and guest sessions are isolated by Event ID in Cloud Firestore.\n3. Guests and studio owners can purge guest face-search sessions at any time.\n4. Original photos in filesDir/photos are never automatically deleted."
                        "Terms of Service" -> "1. Studio photographers retain full ownership over all captured and imported event photos.\n2. Branded Event QR codes obey photographer-configured expiration dates and access modes.\n3. Google Authentication is secured by Firebase Authentication."
                        else -> "Jummo Photos AI (Smart Event Photography Suite) is an international-grade Event Photography, DSLR Import, WorkManager Cloud Upload, Branded QR Sharing, and Consent-First AI Face Search platform."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = JummoTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { activeLegalModal = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoGoldBright,
                        contentColor = JummoObsidian
                    )
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun WebsiteMetaBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = JummoDeepPurple,
        border = BorderStroke(0.75.dp, JummoBorderSubtle),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = JummoTextSecondary,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
