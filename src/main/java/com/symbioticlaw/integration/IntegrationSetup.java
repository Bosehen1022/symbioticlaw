package com.symbioticlaw.integration;

import com.symbioticlaw.Symbioticlaw;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 模组集成初始化设置
 * 在服务器启动时检测并报告模组集成状态
 */
@Mod.EventBusSubscriber(modid = Symbioticlaw.MODID)
public class IntegrationSetup {
    
    private static final Logger LOGGER = LogManager.getLogger("SymbioticLaw/Integration");
    
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("========================================");
        LOGGER.info("《共生律法》模组集成状态报告");
        LOGGER.info("========================================");
        
        // 铁匠职业 - Tetra
        reportTetraIntegration();
        
        // 冒险家职业 - 维度模组
        reportAdventureModIntegration();
        
        // 农夫职业 - Create & Farmer's Delight
        reportFarmerIntegration();
        
        // 厨师职业 - Farmer's Delight
        reportChefIntegration();
        
        // 矿工职业 - Create
        reportMinerIntegration();
        
        // Waystones
        reportWaystonesIntegration();
        
        LOGGER.info("========================================");
    }
    
    private static void reportTetraIntegration() {
        boolean tetraLoaded = ModIntegration.isTetraLoaded();
        if (tetraLoaded) {
            LOGGER.info("[铁匠职业] ✓ Tetra模组已加载");
            LOGGER.info("  - Tetra工作台将仅限铁匠使用");
            LOGGER.info("  - Tetra锤子将仅限铁匠使用");
            LOGGER.info("  - Tetra装备将享受铁匠专属回收价");
        } else {
            LOGGER.info("[铁匠职业] ✗ Tetra模组未加载");
            LOGGER.info("  - 铁匠将无法使用Tetra锻造系统");
            LOGGER.info("  - 建议安装Tetra以获得完整体验");
        }
    }
    
    private static void reportAdventureModIntegration() {
        java.util.List<String> loadedMods = ModIntegration.getLoadedAdventureMods();
        
        LOGGER.info("[冒险家职业] 维度模组检测:");
        
        if (loadedMods.isEmpty()) {
            LOGGER.info("  ✗ 未发现冒险维度模组");
            LOGGER.info("  - 建议安装: 暮色森林、蔚蓝浩空、深暗之园等");
        } else {
            for (String mod : loadedMods) {
                switch (mod) {
                    case "twilightforest" -> {
                        LOGGER.info("  ✓ 暮色森林 (Twilight Forest) - Tier 2 受控异常区");
                        LOGGER.info("    关税: $400, 里程费率: $0.8/格");
                    }
                    case "blue_skies" -> {
                        LOGGER.info("  ✓ 蔚蓝浩空 (Blue Skies) - Tier 2 生态变异区");
                        LOGGER.info("    关税: $400, 里程费率: $0.8/格");
                    }
                    case "undergarden" -> {
                        LOGGER.info("  ✓ 深暗之园 (Undergarden) - Tier 3 重度污染区");
                        LOGGER.info("    关税: $600, 里程费率: $1.0/格");
                    }
                    case "deeperdarker" -> {
                        LOGGER.info("  ✓ 更深更暗 (Deeper & Darker) - Tier 4 深渊湮灭区");
                        LOGGER.info("    关税: $1000, 里程费率: $1.5/格");
                    }
                    case "cataclysm" -> {
                        LOGGER.info("  ✓ 灾变 (Cataclysm) - Boss战利品高价收购");
                        LOGGER.info("    战利品基础价: $10000");
                    }
                }
            }
            
            LOGGER.info("  冒险家在这些维度可享受税务减免（最高30%）");
        }
    }
    
    private static void reportFarmerIntegration() {
        boolean createLoaded = ModIntegration.isCreateLoaded();
        boolean farmersDelightLoaded = ModIntegration.isFarmersDelightLoaded();
        
        LOGGER.info("[农夫职业] 模组集成:");
        if (createLoaded) {
            LOGGER.info("  ✓ Create (机械动力) - 农业机械已锁定");
            LOGGER.info("    - 动力收割机、动力锯、动力犁仅限农夫使用");
        }
        if (farmersDelightLoaded) {
            LOGGER.info("  ✓ Farmer's Delight (农夫乐事) - 作物系统已集成");
            LOGGER.info("    - 卷心菜、番茄、洋葱等作物已加入收购名单");
        }
        if (!createLoaded && !farmersDelightLoaded) {
            LOGGER.info("  ✗ 未发现农业相关模组");
        }
    }
    
    private static void reportChefIntegration() {
        boolean farmersDelightLoaded = ModIntegration.isFarmersDelightLoaded();
        boolean createLoaded = ModIntegration.isCreateLoaded();
        
        LOGGER.info("[厨师职业] 模组集成:");
        if (farmersDelightLoaded) {
            LOGGER.info("  ✓ Farmer's Delight (农夫乐事) - 烹饪系统已集成");
            LOGGER.info("    - 厨锅、切菜板仅限厨师使用");
            LOGGER.info("    - 国家粮仓已开放批发进货");
        }
        if (createLoaded) {
            LOGGER.info("  ✓ Create (机械动力) - 食品加工机械已锁定");
            LOGGER.info("    - 动力搅拌器、注液器仅限厨师使用");
        }
        if (!farmersDelightLoaded && !createLoaded) {
            LOGGER.info("  ✗ 未发现烹饪相关模组");
        }
    }
    
    private static void reportMinerIntegration() {
        boolean createLoaded = ModIntegration.isCreateLoaded();
        
        LOGGER.info("[矿工职业] 模组集成:");
        if (createLoaded) {
            LOGGER.info("  ✓ Create (机械动力) - 矿业机械已锁定");
            LOGGER.info("    - 动力钻头、动力破碎轮仅限矿工使用");
        } else {
            LOGGER.info("  ✗ Create (机械动力) 未加载");
            LOGGER.info("    - 建议安装以启用矿业机械系统");
        }
    }
    
    private static void reportWaystonesIntegration() {
        boolean waystonesLoaded = ModIntegration.isWaystonesLoaded();
        
        LOGGER.info("[传送系统] Waystones:");
        if (waystonesLoaded) {
            LOGGER.info("  ✓ Waystones (传送石碑) - 已接管传送税");
            LOGGER.info("    - 所有传送将征收里程税/维度税");
            LOGGER.info("    - 余额不足将阻止传送");
        } else {
            LOGGER.info("  ✗ Waystones (传送石碑) 未加载");
            LOGGER.info("    - 传送税系统未激活");
            LOGGER.info("    - 建议安装以获得完整税务体验");
        }
    }
}
