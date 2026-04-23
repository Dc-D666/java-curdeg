
# AI审核调试修复计划

## 问题分析
**现象：**
- 有调试输出，说明异步方法被调用了
- 但异步方法中找不到刚刚保存的帖子（ID=27）
- 数据库中确实存在该帖子

**根本原因：**
- `createPost` 方法是 `@Transactional` 的
- `saveAndFlush` 只是刷新到数据库，但**事务未提交**
- 异步方法在另一个线程执行，看不到未提交的事务数据！

## 修复方案
确保事务提交后再调用异步审核。

## 修改内容

### 1. 修改 `BbsPostService.java`
- 将 `moderatePostAsync` 调用移到 `createPost` 方法之外，或者确保事务提交后调用
- 可以通过两种方式：
  - **方案A**：将异步调用放在方法最后，在事务提交之后
  - **方案B**：使用 `ApplicationEventPublisher` 发布事件，在监听器中处理

这里采用**简单直接的方案A**。

## 修改文件列表
- `java-server/src/main/java/cn/edu/sdu/java/server/services/BbsPostService.java`

## 预期效果
异步审核方法能够找到帖子，继续执行AI审核流程。
