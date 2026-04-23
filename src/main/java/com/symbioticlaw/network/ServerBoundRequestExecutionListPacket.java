package com.symbioticlaw.network;

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

            // TODO: Get actual execution list
            List<String> executionList = new ArrayList<>();
            executionList.add("Player1");
            executionList.add("Player2");

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
