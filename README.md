# 🍳 熙熙的小厨房 (Xixi's Kitchen)

> 一款支持多情侣独立空间的浪漫 Android 原生双人点单与厨房互动 App，基于 **Jetpack Compose** 结合 **Glassmorphism（通透毛玻璃）** 视觉系统与极简手势交互设计。

---

## ✨ 核心亮点与设计美学

- 💎 **通透毛玻璃设计系统 (Pure Glassmorphic UI)**
  - 采用自定义 Canvas 渲染引擎，打造 3D 浮动凸面（`glassConvex`）与凹陷沉浸面（`glassConcave`）。
  - 内置柔和冰晶光晕（`GlassMesh`）与高光切边，二级页面叠加背景毛玻璃景深模糊，呈现清透浮动质感。
- 🎨 **可自定义主题 (Theme System)**
  - 内置多套主题，并可在设置页新建、保存、删除自定义配色（支持纯色或自定义对角渐变角度与色值间距）。
  - 主题选择本地持久化且同步读取，冷启动第一帧即应用所选配色，不闪烁默认色。
- 👆 **极简手势与纯粹视觉 (Gesture-Driven & Minimalist UI)**
  - 取消冗余操作按钮与解释性副标题，全站界面回归纯粹（仅保留核心内容与底部四栏导航标签）。
  - **长按卡片**：直接唤起编辑/修改弹窗。
  - **左滑卡片**：iOS 风格停驻式滑动（`anchoredDraggable`），滑到露出红色删除按钮即停，配合稳定列表 `key` 自动复位，触发安全二次确认弹窗（`确认删除`）。
- 💑 **多情侣独立空间与自助配对 (Multi-Couple Space Isolation)**
  - **邀请码准入注册**：凭管理员生成的邀请码注册账号，自动初始化独立单人厨房空间；配对前也可独立维护菜单、纪念日与美食票根。
  - **配对码自助绑定与数据合并**：在设置页一键复制本人配对码或输入对方配对码完成绑定，双方分类、菜品、订单、小纸条、纪念日与美食票根自动合并至同一情侣空间双人共管。
  - **预设保护与超管跨空间视角**：对系统预设核心账号（“熙熙”与“哥哥”）启用防误删与权限保护；超级管理员（`super_admin`）支持切换任意情侣空间视角并管理全局用户与注册邀请码。

---

## 📱 核心功能模块

| 模块 | 功能描述 |
| :--- | :--- |
| 🔐 **登录与注册 (Auth)** | 账号密码登录、凭邀请码注册（自动创建专属空间）、密码重置 |
| 🍽️ **点单 (Kitchen)** | 菜品分类切换、300ms 防抖无跳动搜索框、购物车 (`CartDialog`)、指定伴侣召唤接单与下拉刷新 |
| 📋 **订单 (Orders)** | 状态筛选（`全部` / `等喂食` / `做饭中` / `已享用`）、接单/拒单/完成烹饪生命周期管理与订单详情 |
| 💞 **日记 (Meetup Diary)** | 记录每次见面与分别：哪天见面 / 哪天走，自动推算第 N 次相见与第 M 次分别、相伴天数与间隔天数；上半区文字 + 下半区 1~2 张拍立得合照；长按卡片可选择编辑或删除 |
| 📢 **发现 (Discover)** | **公告栏**（空间公告独立展示）、**小纸条**（双人冰箱贴留言板支持置顶 + 整行全宽纪念日/倒计时卡片）与 **美食日记**（上传美食照交互裁切 + 5 款旅行票根模板生成 + 双人评论回复互动） |
| 👤 **我的 (Mine)** | 极简个人资料卡片（长按修改昵称与头像）、超管空间切换横幅（仅 `super_admin` 可见）、设置入口与退出登录 |
| 🛠️ **设置 (Settings)** | 极简伴侣卡片（顶栏展示 `伴侣：昵称` 或 `伴侣：无`，未绑定点击 `去绑定` 可复制本人配对码或输入对方配对码）、厨房与菜单管理 / 超管控制台入口、主题切换与自定义配色 |
| ⚙️ **厨房管理 / 超管控制台 (Admin)** | 普通成员共管本空间的 **分类 / 菜品 / 订单 / 公告** 4 大模块；超级管理员额外拥有 **用户 / 邀请码** 模块与跨空间管理能力 |

---

## 🛠️ 技术栈 (Technology Stack)

- **语言与构建**：Kotlin 2.0.21 + Android Gradle Plugin 8.13.0（JVM Target 17）
- **UI 框架**：Jetpack Compose（BOM 2025.05.00）+ Material 3
- **渲染引擎**：自定义 `GlassTokens` / `GlassMesh` Canvas 毛玻璃渲染 + `ThemeStore` 主题持久化
- **手势管理**：Compose `anchoredDraggable`（停驻式左滑删除）+ `combinedClickable`（长按编辑）
- **架构**：MVVM（`KitchenViewModel` + `DiscoverViewModel`）+ `StateFlow`
- **网络与图片**：Retrofit 2.11.0 + OkHttp 4.12.0 + Gson + Coil Compose 2.7.0
- **本地安全存储**：`EncryptedSharedPreferences`（AES256-GCM 保存登录令牌）+ DataStore Preferences 1.1.1
- **依赖注入**：Hilt / Dagger 2.52
- **导航与测试**：Navigation Compose 2.9.8 + JUnit 4 / Robolectric 4.14.1 导航单元测试

---

