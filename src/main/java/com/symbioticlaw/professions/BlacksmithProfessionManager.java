package com.symbioticlaw.professions;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 铁匠职业管理器
 * 核心定位：武器/护甲回收与金属精炼专家
 * 职业本质：持有《军工级金属重熔与武器改造许可证》的合法武装回收商
 * 
 * 设计逻辑：
 * - 铁匠收购的是已损坏的装备，通过拆解获得金属和部件
 * - 装备耐久越低，收购价越高（因为含有更多原始金属）
 * - 铁匠专用机器：Tetra工作台（锻造、修理、改造）
 */
public class BlacksmithProfessionManager {

    public enum BlacksmithItemTier {
        TIER_1_SCRAP,        // 废料/原材料 - 基础价格 $1-$5
        TIER_2_COMMON_GEAR,  // 普通装备 - $10-$50
        TIER_3_ENCHANTED,    // 附魔装备 - $50-$200
        TIER_4_RARE_GEAR,    // 稀有装备 - $200-$800
        TIER_5_LEGENDARY     // 传说/史诗装备 - $1000-$5000
    }

    // 基础价格映射
    private static final Map<String, Double> ITEM_BASE_PRICES = new HashMap<>();
    private static final Map<String, BlacksmithItemTier> ITEM_TIER_MAP = new HashMap<>();
    
    // 装备类型倍率
    private static final Map<String, Double> EQUIPMENT_TYPE_MULTIPLIERS = new HashMap<>();
    
    // 可接受的装备标签
    private static final Set<String> VALID_EQUIPMENT_TAGS = Stream.of(
        "minecraft:swords",
        "minecraft:axes", 
        "minecraft:pickaxes",
        "minecraft:shovels",
        "minecraft:hoes",
        "minecraft:armors",
        "forge:tools",
        "forge:armors",
        "tetra:modular_items"
    ).collect(Collectors.toSet());

