package com.symbioticlaw.network;

import com.symbioticlaw.contract.ContractManager;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.WorldData;
import com.symbioticlaw.event.CoreAmbienceHandler;
import net.minecraft.core.BlockPos;
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

            WorldData worldData = WorldData.get(player.serverLevel());
            if (worldData == null) return;
            BlockPos corePos = worldData.getCorePosition();
            if (corePos.equals(BlockPos.ZERO) || !CoreAmbienceHandler.isNearCore(player, corePos)) {
                return;
            }

            ServerPlayer slave = player.getServer().getPlayerList().getPlayer(msg.slaveId);
            if (slave != null) {
                player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(ownerData -> {
                    if (ownerData.getClassTier() < 2) return;
                    if (ownerData.getMasterUUID() != null) return;

                    slave.getCapability(PlayerDataCapability.INSTANCE).ifPresent(slaveData -> {
                        if (slaveData.getClassTier() != 0) return;
                        if (slaveData.getMasterUUID() != null) return;
                        ContractManager.INSTANCE.offerContract(player, slave);
                    });
                });
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