## 🚀 项目构建与运行 (Build & Run)

### 环境要求
- **Android Studio**：Ladybug (2024.2.1) 或更高版本
- **JDK Version**：JDK 17 或 JDK 21
- **Compile SDK**：36
- **Target SDK**：36
- **Min SDK**：24

### 常用 Gradle 命令

```bash
# 1. 运行单元测试
.\gradlew.bat testDebugUnitTest

# 2. 编译并生成调试包 (Debug APK)
.\gradlew.bat assembleDebug

# 3. 编译并生成正式安装包 (Release APK)
.\gradlew.bat assembleRelease
```

*打包产物位置：*
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`
- 历史正式安装包可在 [GitHub Releases](https://github.com/onllovtxy/yushangfang/releases) 下载。

### 发版约定

**每次打包都要递增版本号**，两个都要改（`app/build.gradle` 的 `defaultConfig`）：

- `versionCode`：**必须**递增（每次打包固定 +1），否则手机会拒绝覆盖安装。
- `versionName`：**按本次新增/优化的功能条数递增第三段**——改了 3 个功能就 `+0.0.3`（如 `1.5.1` → `1.5.4`），改 1 个就 `+0.0.1`。不要几个不同的包顶着同一个版本号。

当前版本：**1.5.3**（versionCode 21）

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
│   ├── data/                             # 数据层
│   │   ├── Models.kt                     #   User / Dish / Order / CoupleSpace / InviteCodeItem / CoupleNote / Anniversary / FoodDiary 等数据模型
│   │   ├── ApiService.kt                 #   Retrofit 接口定义
│   │   ├── ApiClient.kt                  #   OkHttp / Retrofit 装配与 Token 拦截
│   │   ├── KitchenRepository.kt          #   统一数据入口，负责拆包与错误抛出
│   │   ├── SessionStore.kt               #   加密存储登录态与超管切换空间上下文
│   │   └── BackendConfig.kt              #   后端地址规范化与图片 URL 解析
│   ├── di/
│   │   └── AppModule.kt                  # Hilt 依赖注入
│   ├── ui/
│   │   ├── designsystem/
│   │   │   ├── theme/                    # 毛玻璃设计系统 (GlassTokens / GlassMesh / GlassTheme / ThemeStore / 自定义主题)
│   │   │   └── motion/                   # 动效时长与缓动，含"减少动效"适配
│   │   ├── components/
│   │   │   ├── CommonComponents.kt       # 公共毛玻璃弹窗、左滑删除 (SwipeRevealBox)、超管空间切换横幅、图片上传与看图器
│   │   │   ├── ImageCropperDialog.kt     # 交互式图片裁切器（缩放/平移/旋转/4种票根画幅比例）
│   │   │   └── TicketStubCard.kt         # 5款旅行/美食票根模板 Canvas 渲染器
│   │   ├── screens/                      # 按功能拆分的页面视图
│   │   │   ├── LoginScreen.kt            #   登录 / 邀请码注册 / 重置密码
│   │   │   ├── KitchenScreen.kt          #   菜单浏览、搜索、购物车与指定伴侣下单
│   │   │   ├── OrdersScreen.kt           #   订单列表、状态流转与订单详情
│   │   │   ├── DiscoverScreen.kt         #   发现页（公告栏 / 小纸条与纪念日 / 美食票根日记）
│   │   │   ├── TicketCreatorDialog.kt    #   票根制作与模板预览工作台
│   │   │   └── MineScreen.kt             #   极简个人主页、超管空间横幅与个人资料编辑
│   │   ├── XixiKitchenApp.kt             # 应用导航壳与全局反馈协调
│   │   ├── AdminScreens.kt               # 后台管理模块视图 (分类 / 菜品 / 订单 / 公告 / 用户 / 邀请码)
│   │   ├── SettingsScreen.kt             # 设置页（伴侣绑定卡片、管理入口与主题选择）
│   │   ├── CustomThemeDialog.kt          # 自定义主题编辑器
│   │   ├── KitchenViewModel.kt           # 主业务 ViewModel (菜单 / 订单 / 空间配对 / 后台)
│   │   ├── DiscoverViewModel.kt          # 发现页 ViewModel (公告 / 小纸条 / 纪念日 / 美食票根)
│   │   ├── ErrorMessageResolver.kt       # 统一网络与业务错误解析
│   │   ├── UploadFileNames.kt            # 图片读取、EXIF 方向校正与压缩
│   │   ├── FoodEmojis.kt                 # 无图菜品的 emoji 兜底头像
│   │   └── UiModifiers.kt                # 无涟漪按压等自定义 Modifier
│   ├── XixiApplication.kt                # Hilt Application 入口
│   └── MainActivity.kt                   # 应用单 Activity 入口
├── app/src/test/
│   └── java/com/xixikitchen/jetpack/ui/
│       └── TopLevelNavigationTest.kt     # 一级导航策略单元测试
├── build.gradle                          # 项目根 Gradle 配置
└── README.md                             # 项目说明文档
```

---

## 🔗 后端服务

服务端为独立仓库 `couple-menu-server`（Spring Boot 3.2.5 + Kotlin + MySQL + MyBatis-Plus + Flyway），支持多情侣 `couple_id` 租户级隔离、邀请码注册与伴侣配对数据合并，线上地址 `https://bbxy.art/`。

---

## 💖 致谢与说明

本项目专为情侣厨房点单打造，带来有温度、有仪式感的生活体验！
