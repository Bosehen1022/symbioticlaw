package com.symbioticlaw.system;

import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.JobType;
import com.symbioticlaw.data.MarketData;
import com.symbioticlaw.data.MarketItem;
import com.symbioticlaw.data.WorldData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * 厨师国家粮仓批发系统
 * 
 * 规格书要求：
 * - 厨师拥有专属的批发进货页面
 * - 价格 = 当前市场收购价 × 1.15 (+15% 服务费)
 * - 库存来源：农夫卖给核心的物资
 * 
 * 核心逻辑：
 * - 农夫把牛肉卖给核心赚 $10
 * - 厨师从核心买回牛肉需要 $11.5
 * - 这 15% 的差价是核心收取的"仓储物流费"
 * - 鼓励厨师优先找农夫私下交易（双赢），核心只是保底供应商
 */
public class ChefWholesaleSystem {
    
    // 批发价格倍率 (15% 服务费)
    private static final double WHOLESALE_MARKUP = 1.15;
    
    // 厨师可批发购买的物品白名单 (农夫产出的原材料)
    private static final Map<String, Double> WHOLESALE_ITEMS = new HashMap<>();
    
    static {
        // 肉类原材料
        WHOLESALE_ITEMS.put("minecraft:beef", 10.0);
        WHOLESALE_ITEMS.put("minecraft:porkchop", 8.0);
        WHOLESALE_ITEMS.put("minecraft:chicken", 5.0);
        WHOLESALE_ITEMS.put("minecraft:mutton", 7.0);
        WHOLESALE_ITEMS.put("minecraft:rabbit", 4.0);
        WHOLESALE_ITEMS.put("minecraft:egg", 2.0);
        
        // 农作物
        WHOLESALE_ITEMS.put("minecraft:wheat", 3.0);
        WHOLESALE_ITEMS.put("minecraft:carrot", 2.5);
        WHOLESALE_ITEMS.put("minecraft:potato", 2.0);
        WHOLESALE_ITEMS.put("minecraft:beetroot", 2.0);
        WHOLESALE_ITEMS.put("minecraft:cabbage", 3.0); // Farmer's Delight
        WHOLESALE_ITEMS.put("farmersdelight:cabbage", 3.0);
        WHOLESALE_ITEMS.put("farmersdelight:tomato", 2.5);
        WHOLESALE_ITEMS.put("farmersdelight:onion", 2.0);
        WHOLESALE_ITEMS.put("farmersdelight:rice", 4.0);
        
        // 其他食材
        WHOLESALE_ITEMS.put("minecraft:apple", 2.0);
        WHOLESALE_ITEMS.put("minecraft:sweet_berries", 1.5);
        WHOLESALE_ITEMS.put("minecraft:glow_berries", 3.0);
        WHOLESALE_ITEMS.put("minecraft:melon_slice", 1.0);
        WHOLESALE_ITEMS.put("minecraft:pumpkin", 5.0);
        WHOLESALE_ITEMS.put("minecraft:sugar", 3.0);
        WHOLESALE_ITEMS.put("minecraft:brown_mushroom", 2.0);
        WHOLESALE_ITEMS.put("minecraft:red_mushroom", 2.0);
        WHOLESALE_ITEMS.put("minecraft:kelp", 1.0);
        WHOLESALE_ITEMS.put("minecraft:dried_kelp", 2.0);
        
        // 农夫乐事特色食材
        WHOLESALE_ITEMS.put("farmersdelight:pumpkin_slice", 1.5);
        WHOLESALE_ITEMS.put("farmersdelight:carrot_crate", 25.0);
        WHOLESALE_ITEMS.put("farmersdelight:potato_crate", 20.0);
        WHOLESALE_ITEMS.put("farmersdelight:beetroot_crate", 20.0);
        WHOLESALE_ITEMS.put("farmersdelight:cabbage_crate", 28.0);
        WHOLESALE_ITEMS.put("farmersdelight:tomato_crate", 23.0);
        WHOLESALE_ITEMS.put("farmersdelight:onion_crate", 18.0);
        WHOLESALE_ITEMS.put("farmersdelight:rice_bale", 40.0);
    }
    
    /**
     * 检查玩家是否是厨师
     */
    private static boolean isChef(ServerPlayer player) {
        var playerData = player.getCapability(PlayerDataCapability.INSTANCE).orElse(null);
        return playerData != null && playerData.getProfession().toJobType() == JobType.CHEF;
    }
    
    /**
     * 获取批发价格（服务端版本）
     * 公式：市场收购价 × 1.15
     */
    public static double getWholesalePrice(String itemId, ServerPlayer player) {
        if (player == null || player.serverLevel() == null) {
            // 回退到默认价格
            return WHOLESALE_ITEMS.getOrDefault(itemId, 5.0) * WHOLESALE_MARKUP;
        }
        
        WorldData worldData = WorldData.get(player.serverLevel());
        if (worldData == null) {
            return WHOLESALE_ITEMS.getOrDefault(itemId, 5.0) * WHOLESALE_MARKUP;
        }
        
        MarketData marketData = worldData.getMarketData();
        MarketItem marketItem = marketData.getMarketItem(itemId);
        
        double basePrice;
        if (marketItem != null) {
            // 使用当前市场收购价
            basePrice = marketItem.basePrice;
        } else {
            // 使用默认价格
            basePrice = WHOLESALE_ITEMS.getOrDefault(itemId, 5.0);
        }
        
        return basePrice * WHOLESALE_MARKUP;
    }
    
