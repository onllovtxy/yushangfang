# 🍳 熙熙的小厨房 (Xixi's Kitchen)

> 一款专为情侣打造的浪漫 Android 原生双人点单与厨房互动 App，基于 **Jetpack Compose** 结合现代 **Glassmorphism (通透毛玻璃)** 视觉与极简手势设计。

---

## ✨ 核心亮点与设计美学

- 💎 **通透毛玻璃设计系统 (Pure Glassmorphic UI)**
  - 采用自定义 Canvas 渲染引擎，打造 3D 浮动凸面（`glassConvex`）与凹陷沉浸面（`glassConcave`）。
  - 内置柔和冰晶光晕（`GlassMesh`）与高光切边（Specular Edge），呈现极致清透的浮动视觉与高端质感。
- 🎨 **可自定义主题 (Theme System)**
  - 内置多套主题，并可在设置页新建、保存、删除自定义配色。
  - 主题选择本地持久化且同步读取，冷启动第一帧就是所选配色，不会先闪一下默认配色。
- 👆 **极简手势交互 (Gesture-Driven Actions)**
  - 全面取消传统硬邦邦的侧边操作图标，回归极简纯粹。
  - **长按卡片**：直接唤起编辑/修改弹窗。
  - **左滑卡片**：iOS 风格停驻式滑动（`anchoredDraggable`），滑到露出删除按钮即停、不会滑过头，触发安全二次确认弹窗（`确认删除`）。
- 🔒 **预设权限保护 (Preset Guard)**
  - 针对系统预设情侣账号（如“熙熙”与“哥哥”）启用全自动逻辑保护，隐藏非必要的操作项，确保核心情侣数据稳定安全；仅对新增用户开放完整管理权限。

---

## 📱 核心功能模块

| 模块 | 功能描述 |
| :--- | :--- |
| 🍽️ **点单 (Kitchen)** | 菜品分类切换、300ms 防抖的无跳动搜索框、双人购物车 (`CartDialog`)、一键下单与实时菜品列表 |
| 📋 **订单 (Orders)** | 极简筛选（`全部` / `等喂食` / `做饭中` / `已享用`）、接单/拒单/完成烹饪生命周期管理 |
| 📢 **公告栏 (Discover)** | 情侣留言与社区公告，使用全一体化通透 `glassConvex` 悬浮面板展示 |
| 👤 **我的 (Mine)** | 极简个人信息卡片、长按修改昵称与头像、主题设置入口、后台管理通道与快速退出 |
| ⚙️ **后台管理 (Admin Console)** | 超级管理员控制台，支持 用户 / 分类 / 菜品 / 公告 / 订单 5 大模块切换与动态管理 |

---

## 🛠️ 技术栈 (Technology Stack)

- **语言**: Kotlin 2.0.21
- **UI 框架**: Jetpack Compose（BOM 2025.05.00）+ Material 3
- **渲染引擎**: 自定义 `GlassTokens` / `GlassMesh` 拟物毛玻璃 Canvas 渲染
- **手势管理**: Compose `anchoredDraggable`（停驻式左滑）+ `combinedClickable`（长按）
- **架构**: MVVM (Model-View-ViewModel) + StateFlow
- **网络与图片**: Retrofit2 + OkHttp3 + Gson + Coil Compose
- **本地安全存储**: `EncryptedSharedPreferences`（AES256-GCM）保存登录令牌
- **依赖注入**: Hilt / Dagger
- **导航**: Navigation Compose（一级页面互切无转场动画）

---

## 🚀 项目构建与运行 (Build & Run)

### 环境要求
- **Android Studio**: Ladybug (2024.2.1) 或更高版本
- **JDK Version**: JDK 17+
- **Compile SDK**: 36
- **Target SDK**: 36
- **Min SDK**: 24

### 常用 Gradle 命令

