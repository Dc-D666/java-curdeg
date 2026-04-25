
# 统计功能 Bug 修复计划

## 问题分析

### 1. ClassCastException: Boolean cannot be cast to Number
- 错误位置：`getPostStatusDistribution()` 方法，status 字段类型问题
- 原因：BBS Post 表 status 字段可能是 tinyint/boolean 类型，返回 Boolean 而不是 Integer

### 2. 帖子状态分布图仍然无法加载
- 问题和上面相同，状态类型处理需要修复

### 3. 标题未正常显示的图表（互动统计中）
- 需要排查数据加载和图表更新逻辑

### 4. 审核通过率和拒绝率保留两位小数
- 当前直接返回 double，需要格式化

### 5. 数据概览关键指标使用整数
- 当前是 Long/Number 类型，确保前端能正确显示为整数

## 需要修改的文件

### 后端

1. `java-server/src/main/java/cn/edu/sdu/java/server/services/StatisticsService.java`
   - `getPostStatusDistribution()` 修复 status 字段类型处理
   - `getModerationOverview()` 格式化通过率为两位小数
   - `getOverview()` 确保整数类型安全
   - 检查 `getModerationTrend()` 方法，查看字段映射是否有问题

2. 检查 repository 查询是否有问题

### 前端

1. `java-fx/src/main/java/com/teach/javafx/controller/StatisticsController.java`
   - 进一步增强数字解析的安全性
   - 确保图表标题和数据正常加载
   - 检查互动统计图表加载代码

2. 检查 FXML 图表配置

## 修复步骤

### Step 1: 修复帖子状态分布图类型转换
- 安全处理 status 字段：支持 Boolean 和 Integer 类型
- 使用 `instanceof` 判断后再转换

### Step 2: 格式化审核通过率
- 使用 `String.format("%.2f", rate)` 返回两位小数的字符串
- 或返回 double 但确保是两位小数精度

### Step 3: 确保数据概览都是整数
- Long 类型本身就是整数，确保在前端正确显示
- 在前端 parseNumberSafely 中确保显示为整数

### Step 4: 排查互动统计图表
- 检查 `loadInteractionTrends()` 方法
- 确保图表正确设置数据

### Step 5: 完整编译测试
- 编译后端
- 编译前端
- 验证功能正常

