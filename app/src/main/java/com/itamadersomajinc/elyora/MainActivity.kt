package com.itamadersomajinc.elyora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

private val refNavTabs = listOf(
    RefNavTab(Screen.HOME, "nav_home", "Home · হোম", Icons.Filled.Psychology, Icons.Outlined.Psychology),
    RefNavTab(Screen.LEARN, "nav_learn", "Learn & Grammar · শিখুন ও ব্যাকরণ", Icons.Filled.Extension, Icons.Outlined.Extension),
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
                    var learnSubTab by remember { mutableIntStateOf(0) } // 0 = Courses, 1 = Grammar
                    var practiceSubTab by remember { mutableIntStateOf(0) } // 0 = Review, 1 = Words, 2 = Verbs
                    var showLanguageSheet by remember { mutableStateOf(false) }

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
                                ElyoraReferenceBottomBar(
                                    selected = selectedScreen,
                                    showBellBadge = showReviewBadge,
                                    onSelect = { navigateTab(it) }
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
                                                    items = listOf("Courses · কোর্স", "Grammar · ব্যাকরণ"),
                                                    selectedIndex = learnSubTab,
                                                    onSelect = { learnSubTab = it }
                                                )
                                                if (learnSubTab == 0) {
                                                    LearnScreen(
                                                        store = store,
                                                        speaker = speaker,
                                                        modifier = Modifier.weight(1f),
                                                        onOpenLanguageSwitcher = { showLanguageSheet = true }
                                                    )
                                                } else {
                                                    GrammarScreen(
                                                        store = store,
                                                        speaker = speaker,
                                                        modifier = Modifier.weight(1f),
                                                        onOpenLanguageSwitcher = { showLanguageSheet = true }
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
 * Minimal 4-icon bottom navigation bar matching the uploaded reference UI:
 * Psychology (Home) · Extension (Learn/Grammar) · Notifications with pink dot (Review/Words) · Person (Profile).
 */
@Composable
private fun ElyoraReferenceBottomBar(
    selected: Screen,
    showBellBadge: Boolean,
    onSelect: (Screen) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val accents = LocalElyoraAccents.current
    Surface(
        color = cs.surface,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = accents.cardBorder.copy(alpha = 0.55f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                refNavTabs.forEach { tab ->
                    val isSelected = selected == tab.screen
                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) cs.primary else cs.onSurface.copy(alpha = 0.72f),
                        label = "navTint"
                    )
                    val interaction = remember { MutableInteractionSource() }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 52.dp)
                            .clickable(
                                interactionSource = interaction,
                                indication = null,
                                onClick = { onSelect(tab.screen) }
                            )
                            .testTag(tab.testTag)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) cs.primary.copy(alpha = 0.12f) else cs.surface.copy(alpha = 0f))
                                .padding(horizontal = 16.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.filled else tab.outlined,
                                contentDescription = tab.contentDesc,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )
                            if (tab.screen == Screen.REVIEW && showBellBadge) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 2.dp, y = (-1).dp)
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(accents.pinkRing)
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 5.dp else 0.dp)
                                .clip(CircleShape)
                                .background(cs.primary)
                        )
                    }
                }
            }
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
