# 桌宠管理APP技术架构文档

## 架构概述
本项目采用MVVM (Model-View-ViewModel) + Clean Architecture架构模式，确保代码的可维护性、可测试性和可扩展性。

## 架构分层

### 1. 表现层 (Presentation Layer)
- **职责**: 处理UI逻辑和用户交互
- **组件**: 
  - Activities/Fragments (使用Jetpack Compose)
  - ViewModels
  - UI状态管理
- **依赖**: 只依赖Domain层

### 2. 领域层 (Domain Layer)
- **职责**: 包含业务逻辑和用例
- **组件**:
  - Use Cases (Interactors)
  - Repository接口
  - 实体(Entities)
- **依赖**: 不依赖其他层，是纯Kotlin模块

### 3. 数据层 (Data Layer)
- **职责**: 处理数据获取和持久化
- **组件**:
  - Repository实现
  - 数据源(Data Sources)
    - Remote (API服务)
    - Local (Room数据库、SharedPreferences)
- **依赖**: 依赖Domain层的接口

## 技术栈

### 核心框架
- **语言**: Kotlin 2.0+
- **UI框架**: Jetpack Compose 1.6+
- **架构模式**: MVVM + Clean Architecture
- **依赖注入**: Hilt 2.51+

### 网络与数据
- **网络请求**: Retrofit 2.11+ + OkHttp 4.12+
- **本地数据库**: Room 2.6+
- **异步处理**: Kotlin Coroutines 1.8+ + Flow
- **图片加载**: Glide 4.16+
- **动画引擎**: Lottie 6.0+

### 其他组件
- **悬浮窗服务**: 自定义FloatingWindowService
- **无障碍服务**: DesktopAccessibilityService
- **权限管理**: 统一权限处理模块
- **LLM集成**: 通义千问Qwen-Max API
- **语音服务**: 讯飞听见(ASR) + 通义听悟(TTS)

## 包结构

```
com.petdesk
├── PetDeskApplication.kt          # 应用入口
├── di/                           # 依赖注入模块
├── presentation/                 # 表现层
│   ├── MainActivity.kt           # 主Activity
│   ├── PetDeskApp.kt             # 主Composable
│   ├── theme/                    # 主题相关
│   └── screens/                  # 各个屏幕
│       ├── home/                 # 主界面
│       ├── chat/                 # 对话界面
│       ├── skills/               # 技能市场
│       └── settings/             # 设置界面
├── domain/                       # 领域层
│   ├── model/                    # 实体模型
│   ├── repository/               # 仓库接口
│   └── usecase/                  # 用例
├── data/                         # 数据层
│   ├── repository/               # 仓库实现
│   ├── source/                   # 数据源
│   │   ├── remote/               # 远程数据源
│   │   └── local/                # 本地数据源
│   └── mapper/                   # 数据映射
├── service/                      # 系统服务
│   ├── FloatingWindowService.kt  # 悬浮窗服务
│   └── DesktopAccessibilityService.kt # 无障碍服务
└── utils/                        # 工具类
```

## 依赖注入
使用Hilt进行依赖注入，确保各层之间的解耦和可测试性。

## 状态管理
使用StateFlow和ViewModel进行UI状态管理，确保状态的一致性和可预测性。

## 错误处理
统一的错误处理机制，包括网络错误、权限错误和业务逻辑错误。

## 测试策略
- **单元测试**: 使用JUnit和MockK测试领域层和数据层
- **集成测试**: 测试Repository和UseCase的集成
- **UI测试**: 使用Compose Testing测试UI组件
- **E2E测试**: 使用Espresso进行端到端测试

## 安全考虑
- 本地敏感数据使用AES-256加密
- 所有网络请求使用HTTPS (TLS 1.3)
- API密钥存储在Android Keystore中
- 遵循权限最小化原则
- 符合GDPR和个人信息保护法要求