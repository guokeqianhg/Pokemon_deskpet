# DeskPet 安卓桌面宠物 App：Codex 实现说明文档

> 用途：把本文档整体复制到 Codex / AI 编程助手中，让它根据当前 Android Studio 项目逐步实现功能。  
> 当前项目：Android Studio 新建的 `Kotlin + Jetpack Compose` 项目。  
> 当前包名示例：`com.example.deskpet`。  
> 当前目标：先做可运行的 MVP，不要一开始做完整 3D、悬浮窗、云同步和好友系统。

---

## 1. 产品目标

我要做一个安卓 App，名字暂定为 **DeskPet**。

核心效果：

```text
用户上传一张图片
→ App 把它显示成一个“桌面宠物”形象
→ 宠物拥有随机性格、动作、表情、装饰等属性
→ 用户可以点击、投喂、和宠物说话
→ 宠物可以温柔回应用户的烦心事
→ 用户的倾诉和宠物回复可以保存成日记
→ 未来可扩展动态壁纸、3D 宠物、好友宠物互动
```

第一版不追求真正的 AI 3D 建模，先做 **App 内 2D / 2.5D 宠物 MVP**。

---

## 2. 当前开发阶段

当前已经完成：

```text
✅ Android Studio 已安装
✅ Kotlin + Jetpack Compose 项目已创建
✅ Pixel 9 API 35 模拟器已创建
✅ 项目可以运行到模拟器
```

现在需要从默认模板页面开始，逐步开发 DeskPet 的第一版功能。

---

## 3. 技术栈要求

请基于现有 Android 项目实现，优先使用：

```text
语言：Kotlin
UI：Jetpack Compose
架构：MVVM
状态管理：ViewModel + StateFlow / MutableStateFlow
图片选择：Android Photo Picker / ActivityResultContracts.PickVisualMedia
图片加载：Coil Compose
动画：Compose Animation
本地设置：DataStore，后续阶段再接
本地数据库：Room，日记阶段再接
网络：MVP 阶段暂不接真实 AI API
```

第一阶段尽量保持依赖简单。能先用 Compose 和本地状态实现的，不要过早引入复杂框架。

---

## 4. 第一版 MVP 范围

第一版要实现：

```text
1. 首页 UI
2. 默认宠物显示
3. 上传图片并替换宠物图片
4. 随机生成宠物性格、动作、表情、装饰
5. 点击宠物互动
6. 投喂宠物
7. 本地规则版“倾听”聊天
8. 本地日记列表
```

第一版暂时不要做：

```text
❌ 真正 3D 模型生成
❌ 动态壁纸
❌ 悬浮窗
❌ 好友系统
❌ 登录注册
❌ 云同步
❌ 真实 AI API
❌ 支付
❌ 上架应用商店
```

---

## 5. App 页面结构

建议页面：

```text
HomeScreen       首页 / 宠物互动页
ChatScreen       倾诉聊天页
DiaryScreen      日记列表页
DiaryDetailScreen 日记详情页，后续可加
```

MVP 可先不引入 Navigation，使用简单状态切换页面。后续可以加入 `Navigation Compose`。

---

## 6. 首页功能要求

首页布局大致如下：

```text
我的桌面宠物

[宠物显示区域]

名字：团团
性格：温柔
当前状态：开心
装饰：小围巾

心情：85
饥饿：30
精力：70
亲密度：12

[上传图片]
[重新生成属性]
[投喂]
[和它说说话]
[查看日记]
```

首页交互：

```text
点击宠物：
- 宠物执行弹跳动画
- action 变成 Happy 或 Clicked
- intimacy + 1
- 显示一句宠物反馈文案

点击投喂：
- action 变成 Eating
- hunger 降低
- mood 增加
- 显示投喂反馈文案
- 几秒后回到 Idle

点击重新生成属性：
- 重新随机 personality、decoration、petName 等
- 性格和动作/回复风格要对应

点击上传图片：
- 打开系统图片选择器
- 用户选择图片后，首页宠物图片替换为用户图片

点击和它说说话：
- 进入 ChatScreen

点击查看日记：
- 进入 DiaryScreen
```

