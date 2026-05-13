package com.example.deskpet.model

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val text: String,
    val createdAt: Long
)

enum class MessageRole {
    User,
    Pet
}
