package com.symbioticlaw.capability;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.network.NetworkHandler;
import com.symbioticlaw.network.PlayerDataSyncPacket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Symbioticlaw.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerDataCapability {
    public static final Capability<IPlayerData> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

    public static void sync(ServerPlayer player) {
        player.getCapability(INSTANCE).ifPresent(data -> {
            NetworkHandler.sendToPlayer(player, new PlayerDataSyncPacket(data));
        });
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(Symbioticlaw.MODID, "player_data"), new PlayerDataProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        oldPlayer.reviveCaps();

        oldPlayer.getCapability(INSTANCE).ifPresent(oldData -> {
            newPlayer.getCapability(INSTANCE).ifPresent(newData -> {
                newData.copyFrom(oldData);
                PlayerDataCapability.sync((ServerPlayer) newPlayer);
            });
        });

        oldPlayer.invalidateCaps();
    }
}

class PlayerDataProvider implements ICapabilitySerializable<CompoundTag> {
    private final PlayerData data = new PlayerData();
    private final LazyOptional<IPlayerData> optional = LazyOptional.of(() -> data);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return PlayerDataCapability.INSTANCE.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return data.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.deserializeNBT(nbt);
    }
}