    static {
        // ========== TIER 1: 原材料/废料 (Scrap) ==========
        // 基础金属原材料
        ITEM_BASE_PRICES.put("minecraft:iron_nugget", 0.5);
        ITEM_BASE_PRICES.put("minecraft:iron_ingot", 5.0);
        ITEM_BASE_PRICES.put("minecraft:gold_nugget", 2.0);
        ITEM_BASE_PRICES.put("minecraft:gold_ingot", 20.0);
        ITEM_BASE_PRICES.put("minecraft:copper_ingot", 3.0);
        ITEM_BASE_PRICES.put("minecraft:netherite_scrap", 200.0);
        ITEM_BASE_PRICES.put("minecraft:netherite_ingot", 800.0);
        
        ITEM_TIER_MAP.put("minecraft:iron_nugget", BlacksmithItemTier.TIER_1_SCRAP);
        ITEM_TIER_MAP.put("minecraft:iron_ingot", BlacksmithItemTier.TIER_1_SCRAP);
        ITEM_TIER_MAP.put("minecraft:gold_nugget", BlacksmithItemTier.TIER_1_SCRAP);
        ITEM_TIER_MAP.put("minecraft:gold_ingot", BlacksmithItemTier.TIER_1_SCRAP);
        ITEM_TIER_MAP.put("minecraft:copper_ingot", BlacksmithItemTier.TIER_1_SCRAP);
        ITEM_TIER_MAP.put("minecraft:netherite_scrap", BlacksmithItemTier.TIER_1_SCRAP);
        ITEM_TIER_MAP.put("minecraft:netherite_ingot", BlacksmithItemTier.TIER_1_SCRAP);

        // ========== TIER 2: 普通装备 (Common Gear) ==========
        // 木质/石质/铁制工具武器
        ITEM_BASE_PRICES.put("minecraft:wooden_sword", 5.0);
        ITEM_BASE_PRICES.put("minecraft:wooden_pickaxe", 5.0);
        ITEM_BASE_PRICES.put("minecraft:wooden_axe", 5.0);
        ITEM_BASE_PRICES.put("minecraft:wooden_shovel", 3.0);
        ITEM_BASE_PRICES.put("minecraft:wooden_hoe", 3.0);
        
        ITEM_BASE_PRICES.put("minecraft:stone_sword", 8.0);
        ITEM_BASE_PRICES.put("minecraft:stone_pickaxe", 8.0);
        ITEM_BASE_PRICES.put("minecraft:stone_axe", 8.0);
        ITEM_BASE_PRICES.put("minecraft:stone_shovel", 5.0);
        ITEM_BASE_PRICES.put("minecraft:stone_hoe", 5.0);
        
        ITEM_BASE_PRICES.put("minecraft:iron_sword", 25.0);
        ITEM_BASE_PRICES.put("minecraft:iron_pickaxe", 25.0);
        ITEM_BASE_PRICES.put("minecraft:iron_axe", 25.0);
        ITEM_BASE_PRICES.put("minecraft:iron_shovel", 15.0);
        ITEM_BASE_PRICES.put("minecraft:iron_hoe", 15.0);
        
        ITEM_BASE_PRICES.put("minecraft:leather_helmet", 15.0);
        ITEM_BASE_PRICES.put("minecraft:leather_chestplate", 25.0);
        ITEM_BASE_PRICES.put("minecraft:leather_leggings", 20.0);
        ITEM_BASE_PRICES.put("minecraft:leather_boots", 12.0);
        
        ITEM_BASE_PRICES.put("minecraft:chainmail_helmet", 30.0);
        ITEM_BASE_PRICES.put("minecraft:chainmail_chestplate", 50.0);
        ITEM_BASE_PRICES.put("minecraft:chainmail_leggings", 40.0);
        ITEM_BASE_PRICES.put("minecraft:chainmail_boots", 25.0);
        
        ITEM_BASE_PRICES.put("minecraft:iron_helmet", 40.0);
        ITEM_BASE_PRICES.put("minecraft:iron_chestplate", 65.0);
        ITEM_BASE_PRICES.put("minecraft:iron_leggings", 55.0);
        ITEM_BASE_PRICES.put("minecraft:iron_boots", 35.0);

        // 设置TIER 2标签
        Stream.of("wooden_sword", "wooden_pickaxe", "wooden_axe", "wooden_shovel", "wooden_hoe",
                  "stone_sword", "stone_pickaxe", "stone_axe", "stone_shovel", "stone_hoe",
                  "iron_sword", "iron_pickaxe", "iron_axe", "iron_shovel", "iron_hoe",
                  "leather_helmet", "leather_chestplate", "leather_leggings", "leather_boots",
                  "chainmail_helmet", "chainmail_chestplate", "chainmail_leggings", "chainmail_boots",
                  "iron_helmet", "iron_chestplate", "iron_leggings", "iron_boots")
            .forEach(item -> ITEM_TIER_MAP.put("minecraft:" + item, BlacksmithItemTier.TIER_2_COMMON_GEAR));

        // ========== TIER 3: 金质装备/附魔装备基础 (Enchanted) ==========
        ITEM_BASE_PRICES.put("minecraft:golden_sword", 40.0);
        ITEM_BASE_PRICES.put("minecraft:golden_pickaxe", 40.0);
        ITEM_BASE_PRICES.put("minecraft:golden_axe", 40.0);
        ITEM_BASE_PRICES.put("minecraft:golden_shovel", 25.0);
        ITEM_BASE_PRICES.put("minecraft:golden_hoe", 25.0);
        
        ITEM_BASE_PRICES.put("minecraft:golden_helmet", 60.0);
        ITEM_BASE_PRICES.put("minecraft:golden_chestplate", 100.0);
        ITEM_BASE_PRICES.put("minecraft:golden_leggings", 80.0);
        ITEM_BASE_PRICES.put("minecraft:golden_boots", 50.0);
        
        Stream.of("golden_sword", "golden_pickaxe", "golden_axe", "golden_shovel", "golden_hoe",
                  "golden_helmet", "golden_chestplate", "golden_leggings", "golden_boots")
            .forEach(item -> ITEM_TIER_MAP.put("minecraft:" + item, BlacksmithItemTier.TIER_3_ENCHANTED));

        // ========== TIER 4: 钻石装备 (Rare Gear) ==========
        ITEM_BASE_PRICES.put("minecraft:diamond_sword", 150.0);
        ITEM_BASE_PRICES.put("minecraft:diamond_pickaxe", 150.0);
        ITEM_BASE_PRICES.put("minecraft:diamond_axe", 150.0);
        ITEM_BASE_PRICES.put("minecraft:diamond_shovel", 100.0);
        ITEM_BASE_PRICES.put("minecraft:diamond_hoe", 100.0);
        
        ITEM_BASE_PRICES.put("minecraft:diamond_helmet", 250.0);
        ITEM_BASE_PRICES.put("minecraft:diamond_chestplate", 400.0);
        ITEM_BASE_PRICES.put("minecraft:diamond_leggings", 350.0);
        ITEM_BASE_PRICES.put("minecraft:diamond_boots", 200.0);
        
        Stream.of("diamond_sword", "diamond_pickaxe", "diamond_axe", "diamond_shovel", "diamond_hoe",
                  "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots")
            .forEach(item -> ITEM_TIER_MAP.put("minecraft:" + item, BlacksmithItemTier.TIER_4_RARE_GEAR));

        // ========== TIER 5: 下界合金装备 (Legendary) ==========
        ITEM_BASE_PRICES.put("minecraft:netherite_sword", 800.0);
        ITEM_BASE_PRICES.put("minecraft:netherite_pickaxe", 800.0);
        ITEM_BASE_PRICES.put("minecraft:netherite_axe", 800.0);
        ITEM_BASE_PRICES.put("minecraft:netherite_shovel", 600.0);
        ITEM_BASE_PRICES.put("minecraft:netherite_hoe", 600.0);
        
        ITEM_BASE_PRICES.put("minecraft:netherite_helmet", 1200.0);
        ITEM_BASE_PRICES.put("minecraft:netherite_chestplate", 2000.0);
        ITEM_BASE_PRICES.put("minecraft:netherite_leggings", 1700.0);
        ITEM_BASE_PRICES.put("minecraft:netherite_boots", 1000.0);
        
        Stream.of("netherite_sword", "netherite_pickaxe", "netherite_axe", "netherite_shovel", "netherite_hoe",
                  "netherite_helmet", "netherite_chestplate", "netherite_leggings", "netherite_boots")
            .forEach(item -> ITEM_TIER_MAP.put("minecraft:" + item, BlacksmithItemTier.TIER_5_LEGENDARY));

        // ========== 特殊装备 ==========
        ITEM_BASE_PRICES.put("minecraft:bow", 30.0);
        ITEM_BASE_PRICES.put("minecraft:crossbow", 80.0);
        ITEM_BASE_PRICES.put("minecraft:shield", 50.0);
        ITEM_BASE_PRICES.put("minecraft:trident", 500.0);
        ITEM_BASE_PRICES.put("minecraft:turtle_helmet", 150.0);
        ITEM_BASE_PRICES.put("minecraft:elytra", 2000.0);
        ITEM_BASE_PRICES.put("minecraft:mace", 1000.0);
        
        ITEM_TIER_MAP.put("minecraft:bow", BlacksmithItemTier.TIER_2_COMMON_GEAR);
        ITEM_TIER_MAP.put("minecraft:crossbow", BlacksmithItemTier.TIER_3_ENCHANTED);
        ITEM_TIER_MAP.put("minecraft:shield", BlacksmithItemTier.TIER_2_COMMON_GEAR);
        ITEM_TIER_MAP.put("minecraft:trident", BlacksmithItemTier.TIER_4_RARE_GEAR);
        ITEM_TIER_MAP.put("minecraft:turtle_helmet", BlacksmithItemTier.TIER_3_ENCHANTED);
        ITEM_TIER_MAP.put("minecraft:elytra", BlacksmithItemTier.TIER_5_LEGENDARY);
        ITEM_TIER_MAP.put("minecraft:mace", BlacksmithItemTier.TIER_4_RARE_GEAR);

        // ========== 装备类型倍率 ==========
        EQUIPMENT_TYPE_MULTIPLIERS.put("sword", 1.2);
        EQUIPMENT_TYPE_MULTIPLIERS.put("axe", 1.1);
        EQUIPMENT_TYPE_MULTIPLIERS.put("pickaxe", 1.0);
        EQUIPMENT_TYPE_MULTIPLIERS.put("shovel", 0.8);
        EQUIPMENT_TYPE_MULTIPLIERS.put("hoe", 0.7);
        EQUIPMENT_TYPE_MULTIPLIERS.put("helmet", 0.8);
        EQUIPMENT_TYPE_MULTIPLIERS.put("chestplate", 1.3);
        EQUIPMENT_TYPE_MULTIPLIERS.put("leggings", 1.1);
        EQUIPMENT_TYPE_MULTIPLIERS.put("boots", 0.7);
        EQUIPMENT_TYPE_MULTIPLIERS.put("shield", 1.0);
        EQUIPMENT_TYPE_MULTIPLIERS.put("bow", 1.0);
        EQUIPMENT_TYPE_MULTIPLIERS.put("crossbow", 1.3);
        EQUIPMENT_TYPE_MULTIPLIERS.put("trident", 1.5);
    }

