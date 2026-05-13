package com.example.deskpet.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.deskpet.model.Personality
import com.example.deskpet.model.PetAction
import com.example.deskpet.model.PetProfile

@Composable
fun PetStage(
    profile: PetProfile,
    bubbleText: String,
    onPetClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = stageColors(profile.stageTheme)
    val infiniteTransition = rememberInfiniteTransition(label = "pet-stage-motion")
    val floatRange = movementRange(profile.personality, profile.actionHint)
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -floatRange,
        targetValue = floatRange,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (profile.action == PetAction.Listening) 2200 else 1450,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stage-float"
    )
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stage-breathe"
    )
    val shake by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (profile.personality == Personality.Foodie) 110 else 160),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stage-shake"
    )
    val actionScale by animateFloatAsState(
        targetValue = when (profile.action) {
            PetAction.Eating -> if (profile.personality == Personality.Foodie) 1.16f else 1.1f
            PetAction.Excited -> 1.16f
            PetAction.Happy, PetAction.Clicked -> 1.1f
            PetAction.Comforting -> 1.04f
            PetAction.Listening -> 1.01f
            else -> 1f
        },
        animationSpec = spring(),
        label = "stage-action-scale"
    )
    val rotation = when {
        profile.action == PetAction.Eating -> shake
        profile.action == PetAction.Excited -> shake * 0.7f
        profile.personality == Personality.Tsundere && profile.action in setOf(PetAction.Happy, PetAction.Clicked) -> -4f
        else -> 0f
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 308.dp, height = 342.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(Brush.verticalGradient(colors))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(238.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.58f), Color.Transparent)
                        )
                    )
            )

            if (profile.action == PetAction.Comforting) {
                Text(
                    text = "♡",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 34.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-26).dp)
                    .size(width = 154.dp, height = 24.dp)
                    .graphicsLayer { alpha = 0.22f }
                    .clip(CircleShape)
                    .background(Color.Black)
            )

            Box(
                modifier = Modifier
                    .size(width = 222.dp, height = 246.dp)
                    .offset(
                        x = if (profile.action == PetAction.Eating || profile.action == PetAction.Excited) shake.dp else 0.dp,
                        y = floatOffset.dp
                    )
                    .graphicsLayer {
                        scaleX = breathe * actionScale
                        scaleY = breathe * actionScale
                        rotationZ = rotation
                    }
                    .shadow(20.dp, RoundedCornerShape(30.dp), clip = false)
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .clickable(onClick = onPetClicked),
                contentAlignment = Alignment.Center
            ) {
                if (profile.imageUri != null) {
                    SubcomposeAsyncImage(
                        model = profile.imageUri,
                        contentDescription = "宠物图片",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = { DefaultPetFace(profile) },
                        error = { DefaultPetFace(profile) }
                    )
                } else {
                    DefaultPetFace(profile)
                }
            }

            PetDecorationLayer(
                profile = profile,
                modifier = Modifier.fillMaxSize()
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = bubbleText.ifBlank { statusBubble(profile) },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            text = "${profile.accentEmoji} ${profile.moodText}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DefaultPetFace(profile: PetProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = profile.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(text = profile.expression)
        Text(text = profile.favoriteFood)
    }
}

private fun stageColors(theme: String): List<Color> = when (theme) {
    "starry" -> listOf(Color(0xFF26384F), Color(0xFF6E93C8), Color(0xFFFFE6A3))
    "cloud" -> listOf(Color(0xFFEAF7FF), Color(0xFFCFEAFF), Color(0xFFFFF8EA))
    "forest" -> listOf(Color(0xFFE9F7E8), Color(0xFFCDE8CB), Color(0xFFFFF3D6))
    "candy" -> listOf(Color(0xFFFFEEF6), Color(0xFFFFC9DE), Color(0xFFFFF1C9))
    else -> listOf(Color(0xFFFFF4DF), Color(0xFFFFD7A8), Color(0xFFFFF9EE))
}

private fun movementRange(personality: Personality, actionHint: String): Float {
    return when {
        actionHint == "bounce" || personality == Personality.Energetic -> 12f
        personality == Personality.Gentle -> 4f
        personality == Personality.Shy -> 3f
        actionHint == "sway" || personality == Personality.Foodie -> 7f
        personality == Personality.Tsundere -> 5f
        else -> 5f
    }
}

private fun statusBubble(profile: PetProfile): String = when (profile.action) {
    PetAction.Idle -> "我在这里陪你～"
    PetAction.Happy, PetAction.Clicked -> when (profile.personality) {
        Personality.Tsundere -> "哼，只是刚好被你点到。"
        else -> "嘿嘿，刚刚被你点到啦！"
    }
    PetAction.Eating -> "吃到喜欢的东西啦！"
    PetAction.Listening -> "我在认真听你说。"
    PetAction.Comforting -> "慢慢来，我陪你。"
    PetAction.Sleepy -> "有点困了，想安静待一会儿。"
    PetAction.Excited -> "今天也要一起加油！"
}
