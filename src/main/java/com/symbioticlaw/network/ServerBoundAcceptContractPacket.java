package com.symbioticlaw.network;

import com.symbioticlaw.data.ContractData;
import com.symbioticlaw.contract.Contract;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerBoundAcceptContractPacket {
    private final UUID contractId;

    public ServerBoundAcceptContractPacket(UUID contractId) {
        this.contractId = contractId;
    }

    public ServerBoundAcceptContractPacket(FriendlyByteBuf buffer) {
        this.contractId = buffer.readUUID();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(contractId);
    }

    public static ServerBoundAcceptContractPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundAcceptContractPacket(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ContractData contractData = ContractData.get(player.serverLevel());
                if (contractData == null) return;
                Contract contract = contractData.getContract(this.contractId);
                if (contract != null) {
                    if (contract.isExpired()) return;
                    if (contract.status != com.symbioticlaw.contract.ContractStatus.OPEN) return;
                    if (contract.issuerId.equals(player.getUUID())) return;
                    contract.accept(player.getUUID(), player.getGameProfile().getName());
                    contractData.setDirty();
                }
            }
        });
        context.setPacketHandled(true);
    }
}
