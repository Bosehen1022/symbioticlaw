package com.symbioticlaw.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import com.symbioticlaw.gui.hud.BorderWarningOverlay;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundWarningPacket {
    private final String message;
    private final WarningType type;

    private final int countdown;

    public ClientBoundWarningPacket(String message, WarningType type, int countdown) {
        this.message = message;
        this.type = type;
        this.countdown = countdown;
    }

    public ClientBoundWarningPacket(String message, WarningType type) {
        this(message, type, 0);
    }

    public ClientBoundWarningPacket(FriendlyByteBuf buf) {
        this.message = buf.readUtf();
        this.type = buf.readEnum(WarningType.class);
        this.countdown = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.message);
        buf.writeEnum(this.type);
        buf.writeInt(this.countdown);
    }

    public static void handle(ClientBoundWarningPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                switch (msg.type) {
                    case TITLE -> {
                        Minecraft.getInstance().gui.setTimes(20, 100, 20);
                        Minecraft.getInstance().gui.setTitle(Component.literal(msg.message));
                        Minecraft.getInstance().gui.setSubtitle(Component.literal(""));
                    }
                    case TOAST -> {
                        SystemToast.add(Minecraft.getInstance().getToasts(), SystemToast.SystemToastIds.TUTORIAL_HINT, Component.literal("Warning"), Component.literal(msg.message));
                    }
                    case BORDER_WARNING -> {
                        String[] parts = msg.message.split("\\n");
                        Minecraft.getInstance().gui.setTitle(Component.literal(parts[0]));
                        Minecraft.getInstance().gui.setSubtitle(Component.literal(parts.length > 1 ? parts[1] : ""));
                        BorderWarningOverlay.setShow(true, msg.countdown);
                    }
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

    public static void encode(ClientBoundWarningPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ClientBoundWarningPacket decode(FriendlyByteBuf buffer) {
        return new ClientBoundWarningPacket(buffer);
    }

    public enum WarningType {
        TITLE, TOAST, BORDER_WARNING
    }
}