    /**
     * 获取物品层级
     */
    public static BlacksmithItemTier getTierFor(ItemStack stack) {
        if (stack.isEmpty()) return null;
        
        Item item = stack.getItem();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        if (itemId == null) return null;
        
        String itemName = itemId.toString();

        // 检查直接物品映射
        if (ITEM_TIER_MAP.containsKey(itemName)) {
            return ITEM_TIER_MAP.get(itemName);
        }

        // 检查是否是Tetra模块化物品
        if (isTetraModularItem(stack)) {
            return getTetraItemTier(stack);
        }

        return null;
    }
    
    /**
     * 获取基础价格
     */
    public static double getBasePrice(ItemStack stack) {
        if (stack.isEmpty()) return 0.0;
        
        Item item = stack.getItem();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        if (itemId == null) return 0.0;
        
        String itemName = itemId.toString();
        
        // 检查直接价格映射
        if (ITEM_BASE_PRICES.containsKey(itemName)) {
            double basePrice = ITEM_BASE_PRICES.get(itemName);
            return applyDurabilityMultiplier(basePrice, stack);
        }
        
        // 处理Tetra物品
        if (isTetraModularItem(stack)) {
            return calculateTetraItemPrice(stack);
        }
        
        return 0.0;
    }

    /**
     * 根据耐久度调整价格
     * 铁匠收购损坏的装备是为了拆解回收金属
     * 耐久越低 = 剩余金属越多 = 价格越高
     */
    private static double applyDurabilityMultiplier(double basePrice, ItemStack stack) {
        if (!stack.isDamageableItem()) return basePrice;
        
        int maxDurability = stack.getMaxDamage();
        int currentDurability = maxDurability - stack.getDamageValue();
        double durabilityRatio = (double) currentDurability / maxDurability;
        
        // 耐久度越低，价格越高（最多翻倍）
        // 全新装备: 100% 价格
        // 损坏装备: 150% 价格（更多可回收金属）
        double multiplier = 1.0 + (1.0 - durabilityRatio) * 0.5;
        
        return basePrice * multiplier;
    }
    
