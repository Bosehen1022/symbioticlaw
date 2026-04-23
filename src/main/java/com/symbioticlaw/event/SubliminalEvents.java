package com.symbioticlaw.event;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Symbioticlaw.MODID)
public class SubliminalEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (event.player instanceof ServerPlayer player) {
                player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(data -> data.setIdleTicks(0));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(data -> data.setIdleTicks(0));
        }
    }
}
