package com.example.deskpet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.deskpet.model.PetProfile

@Composable
fun PetDecorationLayer(
    profile: PetProfile,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 24.dp, y = 26.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
            tonalElevation = 2.dp
        ) {
            Text(
                text = profile.accentEmoji,
                modifier = Modifier.offset(y = (-1).dp)
            )
        }

        Text(
            text = decorationGlyph(profile.decoration, profile.accentEmoji),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-34).dp, y = 34.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 38.dp, y = (-42).dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(themeDotColor(profile.stageTheme))
        )
    }
}

private fun decorationGlyph(decoration: String, fallback: String): String {
    return when {
        decoration.contains("围巾") -> "〰"
        decoration.contains("星") || decoration.contains("铃铛") -> "✦"
        decoration.contains("云") -> "☁"
        decoration.contains("帽") -> "◠"
        decoration.contains("花") -> "✿"
        decoration.contains("糖") || decoration.contains("饭碗") -> "●"
        decoration.contains("披风") -> "◒"
        else -> fallback
    }
}

private fun themeDotColor(theme: String): Color {
    return when (theme) {
        "starry" -> Color(0xFFFFD86B)
        "cloud" -> Color(0xFF9CD5FF)
        "forest" -> Color(0xFF9BD59A)
        "candy" -> Color(0xFFFF9FCB)
        else -> Color(0xFFFFC58A)
    }
}
