# 稷下币商店体系设计文档

## 一、概述

### 1.1 设计背景
项目已有一套成熟的积分体系（别称"山竹瓣"），用户通过发帖、评论、点赞、登录等行为获取积分，积分影响用户等级和特权。现需在此基础上扩展一套**稷下币商店体系**，售卖系统内虚拟道具，丰富用户互动体验。

### 1.2 核心概念
| 概念 | 说明 |
|------|------|
| **山竹瓣** | 现有积分体系，通过社区行为获取，影响等级 |
| **稷下币** | 新交易货币，可通过签到、积分兑换等方式获取，用于商店消费 |
| **虚拟道具** | 系统内功能性道具，如帖子置顶卡、改名卡等 |
| **背包** | 用户已购买但未使用的道具存储空间 |

### 1.3 货币关系
```
社区行为 → 山竹瓣（积分）→ 等级/特权
                ↓ 兑换
            稷下币 → 商店购买 → 虚拟道具 → 使用效果
                ↑
            签到/活动奖励
```

---

## 二、数据库模型设计

### 2.1 用户表扩展（user）
在现有 `user` 表新增字段：

```sql
ALTER TABLE user ADD COLUMN jixia_coins INT NOT NULL DEFAULT 0 COMMENT '稷下币余额';
ALTER TABLE user ADD COLUMN total_jixia_coins INT NOT NULL DEFAULT 0 COMMENT '累计获得稷下币';
```

对应 `User.java` 实体新增：
```java
@Column(name = "jixia_coins", nullable = false)
private Integer jixiaCoins = 0;

@Column(name = "total_jixia_coins", nullable = false)
private Integer totalJixiaCoins = 0;
```

### 2.2 商品表（bbs_store_item）
> 注意：若数据库中已存在 `bbs_item` 预留表，请先删除旧表再创建本表。

```sql
CREATE TABLE bbs_store_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_code VARCHAR(50) NOT NULL UNIQUE COMMENT '商品编码，如 TOP_24H',
    item_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    item_type VARCHAR(30) NOT NULL COMMENT '道具类型：TOP(置顶), RENAME(改名), BADGE(徽章), THEME(主题)等',
    description VARCHAR(500) COMMENT '商品描述',
    icon_url VARCHAR(255) COMMENT '商品图标URL',
    price INT NOT NULL COMMENT '售价（稷下币）',
    original_price INT COMMENT '原价（用于显示折扣）',
    stock INT DEFAULT -1 COMMENT '库存，-1表示无限',
    daily_limit INT COMMENT '每人每日限购数量',
    level_required INT DEFAULT 0 COMMENT '所需最低等级',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    is_on_sale BOOLEAN DEFAULT TRUE COMMENT '是否上架',
    effect_params JSON COMMENT '道具效果参数（MySQL 8+ JSON类型）',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type (item_type),
    INDEX idx_sale (is_on_sale, sort_order)
) COMMENT='商店商品表';
```

### 2.3 用户背包表（bbs_user_inventory）
> 注意：若数据库中已存在 `bbs_user_item` 预留表，请先删除旧表再创建本表。
> 设计意图：同一用户同一道具合并数量（UNUSED 状态下唯一），使用时扣减数量或变更状态。

```sql
CREATE TABLE bbs_user_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '用户ID',
    item_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '持有数量',
    status VARCHAR(20) DEFAULT 'UNUSED' COMMENT '状态：UNUSED(未使用), USED(已使用), EXPIRED(已过期)',
    acquire_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
    expire_time DATETIME COMMENT '过期时间',
    use_time DATETIME COMMENT '使用时间',
    related_id BIGINT COMMENT '使用关联ID（如置顶帖子ID）',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE KEY uk_user_item_status (user_id, item_id, status),
    INDEX idx_user (user_id, status),
    INDEX idx_expire (expire_time)
) COMMENT='用户背包/道具表';
```

