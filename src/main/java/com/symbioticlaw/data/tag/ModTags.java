package com.symbioticlaw.data.tag;

import com.symbioticlaw.Symbioticlaw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> MINER_RESOURCES_T3 = tag("miner_resources_t3");
        public static final TagKey<Item> MINER_METALS_T2 = tag("miner_metals_t2");
        public static final TagKey<Item> MINER_GEMS_T1 = tag("miner_gems_t1");
        public static final TagKey<Item> MINER_GEMS_T5 = tag("miner_gems_t5");
        public static final TagKey<Item> MINER_PROCESSED_T4 = tag("miner_processed_t4");
        public static final TagKey<Item> MINER_SELLABLE = tag("miner_sellables");

        public static final TagKey<Item> FARMER_CROPS_T2 = tag("farmer_crops_t2");
        public static final TagKey<Item> FARMER_FORESTRY_T3 = tag("farmer_forestry_t3");
        public static final TagKey<Item> FARMER_ANIMAL_PRODUCTS_T4 = tag("farmer_animal_products_t4");
        public static final TagKey<Item> FARMER_RAW_MEATS_T5 = tag("farmer_raw_meats_t5");
        public static final TagKey<Item> FARMER_PROCESSED_T6 = tag("farmer_processed_t6");
        public static final TagKey<Item> FARMER_SELLABLE = tag("farmer_sellables");

        public static final TagKey<Item> CHEF_SELLABLES = tag("chef_sellables");
        public static final TagKey<Item> ANGLER_SELLABLES = tag("angler_sellables");
        public static final TagKey<Item> ADVENTURER_SELLABLES = tag("adventurer_sellables");
        public static final TagKey<Item> BLACKSMITH_SELLABLES = tag("blacksmith_sellables");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, ResourceLocation.parse(Symbioticlaw.MODID + ":" + name));
        }
    }
}