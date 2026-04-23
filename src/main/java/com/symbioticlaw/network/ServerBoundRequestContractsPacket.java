package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundRequestContractsPacket {
    public ServerBoundRequestContractsPacket() {}

    public ServerBoundRequestContractsPacket(FriendlyByteBuf buffer) {}

    public void encode(FriendlyByteBuf buffer) {}

    public static ServerBoundRequestContractsPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundRequestContractsPacket(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Placeholder logic
        });
        context.setPacketHandled(true);
    }
}
