#!/usr/bin/env python3
import os
import subprocess
import sys

def check_command(cmd):
    """检查命令是否可用"""
    try:
        subprocess.run(['which', cmd], capture_output=True, check=True)
        return True
    except subprocess.CalledProcessError:
        return False

def convert_with_ffmpeg(input_path, output_path, size):
    """使用ffmpeg转换图片"""
    cmd = [
        'ffmpeg', '-i', input_path,
        '-vf', f'scale={size}:{size}:force_original_aspect_ratio=increase,crop={size}:{size}',
        '-y', output_path
    ]
    try:
        subprocess.run(cmd, capture_output=True, check=True)
        print(f"✓ 生成: {output_path} ({size}x{size})")
        return True
    except subprocess.CalledProcessError as e:
        print(f"✗ 生成失败 {output_path}: {e.stderr.decode()[:100]}")
        return False

def main():
    # 图片路径
    source_img = '/mnt/d/Android/new_project/words/picture/37ebf1185c967470d592eb22ad719352.jpg'
    res_dir = '/mnt/d/Android/new_project/words/app/src/main/res'
    
    # 检查源图片是否存在
    if not os.path.exists(source_img):
        print(f"错误: 源图片不存在: {source_img}")
        sys.exit(1)
    
    # 检查ffmpeg是否可用
    if not check_command('ffmpeg'):
        print("错误: ffmpeg未安装，无法处理图片")
        print("请安装: sudo apt-get install ffmpeg")
        sys.exit(1)
    
    # 不同密度的图标尺寸
    sizes = {
        'mdpi': 48,
        'hdpi': 72,
        'xhdpi': 96,
        'xxhdpi': 144,
        'xxxhdpi': 192
    }
    
    print("开始生成应用图标...")
    print(f"源图片: {source_img}")
    print(f"输出目录: {res_dir}")
    print("-" * 50)
    
    # 为每个密度生成图标
    success_count = 0
    for density, size in sizes.items():
        output_dir = os.path.join(res_dir, f'mipmap-{density}')
        os.makedirs(output_dir, exist_ok=True)
        
        # 生成方形图标
        square_path = os.path.join(output_dir, 'ic_launcher.webp')
        if convert_with_ffmpeg(source_img, square_path, size):
            success_count += 1
        
        # 生成圆形图标（与方形相同）
        round_path = os.path.join(output_dir, 'ic_launcher_round.webp')
        if os.path.exists(square_path):
            # 复制方形图标作为圆形图标
            with open(square_path, 'rb') as src, open(round_path, 'wb') as dst:
                dst.write(src.read())
            print(f"✓ 复制: {round_path}")
            success_count += 1
    
    print("-" * 50)
    total_files = len(sizes) * 2  # 每个密度2个文件
    print(f"完成! 成功生成 {success_count}/{total_files} 个图标文件")
    
    if success_count == total_files:
        print("✅ 所有图标已成功生成!")
    else:
        print("⚠️  部分图标生成失败")

if __name__ == '__main__':
    main()