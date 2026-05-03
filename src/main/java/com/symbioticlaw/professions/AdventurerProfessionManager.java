package com.symbioticlaw.professions;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 冒险家职业管理器
 * 核心定位：废土清剿者、遗迹破译者与高危区域资产回收商
 * 职业本质：持有"广域勘探与战利品回收许可证"的资本探险家
 */
public class AdventurerProfessionManager {

    public enum AdventurerItemTier {
        TIER_1_BIOMASS,      // 基础生物质 - 极致贱价 $0.1-$0.5
        TIER_2_COMBAT,       // 进阶战斗物资 - $10-$20
        TIER_3_DIMENSIONAL,  // 异次元战略物资 - $50-$300
        TIER_4_ANCIENT,      // 远古遗迹产物 - $500-$5000
        TIER_5_BOSS          // Boss战利品 - $8000-$15000
    }

    // 基础价格映射
    private static final Map<String, Double> ITEM_BASE_PRICES = new HashMap<>();
    private static final Map<String, AdventurerItemTier> ITEM_TIER_MAP = new HashMap<>();
    private static final Map<TagKey<Item>, AdventurerItemTier> TAG_TIER_MAP = new HashMap<>();
    
    // Mod ID 映射到定价层级
    private static final Map<String, Double> MOD_PRICING = new HashMap<>();

