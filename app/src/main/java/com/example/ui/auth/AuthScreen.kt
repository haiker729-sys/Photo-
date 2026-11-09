package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled. Diamond
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.JummoBorderGlow
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoElevatedPurple
import com.example.ui.theme.JummoGold
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoGoldSoft
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoTextPrimary
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import com.example.ui.theme.JummoVioletPrimary
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

    scope.launch {
        try {
            val activity = context as? Activity
            if (activity == null) {
                onAuthError("Google Sign-In requires an active Activity context.")
                return@launch
            }
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type returned.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed. Please try again.")
        }
    }
}

fun signOut(
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Failed to clear credential state: ${e.message}")
            } finally {
                Firebase.auth.signOut()
                onSignOutComplete()
            }
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    enableAutoSignIn: Boolean = true,
    initialErrorMessage: String? = null,
    onOpenOfficialWebsite: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember(initialErrorMessage) { mutableStateOf(initialErrorMessage) }

    LaunchedEffect(enableAutoSignIn) {
        if (enableAutoSignIn) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = onAuthSuccess,
                onUnauthenticated = {},
                scope = coroutineScope
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JummoObsidian)
            .testTag("jummo_auth_screen")
    ) {
        // 1. Top Hero Cinematic DSLR & Bokeh Backdrop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .align(Alignment.TopCenter)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_studio),
                contentDescription = "Professional DSLR Camera & Studio Bokeh",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Cinematic Purple & Midnight Black Gradient Blending Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                JummoObsidian.copy(alpha = 0.25f),
                                JummoDeepPurple.copy(alpha = 0.55f),
                                JummoObsidian.copy(alpha = 0.92f),
                                JummoObsidian
                            )
                        )
                    )
            )
            // Subtle Diagonal Violet Light Beam Accent matching reference
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            JummoVioletBright.copy(alpha = 0.65f),
                            JummoVioletPrimary.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, size.height * 0.52f),
                    end = Offset(size.width * 0.42f, size.height * 0.14f),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Bottom Silk Purple & Gold Wave Glow Artwork matching reference
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .align(Alignment.BottomCenter)
        ) {
            val w = size.width
            val h = size.height
            val wavePath = Path().apply {
                moveTo(0f, h * 0.55f)
                cubicTo(
                    w * 0.28f, h * 0.18f,
                    w * 0.58f, h * 0.92f,
                    w, h * 0.30f
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = wavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        JummoVioletPrimary.copy(alpha = 0.32f),
                        JummoDeepPurple.copy(alpha = 0.65f),
                        JummoObsidian
                    )
                )
            )
            val crestPath = Path().apply {
                moveTo(0f, h * 0.55f)
                cubicTo(
                    w * 0.28f, h * 0.18f,
                    w * 0.58f, h * 0.92f,
                    w, h * 0.30f
                )
            }
            drawPath(
                path = crestPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        JummoVioletBright.copy(alpha = 0.35f),
                        JummoVioletBright.copy(alpha = 0.85f),
                        JummoGoldBright.copy(alpha = 0.9f),
                        JummoVioletBright.copy(alpha = 0.45f)
                    )
                ),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Scrollable Foreground Content Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = 500.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .testTag("login_scroll_container"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP HERO BRANDING SECTION
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                // Luxury Outlined Golden Camera Emblem with Glowing Purple Optical Lens
                JummoHeroGoldenCameraLogo(size = 68.dp)

                Spacer(modifier = Modifier.height(6.dp))

                // App Name: "Jummo Photos AI" (White + Champagne Gold "AI")
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = JummoTextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        ) {
                            append("Jummo Photos ")
                        }
                        withStyle(
                            SpanStyle(
                                color = JummoGoldBright,
                                fontWeight = FontWeight.ExtraBold
                            )
                        ) {
                            append("AI")
                        }
                    },
                    fontSize = 26.sp,
                    lineHeight = 30.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("login_app_title")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Tagline Pill flanked by Golden Horizontal Lines: ── ( Smart Event Photography Suite ) ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(1.2.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, JummoGoldBright)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = JummoObsidian.copy(alpha = 0.78f),
                        border = BorderStroke(1.dp, JummoGoldBright.copy(alpha = 0.75f)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Smart Event Photography Suite",
                            style = MaterialTheme.typography.labelLarge,
                            color = JummoGoldSoft,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                                .testTag("login_tagline")
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(1.2.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(JummoGoldBright, Color.Transparent)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. EVENT CATEGORIES: Five compact categories in one horizontal row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_event_categories_row"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoginCategoryItem(
                        icon = Icons.Default.Diamond,
                        label = "Weddings",
                        modifier = Modifier.weight(1f)
                    )
                    CategoryVerticalSeparator()
                    LoginCategoryItem(
                        icon = Icons.Default.EventAvailable,
                        label = "Festivals",
                        modifier = Modifier.weight(1f)
                    )
                    CategoryVerticalSeparator()
                    LoginCategoryItem(
                        icon = Icons.Default.Apartment,
                        label = "Corporate",
                        modifier = Modifier.weight(1f)
                    )
                    CategoryVerticalSeparator()
                    LoginCategoryItem(
                        icon = Icons.Default.School,
                        label = "Schools",
                        modifier = Modifier.weight(1f)
                    )
                    CategoryVerticalSeparator()
                    LoginCategoryItem(
                        icon = Icons.Default.Podcasts,
                        label = "Live Events",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. FEATURE CARD: One compact premium purple glass-style card with four feature rows
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_feature_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = JummoCardPurple.copy(alpha = 0.90f)
                ),
                border = BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            JummoBorderGlow,
                            JummoVioletPrimary.copy(alpha = 0.65f),
                            JummoBorderGlow.copy(alpha = 0.85f)
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
                                    JummoElevatedPurple.copy(alpha = 0.55f),
                                    JummoCardPurple.copy(alpha = 0.92f),
                                    JummoDeepPurple.copy(alpha = 0.95f)
                                )
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // ROW 1: Camera & DSLR Import
                    LuxuryLoginFeatureRow(
                        icon = Icons.Default.CameraAlt,
                        title = "Camera & DSLR Import",
                        subtitle = "Capture or import photos from camera, SD card or storage."
                    )

                    LoginFeatureDivider()

                    // ROW 2: Smart Upload Queue
                    LuxuryLoginFeatureRow(
                        icon = Icons.Default.CloudUpload,
                        title = "Smart Upload Queue",
                        subtitle = "Automatic and manual uploads with network and battery protection."
                    )

                    LoginFeatureDivider()

                    // ROW 3: Event QR & Guest Portal
                    LuxuryLoginFeatureRow(
                        icon = Icons.Default.QrCode2,
                        title = "Event QR & Guest Portal",
                        subtitle = "Create event QR codes and share photos securely with guests."
                    )

                    LoginFeatureDivider()

                    // ROW 4: AI Face Search
                    LuxuryLoginFeatureRow(
                        icon = Icons.Default.CenterFocusStrong,
                        title = "AI Face Search",
                        subtitle = "Find guest photos with explicit consent and privacy controls."
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. GOOGLE LOGIN & 7. SECURITY FOOTER
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
                        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("login_error_banner")
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Prominent Champagne-Gold Gradient Button: "Sign in with Google"
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .semantics { role = Role.Button }
                        .clickable(enabled = !isLoading) {
                            errorMessage = null
                            isLoading = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    onAuthSuccess()
                                },
                                onAuthError = { msg ->
                                    isLoading = false
                                    errorMessage = msg
                                },
                                scope = coroutineScope,
                                onAuthCancelled = {
                                    isLoading = false
                                }
                            )
                        }
                        .testTag("google_sign_in_button"),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, JummoGoldSoft.copy(alpha = 0.9f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFFE28A),
                                        JummoGoldBright,
                                        JummoGold
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = JummoObsidian,
                                    strokeWidth = 2.2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Signing in with Google…",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = JummoObsidian,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                OfficialGoogleBrandBadge(size = 26.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Sign in with Google",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = JummoObsidian,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 7. Security Footer: Golden Shield Icon + "Secured by Firebase Authentication"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .testTag("login_security_footer")
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = JummoGoldBright,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Secured by Firebase Authentication",
                        style = MaterialTheme.typography.bodySmall,
                        color = JummoTextSecondary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "Official Website & APK Release Portal ↗",
                    style = MaterialTheme.typography.labelSmall,
                    color = JummoGoldBright,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onOpenOfficialWebsite() }
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                        .testTag("open_official_website_from_login")
                )
            }
        }
    }
}