---

## 7. 宠物数据模型

请创建类似的数据结构。

```kotlin
enum class Personality {
    Gentle,
    Energetic,
    Shy,
    Foodie,
    Tsundere
}

enum class PetAction {
    Idle,
    Happy,
    Clicked,
    Eating,
    Sleeping,
    Comforting
}

data class PetProfile(
    val id: String,
    val name: String,
    val imageUri: String?,
    val personality: Personality,
    val action: PetAction,
    val expression: String,
    val decoration: String,
    val seed: Long,
    val createdAt: Long
)

data class PetStatus(
    val mood: Int,
    val hunger: Int,
    val energy: Int,
    val intimacy: Int
)
```

数值范围：

```text
mood: 0-100
hunger: 0-100，数值越高越饿
energy: 0-100
intimacy: 0-100
```

请提供 clamp 逻辑，防止数值超过 0-100。

---

## 8. 性格、动作、表情、装饰规则

随机生成时，请保证性格和动作/回复风格对应。

规则建议：

| 性格 | 动作倾向 | 表情 | 装饰 | 回复风格 |
|---|---|---|---|---|
| Gentle 温柔 | 点头、靠近、陪伴 | 微笑 | 小围巾 | 温柔安慰 |
| Energetic 活泼 | 跳跃、转圈 | 星星眼 | 小铃铛 | 元气鼓励 |
| Shy 慢热 | 探头、躲一下 | 害羞 | 小帽子 | 简短但真诚 |
| Foodie 贪吃 | 找食物、抱零食 | 期待 | 小饭碗 | 可爱贪吃 |
| Tsundere 傲娇 | 转身、偷看 | 撇嘴 | 小披风 | 嘴硬心软 |

请实现：

```kotlin
fun generateRandomPet(currentImageUri: String?): PetProfile
fun defaultStatus(): PetStatus
fun personalityDisplayName(personality: Personality): String
fun actionDisplayName(action: PetAction): String
```

---

## 9. 宠物动画要求

MVP 只需要简单动画：

```text
Idle：轻微上下浮动
Clicked：快速放大再恢复
Eating：轻微左右摇摆，并显示“好吃！”
Happy：弹跳
Comforting：慢速靠近/放大一点点，显示温柔文案
```

可以使用：

```kotlin
rememberInfiniteTransition
animateFloatAsState
LaunchedEffect
Modifier.scale()
Modifier.offset()
Modifier.clickable()
```

---

## 10. 图片上传要求

使用 Android Photo Picker。

功能：

```text
点击“上传图片”
→ 打开系统图片选择器
→ 选择一张图片
→ 把 Uri 保存到 PetProfile.imageUri
→ 用 Coil 的 AsyncImage 显示出来
```

依赖建议：

```kotlin
implementation("io.coil-kt:coil-compose:2.7.0")
```

注意：

```text
MVP 阶段只需要显示用户图片。
暂时不做抠图、裁剪、主体分割。
后续再扩展成 2.5D 处理和 AI 抠图。
```

---

## 11. 聊天 / 倾听功能要求

第一版不要接真实 AI API，先做本地规则回复。

ChatScreen 布局：

```text
[返回]
和宠物说说话

聊天记录列表
用户：今天有点累
宠物：我听见啦，今天辛苦了。我们可以先慢慢待一会儿。

[输入框：把烦心事告诉我吧]
[发送]
```

数据结构：

```kotlin
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
```

本地回复规则：

```text
包含“累”“疲惫”“困”：回复休息陪伴类
包含“烦”“压力”“焦虑”：回复理解和陪伴类
包含“难过”“伤心”：回复温柔安慰类
包含“生气”“气”：回复共情和冷静类
其他：回复“我在听，你可以慢慢说。”
```

注意：

