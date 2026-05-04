
package com.symbioticlaw.network;

import com.symbioticlaw.data.MarketData;
import com.symbioticlaw.data.MarketItem;
import com.symbioticlaw.data.WorldData;
import com.symbioticlaw.gui.menu.TradeTerminalMenu;
import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import com.symbioticlaw.data.JobType;
import com.symbioticlaw.system.ProfessionSystem;
import com.symbioticlaw.professions.AdventurerProfessionManager;
import com.symbioticlaw.professions.AnglerProfessionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ServerBoundSellItemPacket {
    private static final ProfessionSystem professionSystem = new ProfessionSystem();
    private final String itemId;
    private final int amount;

    public ServerBoundSellItemPacket(String itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }

    public static void toBytes(ServerBoundSellItemPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.itemId);
        buf.writeInt(packet.amount);
    }

    public static ServerBoundSellItemPacket fromBytes(FriendlyByteBuf buf) {
        return new ServerBoundSellItemPacket(buf.readUtf(), buf.readInt());
    }

    public static void handle(ServerBoundSellItemPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (!(player.containerMenu instanceof TradeTerminalMenu menu)) return;

            if (packet.amount <= 0 || packet.amount > 64) return;
            if (packet.itemId == null || packet.itemId.length() > 128) return;

            WorldData worldData = WorldData.get(player.serverLevel());
            if (worldData == null) return;
            MarketData marketData = worldData.getMarketData();
            MarketItem marketItem = marketData.getMarketItem(packet.itemId);
            if (marketItem == null) return;

            ResourceLocation itemRl = ResourceLocation.tryParse(packet.itemId);
            Item item = itemRl != null ? ForgeRegistries.ITEMS.getValue(itemRl) : null;
            if (item == null) return;

            ItemStack itemStack = new ItemStack(item, packet.amount);
            if (player.getInventory().countItem(item) >= packet.amount) {
                player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                    com.symbioticlaw.data.MarketInfo.AdjustedPrice adjustedPrice = menu.getAdjustedPrice(marketItem);
                    double earnings = adjustedPrice.sellPrice() * packet.amount;

                    // Check professions
                    if (professionSystem.isFarmerItem(itemStack)) {
                        if (playerData.getProfession().toJobType() == JobType.FARMER) {
                            double bonus = professionSystem.getFarmerPriceBonus(playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.FARMER));
                            earnings *= (1.0 + bonus);
                            professionSystem.addFarmerXp(player, itemStack, packet.amount);
                        } else {
                            earnings *= 0.5; // -50% penalty
                            player.sendSystemMessage(Component.literal("§c[⚠ 无证经营：你没有农业经营许可证，核心收取 50% 的非法所得税。"));
                        }
                    } else if (professionSystem.isChefItem(packet.itemId)) {
                        if (playerData.getProfession().toJobType() == JobType.CHEF) {
                            double bonus = professionSystem.getChefPriceBonus(playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.CHEF));
                            earnings *= (1.0 + bonus);
                            professionSystem.addChefXp(player, itemStack, packet.amount);
                        }
                        // No penalty for non-chefs selling food
                    } else if (professionSystem.isMinerItem(packet.itemId)) {
                        if (playerData.getProfession().toJobType() == JobType.MINER) {
                            if (professionSystem.isMinerCompressedItem(packet.itemId) && playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.MINER) < 20) {
                                player.sendSystemMessage(Component.literal("§c你的矿工等级不足20级，无法出售压缩矿物块。"));
                                return;
                            }
                            double bonus = professionSystem.getMinerPriceBonus(playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.MINER));
                            earnings *= (1.0 + bonus);
                            professionSystem.addMinerXp(player, packet.itemId, packet.amount);
                        } else {
                            earnings *= 0.5; // 50% penalty
                            player.sendSystemMessage(Component.literal("§c[⚠ 无证经营：你没有开采许可证，核心收取 50% 的非法所得税。"));
                        }
                    } else if (professionSystem.isAnglerItem(itemStack)) {
                        // Angler items: -90% penalty for non-anglers (almost nothing)
                        if (playerData.getProfession().toJobType() == JobType.ANGLER) {
                            double bonus = professionSystem.getAnglerPriceBonus(playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.ANGLER));
                            earnings *= (1.0 + bonus);
                            professionSystem.addAnglerXp(player, itemStack, packet.amount);
                        } else {
                            earnings *= 0.1; // -90% penalty (almost zero)
                            player.sendSystemMessage(Component.literal("§c[⚠ 非法捕捞：无证挂机所得被视为海洋垃圾，核心拒绝支付报酬。"));
                        }
                    } else if (professionSystem.isAdventurerItem(itemStack)) {
                        // Adventurer items: -80% penalty for non-adventurers
                        if (playerData.getProfession().toJobType() == JobType.ADVENTURER) {
                            double bonus = professionSystem.getAdventurerPriceBonus(playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.ADVENTURER));
                            earnings *= (1.0 + bonus);
                            // Adventurer XP is based on sale amount
                            professionSystem.addAdventurerXp(player, earnings);
                            
                            // Special sound effect for high value sales
                            if (earnings >= 500.0) {
                                player.level().playSound(null, player.blockPosition(), 
                                    net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 
                                    net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.0f);
                            }
                        } else {
                            earnings *= 0.2; // -80% penalty
                            player.sendSystemMessage(Component.literal("§c[⚠ 走私警告：未经授权的古代遗物！系统予以没收并仅支付 20% 封口费。"));
                        }
                    } else if (professionSystem.isBlacksmithItem(itemStack)) {
                        // Blacksmith items: -80% penalty for non-blacksmiths
                        if (playerData.getProfession().toJobType() == JobType.BLACKSMITH) {
                            double bonus = professionSystem.getBlacksmithPriceBonus(playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.BLACKSMITH));
                            earnings *= (1.0 + bonus);
                            // Blacksmith XP is based on sale amount
                            professionSystem.addBlacksmithXp(player, earnings);
                            
                            // Check salvage level requirement
                            int level = playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.BLACKSMITH);
                            if (!professionSystem.canSalvage(itemStack, level)) {
                                player.sendSystemMessage(Component.literal("§c[!] 等级不足：此装备需要更高的铁匠等级才能拆解。"));
                                return;
                            }
                            
                            // Sound effect for metal sales
                            player.level().playSound(null, player.blockPosition(), 
                                net.minecraft.sounds.SoundEvents.ANVIL_USE, 
                                net.minecraft.sounds.SoundSource.PLAYERS, 0.3f, 1.0f);
                        } else {
                            earnings *= 0.2; // -80% penalty
                            player.sendSystemMessage(Component.literal("§c[⚠ 非法拆解：无军工许可证禁止熔炼武器，核心没收装备并罚款80%。"));
                        }
                    }

                    playerData.setBalance(playerData.getBalance() + earnings);
                    marketItem.currentStock += packet.amount;
                    player.getInventory().clearOrCountMatchingItems(p -> p.getItem() == item, packet.amount, player.getInventory());

                    PlayerDataCapability.sync(player);

                    // Daily Quota Logic for Farmer
