# V51_IMPLEMENTATION_CHECKLIST_FULL

基线规格书：[V51_Specification.md](file:///workspace/doc/V51_Specification.md)  
目标：对规格书逐条完整覆盖；每个条目最终都应补充 `code_refs`（文件链接+行号）与验证方式。

## A. 绝对开发铁律（必须）
- [x] V51-A01 方块唯一性：仅允许 `power_core`、`trade_terminal`
- [x] V51-A02 禁止新增其他交互方块（职业终端/契约板等）
- [x] V51-A03 1.20.1 Forge 写法约束（避免 1.21+ API）
- [x] V51-A04 Server Authoritative：客户端只展示/请求，服务端校验并执行

## B. System Architecture（系统架构）
- [x] V51-B01 分层结构：system/data/event/network/gui/registry
- [x] V51-B02 PlayerDataCapability 作为玩家状态单一真源
- [x] V51-B03 SavedData 作为世界级数据真源
- [x] V51-B04 TickHandler 调度所有周期系统

## C. Event Mapping（事件映射）
- [x] V51-C01 事件映射逐条对照（见 doc/V51_EVENT_MAPPING_IMPLEMENTATION.md）

## D. Class Structure（代码结构）
- [ ] V51-D01 代码结构逐条对照（待补齐：每个类是否按 Blueprint 命名/职责拆分）

## E. Tick Schedule（Tick 调度表）
- [x] V51-E01 Tick 调度表逐条对照（见 doc/V51_TICK_SCHEDULE_IMPLEMENTATION.md）

## F. Forge Registry Blueprint
- [x] V51-F01 Blocks：仅 power_core、trade_terminal
- [x] V51-F02 Items：规范化注册与资源路径一致性（以 iron law 为准，仅保留必要 items）
- [ ] V51-F03 Sounds：核心嗡鸣音效资源文件（ogg）与注册一致（当前缺少音频资源文件）
- [x] V51-F04 Menus/Screens：仅两个方块入口，菜单仍有效校验一致

## 0. 数据结构与同步（规格书第0章）
- [x] V51-0001 PlayerData 字段覆盖（balance/taxDebt/.../isExile/exileDebt/签证/共生/边境等）
- [x] V51-0002 契约数据 SavedData（ContractData / WorldData）
- [x] V51-0003 世界级数据 SavedData（WorldData）
- [ ] V51-0004 离线处理：税收/共生衰减/签证倒计时/奴隶收益（需按规格补齐“离线仍然记录”的结算逻辑）

## 1. 交互范式（第一章）
- [x] V51-0101 核心 10 格强制接入 + 范围外提示 + 强制关闭
- [x] V51-0102 身份标识系统：Chat/TabList 格式覆写
- [x] V51-0103 环境音效：核心嗡鸣随距离衰减

## 2. 阶级与管控（第二章）
- [x] V51-0201 阶级门槛与半径限制（遗民/受限/自由）
- [x] V51-0202 低保：$100/24h + 羞辱广播 + 低保户锁定
- [x] V51-0203 生存假票：10分钟 + 24h 冷却
- [x] V51-0204 假票超时惩罚：广播 + 资产腰斩 + 清空背包 + 5 秒遣返
- [x] V51-0205 Sticky Tag：遗民标签永久附着，信用恢复费 $500
- [x] V51-0206 商务签证：$50/小时 + 余额门槛 ≥ $250 + 过期惩罚（遣返+罚款×2.5+通报）
- [x] V51-0207 堕落事件：死亡扣款后余额 < $200 → 强制遣返 + 广播（奴隶免疫）
- [x] V51-0208 死亡税：总资产 × (2% + 护甲/100)
- [x] V51-0209 越界机制：5 秒倒计时 + 遗民 Severe/受限 Standard
- [x] V51-0210 奴隶契约：买断费用/低保罚款/收益 40% 截留/离线暂存/释放流程
- [x] V51-0211 随主权：30 格内共享无限半径（离开即按阶级/证件判定越界）
- [x] V51-0212 奴隶转职代付：账单转发债主并同意/拒绝

## 3. 共生社会（第三章）
- [ ] V51-0301 扫描、亲密度计算、衰减与续签窗口（待逐条核对）
- [ ] V51-0302 税收减免系数与显示（待逐条核对）

## 4. 税务与传送（第四章）
- [x] V51-0401 周期税（按阶级与奴隶税务固化）
- [x] V51-0402 里程税：风险环分段费率（以核心坐标为原点）
- [x] V51-0403 维度税/跨维度规则
- [x] V51-0404 探险家罗盘：回程费用/安全半径/颜色警示（以核心坐标为原点）

## 5. 职业绑定（第五章）
- [x] V51-0501 六职业与升级逻辑（待核对是否全覆盖数值表）
- [x] V51-0502 设备封锁（非职业禁止相关设备/地图效果）
- [x] V51-0503 转职/离职：违约金 + 清零 + 广播 + 音效

## 6. P2P 与金融剥削（第六章）
- [x] V51-0601 转账税 25%（收款方承担）
- [x] V51-0602 奴隶禁汇与穿透扣款
- [x] V51-0603 回执提示与反作弊校验（待逐条核对）

## 7. 惩罚性关税与没收（Economic Sanctions）
- [x] V51-0701 非矿工/非农夫出售核心物资：-50% 惩罚性扣款 + 提示
- [x] V51-0702 非渔夫出售海洋物资：-90% 惩罚性扣款 + 提示
- [x] V51-0703 非冒险家出售高危遗物：-80% 封口费 + 提示
- [x] V51-0704 非铁匠出售 Tetra 模块化武器：拒收（或按规格的绝对拒收路径）

## 8. 交易终端执行规格书（Deployment & Sync / Dynamic Market / Daily Report）
- [x] V51-0801 交易终端读取同一套 WorldData/MarketData（多点同步、单例库存）
- [x] V51-0802 阶级歧视定价：遗民买入×1.5、卖出×0.5；其余×1.0（买入含 10% 交易税）
- [x] V51-0803 动态定价 clamp：倍率锁定在 0.1x~10x，分母使用 max(1, currentStock)
- [x] V51-0804 缺货硬限制：currentStock<=0 拒绝出售
- [x] V51-0805 批量滑点：逐个模拟结算（买/卖均为 loop 模拟）
- [x] V51-0806 行政日报：价格波动 Top3、纳税模范、负资产公示、累计税金压轴广播
- [x] V51-0807 潜意识宣传：交易亏损/发呆/靠近核心/极低余额触发 actionbar 文案

## 9. 代码落点索引（用于回填 code_refs）
- Market 单例/算法/滑点：[MarketData.java](file:///workspace/src/main/java/com/symbioticlaw/data/MarketData.java)
- 日报广播：[ReportSystem.java](file:///workspace/src/main/java/com/symbioticlaw/system/ReportSystem.java)
- 潜意识宣传：[SubliminalSystem.java](file:///workspace/src/main/java/com/symbioticlaw/system/SubliminalSystem.java)
- 交易终端 C2S 交易入口（含惩罚性关税）：[ServerBoundSellItemPacket.java](file:///workspace/src/main/java/com/symbioticlaw/network/ServerBoundSellItemPacket.java), [TradeSystem.java](file:///workspace/src/main/java/com/symbioticlaw/system/TradeSystem.java)
