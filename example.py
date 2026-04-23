import requests
import json

# ================= 配置区域 =================
API_URL = "https://xplt.sdu.edu.cn:4000/v1/chat/completions"
API_KEY = "sk-oXWyxru5nwdf-iDN6a8sJw"
MODEL_NAME = "Ali-dashscope/Qwen3.5-Flash"  # <--- 关键修正：指定模型名

# 极简版审核提示词
SYSTEM_PROMPT = """身份：学生论坛合规审核员，仅输出指定JSON，无任何额外文字/注释/换行。
任务：审核帖子/评论，优先放过正常发言，边界模糊内容强制转人工。

违规分级：
- 严重违规(reject+serious)：违法犯罪、校园霸凌/人肉/隐私泄露、代考代写作弊、校园贷诈骗、极端歧视/煽动仇恨
- 一般违规(reject+normal)：脏话粗口、刷屏水帖、轻微引战、无关广告、不适内容
- 允许通过(pass+none)：学术讨论、校园吐槽、日常分享、合理诉求

特殊规则：识别谐音/缩写/暗语；匿名涉人内容从严；含图片/链接标注需人工复核；置信度<80%转人工。

输出格式：
{"audit_result":"pass|reject|manual","violation_level":"serious|normal|none","violation_type":"","violation_fragments":[],"suggestion":"","confidence":0-100,"remark":""}"""
# ===========================================


def moderate_content(content):
    headers = {
        "Authorization": f"Bearer {API_KEY}",
        "Content-Type": "application/json"
    }

    payload = {
        "model": MODEL_NAME,  # <--- 这里必须加上
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": content}
        ],
        "temperature": 0
    }

    try:
        response = requests.post(API_URL, headers=headers, json=payload, timeout=30)
        response.raise_for_status()
        ai_response = response.json()["choices"][0]["message"]["content"]
        return json.loads(ai_response)
    except Exception as e:
        return {"error": str(e), "raw": response.text if 'response' in locals() else ""}

if __name__ == "__main__":
    test_cases = [
        "代考高数，包过加QQ123456",          # 严重违规
        "这老师真傻逼，上课听不懂",             # 一般违规
        "三食堂糖醋里脊好咸，有同感吗？",       # 正常通过
        "出二手考研资料，有意私聊",             # 正常通过（需根据论坛规则调整）
        "匿名爆料：某室友偷东西...",            # 人工复核（匿名涉人）
    ]

    print("开始审核测试...\n")
    for content in test_cases:
        print(f"输入: {content}")
        print(f"结果: {json.dumps(moderate_content(content), ensure_ascii=False, indent=2)}\n")