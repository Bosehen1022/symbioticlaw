package com.symbioticlaw.network;

import com.symbioticlaw.gui.screen.ExecutionListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientBoundExecutionListPacket {
    private final List<String> executionList;

    public ClientBoundExecutionListPacket(List<String> executionList) {
        this.executionList = executionList;
    }

    public ClientBoundExecutionListPacket(FriendlyByteBuf buf) {
        this.executionList = new ArrayList<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            this.executionList.add(buf.readUtf());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.executionList.size());
        for (String name : this.executionList) {
            buf.writeUtf(name);
        }
    }

    public static void handle(ClientBoundExecutionListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft.getInstance().setScreen(new ExecutionListScreen(Minecraft.getInstance().screen, msg.executionList));
            });
        });
        ctx.get().setPacketHandled(true);
    }

    public static void encode(ClientBoundExecutionListPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ClientBoundExecutionListPacket decode(FriendlyByteBuf buffer) {
        return new ClientBoundExecutionListPacket(buffer);
    }
}
