package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundSellItemsPacket {
    public ServerBoundSellItemsPacket() {}

    public ServerBoundSellItemsPacket(FriendlyByteBuf buffer) {}

    public void encode(FriendlyByteBuf buffer) {}

    public static ServerBoundSellItemsPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundSellItemsPacket(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Placeholder logic
        });
        context.setPacketHandled(true);
    }
}
