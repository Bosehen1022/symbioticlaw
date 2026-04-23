package com.symbioticlaw.professions;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChefProfessionManager {

    public enum ChefItemTier {
        TIER_1_FAST_FOOD,     // 快餐/配给 - 5 XP
        TIER_2_DESSERT,       // 精制甜点 - 8 XP
        TIER_3_FEAST,         // 盛宴/大餐 - 25 XP
        TIER_4_SPECIALTY      // 创造特供 - 5 XP
    }

    private static final Map<String, ChefItemTier> ITEM_TIER_MAP = new HashMap<>();
    private static final Map<ChefItemTier, Double> TIER_XP_MAP = new HashMap<>();

    static {
        TIER_XP_MAP.put(ChefItemTier.TIER_1_FAST_FOOD, 5.0);
        TIER_XP_MAP.put(ChefItemTier.TIER_2_DESSERT, 8.0);
        TIER_XP_MAP.put(ChefItemTier.TIER_3_FEAST, 25.0);
        TIER_XP_MAP.put(ChefItemTier.TIER_4_SPECIALTY, 5.0);

        // Tier 1: 快餐/配给 (5 XP)
        ITEM_TIER_MAP.put("farmersdelight:stuffed_potato", ChefItemTier.TIER_1_FAST_FOOD);
        ITEM_TIER_MAP.put("farmersdelight:hamburger", ChefItemTier.TIER_1_FAST_FOOD);
        ITEM_TIER_MAP.put("farmersdelight:bacon_and_eggs", ChefItemTier.TIER_1_FAST_FOOD);
        ITEM_TIER_MAP.put("farmersdelight:pasta_with_meatballs", ChefItemTier.TIER_1_FAST_FOOD);
        ITEM_TIER_MAP.put("minecraft:pumpkin_pie", ChefItemTier.TIER_1_FAST_FOOD);
        ITEM_TIER_MAP.put("farmersdelight:beef_stew", ChefItemTier.TIER_1_FAST_FOOD);
        ITEM_TIER_MAP.put("farmersdelight:chicken_soup", ChefItemTier.TIER_1_FAST_FOOD);
        ITEM_TIER_MAP.put("farmersdelight:vegetable_soup", ChefItemTier.TIER_1_FAST_FOOD);
        ITEM_TIER_MAP.put("farmersdelight:fish_and_chips", ChefItemTier.TIER_1_FAST_FOOD);
        ITEM_TIER_MAP.put("farmersdelight:mince_pie", ChefItemTier.TIER_1_FAST_FOOD);
        ITEM_TIER_MAP.put("farmersdelight:shepherds_pie", ChefItemTier.TIER_1_FAST_FOOD);
        ITEM_TIER_MAP.put("farmersdelight:dumplings", ChefItemTier.TIER_1_FAST_FOOD);
        ITEM_TIER_MAP.put("farmersdelight:salmon_roll", ChefItemTier.TIER_1_FAST_FOOD);
        ITEM_TIER_MAP.put("farmersdelight:chicken_rice", ChefItemTier.TIER_1_FAST_FOOD);

        // Tier 2: 精制甜点 (8 XP)
        ITEM_TIER_MAP.put("minecraft:cake", ChefItemTier.TIER_2_DESSERT);
        ITEM_TIER_MAP.put("minecraft:cookie", ChefItemTier.TIER_2_DESSERT);
        ITEM_TIER_MAP.put("minecraft:pumpkin_pie", ChefItemTier.TIER_2_DESSERT);
        ITEM_TIER_MAP.put("create:sweet_roll", ChefItemTier.TIER_2_DESSERT);
        ITEM_TIER_MAP.put("farmersdelight:apple_pie", ChefItemTier.TIER_2_DESSERT);
        ITEM_TIER_MAP.put("farmersdelight:chocolate_pie", ChefItemTier.TIER_2_DESSERT);
        ITEM_TIER_MAP.put("farmersdelight:fruit_salad", ChefItemTier.TIER_2_DESSERT);
        ITEM_TIER_MAP.put("farmersdelight:mixed_salad", ChefItemTier.TIER_2_DESSERT);

        // Tier 3: 盛宴/大餐 (25 XP)
        ITEM_TIER_MAP.put("farmersdelight:roast_chicken_block", ChefItemTier.TIER_3_FEAST);
        ITEM_TIER_MAP.put("farmersdelight:shepherds_pie_block", ChefItemTier.TIER_3_FEAST);
        ITEM_TIER_MAP.put("farmersdelight:honey_glazed_ham_block", ChefItemTier.TIER_3_FEAST);
        ITEM_TIER_MAP.put("farmersdelight:stuffed_pumpkin_block", ChefItemTier.TIER_3_FEAST);
        ITEM_TIER_MAP.put("farmersdelight:feast_table", ChefItemTier.TIER_3_FEAST);

        // Tier 4: 创造特供 (5 XP)
        ITEM_TIER_MAP.put("create:bar_of_chocolate", ChefItemTier.TIER_4_SPECIALTY);
        ITEM_TIER_MAP.put("create:honeyed_apple", ChefItemTier.TIER_4_SPECIALTY);
        ITEM_TIER_MAP.put("create:chocolate_glazed_berries", ChefItemTier.TIER_4_SPECIALTY);
        ITEM_TIER_MAP.put("create:tea", ChefItemTier.TIER_4_SPECIALTY);
        ITEM_TIER_MAP.put("create:coffee", ChefItemTier.TIER_4_SPECIALTY);
    }

    private static TagKey<Item> createTag(String tagName) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(tagName));
    }

    private static final Set<ResourceLocation> CHEF_BLOCKS = Stream.of(
            new ResourceLocation("farmersdelight", "cooking_pot"),
            new ResourceLocation("farmersdelight", "cutting_board"),
            new ResourceLocation("create", "mixer"),
            new ResourceLocation("create", "spout")
    ).collect(Collectors.toSet());

    public static ChefItemTier getTierFor(ItemStack stack) {
        if (stack.isEmpty()) return null;

        Item item = stack.getItem();
        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
        if (itemId == null) return null;

        String itemName = itemId.toString();
        return ITEM_TIER_MAP.get(itemName);
    }

    public static double getXpFor(ItemStack stack) {
        ChefItemTier tier = getTierFor(stack);
        if (tier == null) return 0.0;

        Double xpPerItem = TIER_XP_MAP.get(tier);
        if (xpPerItem == null) return 0.0;

        return xpPerItem * stack.getCount();
    }

    public static double getXpPerItem(ChefItemTier tier) {
        return TIER_XP_MAP.getOrDefault(tier, 0.0);
    }

    public static boolean isChefItem(ItemStack stack) {
        return getTierFor(stack) != null;
    }

    public static String getTierName(ChefItemTier tier) {
        switch (tier) {
            case TIER_1_FAST_FOOD: return "快餐配给";
            case TIER_2_DESSERT: return "精制甜点";
            case TIER_3_FEAST: return "盛宴大餐";
            case TIER_4_SPECIALTY: return "创造特供";
            default: return "未知";
        }
    }

    public static double getPriceBonus(int level) {
        if (level >= 46) return 0.15;
        if (level >= 31) return 0.10;
        if (level >= 11) return 0.05;
        return 0.0;
    }

    public static String getTitle(int level) {
        if (level >= 46) return "味觉大师 (Maestro)";
        if (level >= 31) return "膳食官 (Caterer)";
        if (level >= 11) return "主厨 (Chef)";
        return "帮厨 (Kitchen Hand)";
    }

    public static boolean isChefBlock(ResourceLocation blockId) {
        return CHEF_BLOCKS.contains(blockId);
    }

    public static double getTaxRelief(int level) {
        return 0.0;
    }
}