package com.example.deskpet.model

data class PetStatus(
    val mood: Int,
    val hunger: Int,
    val energy: Int,
    val intimacy: Int
) {
    fun clamped(): PetStatus = copy(
        mood = mood.coerceIn(0, 100),
        hunger = hunger.coerceIn(0, 100),
        energy = energy.coerceIn(0, 100),
        intimacy = intimacy.coerceIn(0, 100)
    )
}
