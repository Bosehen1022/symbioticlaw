package com.symbioticlaw;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Symbioticlaw.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // V51 Taxation System Configs
    // 4.1 Periodic Tax
    public static final ForgeConfigSpec.DoubleValue PERIODIC_TAX_PARIAH = BUILDER.comment("Periodic tax for Pariahs").defineInRange("periodicTaxPariah", 5.0, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue PERIODIC_TAX_RESTRICTED = BUILDER.comment("Periodic tax for Restricted Citizens").defineInRange("periodicTaxRestricted", 10.0, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue PERIODIC_TAX_FREE = BUILDER.comment("Periodic tax for Free Citizens").defineInRange("periodicTaxFree", 18.0, 0, Double.MAX_VALUE);

    // 4.2 Mileage Tax (Overworld)
    public static final ForgeConfigSpec.DoubleValue MILEAGE_RATE_TIER1 = BUILDER.comment("Mileage tax rate for Tier 1 (0-2000 blocks)").defineInRange("mileageRateTier1", 0.1, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue MILEAGE_RATE_TIER2 = BUILDER.comment("Mileage tax rate for Tier 2 (2001-5000 blocks)").defineInRange("mileageRateTier2", 0.3, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue MILEAGE_RATE_TIER3 = BUILDER.comment("Mileage tax rate for Tier 3 (>5000 blocks)").defineInRange("mileageRateTier3", 0.8, 0, Double.MAX_VALUE);

    // 4.3 Dimension Entry Tax
    public static final ForgeConfigSpec.DoubleValue DIMENSION_TAX_NETHER = BUILDER.comment("Dimension entry tax for The Nether").defineInRange("dimensionTaxNether", 300.0, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue DIMENSION_TAX_END = BUILDER.comment("Dimension entry tax for The End").defineInRange("dimensionTaxEnd", 500.0, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue DIMENSION_TAX_MODDED = BUILDER.comment("Dimension entry tax for other modded dimensions").defineInRange("dimensionTaxModded", 400.0, 0, Double.MAX_VALUE);
    
    // 4.3.1 Adventure Dimension Taxes (BMC4 Pack)
    public static final ForgeConfigSpec.DoubleValue DIMENSION_TAX_TWILIGHT_FOREST = BUILDER.comment("Dimension entry tax for Twilight Forest").defineInRange("dimensionTaxTwilightForest", 400.0, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue DIMENSION_TAX_BLUE_SKIES = BUILDER.comment("Dimension entry tax for Blue Skies").defineInRange("dimensionTaxBlueSkies", 400.0, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue DIMENSION_TAX_UNDERGARDEN = BUILDER.comment("Dimension entry tax for Undergarden").defineInRange("dimensionTaxUndergarden", 600.0, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue DIMENSION_TAX_DEEPER_DARKER = BUILDER.comment("Dimension entry tax for Deeper & Darker (Otherside)").defineInRange("dimensionTaxDeeperDarker", 1000.0, 0, Double.MAX_VALUE);

    // 4.4 Dimensional Mileage Rates
    public static final ForgeConfigSpec.DoubleValue MILEAGE_RATE_NETHER = BUILDER.comment("Mileage tax rate within The Nether").defineInRange("mileageRateNether", 0.8, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue MILEAGE_RATE_END = BUILDER.comment("Mileage tax rate within The End").defineInRange("mileageRateEnd", 1.0, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue MILEAGE_RATE_MODDED = BUILDER.comment("Mileage tax rate within other modded dimensions").defineInRange("mileageRateModded", 1.2, 0, Double.MAX_VALUE);

    // 4.5 Cross-Dimension Transit
    public static final ForgeConfigSpec.DoubleValue CROSS_DIM_BASE_FEE_NETHER = BUILDER.comment("Cross-dimension base fee from The Nether").defineInRange("crossDimBaseFeeNether", 1500.0, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue CROSS_DIM_BASE_FEE_END = BUILDER.comment("Cross-dimension base fee from The End").defineInRange("crossDimBaseFeeEnd", 2500.0, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue CROSS_DIM_BASE_FEE_MODDED = BUILDER.comment("Cross-dimension base fee from other modded dimensions").defineInRange("crossDimBaseFeeModded", 2000.0, 0, Double.MAX_VALUE);


    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER.comment("Whether to log the dirt block on common setup").define("logDirtBlock", true);

    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER.comment("A magic number").defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER.comment("What you want the introduction message to be for the magic number").define("magicNumberIntroduction", "The magic number is... ");

    public static final ForgeConfigSpec.LongValue PROFESSION_CHANGE_COOLDOWN = BUILDER.comment("Cooldown in ticks for changing profession").defineInRange("professionChangeCooldown", 20 * 60 * 60, 0, Long.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue PROFESSION_CHANGE_COST = BUILDER.comment("Cost for changing profession").defineInRange("professionChangeCost", 1000.0, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_PRICES = BUILDER.comment("A list of items and their prices in format item_id=price.").defineListAllowEmpty("itemPrices", List.of("minecraft:cobblestone=0.1"), o -> o instanceof String);

    // a list of strings that are treated as resource locations for items
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER.comment("A list of items to log on common setup.").defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(ResourceLocation.parse(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream().map(itemName -> ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName))).collect(Collectors.toSet());

    }
}
