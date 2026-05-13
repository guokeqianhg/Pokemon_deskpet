from fastapi import FastAPI, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware

from backend.schemas import ChatRequest, ChatResponse, ImagePetResponse
from backend.services import build_chat_reply, build_pet_from_image


app = FastAPI(title="DeskPet Backend", version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "service": "deskp et-backend".replace(" ", "")}


@app.post("/api/chat", response_model=ChatResponse)
def chat(request: ChatRequest) -> ChatResponse:
    result = build_chat_reply(
        message=request.message,
        personality=request.pet.personality,
    )
    return ChatResponse(
        reply=result.reply,
        mood_tag=result.mood_tag,
        diary_summary=result.diary_summary,
    )


@app.post("/api/pet/from-image", response_model=ImagePetResponse)
async def pet_from_image(file: UploadFile = File(...)) -> ImagePetResponse:
    content = await file.read()
    result = build_pet_from_image(file.filename or "", content)
    return ImagePetResponse(
        original_filename=file.filename or "",
        pet_name=result.pet_name,
        personality=result.personality,
        expression=result.expression,
        decoration=result.decoration,
        favorite_food=result.favorite_food,
        companion_style=result.companion_style,
        stage_theme=result.stage_theme,
        accent_emoji=result.accent_emoji,
        action_hint=result.action_hint,
        description=result.description,
    )
