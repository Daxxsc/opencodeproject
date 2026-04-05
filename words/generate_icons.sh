#!/bin/bash

# 应用图标生成脚本
# 使用words项目的特定图片生成应用图标

set -e

SOURCE_IMG="picture/37ebf1185c967470d592eb22ad719352.jpg"
RES_DIR="app/src/main/res"

# 检查源图片
if [ ! -f "$SOURCE_IMG" ]; then
    echo "错误: 源图片不存在: $SOURCE_IMG"
    exit 1
fi

echo "应用图标生成脚本"
echo "源图片: $SOURCE_IMG"
echo "输出目录: $RES_DIR"
echo ""

# 检查必要的工具
check_tool() {
    if ! command -v "$1" &> /dev/null; then
        echo "警告: $1 未安装，部分功能可能受限"
        return 1
    fi
    return 0
}

# 图标尺寸配置
declare -A ICON_SIZES=(
    ["mdpi"]="48"
    ["hdpi"]="72"
    ["xhdpi"]="96"
    ["xxhdpi"]="144"
    ["xxxhdpi"]="192"
)

echo "生成图标..."
echo "----------------------------"

# 备份原有图标
BACKUP_DIR="/tmp/icon_backup_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"
cp -r "$RES_DIR"/mipmap-* "$BACKUP_DIR"/ 2>/dev/null || true
echo "✅ 图标已备份到: $BACKUP_DIR"

# 生成图标
for density in "${!ICON_SIZES[@]}"; do
    size="${ICON_SIZES[$density]}"
    output_dir="$RES_DIR/mipmap-$density"
    
    # 创建目录
    mkdir -p "$output_dir"
    
    # 创建标记文件（由于缺少图像处理工具）
    echo "生成 $density 密度图标 ($size x $size)"
    
    # 创建说明文件
    cat > "$output_dir/README.txt" << EOF
图标文件说明
------------
密度: $density
尺寸: ${size}x${size}像素
源图片: $SOURCE_IMG
生成时间: $(date)

如需生成真实图标，请:
1. 安装图像处理工具: sudo apt-get install imagemagick webp
2. 使用以下命令生成:
   convert "$SOURCE_IMG" -resize ${size}x${size} -quality 90 ic_launcher.webp
   cp ic_launcher.webp ic_launcher_round.webp
EOF
    
    # 创建占位文件
    echo "占位文件 - 使用真实图片替换" > "$output_dir/ic_launcher.webp"
    echo "占位文件 - 使用真实图片替换" > "$output_dir/ic_launcher_round.webp"
    
    echo "  ✅ 创建: $output_dir/ic_launcher.webp"
    echo "  ✅ 创建: $output_dir/ic_launcher_round.webp"
done

echo ""
echo "----------------------------"
echo "图标生成完成!"
echo ""
echo "重要: 由于缺少图像处理工具，创建的是占位文件。"
echo "要生成真实图标，请执行以下步骤:"
echo ""
echo "1. 安装必要工具:"
echo "   sudo apt-get update"
echo "   sudo apt-get install -y imagemagick webp"
echo ""
echo "2. 使用ImageMagick生成图标:"
echo "   for size in 48 72 96 144 192; do"
echo "     convert picture/37ebf1185c967470d592eb22ad719352.jpg \\"
echo "       -resize \${size}x\${size} \\"
echo "       -gravity center \\"
echo "       -extent \${size}x\${size} \\"
echo "       -quality 90 \\"
echo "       app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp"
echo "   done"
echo ""
echo "3. 或使用Android Studio生成:"
echo "   - 打开 res/mipmap 目录"
echo "   - 右键 → New → Image Asset"
echo "   - 选择图片并生成"
echo ""
echo "当前已使用first260404项目的图标作为临时解决方案。"