### 2.4 稷下币流水表（bbs_jixia_record）
```sql
CREATE TABLE bbs_jixia_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '用户ID',
    change_type VARCHAR(30) NOT NULL COMMENT '变动类型：CHECKIN(签到), EXCHANGE(兑换), BUY(购买), USE(使用), REFUND(退款), ACTIVITY(活动)',
    coins_change INT NOT NULL COMMENT '变动数量（正为增加，负为减少）',
    balance_after INT NOT NULL COMMENT '变动后余额',
    description VARCHAR(255) COMMENT '描述',
    related_id BIGINT COMMENT '关联ID',
    related_type VARCHAR(50) COMMENT '关联类型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (user_id, create_time),
    INDEX idx_type (change_type)
) COMMENT='稷下币流水记录';
```

### 2.5 签到记录表（bbs_checkin_record）
> 连续签到天数复用 `user.consecutive_login_days` 字段，本表仅记录每日签到事实和奖励。

```sql
CREATE TABLE bbs_checkin_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '用户ID',
    checkin_date DATE NOT NULL COMMENT '签到日期',
    reward_coins INT NOT NULL COMMENT '获得稷下币',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, checkin_date),
    INDEX idx_user (user_id, checkin_date)
) COMMENT='签到记录表';
```

### 2.6 积分兑换规则表（bbs_exchange_rule）
```sql
CREATE TABLE bbs_exchange_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_code VARCHAR(50) NOT NULL UNIQUE COMMENT '规则编码',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    points_cost INT NOT NULL COMMENT '消耗积分',
    coins_reward INT NOT NULL COMMENT '获得稷下币',
    daily_limit INT COMMENT '每日兑换上限',
    is_enabled BOOLEAN DEFAULT TRUE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='积分兑换规则表';
```

---

## 三、后端 API 设计

### 3.1 商店商品 API（StoreController）

```java
@RestController
@RequestMapping("/api/bbs/store")
@RequiredArgsConstructor
public class StoreController {

    /** GET /api/bbs/store/items - 获取商品列表（分页） */
    @GetMapping("/items")
    public DataResponse getItems(
        @RequestParam(required = false) String itemType,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "20") int pageSize)
    // 返回: Page<StoreItem>

    /** GET /api/bbs/store/items/{itemId} - 获取商品详情 */
    @GetMapping("/items/{itemId}")
    public DataResponse getItemDetail(@PathVariable Long itemId)
    // 返回: StoreItem（含折扣后价格）

    /** POST /api/bbs/store/orders - 购买商品（创建订单） */
    @PostMapping("/orders")
    public DataResponse createOrder(@RequestBody DataRequest request)
    // 参数: itemId, quantity
    // 返回: {orderId, itemName, quantity, totalPrice, discountPrice, balanceAfter}

    /** GET /api/bbs/store/inventory - 获取我的背包 */
    @GetMapping("/inventory")
    public DataResponse getMyInventory()
    // 返回: List<UserInventory>

    /** POST /api/bbs/store/inventory/{inventoryId}/use - 使用道具 */
    @PostMapping("/inventory/{inventoryId}/use")
    public DataResponse useItem(@PathVariable Long inventoryId, @RequestBody DataRequest request)
    // 参数: targetId(可选，如帖子ID), params(额外参数如昵称)
    // 支持按 itemId 使用：若 inventoryId 为 0 则自动选择一个未使用的道具

    /** GET /api/bbs/store/coins - 获取稷下币余额 */
    @GetMapping("/coins")
    public DataResponse getMyCoins()
    // 返回: {balance, totalEarned}

    /** GET /api/bbs/store/coins/history - 稷下币流水 */
    @GetMapping("/coins/history")
    public DataResponse getCoinHistory(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "20") int pageSize,
        @RequestParam(required = false) String changeType)
    // changeType 可选筛选: CHECKIN/EXCHANGE/BUY/USE/REFUND/ACTIVITY
}
```

### 3.2 签到 API（CheckinController）

```java
@RestController
@RequestMapping("/api/bbs/checkin")
@RequiredArgsConstructor
public class CheckinController {

    /** POST /api/bbs/checkin - 今日签到 */
    @PostMapping
    public DataResponse doCheckin()

    /** GET /api/bbs/checkin/status - 签到状态 */
    @GetMapping("/status")
    public DataResponse getCheckinStatus()
    // 返回: todayChecked(今日是否签到), consecutiveDays(连续天数), nextReward(明日奖励)

    /** GET /api/bbs/checkin/calendar - 签到日历 */
    @GetMapping("/calendar")
    public DataResponse getCheckinCalendar(@RequestParam int year, @RequestParam int month)
}
```

