package com.symbioticlaw.system;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.MarketData;
import com.symbioticlaw.data.MarketItem;
import com.symbioticlaw.data.WorldData;
import com.symbioticlaw.professions.Profession;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

import com.symbioticlaw.professions.AdventurerProfessionManager;
import com.symbioticlaw.professions.AnglerProfessionManager;
import com.symbioticlaw.professions.BlacksmithProfessionManager;
import com.symbioticlaw.professions.FarmerProfessionManager;
import com.symbioticlaw.professions.MinerProfessionManager;

public class TradeSystem {

    private static final SubliminalSystem subliminalSystem = new SubliminalSystem();
    private static final ProfessionSystem professionSystem = new ProfessionSystem();

    public void sellItems(ServerPlayer player, List<ItemStack> items) {
        MarketData marketData = WorldData.get(player.serverLevel()).getMarketData();

        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            double totalValue = 0;

            for (ItemStack itemStack : items) {
                ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
                if (itemKey == null) continue;
                String itemId = itemKey.toString();
                MarketItem marketItem = marketData.getMarketItem(itemId);

                if (marketItem == null) continue;

                // Slippage calculation
                double transactionValue = 0;
                for (int i = 0; i < itemStack.getCount(); i++) {
                    double rawPrice = marketItem.basePrice * Mth.clamp((double) marketItem.targetStock / Math.max(1, marketItem.currentStock), 0.1, 10.0);
                    transactionValue += rawPrice;
                    marketItem.currentStock++; // Stock increases as player sells
                }

                double finalPrice = transactionValue;
                Profession itemProfession = getItemProfession(itemId);

                // Class-based discrimination
                if (playerData.getClassTier() == 0) { // Pariah
                    finalPrice *= 0.5;
                    subliminalSystem.triggerMessage(player, SubliminalSystem.MessageType.PARIAH_SALE);
                } else {
                    // Profession bonus for non-pariahs
                    if (playerData.getProfession() == itemProfession) {
                        int level = playerData.getProfessionLevel(itemProfession);
                        double bonus = 0;
                        if (level > 45) bonus = 0.15; else if (level > 30) bonus = 0.10; else if (level > 10) bonus = 0.05;
                        finalPrice *= (1 + bonus);
                        awardXp(player, playerData, itemProfession, itemStack, finalPrice);
                    } else {
                        double tax = finalPrice * 0.5;
                        finalPrice *= 0.5; // Non-pro penalty
                        playerData.addTaxPaidThisCycle(tax);
                        WorldData.get(player.serverLevel()).addTotalTaxCollected(tax);
                        // Check item profession for specific message
                        if (itemProfession == Profession.MINER) {
                            player.sendSystemMessage(Component.literal("§c[⚠ 无证经营：你没有开采许可证，核心收取 50% 的非法所得税。"));
                        } else if (itemProfession == Profession.FARMER) {
                            player.sendSystemMessage(Component.literal("§c[⚠ 无证经营：你没有农业经营许可证，核心收取 50% 的非法所得税。"));
                        } else if (itemProfession == Profession.ANGLER) {
                            // Angler has -90% penalty (almost nothing)
                            player.sendSystemMessage(Component.literal("§c[⚠ 非法捕捞：无证挂机所得被视为海洋垃圾，核心拒绝支付报酬。"));
                        } else if (itemProfession == Profession.ADVENTURER) {
                            player.sendSystemMessage(Component.literal("§c[⚠ 走私警告：未经授权的古代遗物！系统予以没收并仅支付 20% 封口费。"));
                        } else if (itemProfession == Profession.BLACKSMITH) {
                            player.sendSystemMessage(Component.literal("§c[⚠ 非法拆解：无军工许可证禁止熔炼武器，核心没收装备并罚款50%。"));
                        } else {
                            player.sendSystemMessage(Component.literal("§c[⚠ 无证经营：核心收取 50% 的非法所得税。"));
                        }
                    }
                }
                totalValue += finalPrice;
            }

            if (playerData.getSlaveOwnerUUID() != null) {
                double garnishedAmount = totalValue * 0.4;
                totalValue -= garnishedAmount;

                if (player.getServer() != null) {
                    ServerPlayer owner = player.getServer().getPlayerList().getPlayer(playerData.getSlaveOwnerUUID());
                    if (owner != null) {
                        owner.getCapability(PlayerDataCapability.INSTANCE).ifPresent(ownerData -> {
                            ownerData.addBalance(garnishedAmount);
                            owner.sendSystemMessage(Component.literal(String.format("§a[税金] 您的奴隶 " + player.getGameProfile().getName() + " 上缴收益 +$%.2f", garnishedAmount)));
                        });
                    } else {
                        // Owner is offline, add to unclaimed income
                        WorldData.get(player.serverLevel()).getUnclaimedIncomeData().addIncome(playerData.getSlaveOwnerUUID(), garnishedAmount);
                        WorldData.get(player.serverLevel()).setDirty();
                    }
                }
                player.sendSystemMessage(Component.literal(String.format("§7(扣除 $%.2f 上缴债主)", garnishedAmount)));
            }

            playerData.addBalance(totalValue);
            player.sendSystemMessage(Component.literal(String.format("§a你出售了物品，获得 $%.2f", totalValue)));
        });
    }

    // This needs to be expanded for all professions
    private Profession getItemProfession(String itemId) {
        ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemId)));
        if (FarmerProfessionManager.isFarmerItem(itemStack)) {
            return Profession.FARMER;
        }
        if (MinerProfessionManager.isMinerItem(itemStack)) {
            return Profession.MINER;
        }
        if (AnglerProfessionManager.isAnglerItem(itemStack)) {
            return Profession.ANGLER;
        }
        if (AdventurerProfessionManager.isAdventurerItem(itemStack)) {
            return Profession.ADVENTURER;
        }
        return Profession.UNEMPLOYED;
    }
    
    // Generic XP awarding method
    private void awardXp(ServerPlayer player, IPlayerData playerData, Profession prof, ItemStack itemStack, double saleAmount) {
        switch (prof) {
            case FARMER:
                professionSystem.addFarmerXp(player, itemStack, itemStack.getCount());
                break;
            case MINER:
                professionSystem.addMinerXp(player, ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString(), itemStack.getCount());
                break;
            case ANGLER:
                professionSystem.addAnglerXp(player, itemStack, itemStack.getCount());
                break;
            case ADVENTURER:
                // Adventurer XP is based on sale amount, not item count
                professionSystem.addAdventurerXp(player, saleAmount);
                break;
            case BLACKSMITH:
                // Blacksmith XP is based on sale amount
                professionSystem.addBlacksmithXp(player, saleAmount);
                break;
            // Add other professions here
        }
    }

    public void buyItems(ServerPlayer player, String itemId, int amount) {
        Item itemToBuy = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemId));
        if (itemToBuy == null) {
            player.sendSystemMessage(Component.literal("§c物品不存在。"));
            return;
        }

        MarketData marketData = WorldData.get(player.serverLevel()).getMarketData();
        MarketItem marketItem = marketData.getMarketItem(itemId);
        if (marketItem == null || marketItem.currentStock < amount) {
            player.sendSystemMessage(Component.literal("§c库存不足。"));
            return;
        }

        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            double totalCost = 0;
            for (int i = 0; i < amount; i++) {
                double rawPrice = marketItem.basePrice * Mth.clamp((double) marketItem.targetStock / Math.max(1, marketItem.currentStock), 0.1, 10.0);
                totalCost += rawPrice;
                marketItem.currentStock--;
            }

            double finalCost = totalCost;
            double tax = 0;
            if (playerData.getClassTier() == 0) { // Pariah
                tax = totalCost * 0.5;
                finalCost *= 1.5;
            } else {
                tax = totalCost * 0.1; // Standard 10% tax
                finalCost *= 1.1;
            }
            playerData.addTaxPaidThisCycle(tax);
            WorldData.get(player.serverLevel()).addTotalTaxCollected(tax);


            if (playerData.getBalance() < finalCost) {
                player.sendSystemMessage(Component.literal("§c余额不足。"));
                return; // Should refund the stock change, but this is complex.
            }

            playerData.setBalance(playerData.getBalance() - finalCost);
            player.getInventory().add(new ItemStack(itemToBuy, amount));
            player.sendSystemMessage(Component.literal(String.format("§a你购买了 %d x %s，花费 $%.2f", amount, itemToBuy.getDescription().getString(), finalCost)));
        });
    }
}