```text
宠物不能自称心理医生。
宠物不能做诊断。
宠物不能给医疗建议。
宠物应该是陪伴、倾听、鼓励、记录。
遇到明显危机风险内容时，应温和建议用户联系可信任的人或当地紧急服务/专业人士。
```

示例回复：

```text
“我在听，你今天好像真的很累。先不用急着变好，我陪你待一会儿。”
“这件事让你难受是可以理解的。我们可以先把它写下来。”
“我会陪着你。你可以慢慢说，不用一次说清楚。”
```

---

## 12. 日记功能要求

第一版可以先用内存列表保存，后续再接 Room。

当用户发送一段倾诉并收到宠物回复后，可以生成一条日记。

数据结构：

```kotlin
data class DiaryEntry(
    val id: String,
    val petId: String,
    val userText: String,
    val petReply: String,
    val summary: String,
    val moodTag: String,
    val createdAt: Long
)
```

日记生成规则：

```text
summary：取用户输入前 20-30 个字，或根据关键词生成简短摘要
moodTag：根据关键词判断，如“疲惫”“压力”“难过”“生气”“普通”
createdAt：当前时间戳
```

DiaryScreen 布局：

```text
[返回]
我的心情日记

2026-05-13
标题/摘要：今天有点累
心情：疲惫
宠物回复：我在听，你今天好像真的很累……

[删除]，后续可加
```

MVP 阶段可以用 ViewModel 内存保存。后续再改成 Room 持久化。

---

## 13. 建议项目文件结构

请尽量整理代码，不要全部塞进 MainActivity.kt。

建议结构：

```text
app/src/main/java/com/example/deskpet/
├── MainActivity.kt
├── model/
│   ├── PetProfile.kt
│   ├── PetStatus.kt
│   ├── ChatMessage.kt
│   └── DiaryEntry.kt
├── ui/
│   ├── HomeScreen.kt
│   ├── ChatScreen.kt
│   ├── DiaryScreen.kt
│   └── components/
│       ├── PetAvatar.kt
│       ├── PetStatusPanel.kt
│       └── ActionButton.kt
├── viewmodel/
│   └── DeskPetViewModel.kt
└── util/
    ├── PetGenerator.kt
    ├── PetReplyEngine.kt
    └── DiaryHelper.kt
```

可以根据现有项目结构微调，但请保持职责清晰。

---

## 14. ViewModel 职责

创建 `DeskPetViewModel`，负责统一管理状态。

建议包含：

```kotlin
class DeskPetViewModel : ViewModel() {
    val petProfile: StateFlow<PetProfile>
    val petStatus: StateFlow<PetStatus>
    val chatMessages: StateFlow<List<ChatMessage>>
    val diaryEntries: StateFlow<List<DiaryEntry>>
    val currentScreen: StateFlow<AppScreen>

    fun onPetClicked()
    fun feedPet()
    fun regeneratePet()
    fun updatePetImage(uri: String)
    fun sendUserMessage(text: String)
    fun goToHome()
    fun goToChat()
    fun goToDiary()
}
```

页面切换可以先用简单 enum：

```kotlin
enum class AppScreen {
    Home,
    Chat,
    Diary
}
```

---

## 15. UI 风格要求

整体风格：

```text
温柔
可爱
治愈
简洁
适合长期陪伴
```

颜色可以先使用 Material 3 默认主题，不需要复杂设计。

首页视觉建议：

```text
大标题
圆角卡片
宠物居中
按钮明显
文字温柔
状态展示不要太复杂
```

---

## 16. 开发顺序要求

请按阶段逐步实现，不要一次性改太多。

### 阶段 1：首页 UI

实现：

```text
HomeScreen
宠物显示区域
状态卡片
上传图片按钮
重新生成属性按钮
投喂按钮
聊天入口
日记入口
```

验收：

```text
App 运行后不再显示 Hello Android
而是显示 DeskPet 首页
按钮可见
页面无崩溃
```

---

### 阶段 2：宠物状态和随机属性