### 3.3 积分兑换 API（ExchangeController）

```java
@RestController
@RequestMapping("/api/bbs/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    /** GET /api/bbs/exchange/rules - 获取兑换规则 */
    @GetMapping("/rules")
    public DataResponse getExchangeRules()
    // 返回: List<ExchangeRule>（含今日剩余次数）

    /** POST /api/bbs/exchange/orders - 执行兑换（创建兑换订单） */
    @PostMapping("/orders")
    public DataResponse createExchangeOrder(@RequestBody DataRequest request)
    // 参数: ruleCode
    // 返回: {orderId, pointsCost, coinsReward, balanceAfter}
}
```

---

## 四、核心服务设计

### 4.1 JixiaCoinService（稷下币服务）

参考 `PointService.doPointChange` 实现乐观锁重试机制。

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class JixiaCoinService {

    private final UserRepository userRepository;
    private final JixiaRecordRepository jixiaRecordRepository;

    @Transactional
    public void addCoins(Integer userId, int amount, String changeType,
                         String description, Long relatedId, String relatedType) {
        doCoinChange(userId, amount, changeType, description, relatedId, relatedType);
    }

    @Transactional
    public boolean deductCoins(Integer userId, int amount, String description,
                                Long relatedId, String relatedType) {
        return doCoinChange(userId, -Math.abs(amount), description, relatedId, relatedType);
    }

    private boolean doCoinChange(Integer userId, int coinsChange, String changeType,
                                  String description, Long relatedId, String relatedType) {
        int retries = 3;
        while (retries-- > 0) {
            User user = userRepository.findByPersonPersonId(userId).orElse(null);
            if (user == null) return false;

            int oldVersion = user.getVersion();
            int newCoins = user.getJixiaCoins() + coinsChange;
            if (newCoins < 0) return false; // 余额不足

            int updated = userRepository.updateJixiaCoinsAndVersion(userId, newCoins, oldVersion);
            if (updated > 0) {
                // 更新累计获得（仅增加时）
                if (coinsChange > 0) {
                    userRepository.updateTotalJixiaCoins(userId, user.getTotalJixiaCoins() + coinsChange);
                }

                // 记录流水
                JixiaRecord record = new JixiaRecord();
                record.setUserId(userId);
                record.setChangeType(changeType);
                record.setCoinsChange(coinsChange);
                record.setDescription(description);
                record.setRelatedId(relatedId);
                record.setRelatedType(relatedType);
                record.setBalanceAfter(newCoins);
                jixiaRecordRepository.save(record);
                return true;
            }
        }
        log.error("Failed to update jixia coins for user {} after retries", userId);
        return false;
    }

    public int getBalance(Integer userId) {
        return userRepository.findByPersonPersonId(userId)
                .map(User::getJixiaCoins)
                .orElse(0);
    }
}
```

### 4.2 StoreService（商店服务）

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {

    private final StoreItemRepository storeItemRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final JixiaCoinService jixiaCoinService;
    private final LevelPrivilegeService levelPrivilegeService;
    private final DailyLimitRepository dailyLimitRepository;
    private final ItemEffectService itemEffectService;

    @Transactional
    public StoreOrderResult createOrder(Integer userId, Long itemId, int quantity) {
        // 0. 参数校验
        if (quantity < 1 || quantity > 99) throw new IllegalArgumentException("数量非法");

        // 1. 查询商品（带乐观锁）
        StoreItem item = storeItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        if (!item.getIsOnSale()) throw new RuntimeException("商品已下架");

        // 2. 校验等级要求
        User user = userRepository.findByPersonPersonId(userId).orElseThrow();
        if (user.getLevel() < item.getLevelRequired()) {
            throw new RuntimeException("等级不足");
        }

        // 3. 校验库存（原子操作）
        if (item.getStock() >= 0) {
            int updated = storeItemRepository.decreaseStock(itemId, quantity, item.getVersion());
            if (updated == 0) throw new RuntimeException("库存不足");
        }

        // 4. 校验每日限购（复用 DailyLimit 表）
        if (item.getDailyLimit() != null && item.getDailyLimit() > 0) {
            if (!checkAndIncrementDailyLimit(userId, "STORE_BUY", itemId.toString(), item.getDailyLimit())) {
                throw new RuntimeException("今日已达购买上限");
            }
        }

        // 5. 计算折扣价格（四舍五入到整数）
        BigDecimal discount = levelPrivilegeService.getStoreDiscount(user.getLevel());
        int totalPrice = item.getPrice() * quantity;
        int discountPrice = discount.multiply(BigDecimal.valueOf(totalPrice))
                .setScale(0, RoundingMode.HALF_UP).intValue();

        // 6. 扣减稷下币
        boolean deducted = jixiaCoinService.deductCoins(userId, discountPrice,
                "购买[" + item.getItemName() + "]x" + quantity, itemId, "STORE_ITEM");
        if (!deducted) throw new RuntimeException("余额不足");

        // 7. 发放道具到背包（合并数量）
        addToInventory(userId, itemId, quantity, item.getEffectParams());

        // 8. 返回订单信息
        return new StoreOrderResult(item.getItemName(), quantity, totalPrice, discountPrice,
                user.getJixiaCoins());
    }

    @Transactional
    public void useItem(Integer userId, Long inventoryId, Long targetId, Map<String, Object> params) {
        // 1. 查询背包道具（带乐观锁）
        UserInventory inventory = userInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("道具不存在"));
        if (!inventory.getUserId().equals(userId)) throw new RuntimeException("无权使用");
        if (!"UNUSED".equals(inventory.getStatus())) throw new RuntimeException("道具状态异常");

        // 2. 校验目标资源所有权（如置顶自己的帖子）
        if (targetId != null) {
            validateTargetOwnership(userId, targetId, inventory.getItemId());
        }

        // 3. 执行道具效果
        StoreItem item = storeItemRepository.findById(inventory.getItemId()).orElseThrow();
        itemEffectService.applyEffect(item.getItemCode(), userId, targetId, params);

        // 4. 更新背包状态（数量扣减或标记已使用）
        if (inventory.getQuantity() > 1) {
            inventory.setQuantity(inventory.getQuantity() - 1);
        } else {
            inventory.setStatus("USED");
            inventory.setUseTime(LocalDateTime.now());
        }
        inventory.setRelatedId(targetId);
        userInventoryRepository.save(inventory);
    }

    private boolean checkAndIncrementDailyLimit(Integer userId, String limitType,
                                                 String limitKey, int maxCount) {
        // 复用 PointService.checkAndIncrementDailyLimit 模式
        LocalDate today = LocalDate.now();
        Optional<DailyLimit> opt = dailyLimitRepository
                .findByUserIdAndLimitTypeAndLimitKeyAndRecordDate(userId, limitType, limitKey, today);
        if (opt.isPresent()) {
            DailyLimit limit = opt.get();
            if (limit.getUsedCount() >= maxCount) return false;
            limit.setUsedCount(limit.getUsedCount() + 1);
            dailyLimitRepository.save(limit);
            return true;
        } else {
            DailyLimit limit = new DailyLimit();
            limit.setUserId(userId);
            limit.setLimitType(limitType);
            limit.setLimitKey(limitKey);
            limit.setRecordDate(today);
            limit.setUsedCount(1);
            limit.setMaxCount(maxCount);
            dailyLimitRepository.save(limit);
            return true;
        }
    }
}
```

