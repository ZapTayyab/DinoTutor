package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- Simple enum for screens ---
enum class KidsScreen {
    DASHBOARD,
    MATH_QUEST,
    WORD_SAFARI,
    SCIENCE_EXPLORER,
    AI_TUTOR,
    BADGE_CABINET,
    AVATAR_CUSTOMIZER
}

// --- Dynamic Particle Confetti for Kids' success ---
@Composable
fun ParticleConfetti(triggerTime: Long) {
    if (triggerTime == 0L) return

    val particles = remember(triggerTime) {
        List(40) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f, // start slightly offscreen at the top
                size = Random.nextFloat() * 15f + 10f,
                color = Color(
                    red = Random.nextFloat().coerceIn(0.2f, 1f),
                    green = Random.nextFloat().coerceIn(0.2f, 1f),
                    blue = Random.nextFloat().coerceIn(0.2f, 1f),
                    alpha = 1f
                ),
                speedY = Random.nextFloat() * 6f + 4f,
                speedX = Random.nextFloat() * 4f - 2f,
                rotation = Random.nextFloat() * 360f,
                rotSpeed = Random.nextFloat() * 10f - 5f
            )
        }
    }

    var animate by remember(triggerTime) { mutableStateOf(true) }

    LaunchedEffect(triggerTime) {
        animate = true
        delay(2500) // animate for 2.5 seconds
        animate = false
    }

    if (animate) {
        val infiniteTransition = rememberInfiniteTransition(label = "confetti_anim")
        val progress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "confetti_progress"
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("confetti_canvas")
        ) {
            particles.forEach { p ->
                val currentY = (p.y + progress * p.speedY * 0.4f) * size.height
                val currentX = (p.x + progress * p.speedX * 0.1f) * size.width
                val currentAngle = p.rotation + progress * p.rotSpeed * 100f
                
                if (currentY < size.height) {
                    drawContext.canvas.save()
                    drawContext.canvas.translate(currentX, currentY)
                    drawContext.canvas.rotate(currentAngle)
                    
                    // Draw a star or rectangle depending on custom modulo
                    if (p.size.toInt() % 2 == 0) {
                        drawRect(
                            color = p.color,
                            size = androidx.compose.ui.geometry.Size(p.size, p.size * 1.5f)
                        )
                    } else {
                        drawCircle(
                            color = p.color,
                            radius = p.size / 2f
                        )
                    }
                    drawContext.canvas.restore()
                }
            }
        }
    }
}

class ConfettiParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val speedY: Float,
    val speedX: Float,
    val rotation: Float,
    val rotSpeed: Float
)

// --- Cute general animated button helper ---
@Composable
fun KidsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    testTag: String = "kids_button"
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "btn_scale"
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        modifier = modifier
            .scale(scale)
            .testTag(testTag),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.2f),
                    offset = Offset(1f, 2f),
                    blurRadius = 2f
                )
            )
        )
    }
}

// --- Animated Companion Avatar with pulsing eyes and bounce ---
@Composable
fun SparkyAvatar(
    isThinking: Boolean,
    modifier: Modifier = Modifier
) {
    val duration = if (isThinking) 350 else 1000
    val infiniteTransition = rememberInfiniteTransition(label = "sparky_anim")
    
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isThinking) -15f else -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparky_bounce"
    )

    val animScaleX by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isThinking) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparky_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                translationY = bounceY
                scaleX = animScaleX
            }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(
                    color = Color(0xFF4CAF50), // Lively Dino Green
                    shape = RoundedCornerShape(32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Little cute Dino Horns / Spikes on top using small yellow circles relative to parent
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Orange spikes on forehead
                Box(
                    modifier = Modifier
                        .offset(x = 24.dp, y = (-4).dp)
                        .size(14.dp)
                        .background(Color(0xFFFF9800), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .offset(x = 42.dp, y = (-6).dp)
                        .size(16.dp)
                        .background(Color(0xFFFF9800), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .offset(x = 60.dp, y = (-4).dp)
                        .size(14.dp)
                        .background(Color(0xFFFF9800), CircleShape)
                )

                // Face / Cheek Blushing
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = 10.dp, y = 14.dp)
                        .size(14.dp)
                        .background(Color(0xFFFF8A80), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = (-10).dp, y = 14.dp)
                        .size(14.dp)
                        .background(Color(0xFFFF8A80), CircleShape)
                )
            }

            // Eyes Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left eye
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.Black, CircleShape)
                    )
                }
                // Right eye
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.Black, CircleShape)
                    )
                }
            }

            // Smiling Dino Snout / Mouth
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-14).dp)
                    .size(width = 24.dp, height = 10.dp)
                    .background(Color(0xFF388E3C), RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp))
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = if (isThinking) "Sparky typing..." else "Sparky the Dino",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isThinking) Color(0xFFFF9800) else Color(0xFF666666)
            )
        )
    }
}

// --- Beautiful Badge Medal Widget ---
@Composable
fun BadgeMedal(
    title: String,
    description: String,
    icon: String,
    isUnlocked: Boolean,
    onTap: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFFFFF9C4) else Color(0xFFEEEEEE)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(bottom = 8.dp)
            .testTag("badge_card_$title"),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        color = if (isUnlocked) Color(0xFFFFF176) else Color(0xFFBDBDBD),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (icon) {
                        "calculate" -> "🧮"
                        "spellcheck" -> "🐝"
                        "insights" -> "🧠"
                        "rocket_launch" -> "🚀"
                        "emoji_events" -> "🏆"
                        else -> "🌟"
                    },
                    fontSize = 28.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = if (isUnlocked) Color(0xFF5D4037) else Color(0xFF757575)
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = if (isUnlocked) Color(0xFF795548) else Color(0xFF9E9E9E),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (isUnlocked) {
                Text(
                    text = "Unlocked! 🎉",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            } else {
                Text(
                    text = "Locked 🔒",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
