# 切换账号功能 - Product Requirement Document

## Overview
- **Summary**: 在菜单栏中新增"切换账号"功能，支持在预设账号间快速切换，无需手动输入密码
- **Purpose**: 方便测试和演示不同用户角色的功能
- **Target Users**: 开发者、测试人员、演示人员

## Goals
- 在"管理后台"菜单后新增"切换账号"菜单
- 点击后显示子菜单包含五个预设用户：super、admin1、admin2、user1、user2
- 选择用户后自动退出并重新登录
- 当前登录用户在菜单中显示为禁用状态
- 切换成功后显示主界面

## Non-Goals (Out of Scope)
- 不支持自定义添加用户
- 不支持修改密码
- 不保存切换历史

## Background & Context
- 现有菜单栏：首页、板块、我的、管理后台
- 现有登录功能在LoginController中
- 现有登出功能在MainFrameController的logout()方法中

## Functional Requirements
- **FR-1**: 在菜单栏中新增"切换账号"菜单
- **FR-2**: "切换账号"菜单包含子菜单，显示五个预设用户
- **FR-3**: 当前登录用户在子菜单中显示为禁用状态
- **FR-4**: 点击其他用户执行登出+登录切换
- **FR-5**: 所有预设用户密码都是"123456"

## Non-Functional Requirements
- **NFR-1**: 切换过程快速，无明显延迟
- **NFR-2**: 界面友好，明确显示当前用户状态

## Constraints
- **Technical**: 必须使用JavaFX框架
- **Business**: 预设用户固定为：super、admin1、admin2、user1、user2

## Assumptions
- 这些预设用户在数据库中存在
- 所有用户密码都是"123456"
- 可以通过HttpRequestUtil.login()方法直接登录

## Acceptance Criteria

### AC-1: 新增"切换账号"菜单
- **Given**: 用户已登录并进入主界面
- **When**: 查看菜单栏
- **Then**: 在"管理后台"菜单后看到"切换账号"菜单
- **Verification**: `human-judgment`

### AC-2: 显示五个预设用户子菜单
- **Given**: 用户点击"切换账号"菜单
- **When**: 展开子菜单
- **Then**: 看到super、admin1、admin2、user1、user2五个选项
- **Verification**: `programmatic`

### AC-3: 当前用户显示为禁用
- **Given**: 当前用户登录名为user1
- **When**: 查看"切换账号"子菜单
- **Then**: user1菜单项显示为禁用状态
- **Verification**: `human-judgment`

### AC-4: 成功切换到其他用户
- **Given**: 当前用户为user1
- **When**: 点击admin1菜单项
- **Then**: 系统执行登出，然后以admin1身份登录，显示主界面
- **Verification**: `programmatic`

### AC-5: 切换到用户的密码为"123456"
- **Given**: 选择任意用户
- **When**: 执行登录
- **Then**: 使用密码"123456"进行登录
- **Verification**: `programmatic`

## Open Questions
- 无