### 4.3 CheckinService（签到服务）

复用 `User.consecutiveLoginDays` 和 `lastLoginDate` 字段计算连续天数。

```java
@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinRecordRepository checkinRecordRepository;
    private final UserRepository userRepository;
    private final JixiaCoinService jixiaCoinService;

    @Transactional
    public CheckinResult doCheckin(Integer userId) {
        LocalDate today = LocalDate.now();

        // 1. 检查今日是否已签到
        if (checkinRecordRepository.existsByUserIdAndCheckinDate(userId, today)) {
            throw new RuntimeException("今日已签到");
        }

        // 2. 获取用户连续登录天数（复用 user 表字段）
        User user = userRepository.findByPersonPersonId(userId).orElseThrow();
        int consecutiveDays = user.getConsecutiveLoginDays();
        if (consecutiveDays < 1) consecutiveDays = 1;

        // 3. 计算奖励
        int reward = calculateReward(consecutiveDays);

        // 4. 发放稷下币
        jixiaCoinService.addCoins(userId, reward, "CHECKIN",
                "连续签到" + consecutiveDays + "天", null, null);

        // 5. 记录签到
        CheckinRecord record = new CheckinRecord();
        record.setUserId(userId);
        record.setCheckinDate(today);
        record.setRewardCoins(reward);
        checkinRecordRepository.save(record);

        return new CheckinResult(reward, consecutiveDays,
                calculateReward(consecutiveDays + 1));
    }

    private int calculateReward(int consecutiveDays) {
        int base = 5;
        int bonus = Math.min(Math.max(consecutiveDays - 1, 0), 15);
        return Math.min(base + bonus, 20);
    }

    public CheckinStatus getCheckinStatus(Integer userId) {
        LocalDate today = LocalDate.now();
        boolean todayChecked = checkinRecordRepository
                .existsByUserIdAndCheckinDate(userId, today);
        User user = userRepository.findByPersonPersonId(userId).orElseThrow();
        int consecutiveDays = user.getConsecutiveLoginDays();
        return new CheckinStatus(todayChecked, consecutiveDays,
                calculateReward(consecutiveDays + 1));
    }
}
```

