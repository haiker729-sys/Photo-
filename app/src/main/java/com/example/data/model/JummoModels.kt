package com.example.data.model

import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@IgnoreExtraProperties
data class PhotographerProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val studioName: String = "Jummo Pro Studio",
    val role: String = "photographer",
    val driveConnected: Boolean = false,
    val driveFolderName: String = "Jummo_Events_Cloud",
    val autoUploadEnabled: Boolean = true,
    val wifiOnlyUpload: Boolean = true,
    val batteryNotLowUpload: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PhotoEvent(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val category: String = "Wedding",
    val eventDate: String = "",
    val location: String = "",
    val coverType: String = "wedding",
    val coverUri: String = "",
    val status: String = "Active",
    val qrCodeValue: String = "",
    val qrEnabled: Boolean = true,
    val qrExpirationDate: String = "Never",
    val guestAccessMode: String = "FaceSearchOnly",
    val guestAllowDownload: Boolean = true,
    val totalPhotos: Long = 0L,
    val uploadedPhotos: Long = 0L,
    val pendingPhotos: Long = 0L,
    val failedPhotos: Long = 0L,
    val driveSyncStatus: String = "NotConnected",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    @get:Exclude
    val coverImageUri: String
        get() = coverUri
}

@IgnoreExtraProperties
data class PhotoUploadRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val eventId: String = "",
    val eventName: String = "",
    val fileName: String = "",
    val localUri: String = "",
    val sourceType: String = "MobileCamera",
    val uploadMode: String = "Automatic",
    val status: String = "Pending",
    val progress: Long = 0L,
    val fileSizeBytes: Long = 0L,
    val mimeType: String = "image/jpeg",
    val faceIndexStatus: String = "AwaitingBackend",
    val driveFileId: String = "",
    val errorMessage: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class GuestFaceSearchSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val eventId: String = "",
    val guestLabel: String = "",
    val consentGranted: Boolean = true,
    val selfieUri: String = "",
    val matchStatus: String = "ConsentVerified",
    val matchedPhotoCount: Long = 0L,
    val reportNotes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class HomeBanner(
    @DocumentId val id: String = "",
    val userId: String = "",
    val imageUri: String = "",
    val storagePath: String = "",
    val displayOrder: Long = 0L,
    val isActive: Boolean = true,
    val slideDurationSeconds: Long = 4L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

object BannerPresets {
    const val PRESET_WEDDING = "preset://wedding_cover"
    const val PRESET_FESTIVAL = "preset://festival_cover"
    const val PRESET_STUDIO = "preset://hero_studio"
    const val PRESET_LOGIN_HERO = "preset://login_hero"
    const val PRESET_CORPORATE = "preset://hero_studio"

    val ALL = listOf(PRESET_WEDDING, PRESET_FESTIVAL, PRESET_STUDIO, PRESET_LOGIN_HERO)

    fun forCategory(category: String): String = when (category.lowercase()) {
        "festival" -> PRESET_FESTIVAL
        "corporate", "school" -> PRESET_STUDIO
        else -> PRESET_WEDDING
    }

    fun drawableResForUri(uri: String): Int? = when (uri) {
        PRESET_WEDDING -> R.drawable.img_wedding_cover
        PRESET_FESTIVAL -> R.drawable.img_festival_cover
        PRESET_STUDIO -> R.drawable.img_hero_studio
        PRESET_LOGIN_HERO -> R.drawable.img_hero_studio
        else -> null
    }
}

object EventCategories {
    val ALL = listOf("Wedding", "Birthday", "Festival", "School", "Corporate", "Other")
}

object GuestAccessModes {
    const val FACE_SEARCH_ONLY = "FaceSearchOnly"
    const val VIEW_ALL_PERMITTED = "ViewAllPermitted"
    const val PIN_PROTECTED = "PINProtected"
    const val GALLERY_ONLY = "GalleryOnly"

    val ALL = listOf(FACE_SEARCH_ONLY, VIEW_ALL_PERMITTED, PIN_PROTECTED, GALLERY_ONLY)

    fun displayLabel(mode: String): String = when (mode) {
        FACE_SEARCH_ONLY -> "Consent Face Search Only"
        VIEW_ALL_PERMITTED -> "View All + Face Search"
        PIN_PROTECTED -> "Organizer Approved Access"
        GALLERY_ONLY -> "Gallery Only (No Face Search)"
        else -> mode
    }
}

data class WebsiteThemePaletteSpec(
    val backgroundHex: String,
    val surfaceHex: String,
    val cardHex: String,
    val primaryAccentHex: String,
    val secondaryHighlightHex: String
)

object WebsiteThemes {
    const val ROYAL_PURPLE = "RoyalPurple"
    const val MIDNIGHT_BLACK = "MidnightBlack"
    const val CHAMPAGNE_GOLD = "ChampagneGold"
    const val ELEGANT_IVORY = "ElegantIvory"
    const val CUSTOM = "Custom"

    val ALL = listOf(ROYAL_PURPLE, MIDNIGHT_BLACK, CHAMPAGNE_GOLD, ELEGANT_IVORY, CUSTOM)

    fun displayLabel(theme: String): String = when (theme) {
        ROYAL_PURPLE -> "Royal Purple"
        MIDNIGHT_BLACK -> "Midnight Black"
        CHAMPAGNE_GOLD -> "Champagne Gold"
        ELEGANT_IVORY -> "Elegant Ivory"
        CUSTOM -> "Custom Brand"
        else -> theme
    }

    fun paletteFor(theme: String): WebsiteThemePaletteSpec = when (theme) {
        MIDNIGHT_BLACK -> WebsiteThemePaletteSpec(
            backgroundHex = "#050508",
            surfaceHex = "#0E0E14",
            cardHex = "#181824",
            primaryAccentHex = "#6366F1",
            secondaryHighlightHex = "#F7C747"
        )
        CHAMPAGNE_GOLD -> WebsiteThemePaletteSpec(
            backgroundHex = "#120C06",
            surfaceHex = "#1E140B",
            cardHex = "#2C1D11",
            primaryAccentHex = "#D97706",
            secondaryHighlightHex = "#F7C747"
        )
        ELEGANT_IVORY -> WebsiteThemePaletteSpec(
            backgroundHex = "#FAF7F2",
            surfaceHex = "#F2ECE4",
            cardHex = "#FFFFFF",
            primaryAccentHex = "#6D28D9",
            secondaryHighlightHex = "#B45309"
        )
        else -> WebsiteThemePaletteSpec(
            backgroundHex = "#0B0615",
            surfaceHex = "#160B25",
            cardHex = "#241238",
            primaryAccentHex = "#8B20E8",
            secondaryHighlightHex = "#F7C747"
        )
    }
}

object WebsiteLayouts {
    const val EDITORIAL = "Editorial"
    const val CLASSIC_GRID = "ClassicGrid"
    const val MASONRY = "Masonry"

    val ALL = listOf(EDITORIAL, CLASSIC_GRID, MASONRY)

    fun displayLabel(layout: String): String = when (layout) {
        EDITORIAL -> "Editorial"
        CLASSIC_GRID -> "Classic Grid"
        MASONRY -> "Masonry Gallery"
        else -> layout
    }
}

object WebsiteFonts {
    const val PLAYFAIR_DISPLAY = "PlayfairDisplay"
    const val PLUS_JAKARTA_SANS = "PlusJakartaSans"
    const val CORMORANT_GARAMOND = "CormorantGaramond"
    const val INTER = "Inter"

    val ALL = listOf(PLAYFAIR_DISPLAY, PLUS_JAKARTA_SANS, CORMORANT_GARAMOND, INTER)

    fun displayLabel(font: String): String = when (font) {
        PLAYFAIR_DISPLAY -> "Playfair Display"
        PLUS_JAKARTA_SANS -> "Plus Jakarta Sans"
        CORMORANT_GARAMOND -> "Cormorant Garamond"
        INTER -> "Inter Clean"
        else -> font
    }
}

object BannerHeightModes {
    const val COMPACT = "Compact"
    const val STANDARD = "Standard"
    const val CINEMATIC = "Cinematic"

    val ALL = listOf(COMPACT, STANDARD, CINEMATIC)
}

object BannerCropModes {
    const val CROP = "CenterCrop"
    const val TOP = "TopFocus"
    const val FIT = "FullFrameFit"

    val ALL = listOf(CROP, TOP, FIT)

    fun displayLabel(mode: String): String = when (mode) {
        CROP -> "Center Crop"
        TOP -> "Portrait Focus"
        FIT -> "Full Frame"
        else -> mode
    }
}

/**
 * Complete per-event / studio Website Customization configuration stored in
 * `/website_customizations/{customizationId}`.
 */
@IgnoreExtraProperties
data class WebsiteCustomization(
    @DocumentId val id: String = "",
    val userId: String = "",
    val eventId: String = "",
    val eventName: String = "",
    val studioName: String = "Jummo Pro Studio",
    val photographerName: String = "Meraj Sir",
    val brandingEnabled: Boolean = true,
    val logoUri: String = "",
    val profileImageUri: String = "",
    val coverImageUri: String = BannerPresets.PRESET_WEDDING,
    val bannerUrisCsv: String = "${BannerPresets.PRESET_WEDDING}|${BannerPresets.PRESET_FESTIVAL}|${BannerPresets.PRESET_STUDIO}",
    val bannerImageUris: List<String> = listOf(
        BannerPresets.PRESET_WEDDING,
        BannerPresets.PRESET_FESTIVAL,
        BannerPresets.PRESET_STUDIO
    ),
    val autoSliderEnabled: Boolean = true,
    val sliderIntervalSeconds: Long = 4L,
    val bannerHeightMode: String = BannerHeightModes.STANDARD,
    val bannerCropMode: String = BannerCropModes.CROP,
    val theme: String = WebsiteThemes.ROYAL_PURPLE,
    val themePreset: String = WebsiteThemes.ROYAL_PURPLE,
    val layout: String = WebsiteLayouts.EDITORIAL,
    val websiteLayout: String = WebsiteLayouts.EDITORIAL,
    val fontFamily: String = WebsiteFonts.PLAYFAIR_DISPLAY,
    val fontStyle: String = WebsiteFonts.PLAYFAIR_DISPLAY,
    val backgroundColorHex: String = "#0B0615",
    val surfaceColorHex: String = "#160B25",
    val cardColorHex: String = "#241238",
    val primaryAccentHex: String = "#8B20E8",
    val secondaryAccentHex: String = "#F7C747",
    val secondaryHighlightHex: String = "#F7C747",
    val welcomeMessage: String = "Welcome to our official event gallery. Scan or use AI Face Search to find your memories.",
    val contactEmail: String = "merajalam906090@gmail.com",
    val contactPhone: String = "+91 98765 43210",
    val studioLocation: String = "Mumbai & Udaipur, India",
    val instagramUrl: String = "https://instagram.com/jummophotosai",
    val instagramHandle: String = "@jummophotos.ai",
    val portfolioUrl: String = "https://haiker729-sys.github.io/Photo-/",
    val faceSearchEnabled: Boolean = true,
    val aiFaceSearchEnabled: Boolean = true,
    val galleryEnabled: Boolean = true,
    val photoGalleryEnabled: Boolean = true,
    val downloadsEnabled: Boolean = true,
    val photoDownloadsEnabled: Boolean = true,
    val shareEnabled: Boolean = true,
    val sharePhotosEnabled: Boolean = true,
    val guestAccessMode: String = GuestAccessModes.FACE_SEARCH_ONLY,
    val publicationStatus: String = "Published",
    val isPublished: Boolean = true,
    val isSuspended: Boolean = false,
    val hasUnpublishedChanges: Boolean = false,
    val hasDraftChanges: Boolean = false,
    val publishedVersion: Long = 0L,
    val publishedUrl: String = "",
    val publicWebsiteUrl: String = "",
    val adminAuditNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun bannerUrisList(): List<String> {
        val fromCsv = bannerUrisCsv.split("|").map { it.trim() }.filter { it.isNotBlank() }
        if (fromCsv.isNotEmpty()) return fromCsv
        return bannerImageUris.filter { it.isNotBlank() }.ifEmpty {
            listOf(coverImageUri.ifBlank { BannerPresets.PRESET_WEDDING })
        }
    }

    fun resolvedTheme(): String =
        if (themePreset != WebsiteThemes.ROYAL_PURPLE) themePreset else theme

    fun resolvedLayout(): String =
        if (websiteLayout != WebsiteLayouts.EDITORIAL) websiteLayout else layout

    fun resolvedFont(): String =
        if (fontStyle != WebsiteFonts.PLAYFAIR_DISPLAY) fontStyle else fontFamily

    fun isFaceSearchActive(): Boolean = faceSearchEnabled && aiFaceSearchEnabled

    fun isGalleryActive(): Boolean = galleryEnabled && photoGalleryEnabled

    fun isDownloadsActive(): Boolean = downloadsEnabled && photoDownloadsEnabled

    fun isShareActive(): Boolean = shareEnabled && sharePhotosEnabled

    fun isWebsiteSuspended(): Boolean =
        isSuspended || publicationStatus.equals("Suspended", ignoreCase = true)
}

/**
 * Immutable version snapshot stored in `/website_versions/{versionId}` whenever a photographer
 * publishes or rolls back their event website.
 */
@IgnoreExtraProperties
data class WebsiteVersionSnapshot(
    @DocumentId val id: String = "",
    val userId: String = "",
    val eventId: String = "",
    val customizationId: String = "",
    val versionNumber: Long = 1L,
    val studioName: String = "Jummo Pro Studio",
    val photographerName: String = "Meraj Sir",
    val brandingEnabled: Boolean = true,
    val logoUri: String = "",
    val coverImageUri: String = BannerPresets.PRESET_WEDDING,
    val bannerUrisCsv: String = "",
    val bannerImageUris: List<String> = emptyList(),
    val theme: String = WebsiteThemes.ROYAL_PURPLE,
    val themePreset: String = WebsiteThemes.ROYAL_PURPLE,
    val layout: String = WebsiteLayouts.EDITORIAL,
    val websiteLayout: String = WebsiteLayouts.EDITORIAL,
    val fontFamily: String = WebsiteFonts.PLAYFAIR_DISPLAY,
    val welcomeMessage: String = "",
    val faceSearchEnabled: Boolean = true,
    val aiFaceSearchEnabled: Boolean = true,
    val galleryEnabled: Boolean = true,
    val photoGalleryEnabled: Boolean = true,
    val downloadsEnabled: Boolean = true,
    val photoDownloadsEnabled: Boolean = true,
    val shareEnabled: Boolean = true,
    val guestAccessMode: String = GuestAccessModes.FACE_SEARCH_ONLY,
    val publishedUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

fun buildDefaultWebsiteCustomizationForEvent(
    event: PhotoEvent,
    studioName: String = "Jummo Pro Studio",
    photographerName: String = "Meraj Sir"
): WebsiteCustomization {
    val defaultCover = event.coverUri.ifBlank { BannerPresets.forCategory(event.category) }
    val defaultBanners = listOf(
        defaultCover,
        BannerPresets.PRESET_FESTIVAL,
        BannerPresets.PRESET_STUDIO
    ).distinct()
    val cleanId = event.id.ifBlank { "studio_default" }
    val defaultUrl = "https://haiker729-sys.github.io/Photo-/?event=$cleanId"
    return WebsiteCustomization(
        id = "web_$cleanId",
        userId = event.userId,
        eventId = cleanId,
        eventName = event.name.ifBlank { "Studio Showcase" },
        studioName = studioName.ifBlank { "Jummo Pro Studio" },
        photographerName = photographerName.ifBlank { "Meraj Sir" },
        brandingEnabled = true,
        logoUri = "",
        coverImageUri = defaultCover,
        bannerUrisCsv = defaultBanners.joinToString("|"),
        bannerImageUris = defaultBanners,
        autoSliderEnabled = true,
        sliderIntervalSeconds = 4L,
        bannerHeightMode = BannerHeightModes.STANDARD,
        bannerCropMode = BannerCropModes.CROP,
        theme = WebsiteThemes.ROYAL_PURPLE,
        themePreset = WebsiteThemes.ROYAL_PURPLE,
        layout = WebsiteLayouts.EDITORIAL,
        websiteLayout = WebsiteLayouts.EDITORIAL,
        fontFamily = WebsiteFonts.PLAYFAIR_DISPLAY,
        fontStyle = WebsiteFonts.PLAYFAIR_DISPLAY,
        faceSearchEnabled = event.guestAccessMode != GuestAccessModes.GALLERY_ONLY,
        aiFaceSearchEnabled = event.guestAccessMode != GuestAccessModes.GALLERY_ONLY,
        galleryEnabled = true,
        photoGalleryEnabled = true,
        downloadsEnabled = event.guestAllowDownload,
        photoDownloadsEnabled = event.guestAllowDownload,
        shareEnabled = true,
        sharePhotosEnabled = true,
        guestAccessMode = event.guestAccessMode.ifBlank { GuestAccessModes.FACE_SEARCH_ONLY },
        publicationStatus = "Draft",
        isPublished = false,
        isSuspended = false,
        hasUnpublishedChanges = false,
        hasDraftChanges = false,
        publishedVersion = 0L,
        publishedUrl = defaultUrl,
        publicWebsiteUrl = defaultUrl
    )
}
