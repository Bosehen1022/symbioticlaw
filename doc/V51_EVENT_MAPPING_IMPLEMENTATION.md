# V51 Event Mapping Implementation

来源：`doc/V51_Specification.md` → “Event Mapping（事件映射）”

## 1) Player Lifecycle Events

### PlayerLoggedInEvent
- 处理器：`com.symbioticlaw.event.IdentityEvents#onPlayerLogin`
- 行为：
  - 提示债主离线收益
  - 同步 PlayerData 到客户端（`PlayerDataSyncPacket`）

### PlayerRespawnEvent
- 处理器：`com.symbioticlaw.event.IdentityEvents#onPlayerRespawn`
- 行为：
  - Fall Event（堕落事件）强制遣返与广播

## 2) Combat / Death Events

### LivingDeathEvent
- 处理器：`com.symbioticlaw.event.IdentityEvents#onLivingDeath`
- 行为：
  - 死亡税（奴隶免疫）
  - 余额跌破阈值后标记堕落并由 respawn 阶段处理遣返

## 3) Interaction Events

### RightClickBlock（核心/终端）
- Core：`com.symbioticlaw.block.CoreBlock#use`
- Terminal：`com.symbioticlaw.block.TradeTerminalBlock#use`
- 行为：
  - 打开对应 Menu/Screen
  - 真实业务逻辑通过 C2S 包在服务端执行

## 4) Server Tick Events（统一调度）

### ServerTickEvent
- 调度器：`com.symbioticlaw.event.TickHandler#onServerTick`
- 频率：
  - 每 tick：在线 tick、idle tick、HUD 数据推送、潜意识宣传触发
  - 20 tick：BorderSystem
  - 200 tick：ClassSystem
  - 1200 tick：AffinitySystem、VisaSystem、ExileSystem
  - 24000 tick：ReportSystem（行政日报）

