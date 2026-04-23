package com.symbioticlaw.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register() {
        INSTANCE = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.parse("symbioticlaw:messages"))
            .networkProtocolVersion(() -> "1.0")
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();

        INSTANCE.messageBuilder(TradeRequestPacket.class, id())
            .encoder(TradeRequestPacket::encode)
            .decoder(TradeRequestPacket::new)
            .consumerNetworkThread(TradeRequestPacket::handle)
            .add();
    }

    public static void sendToServer(Object message) {
        INSTANCE.sendToServer(message);
    }
}