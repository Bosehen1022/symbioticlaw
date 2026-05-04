package com.symbioticlaw.network;

import com.symbioticlaw.client.ClientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundAlmanacUpdatePacket {

    private final double returnFee;
    private final double safetyRadius;
    private final int status;
    private final int coreX;
    private final int coreY;
    private final int coreZ;

    public ClientBoundAlmanacUpdatePacket(double returnFee, double safetyRadius, int status, int coreX, int coreY, int coreZ) {
        this.returnFee = returnFee;
        this.safetyRadius = safetyRadius;
        this.status = status;
        this.coreX = coreX;
        this.coreY = coreY;
        this.coreZ = coreZ;
    }

    public ClientBoundAlmanacUpdatePacket(FriendlyByteBuf buf) {
        this.returnFee = buf.readDouble();
        this.safetyRadius = buf.readDouble();
        this.status = buf.readInt();
        this.coreX = buf.readInt();
        this.coreY = buf.readInt();
        this.coreZ = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(returnFee);
        buf.writeDouble(safetyRadius);
        buf.writeInt(status);
        buf.writeInt(coreX);
        buf.writeInt(coreY);
        buf.writeInt(coreZ);
    }

    public static void handle(ClientBoundAlmanacUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientData.setAlmanacData(msg.returnFee, msg.safetyRadius, msg.status, msg.coreX, msg.coreY, msg.coreZ);
        });
        ctx.get().setPacketHandled(true);
    }

    public static ClientBoundAlmanacUpdatePacket fromBytes(FriendlyByteBuf buf) {
        return new ClientBoundAlmanacUpdatePacket(buf);
    }

    public static void encode(ClientBoundAlmanacUpdatePacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ClientBoundAlmanacUpdatePacket decode(FriendlyByteBuf buffer) {
        return fromBytes(buffer);
    }
}