实现：

```text
PetProfile
PetStatus
PetGenerator
DeskPetViewModel
点击重新生成属性后，宠物性格、装饰、名字变化
```

验收：

```text
点击“重新生成属性”后，首页显示的性格、装饰、状态会变化
```

---

### 阶段 3：点击和投喂互动

实现：

```text
点击宠物
投喂按钮
状态数值变化
简单动画
反馈文案
```

验收：

```text
点击宠物后亲密度增加
投喂后饥饿值下降、心情上升
宠物有简单动画反馈
```

---

### 阶段 4：图片上传

实现：

```text
Photo Picker
Coil 显示图片
用户选择图片后替换默认宠物图
```

验收：

```text
点击上传图片可以打开系统相册
选择图片后首页显示该图片
```

---

### 阶段 5：聊天页面

实现：

```text
ChatScreen
输入框
发送按钮
聊天记录
本地规则回复
```

验收：

```text
输入一句话并点击发送后
页面显示用户消息和宠物回复
```

---

### 阶段 6：日记页面

实现：

```text
发送聊天后生成日记
DiaryScreen 显示日记列表
返回首页
```

验收：

```text
聊天后进入日记页面能看到刚刚的内容摘要
```

---

## 17. 后续扩展路线，暂不实现

MVP 完成后再考虑：

```text
1. Room 持久化日记
2. DataStore 保存当前宠物
3. 接入真实 AI API，后端转发，不把 API Key 放进 App
4. 图片裁剪、抠图、2.5D 视觉处理
5. 动态壁纸 WallpaperService
6. 悬浮窗 SYSTEM_ALERT_WINDOW
7. 3D GLB / glTF 宠物加载
8. 登录、云同步、好友宠物访问
```

---

## 18. 代码质量要求

请遵守：

```text
1. 代码能编译运行
2. 不要破坏现有 Gradle 配置
3. 每次新增依赖都要说明原因
4. UI 不要写死太多重复代码
5. 数据模型和 UI 分开
6. ViewModel 管状态，Composable 负责展示
7. 不要在 Composable 里写大量业务逻辑
8. 图片 Uri 先用 String 保存，后续再优化
9. 日记先内存保存，后续再接 Room
10. 回复系统先本地规则，后续再接 AI
```

---

## 19. 给 Codex 的直接执行指令

请根据以上说明，在当前 Android Studio 项目中实现 DeskPet MVP。

优先从以下任务开始：

```text
任务 1：重构 MainActivity.kt，创建 HomeScreen。
任务 2：创建 PetProfile、PetStatus、Personality、PetAction 数据模型。
任务 3：创建 DeskPetViewModel 管理宠物状态。
任务 4：实现首页 UI，包括宠物区域、状态信息、上传图片、重新生成、投喂、聊天、日记按钮。
任务 5：实现随机宠物属性生成。
任务 6：实现点击宠物和投喂互动。
任务 7：实现 Photo Picker 和 Coil 图片显示。
任务 8：实现 ChatScreen 的本地规则回复。
任务 9：实现 DiaryScreen 的内存日记列表。
```

每完成一个任务，请确保项目可以编译运行。

---

## 20. 最终 MVP 验收标准

完成后，App 应该满足：

```text
打开 App 看到“我的桌面宠物”首页
首页显示默认宠物区域
可以上传图片替换宠物图片
可以随机生成宠物性格和装饰
可以点击宠物获得互动反馈
可以投喂宠物并改变状态
可以进入聊天页输入烦心事
宠物会用本地规则回复
聊天内容会生成日记
可以在日记页查看历史记录
```

---

## 21. 重要产品原则

```text
这个 App 的定位是陪伴和记录，不是医疗诊断工具。
宠物应该表达理解、陪伴和鼓励。
不要让宠物给出诊断、治疗承诺或替代专业帮助。
用户的日记和倾诉内容属于隐私数据，后续做持久化和云同步时必须重视本地加密、删除权和明确告知。
```

