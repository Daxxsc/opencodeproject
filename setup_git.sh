#!/bin/bash
echo "设置git仓库..."

# 移除可能存在的.git目录
rm -rf .git

# 初始化git仓库
git init --initial-branch=main

# 配置git
git config user.email "you@example.com"
git config user.name "Your Name"

# 添加文件
git add .

# 创建提交
git commit -m "完成考研单词学习应用开发

- 实现1-2-4-7天间隔重复算法
- 三个主要界面：学习、复习、概览
- Room数据库存储单词数据
- ViewModel管理UI状态
- 20个考研高频词汇初始数据
- 完整的复习流程和状态管理"

echo "Git仓库已初始化并提交"
echo "下一步：连接到GitHub远程仓库"
echo "1. 在GitHub上创建新仓库"
echo "2. 运行: git remote add origin https://github.com/你的用户名/仓库名.git"
echo "3. 运行: git push -u origin main"