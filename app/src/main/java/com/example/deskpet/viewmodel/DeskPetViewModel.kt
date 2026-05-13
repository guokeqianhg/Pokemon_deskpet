package com.example.deskpet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.deskpet.data.DeskPetRepository
import com.example.deskpet.data.DeskPetSnapshot
import com.example.deskpet.data.PetImageStore
import com.example.deskpet.model.AppScreen
import com.example.deskpet.model.ChatMessage
import com.example.deskpet.model.DiaryEntry
import com.example.deskpet.model.MessageRole
import com.example.deskpet.model.Personality
import com.example.deskpet.model.PetAction
import com.example.deskpet.model.PetProfile
import com.example.deskpet.model.PetStatus
import com.example.deskpet.network.DeskPetBackendClient
import com.example.deskpet.util.DiaryHelper
import com.example.deskpet.util.PetReplyEngine
import com.example.deskpet.util.actionMoodText
import com.example.deskpet.util.defaultStatus
import com.example.deskpet.util.feedFeedback
import com.example.deskpet.util.generateRandomPet
import com.example.deskpet.util.personalityFeedback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeskPetViewModel(application: Application) : AndroidViewModel(application) {
    private val backendClient = DeskPetBackendClient()
    private val repository = DeskPetRepository(application)
    private val imageStore = PetImageStore(application)

    private val _petProfile = MutableStateFlow(generateRandomPet(currentImageUri = null))
    val petProfile: StateFlow<PetProfile> = _petProfile.asStateFlow()

    private val _petStatus = MutableStateFlow(defaultStatus())
    val petStatus: StateFlow<PetStatus> = _petStatus.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _diaryEntries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val diaryEntries: StateFlow<List<DiaryEntry>> = _diaryEntries.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.Home)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _feedbackText = MutableStateFlow("轻轻点一点，看看它会怎么回应你。")
    val feedbackText: StateFlow<String> = _feedbackText.asStateFlow()

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    private val _selectedDiaryEntryId = MutableStateFlow<String?>(null)
    val selectedDiaryEntryId: StateFlow<String?> = _selectedDiaryEntryId.asStateFlow()

    private val _selectedDiaryEntry = MutableStateFlow<DiaryEntry?>(null)
    val selectedDiaryEntry: StateFlow<DiaryEntry?> = _selectedDiaryEntry.asStateFlow()

    val backendUrl: String = backendClient.backendUrl()

    private var actionToken = 0

    init {
        viewModelScope.launch {
            val snapshot = repository.loadSnapshot()
            _petProfile.value = snapshot.petProfile.copy(action = PetAction.Idle)
            _petStatus.value = snapshot.petStatus
            _chatMessages.value = snapshot.chatMessages
            _diaryEntries.value = snapshot.diaryEntries.sortedByDescending { it.createdAt }
            _feedbackText.value = _petProfile.value.moodText
            validateStoredImageUri()
        }
    }

    fun onPetClicked() {
        if (_petProfile.value.action == PetAction.Eating) return
        val profile = _petProfile.value
        val action = if (profile.personality == Personality.Energetic) PetAction.Excited else PetAction.Happy
        val token = beginAction(action)
        _petStatus.update { status ->
            status.copy(
                mood = status.mood + 1,
                intimacy = status.intimacy + 1
            ).clamped()
        }
        _feedbackText.value = personalityFeedback(profile.personality)
        persistPet()
        resetActionLater(1500L, token)
    }

    fun feedPet() {
        if (_petProfile.value.action == PetAction.Eating) return
        val profile = _petProfile.value
        val token = beginAction(PetAction.Eating)
        _petStatus.update { status ->
            val moodBoost = if (profile.personality == Personality.Foodie) 8 else 5
            val hungerDrop = if (profile.personality == Personality.Foodie) 22 else 15
            status.copy(
                mood = status.mood + moodBoost,
                hunger = status.hunger - hungerDrop,
                intimacy = status.intimacy + 2
            ).clamped()
        }
        _feedbackText.value = feedFeedback(profile.personality)
        persistPet()
        viewModelScope.launch {
            delay(2000L)
            if (token != actionToken) return@launch
            updateAction(PetAction.Happy)
            persistPet()
            delay(1100L)
            if (token != actionToken) return@launch
            updateAction(PetAction.Idle)
            persistPet()
        }
    }

    fun regeneratePet() {
        val currentImageUri = _petProfile.value.imageUri
        _petProfile.value = generateRandomPet(currentImageUri)
        _petStatus.value = defaultStatus()
        _feedbackText.value = "新的小伙伴醒来啦，它好像有了新的性格。"
        persistPet()
    }

    fun resetPet() {
        _petProfile.value = generateRandomPet(currentImageUri = null)
        _petStatus.value = defaultStatus()
        _chatMessages.value = emptyList()
        _feedbackText.value = "宠物已经重新开始新的陪伴旅程。"
        viewModelScope.launch {
            repository.clearPetData()
            repository.saveChatMessages(emptyList())
            persistPet()
        }
    }

    fun clearImageCache() {
        imageStore.clearCache()
        _petProfile.update { it.copy(imageUri = null) }
        _feedbackText.value = "本地图片缓存已经清理。"
        persistPet()
    }

    fun testBackendConnection() {
        viewModelScope.launch {
            val ok = backendClient.checkHealth()
            _feedbackText.value = if (ok) {
                "后端连接正常。"
            } else {
                "暂时无法连接后端。"
            }
        }
    }

    fun updatePetImage(uri: String) {
        val token = beginAction(PetAction.Happy)
        viewModelScope.launch {
            val cachedUri = kotlinx.coroutines.withContext(Dispatchers.IO) {
                imageStore.cacheImage(uri)
            } ?: uri

            _petProfile.update {
                it.copy(
                    imageUri = cachedUri,
                    moodText = actionMoodText(PetAction.Happy, it.personality)
                )
            }
            _feedbackText.value = "宠物形象已更新。"
            persistPet()

            val response = backendClient.uploadPetImage(getApplication(), cachedUri)
            if (response != null) {
                _petProfile.update { profile ->
                    profile.copy(
                        name = response.petName.ifBlank { profile.name },
                        personality = response.personality.toPersonalityOrNull() ?: profile.personality,
                        expression = response.expression.ifBlank { profile.expression },
                        decoration = response.decoration.ifBlank { profile.decoration },
                        favoriteFood = response.favoriteFood.ifBlank { profile.favoriteFood },
                        companionStyle = response.companionStyle.ifBlank { profile.companionStyle },
                        action = PetAction.Happy,
                        moodText = "它好像有了新的性格"
                    )
                }
                _feedbackText.value = response.description.ifBlank { "宠物形象已更新，它好像有了新的性格。" }
            } else {
                val regenerated = generateRandomPet(_petProfile.value.imageUri)
                _petProfile.update { profile ->
                    profile.copy(
                        name = regenerated.name,
                        personality = regenerated.personality,
                        expression = regenerated.expression,
                        decoration = regenerated.decoration,
                        favoriteFood = regenerated.favoriteFood,
                        companionStyle = regenerated.companionStyle,
                        seed = regenerated.seed,
                        action = PetAction.Happy,
                        moodText = "它好像有了新的性格"
                    )
                }
                _feedbackText.value = "宠物形象已更新，后端暂时没连上，已先生成本地属性。"
            }
            persistPet()
            resetActionLater(1500L, token)
        }
    }

    fun sendUserMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _isSendingMessage.value) return

        val now = System.currentTimeMillis()
        val userMessage = ChatMessage(
            id = "user-$now",
            role = MessageRole.User,
            text = trimmed,
            createdAt = now
        )

        _chatMessages.update { it + userMessage }
        viewModelScope.launch { repository.saveChatMessages(_chatMessages.value) }
        val token = beginAction(PetAction.Listening)
        _isSendingMessage.value = true

        viewModelScope.launch {
            val profile = _petProfile.value
            val backendResponse = backendClient.sendChat(trimmed, profile)
            val reply = backendResponse?.reply ?: PetReplyEngine.replyTo(trimmed, profile.personality)
            val petMessage = ChatMessage(
                id = "pet-$now",
                role = MessageRole.Pet,
                text = reply,
                createdAt = System.currentTimeMillis()
            )

            _chatMessages.update { it + petMessage }
            repository.saveChatMessages(_chatMessages.value)

            val newDiary = DiaryHelper.createEntry(
                userText = trimmed,
                petReply = reply,
                petProfile = profile,
                summaryOverride = backendResponse?.diarySummary,
                moodTagOverride = backendResponse?.moodTag
            )
            _diaryEntries.update { entries -> (listOf(newDiary) + entries).sortedByDescending { it.createdAt } }
            repository.saveDiaryEntries(_diaryEntries.value)

            val moodDelta = moodDeltaFor(backendResponse?.moodTag, trimmed)
            _petStatus.update {
                it.copy(
                    mood = it.mood + moodDelta,
                    intimacy = it.intimacy + 1,
                    energy = it.energy - 1
                ).clamped()
            }
            if (token == actionToken) {
                updateAction(PetAction.Comforting)
            }
            _feedbackText.value = if (backendResponse == null) {
                "后端暂时没连上，我先陪你聊聊。"
            } else {
                "我把这段心情记下来了。"
            }
            _isSendingMessage.value = false
            persistPet()
            resetActionLater(1800L, token)
        }
    }

    fun openDiaryDetail(entryId: String) {
        _selectedDiaryEntryId.value = entryId
        _selectedDiaryEntry.value = _diaryEntries.value.firstOrNull { it.id == entryId }
        _currentScreen.value = AppScreen.DiaryDetail
        viewModelScope.launch {
            _selectedDiaryEntry.value = repository.getDiaryEntry(entryId) ?: _selectedDiaryEntry.value
        }
    }

    fun closeDiaryDetail() {
        _currentScreen.value = AppScreen.Diary
    }

    fun deleteDiaryEntry(entryId: String) {
        _diaryEntries.update { entries -> entries.filterNot { it.id == entryId } }
        viewModelScope.launch { repository.saveDiaryEntries(_diaryEntries.value) }
        if (_selectedDiaryEntryId.value == entryId) {
            _selectedDiaryEntryId.value = null
            _selectedDiaryEntry.value = null
            _currentScreen.value = AppScreen.Diary
        }
        _feedbackText.value = "这条日记已经轻轻收起来了。"
    }

    fun clearDiaryEntries() {
        _diaryEntries.value = emptyList()
        _selectedDiaryEntryId.value = null
        _selectedDiaryEntry.value = null
        viewModelScope.launch { repository.saveDiaryEntries(emptyList()) }
        _feedbackText.value = "日记已经清空。"
    }

    fun goToHome() {
        _currentScreen.value = AppScreen.Home
        if (!_isSendingMessage.value) {
            actionToken += 1
            updateAction(PetAction.Idle)
            persistPet()
        }
    }

    fun goToChat() {
        _currentScreen.value = AppScreen.Chat
        beginAction(PetAction.Listening)
        _feedbackText.value = "正在认真听你说话。"
        persistPet()
    }

    fun goToDiary() {
        _currentScreen.value = AppScreen.Diary
    }

    fun goToSettings() {
        _currentScreen.value = AppScreen.Settings
    }

    fun onSystemBack(): Boolean {
        return when (_currentScreen.value) {
            AppScreen.Home -> false
            AppScreen.DiaryDetail -> {
                closeDiaryDetail()
                true
            }
            else -> {
                goToHome()
                true
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            repository.saveAll(
                DeskPetSnapshot(
                    petProfile = _petProfile.value,
                    petStatus = _petStatus.value,
                    diaryEntries = _diaryEntries.value,
                    chatMessages = _chatMessages.value
                )
            )
        }
        super.onCleared()
    }

    private fun beginAction(action: PetAction): Int {
        actionToken += 1
        updateAction(action)
        return actionToken
    }

    private fun updateAction(action: PetAction) {
        _petProfile.update { profile ->
            profile.copy(
                action = action,
                moodText = actionMoodText(action, profile.personality)
            )
        }
    }

    private fun resetActionLater(delayMillis: Long, token: Int) {
        viewModelScope.launch {
            delay(delayMillis)
            if (token != actionToken) return@launch
            if (_currentScreen.value == AppScreen.Chat && _isSendingMessage.value) {
                updateAction(PetAction.Listening)
            } else {
                updateAction(PetAction.Idle)
            }
            persistPet()
        }
    }

    private fun persistPet() {
        viewModelScope.launch {
            repository.savePet(_petProfile.value, _petStatus.value)
        }
    }

    private fun validateStoredImageUri() {
        val uriString = _petProfile.value.imageUri ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val canRead = imageStore.canRead(uriString)
            if (!canRead) {
                _petProfile.update { it.copy(imageUri = null) }
                _feedbackText.value = "之前的图片暂时无法读取，已先显示默认宠物。"
                repository.savePet(_petProfile.value, _petStatus.value)
            }
        }
    }

    private fun moodDeltaFor(moodTag: String?, text: String): Int {
        return when {
            moodTag == "危机" || text.hasAny("不想活", "自杀", "伤害自己", "结束生命") -> -2
            moodTag == "疲惫" || text.hasAny("累", "疲惫", "困") -> 1
            moodTag == "压力" || text.hasAny("烦", "压力", "焦虑") -> 1
            moodTag == "难过" || text.hasAny("难过", "伤心") -> 1
            moodTag == "生气" || text.hasAny("生气", "气") -> 1
            else -> 2
        }
    }
}

private fun String.toPersonalityOrNull(): Personality? {
    return runCatching { Personality.valueOf(this) }.getOrNull()
}

private fun String.hasAny(vararg keywords: String): Boolean {
    return keywords.any { contains(it, ignoreCase = true) }
}