//                    if (playerData.getJobType() == JobType.FARMER && packet.itemId.equals(playerData.getDailyQuotaItem()) && playerData.getDailyQuotaCompletionTime() == 0) {
//                        long currentProgress = playerData.getDailyQuotaProgress() + packet.amount;
//                        playerData.setDailyQuotaProgress(currentProgress);
//
//                        if (currentProgress >= playerData.getDailyQuotaRequiredAmount()) {
//                            playerData.setBalance(playerData.getBalance() + playerData.getDailyQuotaReward());
//                            playerData.setDailyQuotaCompletionTime(player.level().getGameTime());
//                            player.sendSystemMessage(Component.literal("§a[✔] 每日契约完成！获得了 $" + playerData.getDailyQuotaReward() + " 的保供奖金。"));
//                        }
//                    }

                    List<com.symbioticlaw.data.MarketInfo.MarketItemWithPrice> itemsWithPrices = new ArrayList<>();
                    for (MarketItem mi : marketData.getAllItems()) {
                        itemsWithPrices.add(new com.symbioticlaw.data.MarketInfo.MarketItemWithPrice(mi, menu.getAdjustedPrice(mi)));
                    }
                    NetworkHandler.sendToPlayer(player, new ClientBoundUpdateMarketPacket(itemsWithPrices));
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
