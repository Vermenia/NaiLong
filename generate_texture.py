from PIL import Image, ImageDraw

img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# 主背景 (176x133) - 深灰色背景
draw.rectangle([0, 0, 175, 132], fill=(198, 198, 198, 255))

# 内部面板背景
draw.rectangle([7, 17, 168, 125], fill=(139, 139, 139, 255))

# 名称输入框背景 - 白色
draw.rectangle([8, 4, 167, 15], fill=(0, 0, 0, 255))
draw.rectangle([9, 5, 166, 14], fill=(255, 255, 255, 255))

# 奶龙物品栏标题区域
draw.rectangle([60, 18, 133, 28], fill=(120, 120, 120, 255))

# 绘制9个物品格子（3x3）
for row in range(3):
    for col in range(3):
        x = 62 + col * 18
        y = 32 + row * 18
        # 格子外框
        draw.rectangle([x-1, y-1, x+17, y+17], fill=(55, 55, 55, 255))
        # 格子内部
        draw.rectangle([x, y, x+16, y+16], fill=(139, 139, 139, 255))

# 玩家背包区域 (3x9)
for row in range(3):
    for col in range(9):
        x = 8 + col * 18
        y = 51 + row * 18
        draw.rectangle([x-1, y-1, x+17, y+17], fill=(55, 55, 55, 255))
        draw.rectangle([x, y, x+16, y+16], fill=(139, 139, 139, 255))

# 快捷栏 (1x9)
for col in range(9):
    x = 8 + col * 18
    y = 109
    draw.rectangle([x-1, y-1, x+17, y+17], fill=(55, 55, 55, 255))
    draw.rectangle([x, y, x+16, y+16], fill=(139, 139, 139, 255))

img.save('nailong_inventory.png')
print("纹理文件已生成: nailong_inventory.png")
