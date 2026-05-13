package com.example.deskpet.util

import com.example.deskpet.model.Personality

object PetReplyEngine {
    fun replyTo(text: String, personality: Personality): String {
        val prefix = when (personality) {
            Personality.Gentle -> "我在听，"
            Personality.Energetic -> "收到！"
            Personality.Shy -> "嗯……"
            Personality.Foodie -> "先给你一块小点心，"
            Personality.Tsundere -> "我才不是特意担心你，"
        }

        val body = when {
            text.hasAny("不想活", "自杀", "伤害自己", "结束生命") ->
                "如果你现在有伤害自己的念头，请先联系身边可信任的人，或尽快拨打当地紧急服务电话。你不用一个人扛着。"
            text.hasAny("累", "疲惫", "困") ->
                "你今天好像真的很累。先不用急着变好，我们可以慢慢待一会儿。"
            text.hasAny("烦", "压力", "焦虑") ->
                "这份压力被你撑到现在已经很不容易了。你可以一点点说，我会陪你整理。"
            text.hasAny("难过", "伤心") ->
                "这件事让你难受是可以理解的。我们先把它放在这里，不急着解决。"
            text.hasAny("生气", "气") ->
                "你会生气一定有原因。先呼一口气，我陪你把事情慢慢讲清楚。"
            else ->
                "我在听，你可以慢慢说，不用一次说清楚。"
        }

        return prefix + body
    }
}

private fun String.hasAny(vararg keywords: String): Boolean {
    return keywords.any { contains(it, ignoreCase = true) }
}
