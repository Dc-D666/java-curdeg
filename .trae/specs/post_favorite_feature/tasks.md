# 收藏帖子功能 - 实施计划

## 概述
本功能参考现有点赞功能实现，从数据库到前端全流程开发。

---

## 第一阶段：后端数据库和模型层

### [x] Task 1: 创建数据库实体类 BbsFavorite
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 参考 BbsLike.java 创建 BbsFavorite.java 实体类
  - 包含字段：id, postId, userId, createTime
  - 添加唯一约束 (post_id, user_id)
  - 使用 JPA 注解映射到数据库表 bbs_favorite
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3
- **Test Requirements**:
  - `programmatic` TR-1.1: 实体类编译通过
  - `programmatic` TR-1.2: 包含所有必要的字段和注解
  - `human-judgement` TR-1.3: 代码风格与 BbsLike.java 保持一致
- **Notes**: 文件路径：java-server/src/main/java/cn/edu/sdu/java/server/models/BbsFavorite.java

### [x] Task 2: 更新 BbsPost 实体类添加收藏计数
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 BbsPost.java 中添加 favoriteCount 字段
  - 初始值设为 0
  - 映射到数据库列 favorite_count
- **Acceptance Criteria Addressed**: AC-4, AC-5
- **Test Requirements**:
  - `programmatic` TR-2.1: 字段添加成功，类型为 Integer
  - `programmatic` TR-2.2: 包含 @Column 注解
- **Notes**: 文件路径：java-server/src/main/java/cn/edu/sdu/java/server/models/BbsPost.java

### [x] Task 3: 创建 Repository 接口 BbsFavoriteRepository
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 参考 BbsLikeRepository.java 创建 BbsFavoriteRepository.java
  - 实现方法：findByPostIdAndUserId, existsByPostIdAndUserId, countByPostId, deleteByPostIdAndUserId, deleteByPostId
- **Acceptance Criteria Addressed**: AC-2, AC-3, AC-4
- **Test Requirements**:
  - `programmatic` TR-3.1: 接口编译通过
  - `programmatic` TR-3.2: 包含所有必要的方法签名
  - `human-judgement` TR-3.3: 代码风格与 BbsLikeRepository.java 保持一致
- **Notes**: 文件路径：java-server/src/main/java/cn/edu/sdu/java/server/repositorys/BbsFavoriteRepository.java

---

## 第二阶段：后端服务层

### [x] Task 4: 更新 BbsPostService 添加收藏相关方法
- **Priority**: P0
- **Depends On**: Task 2, Task 3
- **Description**: 
  - 注入 BbsFavoriteRepository
  - 实现 toggleFavorite 方法（参考 toggleLike）
  - 实现 getFavoriteStatus 方法（参考 getLikeStatus）
  - 在 deletePost 方法中添加删除相关收藏记录的逻辑
- **Acceptance Criteria Addressed**: AC-2, AC-3, AC-4
- **Test Requirements**:
  - `programmatic` TR-4.1: toggleFavorite 方法能正确切换收藏状态
  - `programmatic` TR-4.2: getFavoriteStatus 方法能正确返回收藏状态和数量
  - `programmatic` TR-4.3: deletePost 时相关收藏被删除
  - `human-judgement` TR-4.4: 代码风格与 toggleLike 保持一致
- **Notes**: 文件路径：java-server/src/main/java/cn/edu/sdu/java/server/services/BbsPostService.java

---

## 第三阶段：后端控制器层

### [x] Task 5: 更新 BbsPostController 添加收藏接口
- **Priority**: P0
- **Depends On**: Task 4
- **Description**: 
  - 添加 POST /api/bbs/post/{id}/favorite 接口（toggleFavorite）
  - 添加 GET /api/bbs/post/{id}/favorite/status 接口（getFavoriteStatus）
  - 添加适当的权限注解
- **Acceptance Criteria Addressed**: AC-2, AC-3, AC-4
- **Test Requirements**:
  - `programmatic` TR-5.1: 接口路径正确
  - `programmatic` TR-5.2: 权限注解正确（需要登录）
  - `human-judgement` TR-5.3: 代码风格与点赞接口保持一致
- **Notes**: 文件路径：java-server/src/main/java/cn/edu/sdu/java/server/controllers/BbsPostController.java

---

## 第四阶段：前端模型层

### [x] Task 6: 更新 Post 模型添加收藏计数
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 java-fx 的 Post.java 中添加 favoriteCount 字段
  - 添加 getter 和 setter 方法
- **Acceptance Criteria Addressed**: AC-4, AC-5
- **Test Requirements**:
  - `programmatic` TR-6.1: 字段添加成功
  - `programmatic` TR-6.2: getter/setter 方法正确
- **Notes**: 文件路径：java-fx/src/main/java/com/teach/javafx/models/Post.java

---

## 第五阶段：前端请求工具层

### [x] Task 7: 更新 HttpRequestUtil 添加收藏相关方法
- **Priority**: P0
- **Depends On**: Task 5, Task 6
- **Description**: 
  - 添加 toggleFavorite 方法（参考 toggleLike）
  - 添加 getFavoriteStatus 方法（参考 getLikeStatus）
  - 使用正确的 API 路径
- **Acceptance Criteria Addressed**: AC-2, AC-3, AC-4
- **Test Requirements**:
  - `programmatic` TR-7.1: 方法签名正确
  - `programmatic` TR-7.2: API 路径正确
  - `human-judgement` TR-7.3: 代码风格与点赞方法保持一致
- **Notes**: 文件路径：java-fx/src/main/java/com/teach/javafx/request/HttpRequestUtil.java

---

## 第六阶段：前端UI层

### [x] Task 8: 更新 post-detail.fxml 添加收藏按钮
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在点赞按钮旁边添加收藏按钮
  - 配置 fx:id 为 favoriteButton
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `human-judgement` TR-8.1: 按钮位置在点赞按钮旁边
  - `programmatic` TR-8.2: fx:id 配置正确
- **Notes**: 文件路径：java-fx/src/main/resources/com/teach/javafx/post-detail.fxml

### [x] Task 9: 更新 PostDetailController 添加收藏功能逻辑
- **Priority**: P0
- **Depends On**: Task 7, Task 8
- **Description**: 
  - 添加 @FXML 注入 favoriteButton
  - 添加 isFavorited 状态变量
  - 实现 toggleFavorite 方法（参考 toggleLike）
  - 实现 loadFavoriteStatus 方法（参考 loadLikeStatus）
  - 实现 updateFavoriteButtonText 方法（参考 updateLikeButtonText）
  - 在 initialize 中绑定按钮事件
  - 在 loadPostDetail 中调用 loadFavoriteStatus
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3, AC-4, AC-5
- **Test Requirements**:
  - `programmatic` TR-9.1: 按钮事件绑定正确
  - `programmatic` TR-9.2: 收藏状态正确切换
  - `programmatic` TR-9.3: 按钮文本和样式正确更新
  - `human-judgement` TR-9.4: 代码风格与点赞功能保持一致
- **Notes**: 文件路径：java-fx/src/main/java/com/teach/javafx/controller/PostDetailController.java

---

## 总体实施顺序
1. Task 1 → Task 2 → Task 3 (数据库和模型层)
2. Task 4 (服务层)
3. Task 5 (控制器层)
4. Task 6 (前端模型)
5. Task 7 (前端请求工具)
6. Task 8 → Task 9 (前端UI)
