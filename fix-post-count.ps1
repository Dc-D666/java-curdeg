# fix-post-count.ps1 - 发帖数数据一致性修复工具

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   发帖数数据一致性修复工具" -ForegroundColor Cyan
Write-Host "==========================================`n" -ForegroundColor Cyan

# 1. 管理员登录
Write-Host "正在以 admin 用户登录..." -ForegroundColor Yellow
$loginBody = @{
  username = "admin"
  password = "123456"
} | ConvertTo-Json

try {
  $loginResponse = Invoke-WebRequest -Uri "http://localhost:22223/api/auth/login" `
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

# 2. 调用修复 API
Write-Host "正在修复数据..." -ForegroundColor Yellow
try {
  $fixResponse = Invoke-WebRequest -Uri "http://localhost:22223/api/bbs/user/fix-post-count" `
    -Method POST `
    -Headers @{"Authorization" = "Bearer $token"}
  
  $fixContent = $fixResponse.Content | ConvertFrom-Json
  
  if ($fixContent.code -eq 200 -or $fixContent.code -eq 0) {
    Write-Host "`n✅ 修复成功！`n" -ForegroundColor Green
    Write-Host "📊 修复结果统计：" -ForegroundColor Cyan
    Write-Host "   • 总用户数: $($fixContent.data.totalUsers)" -ForegroundColor White
    Write-Host "   • 修复用户数: $($fixContent.data.fixedUsers)`n" -ForegroundColor White
    
    if ($fixContent.data.fixedUsers -eq 0) {
      Write-Host "所有用户的数据已经是一致的了！" -ForegroundColor Green
    }
  } else {
    Write-Host "❌ 修复失败: $($fixContent.message)" -ForegroundColor Red
  }
} catch {
  Write-Host "❌ 修复请求失败: $_" -ForegroundColor Red
}

Write-Host "`n按任意键退出..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
