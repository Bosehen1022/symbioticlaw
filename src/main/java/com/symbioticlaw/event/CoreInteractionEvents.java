package com.symbioticlaw.event;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.WorldData;
import com.symbioticlaw.gui.menu.PowerCoreMenu;
import com.symbioticlaw.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.event.TickEvent;
import com.symbioticlaw.data.JobType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Symbioticlaw.MODID)
public class CoreInteractionEvents {

    private static final int INTERACTION_RADIUS_SQUARED = 100; // 10*10 blocks
    
    // 使用延迟初始化，避免在Create模组未安装时加载不存在的方块
    private static Set<Block> MINER_MACHINES = null;
    private static Set<Block> FARMER_MACHINES = null;
    
    /**
     * 获取矿工机器集合（延迟初始化）
     */
    private static Set<Block> getMinerMachines() {
        if (MINER_MACHINES == null) {
            MINER_MACHINES = new HashSet<>();
            addBlockIfExists(MINER_MACHINES, "create:mechanical_drill");
            addBlockIfExists(MINER_MACHINES, "create:crushing_wheel");
        }
        return MINER_MACHINES;
    }
    
    /**
     * 获取农夫机器集合（延迟初始化）
     */
    private static Set<Block> getFarmerMachines() {
        if (FARMER_MACHINES == null) {
            FARMER_MACHINES = new HashSet<>();
            addBlockIfExists(FARMER_MACHINES, "create:mechanical_harvester");
            addBlockIfExists(FARMER_MACHINES, "create:mechanical_saw");
            addBlockIfExists(FARMER_MACHINES, "create:mechanical_plough");
        }
        return FARMER_MACHINES;
    }
    
    /**
     * 安全地添加方块到集合（如果方块存在且不是空气）
     */
    private static void addBlockIfExists(Set<Block> set, String blockId) {
        ResourceLocation blockRl = ResourceLocation.tryParse(blockId);
        if (blockRl == null) return;
        Block block = ForgeRegistries.BLOCKS.getValue(blockRl);
        if (block != null && block != Blocks.AIR) {
            set.add(block);
        }
    }

    /**
     * Handles right-click on The Core, checking for distance before opening the GUI.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(ModBlocks.POWER_CORE.get())) {
            event.setCanceled(true); // Prevent default block interaction
            ServerPlayer player = (ServerPlayer) event.getEntity();
            BlockPos corePosition = WorldData.get(player.serverLevel()).getCorePosition();

            // Check distance from the one true Core
            if (player.distanceToSqr(corePosition.getCenter()) > INTERACTION_RADIUS_SQUARED) {
                // Per spec: "§c⚠ 接入被拒绝：请靠近至高权力核心。"
                player.sendSystemMessage(Component.literal("§c⚠ 接入被拒绝：请靠近至高权力核心。"));
                // Per spec: "播放“访问拒绝”音效 (block.beacon.deactivate)"
                player.playNotifySound(SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);
                return;
            }

            // Open GUI
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MenuProvider menuProvider) {
                NetworkHooks.openScreen(player, menuProvider, pos);
                // Per spec: "播放气压门开启声 (block.iron_door.open 音调 0.5)"
                player.playNotifySound(SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, 0.5f);
            }
        }

        // 检查是否是职业专属机器
        checkMachineAccess(event, state);
    }
    
    /**
     * 检查机器访问权限
     */
    private static void checkMachineAccess(PlayerInteractEvent.RightClickBlock event, BlockState state) {
        Block block = state.getBlock();
        ServerPlayer player = (ServerPlayer) event.getEntity();
        
        // 检查矿工机器
        if (getMinerMachines().contains(block)) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                if (playerData.getProfession().toJobType() != JobType.MINER) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.literal("§c[🚫 权限不足] 警告：操作动力钻头需要《重型机械操作证》。请联系资深矿工。"));
                }
            });
        }
        // 检查农夫机器
        else if (getFarmerMachines().contains(block)) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                if (playerData.getProfession().toJobType() != JobType.FARMER) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.literal("§c[🚫 权限不足] 警告：操作自动化农林设备需要《有机资源开采证》。请联系资深农夫。"));
                }
            });
        }
    }

    /**
     * Periodically checks if a player with the Core GUI open has moved out of range.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player && player.containerMenu instanceof PowerCoreMenu) {
            BlockPos corePosition = WorldData.get(player.serverLevel()).getCorePosition();
            if (player.distanceToSqr(corePosition.getCenter()) > INTERACTION_RADIUS_SQUARED) {
                player.closeContainer();
                player.sendSystemMessage(Component.literal("§c你必须靠近权力核心才能进行此项操作。"));
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;

        Block block = event.getState().getBlock();
        
        // 检查矿工机器放置
        if (getMinerMachines().contains(block)) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                    if (playerData.getProfession().toJobType() != com.symbioticlaw.data.JobType.MINER) {
                        event.setCanceled(true);
                        player.sendSystemMessage(Component.literal("§c[🚫 权限不足] 警告：操作动力钻头需要《重型机械操作证》。请联系资深矿工。"));
                    }
                });
            }
        }
        // 检查农夫机器放置
        else if (getFarmerMachines().contains(block)) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                    if (playerData.getProfession().toJobType() != com.symbioticlaw.data.JobType.FARMER) {
                        event.setCanceled(true);
                        player.sendSystemMessage(Component.literal("§c[🚫 权限不足] 警告：操作自动化农林设备需要《有机资源开采证》。请联系资深农夫。"));
                    }
                });
            }
        }
    }
}
