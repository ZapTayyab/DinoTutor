package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.CelebrationType
import com.example.ui.viewmodel.LearningViewModel
import com.example.ui.viewmodel.QuizState
import com.example.data.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun KidsAppMainScreen(
    viewModel: LearningViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val unlockedBadges by viewModel.unlockedBadges.collectAsStateWithLifecycle()
    val activityLogs by viewModel.activityLogs.collectAsStateWithLifecycle()
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val isSparkyThinking by viewModel.isSparkyThinking.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(KidsScreen.DASHBOARD) }
    var triggerConfetti by remember { mutableStateOf(0L) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Observe gameplay triggers from VM
    LaunchedEffect(Unit) {
        viewModel.celebrationEvent.collectLatest { event ->
            when (event) {
                CelebrationType.CORRECT_ANSWER -> {
                    triggerConfetti = System.currentTimeMillis()
                }
                CelebrationType.LEVEL_COMPLETE_PERFECT, CelebrationType.LEVEL_COMPLETE -> {
                    triggerConfetti = System.currentTimeMillis()
                }
                CelebrationType.PROFILE_SAVED -> {
                    snackbarHostState.showSnackbar("Your profile has been saved! 🎨")
                }
                CelebrationType.SPARKY_TALK -> {
                    // minor companion trigger
                }
                else -> {}
            }
        }
    }

    // Gradient background representing sky / fantasy playground
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9), // Gentle mint green top
                        Color(0xFFE1F5FE), // Soft celestial blue
                        Color(0xFFFFF9C4)  // Buttercup sunny yellow bottom
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Render Confetti Layer above everything
        ParticleConfetti(triggerTime = triggerConfetti)

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Adaptive container limit for tablets
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 650.dp)
                        .align(Alignment.TopCenter)
                ) {
                    when (currentScreen) {
                        KidsScreen.DASHBOARD -> {
                            DashboardView(
                                profile = profile,
                                unlockedBadges = unlockedBadges,
                                onNavigate = { screen ->
                                    currentScreen = screen
                                    // Start quiz state when shifting to those screens
                                    when (screen) {
                                        KidsScreen.MATH_QUEST -> viewModel.startMathQuest()
                                        KidsScreen.WORD_SAFARI -> viewModel.startWordSafari()
                                        KidsScreen.SCIENCE_EXPLORER -> viewModel.startCosmicScience()
                                        else -> {}
                                    }
                                }
                            )
                        }
                        KidsScreen.MATH_QUEST -> {
                            QuestQuizView(
                                title = "Math Cosmos 🧮",
                                stateFlow = viewModel.mathState,
                                onSubmit = { ans -> viewModel.submitAnswer(ans, "math") },
                                onNext = { viewModel.nextQuestion("math") },
                                onExit = {
                                    viewModel.resetQuiz("math")
                                    currentScreen = KidsScreen.DASHBOARD
                                },
                                sparkyTutorHelpFlow = viewModel.sparkyTutorHelp,
                                isSparkyTutorThinkingFlow = viewModel.isSparkyTutorThinking,
                                onGetHelp = { questionText, options, correctAnswer, isHint, selectedOption ->
                                    viewModel.fetchSparkyQuizHelp(questionText, options, correctAnswer, isHint, selectedOption)
                                },
                                onClearHelp = { viewModel.clearSparkyQuizHelp() }
                            )
                        }
                        KidsScreen.WORD_SAFARI -> {
                            QuestQuizView(
                                title = "Word Safari 🦁",
                                stateFlow = viewModel.wordState,
                                onSubmit = { ans -> viewModel.submitAnswer(ans, "word") },
                                onNext = { viewModel.nextQuestion("word") },
                                onExit = {
                                    viewModel.resetQuiz("word")
                                    currentScreen = KidsScreen.DASHBOARD
                                },
                                sparkyTutorHelpFlow = viewModel.sparkyTutorHelp,
                                isSparkyTutorThinkingFlow = viewModel.isSparkyTutorThinking,
                                onGetHelp = { questionText, options, correctAnswer, isHint, selectedOption ->
                                    viewModel.fetchSparkyQuizHelp(questionText, options, correctAnswer, isHint, selectedOption)
                                },
                                onClearHelp = { viewModel.clearSparkyQuizHelp() }
                            )
                        }
                        KidsScreen.SCIENCE_EXPLORER -> {
                            QuestQuizView(
                                title = "Space Explorer 🚀",
                                stateFlow = viewModel.scienceState,
                                onSubmit = { ans -> viewModel.submitAnswer(ans, "science") },
                                onNext = { viewModel.nextQuestion("science") },
                                onExit = {
                                    viewModel.resetQuiz("science")
                                    currentScreen = KidsScreen.DASHBOARD
                                },
                                sparkyTutorHelpFlow = viewModel.sparkyTutorHelp,
                                isSparkyTutorThinkingFlow = viewModel.isSparkyTutorThinking,
                                onGetHelp = { questionText, options, correctAnswer, isHint, selectedOption ->
                                    viewModel.fetchSparkyQuizHelp(questionText, options, correctAnswer, isHint, selectedOption)
                                },
                                onClearHelp = { viewModel.clearSparkyQuizHelp() }
                            )
                        }
                        KidsScreen.AI_TUTOR -> {
                            AITutorView(
                                chatHistory = chatHistory,
                                isThinking = isSparkyThinking,
                                onAskQuestion = { prompt -> viewModel.askSparkyQuestion(prompt) },
                                onClearChat = { viewModel.clearChat() },
                                onBack = { currentScreen = KidsScreen.DASHBOARD }
                            )
                        }
                        KidsScreen.BADGE_CABINET -> {
                            BadgeCabinetView(
                                unlockedBadges = unlockedBadges,
                                onBack = { currentScreen = KidsScreen.DASHBOARD }
                            )
                        }
                        KidsScreen.AVATAR_CUSTOMIZER -> {
                            AvatarCustomizerView(
                                currentProfile = profile,
                                onSave = { name, avatar, color ->
                                    viewModel.updateProfile(name, avatar, color)
                                    currentScreen = KidsScreen.DASHBOARD
                                },
                                onBack = { currentScreen = KidsScreen.DASHBOARD }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- DASHBOARD VIEW ---
@Composable
fun DashboardView(
    profile: ChildProfile?,
    unlockedBadges: List<UnlockedBadge>,
    onNavigate: (KidsScreen) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
    ) {
        item {
            // Profile & Streak Card
            ProfileHeaderCard(profile = profile, onEditClick = { onNavigate(KidsScreen.AVATAR_CUSTOMIZER) })
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section header
        item {
            Text(
                text = "Choose Your Adventure! ⭐",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = Color(0xFF37474F),
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        // Adventure Grids
        item {
            AdventureCard(
                title = "Math Cosmos",
                description = "Solve puzzle numbers, earn shiny stars, and unlock custom equations!",
                emoji = "🧮",
                accentColor = Color(0xFFFF7043),
                levelString = "Dynamic Levels",
                onClick = { onNavigate(KidsScreen.MATH_QUEST) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            AdventureCard(
                title = "Word Safari",
                description = "Spell cute animal names, find lost letters, and traverse the typing jungle!",
                emoji = "🦁",
                accentColor = Color(0xFF66BB6A),
                levelString = "Spelling Fun",
                onClick = { onNavigate(KidsScreen.WORD_SAFARI) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            AdventureCard(
                title = "Space Science",
                description = "Hop in a rocket, answer space questions, and discover planets!",
                emoji = "🚀",
                accentColor = Color(0xFF26C6DA),
                levelString = "Star Exploration",
                onClick = { onNavigate(KidsScreen.SCIENCE_EXPLORER) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            AdventureCard(
                title = "Sparky AI Companion",
                description = "Ask our cute dinosaur mascot any question you can dream of!",
                emoji = "🦖",
                accentColor = Color(0xFF8D6E63),
                levelString = "Live AI Tutor",
                onClick = { onNavigate(KidsScreen.AI_TUTOR) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Dashboard Panel Shortcuts
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(KidsScreen.BADGE_CABINET) }
                        .testTag("dashboard_badges_btn"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF59D)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("🏅", fontSize = 34.sp)
                        Text(
                            "My Badges",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF5D4037)
                        )
                        Text(
                            "${unlockedBadges.size} Unlocked",
                            fontSize = 12.sp,
                            color = Color(0xFF795548)
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(KidsScreen.AVATAR_CUSTOMIZER) }
                        .testTag("dashboard_avatar_btn"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFCE93D8)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("🎨", fontSize = 34.sp)
                        Text(
                            "Avatar Lab",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF4A148C)
                        )
                        Text(
                            "Customize Me",
                            fontSize = 12.sp,
                            color = Color(0xFF6A1B9A)
                        )
                    }
                }
            }
        }
    }
}

// --- PROFILE EXPOSITION CARD ---
@Composable
fun ProfileHeaderCard(
    profile: ChildProfile?,
    onEditClick: () -> Unit
) {
    val mascotEmoji = when (profile?.avatar) {
        "dino" -> "🦖"
        "rocket" -> "🚀"
        "kitten" -> "🐱"
        "wizard" -> "🧙‍♂️"
        else -> "🚀"
    }
    
    val profileThemeColor = try {
        Color(android.graphics.Color.parseColor(profile?.avatarColor ?: "#4CAF50"))
    } catch (e: Exception) {
        Color(0xFF4CAF50)
    }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(28.dp))
            .testTag("profile_card")
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Large Avatar Mascots with color ring
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(profileThemeColor.copy(alpha = 0.2f), CircleShape)
                    .border(3.dp, profileThemeColor, CircleShape)
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = mascotEmoji, fontSize = 44.sp)
                // tiny edit pencil badge
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd)
                        .background(profileThemeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✏️", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile?.name ?: "Star Cadet",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF37474F)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Streak badge
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFFFECEB), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${profile?.streak ?: 1} Day",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE64A19)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Level ${profile?.level ?: 1} Cadet",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = profileThemeColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                // XP Progress Slider
                val currentXp = profile?.xp ?: 0
                val level = profile?.level ?: 1
                val baseLevelXp = (level - 1) * 100
                val relativeXp = currentXp - baseLevelXp
                val progressFraction = (relativeXp / 100f).coerceIn(0f, 1f)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(CircleShape),
                        color = profileThemeColor,
                        trackColor = Color(0xFFECEFF1)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$relativeXp/100 XP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF78909C)
                    )
                }
            }
        }

        // Bottom horizontal stats
        HorizontalDivider(color = Color(0xFFECEFF1), thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFAFAFA))
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${profile?.stars ?: 0} Stars",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFA000)
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(Color(0xFFE0E0E0))
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📈", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${profile?.xp ?: 0} Total XP",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF689F38)
                )
            }
        }
    }
}

