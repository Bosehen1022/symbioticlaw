package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundOpenContractsPacket {
    public ClientBoundOpenContractsPacket() {}

    public ClientBoundOpenContractsPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public static ClientBoundOpenContractsPacket fromBytes(FriendlyByteBuf buf) {
        return new ClientBoundOpenContractsPacket(buf);
    }

    public static void handle(ClientBoundOpenContractsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {

        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void encode(ClientBoundOpenContractsPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ClientBoundOpenContractsPacket decode(FriendlyByteBuf buffer) {
        return fromBytes(buffer);
    }
}
