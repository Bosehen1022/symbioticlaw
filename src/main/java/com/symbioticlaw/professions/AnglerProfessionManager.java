package com.symbioticlaw.professions;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 渔夫职业管理器
 * 核心定位：海洋废品回收与附魔书供应商
 * 职业本质：持有"海洋打捞许可证"的清洁工
 */
public class AnglerProfessionManager {

    public enum AnglerItemTier {
        TIER_1_BIOMASS,      // 生鳕鱼、生鲑鱼 - 0.01 XP
        TIER_2_TRASH,        // 腐肉、线、骨头、水瓶 - 0 XP
        TIER_3_SPECIAL,      // 河豚、热带鱼、墨囊 - 0.5 XP
        TIER_4_TREASURE,     // 海晶碎片、海晶砂粒 - 2.0 XP
        TIER_5_RELIC         // 鹦鹉螺壳、命名牌、马鞍 - 25.0 XP
    }

    private static final Map<String, AnglerItemTier> ITEM_TIER_MAP = new HashMap<>();
    private static final Map<TagKey<Item>, AnglerItemTier> TAG_TIER_MAP = new HashMap<>();
    private static final Map<AnglerItemTier, Double> TIER_XP_MAP = new HashMap<>();
    
    // 拒收物品（真正的垃圾）
    private static final Set<String> REJECTED_ITEMS = new HashSet<>();

    static {
        // Initialize XP values per tier
        TIER_XP_MAP.put(AnglerItemTier.TIER_1_BIOMASS, 0.01);   // 100 fish = 1 XP
        TIER_XP_MAP.put(AnglerItemTier.TIER_2_TRASH, 0.0);      // No XP
        TIER_XP_MAP.put(AnglerItemTier.TIER_3_SPECIAL, 0.5);    // 2 items = 1 XP
        TIER_XP_MAP.put(AnglerItemTier.TIER_4_TREASURE, 2.0);   // Per item
        TIER_XP_MAP.put(AnglerItemTier.TIER_5_RELIC, 25.0);     // Per item - jackpot!

        // TIER 1 - Biomass (Barely worth anything)
        ITEM_TIER_MAP.put("minecraft:cod", AnglerItemTier.TIER_1_BIOMASS);
        ITEM_TIER_MAP.put("minecraft:salmon", AnglerItemTier.TIER_1_BIOMASS);
        TAG_TIER_MAP.put(createTag("forge:raw_fishes/cod"), AnglerItemTier.TIER_1_BIOMASS);
        TAG_TIER_MAP.put(createTag("forge:raw_fishes/salmon"), AnglerItemTier.TIER_1_BIOMASS);

        // TIER 2 - Industrial Trash (No XP, system doesn't want to handle)
        ITEM_TIER_MAP.put("minecraft:rotten_flesh", AnglerItemTier.TIER_2_TRASH);
        ITEM_TIER_MAP.put("minecraft:string", AnglerItemTier.TIER_2_TRASH);
        ITEM_TIER_MAP.put("minecraft:bone", AnglerItemTier.TIER_2_TRASH);
        ITEM_TIER_MAP.put("minecraft:water_bottle", AnglerItemTier.TIER_2_TRASH);
        ITEM_TIER_MAP.put("minecraft:stick", AnglerItemTier.TIER_2_TRASH);
        ITEM_TIER_MAP.put("minecraft:leather_boots", AnglerItemTier.TIER_2_TRASH);
        
        // TIER 3 - Special Samples (Some value)
        ITEM_TIER_MAP.put("minecraft:pufferfish", AnglerItemTier.TIER_3_SPECIAL);
        ITEM_TIER_MAP.put("minecraft:tropical_fish", AnglerItemTier.TIER_3_SPECIAL);
        ITEM_TIER_MAP.put("minecraft:ink_sac", AnglerItemTier.TIER_3_SPECIAL);

        // TIER 4 - Rare Treasure (Good value)
        ITEM_TIER_MAP.put("minecraft:prismarine_shard", AnglerItemTier.TIER_4_TREASURE);
        ITEM_TIER_MAP.put("minecraft:prismarine_crystals", AnglerItemTier.TIER_4_TREASURE);

        // TIER 5 - Deep Sea Relics (Jackpot!)
        ITEM_TIER_MAP.put("minecraft:nautilus_shell", AnglerItemTier.TIER_5_RELIC);
        ITEM_TIER_MAP.put("minecraft:name_tag", AnglerItemTier.TIER_5_RELIC);
        ITEM_TIER_MAP.put("minecraft:saddle", AnglerItemTier.TIER_5_RELIC);

        // Rejected items - Core refuses to buy these
        // (Note: These are for reference, actual rejection happens in trade logic)
    }

