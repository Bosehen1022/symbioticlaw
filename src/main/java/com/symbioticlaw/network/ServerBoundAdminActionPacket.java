package com.symbioticlaw.network;

import com.symbioticlaw.data.WorldData;
import com.symbioticlaw.data.WorldData;
import com.symbioticlaw.system.BroadcastSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundAdminActionPacket {
    private final ActionType action;

    public ServerBoundAdminActionPacket(ActionType action) {
        this.action = action;
    }

    public ServerBoundAdminActionPacket(FriendlyByteBuf buf) {
        this.action = buf.readEnum(ActionType.class);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(this.action);
    }

    public static void handle(ServerBoundAdminActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.hasPermissions(2)) return;

            switch (msg.action) {
                case RESET_CORE_POSITION -> {
                    WorldData worldData = WorldData.get(player.serverLevel());
                    worldData.setCorePosition(BlockPos.ZERO);
                }
                case FORCE_BROADCAST -> {
                    BroadcastSystem.broadcast(player.getServer(), Component.literal("§c[行政日报] 这是一条强制广播。"));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void encode(ServerBoundAdminActionPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ServerBoundAdminActionPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundAdminActionPacket(buffer);
    }

    public enum ActionType {
        RESET_CORE_POSITION,
        FORCE_BROADCAST
    }
}