/**
 * Outlined Golden Camera Emblem with Glowing Purple Optical Lens matching the top hero of the Login reference image.
 */
@Composable
private fun JummoHeroGoldenCameraLogo(
    size: Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Subtle outer purple + gold optical aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    JummoVioletBright.copy(alpha = 0.35f),
                    JummoGoldBright.copy(alpha = 0.18f),
                    Color.Transparent
                ),
                radius = w * 0.55f
            ),
            radius = w * 0.52f,
            center = Offset(w * 0.5f, h * 0.55f)
        )

        val goldBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFE596),
                JummoGoldBright,
                Color(0xFFC98A1B),
                JummoGoldBright
            )
        )

        // Top Viewfinder Hump Outline
        val humpPath = Path().apply {
            moveTo(w * 0.35f, h * 0.28f)
            lineTo(w * 0.41f, h * 0.16f)
            lineTo(w * 0.59f, h * 0.16f)
            lineTo(w * 0.65f, h * 0.28f)
        }
        drawPath(
            path = humpPath,
            brush = goldBrush,
            style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
        )

        // Small Top-Center Viewfinder Window Bar
        drawRoundRect(
            brush = goldBrush,
            topLeft = Offset(w * 0.44f, h * 0.21f),
            size = Size(w * 0.12f, h * 0.032f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Camera Body Fill (Dark Obsidian-Purple Glass)
        val bodyTop = Offset(w * 0.12f, h * 0.28f)
        val bodySize = Size(w * 0.76f, h * 0.54f)
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF231338).copy(alpha = 0.88f),
                    Color(0xFF0E0718).copy(alpha = 0.94f)
                )
            ),
            topLeft = bodyTop,
            size = bodySize,
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
        )

        // Camera Body Golden Metallic Border
        drawRoundRect(
            brush = goldBrush,
            topLeft = bodyTop,
            size = bodySize,
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
            style = Stroke(width = w * 0.048f)
        )

        // Small Golden Flash Dot on Top Right of Body
        drawCircle(
            color = Color(0xFFFFE596),
            radius = w * 0.028f,
            center = Offset(w * 0.76f, h * 0.38f)
        )

        // Center Lens Outer Golden Bezel
        val lensCenter = Offset(w * 0.5f, h * 0.56f)
        drawCircle(
            brush = goldBrush,
            radius = w * 0.20f,
            center = lensCenter
        )
        drawCircle(
            color = JummoObsidian,
            radius = w * 0.165f,
            center = lensCenter
        )

        // Glowing Purple Optical Glass Center
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE9B8FF),
                    JummoVioletBright,
                    Color(0xFF4B0D7A),
                    Color(0xFF1A062E)
                ),
                center = Offset(lensCenter.x - w * 0.03f, lensCenter.y - h * 0.03f),
                radius = w * 0.16f
            ),
            radius = w * 0.15f,
            center = lensCenter
        )

        // Optical Specular Reflection Dots
        drawCircle(
            color = Color.White.copy(alpha = 0.80f),
            radius = w * 0.032f,
            center = Offset(lensCenter.x - w * 0.045f, lensCenter.y - h * 0.045f)
        )
        drawCircle(
            color = JummoVioletBright.copy(alpha = 0.55f),
            radius = w * 0.018f,
            center = Offset(lensCenter.x + w * 0.045f, lensCenter.y + h * 0.045f)
        )
    }
}

