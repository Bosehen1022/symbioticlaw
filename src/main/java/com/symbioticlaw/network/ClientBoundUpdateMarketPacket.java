package com.symbioticlaw.network;

import com.symbioticlaw.data.MarketInfo;
import com.symbioticlaw.data.MarketItem;
import com.symbioticlaw.gui.menu.TradeTerminalMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class ClientBoundUpdateMarketPacket {

    private final List<MarketInfo.MarketItemWithPrice> marketData;

    public ClientBoundUpdateMarketPacket(List<MarketInfo.MarketItemWithPrice> marketData) {
        this.marketData = marketData;
    }

    public static void toBytes(ClientBoundUpdateMarketPacket packet, FriendlyByteBuf buf) {
        buf.writeCollection(packet.marketData, (byteBuf, data) -> {
            byteBuf.writeUtf(data.item().itemId);
            byteBuf.writeDouble(data.item().basePrice);
            byteBuf.writeInt(data.item().targetStock);
            byteBuf.writeInt(data.item().currentStock);
            byteBuf.writeDouble(data.item().lastCyclePrice);
            byteBuf.writeDouble(data.adjustedPrice().buyPrice());
            byteBuf.writeDouble(data.adjustedPrice().sellPrice());
        });
    }

    public static ClientBoundUpdateMarketPacket fromBytes(FriendlyByteBuf buf) {
        List<MarketInfo.MarketItemWithPrice> marketData = buf.readList(byteBuf -> {
            MarketItem item = new MarketItem(
                byteBuf.readUtf(),
                byteBuf.readDouble(),
                byteBuf.readInt(),
                byteBuf.readInt()
            );
            item.lastCyclePrice = byteBuf.readDouble();
            MarketInfo.AdjustedPrice adjustedPrice = new MarketInfo.AdjustedPrice(
                byteBuf.readDouble(),
                byteBuf.readDouble()
            );
            return new MarketInfo.MarketItemWithPrice(item, adjustedPrice);
        });
        return new ClientBoundUpdateMarketPacket(marketData);
    }

    public static void handle(ClientBoundUpdateMarketPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.player != null && mc.player.containerMenu instanceof TradeTerminalMenu menu) {
                    menu.marketItemsWithPrices.clear();
                    menu.marketItemsWithPrices.addAll(packet.marketData);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
