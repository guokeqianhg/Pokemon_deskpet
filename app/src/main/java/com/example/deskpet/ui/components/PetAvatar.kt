package com.example.deskpet.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.deskpet.model.Personality
import com.example.deskpet.model.PetAction
import com.example.deskpet.model.PetProfile

@Composable
fun PetAvatar(
    profile: PetProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pet-motion")
    val floatAmount = when (profile.personality) {
        Personality.Energetic -> 10f
        Personality.Gentle -> 4f
        Personality.Shy -> 3f
        Personality.Foodie -> 6f
        Personality.Tsundere -> 5f
    }
    val idleOffset by infiniteTransition.animateFloat(
        initialValue = -floatAmount,
        targetValue = floatAmount,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (profile.personality == Personality.Gentle) 1900 else 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle-offset"
    )
    val shake by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (profile.personality == Personality.Foodie) 110 else 160),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eating-shake"
    )
    val scale by animateFloatAsState(
        targetValue = when (profile.action) {
            PetAction.Eating -> if (profile.personality == Personality.Foodie) 1.13f else 1.08f
            PetAction.Excited -> 1.16f
            PetAction.Happy, PetAction.Clicked -> 1.1f
            PetAction.Comforting -> 1.04f
            PetAction.Listening -> 1.02f
            else -> 1f
        },
        animationSpec = spring(),
        label = "pet-scale"
    )
    val rotation = when (profile.action) {
        PetAction.Eating -> shake
        PetAction.Excited -> shake * 0.8f
        else -> 0f
    }

    Box(
        modifier = modifier.size(width = 250.dp, height = 285.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(width = 155.dp, height = 28.dp)
                .graphicsLayer { alpha = 0.24f }
                .clip(CircleShape)
                .background(Color.Black)
        )

        Box(
            modifier = Modifier
                .size(230.dp)
                .offset(
                    x = if (profile.action == PetAction.Eating || profile.action == PetAction.Excited) shake.dp else 0.dp,
                    y = idleOffset.dp
                )
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                }
                .shadow(18.dp, RoundedCornerShape(34.dp), clip = false)
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (profile.imageUri != null) {
                SubcomposeAsyncImage(
                    model = profile.imageUri,
                    contentDescription = "宠物图片",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(34.dp)),
                    contentScale = ContentScale.Crop,
                    loading = { DefaultPetFace(profile) },
                    error = { DefaultPetFace(profile) }
                )
            } else {
                DefaultPetFace(profile)
            }

            AssistChip(
                onClick = {},
                label = { Text(text = profile.decoration) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-10).dp, y = 10.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                )
            )
        }
    }
}

@Composable
private fun DefaultPetFace(profile: PetProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = profile.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = profile.expression,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = profile.favoriteFood,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