### 4.4 ItemEffectService（道具效果服务）—— 策略模式

使用策略模式替代 switch 硬编码，每种道具一个 Handler 实现。

```java
// 1. 定义接口
public interface ItemEffectHandler {
    String getItemCode();
    void apply(Integer userId, Long targetId, Map<String, Object> params);
}

// 2. 置顶卡 Handler
@Component
@RequiredArgsConstructor
public class TopItemHandler implements ItemEffectHandler {

    private final BbsPostService bbsPostService;

    @Override
    public String getItemCode() {
        return "TOP_24H"; // 可扩展为匹配 TOP_* 模式
    }

    @Override
    public void apply(Integer userId, Long targetId, Map<String, Object> params) {
        if (targetId == null) throw new RuntimeException("请选择要置顶的帖子");
        // 校验帖子所有权
        Post post = bbsPostService.getPostById(targetId);
        if (!post.getAuthorId().equals(userId.longValue())) {
            throw new RuntimeException("只能置顶自己的帖子");
        }
        int hours = params != null && params.containsKey("hours")
                ? ((Number) params.get("hours")).intValue() : 24;
        bbsPostService.setTop(targetId, hours);
    }
}

// 3. 改名卡 Handler
@Component
@RequiredArgsConstructor
public class RenameItemHandler implements ItemEffectHandler {

    private final BbsUserService bbsUserService;

    @Override
    public String getItemCode() { return "RENAME_CARD"; }

    @Override
    public void apply(Integer userId, Long targetId, Map<String, Object> params) {
        String newNickname = params != null ? (String) params.get("nickname") : null;
        if (newNickname == null || newNickname.isBlank()) {
            throw new RuntimeException("请输入新昵称");
        }
        bbsUserService.updateNickname(userId, newNickname);
    }
}

// 4. 注册中心
@Service
@RequiredArgsConstructor
public class ItemEffectService {

    private final Map<String, ItemEffectHandler> handlerMap;

    @Autowired
    public ItemEffectService(List<ItemEffectHandler> handlers) {
        this.handlerMap = new HashMap<>();
        for (ItemEffectHandler handler : handlers) {
            handlerMap.put(handler.getItemCode(), handler);
        }
    }

    public void applyEffect(String itemCode, Integer userId, Long targetId,
                            Map<String, Object> params) {
        ItemEffectHandler handler = handlerMap.get(itemCode);
        if (handler == null) {
            throw new RuntimeException("暂不支持的道具类型: " + itemCode);
        }
        handler.apply(userId, targetId, params);
    }
}
```

---

## 五、前端界面设计