    /**
     * 检查是否是铁匠可收购的物品
     */
    public static boolean isBlacksmithItem(ItemStack stack) {
        return getTierFor(stack) != null || isTetraModularItem(stack);
    }
    
    /**
     * 检查是否是高价值装备(T4或T5)
     */
    public static boolean isHighValueGear(ItemStack stack) {
        BlacksmithItemTier tier = getTierFor(stack);
        return tier == BlacksmithItemTier.TIER_4_RARE_GEAR 
            || tier == BlacksmithItemTier.TIER_5_LEGENDARY;
    }

    /**
     * 获取XP奖励
     * 铁匠XP基于物品价值: $50 = 1 XP
     */
    public static double getXpForSale(double saleAmount) {
        return saleAmount / 50.0;
    }

    /**
     * 获取层级名称
     */
    public static String getTierName(BlacksmithItemTier tier) {
        switch (tier) {
            case TIER_1_SCRAP: return "金属废料";
            case TIER_2_COMMON_GEAR: return "普通装备";
            case TIER_3_ENCHANTED: return "精制装备";
            case TIER_4_RARE_GEAR: return "稀有装备";
            case TIER_5_LEGENDARY: return "传说装备";
            default: return "未知";
        }
    }
    
    /**
     * 获取价格加成
     */
    public static double getPriceBonus(int level) {
        if (level >= 46) return 0.15;
        if (level >= 31) return 0.10;
        if (level >= 11) return 0.05;
        return 0.0;
    }
    
    /**
     * 获取税收减免（铁匠无减免，专注于装备处理）
     */
    public static double getTaxRelief(int level) {
        // 铁匠专注于装备回收，不提供税收减免
        return 0.0;
    }
    
    /**
     * 获取铁匠头衔
     */
    public static String getTitle(int level) {
        if (level >= 46) return "火神 (Vulcan)";
        if (level >= 31) return "战争机器 (Warmachine)";
        if (level >= 11) return "熔炉工头 (Smelter)";
        return "敲打者 (Striker)";
    }
    
    /**
     * 检查是否可以拆解某物品（需要达到一定等级）
     */
    public static boolean canSalvage(ItemStack stack, int level) {
        BlacksmithItemTier tier = getTierFor(stack);
        if (tier == null) return false;
        
        switch (tier) {
            case TIER_1_SCRAP:
            case TIER_2_COMMON_GEAR:
                return true; // 任意等级
            case TIER_3_ENCHANTED:
                return level >= 10;
            case TIER_4_RARE_GEAR:
                return level >= 25;
            case TIER_5_LEGENDARY:
                return level >= 40;
            default:
                return false;
        }
    }
    
