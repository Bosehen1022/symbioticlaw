package com.symbioticlaw.mixin;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.event.IdentityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void getDisplayName(CallbackInfoReturnable<Component> cir) {
        Player player = (Player) (Object) this;
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                Component classPrefix = IdentityEvents.getClassPrefix(serverPlayer, playerData);
                Component jobSuffix = IdentityEvents.getJobSuffix(serverPlayer, playerData);
                String playerName = serverPlayer.getGameProfile().getName();
                cir.setReturnValue(Component.empty().append(classPrefix).append(" ").append(playerName).append(" ").append(jobSuffix));
            });
        }
    }
}
