from pydantic import BaseModel, Field


class PetContext(BaseModel):
    pet_id: str = Field(default="")
    pet_name: str = Field(default="团团")
    personality: str = Field(default="Gentle")


class ChatRequest(BaseModel):
    message: str
    pet: PetContext = Field(default_factory=PetContext)


class ChatResponse(BaseModel):
    reply: str
    mood_tag: str
    diary_summary: str


class ImagePetResponse(BaseModel):
    original_filename: str
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


class CutoutResponse(BaseModel):
    success: bool
    mode: str
    message: str
    image_url: str | None = None
    processed_image_path: str | None = None
    suggested_crop: str = "center"
    confidence: float = 0.5
