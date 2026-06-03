package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.RetrofitClient
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

// Sealed UI States for Quizzes
sealed interface QuizState {
    object Idle : QuizState
    data class Question(
        val questionNum: Int,
        val totalQuestions: Int,
        val text: String,
        val emoji: String,
        val options: List<String>,
        val correctAnswer: String,
        val isCorrectlyAnswered: Boolean? = null, // null = unanswered, true/false = status
        val selectedOption: String? = null
    ) : QuizState
    data class Complete(val score: Int, val maxScore: Int, val starsEarned: Int, val xpEarned: Int) : QuizState
}

class LearningViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = LearningRepository(db)

    // Flow observations
    val profile: StateFlow<ChildProfile?> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val unlockedBadges: StateFlow<List<UnlockedBadge>> = repository.unlockedBadgesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityLogs: StateFlow<List<ActivityLog>> = repository.activityLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatHistory: StateFlow<List<ChatMessage>> = repository.chatHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state flows
    private val _mathState = MutableStateFlow<QuizState>(QuizState.Idle)
    val mathState: StateFlow<QuizState> = _mathState.asStateFlow()

    private val _wordState = MutableStateFlow<QuizState>(QuizState.Idle)
    val wordState: StateFlow<QuizState> = _wordState.asStateFlow()

    private val _scienceState = MutableStateFlow<QuizState>(QuizState.Idle)
    val scienceState: StateFlow<QuizState> = _scienceState.asStateFlow()

    private val _isSparkyThinking = MutableStateFlow(false)
    val isSparkyThinking: StateFlow<Boolean> = _isSparkyThinking.asStateFlow()

    // AI Tutor component states
    private val _sparkyTutorHelp = MutableStateFlow<String?>(null)
    val sparkyTutorHelp: StateFlow<String?> = _sparkyTutorHelp.asStateFlow()

    private val _isSparkyTutorThinking = MutableStateFlow(false)
    val isSparkyTutorThinking: StateFlow<Boolean> = _isSparkyTutorThinking.asStateFlow()

    // Celebration indicator
    private val _celebrationEvent = MutableSharedFlow<CelebrationType>()
    val celebrationEvent: SharedFlow<CelebrationType> = _celebrationEvent.asSharedFlow()

    // Temporary storage for quiz lists
    private var currentQuizIndex = 0
    private var quizScore = 0
    private var questionsList = emptyList<QuizQuestion>()

    init {
        viewModelScope.launch {
            repository.getOrCreateProfile()
        }
    }

    // --- Profile customizer ---
    fun updateProfile(name: String, avatar: String, colorHex: String) {
        viewModelScope.launch {
            repository.updateProfileNameAndAvatar(name, avatar, colorHex)
            triggerCelebration(CelebrationType.PROFILE_SAVED)
        }
    }

    // --- Math Quest Quiz Logic ---
    fun startMathQuest() {
        currentQuizIndex = 0
        quizScore = 0
        // Generate 5 addition/subtraction questions based on current profile level
        val currentLevel = profile.value?.level ?: 1
        val list = mutableListOf<QuizQuestion>()
        for (i in 1..5) {
            val isAdd = Random.nextBoolean()
            val numA = Random.nextInt(2 * currentLevel, 5 * currentLevel + 5)
            val numB = Random.nextInt(1, 4 * currentLevel + 3)
            val questionText: String
            val answer: Int
            if (isAdd) {
                questionText = "$numA + $numB = ?"
                answer = numA + numB
            } else {
                val larger = maxOf(numA, numB)
                val smaller = minOf(numA, numB)
                questionText = "$larger - $smaller = ?"
                answer = larger - smaller
            }
            
            // Build options
            val options = mutableSetOf(answer.toString())
            while (options.size < 4) {
                options.add((answer + Random.nextInt(-5, 6).let { if (it == 0) 3 else it }).coerceAtLeast(0).toString())
            }
            list.add(
                QuizQuestion(
                    text = questionText,
                    emoji = listOf("🍎", "🍊", "🍰", "⭐", "🦕", "🍕", "🎈").random(),
                    options = options.toList().shuffled(),
                    answer = answer.toString()
                )
            )
        }
        questionsList = list
        presentQuizQuestion(0, _mathState)
    }

    // --- Word Safari Quiz Logic ---
    fun startWordSafari() {
        currentQuizIndex = 0
        quizScore = 0
        // static list of word questions
        val words = listOf(
            WordQuizTemplate("APPLE", "🍎", 'P'),
            WordQuizTemplate("TIGER", "🐯", 'G'),
            WordQuizTemplate("OCEAN", "🌊", 'C'),
            WordQuizTemplate("LEMON", "🍋", 'M'),
            WordQuizTemplate("SPACE", "🚀", 'A'),
            WordQuizTemplate("DINO", "🦖", 'I'),
            WordQuizTemplate("FROG", "🐸", 'R'),
            WordQuizTemplate("BALL", "⚽", 'L'),
            WordQuizTemplate("CAKE", "🍰", 'K'),
            WordQuizTemplate("BIRD", "🐦", 'I')
        ).shuffled().take(5)

        val list = words.map { w ->
            val cleanWord = w.word
            val missingLet = w.missingChar
            val displayWord = cleanWord.replaceFirst(missingLet, '_')
            
            // Build 4 choice options (A-Z characters)
            val options = mutableSetOf(missingLet.toString())
            while (options.size < 4) {
                options.add(((Random.nextInt(26) + 65).toChar()).toString())
            }

            QuizQuestion(
                text = "What letter finishes the word:\n$displayWord",
                emoji = w.emoji,
                options = options.toList().shuffled(),
                answer = missingLet.toString()
            )
        }
        questionsList = list
        presentQuizQuestion(0, _wordState)
    }

    // --- Cosmic Science Quiz Logic ---
    fun startCosmicScience() {
        currentQuizIndex = 0
        quizScore = 0
        val scienceQuestions = listOf(
            QuizQuestion("Which giant yellow star gives us light and warmth during daytime?", "☀️", listOf("The Moon", "The Sun", "planet Mars", "Jupiter"), "The Sun"),
            QuizQuestion("What celestial body glows in the night sky and orbits around Earth?", "🌙", listOf("The Moon", "A Comet", "The Sun", "Saturn"), "The Moon"),
            QuizQuestion("Which beautiful planet do we currently call home with water, oxygen, and life?", "🌍", listOf("Mars", "Venus", "Earth", "Neptune"), "Earth"),
            QuizQuestion("What space vehicle do highly trained astronauts ride to explore outer space?", "🚀", listOf("A Submarine", "A Space Rocket", "A Cable Car", "A Biplane"), "A Space Rocket"),
            QuizQuestion("Which red, dusty planet is next door to Earth and is guarded by robots?", "🔴", listOf("Mars", "Mercury", "Venus", "Pluto"), "Mars"),
            QuizQuestion("What gorgeous planet has giant, glistening rings made of spinning ice and rocks?", "🪐", listOf("Jupiter", "Saturn", "Uranus", "Mars"), "Saturn")
        ).shuffled().take(5)

        questionsList = scienceQuestions
        presentQuizQuestion(0, _scienceState)
    }

    private fun getFlowByType(type: String): MutableStateFlow<QuizState> {
        return when (type) {
            "math" -> _mathState
            "word" -> _wordState
            else -> _scienceState
        }
    }

    private fun presentQuizQuestion(idx: Int, flow: MutableStateFlow<QuizState>) {
        if (idx < questionsList.size) {
            val q = questionsList[idx]
            flow.value = QuizState.Question(
                questionNum = idx + 1,
                totalQuestions = questionsList.size,
                text = q.text,
                emoji = q.emoji,
                options = q.options,
                correctAnswer = q.answer,
                isCorrectlyAnswered = null,
                selectedOption = null
            )
        }
    }

    fun submitAnswer(selected: String, type: String) {
        val flow = getFlowByType(type)
        val currState = flow.value
        if (currState is QuizState.Question) {
            val correct = selected == currState.correctAnswer
            if (correct) {
                quizScore++
                viewModelScope.launch {
                    _celebrationEvent.emit(CelebrationType.CORRECT_ANSWER)
                }
            } else {
                viewModelScope.launch {
                    _celebrationEvent.emit(CelebrationType.WRONG_ANSWER)
                }
            }
            flow.value = currState.copy(
                isCorrectlyAnswered = correct,
                selectedOption = selected
            )
        }
    }

    fun nextQuestion(type: String) {
        clearSparkyQuizHelp()
        val flow = getFlowByType(type)
        currentQuizIndex++
        if (currentQuizIndex < questionsList.size) {
            presentQuizQuestion(currentQuizIndex, flow)
        } else {
            // Quiz completed!
            val starsEarned = quizScore * 2
            val xpEarned = quizScore * 20
            viewModelScope.launch {
                repository.addXpandStars(
                    xpEarned = xpEarned,
                    starsEarned = starsEarned,
                    activityType = type,
                    score = quizScore,
                    maxScore = questionsList.size
                )
                flow.value = QuizState.Complete(
                    score = quizScore,
                    maxScore = questionsList.size,
                    starsEarned = starsEarned,
                    xpEarned = xpEarned
                )
                if (quizScore == questionsList.size) {
                    _celebrationEvent.emit(CelebrationType.LEVEL_COMPLETE_PERFECT)
                } else {
                    _celebrationEvent.emit(CelebrationType.LEVEL_COMPLETE)
                }
            }
        }
    }

    fun resetQuiz(type: String) {
        clearSparkyQuizHelp()
        getFlowByType(type).value = QuizState.Idle
    }

    // --- AI Tutor Methods ---
    fun fetchSparkyQuizHelp(
        questionText: String,
        options: List<String>,
        correctAnswer: String,
        isHint: Boolean,
        selectedOption: String? = null
    ) {
        viewModelScope.launch {
            _isSparkyTutorThinking.value = true
            _sparkyTutorHelp.value = null
            
            val result = com.example.api.RetrofitClient.getQuizHintOrExplanation(
                questionText = questionText,
                options = options,
                correctAnswer = correctAnswer,
                isHint = isHint,
                selectedOption = selectedOption
            )
            
            _sparkyTutorHelp.value = result
            _isSparkyTutorThinking.value = false
            _celebrationEvent.emit(CelebrationType.SPARKY_TALK)
        }
    }

    fun clearSparkyQuizHelp() {
        _sparkyTutorHelp.value = null
        _isSparkyTutorThinking.value = false
    }

    // --- AI Companion chat logic ---
    fun askSparkyQuestion(prompt: String) {
        if (prompt.isBlank()) return

        viewModelScope.launch {
            // Save child message
            repository.addChatMessage("child", prompt)
            _isSparkyThinking.value = true

            // Fetch compiled conversation for context
            val contextHistory = repository.chatHistoryFlow.firstOrNull() ?: emptyList()
            
            // Query API
            val sparkyReply = RetrofitClient.askSparky(contextHistory)
            
            _isSparkyThinking.value = false
            repository.addChatMessage("ai", sparkyReply)
            
            _celebrationEvent.emit(CelebrationType.SPARKY_TALK)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
            // Add tiny intro message from Sparky
            repository.addChatMessage("ai", "🦖 Roaaar! Hello friend! I am **Sparky your Dinosaur tutor**! Ask me any questions like: \"Why is grass green?\" or \"Where does rain come from?\" and let's explore together!")
        }
    }

    private fun triggerCelebration(type: CelebrationType) {
        viewModelScope.launch {
            _celebrationEvent.emit(type)
        }
    }
}

// Support classes
data class QuizQuestion(
    val text: String,
    val emoji: String,
    val options: List<String>,
    val answer: String
)

data class WordQuizTemplate(
    val word: String,
    val emoji: String,
    val missingChar: Char
)

enum class CelebrationType {
    CORRECT_ANSWER,
    WRONG_ANSWER,
    LEVEL_COMPLETE,
    LEVEL_COMPLETE_PERFECT,
    PROFILE_SAVED,
    SPARKY_TALK
}
