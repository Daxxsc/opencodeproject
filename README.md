# OpenCode Project - 考研单词学习应用

GitHub repository for OpenCode projects

## 📱 项目简介
这是一个基于Android的考研高频词汇学习应用，采用科学记忆算法（1-2-4-7天间隔重复）帮助用户高效记忆单词。

## ✨ 核心功能

### 🎯 科学记忆算法
- 1天、2天、4天、7天间隔重复
- 4次复习完成后标记为"认识"
- 复习失败的单词重新从第1阶段开始

### 📊 三大界面
1. **学习界面** - 新单词学习
2. **复习界面** - 按计划复习
3. **概览界面** - 单词状态管理

### 🗄️ 数据管理
- Room数据库存储
- 20个考研高频词汇初始数据
- 单词状态跟踪（认识/不认识/复习中/隐藏）

## 🚀 快速开始

### 环境要求
- Android Studio
- Java 11+
- Android SDK

### 构建步骤
```bash
# 克隆项目
git clone https://github.com/Daxxsc/opencodeproject.git

# 打开Android Studio
# 导入项目
# 点击 Build → Make Project
```

## 📁 项目结构
```
app/src/main/
├── java/com/example/words/
│   ├── data/           # 数据层
│   ├── viewmodel/      # ViewModel
│   ├── ui/            # UI界面
│   └── MainActivity.kt # 主Activity
└── res/               # 资源文件
```

## 🔧 技术栈
- **语言**: Kotlin + Java
- **架构**: MVVM + Repository
- **数据库**: Room
- **构建**: Gradle + AGP

## 📄 详细文档
更多详细信息请查看 [PROJECT_DOCS.md](PROJECT_DOCS.md)

## 📞 联系
如有问题，请提交Issue或联系项目维护者。
