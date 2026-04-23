package com.symbioticlaw.network;

import com.symbioticlaw.contract.ContractManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerBoundSignContractPacket {
    private final UUID slaveId;

    public ServerBoundSignContractPacket(UUID slaveId) {
        this.slaveId = slaveId;
    }

    public ServerBoundSignContractPacket(FriendlyByteBuf buf) {
        this.slaveId = buf.readUUID();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.slaveId);
    }

    public static void handle(ServerBoundSignContractPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ServerPlayer slave = player.getServer().getPlayerList().getPlayer(msg.slaveId);
            if (slave != null) {
                ContractManager.INSTANCE.offerContract(player, slave);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void encode(ServerBoundSignContractPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ServerBoundSignContractPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundSignContractPacket(buffer);
    }
}
