#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
敏感词批量导入脚本
用法：
1. 修改数据库连接配置
2. 准备敏感词文件 sensitive_words.txt，格式：敏感词,等级
3. 运行脚本：python import_sensitive_words.py
"""

import mysql.connector
from mysql.connector import Error
import sys
import os

# 数据库连接配置 - 请根据实际情况修改
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'database': 'java_2_48',
    'user': 'root',
    'password': 'your_password',
    'charset': 'utf8mb4'
}

def load_sensitive_words(file_path):
    """从文件加载敏感词"""
    words = []
    if not os.path.exists(file_path):
        print(f"错误：文件 {file_path} 不存在！")
        return words
    
    with open(file_path, 'r', encoding='utf-8') as f:
        for line_num, line in enumerate(f, 1):
            line = line.strip()
            # 跳过空行和注释行
            if not line or line.startswith('#'):
                continue
            # 解析敏感词
            parts = line.split(',')
            if len(parts) >= 1:
                word = parts[0].strip()
                level = 1  # 默认普通违规
                if len(parts) >= 2:
                    try:
                        level = int(parts[1].strip())
                        if level not in [1, 2]:
                            print(f"警告：第 {line_num} 行等级无效，使用默认值 1")
                            level = 1
                    except ValueError:
                        print(f"警告：第 {line_num} 行等级格式错误，使用默认值 1")
                words.append((word, level))
    return words

def batch_insert_sensitive_words(words):
    """批量插入敏感词"""
    if not words:
        print("没有需要插入的敏感词")
        return 0
    
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        if connection.is_connected():
            cursor = connection.cursor()
            
            # 批量插入SQL
            insert_query = """
            INSERT IGNORE INTO bbs_sensitive_word (word, level)
            VALUES (%s, %s)
            """
            
            # 执行批量插入
            cursor.executemany(insert_query, words)
            connection.commit()
            
            inserted_count = cursor.rowcount
            print(f"成功插入 {inserted_count} 条敏感词！")
            
            cursor.close()
            connection.close()
            return inserted_count
            
    except Error as e:
        print(f"数据库错误：{e}")
        return 0

def main():
    file_path = os.path.join(os.path.dirname(__file__), 'sensitive_words.txt')
    
    print("=" * 60)
    print("敏感词批量导入工具")
    print("=" * 60)
    
    # 加载敏感词
    print(f"\n正在从 {file_path} 加载敏感词...")
    words = load_sensitive_words(file_path)
    
    if not words:
        print("未找到有效敏感词！")
        return
    
    print(f"加载到 {len(words)} 条敏感词")
    
    # 确认导入
    while True:
        choice = input("\n确认导入？(y/n): ").strip().lower()
        if choice in ['y', 'yes']:
            break
        elif choice in ['n', 'no']:
            print("已取消导入")
            return
    
    # 执行导入
    print("\n正在导入敏感词...")
    count = batch_insert_sensitive_words(words)
    
    if count > 0:
        print("\n导入完成！")
    else:
        print("\n导入失败！")

if __name__ == "__main__":
    main()
