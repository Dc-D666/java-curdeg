import os
import time
import hashlib

# 安装第三方库
def ip():
    B = time.time()
    os.system("pip config set global.index-url https://mirrors.bfsu.edu.cn/pypi/web/simple/")
    # os.system("pip install --upgrade pip")
    os.system("pip install selenium")
    os.system("pip install openpyxl")
    os.system("pip install you-get")
    os.system("pip install urllib3")
    os.system("pip install bs4")
    os.system("pip install numpy")
    os.system("pip install pyaudio")
    os.system("pip install requests")
    os.system("pip install pillow")
    os.system('pip install pyinstaller')
    E = time.time()
    print("Runtime:", str(E - B))
ip()
