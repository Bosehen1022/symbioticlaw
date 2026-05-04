package com.symbioticlaw.network;

import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.WorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ServerBoundRequestExecutionListPacket {
    public ServerBoundRequestExecutionListPacket() {}

    public ServerBoundRequestExecutionListPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public static void handle(ServerBoundRequestExecutionListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.hasPermissions(2)) return;

            List<String> executionList = new ArrayList<>();
            WorldData worldData = WorldData.get(player.serverLevel());
            if (worldData == null) return;
            BlockPos corePos = worldData.getCorePosition();
            if (corePos.equals(BlockPos.ZERO)) return;

            for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
                p.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                    int tier = playerData.getClassTier();
                    if (tier == 2) return;
                    double dist = Math.sqrt(p.distanceToSqr(corePos.getX(), p.getY(), corePos.getZ()));
                    int allowed = tier == 0 ? 100 : 200;
                    boolean hasVisa = playerData.getVisaExpireTime() > p.level().getGameTime();
                    boolean hasPass = playerData.getSurvivalPassExpireTime() > p.level().getGameTime();
                    boolean allowedByPermit = (tier == 1 && hasVisa) || (tier == 0 && hasPass);
                    if (dist > allowed && !allowedByPermit) {
                        executionList.add(p.getGameProfile().getName());
                    }
                });
            }

            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundExecutionListPacket(executionList));
        });
        ctx.get().setPacketHandled(true);
    }

    public static void encode(ServerBoundRequestExecutionListPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ServerBoundRequestExecutionListPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundRequestExecutionListPacket(buffer);
    }
}