// --- ADVENTURE SELECTION CARD COMPONENT ---
@Composable
fun AdventureCard(
    title: String,
    description: String,
    emoji: String,
    accentColor: Color,
    levelString: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .testTag("adventure_card_$title")
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF37474F)
                    )
                    Text(
                        text = levelString,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = accentColor,
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = Color(0xFF78909C),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// --- QUEST QUIZ PLAYER VIEW ---
@Composable
fun QuestQuizView(
    title: String,
    stateFlow: StateFlow<QuizState>,
    onSubmit: (String) -> Unit,
    onNext: () -> Unit,
    onExit: () -> Unit,
    sparkyTutorHelpFlow: StateFlow<String?>,
    isSparkyTutorThinkingFlow: StateFlow<Boolean>,
    onGetHelp: (questionText: String, options: List<String>, correctAnswer: String, isHint: Boolean, selectedOption: String?) -> Unit,
    onClearHelp: () -> Unit
) {
    val state by stateFlow.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Back Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onExit,
                modifier = Modifier.testTag("exit_quiz_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Exit Quiz")
            }
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color(0xFF37474F)
            )
            Text(
                text = "Exit 🚪",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE53935),
                modifier = Modifier.clickable { onExit() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val s = state) {
            is QuizState.Idle -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is QuizState.Question -> {
                // Progress node tracker
                val relativeProgress = (s.questionNum - 1) / s.totalQuestions.toFloat()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Question ${s.questionNum} of ${s.totalQuestions}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF78909C)
                        )
                        Text(
                            text = "⭐ +2 per correct answer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFFFA000)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { relativeProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFECEFF1)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Question bubble container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = s.emoji,
                            fontSize = 68.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = s.text,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = Color(0xFF263238),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("quiz_question_text")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Options Row/Grids
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    s.options.forEach { option ->
                        val isSelected = s.selectedOption == option
                        val isCorrectAns = option == s.correctAnswer
                        val answered = s.isCorrectlyAnswered != null

                        val cardColor = when {
                            answered && isCorrectAns -> Color(0xFFC8E6C9)  // Green highlight for correct
                            answered && isSelected && !isCorrectAns -> Color(0xFFFFCDD2) // Red highlight for incorrect chosen
                            isSelected -> Color(0xFFBBDEFB) // Basic selection highlight
                            else -> Color.White
                        }

                        val strokeColor = when {
                            answered && isCorrectAns -> Color(0xFF4CAF50)
                            answered && isSelected && !isCorrectAns -> Color(0xFFF44336)
                            isSelected -> Color(0xFF2196F3)
                            else -> Color(0xFFCFD8DC)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !answered) { onSubmit(option) }
                                .border(2.dp, strokeColor, RoundedCornerShape(16.dp))
                                .testTag("quiz_option_$option"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = option,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF37474F)
                                )
                                if (answered) {
                                    if (isCorrectAns) {
                                        Text("Correct! 🎉", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    } else if (isSelected) {
                                        Text("Aww! 🦕", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                    }
                                }
                            }
                        }
                    }
                }

                // AI Tutor section representing personal assistance
                val helpText by sparkyTutorHelpFlow.collectAsStateWithLifecycle()
                val isThinking by isSparkyTutorThinkingFlow.collectAsStateWithLifecycle()

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(20.dp))
                        .testTag("sparky_quiz_tutor_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                    border = BorderStroke(2.dp, Color(0xFFC5E1A5))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🦖", fontSize = 32.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sparky's Tutor Corner",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF33691E)
                                )
                                Text(
                                    text = "Need a helper hint or quiz study?",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color(0xFF558B2F)
                                )
                            }
                            if (helpText != null) {
                                IconButton(
                                    onClick = onClearHelp,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear tutor help",
                                        tint = Color(0xFF558B2F)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isThinking) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color(0xFF558B2F),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Consulting dinosaur scrolls... 📜✨",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF558B2F)
                                    )
                                )
                            }
                        } else if (helpText != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = helpText ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF33691E)
                                    )
                                )
                            }
                        } else {
                            val unanswered = s.isCorrectlyAnswered == null
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (unanswered) {
                                    Button(
                                        onClick = {
                                            onGetHelp(s.text, s.options, s.correctAnswer, true, null)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).testTag("quiz_get_hint_btn")
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Get a Hint! 💡",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    val wasIncorrect = s.isCorrectlyAnswered == false
                                    Button(
                                        onClick = {
                                            onGetHelp(s.text, s.options, s.correctAnswer, false, s.selectedOption)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (wasIncorrect) Color(0xFFFFB300) else Color(0xFF81C784)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).testTag("quiz_get_explain_btn")
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (wasIncorrect) "Explain for Me! 🦖" else "Learn More! 📜",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Show Next/Result button
                if (s.isCorrectlyAnswered != null) {
                    KidsButton(
                        text = if (s.questionNum < s.totalQuestions) "Next Challenge 🚀" else "Show Results! 🏆",
                        onClick = onNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        testTag = "quiz_next_button"
                    )
                }
            }

            is QuizState.Complete -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (s.score == s.maxScore) "👑 UNBELIEVABLE! PERFECT!" else "🎉 Adventure Finished! 🎉",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = Color(0xFFE65100),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(Color(0xFFFFF9C4), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (s.score == s.maxScore) "🥇" else "🏅",
                            fontSize = 74.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Score: ${s.score} / ${s.maxScore} correct!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color(0xFF37474F)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("⭐", fontSize = 24.sp)
                                    Text("+${s.starsEarned} Stars", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFFFA000))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📈", fontSize = 24.sp)
                                    Text("+${s.xpEarned} XP", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF4CAF50))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    KidsButton(
                        text = "Back to Map 🗺️",
                        onClick = onExit,
                        testTag = "quiz_exit_complete_btn"
                    )
                }
            }
        }
    }
}

