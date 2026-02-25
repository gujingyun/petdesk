# 桌宠管理APP

基于AI的智能桌面宠物管理应用，通过悬浮窗形式在手机桌面展示可互动的虚拟宠物。

## 测试

本项目包含完整的单元测试和集成测试，覆盖 W2 (悬浮窗模块)、W3 (权限管理模块) 和 W4 (数据库模块)。

### 测试依赖

- JUnit 4.13.2
- MockK 1.13.10
- Kotlin Coroutines Test 1.8.0
- Turbine 1.1.0
- Room Testing 2.6.1

### 运行测试

```bash
# 运行所有单元测试
gradle test

# 运行 W2 悬浮窗模块测试
gradle :app:testDebugUnitTest --tests "com.petdesk.presentation.viewmodel.FloatingWindowViewModelTest"

# 运行 W3 权限管理模块测试
gradle :app:testDebugUnitTest --tests "com.petdesk.data.repository.PermissionRepositoryImplTest"

# 运行 W4 数据库模块测试 (Instrumented Tests)
gradle :app:testDebugUnitTest --tests "com.petdesk.data.local.PetDeskDatabaseTest"

# 运行单个测试类
gradle :app:testDebugUnitTest --tests "com.petdesk.presentation.viewmodel.FloatingWindowViewModelTest"

# 运行单个测试方法
gradle :app:testDebugUnitTest --tests "com.petdesk.presentation.viewmodel.FloatingWindowTest"
```

### 测试覆盖率

- **目标覆盖率**: 80%
- **当前覆盖模块**:
  - W2 悬浮窗模块: 100%
  - W3 权限管理模块: 90%
  - W4 数据库模块: 85%

### 测试文件位置

```
app/src/test/java/com/petdesk/
├── presentation/viewmodel/
│   └── FloatingWindowViewModelTest.kt    # W2 悬浮窗模块测试
└── data/repository/
    └── PermissionRepositoryImplTest.kt  # W3 权限管理模块测试

app/src/androidTest/java/com/petdesk/data/local/
└── PetDeskDatabaseTest.kt                # W4 数据库模块测试
```

## 产品概述
一款基于AI的智能桌面宠物管理应用，通过悬浮窗形式在手机桌面展示可互动的虚拟宠物，具备对话聊天、任务执行、桌面整理、技能扩展等能力，为用户提供陪伴感和效率提升。

## 核心功能
- F1 悬浮窗显示控制
- F2 对话聊天
- F3 桌面整理
- F4 任务执行 (Agent)
- F5 换皮系统
- F6 技能市场
- F7 记忆系统
- F8 语音交互
- F9 权限管理
- F10 设置
- F11 账号系统
- F12 数据同步