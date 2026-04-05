# 考研单词学习应用 - 详细文档

## 📖 项目概述
这是一个完整的Android考研单词学习应用，基于科学的1-2-4-7天间隔重复记忆算法。应用包含学习、复习、概览三个主要界面，帮助用户高效记忆考研高频词汇。

## ✅ 已完成功能

### 🏗️ 核心架构
- **Room数据库配置**：Java实体 + Kotlin DAO混合编程
- **MVVM架构**：Repository + ViewModel + Fragment清晰分层
- **底部导航**：学习、复习、概览三个主要界面无缝切换

### 💾 数据层实现
- **Word实体**：包含英文、中文、状态、复习阶段、隐藏状态等完整字段
- **WordDao**：提供完整的CRUD操作和复杂查询
- **WordRepository**：实现业务逻辑和复习算法
- **初始数据**：20个考研高频词汇（abandon - accuse）

### 🎨 UI界面设计

#### 1. 学习界面 (`LearningFragment`)
- 显示英文单词卡片
- 点击卡片显示中文翻译
- "认识"/"不认识"按钮进行学习反馈
- 学习进度实时显示

#### 2. 复习界面 (`ReviewFragment`)
- 基于1-2-4-7天复习算法自动调度
- 显示当前复习阶段和下次复习时间
- "记住了"/"忘记了"按钮进行复习反馈
- 复习进度可视化

#### 3. 概览界面 (`OverviewFragment`)
- 按状态分类显示所有单词
- 实时统计：认识、不认识、复习中、已隐藏数量
- 隐藏单词管理功能
- 单词详情查看和状态修改

### 🔄 复习算法实现

#### 算法逻辑
```kotlin
// 1-2-4-7天间隔重复
private fun calculateNextReviewDate(stage: Int): Date {
    val calendar = Calendar.getInstance()
    when (stage) {
        1 -> calendar.add(Calendar.DAY_OF_YEAR, 1)  // 1天后
        2 -> calendar.add(Calendar.DAY_OF_YEAR, 2)  // 2天后
        3 -> calendar.add(Calendar.DAY_OF_YEAR, 4)  // 4天后
        4 -> calendar.add(Calendar.DAY_OF_YEAR, 7)  // 7天后
    }
    return calendar.time
}
```

#### 状态流转
1. **不认识** → 标记为复习 → 进入第1阶段（1天后复习）
2. **第1阶段** → 记住 → 进入第2阶段（2天后复习）
3. **第2阶段** → 记住 → 进入第3阶段（4天后复习）
4. **第3阶段** → 记住 → 进入第4阶段（7天后复习）
5. **第4阶段** → 记住 → 标记为"认识"
6. **任何阶段忘记** → 重新从第1阶段开始

## ⚠️ 当前问题与解决方案

### 构建环境问题
项目在WSL2环境中无法构建，原因：
1. **Java环境缺失**：系统未安装Java
2. **网络连接问题**：无法下载Gradle依赖

### 解决方案

#### 方案1：在Windows主机上构建（推荐）
1. 安装Android Studio（如果尚未安装）
2. 打开项目文件夹 `D:\Android\new_project\words`
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

## 📋 关键文件说明

### 数据模型 (`data/Word.java`)
```java
@Entity(tableName = "words")
public class Word {
    @PrimaryKey(autoGenerate = true)
    public long id = 0;
    
    public String english;
    public String chinese;
    public String example = "";
    public String phonetic = "";
    public String status = "UNKNOWN";
    public int reviewStage = 0;
    public Date nextReviewDate;
    public Date lastReviewedDate;
    public boolean isHidden = false;
    public Date createdAt = new Date();
    
    // Getter/Setter for Kotlin compatibility
    public boolean getIsHidden() { return isHidden; }
    public void setIsHidden(boolean hidden) { isHidden = hidden; }
}
```

### 数据库配置 (`data/WordDatabase.java`)
```java
@Database(entities = {Word.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class WordDatabase extends RoomDatabase {
    public abstract WordDao wordDao();
    
    // 单例模式，线程安全
    private static volatile WordDatabase INSTANCE;
    
    public static WordDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WordDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        WordDatabase.class,
                        "word_database"
                    ).allowMainThreadQueries()  // 调试用，生产环境应移除
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
```

### 类型转换器 (`data/Converters.java`)
```java
public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
```

## 🎯 下一步开发建议

### 短期目标（1-2周）
1. **解决构建环境问题**：确保项目能在Windows/Android Studio中正常构建
2. **功能测试**：测试所有界面的基本功能
3. **扩展词汇库**：添加更多考研高频词汇（500+）

### 中期目标（1-2个月）
1. **单词发音功能**：集成TTS或预录音频
2. **复习提醒通知**：定时提醒用户复习
3. **学习统计报表**：可视化学习进度和效果
4. **多单词书切换**：支持不同类别的单词书

### 长期目标（3-6个月）
1. **云同步功能**：用户数据云端备份和同步
2. **社区分享功能**：用户分享学习心得和单词表
3. **AI智能推荐**：基于学习情况智能推荐复习内容
4. **跨平台版本**：开发iOS和Web版本

## 🔧 技术细节

### 构建配置
- **AGP版本**：8.3.0
- **Gradle版本**：8.4
- **编译SDK**：34
- **最小SDK**：24（Android 7.0）
- **目标SDK**：34

### 依赖库
```gradle
dependencies {
    // AndroidX核心
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.10.0'
    
    // Room数据库
    implementation 'androidx.room:room-runtime:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    
    // ViewModel和LiveData
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
    
    // 测试
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
}
```

### 项目配置
- **Java兼容性**：Java 11
- **Kotlin代码风格**：官方标准
- **构建脚本**：Groovy DSL（与参考项目secondapp保持一致）

## 🐛 已知问题与解决方案

### 问题1：WSL2环境构建失败
**症状**：`Installed Build Tools revision XX.0.0 is corrupted`
**原因**：WSL2无法运行Windows的Build Tools可执行文件（.exe）
**解决方案**：在Windows环境中使用Android Studio构建

### 问题2：Java-Kotlin互操作性
**症状**：Kotlin代码无法访问Java类的boolean字段
**解决方案**：为Word类添加`getIsHidden()`和`setIsHidden()`方法

### 问题3：Room数据库类型转换
**症状**：`Cannot figure out how to save this field into database`
**解决方案**：添加`Converters.java`类型转换器并在数据库添加`@TypeConverters`

## 📞 技术支持

### 常见问题排查
1. **构建失败**：检查Java环境、Gradle版本、网络连接
2. **应用闪退**：查看Logcat日志，检查数据库初始化
3. **功能异常**：检查ViewModel状态管理和Fragment生命周期

### 调试建议
1. 启用`android:debuggable="true"`（已在AndroidManifest.xml中配置）
2. 查看Logcat输出，过滤`com.example.words`相关日志
3. 使用Android Studio的调试功能逐步执行

## 📄 许可证
本项目采用MIT许可证。详见LICENSE文件。

## 🤝 贡献指南
欢迎提交Issue和Pull Request来改进这个项目！

---

**项目状态**：✅ 功能完整，✅ 代码提交，✅ GitHub仓库同步  
**构建状态**：⚠️ 需在Windows/Android Studio环境中构建  
**应用状态**：📱 完整功能，待测试运行