### 5.1 商店页面（store.fxml）
- 顶部：用户稷下币余额展示 + 签到按钮 + 搜索框 + 排序选项（价格/热度/上架时间）
- 中部：商品分类Tab（全部/置顶/功能/装饰）
- 商品卡片网格：图标、名称、价格（划线原价+折扣价）、购买按钮
- 商品详情弹窗：大图、描述、效果说明、等级要求、购买数量选择、购买成功动画

### 5.2 背包页面（inventory.fxml）
- 道具网格展示：图标、名称、数量、过期时间
- 操作按钮：使用（弹出目标选择）、丢弃
- 空状态提示
- （未来扩展）批量使用/整理功能

### 5.3 签到页面（checkin.fxml）
- 日历视图：标记已签到日期
- 今日签到按钮（已签到则禁用）
- 连续签到天数展示
- 奖励预览：明日可获多少稷下币
- （未来扩展）补签功能

### 5.4 积分兑换页面（exchange.fxml）
- 当前积分余额展示
- 兑换规则列表：消耗积分 → 获得稷下币
- 兑换按钮 + 每日剩余次数

### 5.5 稷下币流水页面（coin-history.fxml）
- 类似积分流水，展示稷下币收支明细
- 支持按类型筛选

### 5.6 管理后台页面（admin-store.fxml）
- 商品列表：上架/下架开关、库存调整、价格编辑
- 兑换规则管理：启用/禁用、调整兑换比例和上限
- 签到配置：基础奖励、连续加成规则

---

## 六、道具设计清单

| 道具编码 | 名称 | 类型 | 价格 | 效果 |
|---------|------|------|------|------|
| TOP_24H | 帖子置顶卡(24h) | 功能 | 50 | 使指定帖子置顶24小时 |
| TOP_72H | 帖子置顶卡(72h) | 功能 | 120 | 使指定帖子置顶72小时 |
| RENAME_CARD | 改名卡 | 功能 | 100 | 修改昵称一次 |
| SIGNBOARD_7D | 个性签名板(7天) | 装饰 | 30 | 彩色签名效果7天 |
| BADGE_EARLY | 先驱者徽章 | 装饰 | 200 | 永久佩戴徽章 |
| POST_COLOR_7D | 炫彩发帖(7天) | 装饰 | 50 | 发帖字体彩色效果 |
| EXP_DOUBLE_1D | 双倍积分卡(1天) | 功能 | 80 | 24小时内获得双倍积分 |
| LUCKY_BAG | 幸运福袋 | 功能 | 30 | 随机获得5-50稷下币 |

---

## 七、与现有体系的关联

### 7.1 等级折扣
利用现有 `LevelPrivilegeService.getStoreDiscount(level)`，高等级用户购买道具享受折扣。
折扣计算：`discountPrice = round(price * discount)`，结果四舍五入到整数。

### 7.2 积分兑换稷下币
在 `PointService` 中新增兑换方法：
```java
@Transactional
public void exchangePointsToCoins(Integer userId, String ruleCode) {
    // 1. 查询兑换规则
    // 2. 校验每日限制（复用 DailyLimit 表，limit_type = "EXCHANGE_RULE"）
    // 3. 扣除积分（复用 doPointChange 乐观锁机制）
    // 4. 增加稷下币（调用 JixiaCoinService.addCoins）
}
```

### 7.3 签到与登录
复用 `AuthService` 中的连续登录天数逻辑：
- `User.consecutiveLoginDays` 和 `lastLoginDate` 由登录流程维护
- `CheckinService` 直接读取该字段计算奖励，不独立维护连续天数

### 7.4 道具效果与帖子系统
道具使用效果直接调用 `BbsPostService` 等现有服务。
需确认 `BbsPost` 实体是否已有置顶字段，若无则需扩展：
```sql
ALTER TABLE bbs_post ADD COLUMN is_top BOOLEAN DEFAULT FALSE COMMENT '是否置顶';
ALTER TABLE bbs_post ADD COLUMN top_expire_time DATETIME COMMENT '置顶过期时间';
```

---

## 八、安全与限制

