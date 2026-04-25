# 邮箱验证码注册功能 - 实施任务清单

## 任务优先级说明
- P0：核心功能，必须优先完成
- P1：重要功能，建议完成
- P2：优化和增强

---

## 一、后端开发任务

### [ ] Task 1: 创建数据库表
- **优先级**：P0
- **依赖**：无
- **描述**：创建 email_verification 表，用于存储邮箱验证码
- **验收标准**：
  - 表结构符合PRD中定义
  - 索引创建成功
  - 可以正常插入和查询数据
- **测试要求**：
  - 执行 SQL 脚本验证表创建
  - 尝试插入测试数据
  - 查询测试数据
- **相关文件**：
  - java-server/src/main/resources/sql/23_add_email_verification_table.sql
- **工时**：0.5小时

---

### [ ] Task 2: 创建实体类 EmailVerification
- **优先级**：P0
- **依赖**：Task 1
- **描述**：创建 JPA 实体类，映射数据库表
- **验收标准**：
  - 字段完整映射数据库表
  - 使用 Lombok 简化代码
  - 添加必要的注解
- **测试要求**：
  - 实体类编译通过
  - 字段类型匹配
- **相关文件**：
  - java-server/src/main/java/cn/edu/sdu/java/server/models/EmailVerification.java
- **工时**：1小时

---

### [ ] Task 3: 创建 Repository 接口
- **优先级**：P0
- **依赖**：Task 2
- **描述**：创建 EmailVerificationRepository 接口
- **验收标准**：
  - 继承 JpaRepository
  - 定义必要的查询方法
  - 支持按邮箱和类型查询
- **测试要求**：
  - Repository 编译通过
  - 查询方法定义正确
- **相关文件**：
  - java-server/src/main/java/cn/edu/sdu/java/server/repositorys/EmailVerificationRepository.java
- **工时**：0.5小时

---

### [ ] Task 4: 配置邮件服务
- **优先级**：P0
- **依赖**：无
- **描述**：在 application.yml 中配置 QQ 邮箱 SMTP
- **验收标准**：
  - 配置文件包含邮件参数
  - 添加说明文档
  - 配置模板邮件可以成功发送
- **测试要求**：
  - 编写测试类
  - 验证邮件可以成功发送
- **相关文件**：
  - java-server/src/main/resources/application.yml
- **工时**：1小时

---

### [ ] Task 5: 创建邮件发送服务
- **优先级**：P0
- **依赖**：Task 4
- **描述**：创建 EmailService，封装邮件发送逻辑
- **验收标准**：
  - 支持发送简单文本邮件
  - 支持发送 HTML 邮件（可选）
  - 处理发送异常
- **测试要求**：
  - 测试邮件可以成功发送
  - 测试发送失败的异常处理
- **相关文件**：
  - java-server/src/main/java/cn/edu/sdu/java/server/services/EmailService.java
- **工时**：1.5小时

---

### [ ] Task 6: 创建验证码服务
- **优先级**：P0
- **依赖**：Task 3, Task 5
- **描述**：创建 EmailVerificationService，实现验证码的生成、发送、验证逻辑
- **验收标准**：
  - 生成6位随机验证码
  - 检查发送频率限制
  - 检查每日发送上限
  - 验证验证码正确性
  - 检查验证码有效期
  - 标记验证码已使用
- **测试要求**：
  - 测试发送验证码成功
  - 测试发送频率限制生效
  - 测试验证成功
  - 测试验证失败的各种情况
- **相关文件**：
  - java-server/src/main/java/cn/edu/sdu/java/server/services/EmailVerificationService.java
- **工时**：3小时

---

### [ ] Task 7: 创建发送验证码接口
- **优先级**：P0
- **依赖**：Task 6
- **描述**：在 AuthController 中添加发送验证码接口
- **验收标准**：
  - 接口路径：POST /api/auth/sendEmailCode
  - 验证邮箱格式
  - 调用验证码服务发送
  - 返回统一响应
- **测试要求**：
  - 测试发送成功
  - 测试发送失败的各种错误提示
