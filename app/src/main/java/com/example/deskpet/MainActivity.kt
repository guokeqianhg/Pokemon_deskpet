package com.example.deskpet

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.deskpet.model.AppScreen
import com.example.deskpet.ui.ChatScreen
import com.example.deskpet.ui.DiaryDetailScreen
import com.example.deskpet.ui.DiaryScreen
import com.example.deskpet.ui.HomeScreen
import com.example.deskpet.ui.SettingsScreen
import com.example.deskpet.ui.theme.DeskPetTheme
import com.example.deskpet.viewmodel.DeskPetViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: DeskPetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeskPetTheme {
                DeskPetApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun DeskPetApp(viewModel: DeskPetViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val petProfile by viewModel.petProfile.collectAsState()
    val petStatus by viewModel.petStatus.collectAsState()
    val feedbackText by viewModel.feedbackText.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val diaryEntries by viewModel.diaryEntries.collectAsState()
    val isSendingMessage by viewModel.isSendingMessage.collectAsState()
    val isFeeding by viewModel.isFeeding.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val selectedDiaryEntry by viewModel.selectedDiaryEntry.collectAsState()

    BackHandler(enabled = currentScreen != AppScreen.Home) {
        viewModel.onSystemBack()
    }

    when (currentScreen) {
        AppScreen.Home -> HomeScreen(
            petProfile = petProfile,
            petStatus = petStatus,
            feedbackText = feedbackText,
            isFeeding = isFeeding,
            isUploading = isUploading,
            onPetClicked = viewModel::onPetClicked,
            onFeedPet = viewModel::feedPet,
            onRegeneratePet = viewModel::regeneratePet,
            onImageSelected = viewModel::updatePetImage,
            onImageTransformChanged = viewModel::updatePetImageTransform,
            onResetImageTransform = viewModel::resetPetImageTransform,
            onOpenChat = viewModel::goToChat,
            onOpenDiary = viewModel::goToDiary,
            onOpenSettings = viewModel::goToSettings
        )

        AppScreen.Chat -> ChatScreen(
            messages = chatMessages,
            petName = petProfile.name,
            isSending = isSendingMessage,
            onBack = viewModel::goToHome,
            onSendMessage = viewModel::sendUserMessage
        )

        AppScreen.Diary -> DiaryScreen(
            entries = diaryEntries,
            onBack = viewModel::goToHome,
            onOpenDetail = viewModel::openDiaryDetail,
            onDeleteEntry = viewModel::deleteDiaryEntry,
            onClearEntries = viewModel::clearDiaryEntries
        )

        AppScreen.DiaryDetail -> DiaryDetailScreen(
            entry = selectedDiaryEntry,
            onBack = viewModel::closeDiaryDetail,
            onDelete = viewModel::deleteDiaryEntry
        )

        AppScreen.Settings -> SettingsScreen(
            backendUrl = viewModel.backendUrl,
            onBack = viewModel::goToHome,
            onTestBackend = viewModel::testBackendConnection,
            onClearDiary = viewModel::clearDiaryEntries,
            onResetPet = viewModel::resetPet,
            onClearImageCache = viewModel::clearImageCache
        )
    }
}
