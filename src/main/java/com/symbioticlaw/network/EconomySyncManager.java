package com.symbioticlaw.network;

import com.symbioticlaw.data.WorldData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.system.ChefWholesaleSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

/**
 * 经济数据同步管理器
 * 
 * 负责：
 * 1. 定期广播经济数据给所有玩家
 * 2. 玩家登录时发送初始数据
 * 3. 响应客户端请求发送数据
 */
@Mod.EventBusSubscriber
public class EconomySyncManager {
    
    // 同步间隔 (tick) - 每30秒同步一次
    private static final int SYNC_INTERVAL = 600;
    private static int tickCounter = 0;
    
    // 默认税率
    private static final double DEFAULT_TRANSFER_TAX = 0.25;
    private static final double DEFAULT_INCOME_TAX = 0.20;
    private static final double DEFAULT_SALES_TAX = 0.10;
    
    /**
     * 服务器 tick 事件 - 定期同步
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        tickCounter++;
        if (tickCounter >= SYNC_INTERVAL) {
            tickCounter = 0;
            broadcastEconomyData(event.getServer());
        }
    }
    
    /**
     * 玩家登录事件 - 发送初始数据
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendEconomyData(player);
        }
    }
    
    /**
     * 广播经济数据给所有在线玩家
     */
    public static void broadcastEconomyData(MinecraftServer server) {
        if (server == null) return;
        
        WorldData worldData = WorldData.get(server.overworld());
        if (worldData == null) return;
        
        ClientBoundEconomySyncPacket packet = createSyncPacket(worldData);
        
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NetworkHandler.sendToPlayer(player, packet);
        }
    }
    
    /**
     * 发送经济数据给指定玩家
     */
    public static void sendEconomyData(ServerPlayer player) {
        if (player == null || player.serverLevel() == null) return;
        
        WorldData worldData = WorldData.get(player.serverLevel());
        if (worldData == null) return;
        
        ClientBoundEconomySyncPacket packet = createSyncPacket(worldData, player);
        NetworkHandler.sendToPlayer(player, packet);
    }
    
    /**
     * 创建同步数据包（通用版本）
     */
    private static ClientBoundEconomySyncPacket createSyncPacket(WorldData worldData) {
        return createSyncPacket(worldData, null);
    }
    
    /**
     * 创建同步数据包（带玩家特定数据）
     */
    private static ClientBoundEconomySyncPacket createSyncPacket(WorldData worldData, ServerPlayer player) {
        // 获取批发价格（使用默认价格，因为需要 ServerPlayer 才能获取实时价格）
        Map<String, Double> wholesalePrices = new HashMap<>();
        for (Map.Entry<String, Double> entry : getDefaultWholesalePrices().entrySet()) {
            wholesalePrices.put(entry.getKey(), entry.getValue());
        }
        
        // 如果提供了玩家，获取实时价格
        if (player != null) {
            try {
                Map<String, Double> realPrices = ChefWholesaleSystem.getWholesaleCatalog(player);
                if (realPrices != null) {
                    wholesalePrices = realPrices;
                }
            } catch (Exception ignored) {
                // 使用默认价格
            }
        }
        
        double economyIndex = calculateEconomyIndex(worldData);
        double inflationRate = 0.0;
        long totalMoneySupply = (long) worldData.getTotalTaxCollected(); // 使用税收作为货币供应参考
        
        // 每日定额数据
        String quotaItem = worldData.getDailyQuotaItem();
        int quotaRequired = worldData.getDailyQuotaAmount();
        int quotaProgress = 0;
        double quotaReward = worldData.getDailyQuotaReward();
        if (player != null) {
            quotaProgress = player.getCapability(PlayerDataCapability.INSTANCE)
                    .map(com.symbioticlaw.capability.IPlayerData::getDailyQuotaProgress)
                    .orElse(0);
        }
        
        return new ClientBoundEconomySyncPacket(
            wholesalePrices,
            DEFAULT_TRANSFER_TAX,  // transferTaxRate
            DEFAULT_INCOME_TAX,    // incomeTaxRate  
            DEFAULT_SALES_TAX,     // salesTaxRate
            1.0,                   // classTaxMultiplier (默认1.0x)
            economyIndex,
            inflationRate,
            totalMoneySupply,
            quotaItem,
            quotaRequired,
            quotaProgress,
            quotaReward
        );
    }
    
    /**
     * 计算经济指数
     * 基于总税收和玩家数量
     */
    private static double calculateEconomyIndex(WorldData worldData) {
        double taxCollected = worldData.getTotalTaxCollected();
        // 简单的经济指数计算公式
        // 基准：10000税收 = 1.0指数
        double index = taxCollected / 10000.0;
        // 限制在 0.5 - 2.0 范围内
        return Math.max(0.5, Math.min(2.0, index));
    }
    
    /**
     * 获取默认批发价格
     */
    private static Map<String, Double> getDefaultWholesalePrices() {
        Map<String, Double> defaults = new HashMap<>();
        // 肉类
        defaults.put("minecraft:beef", 11.5);
        defaults.put("minecraft:porkchop", 9.2);
        defaults.put("minecraft:chicken", 5.75);
        defaults.put("minecraft:mutton", 8.05);
        defaults.put("minecraft:rabbit", 4.6);
        defaults.put("minecraft:egg", 2.3);
        // 农作物
        defaults.put("minecraft:wheat", 3.45);
        defaults.put("minecraft:carrot", 2.88);
        defaults.put("minecraft:potato", 2.3);
        defaults.put("minecraft:beetroot", 2.3);
        // 其他
        defaults.put("minecraft:apple", 2.3);
        defaults.put("minecraft:sugar", 3.45);
        
        return defaults;
    }
    
    /**
     * 强制立即同步（供管理员使用）
     */
    public static void forceSync(MinecraftServer server) {
        tickCounter = SYNC_INTERVAL; // 下次tick立即同步
    }
}
