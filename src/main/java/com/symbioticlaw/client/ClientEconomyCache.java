package com.symbioticlaw.client;

import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户端经济数据缓存
 * 服务器定期同步，GUI只读取此缓存
 * 
 * 包含数据：
 * - 批发价格 (WholesalePrice)
 * - 税率 (TaxRate)  
 * - 经济指数 (EconomyIndex)
 * - 市场数据 (MarketData)
 * - 每日定额 (DailyQuota)
 */
public class ClientEconomyCache {
    
    // ===== 批发价格 =====
    private static Map<String, Double> wholesalePrices = new HashMap<>();
    private static long wholesaleLastUpdate = 0;
    
    // ===== 税率数据 =====
    private static double transferTaxRate = 0.25; // 默认25%
    private static double incomeTaxRate = 0.20;   // 默认20%
    private static double salesTaxRate = 0.10;    // 默认10%
    private static double classTaxMultiplier = 1.0; // 阶级税率倍率
    
    // ===== 经济指数 =====
    private static double economyIndex = 1.0;     // 经济指数 (影响所有价格)
    private static double inflationRate = 0.0;    // 通胀率
    private static long totalMoneySupply = 0;     // 总货币供应量
    
    // ===== 市场数据 (简化版) =====
    private static Map<String, MarketItemInfo> marketCache = new HashMap<>();
    
    // ===== 每日定额 =====
    private static String dailyQuotaItem = "";
    private static int dailyQuotaRequired = 0;
    private static int dailyQuotaProgress = 0;
    private static double dailyQuotaReward = 0.0;
    private static long quotaLastUpdate = 0;
    
    // ===== 同步状态 =====
    private static long lastServerSync = 0;
    private static boolean syncPending = false;
    
    // ===== 数据过期时间 (毫秒) =====
    private static final long CACHE_TTL = 60000; // 1分钟
    
    /**
     * 更新批发价格
     */
    public static void updateWholesalePrices(Map<String, Double> prices) {
        wholesalePrices = new HashMap<>(prices);
        wholesaleLastUpdate = System.currentTimeMillis();
    }
    
    /**
     * 获取批发价格
     */
    public static double getWholesalePrice(String itemId) {
        return wholesalePrices.getOrDefault(itemId, 0.0);
    }
    
    /**
     * 获取所有批发价格
     */
    public static Map<String, Double> getWholesalePrices() {
        return Collections.unmodifiableMap(wholesalePrices);
    }
    
    /**
     * 更新税率
     */
    public static void updateTaxRates(double transferTax, double incomeTax, double salesTax, double classMultiplier) {
        transferTaxRate = transferTax;
        incomeTaxRate = incomeTax;
        salesTaxRate = salesTax;
        classTaxMultiplier = classMultiplier;
    }
    
    public static double getTransferTaxRate() { return transferTaxRate; }
    public static double getIncomeTaxRate() { return incomeTaxRate; }
    public static double getSalesTaxRate() { return salesTaxRate; }
    public static double getClassTaxMultiplier() { return classTaxMultiplier; }
    
    /**
     * 计算转账税后金额
     */
    public static double calculateTransferAfterTax(double amount) {
        return amount * (1.0 - transferTaxRate * classTaxMultiplier);
    }
    
    /**
     * 更新经济指数
     */
    public static void updateEconomyIndex(double index, double inflation, long moneySupply) {
        economyIndex = index;
        inflationRate = inflation;
        totalMoneySupply = moneySupply;
    }
    
    public static double getEconomyIndex() { return economyIndex; }
    public static double getInflationRate() { return inflationRate; }
    public static long getTotalMoneySupply() { return totalMoneySupply; }
    
    /**
     * 更新市场数据
     */
    public static void updateMarketData(Map<String, MarketItemInfo> data) {
        marketCache = new HashMap<>(data);
    }
    
    public static MarketItemInfo getMarketItem(String itemId) {
        return marketCache.get(itemId);
    }

    public static Map<String, MarketItemInfo> getMarketCache() {
        return marketCache;
    }
    
    /**
     * 更新每日定额
     */
    public static void updateDailyQuota(String item, int required, int progress, double reward) {
        dailyQuotaItem = item;
        dailyQuotaRequired = required;
        dailyQuotaProgress = progress;
        dailyQuotaReward = reward;
        quotaLastUpdate = System.currentTimeMillis();
    }
    
    public static String getDailyQuotaItem() { return dailyQuotaItem; }
    public static int getDailyQuotaRequired() { return dailyQuotaRequired; }
    public static int getDailyQuotaProgress() { return dailyQuotaProgress; }
    public static double getDailyQuotaReward() { return dailyQuotaReward; }
    
    /**
     * 检查数据是否过期
     */
    public static boolean isDataStale() {
        return System.currentTimeMillis() - lastServerSync > CACHE_TTL;
    }
    
    public static boolean isWholesaleDataStale() {
        return System.currentTimeMillis() - wholesaleLastUpdate > CACHE_TTL;
    }
    
    public static boolean isQuotaDataStale() {
        return System.currentTimeMillis() - quotaLastUpdate > CACHE_TTL;
    }
    
    /**
     * 标记同步完成
     */
    public static void markSynced() {
        lastServerSync = System.currentTimeMillis();
        syncPending = false;
    }
    
    public static void markSyncPending() {
        syncPending = true;
    }
    
    public static boolean isSyncPending() {
        return syncPending;
    }
    
    /**
     * 清空所有缓存
     */
    public static void clear() {
        wholesalePrices.clear();
        marketCache.clear();
        dailyQuotaItem = "";
        dailyQuotaRequired = 0;
        dailyQuotaProgress = 0;
        dailyQuotaReward = 0.0;
        lastServerSync = 0;
        syncPending = false;
    }
    
    /**
     * 市场物品信息 (简化版)
     */
    public static class MarketItemInfo {
        public final String itemId;
        public final String displayName;
        public final double basePrice;
        public final double currentPrice;
        public final double demand;
        public final double supply;
        
        public MarketItemInfo(String itemId, String displayName, double basePrice, 
                              double currentPrice, double demand, double supply) {
            this.itemId = itemId;
            this.displayName = displayName;
            this.basePrice = basePrice;
            this.currentPrice = currentPrice;
            this.demand = demand;
            this.supply = supply;
        }
    }
}