    private static TagKey<Item> createTag(String tagName) {
        String[] parts = tagName.split(":");
        return TagKey.create(Registries.ITEM, new ResourceLocation(parts[0], parts[1]));
    }

    public static double getPriceBonus(int level) {
        if (level >= 46) return 0.15;
        if (level >= 31) return 0.10;
        if (level >= 11) return 0.05;
        return 0.0;
    }

    public static String getTitle(int level) {
        if (level >= 46) return "深渊主宰 (Abyss Lord)";
        if (level >= 31) return "沉船打捞员 (Salvor)";
        if (level >= 11) return "河床清道夫 (Dredger)";
        return "浮沫滤网 (Skimmer)";
    }

    public static double getTaxRelief(int level) {
        return 0.0;
    }

    /**
     * Get the tier for an item
     */
    public static AnglerItemTier getTierFor(ItemStack stack) {
        if (stack.isEmpty()) return null;
        
        Item item = stack.getItem();
        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
        if (itemId == null) return null;
        
        String itemName = itemId.toString();

        // Check direct item map first
        if (ITEM_TIER_MAP.containsKey(itemName)) {
            return ITEM_TIER_MAP.get(itemName);
        }

        // Check tags
        for (Map.Entry<TagKey<Item>, AnglerItemTier> entry : TAG_TIER_MAP.entrySet()) {
            if (stack.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Get XP for selling items
     * Based on the spec: Anti-AFK Matrix
     */
    public static double getXpFor(ItemStack stack) {
        AnglerItemTier tier = getTierFor(stack);
        if (tier == null) return 0.0;
        
        Double xpPerItem = TIER_XP_MAP.get(tier);
        if (xpPerItem == null) return 0.0;
        
        return xpPerItem * stack.getCount();
    }
    
    /**
     * Get XP per single item
     */
    public static double getXpPerItem(AnglerItemTier tier) {
        return TIER_XP_MAP.getOrDefault(tier, 0.0);
    }

    /**
     * Check if item is an angler item (can be sold by anglers)
     */
    public static boolean isAnglerItem(ItemStack stack) {
        return getTierFor(stack) != null;
    }
    
    /**
     * Check if item is a treasure/relic (T4 or T5)
     * These are the real money makers
     */
    public static boolean isTreasure(ItemStack stack) {
        AnglerItemTier tier = getTierFor(stack);
        return tier == AnglerItemTier.TIER_4_TREASURE || tier == AnglerItemTier.TIER_5_RELIC;
    }
    
    /**
     * Check if item is a relic (T5 - jackpot items)
     */
    public static boolean isRelic(ItemStack stack) {
        AnglerItemTier tier = getTierFor(stack);
        return tier == AnglerItemTier.TIER_5_RELIC;
    }
    
    /**
     * Get the tier name for display
     */
    public static String getTierName(AnglerItemTier tier) {
        switch (tier) {
            case TIER_1_BIOMASS: return "生物质废料";
            case TIER_2_TRASH: return "工业垃圾";
            case TIER_3_SPECIAL: return "特种样本";
            case TIER_4_TREASURE: return "稀有宝藏";
            case TIER_5_RELIC: return "深海遗物";
            default: return "未知";
        }
    }
    
    /**
     * Get base price for angler items (before market adjustments)
     * These are the prices mentioned in the spec
     */
    public static double getBasePrice(ItemStack stack) {
        AnglerItemTier tier = getTierFor(stack);
        if (tier == null) return 0.0;
        
        String itemName = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        
        switch (tier) {
            case TIER_1_BIOMASS:
                // Cod/Salmon: $0.1 - $0.2
                return 0.15;
            case TIER_3_SPECIAL:
                // Pufferfish: $1.0, Tropical Fish: $0.5, Ink Sac: $5.0
                if (itemName.equals("minecraft:ink_sac")) return 5.0;
                if (itemName.equals("minecraft:pufferfish")) return 1.0;
                if (itemName.equals("minecraft:tropical_fish")) return 0.5;
                return 0.5;
            case TIER_4_TREASURE:
                // Prismarine: $8.0
                return 8.0;
            case TIER_5_RELIC:
                // Nautilus Shell: $50.0, Name Tag: $30.0, Saddle: $25.0
                if (itemName.equals("minecraft:nautilus_shell")) return 50.0;
                if (itemName.equals("minecraft:name_tag")) return 30.0;
                if (itemName.equals("minecraft:saddle")) return 25.0;
                return 25.0;
            case TIER_2_TRASH:
            default:
                return 0.0; // Trash has no value
        }
    }
}
