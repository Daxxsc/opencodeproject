#!/bin/bash

echo "=== 环境检查脚本 ==="
echo "检查时间: $(date)"
echo ""

echo "1. 检查Java环境..."
if command -v java &> /dev/null; then
    echo "✓ Java已安装: $(java -version 2>&1 | head -1)"
    if [ -n "$JAVA_HOME" ]; then
        echo "✓ JAVA_HOME已设置: $JAVA_HOME"
    else
        echo "⚠ JAVA_HOME未设置"
    fi
else
    echo "✗ Java未安装"
    echo "建议: sudo apt install openjdk-11-jdk"
fi
echo ""

echo "2. 检查Gradle..."
if [ -f "gradlew" ]; then
    echo "✓ Gradle wrapper存在"
    chmod +x gradlew
else
    echo "✗ Gradle wrapper不存在"
fi
echo ""

echo "3. 检查网络连接..."
if ping -c 1 mirrors.cloud.tencent.com &> /dev/null; then
    echo "✓ 可以连接到镜像源"
else
    echo "✗ 网络连接问题"
    echo "建议: 检查网络或使用代理"
fi
echo ""

echo "4. 检查项目文件..."
if [ -f "app/build.gradle.kts" ]; then
    echo "✓ 项目配置文件存在"
    
    # 检查关键文件
    files=(
        "app/src/main/java/com/example/words/MainActivity.kt"
        "app/src/main/java/com/example/words/data/Word.java"
        "app/src/main/java/com/example/words/ui/learning/LearningFragment.kt"
        "app/src/main/res/layout/activity_main.xml"
    )
    
    missing=0
    for file in "${files[@]}"; do
        if [ -f "$file" ]; then
            echo "  ✓ $file"
        else
            echo "  ✗ $file (缺失)"
            missing=$((missing+1))
        fi
    done
    
    if [ $missing -eq 0 ]; then
        echo "✓ 所有关键文件都存在"
    else
        echo "⚠ 缺失 $missing 个关键文件"
    fi
else
    echo "✗ 项目配置文件缺失"
fi
echo ""

echo "5. 构建建议..."
echo "如果Java已安装但JAVA_HOME未设置，请运行:"
echo "  export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64"
echo "  export PATH=\$JAVA_HOME/bin:\$PATH"
echo ""
echo "然后尝试构建:"
echo "  ./gradlew build --offline  # 离线模式"
echo "  ./gradlew assembleDebug    # 构建调试APK"
echo ""

echo "6. 备用方案..."
echo "如果无法解决环境问题，可以:"
echo "1. 在Windows上使用Android Studio打开项目"
echo "2. 使用Docker容器构建"
echo "3. 手动下载Gradle并配置本地路径"
echo ""

echo "=== 检查完成 ==="