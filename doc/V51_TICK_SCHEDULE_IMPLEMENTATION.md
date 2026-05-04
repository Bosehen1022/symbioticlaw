# V51 Tick Schedule Implementation

来源：`doc/V51_Specification.md` → “Tick Schedule（Tick 调度表）/ Event Mapping”

## 调度入口
- `com.symbioticlaw.event.TickHandler#onServerTick`

## 周期任务
- 每 tick（对每个在线玩家）：
  - OnlineTickCount 增量、周期税触发（24000 tick）
  - IdleTicks 增量、潜意识宣传触发
  - HUD 数据同步（罗盘 HUD）
- 每 20 tick（1 秒）：
  - `BorderSystem.tick(server)`：越界检测、倒计时、制裁
- 每 200 tick（10 秒）：
  - `ClassSystem.tick(server)`：阶级判定与状态维护
- 每 1200 tick（60 秒）：
  - `AffinitySystem.tick(server)`：共生扫描与亲密度增减、断链
  - `VisaSystem.tick(server)`：签证/假票过期状态归一
  - `ExileSystem.tick(server)`：遗民债务字段维护（与 balance 负数状态对齐）
- 每 24000 tick（20 分钟 / 1 MC 日）：
  - `ReportSystem.tick(server)`：行政日报广播与周期数据重置

