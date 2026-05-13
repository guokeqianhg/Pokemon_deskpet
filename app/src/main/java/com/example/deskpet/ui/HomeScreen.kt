package com.example.deskpet.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.deskpet.model.PetProfile
import com.example.deskpet.model.PetStatus
import com.example.deskpet.ui.components.ActionButton
import com.example.deskpet.ui.components.PetCard
import com.example.deskpet.ui.components.PetStatusPanel
import com.example.deskpet.ui.components.ScreenTopBar

@Composable
fun HomeScreen(
    petProfile: PetProfile,
    petStatus: PetStatus,
    feedbackText: String,
    onPetClicked: () -> Unit,
    onFeedPet: () -> Unit,
    onRegeneratePet: () -> Unit,
    onImageSelected: (String) -> Unit,
    onOpenChat: () -> Unit,
    onOpenDiary: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onImageSelected(it.toString()) }
    }

    Scaffold(
        topBar = { ScreenTopBar(title = "我的桌面宠物") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PetCard(
                profile = petProfile,
                onPetClicked = onPetClicked
            )

            PetStatusPanel(status = petStatus)

            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = feedbackText,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionButton(
                        text = "投喂",
                        onClick = onFeedPet,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        text = "上传图片",
                        onClick = {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                ActionButton(
                    text = "重新生成属性",
                    onClick = onRegeneratePet,
                    outlined = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionButton(
                        text = "和它说说话",
                        onClick = onOpenChat,
                        modifier = Modifier.weight(1f),
                        outlined = true
                    )
                    ActionButton(
                        text = "查看日记",
                        onClick = onOpenDiary,
                        modifier = Modifier.weight(1f),
                        outlined = true
                    )
                }

                ActionButton(
                    text = "设置",
                    onClick = onOpenSettings,
                    outlined = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
