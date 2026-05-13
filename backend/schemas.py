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
    description: str
