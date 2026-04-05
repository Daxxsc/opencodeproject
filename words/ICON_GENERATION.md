# 应用图标生成指南

## 当前状态
✅ 已成功复制first260404项目的应用图标到当前项目。

## 使用的图片
- 源图片: `picture/37ebf1185c967470d592eb22ad719352.jpg` (954x882像素)
- 当前显示: first260404项目的头像图标

## 生成真实图标的步骤

### 方法1：使用Android Studio（推荐）
1. 打开Android Studio
2. 打开项目: `D:\Android\new_project\words`
3. 找到目录: `app/src/main/res/`
4. 右键点击 `mipmap` 目录 → **New** → **Image Asset**
5. 选择图片: `picture/37ebf1185c967470d592eb22ad719352.jpg`
6. 图标类型: Launcher Icons (Adaptive and Legacy)
7. 点击 **Next** → **Finish**

### 方法2：使用命令行工具（需要安装工具）

#### 安装所需工具：
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install -y imagemagick webp

# 或安装Python库
pip install Pillow
```

#### 使用提供的生成脚本：
```bash
# 运行生成脚本
python3 generate_icons.py
```

### 方法3：手动生成各尺寸图标

需要的图标尺寸：
- **mdpi**: 48x48像素
- **hdpi**: 72x72像素  
- **xhdpi**: 96x96像素
- **xxhdpi**: 144x144像素
- **xxxhdpi**: 192x192像素

生成命令示例（使用ImageMagick）：
```bash
# 转换并调整尺寸
convert picture/37ebf1185c967470d592eb22ad719352.jpg \
  -resize 192x192^ \
  -gravity center \
  -extent 192x192 \
  -quality 90 \
  app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp

# 复制为圆形版本
cp app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp \
   app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp
```

## 构建和测试
```bash
# 清理构建
./gradlew clean

# 构建APK
./gradlew assembleDebug

# APK位置
app/build/outputs/apk/debug/app-debug.apk
```

## 注意事项
1. 已禁用自适应图标系统，使用传统图标
2. 圆形图标与方形图标相同，系统会自动应用圆形蒙版
3. 安装新APK前建议卸载旧版本
4. 如果图标不显示，尝试清除应用缓存或重启设备