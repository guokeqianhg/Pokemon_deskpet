package com.example.deskpet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deskpet.model.PetStatus

@Composable
fun PetStatusPanel(
    status: PetStatus,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "今日状态",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusMiniCard(
                    icon = "😊",
                    label = "心情",
                    value = status.mood,
                    caption = moodCaption(status.mood),
                    modifier = Modifier.weight(1f)
                )
                StatusMiniCard(
                    icon = "🍙",
                    label = "饥饿",
                    value = status.hunger,
                    caption = hungerCaption(status.hunger),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusMiniCard(
                    icon = "🌙",
                    label = "精力",
                    value = status.energy,
                    caption = energyCaption(status.energy),
                    modifier = Modifier.weight(1f)
                )
                StatusMiniCard(
                    icon = "💛",
                    label = "亲密",
                    value = status.intimacy,
                    caption = intimacyCaption(status.intimacy),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusMiniCard(
    icon: String,
    label: String,
    value: Int,
    caption: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
    ) {
        Column(
            modifier = Modifier.padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "$icon $label", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = value.coerceIn(0, 100).toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { value.coerceIn(0, 100) / 100f },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun moodCaption(value: Int): String = when {
    value >= 85 -> "心情很好"
    value >= 60 -> "挺放松"
    value >= 35 -> "需要陪陪"
    else -> "有点低落"
}

private fun hungerCaption(value: Int): String = when {
    value <= 10 -> "吃饱啦"
    value <= 35 -> "不太饿"
    value <= 65 -> "有点馋"
    else -> "想吃东西"
}

private fun energyCaption(value: Int): String = when {
    value >= 80 -> "精神满满"
    value >= 55 -> "还有精神"
    value >= 30 -> "慢慢来"
    else -> "想休息"
}

private fun intimacyCaption(value: Int): String = when {
    value >= 85 -> "非常亲近"
    value >= 55 -> "很信任你"
    value >= 25 -> "正在熟悉"
    else -> "刚刚靠近"
}
