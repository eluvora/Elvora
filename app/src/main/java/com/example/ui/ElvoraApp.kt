package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.prefs.ThemePreference
import com.example.ui.components.LockedPremiumBadge
import com.example.ui.components.MusicMutePill
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FeedbackScreen
import com.example.ui.screens.FlipCardsScreen
import com.example.ui.screens.HelpSupportScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PremiumFeatureGateScreen
import com.example.ui.screens.PremiumFeatureType
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.QuickAnswersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmartMcqsScreen
import com.example.ui.screens.SmartSummaryScreen
import com.example.ui.screens.SubscriptionPlansScreen
import com.example.ui.screens.WelcomeAnimationScreen
import com.example.ui.theme.ElvoraTheme
import com.example.ui.theme.LocalElvoraTokens
import com.example.ui.theme.VividMagentaPink
import kotlinx.coroutines.launch

object ElvoraRoutes {
    const val ROUTE_AUTH = "auth"
    const val ROUTE_WELCOME = "welcome"
    const val ROUTE_HOME = "dashboard"
    const val ROUTE_LIBRARY = "library"
    const val ROUTE_PROGRESS = "progress"
    const val ROUTE_FEEDBACK = "feedback"
    const val ROUTE_TOOL_MCQS = "tool_mcqs"
    const val ROUTE_TOOL_FLIPCARDS = "tool_flipcards"
    const val ROUTE_TOOL_SUMMARY = "tool_summary"
    const val ROUTE_TOOL_QUICK_ANSWERS = "tool_quick_answers"
    const val ROUTE_PREMIUM_SMART_NOTES = "premium_smart_notes"
    const val ROUTE_PREMIUM_QUIZ_FORGE = "premium_quiz_forge"
    const val ROUTE_PREMIUM_REVISION = "premium_revision"
    const val ROUTE_PREMIUM_STUDY_CAST = "premium_study_cast"
    const val ROUTE_PLANS = "subscription_plans"
    const val ROUTE_PROFILE = "profile"
    const val ROUTE_SETTINGS = "settings"
    const val ROUTE_HELP = "help_support"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElvoraApp(vm: ElvoraViewModel = viewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val currentUser by vm.currentUser.collectAsStateWithLifecycle()
    val authUiState by vm.authUiState.collectAsStateWithLifecycle()
    val libraryItems by vm.libraryItems.collectAsStateWithLifecycle()
    val quizAttempts by vm.quizAttempts.collectAsStateWithLifecycle()
    val mistakes by vm.mistakes.collectAsStateWithLifecycle()
    val activityLogs by vm.activityLogs.collectAsStateWithLifecycle()
    val feedbackList by vm.feedbackList.collectAsStateWithLifecycle()
    val snackbarMsg by vm.snackbarMessage.collectAsStateWithLifecycle()

    val ttsReady by vm.ttsReady.collectAsStateWithLifecycle()
    val ttsSpeaking by vm.ttsSpeaking.collectAsStateWithLifecycle()
    val ttsActiveLine by vm.ttsActiveLine.collectAsStateWithLifecycle()

    val useDarkTheme = when (settings.themePreference) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

    ElvoraTheme(darkTheme = useDarkTheme) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route ?: ElvoraRoutes.ROUTE_AUTH

        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(snackbarMsg) {
            snackbarMsg?.let {
                snackbarHostState.showSnackbar(it)
                vm.clearSnackbar()
            }
        }

        // Keep session synced cleanly on startup
        LaunchedEffect(settings.currentUserEmail, settings.onboardingCompleted) {
            if (settings.currentUserEmail.isBlank() && currentRoute != ElvoraRoutes.ROUTE_AUTH) {
                navController.navigate(ElvoraRoutes.ROUTE_AUTH) {
                    popUpTo(0) { inclusive = true }
                }
            } else if (settings.currentUserEmail.isNotBlank() && currentRoute == ElvoraRoutes.ROUTE_AUTH) {
                vm.startAmbientMusicIfEnabled()
                val target = if (settings.onboardingCompleted) ElvoraRoutes.ROUTE_HOME else ElvoraRoutes.ROUTE_WELCOME
                navController.navigate(target) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        val mainTabRoutes = setOf(
            ElvoraRoutes.ROUTE_HOME,
            ElvoraRoutes.ROUTE_LIBRARY,
            ElvoraRoutes.ROUTE_PROGRESS,
            ElvoraRoutes.ROUTE_FEEDBACK
        )
        val showChrome = currentRoute in mainTabRoutes

        val navigateDrawerRoute: (String) -> Unit = { route ->
            scope.launch { drawerState.close() }
            navController.navigate(route) { launchSingleTop = true }
        }

        val appTokens = LocalElvoraTokens.current

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = showChrome,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp),
                    drawerContainerColor = appTokens.navBarContainer
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(appTokens.heroBannerBrush)
                                .padding(20.dp)
                        ) {
                            Column {
                                Text("ELVORA", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Modern Frameworks for a Modern World.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentUser?.email ?: "demo@elvora.local",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "PREMIUM FEATURES",
                            style = MaterialTheme.typography.labelSmall,
                            color = VividMagentaPink,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        DrawerPremiumItem("SmartNotes", Icons.Filled.EditNote) {
                            navigateDrawerRoute(ElvoraRoutes.ROUTE_PREMIUM_SMART_NOTES)
                        }
                        DrawerPremiumItem("QuizForge", Icons.Filled.Quiz) {
                            navigateDrawerRoute(ElvoraRoutes.ROUTE_PREMIUM_QUIZ_FORGE)
                        }
                        DrawerPremiumItem("LastMinute Revision", Icons.Filled.Speed) {
                            navigateDrawerRoute(ElvoraRoutes.ROUTE_PREMIUM_REVISION)
                        }
                        DrawerPremiumItem("StudyCast", Icons.Filled.Headphones) {
                            navigateDrawerRoute(ElvoraRoutes.ROUTE_PREMIUM_STUDY_CAST)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        NavigationDrawerItem(
                            label = { Text("Subscription Plans") },
                            selected = currentRoute == ElvoraRoutes.ROUTE_PLANS,
                            onClick = { navigateDrawerRoute(ElvoraRoutes.ROUTE_PLANS) },
                            icon = { Icon(Icons.Filled.CreditCard, contentDescription = null) }
                        )
                        NavigationDrawerItem(
                            label = { Text("Profile") },
                            selected = currentRoute == ElvoraRoutes.ROUTE_PROFILE,
                            onClick = { navigateDrawerRoute(ElvoraRoutes.ROUTE_PROFILE) },
                            icon = { Icon(Icons.Filled.Person, contentDescription = null) }
                        )
                        NavigationDrawerItem(
                            label = { Text("Settings") },
                            selected = currentRoute == ElvoraRoutes.ROUTE_SETTINGS,
                            onClick = { navigateDrawerRoute(ElvoraRoutes.ROUTE_SETTINGS) },
                            icon = { Icon(Icons.Filled.Settings, contentDescription = null) }
                        )
                        NavigationDrawerItem(
                            label = { Text("Help & Support") },
                            selected = currentRoute == ElvoraRoutes.ROUTE_HELP,
                            onClick = { navigateDrawerRoute(ElvoraRoutes.ROUTE_HELP) },
                            icon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null) }
                        )
                    }
                }
            }
        ) {
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    if (showChrome) {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = appTokens.navBarContainer
                            ),
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = VividMagentaPink
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ELVORA", fontWeight = FontWeight.Bold)
                                }
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("hamburger_menu_button")
                                ) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Open navigation menu")
                                }
                            },
                            actions = {
                                MusicMutePill(
                                    musicEnabled = settings.musicEnabled,
                                    onToggle = { vm.toggleMusic() },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        )
                    }
                },
                bottomBar = {
                    if (showChrome) {
                        val navItemColors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VividMagentaPink,
                            selectedTextColor = VividMagentaPink,
                            indicatorColor = appTokens.navBarIndicator,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        NavigationBar(containerColor = appTokens.navBarContainer) {
                            NavigationBarItem(
                                selected = currentRoute == ElvoraRoutes.ROUTE_HOME,
                                onClick = {
                                    navController.navigate(ElvoraRoutes.ROUTE_HOME) {
                                        popUpTo(ElvoraRoutes.ROUTE_HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = navItemColors,
                                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                                label = { Text("Home") },
                                modifier = Modifier.testTag("bottom_nav_home")
                            )
                            NavigationBarItem(
                                selected = currentRoute == ElvoraRoutes.ROUTE_LIBRARY,
                                onClick = {
                                    navController.navigate(ElvoraRoutes.ROUTE_LIBRARY) {
                                        popUpTo(ElvoraRoutes.ROUTE_HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = navItemColors,
                                icon = { Icon(Icons.Filled.Book, contentDescription = "Library") },
                                label = { Text("Library") },
                                modifier = Modifier.testTag("bottom_nav_library")
                            )
                            NavigationBarItem(
                                selected = currentRoute == ElvoraRoutes.ROUTE_PROGRESS,
                                onClick = {
                                    navController.navigate(ElvoraRoutes.ROUTE_PROGRESS) {
                                        popUpTo(ElvoraRoutes.ROUTE_HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = navItemColors,
                                icon = { Icon(Icons.Filled.Analytics, contentDescription = "Progress") },
                                label = { Text("Progress") },
                                modifier = Modifier.testTag("bottom_nav_progress")
                            )
                            NavigationBarItem(
                                selected = currentRoute == ElvoraRoutes.ROUTE_FEEDBACK,
                                onClick = {
                                    navController.navigate(ElvoraRoutes.ROUTE_FEEDBACK) {
                                        popUpTo(ElvoraRoutes.ROUTE_HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = navItemColors,
                                icon = { Icon(Icons.Filled.Feedback, contentDescription = "Feedback") },
                                label = { Text("Feedback") },
                                modifier = Modifier.testTag("bottom_nav_feedback")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(appTokens.pageBackgroundBrush)
                        .padding(innerPadding)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = ElvoraRoutes.ROUTE_AUTH
                    ) {
                        composable(ElvoraRoutes.ROUTE_AUTH) {
                            AuthScreen(
                                authUiState = authUiState,
                                musicEnabled = settings.musicEnabled,
                                onToggleMusic = { vm.toggleMusic() },
                                onLogin = { email, pass ->
                                    vm.login(email, pass) {
                                        vm.startAmbientMusicIfEnabled()
                                        navController.navigate(ElvoraRoutes.ROUTE_HOME) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                },
                                onRegister = { name, email, pass ->
                                    vm.register(name, email, pass) {
                                        vm.startAmbientMusicIfEnabled()
                                        navController.navigate(ElvoraRoutes.ROUTE_WELCOME) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                },
                                onDemoStudent = {
                                    vm.continueAsDemoStudent {
                                        vm.startAmbientMusicIfEnabled()
                                        navController.navigate(ElvoraRoutes.ROUTE_WELCOME) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                },
                                onResetPassword = { email, newPass -> vm.resetLocalPassword(email, newPass) },
                                onClearMessages = { vm.clearAuthMessages() }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_WELCOME) {
                            WelcomeAnimationScreen(
                                studentName = currentUser?.name ?: "Scholar",
                                musicEnabled = settings.musicEnabled,
                                musicVolume = settings.musicVolume,
                                reducedMotion = settings.reducedMotion,
                                ttsReady = ttsReady,
                                onStartMusic = { vm.startAmbientMusicIfEnabled() },
                                onToggleMusic = { vm.toggleMusic() },
                                onVolumeChange = { vm.setMusicVolume(it) },
                                onSpeakText = { vm.speakText(it) },
                                onStopSpeech = { vm.stopTts() },
                                onCompleteOrSkip = {
                                    vm.completeWelcome()
                                    navController.navigate(ElvoraRoutes.ROUTE_HOME) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_HOME) {
                            DashboardScreen(
                                user = currentUser,
                                attempts = quizAttempts,
                                activities = activityLogs,
                                reducedMotion = settings.reducedMotion,
                                onOpenMcqs = { navController.navigate(ElvoraRoutes.ROUTE_TOOL_MCQS) },
                                onOpenFlipCards = { navController.navigate(ElvoraRoutes.ROUTE_TOOL_FLIPCARDS) },
                                onOpenSummary = { navController.navigate(ElvoraRoutes.ROUTE_TOOL_SUMMARY) },
                                onOpenQuickAnswers = { navController.navigate(ElvoraRoutes.ROUTE_TOOL_QUICK_ANSWERS) },
                                onExplorePremium = { scope.launch { drawerState.open() } },
                                onOpenProfile = { navController.navigate(ElvoraRoutes.ROUTE_PROFILE) },
                                onOpenProgress = { navController.navigate(ElvoraRoutes.ROUTE_PROGRESS) },
                                onReplayWelcome = { navController.navigate(ElvoraRoutes.ROUTE_WELCOME) }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_TOOL_MCQS) {
                            SmartMcqsScreen(
                                isPremium = false,
                                reducedMotion = settings.reducedMotion,
                                onOpenPlans = {
                                    navController.navigate(ElvoraRoutes.ROUTE_PLANS) { launchSingleTop = true }
                                },
                                onBack = { navController.popBackStack() },
                                onRecordAttempt = { topic, diff, qs, sel, src ->
                                    vm.recordMcqQuizResult(topic, diff, qs, sel, src)
                                },
                                onSaveToLibrary = { topic, qs, src ->
                                    vm.saveMcqSetToLibrary(topic, qs, src)
                                }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_TOOL_FLIPCARDS) {
                            FlipCardsScreen(
                                isPremium = false,
                                reducedMotion = settings.reducedMotion,
                                onOpenPlans = {
                                    navController.navigate(ElvoraRoutes.ROUTE_PLANS) { launchSingleTop = true }
                                },
                                onBack = { navController.popBackStack() },
                                onMarkLearntLog = { topic, cnt -> vm.markFlipCardsLearnt(topic, cnt) },
                                onSaveToLibrary = { topic, cards, src ->
                                    vm.saveFlipCardsToLibrary(topic, cards, src)
                                }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_TOOL_SUMMARY) {
                            SmartSummaryScreen(
                                isPremium = false,
                                onOpenPlans = {
                                    navController.navigate(ElvoraRoutes.ROUTE_PLANS) { launchSingleTop = true }
                                },
                                onBack = { navController.popBackStack() },
                                onSaveToLibrary = { sum, src -> vm.saveSummaryToLibrary(sum, src) }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_TOOL_QUICK_ANSWERS) {
                            QuickAnswersScreen(
                                isPremium = false,
                                onOpenPlans = {
                                    navController.navigate(ElvoraRoutes.ROUTE_PLANS) { launchSingleTop = true }
                                },
                                onBack = { navController.popBackStack() },
                                onSaveToLibrary = { qa, topic -> vm.saveQuickAnswerToLibrary(qa, topic) }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_LIBRARY) {
                            LibraryScreen(
                                items = libraryItems,
                                onUpdateItem = { vm.updateLibraryItem(it) },
                                onDeleteItem = { vm.deleteLibraryItem(it) }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_PROGRESS) {
                            ProgressScreen(
                                attempts = quizAttempts,
                                mistakes = mistakes,
                                activities = activityLogs
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_FEEDBACK) {
                            FeedbackScreen(
                                submissions = feedbackList,
                                onSubmitFeedback = { feature, rating, liked, imp, done ->
                                    vm.submitFeedback(feature, rating, liked, imp, done)
                                },
                                onMaybeLater = {
                                    vm.dismissFeedbackPrompt()
                                    navController.navigate(ElvoraRoutes.ROUTE_HOME) {
                                        popUpTo(ElvoraRoutes.ROUTE_HOME) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_PREMIUM_SMART_NOTES) {
                            PremiumFeatureGateScreen(
                                feature = PremiumFeatureType.SMART_NOTES,
                                ttsReady = ttsReady,
                                ttsSpeaking = ttsSpeaking,
                                ttsActiveLine = ttsActiveLine,
                                onPlayTtsDialogue = { lines, rate -> vm.playTtsDialogue(lines, rate) },
                                onPauseTts = { vm.pauseTts() },
                                onResumeTts = { vm.resumeTts() },
                                onStopTts = { vm.stopTts() },
                                onSetTtsRate = { vm.setTtsRate(it) },
                                onOpenPlans = { navController.navigate(ElvoraRoutes.ROUTE_PLANS) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_PREMIUM_QUIZ_FORGE) {
                            PremiumFeatureGateScreen(
                                feature = PremiumFeatureType.QUIZ_FORGE,
                                ttsReady = ttsReady,
                                ttsSpeaking = ttsSpeaking,
                                ttsActiveLine = ttsActiveLine,
                                onPlayTtsDialogue = { lines, rate -> vm.playTtsDialogue(lines, rate) },
                                onPauseTts = { vm.pauseTts() },
                                onResumeTts = { vm.resumeTts() },
                                onStopTts = { vm.stopTts() },
                                onSetTtsRate = { vm.setTtsRate(it) },
                                onOpenPlans = { navController.navigate(ElvoraRoutes.ROUTE_PLANS) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_PREMIUM_REVISION) {
                            PremiumFeatureGateScreen(
                                feature = PremiumFeatureType.LAST_MINUTE_REVISION,
                                ttsReady = ttsReady,
                                ttsSpeaking = ttsSpeaking,
                                ttsActiveLine = ttsActiveLine,
                                onPlayTtsDialogue = { lines, rate -> vm.playTtsDialogue(lines, rate) },
                                onPauseTts = { vm.pauseTts() },
                                onResumeTts = { vm.resumeTts() },
                                onStopTts = { vm.stopTts() },
                                onSetTtsRate = { vm.setTtsRate(it) },
                                onOpenPlans = { navController.navigate(ElvoraRoutes.ROUTE_PLANS) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_PREMIUM_STUDY_CAST) {
                            PremiumFeatureGateScreen(
                                feature = PremiumFeatureType.STUDY_CAST,
                                ttsReady = ttsReady,
                                ttsSpeaking = ttsSpeaking,
                                ttsActiveLine = ttsActiveLine,
                                onPlayTtsDialogue = { lines, rate -> vm.playTtsDialogue(lines, rate) },
                                onPauseTts = { vm.pauseTts() },
                                onResumeTts = { vm.resumeTts() },
                                onStopTts = { vm.stopTts() },
                                onSetTtsRate = { vm.setTtsRate(it) },
                                onOpenPlans = { navController.navigate(ElvoraRoutes.ROUTE_PLANS) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_PLANS) {
                            SubscriptionPlansScreen(onBack = { navController.popBackStack() })
                        }

                        composable(ElvoraRoutes.ROUTE_PROFILE) {
                            ProfileScreen(
                                user = currentUser,
                                totalActivities = activityLogs.size,
                                totalSaved = libraryItems.size,
                                onUpdateName = { vm.updateProfileName(it) },
                                onOpenPlans = { navController.navigate(ElvoraRoutes.ROUTE_PLANS) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_SETTINGS) {
                            SettingsScreen(
                                settings = settings,
                                onSetTheme = { vm.setThemePreference(it) },
                                onToggleMusic = { vm.toggleMusic() },
                                onSetVolume = { vm.setMusicVolume(it) },
                                onSetNotifications = { vm.setNotificationsEnabled(it) },
                                onSetReducedMotion = { vm.setReducedMotion(it) },
                                onClearData = { vm.clearLocalUserData() },
                                onLogout = {
                                    vm.logout {
                                        navController.navigate(ElvoraRoutes.ROUTE_AUTH) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(ElvoraRoutes.ROUTE_HELP) {
                            HelpSupportScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerPremiumItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title)
                LockedPremiumBadge()
            }
        },
        selected = false,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) }
    )
}
