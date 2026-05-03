package com.symbioticlaw.network;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ServerBoundRequestNearbyPariahsPacket {
    public ServerBoundRequestNearbyPariahsPacket() {}

    public ServerBoundRequestNearbyPariahsPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public static void handle(ServerBoundRequestNearbyPariahsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(requesterData -> {
                if (requesterData.getClassTier() < 2 && !requesterData.hasSlaves()) {
                    return;
                }

                List<ClientBoundPariahListPacket.PariahInfo> pariahs = new ArrayList<>();
                for (ServerPlayer p : player.server.getPlayerList().getPlayers()) {
                    if (p != player && p.distanceToSqr(player) <= 100) {
                        p.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                            if (playerData.getClassTier() == 0) {
                                pariahs.add(new ClientBoundPariahListPacket.PariahInfo(
                                    p.getUUID(),
                                    p.getGameProfile().getName(),
                                    playerData.getBalance(),
                                    playerData.isWelfareRecipient()
                                ));
                            }
                        });
                    }
                }
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundPariahListPacket(pariahs));
            });
        });
        ctx.get().setPacketHandled(true);
    }

    public static void encode(ServerBoundRequestNearbyPariahsPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ServerBoundRequestNearbyPariahsPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundRequestNearbyPariahsPacket(buffer);
    }
}
