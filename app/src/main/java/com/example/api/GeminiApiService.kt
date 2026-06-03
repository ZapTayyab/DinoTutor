package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun askSparky(conversation: List<com.example.data.ChatMessage>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "🦖 *Sparky waves!* Oh boy! I cannot connect to the internet star-grid right now because my secret key is sleeping. Please tell an adult to add my **GEMINI_API_KEY** into the Secrets panel in AI Studio! Deeply roaring for joy!"
        }

        val systemPrompt = """
            You are Sparky, an incredibly friendly and adorable little green dinosaur who loves teaching children! 
            You are a wise and cheerful tutor for kids aged 5 to 10 years old.
            Always reply in a very playful, encouraging, simple, and child-safe manner.
            Keep responses brief and highly visual:
            - Use cute emojis often (🌟, 🦖, 🚀, 🎨, 🍎, 🦄).
            - **Bold** key educational concepts.
            - Break down explanations into tiny paragraphs.
            - STRICTLY avoid complicated industry jargon.
            - End every answer with a super quick, funny 1-question micro-quiz or challenge for the child to keep them engaged!
        """.trimIndent()

        // Gather last few messages for context
        val contextParts = conversation.map { msg ->
            val role = if (msg.sender == "child") "User" else "Model"
            Content(parts = listOf(Part(text = "$role: ${msg.text}")))
        }

        // Add final prompt explicitly
        val request = GeminiRequest(
            contents = contextParts,
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "🦖 Sparky scratching head: I couldn't find the answers in my fossil library! Let's try another question!"
        } catch (e: Exception) {
            "Error: ${e.message ?: "Could not connect to Sparky!"}\n🦖 Sparky says: Make sure your internet connection is active, friend!"
        }
    }

    suspend fun getQuizHintOrExplanation(
        questionText: String,
        options: List<String>,
        correctAnswer: String,
        isHint: Boolean,
        selectedOption: String? = null
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "🦖 *Sparky waves!* I can't connect to my space archives right now because my secret key is not set! Ask an adult to put the **GEMINI_API_KEY** in the Secrets panel in AI Studio."
        }

        val prompt = if (isHint) {
            """
            The child is working on a quiz question: "$questionText" 
            The options are: ${options.joinToString(", ")}.
            The correct answer is: "$correctAnswer".
            
            Please provide a playful and friendly HINT for the child. 
            DO NOT reveal the correct answer directly. 
            Guide them with an encouraging, cute clue or a mental picture that helps them think of the answer. 
            Keep it very simple (suited for kids aged 5 to 10), short, and full of cozy emojis (🌟, 🦖, 🍕)!
            """.trimIndent()
        } else {
            """
            The child answered a quiz question: "$questionText".
            The options were: ${options.joinToString(", ")}.
            The correct answer is: "$correctAnswer".
            ${if (selectedOption != null) "The child selected: \"$selectedOption\" which was incorrect." else ""}
            
            Please provide a friendly, loving, and encouraging EXPLANATION of why the correct answer is "$correctAnswer".
            Console them gently ("Aww, that's okay! We learn by trying!"), and explain the concept very simply.
            For math, use visual objects (e.g. "If you have 5 apples and get 3 more...").
            Keep it brief, warm, colorful, and end with a roaring cheer!
            """.trimIndent()
        }

        val systemInstruction = """
            You are Sparky, an incredibly friendly and adorable little green dinosaur who loves teaching children! 
            You are a wise and cheerful tutor for kids aged 5 to 10 years old.
            Always reply in a playful, encouraging, simple, and child-safe tone. 
            Keep explanations short, interactive, and easy to read with emojis and bullet points.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "🦖 Sparky scratch: Let's try again, friend!"
        } catch (e: Exception) {
            "Error: ${e.message ?: "Could not reach Sparky!"}\n🦖 Sparky says: Make sure your internet connection is active, friend!"
        }
    }
}
