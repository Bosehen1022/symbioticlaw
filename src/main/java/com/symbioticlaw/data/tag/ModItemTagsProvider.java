package com.symbioticlaw.data.tag;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.data.tag.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               CompletableFuture<TagLookup<Block>> blockTagLookup, ExistingFileHelper existingFileHelper) {
        super(packOutput, lookupProvider, blockTagLookup, Symbioticlaw.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider provider) {
        addMinerTags();
        addFarmerTags();
    }

    private void addMinerTags() {
        tag(ModTags.Items.MINER_RESOURCES_T3)
                .add(Items.COAL)
                .add(Items.LAPIS_LAZULI)
                .add(Items.REDSTONE);

        tag(ModTags.Items.MINER_METALS_T2)
                .add(Items.RAW_IRON, Items.RAW_GOLD, Items.RAW_COPPER)
                .addOptional(ResourceLocation.parse("forge:raw_materials/tin"))
                .addOptional(ResourceLocation.parse("forge:raw_materials/lead"))
                .addOptional(ResourceLocation.parse("forge:raw_materials/silver"))
                .addOptional(ResourceLocation.parse("forge:raw_materials/nickel"));

        tag(ModTags.Items.MINER_GEMS_T1)
                .add(Items.QUARTZ);

        tag(ModTags.Items.MINER_GEMS_T5)
                .add(Items.DIAMOND)
                .add(Items.EMERALD);

        tag(ModTags.Items.MINER_PROCESSED_T4)
                .add(Items.IRON_INGOT, Items.GOLD_INGOT, Items.COPPER_INGOT)
                .add(Items.NETHERITE_SCRAP, Items.NETHERITE_INGOT);

        tag(ModTags.Items.MINER_SELLABLE)
                .addTag(ModTags.Items.MINER_RESOURCES_T3)
                .addTag(ModTags.Items.MINER_METALS_T2)
                .addTag(ModTags.Items.MINER_GEMS_T1)
                .addTag(ModTags.Items.MINER_GEMS_T5)
                .addTag(ModTags.Items.MINER_PROCESSED_T4);
    }

    private void addFarmerTags() {
        tag(ModTags.Items.FARMER_CROPS_T2)
                .add(Items.WHEAT, Items.CARROT, Items.POTATO, Items.BEETROOT, Items.PUMPKIN, Items.MELON_SLICE, Items.CACTUS, Items.COCOA_BEANS)
                .addTag(TagKey.create(Registries.ITEM, ResourceLocation.parse("forge:crops")))
                .addOptional(ResourceLocation.parse("farmersdelight:cabbage"))
                .addOptional(ResourceLocation.parse("farmersdelight:tomato"))
                .addOptional(ResourceLocation.parse("farmersdelight:onion"))
                .addOptional(ResourceLocation.parse("farmersdelight:rice"));

        tag(ModTags.Items.FARMER_FORESTRY_T3)
                .addTag(ItemTags.LOGS)
                .add(Items.BAMBOO)
                .add(Items.SUGAR_CANE);

        tag(ModTags.Items.FARMER_ANIMAL_PRODUCTS_T4)
                .add(Items.FEATHER)
                .add(Items.EGG)
                .addTag(ItemTags.WOOL);

        tag(ModTags.Items.FARMER_RAW_MEATS_T5)
                .add(Items.BEEF, Items.PORKCHOP, Items.MUTTON, Items.CHICKEN, Items.RABBIT)
                .addTag(TagKey.create(Registries.ITEM, ResourceLocation.parse("forge:raw_fishes")));

        tag(ModTags.Items.FARMER_PROCESSED_T6)
                .add(Items.HAY_BLOCK)
                .addTag(TagKey.create(Registries.ITEM, ResourceLocation.parse("forge:stripped_logs")))
                .addTag(TagKey.create(Registries.ITEM, ResourceLocation.parse("forge:stripped_wood")))
                .addOptional(ResourceLocation.parse("farmersdelight:rice_bale"))
                .addOptional(ResourceLocation.parse("create:tree_bark"));

        tag(ModTags.Items.FARMER_SELLABLE)
                .addTag(ModTags.Items.FARMER_CROPS_T2)
                .addTag(ModTags.Items.FARMER_FORESTRY_T3)
                .addTag(ModTags.Items.FARMER_ANIMAL_PRODUCTS_T4)
                .addTag(ModTags.Items.FARMER_RAW_MEATS_T5)
                .addTag(ModTags.Items.FARMER_PROCESSED_T6)
                .addTag(ItemTags.SAPLINGS)
                .add(Items.LEATHER);
    }
}