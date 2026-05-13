# DeskPet

DeskPet 是一个 Android 桌面宠物 MVP。当前版本使用 Kotlin + Jetpack Compose 做 Android 客户端，使用 Python FastAPI 做本地后端占位服务。MVP 目标是先完成“上传图片 -> 生成宠物属性 -> 互动/聊天 -> 生成心情日记”的闭环，不接真实 AI、不做真正 3D。

## 当前已实现

- 首页宠物展示与 2.5D 表现：圆角、阴影、底部影子、轻微悬浮、点击/投喂动画。
- 宠物舞台：上传图片会被放入带背景光晕、地面阴影、装饰层和状态气泡的舞台，不再裸显示图片。
- 动作状态生命周期：点击、投喂、聊天倾听/安慰等短暂动作会自动复位，状态气泡也会回到默认待机文案。
- 上传图片：使用 Android Photo Picker，选择后立即显示为宠物图像。
- 主体化展示：上传图会使用柔和主体模式显示，包含居中裁剪、椭圆/圆角遮罩、背景弱化、边缘柔化和底部阴影。
- 图片缓存：选择图片后会压缩并复制到 App 私有目录，重启 App 后仍可显示。
- 数据持久化：当前使用 Room 持久化宠物资料、状态、日记和聊天记录。
- 兼容迁移：如果旧版 SharedPreferences JSON 中已有数据，首次进入新版会自动迁移到 Room。
- 舞台建议字段：后端图片接口会返回 `stage_theme`、`accent_emoji`、`action_hint`，Android 会用于舞台主题、装饰层和动画风格。
- 宠物资料：名字、性格、表情、装饰、喜欢的食物、陪伴风格、当前动作和心情文案。
- 宠物状态：心情、饥饿、精力、亲密度，数值限制在 0-100。
- 互动：点击、投喂、重新生成属性。
- 聊天：优先请求 Python 后端，失败时自动 fallback 到本地规则回复。
- 日记：聊天后生成心情日记，支持单条删除和清空全部。
- 日记详情：可以从日记列表进入详情页，查看完整内容并删除当前记录。
- 设置页：支持测试后端连接、清空日记、重置宠物、清理本地图片缓存。
- 本地持久化：当前使用 Room 保存宠物、状态、聊天记录和日记。

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

后端还提供占位抠图接口：

```text
POST http://127.0.0.1:8000/api/pet/cutout
```

它当前不会调用真实 AI 或背景移除模型，只返回 `soft_cutout` 建议，Android 端继续使用本地主体化展示。后续可以把这个接口替换为真实抠图服务。

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

上传横图或竖图后，首页宠物舞台会用柔和主体模式展示图片：主体层居中裁剪，边缘更圆润，背后有弱化光晕和阴影，看起来更像一个桌宠主体，而不是完整矩形照片。

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

## 测试动作复位

1. 首页点击宠物，确认出现点击反馈和弹跳/偏转动画。
2. 等待约 0.9 秒，确认动作回到“待机”，气泡回到默认陪伴文案。
3. 快速连续点击 10 次，确认 App 不崩溃，最后仍能自动回到待机。
4. 点击“投喂”，确认进入吃东西状态；约 2 秒后短暂开心，再回到待机。
5. 快速连续点投喂，按钮会在投喂中禁用，不会叠加多个旧动画流程。

## 测试日记持久化和详情页

1. 启动 App，先和宠物聊几句。
2. 进入“查看日记”，确认列表中出现新记录。
3. 点开某条日记进入详情页，再返回。
4. 彻底关闭 App，重新打开。
5. 再次进入日记页，确认记录仍然存在。

## 测试设置页

1. 首页点击“设置”。
2. 点“测试后端连接”，确认有成功或失败反馈。
3. 点“清空全部日记”，确认会弹二次确认框。
4. 点“重置宠物”，确认宠物资料和状态被恢复默认。
5. 点“清理本地图片缓存”，确认宠物图片被清掉并回到默认占位图。

## 当前未实现但计划实现

- 真实抠图、主体分割和真正 3D 模型。
- 日记搜索和导出。
- 真实 AI 聊天和图片模型，后端转发，不把 API Key 放进 App。
- 动态壁纸、悬浮窗、3D GLB/glTF 宠物。
- 云同步。