- **相关文件**：
  - java-server/src/main/java/cn/edu/sdu/java/server/controllers/AuthController.java
- **工时**：1小时

---

### [ ] Task 8: 改造注册接口
- **优先级**：P0
- **依赖**：Task 6
- **描述**：改造 /api/auth/registerUser 接口，增加验证码验证
- **验收标准**：
  - 接收 emailCode 参数
  - 验证验证码
  - 验证成功后注册
  - 标记验证码已使用
- **测试要求**：
  - 测试验证通过注册成功
  - 测试验证失败注册失败
  - 测试各种验证失败提示
- **相关文件**：
  - java-server/src/main/java/cn/edu/sdu/java/server/services/AuthService.java
- **工时**：2小时

---

## 二、前端开发任务

### [ ] Task 9: 修改注册页面 FXML
- **优先级**：P0
- **依赖**：无
- **描述**：在 register-view.fxml 中添加验证码输入框和发送按钮
- **验收标准**：
  - 添加验证码输入框
  - 添加发送验证码按钮
  - 布局合理美观
- **测试要求**：
  - 界面显示正常
- **相关文件**：
  - java-fx/src/main/resources/com/teach/javafx/base/register-view.fxml
- **工时**：1小时

---

### [ ] Task 10: 修改 RegisterController
- **优先级**：P0
- **依赖**：Task 9
- **描述**：添加发送验证码的逻辑和倒计时功能
- **验收标准**：
  - 点击发送按钮调用后端接口
  - 发送后按钮禁用
  - 显示60秒倒计时
  - 倒计时结束后恢复按钮
  - 验证通过后才允许注册
- **测试要求**：
  - 测试发送验证码成功
  - 测试倒计时正常
  - 测试验证成功
- **相关文件**：
  - java-fx/src/main/java/com/teach/javafx/controller/base/RegisterController.java
- **工时**：2小时

---

### [ ] Task 11: 修改 HttpRequestUtil
- **优先级**：P0
- **依赖**：无
- **描述**：添加发送验证码的请求方法
- **验收标准**：
  - 添加 sendEmailCode 方法
  - 修改 registerUser 方法，增加验证码参数
- **测试要求**：
  - 测试请求发送成功
- **相关文件**：
  - java-fx/src/main/java/com/teach/javafx/request/HttpRequestUtil.java
- **工时**：1小时

---

## 三、测试与联调任务

### [ ] Task 12: 后端单元测试
- **优先级**：P1
- **依赖**：Task 8
- **描述**：编写后端单元测试
- **验收标准**：
  - 测试发送验证码接口测试
  - 注册接口测试
  - 各种边界情况测试
- **测试要求**：
  - 测试用例覆盖主要流程
- **工时**：2小时

---

### [ ] Task 13: 前后端联调
- **优先级**：P0
- **依赖**：Task 11, Task 8
- **描述**：前后端联调测试完整注册流程
- **验收标准**：
  - 完整注册流程可以正常执行
  - 发送验证码成功
  - 验证通过成功注册
  - 各种错误提示正常显示
- **测试要求**：
  - 测试完整注册流程
  - 测试各种失败情况
- **工时**：2小时

---

### [ ] Task 14: 文档与配置说明
- **优先级**：P2
- **依赖**：Task 13
- **描述**：编写配置说明文档
- **验收标准**：
  - 说明如何获取QQ邮箱授权码
  - 说明如何配置SMTP
  - 说明如何执行SQL
- **相关文件**：
  - README.md 或单独文档
- **工时**：1小时

---

## 任务总计

| 优先级 | 数量 | 估计工时 |
|--------|------|---------|
| P0 | 9 | 13.5小时 |
| P1 | 1 | 2小时 |
| P2 | 1 | 1小时 |
| **总计** | **11** | **16.5小时** |

---

## 实施顺序建议

1. 第一阶段（后端核心）：Task 1 → Task 2 → Task 3 → Task 4 → Task 5 → Task 6 → Task 7 → Task 8
2. 第二阶段（前端）：Task 9 → Task 10 → Task 11
3. 第三阶段（测试）：Task 12 → Task 13 → Task 14