    static {
        // ========== TIER 1: 基础生物质 (Low Risk) ==========
        // 极致贱价 - 可通过刷怪塔量产
        ITEM_BASE_PRICES.put("minecraft:rotten_flesh", 0.1);
        ITEM_BASE_PRICES.put("minecraft:string", 0.1);
        ITEM_BASE_PRICES.put("minecraft:bone", 0.5);
        ITEM_BASE_PRICES.put("minecraft:spider_eye", 0.5);
        ITEM_BASE_PRICES.put("minecraft:arrow", 0.5);
        
        ITEM_TIER_MAP.put("minecraft:rotten_flesh", AdventurerItemTier.TIER_1_BIOMASS);
        ITEM_TIER_MAP.put("minecraft:string", AdventurerItemTier.TIER_1_BIOMASS);
        ITEM_TIER_MAP.put("minecraft:bone", AdventurerItemTier.TIER_1_BIOMASS);
        ITEM_TIER_MAP.put("minecraft:spider_eye", AdventurerItemTier.TIER_1_BIOMASS);
        ITEM_TIER_MAP.put("minecraft:arrow", AdventurerItemTier.TIER_1_BIOMASS);

        // ========== TIER 2: 进阶战斗物资 (Medium Risk) ==========
        // 需要战斗互动，无法全自动量产
        ITEM_BASE_PRICES.put("minecraft:gunpowder", 10.0);
        ITEM_BASE_PRICES.put("minecraft:slime_ball", 15.0);
        ITEM_BASE_PRICES.put("minecraft:phantom_membrane", 20.0);
        
        ITEM_TIER_MAP.put("minecraft:gunpowder", AdventurerItemTier.TIER_2_COMBAT);
        ITEM_TIER_MAP.put("minecraft:slime_ball", AdventurerItemTier.TIER_2_COMBAT);
        ITEM_TIER_MAP.put("minecraft:phantom_membrane", AdventurerItemTier.TIER_2_COMBAT);

        // ========== TIER 3: 异次元战略物资 (High Risk) ==========
        // 必须承担维度关税和异界里程费
        ITEM_BASE_PRICES.put("minecraft:blaze_rod", 80.0);
        ITEM_BASE_PRICES.put("minecraft:ender_pearl", 50.0);
        ITEM_BASE_PRICES.put("minecraft:ghast_tear", 150.0);
        ITEM_BASE_PRICES.put("minecraft:shulker_shell", 300.0);
        
        ITEM_TIER_MAP.put("minecraft:blaze_rod", AdventurerItemTier.TIER_3_DIMENSIONAL);
        ITEM_TIER_MAP.put("minecraft:ender_pearl", AdventurerItemTier.TIER_3_DIMENSIONAL);
        ITEM_TIER_MAP.put("minecraft:ghast_tear", AdventurerItemTier.TIER_3_DIMENSIONAL);
        ITEM_TIER_MAP.put("minecraft:shulker_shell", AdventurerItemTier.TIER_3_DIMENSIONAL);

        // ========== TIER 4: 远古遗迹产物 (Extreme Risk) ==========
        // 不可再生，深入远古城市或危险地牢
        ITEM_BASE_PRICES.put("minecraft:echo_shard", 500.0);
        ITEM_BASE_PRICES.put("minecraft:enchanted_golden_apple", 5000.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_13", 100.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_cat", 100.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_blocks", 100.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_chirp", 100.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_far", 100.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_mall", 100.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_mellohi", 100.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_stal", 100.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_strad", 100.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_ward", 100.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_11", 100.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_wait", 100.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_otherside", 200.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_5", 200.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_pigstep", 200.0);
        ITEM_BASE_PRICES.put("minecraft:music_disc_relic", 200.0);
        ITEM_BASE_PRICES.put("minecraft:iron_horse_armor", 100.0);
        ITEM_BASE_PRICES.put("minecraft:golden_horse_armor", 150.0);
        ITEM_BASE_PRICES.put("minecraft:diamond_horse_armor", 200.0);
        
        ITEM_TIER_MAP.put("minecraft:echo_shard", AdventurerItemTier.TIER_4_ANCIENT);
        ITEM_TIER_MAP.put("minecraft:enchanted_golden_apple", AdventurerItemTier.TIER_4_ANCIENT);
        
        // Smithing Templates - 锻造模板 $1500-$3000
        // 使用标签匹配
        TAG_TIER_MAP.put(createTag("minecraft:trim_templates"), AdventurerItemTier.TIER_4_ANCIENT);
        
        // Nether Star / Dragon Egg
        ITEM_BASE_PRICES.put("minecraft:nether_star", 8000.0);
        ITEM_BASE_PRICES.put("minecraft:dragon_egg", 8000.0);
        ITEM_TIER_MAP.put("minecraft:nether_star", AdventurerItemTier.TIER_5_BOSS);
        ITEM_TIER_MAP.put("minecraft:dragon_egg", AdventurerItemTier.TIER_5_BOSS);

        // ========== TIER 5: Boss战利品 (Cataclysm & Others) ==========
        // Cataclysm模组Boss
        MOD_PRICING.put("cataclysm", 10000.0); // Base price for cataclysm items
        
        // Twilight Forest - 暮色森林
        MOD_PRICING.put("twilightforest", 3000.0);
        
        // Blue Skies - 蔚蓝浩空
        MOD_PRICING.put("blue_skies", 3000.0);
        
        // Aquamirae - 水下幻象
        MOD_PRICING.put("aquamirae", 2500.0);
        
        // Deeper & Darker - 更深更暗
        MOD_PRICING.put("deeperdarker", 2000.0);
        
        // Undergarden - 深暗之园
        MOD_PRICING.put("undergarden", 1500.0);
    }

    private static TagKey<Item> createTag(String tagName) {
        int idx = tagName.indexOf(':');
        if (idx <= 0 || idx >= tagName.length() - 1) {
            return TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "air"));
        }
        return TagKey.create(Registries.ITEM, new ResourceLocation(tagName.substring(0, idx), tagName.substring(idx + 1)));
    }

    /**
     * Get the tier for an item
     */
    public static AdventurerItemTier getTierFor(ItemStack stack) {
        if (stack.isEmpty()) return null;
        
        Item item = stack.getItem();
        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
        if (itemId == null) return null;
        
        String itemName = itemId.toString();
        String modId = itemId.getNamespace();

        // Check direct item map first
        if (ITEM_TIER_MAP.containsKey(itemName)) {
            return ITEM_TIER_MAP.get(itemName);
        }

        // Check tags
        for (Map.Entry<TagKey<Item>, AdventurerItemTier> entry : TAG_TIER_MAP.entrySet()) {
            if (stack.is(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Check by mod ID for modded dimensions
        if (MOD_PRICING.containsKey(modId)) {
            // Determine tier based on rarity
            Rarity rarity = item.getRarity(stack);
            if (rarity == Rarity.EPIC) {
                return AdventurerItemTier.TIER_5_BOSS;
            } else if (rarity == Rarity.RARE) {
                return AdventurerItemTier.TIER_4_ANCIENT;
            } else {
                return AdventurerItemTier.TIER_3_DIMENSIONAL;
            }
        }

        return null;
    }
    
    /**
     * Get base price for an item
     */
    public static double getBasePrice(ItemStack stack) {
        if (stack.isEmpty()) return 0.0;
        
        Item item = stack.getItem();
        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
        if (itemId == null) return 0.0;
        
        String itemName = itemId.toString();
        String modId = itemId.getNamespace();
        
        // Check direct price map
        if (ITEM_BASE_PRICES.containsKey(itemName)) {
            return ITEM_BASE_PRICES.get(itemName);
        }
        
        // Check by mod ID for special pricing
        if (MOD_PRICING.containsKey(modId)) {
            double baseModPrice = MOD_PRICING.get(modId);
            
            // Adjust based on item name patterns
            String itemPath = itemId.getPath().toLowerCase();
            
            // Boss trophies and cores
            if (itemPath.contains("trophy") || itemPath.contains("core") || 
                itemPath.contains("shield") || itemPath.contains("ignis") ||
                itemPath.contains("leviathan")) {
                return baseModPrice * 1.5; // $15000 for cataclysm, etc.
            }
            
            // Scales and tears
            if (itemPath.contains("scale") || itemPath.contains("tear") ||
                itemPath.contains("shard") || itemPath.contains("fragment")) {
                return baseModPrice * 0.5; // $1500-2500
            }
            
            // Regular drops
            return baseModPrice * 0.1; // $200-1000
        }
        
        return 0.0;
    }

    /**
     * Get XP for selling items
     * Formula: $10 of sale = 1 XP
     */
    public static double getXpForSale(double saleAmount) {
        return saleAmount / 10.0;
    }
    
    /**
     * Check if item is an adventurer item
     */
    public static boolean isAdventurerItem(ItemStack stack) {
        return getTierFor(stack) != null;
    }
    
    /**
     * Check if item is a high-value treasure (T4 or T5)
     */
    public static boolean isHighValueTreasure(ItemStack stack) {
        AdventurerItemTier tier = getTierFor(stack);
        return tier == AdventurerItemTier.TIER_4_ANCIENT || tier == AdventurerItemTier.TIER_5_BOSS;
    }
    
    /**
     * Get tier name for display
     */
    public static String getTierName(AdventurerItemTier tier) {
        switch (tier) {
            case TIER_1_BIOMASS: return "基础生物质";
            case TIER_2_COMBAT: return "战斗物资";
            case TIER_3_DIMENSIONAL: return "异次元物资";
            case TIER_4_ANCIENT: return "远古遗物";
            case TIER_5_BOSS: return "Boss战利品";
            default: return "未知";
        }
    }
    
    /**
     * Get the tax relief percentage based on adventurer level
     */
    public static double getTaxRelief(int level) {
        if (level >= 46) return 0.30; // 30%减免
        if (level >= 31) return 0.20; // 20%减免
        if (level >= 11) return 0.10; // 10%减免
        return 0.0; // 无减免
    }
    
    /**
     * Get price bonus based on level
     */
    public static double getPriceBonus(int level) {
        if (level >= 46) return 0.15;
        if (level >= 31) return 0.10;
        if (level >= 11) return 0.05;
        return 0.0;
    }
    
    /**
     * Get adventurer title based on level
     */
    public static String getTitle(int level) {
        if (level >= 46) return "位面先锋 (Vanguard)";
        if (level >= 31) return "深潜探员 (Delver)";
        if (level >= 11) return "废土游侠 (Wanderer)";
        return "清道夫 (Scavenger)";
    }
    
    // ================== 维度关税查询 ==================
    
    /**
     * 维度类型枚举
     */
    public enum DimensionType {
        OVERWORLD("主世界", 0, 0),
        NETHER("下界", 1, 300),
        END("末地", 1, 500),
        TWILIGHT_FOREST("暮色森林", 2, 400),
        BLUE_SKIES("蔚蓝浩空", 2, 400),
        UNDERGARDEN("深暗之园", 3, 600),
        DEEPER_DARKER("更深更暗", 4, 1000),
        OTHER_MODDED("其他模组维度", 2, 400);
        
        public final String displayName;
        public final int dangerTier;
        public final double baseEntryTax;
        
        DimensionType(String displayName, int dangerTier, double baseEntryTax) {
            this.displayName = displayName;
            this.dangerTier = dangerTier;
            this.baseEntryTax = baseEntryTax;
        }
    }
    
    /**
     * 识别维度类型
     */
    public static DimensionType getDimensionType(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {
        String dimPath = dimension.location().getPath().toLowerCase();
        String dimNamespace = dimension.location().getNamespace().toLowerCase();
        
        if (dimension.equals(net.minecraft.world.level.Level.OVERWORLD)) {
            return DimensionType.OVERWORLD;
        } else if (dimension.equals(net.minecraft.world.level.Level.NETHER)) {
            return DimensionType.NETHER;
        } else if (dimension.equals(net.minecraft.world.level.Level.END)) {
            return DimensionType.END;
        } else if (dimPath.contains("twilightforest") || dimPath.contains("twilight_forest")) {
            return DimensionType.TWILIGHT_FOREST;
        } else if (dimNamespace.contains("blue_skies") || 
                   dimPath.contains("everbright") || 
                   dimPath.contains("everdawn")) {
            return DimensionType.BLUE_SKIES;
        } else if (dimPath.contains("undergarden")) {
            return DimensionType.UNDERGARDEN;
        } else if (dimPath.contains("deeperdarker") || dimPath.contains("otherside")) {
            return DimensionType.DEEPER_DARKER;
        } else {
            return DimensionType.OTHER_MODDED;
        }
    }
    
    /**
     * 获取维度内的里程费率
     */
    public static double getDimensionalMileageRate(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {
        DimensionType type = getDimensionType(dimension);
        return switch (type) {
            case NETHER -> com.symbioticlaw.Config.MILEAGE_RATE_NETHER.get();
            case END -> com.symbioticlaw.Config.MILEAGE_RATE_END.get();
            case TWILIGHT_FOREST, BLUE_SKIES -> 0.8;
            case UNDERGARDEN -> 1.0;
            case DEEPER_DARKER -> 1.5;
            default -> com.symbioticlaw.Config.MILEAGE_RATE_MODDED.get();
        };
    }
    
    /**
     * 获取维度进入税（考虑冒险家等级减免）
     */
    public static double getDimensionEntryTax(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension, int adventurerLevel) {
        DimensionType type = getDimensionType(dimension);
        if (type == DimensionType.OVERWORLD) return 0;
        
        double baseTax = switch (type) {
            case NETHER -> com.symbioticlaw.Config.DIMENSION_TAX_NETHER.get();
            case END -> com.symbioticlaw.Config.DIMENSION_TAX_END.get();
            case TWILIGHT_FOREST -> com.symbioticlaw.Config.DIMENSION_TAX_TWILIGHT_FOREST.get();
            case BLUE_SKIES -> com.symbioticlaw.Config.DIMENSION_TAX_BLUE_SKIES.get();
            case UNDERGARDEN -> com.symbioticlaw.Config.DIMENSION_TAX_UNDERGARDEN.get();
            case DEEPER_DARKER -> com.symbioticlaw.Config.DIMENSION_TAX_DEEPER_DARKER.get();
            default -> com.symbioticlaw.Config.DIMENSION_TAX_MODDED.get();
        };
        
        // 应用冒险家减免
        double relief = getTaxRelief(adventurerLevel);
        return baseTax * (1 - relief);
    }
}
