# DeskPet

DeskPet 是一个 Android 桌面宠物 MVP。当前版本使用 Kotlin + Jetpack Compose 做 Android 客户端，使用 Python FastAPI 做本地后端占位服务。MVP 目标是先完成“上传图片 -> 生成宠物属性 -> 互动/聊天 -> 生成心情日记”的闭环，不接真实 AI、不做真正 3D。

## 当前已实现

- 首页宠物展示与 2.5D 表现：圆角、阴影、底部影子、轻微悬浮、点击/投喂动画。
- 上传图片：使用 Android Photo Picker，选择后立即显示为宠物图像。
- 图片缓存：选择图片后会复制到 App 内部存储，重启 App 后仍可显示。
- 宠物资料：名字、性格、表情、装饰、喜欢的食物、陪伴风格、当前动作和心情文案。
- 宠物状态：心情、饥饿、精力、亲密度，数值限制在 0-100。
- 互动：点击、投喂、重新生成属性。
- 聊天：优先请求 Python 后端，失败时自动 fallback 到本地规则回复。
- 日记：聊天后生成心情日记，支持单条删除和清空全部。
- 本地持久化：使用 `DeskPetRepository + SharedPreferences(JSON)` 保存宠物、状态、聊天记录和日记。

## Android 运行

1. 用 Android Studio 打开项目根目录：

```text
D:\Pokemon_deskpet
```

2. 选择 Pixel 9 API 35 模拟器。
3. 点击 Android Studio 的 Run。

也可以在 PowerShell 编译：

```powershell
cd D:\Pokemon_deskpet
$env:JAVA_HOME='D:\AndroidStudio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :app:assembleDebug
```

编译产物位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Python 后端启动

后端位于 `backend/`，虚拟环境位于 `pokemon/`。

```powershell
cd D:\Pokemon_deskpet
.\pokemon\Scripts\python.exe -m uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload
```

健康检查：

```text
http://127.0.0.1:8000/health
```

## 为什么模拟器使用 10.0.2.2

Android 模拟器里的 `localhost` 指的是模拟器自己，不是电脑主机。Android Emulator 提供 `10.0.2.2` 作为“访问宿主机”的特殊地址，所以 App 内后端地址使用：

```text
http://10.0.2.2:8000
```

## 测试上传图片

把电脑图片放进模拟器：

```powershell
D:\AndroidSDK\platform-tools\adb.exe devices
D:\AndroidSDK\platform-tools\adb.exe push D:\Pokemon_deskpet\c3b3448e-9114-4e48-ac11-b27a15d01d45.png /sdcard/Pictures/deskpet.png
D:\AndroidSDK\platform-tools\adb.exe shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/deskpet.png
```

然后在 App 首页点击“上传图片”，从系统图片选择器中选择 `deskpet.png`。

选择后 App 会把图片复制到自己的内部存储，因此后续关闭再打开 App，宠物图像也会保留。

## 测试聊天

1. 启动 Python 后端。
2. App 首页点击“和它说说话”。
3. 输入“今天压力有点大”并发送。
4. 后端终端应看到 `/api/chat` 请求，App 中会显示宠物回复。
5. 进入“查看日记”，确认生成心情记录。

## 测试后端 fallback

1. 关闭 Python 后端。
2. 在聊天页继续发送消息。
3. App 应自动使用本地规则回复，不崩溃，并在首页反馈“后端暂时没连上，我先陪你聊聊。”

## 当前未实现但计划实现

- Room 持久化替换 SharedPreferences JSON。
- Photo Picker 长期 URI 权限和图片缓存策略。
- 日记详情页、搜索和导出。
- 真实 AI 聊天和图片模型，后端转发，不把 API Key 放进 App。
- 图片裁剪、主体分割、2.5D 抠图。
- 动态壁纸、悬浮窗、3D GLB/glTF 宠物。
