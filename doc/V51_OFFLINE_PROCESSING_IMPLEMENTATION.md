# V51 Offline Processing Implementation

来源：`doc/V51_Specification.md` → “0.6 离线处理”

## 奴隶收益
- 在线：`com.symbioticlaw.system.IncomeManager#processIncome` 将 40% 直接结算给在线债主
- 离线：债主不在线时，40% 写入 `WorldData.unclaimedIncome`（`UnclaimedIncomeData`）
- 提示：债主登录时由 `com.symbioticlaw.event.IdentityEvents#onPlayerLogin` 提示并引导前往核心领取

## 共生衰减
- `com.symbioticlaw.system.AffinitySystem#tick` 每 60 秒扫描一次；任一方离线时视为不在范围内 → affinity -1

## 签证/假票时间
- 以 `gameTime` 作为绝对时间基准存储 expireTime；即使玩家离线，世界 `gameTime` 仍推进，因此到期判定在玩家下次上线或边境检测时自然生效

## 周期税
- 周期税按 OnlineTickCount 计时：离线不计入在线时长；上线后继续累计至 24000 tick 触发扣税

