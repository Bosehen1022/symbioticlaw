package com.symbioticlaw.system;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.MarketData;
import com.symbioticlaw.data.MarketItem;
import com.symbioticlaw.data.WorldData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReportSystem {

    public void tick(MinecraftServer server) {
        WorldData worldData = WorldData.get(server.overworld());
        MarketData marketData = worldData.getMarketData();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        // --- Broadcast Channels ---
        broadcastHeader(server);

        // --- Content Sections ---
        broadcastMarketVolatility(server, marketData);
        broadcastFinancialCredit(server, players);
        broadcastMacroMetrics(server, worldData);

        // --- Reset Cycle Data ---
        resetCycleData(players, marketData, worldData);
    }

    private void broadcastHeader(MinecraftServer server) {
        long day = server.overworld().getGameTime() / 24000;
        Component title = Component.literal("第 " + day + " 周期结算完成。");

        // Title
        ClientboundSetTitlesAnimationPacket times = new ClientboundSetTitlesAnimationPacket(10, 70, 20);
        ClientboundSetTitleTextPacket titlePacket = new ClientboundSetTitleTextPacket(title);

        server.getPlayerList().getPlayers().forEach(player -> {
            // Sound
            player.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.MASTER, 1.0f, 1.0f);
            // Title
            player.connection.send(times);
            player.connection.send(titlePacket);
            // Chat
            player.sendSystemMessage(Component.literal("§6[行政日报] 第 " + day + " 周期结算完成。"));
        });
    }

    private void broadcastMarketVolatility(MinecraftServer server, MarketData marketData) {
        List<MarketItem> volatileItems = marketData.getAllItems().stream()
                .filter(item -> item.lastCyclePrice > 0)
                .map(item -> {
                    double currentPrice = getCurrentPrice(item);
                    double volatility = Math.abs(currentPrice / item.lastCyclePrice - 1);
                    return new VolatileItem(item, volatility, currentPrice);
                })
                .sorted(Comparator.comparingDouble(VolatileItem::getVolatility).reversed())
                .limit(3)
                .map(VolatileItem::getItem)
                .collect(Collectors.toList());

        boolean fluctuation = false; // Renamed for clarity, meaning 'fluctuation'
        for (MarketItem item : volatileItems) {
            double currentPrice = getCurrentPrice(item);
            double change = (currentPrice / item.lastCyclePrice - 1) * 100;

            if (change > 30) {
                server.getPlayerList().broadcastSystemMessage(Component.literal(String.format("§a[📈 机会] %s 极度紧缺，价格飙升 +%.0f%%。核心建议：加大生产投入。", new ItemStack(ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.parse(item.itemId))).getDisplayName().getString(), change)), false);
                fluctuation = true;
            } else if (change < -30) {
                server.getPlayerList().broadcastSystemMessage(Component.literal(String.format("§c[📉 熔断] %s 产能过剩，价格暴跌 -%.0f%%。核心建议：立即去库存。", new ItemStack(ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.parse(item.itemId))).getDisplayName().getString(), Math.abs(change))), false);
                fluctuation = true;
            }
        }

        if (!fluctuation) {
            server.getPlayerList().broadcastSystemMessage(Component.literal("§7[⚖ 市场] 本周期物价平稳。秩序井然。"), false);
        }
    }

    private void broadcastFinancialCredit(MinecraftServer server, List<ServerPlayer> players) {
        // The Patriot
        players.stream()
                .max(Comparator.comparingDouble(p -> p.getCapability(PlayerDataCapability.INSTANCE).map(IPlayerData::getTaxPaidThisCycle).orElse(0.0)))
                .ifPresent(patriot -> {
                    patriot.getCapability(PlayerDataCapability.INSTANCE).ifPresent(data -> {
                        double taxPaid = data.getTaxPaidThisCycle();
                        if (taxPaid > 0) {
                            server.getPlayerList().broadcastSystemMessage(Component.literal(String.format("§e[⭐ 荣耀] 自由公民 %s 单日纳税 $%.2f。他是繁荣的引擎。", patriot.getGameProfile().getName(), taxPaid)), false);
                        }
                    });
                });

        // The Burden
        players.stream()
                .min(Comparator.comparingDouble((ServerPlayer p) -> p.getCapability(PlayerDataCapability.INSTANCE).map(IPlayerData::getBalance).orElse(Double.MAX_VALUE))
                        .thenComparing(Comparator.comparingInt((ServerPlayer p) -> p.getCapability(PlayerDataCapability.INSTANCE).map(IPlayerData::getWelfareClaimsThisCycle).orElse(0)).reversed()))
                .ifPresent(burden -> {
                    burden.getCapability(PlayerDataCapability.INSTANCE).ifPresent(data -> {
                        server.getPlayerList().broadcastSystemMessage(Component.literal(String.format("§8[⚡ 警示] 遗民 %s 资产停滞于 $%.2f。贫穷是秩序的漏洞。", burden.getGameProfile().getName(), data.getBalance())), false);
                    });
                });
    }

    private void broadcastMacroMetrics(MinecraftServer server, WorldData worldData) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(String.format("§6[🏛️ 权力基石] 核心纪元至今，系统已累计从全体玩家处征收税款总计 §l$%.2f§r§6。感谢你们的服从，秩序因剥削而坚固。", worldData.getTotalTaxCollected())), false);
    }

    private void resetCycleData(List<ServerPlayer> players, MarketData marketData, WorldData worldData) {
        // Reset player data
        for (ServerPlayer player : players) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(data -> {
                data.setTaxPaidThisCycle(0);
                data.setWelfareClaimsThisCycle(0);
            });
        }

        // Update market prices
        for (MarketItem item : marketData.getAllItems()) {
            item.lastCyclePrice = getCurrentPrice(item);
        }

        worldData.setDirty();
    }

    private double getCurrentPrice(MarketItem item) {
        return item.basePrice * Mth.clamp((double) item.targetStock / Math.max(1, item.currentStock), 0.1, 10.0);
    }
    
    // Helper class for sorting
    private static class VolatileItem {
        private final MarketItem item;
        private final double volatility;
        private final double currentPrice;

        public VolatileItem(MarketItem item, double volatility, double currentPrice) {
            this.item = item;
            this.volatility = volatility;
            this.currentPrice = currentPrice;
        }

        public MarketItem getItem() {
            return item;
        }

        public double getVolatility() {
            return volatility;
        }
    }
}
