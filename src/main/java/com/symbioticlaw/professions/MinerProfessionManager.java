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

public class MinerProfessionManager {

    private static final Map<String, Integer> ITEM_XP_MAP = new HashMap<>();
    private static final Map<TagKey<Item>, Integer> TAG_XP_MAP = new HashMap<>();
    private static final Set<String> COMPRESSED_BLOCKS = new HashSet<>();

    static {
        // TIER 1 - Fuel (1 XP)
        ITEM_XP_MAP.put("minecraft:coal", 1);

        // TIER 2 - Base Metals (2 XP)
        TAG_XP_MAP.put(createTag("forge:ingots/copper"), 2);
        TAG_XP_MAP.put(createTag("forge:ingots/tin"), 2);
        TAG_XP_MAP.put(createTag("forge:ingots/lead"), 2);
        TAG_XP_MAP.put(createTag("forge:ingots/silver"), 2);
        TAG_XP_MAP.put(createTag("forge:ingots/nickel"), 2);
        TAG_XP_MAP.put(createTag("forge:ingots/aluminum"), 2);
        TAG_XP_MAP.put(createTag("forge:ingots/aluminium"), 2); // Some mods use different spelling

        // TIER 3 - Industrial Metals (5 XP)
        TAG_XP_MAP.put(createTag("forge:ingots/iron"), 5);
        TAG_XP_MAP.put(createTag("forge:ingots/gold"), 5);
        TAG_XP_MAP.put(createTag("forge:ingots/zinc"), 5);
        TAG_XP_MAP.put(createTag("forge:ingots/brass"), 5);
        TAG_XP_MAP.put(createTag("forge:ingots/steel"), 5);
        TAG_XP_MAP.put(createTag("forge:ingots/bronze"), 5);
        TAG_XP_MAP.put(createTag("forge:ingots/invar"), 5);
        TAG_XP_MAP.put(createTag("forge:ingots/electrum"), 5);
        TAG_XP_MAP.put(createTag("forge:ingots/constantan"), 5);

        // TIER 4 - Precious Resources (20 XP)
        ITEM_XP_MAP.put("minecraft:diamond", 20);
        ITEM_XP_MAP.put("minecraft:emerald", 20);
        TAG_XP_MAP.put(createTag("forge:ingots/uranium"), 20);
        TAG_XP_MAP.put(createTag("forge:ingots/osmium"), 20);
        
        // TIER 5 - Strategic Nether Resources (100 XP)
        ITEM_XP_MAP.put("minecraft:netherite_scrap", 100);
        ITEM_XP_MAP.put("minecraft:netherite_ingot", 100);
        
        // COMPRESSED BLOCKS (Tier 6) - Lv 20+ miners only
        // These give 9 * base_ingot_xp * 1.1 bonus
        COMPRESSED_BLOCKS.add("minecraft:iron_block");
        COMPRESSED_BLOCKS.add("minecraft:gold_block");
        COMPRESSED_BLOCKS.add("minecraft:diamond_block");
        COMPRESSED_BLOCKS.add("minecraft:emerald_block");
        COMPRESSED_BLOCKS.add("minecraft:netherite_block");
        COMPRESSED_BLOCKS.add("minecraft:copper_block");
        COMPRESSED_BLOCKS.add("create:zinc_block");
        COMPRESSED_BLOCKS.add("create:brass_block");
        COMPRESSED_BLOCKS.add("create:iron_block"); // If Create adds its own
    }

    private static TagKey<Item> createTag(String tagName) {
        return TagKey.create(Registries.ITEM, ResourceLocation.parse(tagName));
    }

    public static int getXpFor(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        
        Item item = stack.getItem();
        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
        if (itemId == null) return 0;
        
        String itemName = itemId.toString();

        // Check if it's a compressed block
        if (isCompressedBlock(itemName)) {
            return calculateCompressedBlockXp(itemName);
        }

        // Check direct item map first
        if (ITEM_XP_MAP.containsKey(itemName)) {
            return ITEM_XP_MAP.get(itemName);
        }

        // Check tags
        for (Map.Entry<TagKey<Item>, Integer> entry : TAG_XP_MAP.entrySet()) {
            if (stack.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return 0;
    }
    
    public static boolean isCompressedBlock(String itemName) {
        return COMPRESSED_BLOCKS.contains(itemName) || 
               (itemName.endsWith("_block") && isMineralBlock(itemName));
    }
    
    private static boolean isMineralBlock(String itemName) {
        // Check if it's a known mineral block
        String lower = itemName.toLowerCase();
        return lower.contains("iron") || lower.contains("gold") || lower.contains("diamond") || 
               lower.contains("emerald") || lower.contains("netherite") || lower.contains("copper") ||
               lower.contains("zinc") || lower.contains("brass") || lower.contains("tin") ||
               lower.contains("lead") || lower.contains("silver") || lower.contains("nickel") ||
               lower.contains("steel") || lower.contains("bronze") || lower.contains("uranium");
    }
    
    private static int calculateCompressedBlockXp(String blockName) {
        // Try to derive XP from the corresponding ingot
        String ingotName = blockName.replace("_block", "_ingot");
        
        // Special case for vanilla blocks
        if (blockName.equals("minecraft:iron_block")) {
            return (int) Math.round(5 * 9 * 1.1); // 50 XP (Tier 3 * 9 * 1.1)
        }
        if (blockName.equals("minecraft:gold_block")) {
            return (int) Math.round(5 * 9 * 1.1); // 50 XP
        }
        if (blockName.equals("minecraft:diamond_block")) {
            return (int) Math.round(20 * 9 * 1.1); // 198 XP
        }
        if (blockName.equals("minecraft:emerald_block")) {
            return (int) Math.round(20 * 9 * 1.1); // 198 XP
        }
        if (blockName.equals("minecraft:netherite_block")) {
            return (int) Math.round(100 * 9 * 1.1); // 990 XP
        }
        if (blockName.equals("minecraft:copper_block")) {
            return (int) Math.round(2 * 9 * 1.1); // 20 XP
        }
        if (blockName.equals("create:zinc_block")) {
            return (int) Math.round(5 * 9 * 1.1); // 50 XP
        }
        if (blockName.equals("create:brass_block")) {
            return (int) Math.round(5 * 9 * 1.1); // 50 XP
        }
        
        // Generic calculation for other blocks
        try {
            ItemStack ingotStack = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.parse(ingotName)));
            if (!ingotStack.isEmpty()) {
                int ingotXp = getXpFor(ingotStack);
                if (ingotXp > 0) {
                    return (int) Math.round(ingotXp * 9 * 1.1);
                }
            }
        } catch (Exception e) {
            // Fall through to default
        }
        
        return 0;
    }

    public static boolean isMinerItem(ItemStack stack) {
        return getXpFor(stack) > 0;
    }
    
    public static boolean isCompressedBlock(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) return false;
        return isCompressedBlock(itemId.toString());
    }
}
