package com.symbioticlaw.data;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import com.symbioticlaw.system.ProfessionSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MarketData extends SavedData {

    private static final ProfessionSystem professionSystem = new ProfessionSystem();
    private static final String DATA_NAME = "symbioticlaw_market_data";
    private final Map<String, MarketItem> marketItems = new HashMap<>();

    public MarketData() {
        super();
    }

    public MarketData(CompoundTag tag) {
        ListTag list = tag.getList("marketItems", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            MarketItem item = MarketItem.deserializeNBT(itemTag);
            marketItems.put(item.itemId, item);
        }
    }

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        ListTag list = new ListTag();
        for (MarketItem item : marketItems.values()) {
            list.add(item.serializeNBT());
        }
        pCompoundTag.put("marketItems", list);
        return pCompoundTag;
    }

    public void load(CompoundTag tag) {
        marketItems.clear();
        ListTag list = tag.getList("marketItems", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            MarketItem item = MarketItem.deserializeNBT(itemTag);
            marketItems.put(item.itemId, item);
        }
    }

    public static MarketData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(MarketData::new, MarketData::new, DATA_NAME);
    }

    public MarketItem getMarketItem(String itemId) {
        return marketItems.get(itemId);
    }

    public Collection<MarketItem> getAllItems() {
        return marketItems.values();
    }

    public void setMarketItem(String itemId, double basePrice, int targetStock) {
        MarketItem item = marketItems.get(itemId);
        if (item != null) {
            item.basePrice = basePrice;
            item.targetStock = targetStock;
        } else {
            item = new MarketItem(itemId, basePrice, targetStock, targetStock); // Start with stock at target
            marketItems.put(itemId, item);
        }
        setDirty();
    }

    public static double getRawPrice(MarketItem item) {
        double priceMultiplier = (double) item.targetStock / Math.max(1, item.currentStock);
        priceMultiplier = Mth.clamp(priceMultiplier, 0.1, 10.0);
        return item.basePrice * priceMultiplier;
    }

    public static double getBuyPrice(Player player, MarketItem item) {
        Optional<IPlayerData> data = player.getCapability(PlayerDataCapability.INSTANCE).resolve();
        if (data.isPresent()) {
            IPlayerData playerData = data.get();
            double rawPrice = getRawPrice(item);
            double classMultiplier = 1.0;
            // Citizens (Tier 1+) vs Pariahs (Tier 0)
            if (playerData.getClassTier() == 0) { // Pariah/Remnant
                classMultiplier = 1.5;
            }
            return rawPrice * 1.1 * classMultiplier; // 10% tax + class modifier
        }
        return Double.MAX_VALUE; // Should not happen
    }

    public static double getSellPrice(Player player, MarketItem item) {
        Optional<IPlayerData> data = player.getCapability(PlayerDataCapability.INSTANCE).resolve();
        if (data.isPresent()) {
            IPlayerData playerData = data.get();
            double rawPrice = getRawPrice(item);
            double classMultiplier = 1.0;
            if (playerData.getClassTier() == 0) { // Pariah/Remnant
                classMultiplier = 0.5; // Exploitation
            }
            return rawPrice * classMultiplier;
        }
        return 0.0; // Should not happen
    }

    // For GUI display only - does not change state
    public static double calculateBulkBuyPrice(Player player, MarketItem item, int quantity) {
        if (item.currentStock < quantity) {
            return -1; // Indicate not enough stock
        }
        Optional<IPlayerData> data = player.getCapability(PlayerDataCapability.INSTANCE).resolve();
        if (data.isEmpty()) {
            return Double.MAX_VALUE;
        }
        int classTier = data.get().getClassTier();
        double buyMultiplier = (classTier == 0) ? 1.5 : 1.0;

        double totalCost = 0;
        int simulatedStock = item.currentStock;

        for (int i = 0; i < quantity; i++) {
            double priceMultiplier = (double) item.targetStock / Math.max(1, simulatedStock);
            priceMultiplier = Mth.clamp(priceMultiplier, 0.1, 10.0);
            double rawPrice = item.basePrice * priceMultiplier;
            totalCost += rawPrice * 1.1 * buyMultiplier;
            simulatedStock--;
        }
        return totalCost;
    }

    // For GUI display only - does not change state
    public static double calculateBulkSellPrice(Player player, MarketItem item, int quantity) {
        Optional<IPlayerData> data = player.getCapability(PlayerDataCapability.INSTANCE).resolve();
        if (data.isEmpty()) {
            return 0.0;
        }
        int classTier = data.get().getClassTier();
        double sellMultiplier = (classTier == 0) ? 0.5 : 1.0;

        double totalGain = 0;
        int simulatedStock = item.currentStock;

        for (int i = 0; i < quantity; i++) {
            double priceMultiplier = (double) item.targetStock / Math.max(1, simulatedStock);
            priceMultiplier = Mth.clamp(priceMultiplier, 0.1, 10.0);
            double rawPrice = item.basePrice * priceMultiplier;
            totalGain += rawPrice * sellMultiplier;
            simulatedStock++;
        }
        return totalGain;
    }

    public boolean buyItem(Player player, String itemId, int quantity) {
        MarketItem marketItem = getMarketItem(itemId);
        // Hard limit: out of stock
        if (marketItem == null || marketItem.currentStock <= 0) {
            return false;
        }

        int originalQuantity = quantity;
        int availableQuantity = Math.min(originalQuantity, marketItem.currentStock);

        if (availableQuantity <= 0) {
            return false;
        }

        Optional<IPlayerData> playerDataOpt = player.getCapability(PlayerDataCapability.INSTANCE).resolve();
        if (playerDataOpt.isEmpty()) {
            return false;
        }
        IPlayerData playerData = playerDataOpt.get();
        int classTier = playerData.getClassTier();
        double buyMultiplier = (classTier == 0) ? 1.5 : 1.0;

        // Step 1: Simulate to calculate total cost without changing state
        double totalCost = 0;
        MarketItem simulatedItem = new MarketItem(marketItem.itemId, marketItem.basePrice, marketItem.targetStock, marketItem.currentStock);

        for (int i = 0; i < availableQuantity; i++) {
            double rawPrice = getRawPrice(simulatedItem);
            totalCost += rawPrice * 1.1 * buyMultiplier;
            simulatedItem.currentStock--;
        }

        // Step 2: Check if player can afford it
        if (playerData.getBalance() < totalCost) {
            return false; // Not enough money, no state was changed
        }

        // Step 3: Execute the transaction now that it's validated
        playerData.addBalance(-totalCost);
        marketItem.currentStock -= availableQuantity; // Update real stock
        net.minecraft.resources.ResourceLocation itemRl = net.minecraft.resources.ResourceLocation.tryParse(itemId);
        if (itemRl == null) return false;
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemRl);
        if (item == net.minecraft.world.item.Items.AIR) return false;
        player.getInventory().add(new net.minecraft.world.item.ItemStack(item, availableQuantity));

        setDirty();
        return true;
    }

    public boolean sellItem(Player player, String itemId, int quantity) {
        MarketItem marketItem = getMarketItem(itemId);
        if (marketItem == null) {
            return false; // Item not traded in the market
        }

        net.minecraft.resources.ResourceLocation itemRl = net.minecraft.resources.ResourceLocation.tryParse(itemId);
        if (itemRl == null) return false;
        var itemToSell = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemRl);
        if (itemToSell == net.minecraft.world.item.Items.AIR) return false;
        if (player.getInventory().countItem(itemToSell) < quantity) {
            return false; // Not enough items in inventory
        }

        Optional<IPlayerData> playerDataOpt = player.getCapability(PlayerDataCapability.INSTANCE).resolve();
        if (playerDataOpt.isEmpty()) {
            return false;
        }
        IPlayerData playerData = playerDataOpt.get();
        int classTier = playerData.getClassTier();
        double sellMultiplier = (classTier == 0) ? 0.5 : 1.0;

        var itemStackToSell = new net.minecraft.world.item.ItemStack(itemToSell, quantity);
        if (professionSystem.isMinerItem(itemId)) {
            if (playerData.getProfession() == com.symbioticlaw.professions.Profession.MINER) {
                if (playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.MINER) < 20 && professionSystem.isMinerCompressedItem(itemId)) {
                    player.sendSystemMessage(Component.literal("§c你的矿工等级不足20级，无法出售压缩矿物块。"));
                    return false;
                }
                double bonus = professionSystem.getMinerPriceBonus(playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.MINER));
                sellMultiplier = 1.0 + bonus;
                professionSystem.addMinerXp((net.minecraft.server.level.ServerPlayer) player, itemId, quantity);
            } else {
                sellMultiplier = 0.5; // 50% penalty
                player.sendSystemMessage(Component.literal("§c[⚠] 无证经营：你没有开采许可证，核心收取 50% 的非法所得税。"));
            }
        } else if (professionSystem.isFarmerItem(itemStackToSell)) {
            if (playerData.getProfession() == com.symbioticlaw.professions.Profession.FARMER) {
                if (professionSystem.isFarmerProcessedItem(itemStackToSell) && playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.FARMER) < 20) {
                    player.sendSystemMessage(Component.literal("§c你的农夫等级不足20级，无法出售压缩/加工品。"));
                    return false;
                }
                double bonus = professionSystem.getFarmerPriceBonus(playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.FARMER));
                sellMultiplier = 1.0 + bonus;
                professionSystem.addFarmerXp((net.minecraft.server.level.ServerPlayer) player, itemStackToSell, quantity);
            } else {
                sellMultiplier = 0.5; // 50% penalty
                player.sendSystemMessage(Component.literal("§c[⚠] 无证经营：你没有农业经营许可证，核心收取 50% 的非法所得税。"));
            }
        }

        // All checks passed, perform transaction
        player.getInventory().removeItem(new net.minecraft.world.item.ItemStack(itemToSell, quantity));

        double totalGain = 0;
        for (int i = 0; i < quantity; i++) {
            double rawPrice = getRawPrice(marketItem);
            totalGain += rawPrice * sellMultiplier;
            marketItem.currentStock++; // Inject into stock
        }

        WorldData worldData = WorldData.get(player.getServer().overworld());
        if (itemId.equals(worldData.getDailyQuotaItem()) && quantity >= worldData.getDailyQuotaAmount() && !worldData.hasCompletedDailyQuota(player.getUUID())) {
            totalGain += worldData.getDailyQuotaReward();
            worldData.setDailyQuotaCompleted(player.getUUID());
            player.sendSystemMessage(Component.literal("§a你已完成每日配额，获得额外奖励！"));
        }

        playerData.addBalance(totalGain);
        setDirty();
        return true;
    }
}
