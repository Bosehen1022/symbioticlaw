package com.symbioticlaw.integration;

import net.minecraftforge.fml.ModList;

/**
 * 模组集成管理器
 * 检测和管理与其他模组的集成
 */
public class ModIntegration {
    
    private static final String WAYSTONES_MODID = "waystones";
    private static final String CREATE_MODID = "create";
    private static final String FARMERS_DELIGHT_MODID = "farmersdelight";
    private static final String TETRA_MODID = "tetra";
    private static final String JOURNEYMAP_MODID = "journeymap";
    private static final String AQUAMIRAE_MODID = "aquamirae";
    private static final String TWILIGHT_FOREST_MODID = "twilightforest";
    private static final String BLUE_SKIES_MODID = "blue_skies";
    private static final String DEEPER_DARKER_MODID = "deeperdarker";
    private static final String UNDERGARDEN_MODID = "undergarden";
    private static final String CATALYSM_MODID = "cataclysm";
    
    /**
     * 检查Waystones模组是否加载
     */
    public static boolean isWaystonesLoaded() {
        return ModList.get().isLoaded(WAYSTONES_MODID);
    }
    
    /**
     * 检查Create模组是否加载
     */
    public static boolean isCreateLoaded() {
        return ModList.get().isLoaded(CREATE_MODID);
    }
    
    /**
     * 检查Farmer's Delight模组是否加载
     */
    public static boolean isFarmersDelightLoaded() {
        return ModList.get().isLoaded(FARMERS_DELIGHT_MODID);
    }
    
    /**
     * 检查Tetra模组是否加载
     */
    public static boolean isTetraLoaded() {
        return ModList.get().isLoaded(TETRA_MODID);
    }
    
    /**
     * 检查JourneyMap模组是否加载
     */
    public static boolean isJourneyMapLoaded() {
        return ModList.get().isLoaded(JOURNEYMAP_MODID);
    }
    
    /**
     * 检查冒险维度模组是否加载
     */
    public static boolean isAdventureDimensionModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }
    
    /**
     * 检查暮色森林是否加载
     */
    public static boolean isTwilightForestLoaded() {
        return ModList.get().isLoaded(TWILIGHT_FOREST_MODID);
    }
    
    /**
     * 检查蔚蓝浩空是否加载
     */
    public static boolean isBlueSkiesLoaded() {
        return ModList.get().isLoaded(BLUE_SKIES_MODID);
    }
    
    /**
     * 检查更深更暗是否加载
     */
    public static boolean isDeeperDarkerLoaded() {
        return ModList.get().isLoaded(DEEPER_DARKER_MODID);
    }
    
    /**
     * 检查深暗之园是否加载
     */
    public static boolean isUndergardenLoaded() {
        return ModList.get().isLoaded(UNDERGARDEN_MODID);
    }
    
    /**
     * 检查灾变模组是否加载
     */
    public static boolean isCataclysmLoaded() {
        return ModList.get().isLoaded(CATALYSM_MODID);
    }
    
    /**
     * 获取所有已加载的冒险维度模组
     */
    public static java.util.List<String> getLoadedAdventureMods() {
        java.util.List<String> mods = new java.util.ArrayList<>();
        if (isTwilightForestLoaded()) mods.add("twilightforest");
        if (isBlueSkiesLoaded()) mods.add("blue_skies");
        if (isDeeperDarkerLoaded()) mods.add("deeperdarker");
        if (isUndergardenLoaded()) mods.add("undergarden");
        if (isCataclysmLoaded()) mods.add("cataclysm");
        return mods;
    }
}
