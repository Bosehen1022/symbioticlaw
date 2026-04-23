package com.symbioticlaw.network;

import com.symbioticlaw.data.MarketData;
import com.symbioticlaw.data.MarketItem;
import com.symbioticlaw.data.WorldData;
import com.symbioticlaw.data.MarketInfo;
import com.symbioticlaw.gui.menu.TradeTerminalMenu;
import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ServerBoundBuyItemPacket {
    private final String itemId;
    private final int amount;

    public ServerBoundBuyItemPacket(String itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }

    public static void toBytes(ServerBoundBuyItemPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.itemId);
        buf.writeInt(packet.amount);
    }

    public static ServerBoundBuyItemPacket fromBytes(FriendlyByteBuf buf) {
        return new ServerBoundBuyItemPacket(buf.readUtf(), buf.readInt());
    }

    public static void handle(ServerBoundBuyItemPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (!(player.containerMenu instanceof TradeTerminalMenu menu)) return;

            MarketData marketData = WorldData.get(player.serverLevel()).getMarketData();
            MarketItem marketItem = marketData.getMarketItem(packet.itemId);
            if (marketItem == null) return;

            MarketInfo.AdjustedPrice adjustedPrice = menu.getAdjustedPrice(marketItem);
            double cost = adjustedPrice.buyPrice() * packet.amount;

            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                if (playerData.getBalance() >= cost) {
                    if (marketItem.currentStock >= packet.amount) {
                        playerData.setBalance(playerData.getBalance() - cost);
                        marketItem.currentStock -= packet.amount;
                        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(packet.itemId));
                        if (item != null) {
                            player.getInventory().add(new ItemStack(item, packet.amount));
                            // Sync player data back to the client
                            NetworkHandler.sendToPlayer(player, new PlayerDataSyncPacket(playerData));

                            List<MarketInfo.MarketItemWithPrice> itemsWithPrices = new ArrayList<>();
                            for (MarketItem mi : marketData.getAllItems()) {
                                itemsWithPrices.add(new MarketInfo.MarketItemWithPrice(mi, menu.getAdjustedPrice(mi)));
                            }
                            NetworkHandler.sendToPlayer(player, new ClientBoundUpdateMarketPacket(itemsWithPrices));
                        }
                    }
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
