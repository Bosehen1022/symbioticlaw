package com.symbioticlaw.professions;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class FarmerProfessionManager {

    public enum FarmerItemCategory {
        CROP,        // Tier 2: 0.1 XP each (10 = 1 XP)
        LOG,         // Tier 3: 0.25 XP each (4 = 1 XP)
        MEAT_LEATHER,// Tier 5: 1.0 XP each
        PROCESSED,   // Tier 6: 2.0 XP each
        SAPLING,     // Tier 1: 0.0 XP (no XP to prevent seed farming)
        BYPRODUCT    // Tier 4: 0.5 XP each (feather, wool, etc)
    }

    private static final Map<String, FarmerItemCategory> ITEM_CATEGORY_MAP = new HashMap<>();
    private static final Map<TagKey<Item>, FarmerItemCategory> TAG_CATEGORY_MAP = new HashMap<>();
    
    // XP values per item
    private static final Map<FarmerItemCategory, Double> CATEGORY_XP = new HashMap<>();

    static {
        // Initialize XP values
        CATEGORY_XP.put(FarmerItemCategory.SAPLING, 0.0);
        CATEGORY_XP.put(FarmerItemCategory.CROP, 0.1);
        CATEGORY_XP.put(FarmerItemCategory.LOG, 0.25);
        CATEGORY_XP.put(FarmerItemCategory.BYPRODUCT, 0.5);
        CATEGORY_XP.put(FarmerItemCategory.MEAT_LEATHER, 1.0);
        CATEGORY_XP.put(FarmerItemCategory.PROCESSED, 2.0);
        
        // Crops - Tier 2 (0.1 XP each)
        TAG_CATEGORY_MAP.put(createTag("forge:crops"), FarmerItemCategory.CROP);
        TAG_CATEGORY_MAP.put(createTag("forge:vegetables"), FarmerItemCategory.CROP);
        TAG_CATEGORY_MAP.put(createTag("forge:fruits"), FarmerItemCategory.CROP);
        
        // Farmer's Delight specific crops
        ITEM_CATEGORY_MAP.put("farmersdelight:cabbage", FarmerItemCategory.CROP);
        ITEM_CATEGORY_MAP.put("farmersdelight:tomato", FarmerItemCategory.CROP);
        ITEM_CATEGORY_MAP.put("farmersdelight:onion", FarmerItemCategory.CROP);
        ITEM_CATEGORY_MAP.put("farmersdelight:rice", FarmerItemCategory.CROP);
        
        // Forestry - Tier 3 (0.25 XP each)
        TAG_CATEGORY_MAP.put(createTag("minecraft:logs"), FarmerItemCategory.LOG);
        TAG_CATEGORY_MAP.put(createTag("forge:bamboo"), FarmerItemCategory.LOG);
        
        // Saplings - Tier 1 (0 XP to prevent seed farming)
        TAG_CATEGORY_MAP.put(createTag("minecraft:saplings"), FarmerItemCategory.SAPLING);
        
        // Animal Products - Tier 5 (1.0 XP each)
        TAG_CATEGORY_MAP.put(createTag("forge:raw_beef"), FarmerItemCategory.MEAT_LEATHER);
        TAG_CATEGORY_MAP.put(createTag("forge:raw_pork"), FarmerItemCategory.MEAT_LEATHER);
        TAG_CATEGORY_MAP.put(createTag("forge:raw_mutton"), FarmerItemCategory.MEAT_LEATHER);
        TAG_CATEGORY_MAP.put(createTag("forge:raw_chicken"), FarmerItemCategory.MEAT_LEATHER);
        TAG_CATEGORY_MAP.put(createTag("forge:raw_fishes"), FarmerItemCategory.MEAT_LEATHER);
        ITEM_CATEGORY_MAP.put("minecraft:rabbit", FarmerItemCategory.MEAT_LEATHER);
        ITEM_CATEGORY_MAP.put("minecraft:leather", FarmerItemCategory.MEAT_LEATHER);
        
        // Byproducts - Tier 4 (0.5 XP each)
        ITEM_CATEGORY_MAP.put("minecraft:feather", FarmerItemCategory.BYPRODUCT);
        ITEM_CATEGORY_MAP.put("minecraft:wool", FarmerItemCategory.BYPRODUCT);
        ITEM_CATEGORY_MAP.put("minecraft:egg", FarmerItemCategory.BYPRODUCT);
        TAG_CATEGORY_MAP.put(createTag("forge:eggs"), FarmerItemCategory.BYPRODUCT);
        
        // Processed Goods - Tier 6 (2.0 XP each) - Lv 20+ only
        ITEM_CATEGORY_MAP.put("minecraft:hay_block", FarmerItemCategory.PROCESSED);
        TAG_CATEGORY_MAP.put(createTag("minecraft:stripped_logs"), FarmerItemCategory.PROCESSED);
        ITEM_CATEGORY_MAP.put("farmersdelight:straw_bale", FarmerItemCategory.PROCESSED);
        
        // Add stripped wood variants
        ITEM_CATEGORY_MAP.put("minecraft:stripped_oak_log", FarmerItemCategory.PROCESSED);
        ITEM_CATEGORY_MAP.put("minecraft:stripped_birch_log", FarmerItemCategory.PROCESSED);
        ITEM_CATEGORY_MAP.put("minecraft:stripped_spruce_log", FarmerItemCategory.PROCESSED);
        ITEM_CATEGORY_MAP.put("minecraft:stripped_jungle_log", FarmerItemCategory.PROCESSED);
        ITEM_CATEGORY_MAP.put("minecraft:stripped_acacia_log", FarmerItemCategory.PROCESSED);
        ITEM_CATEGORY_MAP.put("minecraft:stripped_dark_oak_log", FarmerItemCategory.PROCESSED);
        ITEM_CATEGORY_MAP.put("minecraft:stripped_mangrove_log", FarmerItemCategory.PROCESSED);
        ITEM_CATEGORY_MAP.put("minecraft:stripped_cherry_log", FarmerItemCategory.PROCESSED);
    }

    private static TagKey<Item> createTag(String tagName) {
        return TagKey.create(Registries.ITEM, ResourceLocation.parse(tagName));
    }

    public static FarmerItemCategory getCategoryFor(ItemStack stack) {
        if (stack.isEmpty()) return null;
        
        Item item = stack.getItem();
        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
        if (itemId == null) return null;
        
        String itemName = itemId.toString();

        if (ITEM_CATEGORY_MAP.containsKey(itemName)) {
            return ITEM_CATEGORY_MAP.get(itemName);
        }

        for (Map.Entry<TagKey<Item>, FarmerItemCategory> entry : TAG_CATEGORY_MAP.entrySet()) {
            if (stack.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }
    
    /**
     * Get XP for selling farmer items
     * Based on the spec: value weight system
     */
    public static double getXpFor(ItemStack stack) {
        FarmerItemCategory category = getCategoryFor(stack);
        if (category == null) return 0.0;
        
        Double xpPerItem = CATEGORY_XP.get(category);
        if (xpPerItem == null) return 0.0;
        
        return xpPerItem * stack.getCount();
    }
    
    /**
     * Get XP per single item
     */
    public static double getXpPerItem(FarmerItemCategory category) {
        return CATEGORY_XP.getOrDefault(category, 0.0);
    }
    
    /**
     * Check if item requires Lv 20+ to sell (compressed/processed items)
     */
    public static boolean requiresLevel20(ItemStack stack) {
        FarmerItemCategory category = getCategoryFor(stack);
        return category == FarmerItemCategory.PROCESSED;
    }

    public static boolean isFarmerItem(ItemStack stack) {
        return getCategoryFor(stack) != null;
    }
    
    public static boolean isFarmerProcessedItem(ItemStack stack) {
        return getCategoryFor(stack) == FarmerItemCategory.PROCESSED;
    }
}
