package com.example.deskpet.model

enum class Personality {
    Gentle,
    Energetic,
    Shy,
    Foodie,
    Tsundere
}

enum class PetAction {
    Idle,
    Happy,
    Clicked,
    Eating,
    Listening,
    Sleepy,
    Excited,
    Comforting
}

data class PetProfile(
    val id: String,
    val name: String,
    val imageUri: String?,
    val personality: Personality,
    val action: PetAction,
    val expression: String,
    val decoration: String,
    val favoriteFood: String,
    val moodText: String,
    val companionStyle: String,
    val seed: Long,
    val createdAt: Long
)
