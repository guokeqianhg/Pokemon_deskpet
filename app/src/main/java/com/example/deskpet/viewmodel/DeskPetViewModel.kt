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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val _feedbackText = MutableStateFlow("我在这里陪你～")
    val feedbackText: StateFlow<String> = _feedbackText.asStateFlow()

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    private val _isFeeding = MutableStateFlow(false)
    val isFeeding: StateFlow<Boolean> = _isFeeding.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _selectedDiaryEntryId = MutableStateFlow<String?>(null)
    val selectedDiaryEntryId: StateFlow<String?> = _selectedDiaryEntryId.asStateFlow()

    private val _selectedDiaryEntry = MutableStateFlow<DiaryEntry?>(null)
    val selectedDiaryEntry: StateFlow<DiaryEntry?> = _selectedDiaryEntry.asStateFlow()

    val backendUrl: String = backendClient.backendUrl()

    private var actionToken = 0
    private var actionResetJob: Job? = null
    private var feedbackResetJob: Job? = null
    private var isTestingBackend = false

    init {
        viewModelScope.launch {
            val snapshot = repository.loadSnapshot()
            val loadedProfile = snapshot.petProfile.copy(
                action = PetAction.Idle,
                moodText = actionMoodText(PetAction.Idle, snapshot.petProfile.personality)
            )
            _petProfile.value = loadedProfile
            _petStatus.value = snapshot.petStatus.clamped()
            _chatMessages.value = snapshot.chatMessages
            _diaryEntries.value = snapshot.diaryEntries.sortedByDescending { it.createdAt }
            restoreDefaultBubble()
            validateStoredImageUri()
        }
    }

    fun onPetClicked() {
        if (_isFeeding.value || _petProfile.value.action == PetAction.Eating) {
            showTransientFeedback("它正在吃呢～")
            return
        }
        if (_isSendingMessage.value) {
            showTransientFeedback("我在认真听你说。")
            return
        }

        val profile = _petProfile.value
        val action = if (profile.personality == Personality.Energetic) {
            PetAction.Excited
        } else {
            PetAction.Happy
        }
        _petStatus.update { status ->
            status.copy(
                mood = status.mood + 1,
                intimacy = status.intimacy + 1
            ).clamped()
        }
        startTransientAction(
            action = action,
            bubbleText = personalityFeedback(profile.personality),
            durationMillis = 900L
        )
        persistPet()
    }

    fun feedPet() {
        if (_isFeeding.value || _petProfile.value.action == PetAction.Eating) {
            showTransientFeedback("它正在吃呢～")
            return
        }

        val profile = _petProfile.value
        val token = startPersistentAction(PetAction.Eating, feedFeedback(profile.personality))
        _isFeeding.value = true
        _petStatus.update { status ->
            val moodBoost = if (profile.personality == Personality.Foodie) 8 else 5
            val hungerDrop = if (profile.personality == Personality.Foodie) 22 else 15
            status.copy(
                mood = status.mood + moodBoost,
                hunger = status.hunger - hungerDrop,
                intimacy = status.intimacy + 2
            ).clamped()
        }
        persistPet()

        actionResetJob = viewModelScope.launch {
            delay(2000L)
            if (token != actionToken) return@launch
            updateAction(PetAction.Happy)
            _feedbackText.value = "吃饱啦，心情变好了。"
            persistPet()

            delay(800L)
            resetToIdleIfCurrent(token)
        }
    }

    fun regeneratePet() {
        if (_isUploading.value) {
            showTransientFeedback("图片正在处理中，先等它完成。")
            return
        }
        if (_isFeeding.value) {
            showTransientFeedback("它正在吃呢，等一下再换新性格。")
            return
        }
        val currentImageUri = _petProfile.value.imageUri
        _petProfile.value = generateRandomPet(currentImageUri)
        _petStatus.value = defaultStatus()
        startTransientAction(
            action = PetAction.Happy,
            bubbleText = "新的小伙伴醒来啦，它好像有了新的性格。",
            durationMillis = 1600L
        )
        persistPet()
    }

    fun resetPet() {
        cancelTransientState(resetAction = false)
        imageStore.clearCache()
        _petProfile.value = generateRandomPet(currentImageUri = null)
        _petStatus.value = defaultStatus()
        _chatMessages.value = emptyList()
        startTransientAction(
            action = PetAction.Happy,
            bubbleText = "宠物已经重新开始新的陪伴旅程。",
            durationMillis = 1600L
        )
        viewModelScope.launch {
            repository.clearPetData()
            repository.saveChatMessages(emptyList())
            persistPet()
        }
    }

    fun clearImageCache() {
        if (_isUploading.value) {
            showTransientFeedback("图片还在处理中，稍等一下就好。")
            return
        }
        imageStore.clearCache()
        _petProfile.update {
            it.copy(
                imageUri = null,
                action = PetAction.Idle,
                moodText = actionMoodText(PetAction.Idle, it.personality)
            )
        }
        restoreDefaultBubble()
        showTransientFeedback("本地图片缓存已经清理。")
        persistPet()
    }

    fun testBackendConnection() {
        if (isTestingBackend) return
        isTestingBackend = true
        viewModelScope.launch {
            val ok = backendClient.checkHealth()
            showTransientFeedback(
                if (ok) "后端连接正常。" else "暂时无法连接后端。"
            )
            isTestingBackend = false
        }
    }

    fun updatePetImage(uri: String) {
        if (_isUploading.value) {
            showTransientFeedback("图片正在处理中，马上就好。")
            return
        }
        _isUploading.value = true
        startTransientAction(
            action = PetAction.Happy,
            bubbleText = "我帮它整理成宠物形象啦～",
            durationMillis = 1200L
        )

        viewModelScope.launch {
            try {
                val cachedUri = withContext(Dispatchers.IO) {
                    imageStore.cacheImage(uri)
                } ?: uri

                _petProfile.update {
                    it.copy(
                        imageUri = cachedUri,
                        action = PetAction.Happy,
                        moodText = actionMoodText(PetAction.Happy, it.personality)
                    )
                }
                persistPet()

                viewModelScope.launch {
                    val cutout = backendClient.requestPetCutout(getApplication(), cachedUri)
                    val message = when {
                        cutout?.mode == "real_cutout" && (cutout.imageUrl != null || cutout.processedImagePath != null) ->
                            "抠图结果已准备好，后续可以切换为处理图。"
                        cutout?.mode == "soft_cutout" ->
                            "已使用柔和主体模式展示。"
                        else ->
                            "先用本地展示模式，之后还能升级抠图。"
                    }
                    showTransientFeedback(message, durationMillis = 1400L)
                }

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
                            stageTheme = response.stageTheme.ifBlank { profile.stageTheme },
                        accentEmoji = response.accentEmoji.ifBlank { profile.accentEmoji },
                        actionHint = response.actionHint.ifBlank { profile.actionHint },
                        imageScale = 1f,
                        imageOffsetX = 0f,
                        imageOffsetY = 0f,
                        action = PetAction.Happy,
                        moodText = actionMoodText(PetAction.Happy, response.personality.toPersonalityOrNull() ?: profile.personality)
                    )
                    }
                    startTransientAction(
                        action = PetAction.Happy,
                        bubbleText = response.description.ifBlank { "它看起来更像一个小伙伴了。" },
                        durationMillis = 1200L
                    )
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
                            stageTheme = regenerated.stageTheme,
                        accentEmoji = regenerated.accentEmoji,
                        actionHint = regenerated.actionHint,
                        imageScale = 1f,
                        imageOffsetX = 0f,
                        imageOffsetY = 0f,
                        seed = regenerated.seed,
                        action = PetAction.Happy,
                        moodText = actionMoodText(PetAction.Happy, regenerated.personality)
                        )
                    }
                    startTransientAction(
                        action = PetAction.Happy,
                        bubbleText = "先用柔和主体模式展示，之后还能升级成真正抠图哦。",
                        durationMillis = 1200L
                    )
                }
                persistPet()
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun updatePetImageTransform(scale: Float, offsetX: Float, offsetY: Float) {
        _petProfile.update { profile ->
            profile.copy(
                imageScale = scale.coerceIn(MIN_IMAGE_SCALE, MAX_IMAGE_SCALE),
                imageOffsetX = offsetX.coerceIn(MIN_IMAGE_OFFSET_X, MAX_IMAGE_OFFSET_X),
                imageOffsetY = offsetY.coerceIn(MIN_IMAGE_OFFSET_Y, MAX_IMAGE_OFFSET_Y)
            )
        }
        showTransientFeedback("形象位置已经保存啦。")
        persistPet()
    }

    fun resetPetImageTransform() {
        updatePetImageTransform(scale = 1f, offsetX = 0f, offsetY = 0f)
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
        startPersistentAction(PetAction.Listening, "我在认真听你说。")
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
            _isSendingMessage.value = false
            startTransientAction(
                action = PetAction.Comforting,
                bubbleText = if (backendResponse == null) {
                    "后端暂时没连上，我先陪你聊聊。"
                } else {
                    "我把这段心情记下来了。"
                },
                durationMillis = 2000L
            )
            persistPet()
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
        showTransientFeedback("这条日记已经轻轻收起来了。")
    }

    fun clearDiaryEntries() {
        _diaryEntries.value = emptyList()
        _selectedDiaryEntryId.value = null
        _selectedDiaryEntry.value = null
        viewModelScope.launch { repository.saveDiaryEntries(emptyList()) }
        showTransientFeedback("日记已经清空。")
    }

    fun goToHome() {
        _currentScreen.value = AppScreen.Home
        if (!_isSendingMessage.value) {
            resetTransientStateToIdle()
        } else {
            _feedbackText.value = bubbleForAction(PetAction.Listening, _petProfile.value, _petStatus.value)
        }
    }

    fun goToChat() {
        if (!_isSendingMessage.value) {
            resetTransientStateToIdle()
            startPersistentAction(PetAction.Listening, "我在认真听你说。")
            persistPet()
        }
        _currentScreen.value = AppScreen.Chat
    }

    fun goToDiary() {
        if (!_isSendingMessage.value) {
            resetTransientStateToIdle()
        }
        _currentScreen.value = AppScreen.Diary
    }

    fun goToSettings() {
        if (!_isSendingMessage.value) {
            resetTransientStateToIdle()
        }
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
        val profileToSave = _petProfile.value.copy(
            action = PetAction.Idle,
            moodText = actionMoodText(PetAction.Idle, _petProfile.value.personality)
        )
        viewModelScope.launch {
            repository.saveAll(
                DeskPetSnapshot(
                    petProfile = profileToSave,
                    petStatus = _petStatus.value.clamped(),
                    diaryEntries = _diaryEntries.value,
                    chatMessages = _chatMessages.value
                )
            )
        }
        super.onCleared()
    }

    private fun startPersistentAction(action: PetAction, bubbleText: String): Int {
        actionToken += 1
        actionResetJob?.cancel()
        feedbackResetJob?.cancel()
        if (action != PetAction.Eating) {
            _isFeeding.value = false
        }
        updateAction(action)
        _feedbackText.value = bubbleText.ifBlank {
            bubbleForAction(action, _petProfile.value, _petStatus.value)
        }
        return actionToken
    }

    private fun startTransientAction(
        action: PetAction,
        bubbleText: String,
        durationMillis: Long
    ): Int {
        val token = startPersistentAction(action, bubbleText)
        actionResetJob = viewModelScope.launch {
            delay(durationMillis)
            resetToIdleIfCurrent(token)
        }
        return token
    }

    private fun resetToIdleIfCurrent(token: Int) {
        if (token != actionToken) return
        _isFeeding.value = false
        updateAction(PetAction.Idle)
        restoreDefaultBubble()
        persistPet()
    }

    private fun resetTransientStateToIdle() {
        cancelTransientState(resetAction = true)
        persistPet()
    }

    private fun cancelTransientState(resetAction: Boolean) {
        actionToken += 1
        actionResetJob?.cancel()
        feedbackResetJob?.cancel()
        _isFeeding.value = false
        if (resetAction) {
            updateAction(PetAction.Idle)
        }
        restoreDefaultBubble()
    }

    private fun updateAction(action: PetAction) {
        _petProfile.update { profile ->
            profile.copy(
                action = action,
                moodText = actionMoodText(action, profile.personality)
            )
        }
    }

    private fun showTransientFeedback(text: String, durationMillis: Long = 1600L) {
        feedbackResetJob?.cancel()
        _feedbackText.value = text
        val token = actionToken
        feedbackResetJob = viewModelScope.launch {
            delay(durationMillis)
            if (token == actionToken) {
                restoreDefaultBubble()
            }
        }
    }

    private fun restoreDefaultBubble() {
        _feedbackText.value = bubbleForAction(_petProfile.value.action, _petProfile.value, _petStatus.value)
    }

    private fun persistPet() {
        viewModelScope.launch {
            repository.savePet(_petProfile.value, _petStatus.value.clamped())
        }
    }

    private fun validateStoredImageUri() {
        val uriString = _petProfile.value.imageUri ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val canRead = imageStore.canRead(uriString)
            if (!canRead) {
                _petProfile.update { it.copy(imageUri = null) }
                repository.savePet(_petProfile.value, _petStatus.value.clamped())
                showTransientFeedback("之前的图片暂时无法读取，已先显示默认宠物。")
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

private fun bubbleForAction(action: PetAction, profile: PetProfile, status: PetStatus): String {
    return when (action) {
        PetAction.Idle -> idleBubble(profile, status)
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
}

private fun idleBubble(profile: PetProfile, status: PetStatus): String {
    return when {
        status.energy <= 20 -> "有点困了，想安静待一会儿。"
        status.hunger >= 78 -> "肚子有点空空的，但我还在这里。"
        profile.personality == Personality.Gentle -> "今天也一起慢慢来。"
        profile.personality == Personality.Energetic -> "我在这里陪你，随时可以出发！"
        profile.personality == Personality.Shy -> "我安静地在这里陪你。"
        profile.personality == Personality.Foodie -> "我在这里陪你～也惦记着小点心。"
        profile.personality == Personality.Tsundere -> "我只是刚好待在这里啦。"
        else -> "我在这里陪你～"
    }
}

private fun String.toPersonalityOrNull(): Personality? {
    return runCatching { Personality.valueOf(this) }.getOrNull()
}

private fun String.hasAny(vararg keywords: String): Boolean {
    return keywords.any { contains(it, ignoreCase = true) }
}

private const val MIN_IMAGE_SCALE = 0.75f
private const val MAX_IMAGE_SCALE = 2.4f
private const val MIN_IMAGE_OFFSET_X = -80f
private const val MAX_IMAGE_OFFSET_X = 80f
private const val MIN_IMAGE_OFFSET_Y = -90f
private const val MAX_IMAGE_OFFSET_Y = 90f
