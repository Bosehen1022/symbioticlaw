package com.symbioticlaw.event;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.professions.Profession;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.symbioticlaw.data.WorldData;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import com.symbioticlaw.system.TaxSystem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import com.symbioticlaw.data.UnclaimedIncomeData;
import com.symbioticlaw.network.NetworkHandler;
import com.symbioticlaw.network.PlayerDataSyncPacket;

@Mod.EventBusSubscriber(modid = Symbioticlaw.MODID)
public class IdentityEvents {
    
    private static final TaxSystem taxSystem = new TaxSystem();

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResourceKey<Level> toDimKey = event.getTo();
            
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                double baseTax = taxSystem.calculateDimensionTax(toDimKey, null);
                double finalTax = taxSystem.calculateDimensionTax(toDimKey, playerData);
                double discount = baseTax - finalTax;
                
                if (baseTax <= 0) return; // No tax for Overworld

                if (playerData.getBalance() < finalTax) {
                    playerData.setBalance(0);
                    if (playerData.getClassTier() != 0) {
                        playerData.setClassTier(0);
                        playerData.setFallen(true);
                    }
                    player.sendSystemMessage(Component.literal(String.format("§c余额不足以支付维度关税 $%.2f，已将您打为遗民。", finalTax)));
                } else {
                    playerData.addBalance(-finalTax);
                    playerData.addTaxPaidThisCycle(finalTax);
                    WorldData.get(player.serverLevel()).addTotalTaxCollected(finalTax);
                    
                    String message = String.format("§e[关税] 检测到维度跨越。环境维护费 -$%.2f 已扣除。", finalTax);
                    if (discount > 0) {
                        message += String.format(" §a(开拓减免: $%.2f)", discount);
                    }
                    player.sendSystemMessage(Component.literal(message));
                }
            });
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                // Slaves are immune to The Fall Event
                if (playerData.getSlaveOwnerUUID() != null) {
                    return;
                }

                if (playerData.getClassTier() == 0) return; // Pariahs are exempt from death tax as they are already at the bottom.

                double balance = playerData.getBalance();
                double armorValue = player.getArmorValue();
                double taxRate = 0.02 + (armorValue / 100.0);
                double taxAmount = balance * taxRate;

                playerData.setBalance(balance - taxAmount);
                playerData.addTaxPaidThisCycle(taxAmount);
                WorldData.get(player.serverLevel()).addTotalTaxCollected(taxAmount);
                player.sendSystemMessage(Component.literal(String.format("§c死亡税: -$%.2f", taxAmount)));

                if (playerData.getBalance() < 200) {
                    playerData.setClassTier(0);
                    playerData.setFallen(true);
                    // The rest of the fall event is handled on respawn
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                if (playerData.isFallen()) {
                    WorldData worldData = WorldData.get(player.serverLevel());
                    BlockPos corePos = worldData.getCorePosition();
                    if (!corePos.equals(BlockPos.ZERO)) {
                        player.setRespawnPosition(player.serverLevel().dimension(), corePos, 0.0f, true, false);
                        player.teleportTo(corePos.getX() + 0.5, corePos.getY() + 0.5, corePos.getZ() + 0.5);
                    }

                    Component message = Component.literal("§6📢 系统通知：玩家 " + player.getGameProfile().getName() + " 因贪婪深入蛮荒而耗尽盘缠，现已堕落成遗民，被遣返回城。");
                    if (player.getServer() != null) {
                        player.getServer().getPlayerList().broadcastSystemMessage(message, false);
                    }

                    playerData.setFallen(false);
                }
            });
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                UnclaimedIncomeData incomeData = WorldData.get(player.serverLevel()).getUnclaimedIncomeData();
                double income = incomeData.getIncome(player.getUUID());
                if (income > 0) {
                    player.sendSystemMessage(Component.literal(String.format("§a[提示] 您有离线期间奴隶上缴的税款: $%.2f，请前往核心领取。", income)));
                }
                
                NetworkHandler.sendToPlayer(player, new PlayerDataSyncPacket(playerData));
            });
        }
    }

    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> event.setDisplayName(createFormattedName(player, playerData)));
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player == null) return;

        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            Component formattedName = createFormattedName(player, playerData);
            Component message = event.getMessage();
            // The default chat format is <PlayerName> message
            // We are replacing the entire component to ensure our format takes precedence.
            Component finalMessage = Component.literal("<").append(formattedName).append("> ").append(message);

            // Cancel the original event and broadcast the new one
            event.setCanceled(true);
            player.getServer().getPlayerList().broadcastSystemMessage(finalMessage, false);
        });
    }

    private static Component createFormattedName(ServerPlayer player, IPlayerData playerData) {
        Component classPrefix = getClassPrefix(player, playerData);
        Component jobSuffix = getJobSuffix(player, playerData);
        String playerName = player.getGameProfile().getName();

        return Component.empty().append(classPrefix).append(" ").append(playerName).append(" ").append(jobSuffix);
    }

    public static Component getClassPrefix(ServerPlayer player, IPlayerData playerData) {
        if (player.hasPermissions(2)) {
            return Component.literal("§c<核心执行官>");
        }

        UUID ownerUUID = playerData.getSlaveOwnerUUID();
        if (ownerUUID != null) {
            if (player.getServer() != null && player.getServer().getProfileCache() != null) {
                String ownerName = player.getServer().getProfileCache().get(ownerUUID)
                        .map(com.mojang.authlib.GameProfile::getName)
                        .orElse("???");
                return Component.literal("§7[" + ownerName + "的奴隶]");
            }
            return Component.literal("§7[???的奴隶]");
        }

        return switch (playerData.getClassTier()) {
            case 0 -> Component.literal("§7<遗民>");
            case 1 -> Component.literal("§e<限制公民>");
            case 2 -> Component.literal("§b<自由公民>");
            default -> Component.empty();
        };
    }

    public static Component getJobSuffix(ServerPlayer player, IPlayerData playerData) {
        if (player.hasPermissions(2)) {
            return Component.literal("<最高权限>");
        }

        Profession profession = playerData.getProfession();
        int level = playerData.getProfessionLevel(profession);

        if (playerData.isWelfareRecipient()) {
            return Component.literal("<低保户>");
        }

        if (profession == Profession.UNEMPLOYED) {
            return Component.literal("<无业游民>");
        }

        String title = getProfessionTitle(profession, level);
        return Component.literal("<" + title + ">");
    }

    private static String getProfessionTitle(Profession profession, int level) {
        return switch (profession) {
            case MINER -> {
                if (level <= 10) yield "实习矿工";
                if (level <= 30) yield "正式矿工";
                if (level <= 45) yield "资深工头";
                yield "资源大亨";
            }
            case FARMER -> {
                if (level <= 10) yield "佃农";
                if (level <= 30) yield "庄园主";
                if (level <= 45) yield "大地主";
                yield "生命编织者";
            }
            case CHEF -> {
                if (level <= 10) yield "帮厨";
                if (level <= 30) yield "主厨";
                if (level <= 45) yield "膳食官";
                yield "味觉大师";
            }
            case ANGLER -> {
                if (level <= 10) yield "浮沫滤网";
                if (level <= 30) yield "河床清道夫";
                if (level <= 45) yield "沉船打捞员";
                yield "深渊主宰";
            }
            case ADVENTURER -> {
                if (level <= 10) yield "清道夫";
                if (level <= 30) yield "废土游侠";
                if (level <= 45) yield "深潜探员";
                yield "位面先锋";
            }
            case BLACKSMITH -> {
                if (level <= 10) yield "敲打者";
                if (level <= 30) yield "熔炉工头";
                if (level <= 45) yield "战争机器";
                yield "火神";
            }
            default -> {
                yield profession.getDisplayName().getString();
            }
        };
    }
}
