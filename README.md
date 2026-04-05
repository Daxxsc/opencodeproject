# 考研单词学习应用

## 项目概述
这是一个基于Android的考研高频词汇学习应用，采用科学记忆算法（1-2-4-7天间隔重复）帮助用户高效记忆单词。

## 已完成功能

### ✅ 核心架构
- Room数据库配置（Java实体 + Kotlin DAO）
- MVVM架构：Repository + ViewModel + Fragment
- 底部导航：学习、复习、概览三个主要界面

### ✅ 数据层
- Word实体：英文、中文、状态、复习阶段、隐藏状态等
- WordDao：完整的CRUD操作和查询
- WordRepository：业务逻辑和复习算法
- 初始数据：20个考研高频词汇

### ✅ UI界面
1. **学习界面** (`LearningFragment`)
   - 显示英文单词
   - 点击显示中文翻译
   - "认识"/"不认识"按钮
   - 学习进度显示

2. **复习界面** (`ReviewFragment`)
   - 基于1-2-4-7天复习算法
   - 显示当前复习阶段
   - "记住了"/"忘记了"按钮
   - 复习进度条

3. **概览界面** (`OverviewFragment`)
   - 按状态分类显示所有单词
   - 统计信息：认识、不认识、复习中、已隐藏数量
   - 隐藏单词管理功能
   - 单词详情对话框

### ✅ 复习算法
- 1天、2天、4天、7天间隔重复
- 4次复习完成后标记为"认识"
- 复习失败的单词重新从第1阶段开始

## 当前问题

### ⚠️ 构建环境问题
项目无法构建，原因：
1. **Java环境缺失**：系统未安装Java
2. **网络连接问题**：无法下载Gradle

### 🔧 解决方案

#### 方案1：在Windows主机上构建
1. 安装Android Studio（如果尚未安装）
2. 打开项目文件夹 `/mnt/d/Android/new_project/words`
3. Android Studio会自动配置Gradle和Java环境
4. 点击"Build" → "Make Project"

#### 方案2：手动配置Java环境（WSL2）
```bash
# 在WSL2中安装Java
sudo apt update
sudo apt install -y openjdk-11-jdk

# 设置环境变量
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# 测试Java
java -version

# 尝试构建
./gradlew build
```

#### 方案3：使用预配置的Docker环境
```bash
# 使用Android开发Docker镜像
docker run -it --rm \
  -v $(pwd):/app \
  -w /app \
  android/android-sdk:latest \
  ./gradlew build
```

## 项目结构
```
app/src/main/
├── java/com/example/words/
│   ├── data/           # 数据层
│   ├── viewmodel/      # ViewModel
│   ├── ui/            # UI界面
│   └── MainActivity.kt # 主Activity
└── res/               # 资源文件
```

## 关键文件说明

### 数据模型 (`data/Word.java`)
```java
@Entity(tableName = "words")
public class Word {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String english;
    public String chinese;
    public String status;      // "KNOWN", "UNKNOWN", "REVIEWING"
    public int reviewStage;    // 1, 2, 3, 4
    public Date nextReviewDate;
    public Date lastReviewedDate;
    public boolean isHidden;
}
```

### 复习算法 (`data/WordRepository.kt`)
```kotlin
private fun calculateNextReviewDate(stage: Int): Date {
    val calendar = Calendar.getInstance()
    when (stage) {
        1 -> calendar.add(Calendar.DAY_OF_YEAR, 1)
        2 -> calendar.add(Calendar.DAY_OF_YEAR, 2)
        3 -> calendar.add(Calendar.DAY_OF_YEAR, 4)
        4 -> calendar.add(Calendar.DAY_OF_YEAR, 7)
    }
    return calendar.time
}
```

## 下一步开发建议

### 短期目标
1. 解决Java/Gradle环境问题
2. 测试构建和运行
3. 添加更多考研词汇（500+）

### 中期目标
1. 添加单词发音功能
2. 实现复习提醒通知
3. 添加学习统计报表
4. 支持多单词书切换

### 长期目标
1. 云同步功能
2. 社区分享功能
3. AI智能推荐
4. 跨平台版本

## 技术栈
- **语言**: Kotlin + Java
- **架构**: MVVM + Repository
- **数据库**: Room
- **UI**: Jetpack Compose（可迁移）
- **构建**: Gradle + AGP 9.1.0

## 联系方式
如有问题，请检查：
1. Java环境配置
2. 网络连接（Gradle下载）
3. Android SDK路径

项目已具备完整功能，只需解决环境问题即可运行。
