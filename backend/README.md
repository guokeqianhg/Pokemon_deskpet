# DeskPet Python Backend

FastAPI service for the DeskPet MVP.

Run from the project root:

```powershell
.\pokemon\Scripts\python.exe -m pip install -r backend\requirements.txt
.\pokemon\Scripts\python.exe -m uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload
```

Android emulator base URL:

```text
http://10.0.2.2:8000
```

Local browser health check:

```text
http://127.0.0.1:8000/health
```
