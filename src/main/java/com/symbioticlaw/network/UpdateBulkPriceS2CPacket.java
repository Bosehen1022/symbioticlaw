package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateBulkPriceS2CPacket {

    private final double bulkPrice;
    private final double singlePrice;

    public UpdateBulkPriceS2CPacket(double bulkPrice, double singlePrice) {
        this.bulkPrice = bulkPrice;
        this.singlePrice = singlePrice;
    }

    public UpdateBulkPriceS2CPacket(FriendlyByteBuf buf) {
        this.bulkPrice = buf.readDouble();
        this.singlePrice = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(bulkPrice);
        buf.writeDouble(singlePrice);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                if (net.minecraft.client.Minecraft.getInstance().screen instanceof com.symbioticlaw.gui.screen.TradeTerminalScreen screen) {
                    screen.updateBulkPriceData(bulkPrice, singlePrice);
                }
            });
        });
        context.setPacketHandled(true);
    }
}
