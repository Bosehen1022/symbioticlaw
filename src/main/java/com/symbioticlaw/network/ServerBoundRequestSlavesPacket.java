package com.symbioticlaw.network;

import com.symbioticlaw.data.WorldData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ServerBoundRequestSlavesPacket {
    public ServerBoundRequestSlavesPacket() {}

    public ServerBoundRequestSlavesPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public static void handle(ServerBoundRequestSlavesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            List<UUID> slaveUuids = WorldData.get(player.serverLevel()).getSlavesOf(player.getUUID());
            List<ClientBoundSlavesPacket.SlaveInfo> slaves = slaveUuids.stream().map(uuid -> {
                ServerPlayer slavePlayer = player.getServer().getPlayerList().getPlayer(uuid);
                if (slavePlayer != null) {
                    return new ClientBoundSlavesPacket.SlaveInfo(uuid, slavePlayer.getGameProfile().getName());
                }
                return null;
            }).filter(info -> info != null).collect(Collectors.toList());

            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundSlavesPacket(slaves));
        });
        ctx.get().setPacketHandled(true);
    }

    public static void encode(ServerBoundRequestSlavesPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ServerBoundRequestSlavesPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundRequestSlavesPacket(buffer);
    }
}
