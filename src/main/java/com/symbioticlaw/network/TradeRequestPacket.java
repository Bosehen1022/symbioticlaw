package com.symbioticlaw.network;

import com.symbioticlaw.system.ChefWholesaleSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class TradeRequestPacket {
    private final String tradeItemId;
    private final int amount;

    public TradeRequestPacket(String tradeItemId, int amount) {
        this.tradeItemId = tradeItemId;
        this.amount = amount;
    }

    public TradeRequestPacket(FriendlyByteBuf buffer) {
        this.tradeItemId = buffer.readUtf();
        this.amount = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.tradeItemId);
        buffer.writeInt(this.amount);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ChefWholesaleSystem.purchaseWholesale(player, this.tradeItemId, this.amount);
            }
        });
        context.setPacketHandled(true);
    }
}
