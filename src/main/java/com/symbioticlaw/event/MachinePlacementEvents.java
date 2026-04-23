package com.symbioticlaw.event;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.professions.Profession;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// 指定在 Forge 总线上注册事件
@Mod.EventBusSubscriber(modid = Symbioticlaw.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MachinePlacementEvents {

    private static final Set<ResourceLocation> MINER_MACHINES = Stream.of(
            new ResourceLocation("create", "mechanical_drill"),
            new ResourceLocation("create", "crushing_wheel"),
            new ResourceLocation("create", "crushing_wheel_controller")
    ).collect(Collectors.toSet());

    private static final Set<ResourceLocation> FARMER_MACHINES = Stream.of(
            new ResourceLocation("create", "mechanical_harvester"),
            new ResourceLocation("create", "mechanical_saw"),
            new ResourceLocation("create", "mechanical_plough")
    ).collect(Collectors.toSet());

    // 监听方块放置
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Player player)) {
            return;
        }
        Block placedBlock = event.getState().getBlock();
        if (!checkAndNotifyMachinePermission(player, placedBlock)) {
            event.setCanceled(true);
        }
    }

    // 监听右键方块交互
    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Player player = event.getEntity();
        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
        if (!checkAndNotifyMachinePermission(player, block)) {
            event.setCanceled(true);
        }
    }

    // 公共检查逻辑，减少重复代码
    private static boolean checkAndNotifyMachinePermission(Player player, Block block) {
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        if (blockId == null) return true; // Not a registered block, allow.

        IPlayerData playerData = player.getCapability(PlayerDataCapability.INSTANCE).orElse(null);
        if (playerData == null) return true; // No data, allow.

        Profession currentProfession = playerData.getProfession();

        if (MINER_MACHINES.contains(blockId) && currentProfession != Profession.MINER) {
            player.displayClientMessage(Component.literal("§c[🚫 权限不足] 警告：操作该设备需要《重型机械操作证》（矿工）。"), true);
            return false;
        }

        if (FARMER_MACHINES.contains(blockId) && currentProfession != Profession.FARMER) {
            player.displayClientMessage(Component.literal("§c[🚫 权限不足] 警告：操作该设备需要《有机资源开采证》（农夫）。"), true);
            return false;
        }

        return true;
    }
}