@Composable
private fun LoginCategoryItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = JummoGoldBright,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CategoryVerticalSeparator() {
    VerticalDivider(
        modifier = Modifier
            .height(26.dp)
            .width(0.8.dp),
        color = JummoBorderSubtle.copy(alpha = 0.65f)
    )
}

@Composable
private fun LuxuryLoginFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rounded-Square Purple Glass Icon Container with Golden Icon
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            JummoVioletPrimary.copy(alpha = 0.42f),
                            JummoElevatedPurple.copy(alpha = 0.90f)
                        )
                    )
                )
                .border(
                    width = 0.9.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            JummoBorderGlow.copy(alpha = 0.85f),
                            JummoBorderSubtle
                        )
                    ),
                    shape = RoundedCornerShape(13.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = JummoGoldBright,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = JummoTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun LoginFeatureDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        thickness = 0.75.dp,
        color = JummoBorderSubtle.copy(alpha = 0.45f)
    )
}

/**
 * Official Google "G" multi-color emblem inside a crisp white circular badge.
 */
@Composable
private fun OfficialGoogleBrandBadge(
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.64f)) {
            val stroke = this.size.width * 0.22f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)

            // Red Top Arc
            drawArc(
                color = Color(0xFFEA4335),
                startAngle = 215f,
                sweepAngle = 105f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
            // Yellow Left Arc
            drawArc(
                color = Color(0xFFFBBC05),
                startAngle = 145f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
            // Green Bottom Arc
            drawArc(
                color = Color(0xFF34A853),
                startAngle = 45f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
            // Blue Right Arc
            drawArc(
                color = Color(0xFF4285F4),
                startAngle = -15f,
                sweepAngle = 60f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
            // Horizontal Blue Crossbar
            drawRect(
                color = Color(0xFF4285F4),
                topLeft = Offset(this.size.width * 0.50f, this.size.height * 0.41f),
                size = Size(this.size.width * 0.46f, stroke * 0.92f)
            )
        }
    }
}
