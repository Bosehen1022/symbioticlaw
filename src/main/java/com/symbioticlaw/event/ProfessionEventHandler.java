package com.symbioticlaw.event;

import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.JobType;
import com.symbioticlaw.professions.BlacksmithProfessionManager;
import com.symbioticlaw.system.DailyQuotaSystem;
import com.symbioticlaw.system.ProfessionSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber
public class ProfessionEventHandler {

    private static final ProfessionSystem professionSystem = new ProfessionSystem();

    private static final Set<ResourceLocation> CHEF_BLOCKS = Stream.of(
            new ResourceLocation("farmersdelight", "cooking_pot"),
            new ResourceLocation("farmersdelight", "cutting_board"),
            new ResourceLocation("create", "mixer"),
            new ResourceLocation("create", "spout")
    ).collect(Collectors.toSet());

    private static final Set<ResourceLocation> FARMER_MACHINES = Stream.of(
            new ResourceLocation("create", "mechanical_harvester"),
            new ResourceLocation("create", "mechanical_saw"),
            new ResourceLocation("create", "mechanical_plough")
    ).collect(Collectors.toSet());

    private static final Set<ResourceLocation> BLACKSMITH_BLOCKS = Stream.of(
            new ResourceLocation("tetra", "workbench"),
            new ResourceLocation("tetra", "basic_workbench"),
            new ResourceLocation("tetra", "forged_workbench"),
            new ResourceLocation("tetra", "hammer_base"),
            new ResourceLocation("tetra", "hammer_head"),
            new ResourceLocation("tetra", "core_extractor"),
            new ResourceLocation("tetra", "seeping_bedrock"),
            new ResourceLocation("tetra", "fractured_bedrock"),
            new ResourceLocation("tetra", "transfer_unit"),
            new ResourceLocation("tetra", "rack"),
            new ResourceLocation("tetra", "scroll_block"),
            new ResourceLocation("tetra", "holosphere")
    ).collect(Collectors.toSet());

    private static final Set<ResourceLocation> ADVENTURER_BLOCKS = Stream.of(
            new ResourceLocation("minecraft", "cartography_table")
    ).collect(Collectors.toSet());

    private static final Set<ResourceLocation> EXPLORER_MAPS = Stream.of(
            new ResourceLocation("minecraft", "filled_map"),
            new ResourceLocation("minecraft", "map")
    ).collect(Collectors.toSet());

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        Player player = event.getEntity();

        if (CHEF_BLOCKS.contains(blockId)) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                if (playerData.getProfession().toJobType() != JobType.CHEF) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.literal("§c[🚫] 您没有食品加工许可证。"));
                }
            });
        }
        
        // Cartography Table - Adventurer exclusive
        if (ADVENTURER_BLOCKS.contains(blockId)) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                if (playerData.getProfession().toJobType() != JobType.ADVENTURER) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.literal("§c[🚫 权限不足] 警告：您未持有《广域勘探许可证》，无法操作制图台。"));
                }
            });
        }
        
        // Tetra Workbenches - Blacksmith exclusive
        if (BLACKSMITH_BLOCKS.contains(blockId)) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                if (playerData.getProfession().toJobType() != JobType.BLACKSMITH) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.literal("§c[🚫 权限不足] 警告：您未持有《军工级金属重熔许可证》，无法使用锻造设备。"));
                }
            });
        }
    }
    
    /**
     * Handle explorer maps - adventurer exclusive
     */
    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() || event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
        
        Player player = event.player;
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        
        // Check if holding explorer map (filled_map with specific name pattern)
        checkExplorerMap(player, mainHand);
        checkExplorerMap(player, offHand);
    }
    
    private static void checkExplorerMap(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
        
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) return;
        
        // Check if it's a filled map (explorer map)
        if (itemId.toString().equals("minecraft:filled_map") || 
            itemId.toString().equals("minecraft:map")) {
            
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                if (playerData.getProfession().toJobType() != JobType.ADVENTURER) {
                    // Apply slowness effect
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false, true));
                    
                    // Send message occasionally (every 5 seconds)
                    if (player.level().getGameTime() % 100 == 0) {
                        player.sendSystemMessage(Component.literal("§c[🚫] 您无法破译这张地图。请联系冒险家协助解读。"));
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Block block = event.getState().getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);

        if (FARMER_MACHINES.contains(blockId)) {
            if (event.getEntity() instanceof Player player) {
                player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                    if (playerData.getProfession().toJobType() != com.symbioticlaw.data.JobType.FARMER) {
                        event.setCanceled(true);
                        player.sendSystemMessage(Component.literal("§c[🚫 权限不足] 警告：操作自动化农林设备需要《有机资源开采证》。请联系资深农夫。"));
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Check and refresh daily quota
            DailyQuotaSystem.checkAndRefreshQuota(player);
        }
    }
    
    // ================== Tetra 锤子使用封锁 ==================
    
    /**
     * 封锁非铁匠使用Tetra锤子
     * Tetra锤子是铁匠的专业工具，用于锻造和改造装备
     */
    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) return;
        
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        
        if (itemId == null) return;
        
        // 检查是否是Tetra锤子 (tetra:hammer_* 或类似命名)
        if (isTetraHammer(itemId)) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                if (playerData.getProfession().toJobType() != JobType.BLACKSMITH) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.literal(
                        "§c[🚫 权限不足] 警告：您未持有《精密锻造许可证》，无法使用专业锻造锤。"));
                }
            });
        }
    }
    
    /**
     * 检查物品是否是Tetra锤子
     */
    private static boolean isTetraHammer(ResourceLocation itemId) {
        // 使用BlacksmithProfessionManager的检测逻辑
        if (!itemId.getNamespace().equals("tetra")) return false;
        
        String path = itemId.getPath().toLowerCase();
        // 匹配各种Tetra锤子
        return path.contains("hammer") || 
               path.contains("mallet") ||
               (path.contains("forge") && path.contains("hammer")) ||
               (path.contains("tool") && path.contains("metal"));
    }
    
    // ================== 模组集成状态提示 ==================
    
    /**
     * 玩家登录时发送模组集成状态提示（仅OP或调试模式）
     */
    public static void sendModIntegrationInfo(ServerPlayer player) {
        if (!player.hasPermissions(2)) return; // 仅OP可见
        
        com.symbioticlaw.integration.ModIntegration integration = 
            new com.symbioticlaw.integration.ModIntegration();
        
        player.sendSystemMessage(Component.literal("§7[模组集成状态]"));
        
        // Tetra (铁匠)
        boolean tetraLoaded = com.symbioticlaw.integration.ModIntegration.isTetraLoaded();
        player.sendSystemMessage(Component.literal(
            String.format("§7- Tetra (铁匠系统): %s", tetraLoaded ? "§a已集成" : "§c未加载")));
        
        // 冒险维度模组
        java.util.List<String> adventureMods = 
            com.symbioticlaw.integration.ModIntegration.getLoadedAdventureMods();
        if (!adventureMods.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                String.format("§7- 冒险维度模组: §a%s", String.join(", ", adventureMods))));
        }
        
        // Waystones
        boolean waystonesLoaded = com.symbioticlaw.integration.ModIntegration.isWaystonesLoaded();
        player.sendSystemMessage(Component.literal(
            String.format("§7- Waystones (传送税): %s", waystonesLoaded ? "§a已集成" : "§c未加载")));
    }
}
