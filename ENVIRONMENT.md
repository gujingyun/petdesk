# PetDesk 开发环境配置

## 基础环境要求

### 操作系统
- Windows 10/11
- macOS 10.14+
- Linux (Ubuntu 18.04+)

### JDK (Java Development Kit)
- **版本**: JDK 17 (JDK 17.0.18 Zulu)
- **路径示例**:
  - Windows: `C:\Program Files\Zulu\zulu-17`
  - macOS/Linux: `/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home`

### Gradle
- **版本**: Gradle 8.9
- **Wrapper**: 已包含在项目中 (`gradlew`)

### Android SDK
- **Compile SDK**: 34
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Build Tools**: 34.0.0

---

## 开发工具

### IDE
- **推荐**: Android Studio Jellyfish (2024.1.1) 或更新版本
- **可选**: IntelliJ IDEA 2024.1+ 配合 Android 插件

### Kotlin
- **版本**: Kotlin 2.0.0
- **Compose 编译器版本**: 2.0.0

---

## 项目依赖版本

### 核心框架
| 依赖 | 版本 |
|------|------|
| AndroidX Core KTX | 1.13.1 |
| AndroidX Lifecycle | 2.8.4 |
| AndroidX Activity Compose | 1.9.1 |
| Jetpack Compose BOM | 2024.06.00 |
| Jetpack Compose UI | (BOM 管理) |
| Jetpack Compose Material3 | (BOM 管理) |
| Material Icons Extended | (BOM 管理) |

### 依赖注入
| 依赖 | 版本 |
|------|------|
| Hilt Android | 2.51 |
| Hilt Compiler | 2.51 |
| Hilt Navigation Compose | 1.2.0 |

### 网络
| 依赖 | 版本 |
|------|------|
| Retrofit | 2.11.0 |
| Retrofit Converter Gson | 2.11.0 |
| OkHttp | 4.12.0 |

### 数据库
| 依赖 | 版本 |
|------|------|
| Room Runtime | 2.6.1 |
| Room KTX | 2.6.1 |
| Room Compiler | 2.6.1 |

### 异步
| 依赖 | 版本 |
|------|------|
| Kotlinx Coroutines Core | 1.8.0 |
| Kotlinx Coroutines Android | 1.8.0 |

### 图像与动画
| 依赖 | 版本 |
|------|------|
| Glide | 4.16.0 |
| Glide Compiler | 4.16.0 |
| Lottie Compose | 6.0.0 |

### 测试
| 依赖 | 版本 |
|------|------|
| JUnit | 4.13.2 |
| MockK | 1.13.10 |
| Kotlinx Coroutines Test | 1.8.0 |
| Turbine | 1.1.0 |
| AndroidX Test JUnit | 1.2.1 |
| Espresso Core | 3.6.1 |
| Room Testing | 2.6.1 |

---

## 构建配置

### Gradle 配置
- **Android Gradle Plugin**: 8.5.0
- **Kotlin Gradle Plugin**: 2.0.0
- **Hilt Gradle Plugin**: 2.51

### Java 兼容性
```groovy
sourceCompatibility = JavaVersion.VERSION_17
targetCompatibility = JavaVersion.VERSION_17
jvmTarget = '17'
```

---

## 构建命令

### 常用命令
```bash
# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 清理构建
./gradlew clean

# 运行单元测试
./gradlew test

# 运行特定测试类
./gradlew :app:testDebugUnitTest --tests "com.petdesk.domain.usecase.*"

# 运行单个测试
./gradlew :app:testDebugUnitTest --tests "com.petdesk.domain.usecase.GetPetStateUseCaseTest"

# 查看依赖树
./gradlew dependencies

# 诊断构建
./gradlew buildEnvironment
```

### 使用 Java 17 构建
```bash
# Windows
set JAVA_HOME=C:\Program Files\Zulu\zulu-17
./gradlew assembleDebug

# macOS/Linux
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home
./gradlew assembleDebug
```

---

## 项目结构

```
petdesk/
├── app/                          # 应用模块
│   ├── src/main/
│   │   ├── java/com/petdesk/
│   │   │   ├── presentation/    # UI 层 (Compose, ViewModels)
│   │   │   ├── domain/           # 业务逻辑层
│   │   │   ├── data/             # 数据层
│   │   │   ├── di/               # Hilt 依赖注入模块
│   │   │   ├── service/          # Android 系统服务
│   │   │   └── utils/            # 工具类
│   │   └── res/                   # 资源文件
│   └── build.gradle
├── build.gradle                   # 根构建文件
├── settings.gradle                # 项目设置
├── gradle/                        # Gradle wrapper
├── gradlew                        # Gradle wrapper 脚本
└── .gitignore                    # Git 忽略文件
```

---

## 架构说明

项目采用 **MVVM + Clean Architecture** 架构：

- **Presentation Layer**: UI 层，使用 Jetpack Compose + ViewModel
- **Domain Layer**: 业务逻辑层，包含 Use Case、Repository 接口、Domain Model
- **Data Layer**: 数据层，包含 Repository 实现、Data Source

---

## 注意事项

1. **Kotlin 2.0**: 项目使用 Kotlin 2.0，需确保 IDE 支持
2. **KAPT**: KAPT 暂不支持 Kotlin 2.0 语言版本，会回退到 1.9
3. **Hilt**: 使用 KAPT 进行注解处理
4. **Compose**: 使用新的 Compose 编译器插件 (`org.jetbrains.kotlin.plugin.compose`)

---

## 环境变量 (可选)

在 `~/.bashrc` 或 `~/.zshrc` 中添加：

```bash
# Java
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home

# Android SDK
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools
```

---

## 常见问题

### Q: 构建失败提示 "Java 8 JVM"
A: 需要使用 Java 11 或更高版本，设置 `JAVA_HOME` 环境变量指向 JDK 17

### Q: KAPT 警告
A: KAPT 暂不支持 Kotlin 2.0，会显示回退警告，不影响构建

### Q: Gradle 内存不足
A: 可在 `gradle.properties` 中添加：
```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```
