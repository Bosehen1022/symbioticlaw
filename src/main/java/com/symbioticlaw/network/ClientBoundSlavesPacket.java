package com.symbioticlaw.network;

import com.symbioticlaw.gui.screen.ReleaseSlaveScreen;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ClientBoundSlavesPacket {
    private final List<SlaveInfo> slaves;

    public ClientBoundSlavesPacket(List<SlaveInfo> slaves) {
        this.slaves = slaves;
    }

    public ClientBoundSlavesPacket(FriendlyByteBuf buf) {
        this.slaves = new ArrayList<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            this.slaves.add(new SlaveInfo(buf.readUUID(), buf.readUtf()));
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.slaves.size());
        for (SlaveInfo info : this.slaves) {
            buf.writeUUID(info.uuid);
            buf.writeUtf(info.name);
        }
    }

    public static void handle(ClientBoundSlavesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft.getInstance().setScreen(new ReleaseSlaveScreen(Minecraft.getInstance().screen, msg.slaves));
            });
        });
        ctx.get().setPacketHandled(true);
    }

    public static void encode(ClientBoundSlavesPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ClientBoundSlavesPacket decode(FriendlyByteBuf buffer) {
        return new ClientBoundSlavesPacket(buffer);
    }

    public record SlaveInfo(UUID uuid, String name) {}
}