// --- AMBIENT AI TUTOR CHAT SCREEN ---
@Composable
fun AITutorView(
    chatHistory: List<ChatMessage>,
    isThinking: Boolean,
    onAskQuestion: (String) -> Unit,
    onClearChat: () -> Unit,
    onBack: () -> Unit
) {
    var promptText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to latest chat automatically when history changes
    LaunchedEffect(chatHistory.size, isThinking) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("ai_back_btn")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Sparky's Dino Lab 🦖",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color(0xFF37474F)
            )
            Text(
                text = "Clear 🗑️",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF78909C),
                modifier = Modifier.clickable { onClearChat() }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sparky Avatar Banner with bouncy states
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SparkyAvatar(isThinking = isThinking, modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your Friendly Learning buddy!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "I answer all science, space, and math marvel secrets with fun dinosaur stories! Roaaar!",
                    fontSize = 11.sp,
                    color = Color(0xFF555555),
                    lineHeight = 14.sp
                )
            }
        }

        // Suggestion Chips / Fun prompt suggestions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "Why is sky blue? ☀️",
                "Where does rain go? 🌧️",
                "Tell dino story! 🦖",
                "How do leaves work? 🌿",
                "Who made the moon? 🌙"
            )
            suggestions.forEach { sug ->
                SuggestionChip(
                    onClick = { onAskQuestion(sug) },
                    label = { Text(sug, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("chip_$sug")
                )
            }
        }

        // Chat Conversation history box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFFECEFF1), RoundedCornerShape(20.dp))
                .padding(12.dp)
        ) {
            if (chatHistory.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💬", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Sparky is ready! Ask me anything!",
                        style = TextStyle(fontWeight = FontWeight.Bold, color = Color(0xFF78909C))
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatHistory) { msg ->
                        val isChild = msg.sender == "child"
                        val alignment = if (isChild) Alignment.End else Alignment.Start
                        val bubbleColor = if (isChild) Color(0xFFDCF8C6) else Color(0xFFE1F5FE)
                        val textAndAccentColor = if (isChild) Color(0xFF2E7D32) else Color(0xFF0288D1)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = alignment
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isChild) 16.dp else 2.dp,
                                    bottomEnd = if (isChild) 2.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Render rich format (bold, symbols, etc.)
                                    Text(
                                        text = msg.text,
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp,
                                            color = Color(0xFF263238),
                                            fontWeight = if (isChild) FontWeight.Normal else FontWeight.Medium
                                        )
                                    )
                                }
                            }
                            Text(
                                text = if (isChild) "You" else "Sparky",
                                fontSize = 10.sp,
                                color = textAndAccentColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Keyboard Panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = promptText,
                onValueChange = { promptText = it },
                placeholder = { Text("Ask Sparky a secret...", fontSize = 14.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(24.dp))
                    .testTag("ai_prompt_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (promptText.isNotBlank()) {
                        onAskQuestion(promptText)
                        promptText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF4CAF50), CircleShape)
                    .testTag("ai_send_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// --- BADGE CABINET LIST VIEW ---
@Composable
fun BadgeCabinetView(
    unlockedBadges: List<UnlockedBadge>,
    onBack: () -> Unit
) {
    // Definitive list of all possible earnable badges
    val allBadges = listOf(
        UnlockedBadge("badge_math_first", "Math Rookie", "Completed your first Math Cosmos adventure!", "calculate"),
        UnlockedBadge("badge_math_perfect", "Math Wizard", "Got a perfect score in Math Cosmos!", "insights"),
        UnlockedBadge("badge_word_first", "Word Scout", "Spelled your first words in Alphabet Camp!", "spellcheck"),
        UnlockedBadge("badge_word_perfect", "Vocabulary Master", "Spelled all words flawlessly in Word Safari!", "emoji_events"),
        UnlockedBadge("badge_science_first", "Star Voyager", "Finished your first Space Science trivia quiz!", "rocket_launch"),
        UnlockedBadge("badge_lvl2", "Rising Comet", "Reached level 2! Keep climbing the universe!", "stars"),
        UnlockedBadge("badge_lvl5", "Galaxy Brain", "Reached level 5! You are absolutely stellar!", "school"),
        UnlockedBadge("badge_curious", "Curious Explorer", "Asked your very first questions to Sparky the Dino!", "emoji_nature")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("badge_back_btn")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Cadet Badge Cabinet 🏅",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Color(0xFF37474F),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🏆", fontSize = 36.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Adventures Champion",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF37474F)
                    )
                    Text(
                        "You have unlocked ${unlockedBadges.size} of ${allBadges.size} badges of universe travel!",
                        fontSize = 12.sp,
                        color = Color(0xFF78909C)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allBadges) { badge ->
                val isUnlocked = unlockedBadges.any { it.id == badge.id }
                BadgeMedal(
                    title = badge.title,
                    description = badge.description,
                    icon = badge.iconName,
                    isUnlocked = isUnlocked,
                    onTap = {}
                )
            }
        }
    }
}

