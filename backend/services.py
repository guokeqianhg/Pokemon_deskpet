from __future__ import annotations

import hashlib
from dataclasses import dataclass


PERSONALITY_NAMES = {
    "Gentle": "温柔",
    "Energetic": "活泼",
    "Shy": "慢热",
    "Foodie": "贪吃",
    "Tsundere": "傲娇",
}


@dataclass(frozen=True)
class ChatResult:
    reply: str
    mood_tag: str
    diary_summary: str


@dataclass(frozen=True)
class ImagePetResult:
    pet_name: str
    personality: str
    expression: str
    decoration: str
    favorite_food: str
    companion_style: str
    stage_theme: str
    accent_emoji: str
    action_hint: str
    description: str


@dataclass(frozen=True)
class CutoutResult:
    success: bool
    mode: str
    message: str
    suggested_crop: str
    confidence: float


def build_chat_reply(message: str, personality: str) -> ChatResult:
    text = message.strip()
    prefix = _reply_prefix(personality)

    if _has_any(text, "不想活", "自杀", "伤害自己", "结束生命"):
        body = (
            "如果你现在有伤害自己的念头，请先联系身边可信任的人，"
            "或尽快拨打当地紧急服务电话。你不用一个人扛着。"
        )
        mood = "危机"
    elif _has_any(text, "累", "疲惫", "困"):
        body = "你今天好像真的很累。先不用急着变好，我们可以慢慢待一会儿。"
        mood = "疲惫"
    elif _has_any(text, "烦", "压力", "焦虑"):
        body = "这份压力被你撑到现在已经很不容易了。你可以一点点说，我会陪你整理。"
        mood = "压力"
    elif _has_any(text, "难过", "伤心"):
        body = "这件事让你难受是可以理解的。我们先把它放在这里，不急着解决。"
        mood = "难过"
    elif _has_any(text, "生气", "气"):
        body = "你会生气一定有原因。先呼一口气，我陪你把事情慢慢讲清楚。"
        mood = "生气"
    else:
        body = "我在听，你可以慢慢说，不用一次说清楚。"
        mood = "普通"

    return ChatResult(
        reply=prefix + body,
        mood_tag=mood,
        diary_summary=_summarize(text),
    )


def build_pet_from_image(filename: str, content: bytes) -> ImagePetResult:
    digest = hashlib.sha256(content or filename.encode("utf-8")).hexdigest()
    index = int(digest[:2], 16) % 5
    personalities = ["Gentle", "Energetic", "Shy", "Foodie", "Tsundere"]
    personality = personalities[index]

    expression, decoration, favorite_food, companion_style, stage_theme, accent_emoji, action_hint = {
        "Gentle": ("微笑", "小围巾", "热牛奶", "温柔陪伴", "warm", "🌸", "calm"),
        "Energetic": ("星星眼", "星星发夹", "能量果冻", "元气鼓励", "starry", "⭐", "bounce"),
        "Shy": ("害羞", "云朵贴纸", "小饼干", "安静倾听", "cloud", "☁️", "calm"),
        "Foodie": ("期待", "糖果", "草莓蛋糕", "用小点心安慰你", "candy", "🍬", "sway"),
        "Tsundere": ("撇嘴", "小帽子", "焦糖布丁", "嘴硬但在乎你", "forest", "🍃", "tilt"),
    }[personality]

    names = ["团团", "软糖", "布丁", "栗子", "云朵"]
    pet_name = names[int(digest[2:4], 16) % len(names)]

    return ImagePetResult(
        pet_name=pet_name,
        personality=personality,
        expression=expression,
        decoration=decoration,
        favorite_food=favorite_food,
        companion_style=companion_style,
        stage_theme=stage_theme,
        accent_emoji=accent_emoji,
        action_hint=action_hint,
        description=f"已根据 {filename or '用户图片'} 生成一个{PERSONALITY_NAMES[personality]}风格的桌面宠物占位结果。",
    )


def build_pet_cutout(filename: str, content: bytes) -> CutoutResult:
    return CutoutResult(
        success=True,
        mode="soft_cutout",
        message="当前使用本地主体化展示，后续可接入真实抠图模型",
        suggested_crop="center",
        confidence=0.5 if content or filename else 0.0,
    )


def _reply_prefix(personality: str) -> str:
    return {
        "Gentle": "我在听，",
        "Energetic": "收到！",
        "Shy": "嗯……",
        "Foodie": "先给你一块小点心，",
        "Tsundere": "我才不是特意担心你，",
    }.get(personality, "我在听，")


def _has_any(text: str, *keywords: str) -> bool:
    return any(keyword in text for keyword in keywords)


def _summarize(text: str) -> str:
    if len(text) <= 28:
        return text
    return text[:28] + "..."
