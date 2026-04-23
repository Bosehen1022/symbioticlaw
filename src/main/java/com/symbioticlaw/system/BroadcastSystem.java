package com.symbioticlaw.system;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class BroadcastSystem {
    public static void broadcast(MinecraftServer server, Component message) {
        server.getPlayerList().broadcastSystemMessage(message, false);
    }
}
