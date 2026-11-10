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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAF9FF),
                        Color(0xFFEDE9FE).copy(alpha = 0.6f),
                        Color(0xFFFAF9FF)
                    )
                )
            )
            .testTag("jummo_auth_screen")
    ) {
        // Scrollable Foreground Content Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = 500.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
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
                JummoHeroGoldenCameraLogo(size = 64.dp)

                Spacer(modifier = Modifier.height(8.dp))

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
                                color = JummoVioletPrimary,
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

                Surface(
                    color = JummoCardPurple,
                    border = BorderStroke(1.dp, JummoBorderSubtle),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Smart Event Photography Suite",
                        style = MaterialTheme.typography.labelLarge,
                        color = JummoVioletPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                            .testTag("login_tagline")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // EVENT CATEGORIES: Five compact categories in one horizontal row
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

            Spacer(modifier = Modifier.height(12.dp))

            // FEATURE CARD: Clean lavender surface (#EDE9FE) with subtle border (#C7B3FF)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_feature_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = JummoCardPurple
                ),
                border = BorderStroke(1.dp, JummoBorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    LuxuryLoginFeatureRow(
                        icon = Icons.Default.CameraAlt,
                        title = "Camera & DSLR Import",
                        subtitle = "Capture or import photos from camera, SD card or storage."
                    )

                    LoginFeatureDivider()

                    LuxuryLoginFeatureRow(
                        icon = Icons.Default.CloudUpload,
                        title = "Smart Upload Queue",
                        subtitle = "Automatic and manual uploads with network and battery protection."
                    )

                    LoginFeatureDivider()

                    LuxuryLoginFeatureRow(
                        icon = Icons.Default.QrCode2,
                        title = "Event QR & Guest Portal",
                        subtitle = "Create event QR codes and share photos securely with guests."
                    )

                    LoginFeatureDivider()

                    LuxuryLoginFeatureRow(
                        icon = Icons.Default.CenterFocusStrong,
                        title = "AI Face Search",
                        subtitle = "Find guest photos with explicit consent and privacy controls."
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // GOOGLE LOGIN & SECURITY FOOTER
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
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
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Primary Purple Button (#5B21B6 -> #8B5CF6) with #FFFFFF Button Text
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
                    border = BorderStroke(1.dp, JummoBorderGlow)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        JummoVioletPrimary,
                                        JummoVioletBright
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
                                    color = Color.White,
                                    strokeWidth = 2.2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Signing in…",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
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
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Security Footer
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
                        tint = JummoVioletPrimary,
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
                    text = "Official Website ↗",
                    style = MaterialTheme.typography.labelSmall,
                    color = JummoVioletPrimary,
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
 * Purple Premium Camera Emblem for the Login Hero.
 */
@Composable
private fun JummoHeroGoldenCameraLogo(
    size: Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    JummoVioletBright.copy(alpha = 0.28f),
                    Color.Transparent
                ),
                radius = w * 0.55f
            ),
            radius = w * 0.52f,
            center = Offset(w * 0.5f, h * 0.55f)
        )

        val purpleBrush = Brush.linearGradient(
            colors = listOf(
                JummoVioletPrimary,
                JummoVioletBright
            )
        )

        val humpPath = Path().apply {
            moveTo(w * 0.35f, h * 0.28f)
            lineTo(w * 0.41f, h * 0.16f)
            lineTo(w * 0.59f, h * 0.16f)
            lineTo(w * 0.65f, h * 0.28f)
        }
        drawPath(
            path = humpPath,
            brush = purpleBrush,
            style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
        )

        drawRoundRect(
            brush = purpleBrush,
            topLeft = Offset(w * 0.44f, h * 0.21f),
            size = Size(w * 0.12f, h * 0.032f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        val bodyTop = Offset(w * 0.12f, h * 0.28f)
        val bodySize = Size(w * 0.76f, h * 0.54f)
        drawRoundRect(
            color = JummoCardPurple,
            topLeft = bodyTop,
            size = bodySize,
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
        )

        drawRoundRect(
            brush = purpleBrush,
            topLeft = bodyTop,
            size = bodySize,
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
            style = Stroke(width = w * 0.048f)
        )

        drawCircle(
            color = JummoVioletBright,
            radius = w * 0.028f,
            center = Offset(w * 0.76f, h * 0.38f)
        )

        val lensCenter = Offset(w * 0.5f, h * 0.56f)
        drawCircle(
            brush = purpleBrush,
            radius = w * 0.20f,
            center = lensCenter
        )
        drawCircle(
            color = JummoObsidian,
            radius = w * 0.165f,
            center = lensCenter
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    JummoGoldSoft,
                    JummoVioletBright,
                    JummoVioletPrimary
                ),
                center = Offset(lensCenter.x - w * 0.03f, lensCenter.y - h * 0.03f),
                radius = w * 0.16f
            ),
            radius = w * 0.15f,
            center = lensCenter
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = w * 0.032f,
            center = Offset(lensCenter.x - w * 0.045f, lensCenter.y - h * 0.045f)
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
            tint = JummoVioletPrimary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = JummoTextPrimary,
            fontWeight = FontWeight.SemiBold,
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
        color = JummoBorderSubtle
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
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            JummoVioletPrimary,
                            JummoVioletBright
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = JummoTextPrimary,
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
        color = JummoBorderSubtle.copy(alpha = 0.65f)
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