```bash
# 1. 编译并生成调试包 (Debug APK)
.\gradlew.bat assembleDebug

# 2. 编译并生成正式安装包 (Release APK)
.\gradlew.bat assembleRelease

# 3. 运行完整构建与单元测试
.\gradlew.bat build
```

*打包产物位置：*
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`

### 发版约定

**每次打包都要递增版本号**，两个都要改（`app/build.gradle` 的 `defaultConfig`）：

- `versionCode`：**必须**递增，否则手机会拒绝覆盖安装。
- `versionName`：每次交付都进一位（1.0.7 → 1.0.8），不要几个不同的包顶着同一个版本号。

当前版本：**1.0.9**（versionCode 11）

### ⚠️ 签名密钥说明

本仓库**当前跟踪着** release 签名文件 `app/xixi-release.jks` 及其配置 `keystore.properties`（内含明文密码）。

这么做的代价是：**任何能读到本仓库的人，都能签出同一签名的 APK**。如果仓库是对外公开的，或将来要上架应用商店，建议尽快处理：

1. 把 `keystore.properties` 从 git 中移除并加入 `.gitignore`。`app/build.gradle` 已支持从 `local.properties`、Gradle 属性或环境变量（`RELEASE_STORE_FILE` / `RELEASE_STORE_PASSWORD` / `RELEASE_KEY_ALIAS` / `RELEASE_KEY_PASSWORD`）读取这些值，本地打包流程不受影响。
2. 轮换签名密钥。注意签名一旦改变，已安装的旧版本将**无法覆盖升级**，用户必须卸载重装。

---

## 📂 项目结构 (Project Structure)

```text
yushangfang/
├── app/src/main/java/com/xixikitchen/jetpack/
│   ├── data/                      # 数据层
│   │   ├── Models.kt              #   User / Dish / Order / Category 等数据模型
│   │   ├── ApiService.kt          #   Retrofit 接口定义
│   │   ├── ApiClient.kt           #   OkHttp / Retrofit 装配
│   │   ├── KitchenRepository.kt   #   统一数据入口，负责拆包与错误抛出
│   │   ├── SessionStore.kt        #   加密存储登录态
│   │   └── BackendConfig.kt       #   后端地址规范化与图片 URL 解析
│   ├── di/
│   │   └── AppModule.kt           # Hilt 依赖注入
│   ├── ui/
│   │   ├── designsystem/
│   │   │   ├── theme/             # 玻璃态设计系统 (GlassTokens / GlassMesh / GlassTheme / 自定义主题)
│   │   │   └── motion/            # 动效时长与缓动，含"减少动效"适配
│   │   ├── XixiKitchenApp.kt      # 主界面入口 (登录、点单、订单、公告栏、我的、购物车弹窗)
│   │   ├── AdminScreens.kt        # 后台管理模块视图 (用户/分类/菜品/公告/订单)
│   │   ├── SettingsScreen.kt      # 设置页与主题选择
│   │   ├── CustomThemeDialog.kt   # 自定义主题编辑器
│   │   ├── KitchenViewModel.kt    # 全局单一 ViewModel (StateFlow)
│   │   ├── UploadFileNames.kt     # 图片读取、EXIF 方向校正与压缩
│   │   ├── FoodEmojis.kt          # 无图菜品的 emoji 兜底头像
│   │   └── UiModifiers.kt         # 无涟漪按压等自定义 Modifier
│   └── MainActivity.kt            # 应用单 Activity 入口
├── app/src/test/                  # 单元测试 (一级导航策略)
├── build.gradle                   # 项目根 Gradle 配置
└── README.md                      # 项目说明文档
```

---

## 🔗 后端服务

服务端是独立仓库 `couple-menu-server`（Spring Boot 3.2.5 + Kotlin + MySQL + MyBatis-Plus + Flyway），线上地址 `https://bbxy.art/`。

---

## 💖 致谢与说明

本项目专为情侣厨房点单打造，带来有温度、有仪式感的生活体验！
