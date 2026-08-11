# 🍳 熙熙的小厨房 (Xixi's Kitchen)

> 一款专为情侣打造的浪漫 Android 原生双人点单与厨房互动 App，基于 **Jetpack Compose** 结合现代 **Glassmorphism (通透毛玻璃)** 视觉与极简手势设计。

---

## ✨ 核心亮点与设计美学

- 💎 **通透毛玻璃设计系统 (Pure Glassmorphic UI)**
  - 采用自定义 Canvas 渲染引擎，打造 3D 浮动凸面（`glassConvex`）与凹陷沉浸面（`glassConcave`）。
  - 内置柔和冰晶光晕（`GlassMesh`）与高光切边（Specular Edge），呈现极致清透的浮动视觉与高端质感。
- 👆 **极简手势交互 (Gesture-Driven Actions)**
  - 全面取消传统硬邦邦的侧边操作图标，回归极简纯粹。
  - **长按卡片**：直接唤起编辑/修改弹窗。
  - **左滑卡片**：配合滑动动画露出红底，触发安全二次确认弹窗（`确认删除`）。
- 🔒 **预设权限保护 (Preset Guard)**
  - 针对系统预设情侣账号（如“熙熙”与“哥哥”）启用全自动逻辑保护，隐藏非必要的操作项，确保核心情侣数据稳定安全；仅对新增用户开放完整管理权限。

---

## 📱 核心功能模块

| 模块 | 功能描述 |
| :--- | :--- |
| 🍽️ **点单 (Kitchen)** | 菜品分类切换、无跳动流畅搜索框、双人购物车 (`CartDialog`)、一键下单与实时菜品列表 |
| 📋 **订单 (Orders)** | 极简筛选（`全部` / `等喂食` / `做饭中` / `已享用`）、接单/拒单/完成烹饪生命周期管理、五星菜品评分 |
| 📢 **公告栏 (Discover)** | 情侣留言与社区公告，使用全一体化通透 `glassConvex` 悬浮面板展示 |
| 👤 **我的 (Mine)** | 极简个人信息卡片、长按修改昵称与头像、后台管理通道与快速退出 |
| ⚙️ **后台管理 (Admin Console)** | 超级管理员控制台，支持用户、分类、菜品、公告、订单 5 大模块切换与动态管理 |

---

## 🛠️ 技术栈 (Technology Stack)

- **语言**: Kotlin (1.9+ / 2.0)
- **UI 框架**: Jetpack Compose (Material 3)
- **渲染引擎**: 自定义 `GlassTokens` / `GlassMesh` 拟物毛玻璃 Canvas 渲染
- **手势管理**: Compose `SwipeToDismissBox` + `combinedClickable`
- **架构**: MVVM (Model-View-ViewModel) + LiveData / StateFlow
- **网络与图片**: Retrofit2 + OkHttp3 + Coil Compose
- **依赖注入**: Hilt / Dagger
- **导航**: Navigation Compose

---

## 🚀 项目构建与运行 (Build & Run)

### 环境要求
- **Android Studio**: Ladybug (2024.2.1) 或更高版本
- **JDK Version**: JDK 17+
- **Compile SDK**: 34
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

---

## 📂 项目结构 (Project Structure)

```text
couple-menu-android-jetpack/
├── app/src/main/java/com/xixikitchen/jetpack/
│   ├── data/                      # 数据模型 (User, Dish, Order等) 与 Backend 交互
│   ├── ui/
│   │   ├── designsystem/
│   │   │   └── theme/             # 玻璃态设计系统 (GlassTokens, GlassMesh, GlassTheme)
│   │   ├── screens/
│   │   │   └── orders/            # 订单组件与页面细节
│   │   ├── AdminScreens.kt        # 后台管理模块视图 (用户/分类/菜品/公告管理)
│   │   └── XixiKitchenApp.kt      # 主界面入口 (点单、订单、公告栏、我的、购物车弹窗)
│   └── MainActivity.kt            # 应用单 Activity 入口
├── build.gradle                   # 项目根 Gradle 配置
└── README.md                      # 项目说明文档
```

---

## 💖 致谢与说明

本项目专为情侣厨房点单打造，带来有温度、有仪式感的生活体验！
