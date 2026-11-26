package com.itamadersomajinc.elyora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class Screen { HOME, LEARN, REVIEW, GRAMMAR, DICTIONARY, PROFILE }

@Serializable object RouteHome
@Serializable object RouteLearn
@Serializable object RouteReview
@Serializable object RouteProfile

enum class FeatureTool {
    DAILY_CHALLENGE,
    TENSE_MASTERY,
    GRAMMAR_QUIZ,
    SENTENCE_BUILDER,
    SPEAKING,
    LISTENING,
    WRITING,
    AI_ASSISTANT
}

private data class RefNavTab(
    val screen: Screen,
    val testTag: String,
    val contentDesc: String,
    val filled: ImageVector,
    val outlined: ImageVector
)

private val leftNavTabs = listOf(
    RefNavTab(Screen.HOME, "nav_home", "Home · হোম", Icons.Filled.Home, Icons.Outlined.Home),
    RefNavTab(Screen.LEARN, "nav_learn", "Learn & AI · শিখুন ও চ্যাট", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline)
)

private val rightNavTabs = listOf(
    RefNavTab(Screen.REVIEW, "nav_review", "Review & Words · রিভিশন ও শব্দ", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    RefNavTab(Screen.PROFILE, "nav_profile", "Profile · প্রোফাইল", Icons.Filled.Person, Icons.Outlined.PersonOutline)
)

class MainActivity : ComponentActivity() {

    private lateinit var speaker: Speaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = ElyoraAppProvider.get(applicationContext)

        val store = Store(applicationContext)
        store.load()
        KanjiData.init(applicationContext)
        GrammarAcademyData.init(applicationContext)
        Gloss.attach(applicationContext)
        Gloss.ensureBn()
        Gloss.ensure(store.nativeLang)
        Gloss.ensureTarget(store.target)
        Thread { DictionaryData.init(applicationContext) }.start()
        Thread { Verbs.init(applicationContext) }.start()
        container.warmUpIndexes()
        speaker = Speaker(applicationContext)

        setContent {
            val prefsData by container.prefs.preferencesFlow.collectAsStateWithLifecycle(initialValue = AppPreferencesData())
            val scope = rememberCoroutineScope()

            LaunchedEffect(prefsData.offlineVoicePreferred) {
                speaker.setPreferOffline(prefsData.offlineVoicePreferred)
            }

            CompositionLocalProvider(LocalAppContainer provides container) {
                ElyoraTheme(themeMode = prefsData.themeMode) {
                    Gloss.ensureBn()
                    Gloss.ensure(store.nativeLang)
                    Gloss.ensureTarget(store.target)

                    val navController = rememberNavController()
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = backStackEntry?.destination
                    var activeTool by remember { mutableStateOf<FeatureTool?>(null) }
                    var learnSubTab by remember { mutableIntStateOf(0) } // 0 = Courses, 1 = Grammar, 2 = AI Chat
                    var practiceSubTab by remember { mutableIntStateOf(0) } // 0 = Review, 1 = Words, 2 = Verbs
                    var showLanguageSheet by remember { mutableStateOf(false) }
                    var showQuickActionSheet by remember { mutableStateOf(false) }

                    val ctx = LocalContext.current
                    var playUpdateReadyToInstall by remember { mutableStateOf(false) }
                    val playUpdateLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.StartIntentSenderForResult()
                    ) { }

                    DisposableEffect(Unit) {
                        val mgr = Updater.manager(ctx)
                        val listener = Updater.registerFlexibleListener(mgr) {
                            playUpdateReadyToInstall = true
                        }
                        onDispose {
                            runCatching { mgr.unregisterListener(listener) }
                        }
                    }

                    LaunchedEffect(store.onboarded) {
                        if (!store.onboarded) return@LaunchedEffect
                        Updater.checkPlayUpdate(ctx, playUpdateLauncher) { status ->
                            if (status == PlayUpdateStatus.DOWNLOADED_READY) {
                                playUpdateReadyToInstall = true
                            }
                        }
                    }

                    fun navigateTab(targetScreen: Screen) {
                        activeTool = null
                        val dest: Any = when (targetScreen) {
                            Screen.HOME -> RouteHome
                            Screen.LEARN -> {
                                learnSubTab = 0
                                RouteLearn
                            }
                            Screen.GRAMMAR -> {
                                learnSubTab = 1
                                RouteLearn
                            }
                            Screen.REVIEW -> {
                                practiceSubTab = 0
                                RouteReview
                            }
                            Screen.DICTIONARY -> {
                                practiceSubTab = 1
                                RouteReview
                            }
                            Screen.PROFILE -> RouteProfile
                        }
                        navController.navigate(dest) {
                            popUpTo(RouteHome) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }

                    BackHandler(enabled = activeTool != null) {
                        activeTool = null
                    }

                    if (!store.onboarded) {
                        OnboardingDialog(store, onDone = { navigateTab(Screen.HOME) })
                    }

                    val showReviewBadge = store.dueCount() > 0 || !store.isDailyChallengeDoneToday()

                    Scaffold(
                        bottomBar = {
                            if (activeTool == null) {
                                val selectedScreen = when {
                                    currentDestination?.hasRoute<RouteLearn>() == true -> Screen.LEARN
                                    currentDestination?.hasRoute<RouteReview>() == true -> Screen.REVIEW
                                    currentDestination?.hasRoute<RouteProfile>() == true -> Screen.PROFILE
                                    else -> Screen.HOME
                                }
                                ElyoraFloatingCrestBottomBar(
                                    selected = selectedScreen,
                                    showBellBadge = showReviewBadge,
                                    isCenterExpanded = showQuickActionSheet,
                                    onSelect = { navigateTab(it) },
                                    onCenterAction = { showQuickActionSheet = true }
                                )
                            }
                        }
                    ) { pad ->
                        val contentMod = Modifier.padding(pad)
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (playUpdateReadyToInstall) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Google Play update ready · আপডেট প্রস্তুত",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = { Updater.completePlayUpdate(ctx) }) {
                                            Text("Restart")
                                        }
                                    }
                                }
                            }

                            key(store.nativeLang, store.target) {
                                val tool = activeTool
                                if (tool != null) {
                                    when (tool) {
                                        FeatureTool.DAILY_CHALLENGE -> DailyChallengeScreen(store, speaker, contentMod, onClose = { activeTool = null })
                                        FeatureTool.TENSE_MASTERY -> TenseMasteryScreen(store, speaker, contentMod, onClose = { activeTool = null })
                                        FeatureTool.GRAMMAR_QUIZ -> GrammarQuizScreen(store, speaker, contentMod, onClose = { activeTool = null })
                                        FeatureTool.SENTENCE_BUILDER -> SentenceBuilderScreen(store, speaker, contentMod, onClose = { activeTool = null })
                                        FeatureTool.SPEAKING -> SpeakingPracticeScreen(store, speaker, contentMod, onClose = { activeTool = null })
                                        FeatureTool.LISTENING -> ListeningPracticeScreen(store, speaker, contentMod, onClose = { activeTool = null })
                                        FeatureTool.WRITING -> WritingPracticeScreen(store, speaker, contentMod, onClose = { activeTool = null })
                                        FeatureTool.AI_ASSISTANT -> AiAssistantScreen(
                                            store = store,
                                            speaker = speaker,
                                            onOpenLanguageSwitcher = { showLanguageSheet = true },
                                            modifier = contentMod,
                                            onClose = { activeTool = null },
                                            startInVoiceChat = true
                                        )
                                    }
                                } else {
                                    NavHost(
                                        navController = navController,
                                        startDestination = RouteHome,
                                        enterTransition = { fadeIn(animationSpec = tween(220)) },
                                        exitTransition = { fadeOut(animationSpec = tween(180)) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        composable<RouteHome> {
                                            HomeScreen(
                                                store = store,
                                                speaker = speaker,
                                                themeMode = prefsData.themeMode,
                                                onToggleTheme = {
                                                    val next = if (prefsData.themeMode == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
                                                    scope.launch { container.prefs.setThemeMode(next) }
                                                },
                                                onOpenLanguageSwitcher = { showLanguageSheet = true },
                                                modifier = contentMod,
                                                onNav = { navigateTab(it) },
                                                onOpenTool = { activeTool = it },
                                                onOpenVerbs = {
                                                    practiceSubTab = 2
                                                    activeTool = null
                                                    navController.navigate(RouteReview) {
                                                        popUpTo(RouteHome) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            )
                                        }
                                        composable<RouteLearn> {
                                            Column(modifier = contentMod.fillMaxSize()) {
                                                SubHubSegmentedBar(
                                                    items = listOf("Courses · কোর্স", "Grammar · ব্যাকরণ", "AI Chat · এআই"),
                                                    selectedIndex = learnSubTab,
                                                    onSelect = { learnSubTab = it }
                                                )
                                                when (learnSubTab) {
                                                    0 -> LearnScreen(
                                                        store = store,
                                                        speaker = speaker,
                                                        modifier = Modifier.weight(1f),
                                                        onOpenLanguageSwitcher = { showLanguageSheet = true }
                                                    )
                                                    1 -> GrammarScreen(
                                                        store = store,
                                                        speaker = speaker,
                                                        modifier = Modifier.weight(1f),
                                                        onOpenLanguageSwitcher = { showLanguageSheet = true }
                                                    )
                                                    else -> AiAssistantScreen(
                                                        store = store,
                                                        speaker = speaker,
                                                        onOpenLanguageSwitcher = { showLanguageSheet = true },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                        composable<RouteReview> {
                                            Column(modifier = contentMod.fillMaxSize()) {
                                                SubHubSegmentedBar(
                                                    items = listOf("Review · রিভিশন", "Words · শব্দ", "Verbs · ক্রিয়া"),
                                                    selectedIndex = practiceSubTab,
                                                    onSelect = { practiceSubTab = it }
                                                )
                                                when (practiceSubTab) {
                                                    0 -> ReviewScreen(store, speaker, Modifier.weight(1f))
                                                    1 -> WordsScreen(store, speaker, Modifier.weight(1f), initialShowVerbs = false)
                                                    else -> WordsScreen(store, speaker, Modifier.weight(1f), initialShowVerbs = true)
                                                }
                                            }
                                        }
                                        composable<RouteProfile> {
                                            ProfileScreen(
                                                store = store,
                                                speaker = speaker,
                                                prefsData = prefsData,
                                                onSelectTheme = { mode ->
                                                    scope.launch { container.prefs.setThemeMode(mode) }
                                                },
                                                onToggleReminder = { enabled ->
                                                    scope.launch {
                                                        container.prefs.setDailyReminder(enabled)
                                                        WorkScheduler.setDailyReminder(ctx, enabled)
                                                    }
                                                },
                                                onToggleOfflineVoice = { offline ->
                                                    scope.launch { container.prefs.setOfflineVoicePreferred(offline) }
                                                },
                                                modifier = contentMod
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showLanguageSheet) {
                        LanguageSwitcherSheet(
                            store = store,
                            onDismiss = { showLanguageSheet = false }
                        )
                    }

                    if (showQuickActionSheet) {
                        QuickPracticeActionSheet(
                            store = store,
                            onDismiss = { showQuickActionSheet = false },
                            onOpenTool = {
                                showQuickActionSheet = false
                                activeTool = it
                            },
                            onNav = {
                                showQuickActionSheet = false
                                navigateTab(it)
                            },
                            onOpenLanguage = {
                                showQuickActionSheet = false
                                showLanguageSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Updater.checkDownloadedOnResume(this) {
            Updater.completePlayUpdate(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::speaker.isInitialized) speaker.shutdown()
    }
}

/**
 * Custom pill shape with a smooth center convex crest bump matching the reference navbar.
 */
private class FloatingCrestPillShape(
    private val crestHeightDp: Float = 11f,
    private val crestHalfWidthDp: Float = 54f
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val crestH = with(density) { crestHeightDp.dp.toPx() }.coerceAtMost(h * 0.25f)
        val crestHalfW = with(density) { crestHalfWidthDp.dp.toPx() }.coerceAtMost(w * 0.28f)
        val bodyH = h - crestH
        val r = bodyH / 2f
        val cx = w / 2f

        val path = Path().apply {
            // Left semicircle
            arcTo(
                rect = Rect(0f, crestH, bodyH, h),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 180f,
                forceMoveTo = true
            )
            // Straight top edge to start of center crest
            val leftCrestStart = (cx - crestHalfW).coerceAtLeast(r)
            lineTo(leftCrestStart, crestH)
            // Smooth S-curve up to peak
            cubicTo(
                leftCrestStart + crestHalfW * 0.42f, crestH,
                cx - crestHalfW * 0.44f, 0f,
                cx, 0f
            )
            // Smooth S-curve down from peak
            val rightCrestEnd = (cx + crestHalfW).coerceAtMost(w - r)
            cubicTo(
                cx + crestHalfW * 0.44f, 0f,
                rightCrestEnd - crestHalfW * 0.42f, crestH,
                rightCrestEnd, crestH
            )
            // Straight top edge to right semicircle
            lineTo(w - r, crestH)
            // Right semicircle
            arcTo(
                rect = Rect(w - bodyH, crestH, w, h),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            // Bottom edge
            lineTo(r, h)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Floating pill bottom navigation bar with center convex crest, violet dot under active icon,
 * glowing purple + center button (solid purple gradient + dotted halo in Light mode, neon purple ring in Dark mode).
 */
@Composable
private fun ElyoraFloatingCrestBottomBar(
    selected: Screen,
    showBellBadge: Boolean,
    isCenterExpanded: Boolean,
    onSelect: (Screen) -> Unit,
    onCenterAction: () -> Unit
) {
    val accents = LocalElyoraAccents.current
    val isDark = accents.isDark
    val pillBg = if (isDark) Color(0xFF1D1E26) else Color(0xFFFCFCFE)
    val pillBorder = if (isDark) Color(0xFF2D303D) else Color(0xFFEBEAF4)
    val activeIconColor = if (isDark) Color.White else Color(0xFF1B1C24)
    val inactiveIconColor = if (isDark) Color(0xFF9699AB) else Color(0xFF3E404E)
    val violetAccent = Color(0xFF6E4DFF)
    val pillShape = remember { FloatingCrestPillShape() }

    val infiniteTransition = rememberInfiniteTransition(label = "navbarCenterAnim")
    val haloRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloRotation"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val centerInteraction = remember { MutableInteractionSource() }
    val isCenterPressed by centerInteraction.collectIsPressedAsState()
    val centerScale by animateFloatAsState(
        targetValue = when {
            isCenterPressed -> 0.88f
            isCenterExpanded -> 1.08f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "centerScale"
    )
    val plusRotation by animateFloatAsState(
        targetValue = if (isCenterExpanded) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "plusRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = pillShape,
            color = pillBg,
            shadowElevation = if (isDark) 12.dp else 10.dp,
            border = BorderStroke(1.dp, pillBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp, start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                leftNavTabs.forEach { tab ->
                    FloatingNavTabItem(
                        tab = tab,
                        isSelected = selected == tab.screen,
                        showBadge = false,
                        activeColor = activeIconColor,
                        inactiveColor = inactiveIconColor,
                        dotColor = violetAccent,
                        onClick = { onSelect(tab.screen) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Center Hero '+' Button with animated orbiting dotted halo (Light) or pulsing neon ring (Dark)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .offset(y = (-4).dp)
                        .defaultMinSize(minHeight = 54.dp)
                ) {
                    Canvas(modifier = Modifier.size(64.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val pulseRadius = (size.minDimension / 2f) * glowPulse.coerceAtMost(1.06f)
                        if (isDark) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(
                                        violetAccent.copy(alpha = 0.24f + 0.14f * (glowPulse - 0.88f)),
                                        Color.Transparent
                                    )
                                ),
                                radius = pulseRadius
                            )
                        } else {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(
                                        violetAccent.copy(alpha = 0.20f + 0.12f * (glowPulse - 0.88f)),
                                        Color.Transparent
                                    )
                                ),
                                radius = pulseRadius
                            )
                        }
                        rotate(degrees = haloRotation, pivot = Offset(cx, cy)) {
                            val dotRingR = size.minDimension * 0.45f
                            val dotCount = 18
                            for (i in 0 until dotCount) {
                                val rad = (2.0 * PI * i) / dotCount
                                val dx = cx + (dotRingR * cos(rad)).toFloat()
                                val dy = cy + (dotRingR * sin(rad)).toFloat()
                                val dotAlpha = if (isDark) {
                                    if (i % 2 == 0) 0.45f else 0.16f
                                } else {
                                    if (i % 3 == 0) 0.75f else 0.40f
                                }
                                drawCircle(
                                    color = violetAccent.copy(alpha = dotAlpha),
                                    radius = if (i % 3 == 0) 1.6.dp.toPx() else 1.2.dp.toPx(),
                                    center = Offset(dx, dy)
                                )
                            }
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                scaleX = centerScale
                                scaleY = centerScale
                            }
                            .clip(CircleShape)
                            .then(
                                if (isDark) {
                                    Modifier
                                        .background(Color(0xFF1F2029))
                                        .border(2.dp, Color(0xFF7B61FF), CircleShape)
                                } else {
                                    Modifier.background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF8363FF), Color(0xFF6037F8))
                                        )
                                    )
                                }
                            )
                            .clickable(
                                interactionSource = centerInteraction,
                                indication = null,
                                onClick = onCenterAction
                            )
                            .testTag("nav_center_action")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Quick AI & Practice Studio · দ্রুত শুরু",
                            tint = if (isDark) Color(0xFF8F77FF) else Color.White,
                            modifier = Modifier
                                .size(25.dp)
                                .graphicsLayer { rotationZ = plusRotation }
                        )
                    }
                }

                rightNavTabs.forEach { tab ->
                    FloatingNavTabItem(
                        tab = tab,
                        isSelected = selected == tab.screen,
                        showBadge = tab.screen == Screen.REVIEW && showBellBadge,
                        activeColor = activeIconColor,
                        inactiveColor = inactiveIconColor,
                        dotColor = violetAccent,
                        onClick = { onSelect(tab.screen) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavTabItem(
    tab: RefNavTab,
    isSelected: Boolean,
    showBadge: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    dotColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()

    val tint by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(220),
        label = "floatingNavTint"
    )
    val iconScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.86f
            isSelected -> 1.15f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "floatingNavScale"
    )
    val iconLift by animateDpAsState(
        targetValue = if (isSelected) (-2).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "floatingNavLift"
    )
    val dotSize by animateDpAsState(
        targetValue = if (isSelected) 5.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "floatingNavDotSize"
    )
    val dotAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(200),
        label = "floatingNavDotAlpha"
    )

    val badgeScale = if (showBadge) {
        val badgeTransition = rememberInfiniteTransition(label = "badgePulse")
        badgeTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.28f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "badgeScale"
        ).value
    } else {
        1f
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .testTag(tab.testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset { IntOffset(0, iconLift.roundToPx()) }
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                }
        ) {
            Crossfade(
                targetState = isSelected,
                animationSpec = tween(200),
                label = "tabIconCrossfade"
            ) { selectedState ->
                Icon(
                    imageVector = if (selectedState) tab.filled else tab.outlined,
                    contentDescription = tab.contentDesc,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (showBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-1).dp)
                        .size(7.dp)
                        .graphicsLayer {
                            scaleX = badgeScale
                            scaleY = badgeScale
                        }
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(dotSize)
                .graphicsLayer { alpha = dotAlpha }
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun QuickPracticeActionSheet(
    store: Store,
    onDismiss: () -> Unit,
    onOpenTool: (FeatureTool) -> Unit,
    onNav: (Screen) -> Unit,
    onOpenLanguage: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val accents = LocalElyoraAccents.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Quick Studio · দ্রুত শুরু করুন",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            // Featured Offline Voice AI Action
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenTool(FeatureTool.AI_ASSISTANT) }
                    .testTag("quick_sheet_voice_ai"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = accents.lavender.container)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = cs.primary, modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Offline AI Voice Chat (Manus)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = accents.lavender.content
                        )
                        Text(
                            "Qwen2.5 / Qwen3 .gguf · বাংলা ও ইংরেজি ভয়েস চ্যাট",
                            style = MaterialTheme.typography.labelSmall,
                            color = accents.lavender.content.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickChipAction("Daily Challenge", Icons.Filled.AutoAwesome) { onOpenTool(FeatureTool.DAILY_CHALLENGE) }
                QuickChipAction("13 Tenses", Icons.Filled.Schedule) { onOpenTool(FeatureTool.TENSE_MASTERY) }
                QuickChipAction("Grammar Quiz", Icons.Filled.Quiz) { onOpenTool(FeatureTool.GRAMMAR_QUIZ) }
                QuickChipAction("Sentence Builder", Icons.Filled.ViewModule) { onOpenTool(FeatureTool.SENTENCE_BUILDER) }
                QuickChipAction("Speaking", Icons.Filled.Mic) { onOpenTool(FeatureTool.SPEAKING) }
                QuickChipAction("Listening", Icons.Filled.Headphones) { onOpenTool(FeatureTool.LISTENING) }
                QuickChipAction("Writing Studio", Icons.Filled.Create) { onOpenTool(FeatureTool.WRITING) }
                QuickChipAction("Grammar 210+", Icons.Filled.Translate) { onNav(Screen.GRAMMAR) }
            }

            Button(
                onClick = onOpenLanguage,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Switch Language (${languageLabel(store.target)}) · ভাষা পরিবর্তন")
            }
            Spacer(Modifier.height(22.dp))
        }
    }
}

@Composable
private fun QuickChipAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SubHubSegmentedBar(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val accents = LocalElyoraAccents.current
    Surface(
        color = cs.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEachIndexed { idx, label ->
                val active = idx == selectedIndex
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (active) cs.surface else cs.surface.copy(alpha = 0f),
                    shadowElevation = if (active) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .clickable { onSelect(idx) }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Text(
                            label,
                            fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (active) accents.lavender.content else cs.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
