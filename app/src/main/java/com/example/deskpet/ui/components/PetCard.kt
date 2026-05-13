package com.example.deskpet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deskpet.model.PetProfile
import com.example.deskpet.util.actionDisplayName
import com.example.deskpet.util.personalityDisplayName

@Composable
fun PetCard(
    profile: PetProfile,
    bubbleText: String,
    onPetClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PetStage(
                profile = profile,
                bubbleText = bubbleText,
                onPetClicked = onPetClicked
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoLine(label = "名字", value = profile.name)
                InfoLine(label = "性格", value = personalityDisplayName(profile.personality))
                InfoLine(label = "当前状态", value = actionDisplayName(profile.action))
                InfoLine(label = "表情", value = profile.expression)
                InfoLine(label = "喜欢", value = profile.favoriteFood)
                InfoLine(label = "陪伴风格", value = profile.companionStyle)
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}