    // ========== Tetra 模组联动 ==========
    
    /**
     * 检查是否是Tetra物品 (别名方法)
     */
    public static boolean isTetraItem(ItemStack stack) {
        return isTetraModularItem(stack);
    }
    
    /**
     * 检查是否是Tetra模块化物品
     */
    public static boolean isTetraModularItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) return false;
        
        // 检查mod id
        if (!itemId.getNamespace().equals("tetra")) return false;
        
        // 检查是否是模块化物品
        String path = itemId.getPath();
        return path.contains("modular") || 
               path.contains("double") || 
               path.contains("single") ||
               path.contains("sword") ||
               path.contains("toolbelt");
    }
    
    /**
     * 计算Tetra物品价格
     * 基于物品稀有度和模块复杂度
     */
    private static double calculateTetraItemPrice(ItemStack stack) {
        // 基础价格
        double basePrice = 100.0;
        
        // 根据稀有度调整
        Rarity rarity = stack.getRarity();
        double rarityMultiplier = switch (rarity) {
            case COMMON -> 1.0;
            case UNCOMMON -> 2.0;
            case RARE -> 5.0;
            case EPIC -> 15.0;
            default -> 1.0;
        };
        
        // 根据NBT数据判断复杂度（简化处理）
        double complexityBonus = 1.0;
        if (stack.hasTag()) {
            var tag = stack.getTag();
            if (tag != null && tag.contains("modules")) {
                complexityBonus = 1.5; // 有模块的物品价值更高
            }
        }
        
        return basePrice * rarityMultiplier * complexityBonus;
    }
    
    /**
     * 获取Tetra物品层级
     */
    private static BlacksmithItemTier getTetraItemTier(ItemStack stack) {
        Rarity rarity = stack.getRarity();
        return switch (rarity) {
            case COMMON -> BlacksmithItemTier.TIER_2_COMMON_GEAR;
            case UNCOMMON -> BlacksmithItemTier.TIER_3_ENCHANTED;
            case RARE -> BlacksmithItemTier.TIER_4_RARE_GEAR;
            case EPIC -> BlacksmithItemTier.TIER_5_LEGENDARY;
            default -> BlacksmithItemTier.TIER_2_COMMON_GEAR;
        };
    }
    
    /**
     * 获取铁匠每日定额选项
     */
    public static Map<String, Integer> getDailyQuotaOptions() {
        Map<String, Integer> quotas = new HashMap<>();
        // 损坏的装备回收需求
        quotas.put("minecraft:iron_sword", 8);
        quotas.put("minecraft:iron_helmet", 4);
        quotas.put("minecraft:iron_chestplate", 2);
        quotas.put("minecraft:iron_leggings", 3);
        quotas.put("minecraft:iron_boots", 4);
        quotas.put("minecraft:shield", 4);
        quotas.put("minecraft:bow", 6);
        quotas.put("minecraft:diamond_pickaxe", 2);
        quotas.put("minecraft:iron_pickaxe", 6);
        quotas.put("minecraft:iron_axe", 6);
        return quotas;
    }
    
    // ================== Tetra 锤子检测 ==================
    
    /**
     * 检查物品是否是Tetra锤子
     * 铁匠的专业工具，非铁匠无法使用
     */
    public static boolean isTetraHammer(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) return false;
        
        // 检查是否是Tetra模组物品
        if (!itemId.getNamespace().equals("tetra")) return false;
        
        String path = itemId.getPath().toLowerCase();
        
        // 匹配Tetra锤子相关物品
        return path.contains("hammer") || 
               path.contains("mallet") ||
               (path.contains("tool") && path.contains("metal"));
    }
    
    /**
     * 获取Tetra锤子的等级要求
     */
    public static int getHammerRequiredLevel(ItemStack stack) {
        if (!isTetraHammer(stack)) return 0;
        
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) return 0;
        
        String path = itemId.getPath().toLowerCase();
        
        // 高级锤子需要更高等级
        if (path.contains("netherite") || path.contains("diamond")) {
            return 25;
        } else if (path.contains("iron") || path.contains("steel")) {
            return 10;
        }
        
        // 基础锤子（石质、木质）
        return 1;
    }
}
