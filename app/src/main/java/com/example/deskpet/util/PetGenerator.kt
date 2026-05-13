package com.example.deskpet.util

import com.example.deskpet.model.Personality
import com.example.deskpet.model.PetAction
import com.example.deskpet.model.PetProfile
import com.example.deskpet.model.PetStatus
import kotlin.random.Random

fun generateRandomPet(currentImageUri: String?): PetProfile {
    val now = System.currentTimeMillis()
    val seed = now xor Random.nextLong()
    val random = Random(seed)
    val personality = Personality.entries[random.nextInt(Personality.entries.size)]
    val names = listOf("团团", "软糖", "布丁", "栗子", "云朵", "小桃", "泡芙")

    return PetProfile(
        id = "pet-$now",
        name = names[random.nextInt(names.size)],
        imageUri = currentImageUri,
        personality = personality,
        action = PetAction.Idle,
        expression = expressionFor(personality),
        decoration = decorationFor(personality),
        favoriteFood = favoriteFoodFor(personality),
        moodText = actionMoodText(PetAction.Idle, personality),
        companionStyle = companionStyleFor(personality),
        stageTheme = stageThemeFor(personality),
        accentEmoji = accentEmojiFor(personality),
        actionHint = actionHintFor(personality),
        imageScale = 1f,
        imageOffsetX = 0f,
        imageOffsetY = 0f,
        seed = seed,
        createdAt = now
    )
}

fun defaultStatus(): PetStatus = PetStatus(
    mood = 82,
    hunger = 28,
    energy = 72,
    intimacy = 8
)

fun personalityDisplayName(personality: Personality): String = when (personality) {
    Personality.Gentle -> "温柔"
    Personality.Energetic -> "活泼"
    Personality.Shy -> "慢热"
    Personality.Foodie -> "贪吃"
    Personality.Tsundere -> "傲娇"
}

fun actionDisplayName(action: PetAction): String = when (action) {
    PetAction.Idle -> "待机"
    PetAction.Happy -> "开心"
    PetAction.Clicked -> "撒娇"
    PetAction.Eating -> "吃东西"
    PetAction.Listening -> "倾听"
    PetAction.Sleepy -> "困了"
    PetAction.Excited -> "兴奋"
    PetAction.Comforting -> "陪伴"
}

fun actionMoodText(action: PetAction, personality: Personality): String = when (action) {
    PetAction.Idle -> "正在发呆"
    PetAction.Happy -> "开心地蹦了一下"
    PetAction.Clicked -> when (personality) {
        Personality.Tsundere -> "才不是因为你点它才开心"
        Personality.Shy -> "轻轻靠近了一点"
        else -> "开心地回应了你"
    }
    PetAction.Eating -> when (personality) {
        Personality.Foodie -> "吃饱啦，心情变好了"
        Personality.Tsundere -> "勉强承认很好吃"
        else -> "正在认真吃东西"
    }
    PetAction.Listening -> "正在认真听你说话"
    PetAction.Sleepy -> "有点困，想安静待着"
    PetAction.Excited -> when (personality) {
        Personality.Energetic -> "兴奋地蹦了好几下"
        else -> "眼睛亮了一下"
    }
    PetAction.Comforting -> "轻轻陪在你旁边"
}

fun personalityFeedback(personality: Personality): String = when (personality) {
    Personality.Gentle -> "我在这里陪着你。"
    Personality.Energetic -> "嘿嘿，今天也一起加油！"
    Personality.Shy -> "我、我很开心你来找我。"
    Personality.Foodie -> "要不要一起吃点好吃的？"
    Personality.Tsundere -> "我只是刚好想陪你一下。"
}

fun feedFeedback(personality: Personality): String = when (personality) {
    Personality.Gentle -> "谢谢你，味道很温柔。"
    Personality.Energetic -> "好吃！能量回来了！"
    Personality.Shy -> "谢谢，悄悄开心一下。"
    Personality.Foodie -> "好吃！还能再来一小口吗？"
    Personality.Tsundere -> "还、还不错啦。"
}

private fun expressionFor(personality: Personality): String = when (personality) {
    Personality.Gentle -> "微笑"
    Personality.Energetic -> "星星眼"
    Personality.Shy -> "害羞"
    Personality.Foodie -> "期待"
    Personality.Tsundere -> "撇嘴"
}

private fun decorationFor(personality: Personality): String = when (personality) {
    Personality.Gentle -> "小围巾"
    Personality.Energetic -> "小铃铛"
    Personality.Shy -> "小帽子"
    Personality.Foodie -> "小饭碗"
    Personality.Tsundere -> "小披风"
}

private fun favoriteFoodFor(personality: Personality): String = when (personality) {
    Personality.Gentle -> "热牛奶"
    Personality.Energetic -> "能量果冻"
    Personality.Shy -> "小饼干"
    Personality.Foodie -> "草莓蛋糕"
    Personality.Tsundere -> "焦糖布丁"
}

private fun companionStyleFor(personality: Personality): String = when (personality) {
    Personality.Gentle -> "温柔陪伴"
    Personality.Energetic -> "元气鼓励"
    Personality.Shy -> "安静倾听"
    Personality.Foodie -> "用小点心安慰你"
    Personality.Tsundere -> "嘴硬但在乎你"
}

private fun stageThemeFor(personality: Personality): String = when (personality) {
    Personality.Gentle -> "warm"
    Personality.Energetic -> "starry"
    Personality.Shy -> "cloud"
    Personality.Foodie -> "candy"
    Personality.Tsundere -> "forest"
}

private fun accentEmojiFor(personality: Personality): String = when (personality) {
    Personality.Gentle -> "🌸"
    Personality.Energetic -> "⭐"
    Personality.Shy -> "☁️"
    Personality.Foodie -> "🍬"
    Personality.Tsundere -> "🍃"
}

private fun actionHintFor(personality: Personality): String = when (personality) {
    Personality.Gentle -> "calm"
    Personality.Energetic -> "bounce"
    Personality.Shy -> "calm"
    Personality.Foodie -> "sway"
    Personality.Tsundere -> "tilt"
}
