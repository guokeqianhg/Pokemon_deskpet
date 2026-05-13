package com.example.deskpet.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deskpet.model.PetProfile
import com.example.deskpet.model.PetStatus
import com.example.deskpet.ui.components.PetCard
import com.example.deskpet.ui.components.PetStatusPanel

@Composable
fun HomeScreen(
    petProfile: PetProfile,
    petStatus: PetStatus,
    feedbackText: String,
    isFeeding: Boolean,
    isUploading: Boolean,
    onPetClicked: () -> Unit,
    onFeedPet: () -> Unit,
    onRegeneratePet: () -> Unit,
    onImageSelected: (String) -> Unit,
    onImageTransformChanged: (Float, Float, Float) -> Unit,
    onResetImageTransform: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenDiary: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var showAdjustDialog by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onImageSelected(it.toString()) }
    }
    val background = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.16f)
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HomeHeader(
                onOpenSettings = onOpenSettings
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = HomeTokens.PagePadding, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HomeTokens.SectionGap)
            ) {
                PetCard(
                    profile = petProfile,
                    bubbleText = feedbackText,
                    onPetClicked = onPetClicked
                )

                PetStatusPanel(status = petStatus)

                HomeActionPanel(
                    isFeeding = isFeeding,
                    isUploading = isUploading,
                    hasImage = petProfile.imageUri != null,
                    onFeedPet = onFeedPet,
                    onOpenChat = onOpenChat,
                    onUploadImage = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onAdjustImage = { showAdjustDialog = true },
                    onRegeneratePet = onRegeneratePet,
                    onOpenDiary = onOpenDiary
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showAdjustDialog) {
        PetImageAdjustDialog(
            profile = petProfile,
            onDismiss = { showAdjustDialog = false },
            onSave = { scale, offsetX, offsetY ->
                onImageTransformChanged(scale, offsetX, offsetY)
                showAdjustDialog = false
            },
            onReset = {
                onResetImageTransform()
                showAdjustDialog = false
            }
        )
    }
}

@Composable
private fun HomeHeader(
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = HomeTokens.PagePadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "我的桌面宠物",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "今天也在这里陪你",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            tonalElevation = 1.dp,
            modifier = Modifier.border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f),
                shape = RoundedCornerShape(999.dp)
            )
        ) {
            TextButton(
                onClick = onOpenSettings,
                contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
                Text(text = "设置")
            }
        }
    }
}

@Composable
private fun HomeActionPanel(
    isFeeding: Boolean,
    isUploading: Boolean,
    hasImage: Boolean,
    onFeedPet: () -> Unit,
    onOpenChat: () -> Unit,
    onUploadImage: () -> Unit,
    onAdjustImage: () -> Unit,
    onRegeneratePet: () -> Unit,
    onOpenDiary: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                onClick = onFeedPet,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                enabled = !isFeeding,
                shape = RoundedCornerShape(HomeTokens.ButtonRadius)
            ) {
                Text(text = if (isFeeding) "正在吃" else "给它一点小零食")
            }
            FilledTonalButton(
                onClick = onOpenChat,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(HomeTokens.ButtonRadius),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "和它说说话")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SecondaryActionButton(
                text = if (isUploading) "处理中" else "上传图片",
                onClick = onUploadImage,
                enabled = !isUploading,
                modifier = Modifier.weight(1f)
            )
            SecondaryActionButton(
                text = "调整形象",
                onClick = onAdjustImage,
                enabled = hasImage,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SecondaryActionButton(
                text = "重新生成",
                onClick = onRegeneratePet,
                modifier = Modifier.weight(1f)
            )
            SecondaryActionButton(
                text = "查看日记",
                onClick = onOpenDiary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        enabled = enabled,
        shape = RoundedCornerShape(HomeTokens.ButtonRadius)
    ) {
        Text(text = text)
    }
}

private object HomeTokens {
    val PagePadding = 20.dp
    val SectionGap = 14.dp
    val ButtonRadius = 24.dp
}

@Composable
private fun PetImageAdjustDialog(
    profile: PetProfile,
    onDismiss: () -> Unit,
    onSave: (Float, Float, Float) -> Unit,
    onReset: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(profile.imageScale) }
    var offsetX by remember { mutableFloatStateOf(profile.imageOffsetX) }
    var offsetY by remember { mutableFloatStateOf(profile.imageOffsetY) }
    val scrollState = rememberScrollState()

    LaunchedEffect(profile.id, profile.imageUri) {
        scale = profile.imageScale
        offsetX = profile.imageOffsetX
        offsetY = profile.imageOffsetY
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "调整宠物形象") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PetCard(
                    profile = profile.copy(
                        imageScale = scale,
                        imageOffsetX = offsetX,
                        imageOffsetY = offsetY
                    ),
                    bubbleText = "把它调整到最舒服的位置～",
                    onPetClicked = {}
                )
                SliderRow(
                    label = "缩放",
                    value = scale,
                    range = 0.75f..2.4f,
                    onValueChange = { scale = it }
                )
                SliderRow(
                    label = "左右",
                    value = offsetX,
                    range = -80f..80f,
                    onValueChange = { offsetX = it }
                )
                SliderRow(
                    label = "上下",
                    value = offsetY,
                    range = -90f..90f,
                    onValueChange = { offsetY = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(scale, offsetX, offsetY) }) {
                Text(text = "保存")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onReset) {
                    Text(text = "重置")
                }
                TextButton(onClick = onDismiss) {
                    Text(text = "取消")
                }
            }
        }
    )
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$label ${"%.1f".format(value)}",
            style = MaterialTheme.typography.labelLarge
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range
        )
    }
}
