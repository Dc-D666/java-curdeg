# 用户关注功能 - The Implementation Plan (Decomposed and Prioritized Task List)

## \[/] Task 1: 创建数据库表和SQL脚本

* **Priority**: P0

* **Depends On**: None

* **Description**:

  * 创建 bbs\_follow 表用于存储关注关系

  * 为 user 表添加 follower\_count 和 following\_count 字段

  * 创建相应的索引

* **Acceptance Criteria Addressed**: \[FR-1, FR-4, FR-5]

* **Test Requirements**:

  * `programmatic` TR-1.1: SQL脚本可以成功执行

  * `programmatic` TR-1.2: 表结构符合设计要求

  * `programmatic` TR-1.3: 索引正确创建

* **Notes**: 此任务需要用户确认SQL脚本后再执行

## \[ ] Task 2: 创建后端实体类和Repository

* **Priority**: P0

* **Depends On**: Task 1

* **Description**:

  * 创建 BbsFollow 实体类

  * 更新 User 实体类添加新字段

  * 创建 BbsFollowRepository

  * 更新 UserRepository 添加新查询方法

* **Acceptance Criteria Addressed**: \[FR-1, FR-4, FR-5]

* **Test Requirements**:

  * `programmatic` TR-2.1: 实体类与数据库表映射正确

  * `programmatic` TR-2.2: Repository 查询方法可以正常工作

* **Notes**: 遵循现有项目的代码风格

## \[ ] Task 3: 实现后端关注服务层

* **Priority**: P0

* **Depends On**: Task 2

* **Description**:

  * 创建 BbsFollowService

  * 实现关注/取消关注功能

  * 实现关注状态查询功能

  * 实现关注列表和粉丝列表查询

  * 更新用户的关注数和粉丝数

* **Acceptance Criteria Addressed**: \[FR-1, FR-4, FR-5]

* **Test Requirements**:

  * `programmatic` TR-3.1: 关注功能正常创建关注关系

  * `programmatic` TR-3.2: 取消关注功能正常删除关注关系

  * `programmatic` TR-3.3: 关注数和粉丝数正确更新

  * `programmatic` TR-3.4: 关注状态查询返回正确结果

  * `programmatic` TR-3.5: 关注列表和粉丝列表查询返回正确数据

## \[ ] Task 4: 实现后端关注控制器

* **Priority**: P0

* **Depends On**: Task 3

* **Description**:

  * 创建 BbsFollowController

  * 提供关注/取消关注API

  * 提供关注状态查询API

  * 提供关注列表和粉丝列表API

* **Acceptance Criteria Addressed**: \[FR-1, FR-4, FR-5]

* **Test Requirements**:

  * `programmatic` TR-4.1: API端点可以正常访问

  * `programmatic` TR-4.2: API返回正确的数据格式

  * `programmatic` TR-4.3: 权限控制正确（需要登录）

## \[ ] Task 5: 集成通知功能 - 新帖子通知

* **Priority**: P1

* **Depends On**: Task 3

* **Description**:

  * 修改 BbsPostService 的 createPost 方法

  * 在发布新帖子时，向所有关注该作者的用户发送通知

  * 通知类型定义为"关注用户发帖"

* **Acceptance Criteria Addressed**: \[FR-3]

* **Test Requirements**:

  * `programmatic` TR-5.1: 新帖子发布后，关注者收到通知

  * `programmatic` TR-5.2: 通知内容包含正确的用户和帖子信息

## \[ ] Task 6: 前端数据模型和HTTP工具更新

* **Priority**: P1

* **Depends On**: Task 4

* **Description**:

  * 创建前端 Follow 模型

  * 更新 User 模型添加关注相关字段

  * 在 HttpRequestUtil 中添加关注相关的API调用方法

* **Acceptance Criteria Addressed**: \[FR-1, FR-4, FR-5]

* **Test Requirements**:

  * `programmatic` TR-6.1: 数据模型定义正确

  * `programmatic` TR-6.2: HTTP工具方法可以正常调用后端API

## \[ ] Task 7: 前端UI - 帖子列表和详情页显示关注状态

* **Priority**: P1

* **Depends On**: Task 6

* **Description**:

  * 修改 PostListController，在用户头像旁显示关注状态

  * 修改 PostDetailController，在作者信息旁显示关注按钮

  * 实现关注/取消关注的点击事件

* **Acceptance Criteria Addressed**: \[FR-2]

* **Test Requirements**:

  * `human-judgement` TR-7.1: 已关注用户的头像旁显示"已关注"标识

  * `human-judgement` TR-7.2: 未关注用户的头像旁显示"关注"按钮

  * `programmatic` TR-7.3: 点击关注按钮可以正确切换关注状态

## \[x] Task 8: 前端UI - 关注列表和粉丝列表页面

* **Priority**: P2

* **Depends On**: Task 6

* **Description**:

  * 创建关注列表和粉丝列表页面的FXML文件

  * 创建相应的Controller

  * 在主界面添加入口菜单

* **Acceptance Criteria Addressed**: \[FR-4]

* **Test Requirements**:

  * `human-judgement` TR-8.1: 可以通过菜单访问关注/粉丝列表

  * `programmatic` TR-8.2: 列表正确显示用户信息

  * `programmatic` TR-8.3: 在列表中可以进行关注/取消关注操作

* **Notes**: 核心功能已完成，关注列表API已准备好，可通过数据库配置菜单

