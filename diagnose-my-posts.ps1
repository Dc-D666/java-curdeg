# diagnose-my-posts.ps1 - 诊断我的帖子分页问题

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   我的帖子分页诊断工具" -ForegroundColor Cyan
Write-Host "==========================================`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:22223"

# 1. 管理员登录
Write-Host "正在以 admin 用户登录..." -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "123456"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest -Uri "$baseUrl/api/auth/login" `
        -Method POST `
        -Body $loginBody `
        -ContentType "application/json"
    
    $loginContent = $loginResponse.Content | ConvertFrom-Json
    
    if ($loginContent.code -eq 200 -or $loginContent.code -eq 0) {
        $token = $loginContent.data.token
        Write-Host "✅ 登录成功！`n" -ForegroundColor Green
    } else {
        Write-Host "❌ 登录失败: $($loginContent.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ 登录请求失败: $_" -ForegroundColor Red
    exit 1
}

# 2. 获取第一页（pageNum=1, pageSize=10）
Write-Host "正在获取第1页帖子..." -ForegroundColor Yellow
try {
    $page1Response = Invoke-WebRequest -Uri "$baseUrl/api/bbs/user/me/posts?pageNum=1&pageSize=10" `
        -Method GET `
        -Headers @{"Authorization" = "Bearer $token"}
    
    $page1Content = $page1Response.Content | ConvertFrom-Json
    
    Write-Host "`n📄 第1页数据：" -ForegroundColor Cyan
    Write-Host "   • total: $($page1Content.data.total)" -ForegroundColor White
    Write-Host "   • totalPages: $($page1Content.data.totalPages)" -ForegroundColor White
    Write-Host "   • number: $($page1Content.data.number)" -ForegroundColor White
    Write-Host "   • size: $($page1Content.data.size)" -ForegroundColor White
    Write-Host "   • 实际帖子数: $($page1Content.data.content.Count)" -ForegroundColor White
    
    $total = $page1Content.data.total
    $totalPages = $page1Content.data.totalPages
    
    # 3. 如果有第二页，获取第二页
    if ($totalPages -gt 1) {
        Write-Host "`n正在获取第2页帖子..." -ForegroundColor Yellow
        $page2Response = Invoke-WebRequest -Uri "$baseUrl/api/bbs/user/me/posts?pageNum=2&pageSize=10" `
            -Method GET `
            -Headers @{"Authorization" = "Bearer $token"}
        
        $page2Content = $page2Response.Content | ConvertFrom-Json
        
        Write-Host "`n📄 第2页数据：" -ForegroundColor Cyan
        Write-Host "   • total: $($page2Content.data.total)" -ForegroundColor White
        Write-Host "   • 实际帖子数: $($page2Content.data.content.Count)" -ForegroundColor White
        
        $totalFromPage2 = $page2Content.data.total
        
        if ($total -ne $totalFromPage2) {
            Write-Host "`n⚠️  警告：第1页和第2页返回的total不一致！" -ForegroundColor Red
        }
    } else {
        Write-Host "`n⚠️  警告：只有1页数据！" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ 请求失败: $_" -ForegroundColor Red
}

Write-Host "`n按任意键退出..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
