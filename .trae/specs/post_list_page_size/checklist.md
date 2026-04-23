# 帖子列表每页条数选择功能 - Verification Checklist

- [x] post-list.fxml 中新增了"每页显示："Label 和 ComboBox 控件
- [x] 控件布局合理，位于翻页按钮旁边
- [x] PostListController 中有 pageSizeComboBox 的 @FXML 注入
- [x] currentPageSize 初始值为 20
- [x] 下拉框包含 10、20、50 三个选项
- [x] 默认选中 20
- [x] 切换选项后 currentPageSize 正确更新
- [x] 切换选项后 currentPageNum 重置为 1
- [x] 切换选项后列表自动刷新
- [x] 后端 BbsPostService 的默认 pageSize 调整为 20
- [x] 功能完整测试通过（运行应用验证）