    /**
     * 检查物品是否可批发
     */
    public static boolean isWholesaleAvailable(String itemId) {
        return WHOLESALE_ITEMS.containsKey(itemId);
    }
    
    /**
     * 获取所有可批发物品（旧版本，客户端请勿使用）
     */
    public static Map<String, Double> getWholesaleCatalog() {
        Map<String, Double> catalog = new HashMap<>();
        for (String itemId : WHOLESALE_ITEMS.keySet()) {
            catalog.put(itemId, WHOLESALE_ITEMS.getOrDefault(itemId, 5.0) * WHOLESALE_MARKUP);
        }
        return catalog;
    }
    
    /**
     * 获取所有可批发物品（服务端版本，带正确价格）
     */
    public static Map<String, Double> getWholesaleCatalog(ServerPlayer player) {
        Map<String, Double> catalog = new HashMap<>();
        for (String itemId : WHOLESALE_ITEMS.keySet()) {
            catalog.put(itemId, getWholesalePrice(itemId, player));
        }
        return catalog;
    }
    
    /**
     * 执行批发购买
     * 
     * @param player 购买的厨师
     * @param itemId 物品ID
     * @param amount 数量
     * @return 是否成功
     */
    public static boolean purchaseWholesale(ServerPlayer player, String itemId, int amount) {
        if (!isChef(player)) {
            player.sendSystemMessage(Component.literal("§c[🚫] 只有厨师才能访问国家粮仓。"));
            return false;
        }
        
        if (!isWholesaleAvailable(itemId)) {
            player.sendSystemMessage(Component.literal("§c[🚫] 该物品不在国家粮仓供应范围内。"));
            return false;
        }
        
        var playerData = player.getCapability(PlayerDataCapability.INSTANCE).orElse(null);
        if (playerData == null) return false;
        
        double unitPrice = getWholesalePrice(itemId, player);
        double totalCost = unitPrice * amount;
        
        // 检查余额
        if (playerData.getBalance() < totalCost) {
            player.sendSystemMessage(Component.literal(
                String.format("§c[🚫] 余额不足。需要 $%.2f，当前余额 $%.2f", 
                    totalCost, playerData.getBalance())));
            return false;
        }
        
        // 检查背包空间
        net.minecraft.resources.ResourceLocation itemRl = net.minecraft.resources.ResourceLocation.tryParse(itemId);
        Item item = itemRl != null ? ForgeRegistries.ITEMS.getValue(itemRl) : null;
        if (item == null) {
            player.sendSystemMessage(Component.literal("§c[🚫] 物品不存在。"));
            return false;
        }
        
        ItemStack stack = new ItemStack(item, amount);
        if (!player.getInventory().add(stack)) {
            player.sendSystemMessage(Component.literal("§c[🚫] 背包空间不足。"));
            return false;
        }
        
        // 扣除费用
        playerData.setBalance(playerData.getBalance() - totalCost);
        
        // 发送确认消息
        player.sendSystemMessage(Component.literal(
            String.format("§a[批发进货] 购买 %d x %s，花费 $%.2f (含15%%服务费)", 
                amount, 
                stack.getDisplayName().getString(),
                totalCost)));
        
        // 详细费用说明
        double baseCost = totalCost / WHOLESALE_MARKUP;
        double serviceFee = totalCost - baseCost;
        player.sendSystemMessage(Component.literal(
            String.format("§7明细: 基础价 $%.2f + 仓储物流费 $%.2f", 
                baseCost, serviceFee)));
        
        return true;
    }
    
    /**
     * 显示批发目录
     */
    public static void displayWholesaleCatalog(ServerPlayer player) {
        if (!isChef(player)) {
            player.sendSystemMessage(Component.literal("§c[🚫] 只有厨师才能查看国家粮仓。"));
            return;
        }
        
        player.sendSystemMessage(Component.literal("§b═══════════════════════════════════════"));
        player.sendSystemMessage(Component.literal("§b[国家粮仓批发目录] §7(价格含15%服务费)"));
        player.sendSystemMessage(Component.literal(""));
        
        Map<String, Double> catalog = getWholesaleCatalog(player);
        for (Map.Entry<String, Double> entry : catalog.entrySet()) {
            String itemId = entry.getKey();
            double price = entry.getValue();
            double basePrice = price / WHOLESALE_MARKUP;
            
            // 获取物品显示名称
            net.minecraft.resources.ResourceLocation itemRl = net.minecraft.resources.ResourceLocation.tryParse(itemId);
            Item item = itemRl != null ? ForgeRegistries.ITEMS.getValue(itemRl) : null;
            String itemName = item != null ? item.getDescription().getString() : itemId;
            
            player.sendSystemMessage(Component.literal(
                String.format("§7%s: §f$%.2f §7(农夫收购价: $%.2f)", 
                    itemName, price, basePrice)));
        }
        
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§e提示: 这些价格比直接从农夫购买贵15%，"));
        player.sendSystemMessage(Component.literal("§e建议优先与农夫私下交易以获得更优价格。"));
        player.sendSystemMessage(Component.literal("§b═══════════════════════════════════════"));
    }
    
    /**
     * 获取批发系统的提示信息
     */
    public static String getWholesaleTip() {
        return "厨师特权: 从国家粮仓批发原材料，价格 = 市场价 × 1.15";
    }
}
