package com.symbioticlaw.event;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.professions.Profession;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Symbioticlaw.MODID)
public class ProfessionEvents {

    private static final Map<Profession, Set<ResourceLocation>> LOCKED_BLOCKS = Map.of(
            Profession.MINER, Set.of(
                    new ResourceLocation("create", "mechanical_drill"),
                    new ResourceLocation("create", "crushing_wheel")
            ),
            Profession.FARMER, Set.of(
                    new ResourceLocation("create", "mechanical_harvester"),
                    new ResourceLocation("create", "mechanical_saw"),
                    new ResourceLocation("create", "mechanical_plough")
            ),
            Profession.CHEF, Set.of(
                    new ResourceLocation("farmersdelight", "cooking_pot"),
                    new ResourceLocation("farmersdelight", "cutting_board"),
                    new ResourceLocation("create", "mixer"),
                    new ResourceLocation("create", "spout")
            ),
            Profession.BLACKSMITH, Set.of(
                    new ResourceLocation("tetra", "workbench"),
                    new ResourceLocation("tetra", "basic_workbench"),
                    new ResourceLocation("tetra", "forged_workbench"),
                    new ResourceLocation("tetra", "hammer_base"),
                    new ResourceLocation("tetra", "transfer_unit")
            )
    );

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            checkAndCancel(player, event.getPlacedBlock().getBlock(), event);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Block block = event.getLevel().getBlockState(event.getHitVec().getBlockPos()).getBlock();
        checkAndCancel(event.getEntity(), block, event);
    }

    private static void checkAndCancel(net.minecraft.world.entity.player.Player player, Block block, net.minecraftforge.eventbus.api.Event event) {
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            Profession playerProfession = playerData.getProfession();

            for (Map.Entry<Profession, Set<ResourceLocation>> entry : LOCKED_BLOCKS.entrySet()) {
                Profession requiredProfession = entry.getKey();
                if (entry.getValue().contains(blockId) && playerProfession != requiredProfession) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.literal("§c[🚫 权限不足] 你没有操作此设备的许可。需要 " + requiredProfession.getDisplayName().getString() + " 职业。"));
                    break;
                }
            }
        });
    }
}