1. **乐观锁**：用户余额更新使用 `@Version` 乐观锁 + 3次重试机制（参考 PointService）
2. **库存原子扣减**：使用 `UPDATE store_item SET stock = stock - ? WHERE id = ? AND stock >= ?` 原子SQL
3. **每日限制统一**：复用 `DailyLimit` 表，limit_type 区分 `STORE_BUY` / `EXCHANGE_RULE`
4. **事务一致性**：购买流程使用 `@Transactional` 保证扣币和发道具原子性
5. **权限校验**：所有接口通过 Spring Security 校验登录状态；道具使用校验目标资源所有权
6. **参数校验**：购买数量 `@Min(1) @Max(99)`，防止非法输入
7. **幂等性**：签到接口通过 `uk_user_date` 唯一索引天然防重；购买接口可通过前端幂等键扩展
8. **道具过期**：Spring `@Scheduled(cron = "0 0 3 * * ?")` 每日凌晨3点清理过期道具

---

## 十、管理后台 API（AdminStoreController）

```java
@RestController
@RequestMapping("/api/admin/store")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
@RequiredArgsConstructor
public class AdminStoreController {

    /** GET /api/admin/store/items - 商品列表（管理端） */
    @GetMapping("/items")
    public DataResponse listItems(@RequestParam int pageNum, @RequestParam int pageSize)

    /** POST /api/admin/store/items - 新增商品 */
    @PostMapping("/items")
    public DataResponse createItem(@RequestBody DataRequest request)

    /** PUT /api/admin/store/items/{itemId} - 编辑商品 */
    @PutMapping("/items/{itemId}")
    public DataResponse updateItem(@PathVariable Long itemId, @RequestBody DataRequest request)

    /** PATCH /api/admin/store/items/{itemId}/status - 上下架 */
    @PatchMapping("/items/{itemId}/status")
    public DataResponse toggleItemStatus(@PathVariable Long itemId, @RequestBody DataRequest request)

    /** PATCH /api/admin/store/items/{itemId}/stock - 调整库存 */
    @PatchMapping("/items/{itemId}/stock")
    public DataResponse adjustStock(@PathVariable Long itemId, @RequestBody DataRequest request)

    /** GET /api/admin/store/exchange-rules - 兑换规则列表 */
    @GetMapping("/exchange-rules")
    public DataResponse listExchangeRules()

    /** POST /api/admin/store/exchange-rules - 新增/编辑兑换规则 */
    @PostMapping("/exchange-rules")
    public DataResponse saveExchangeRule(@RequestBody DataRequest request)
}
```

---

## 十一、实施计划

### Phase 1：基础框架 + 数据库
1. 编写 SQL 迁移脚本 `33_add_store_system.sql`
   - user 表扩展（jixia_coins, total_jixia_coins）
   - 6 张新表（bbs_store_item, bbs_user_inventory, bbs_jixia_record, bbs_checkin_record, bbs_exchange_rule）
   - 删除旧预留表 bbs_item / bbs_user_item（若存在）
2. 实体类、Repository 搭建
3. JixiaCoinService（乐观锁重试 + 流水）

### Phase 2：商店核心 + 签到
4. StoreItem 管理后台 API（AdminStoreController）
5. 购买流程（createOrder：库存原子扣减 + 扣币 + 发背包 + 流水）
6. 签到功能（CheckinService，复用 user 连续登录天数）
7. 置顶卡效果实现（TopItemHandler + BbsPost 置顶字段扩展）

### Phase 3：积分关联 + 背包使用
8. 积分兑换稷下币（PointService.exchangePointsToCoins，复用 DailyLimit）
9. 背包展示与道具使用（useItem + ItemEffectService 策略模式）
10. 稷下币流水查询

### Phase 4：前端界面
11. JavaFX 商店页面（store.fxml + StoreController）
12. 背包页面（inventory.fxml）
13. 签到页面（checkin.fxml）
14. 积分兑换页面（exchange.fxml）
15. 稷下币流水页面（coin-history.fxml）
16. 菜单栏入口（main-frame.fxml 新增"商店""背包""签到"菜单）

### Phase 5：扩展道具
17. 改名卡、徽章、双倍积分卡等道具 Handler 逐个实现
18. 管理后台前端界面（admin-store.fxml）
19. 回归测试（积分/等级计算不受影响）