// --- AVATAR LAYOUT CUSTOMIZER SHEET ---
@Composable
fun AvatarCustomizerView(
    currentProfile: ChildProfile?,
    onSave: (String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var nameInput by remember { mutableStateOf(currentProfile?.name ?: "Little Cadet") }
    var selectedMascot by remember { mutableStateOf(currentProfile?.avatar ?: "dino") }
    var selectedColorHex by remember { mutableStateOf(currentProfile?.avatarColor ?: "#4CAF50") }

    val mascots = listOf(
        Pair("dino", "🦖 Dino Sparky"),
        Pair("rocket", "🚀 Star Shuttle"),
        Pair("kitten", "🐱 Kitten Whiskers"),
        Pair("wizard", "🧙‍♂️ Spell Wizard")
    )

    val colors = listOf(
        "#4CAF50", // Green
        "#FF5722", // Deep Orange
        "#2196F3", // Blue
        "#9C27B0", // Purple
        "#FFC107"  // Gold Amber
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("avatar_back_btn")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Cadet Customize Hub 🎨",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Color(0xFF37474F),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large Preview panel
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Color(android.graphics.Color.parseColor(selectedColorHex)).copy(alpha = 0.2f),
                            CircleShape
                        )
                        .border(4.dp, Color(android.graphics.Color.parseColor(selectedColorHex)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (selectedMascot) {
                            "dino" -> "🦖"
                            "rocket" -> "🚀"
                            "kitten" -> "🐱"
                            "wizard" -> "🧙‍♂️"
                            else -> "🐱"
                        },
                        fontSize = 58.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = nameInput.ifBlank { "Star Cadet" },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = Color(0xFF37474F)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Name builder
        Text(
            text = "Your Nickname:",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF555555),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        TextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFCFD8DC), RoundedCornerShape(16.dp))
                .testTag("avatar_name_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Choose mascot
        Text(
            text = "Choose Mascot Theme:",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF555555),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            mascots.forEach { mascot ->
                FilterChip(
                    selected = selectedMascot == mascot.first,
                    onClick = { selectedMascot = mascot.first },
                    label = { Text(mascot.second, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("mascot_chip_${mascot.first}")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Choose color
        Text(
            text = "Select Flavor Accent Color:",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF555555),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            colors.forEach { hex ->
                val col = Color(android.graphics.Color.parseColor(hex))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(col, CircleShape)
                        .border(
                            width = if (selectedColorHex == hex) 4.dp else 0.dp,
                            color = if (selectedColorHex == hex) Color.White else Color.Transparent,
                            shape = CircleShape
                        )
                        .shadow(2.dp, CircleShape)
                        .clickable { selectedColorHex = hex }
                        .testTag("color_circle_$hex")
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        KidsButton(
            text = "Save Profile ✨",
            onClick = { onSave(nameInput, selectedMascot, selectedColorHex) },
            containerColor = Color(android.graphics.Color.parseColor(selectedColorHex)),
            modifier = Modifier.fillMaxWidth(),
            testTag = "save_avatar_btn"
        )
    }
}
