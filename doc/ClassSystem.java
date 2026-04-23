package SY.symbioticlaw.system;

import SY.symbioticlaw.Config;
import SY.symbioticlaw.capability.PlayerDataCapability;
import SY.symbioticlaw.system.ProfessionSystem;
import SY.symbioticlaw.network.NetworkHandler;
import SY.symbioticlaw.network.PlayerDataSyncPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class ClassSystem {

    public static final double PARIAH_THRESHOLD = 200.0;
    public static final double FREE_CITIZEN_THRESHOLD = 1000.0;

    /**
     * Updates the player's class based on their balance.
     * This should be called whenever a player's balance changes.
     * @param player The player to update.
     */
    public static void updatePlayerClass(ServerPlayer player) {
        player.getCapability(PlayerDataCapability.PLAYER_DATA).ifPresent(playerData -> {
            int oldClass = playerData.getClassTier();
            // If player is a pariah and hasn't paid the fee, their class is locked.
            if (oldClass == 0 && !playerData.canEscapePariah()) {
                return;
            }

            double balance = playerData.getBalance();
            int newClass;

            if (balance < PARIAH_THRESHOLD) {
                newClass = 0; // Pariah
            } else if (balance <= FREE_CITIZEN_THRESHOLD) {
                newClass = 1; // Restricted
            } else {
                newClass = 2; // Free
            }

            if (oldClass != newClass) {
                playerData.setClassTier(newClass);

                // The "Fall Event"
                if (oldClass > 0 && newClass == 0) {
                    handleFallEvent(player);
                } else {
                    if (oldClass == 0 && newClass == 1) {
                        player.sendSystemMessage(Component.literal("§a[系统] 恭喜！您的信用评级已恢复，现在是二等公民。"));
                    } else if (oldClass == 1 && newClass == 2) {
                        player.sendSystemMessage(Component.literal("§a[系统] 恭喜！您已成为一等公民，享有所有权利。"));
                    }
                }

                NetworkHandler.sendToPlayer(player, new PlayerDataSyncPacket(playerData));
            }

            // Reset the flag after promotion out of Pariah status
            if (oldClass == 0 && newClass > 0 && playerData.canEscapePariah()) {
                playerData.setCanEscapePariah(false);
            }
        });
    }

    /**
     * Handles the "Fall Event" when a player becomes a Pariah.
     * @param player The player who has fallen.
     */
    private static void handleFallEvent(ServerPlayer player) {
        Vec3 corePosition = new Vec3(Config.CORE_X.get(), Config.CORE_Y.get(), Config.CORE_Z.get());
        player.teleportTo(player.serverLevel(), corePosition.x, corePosition.y, corePosition.z, player.getYRot(), player.getXRot());

        if (player.getServer() != null) {
            player.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§6📢 系统通知：玩家 " + player.getGameProfile().getName() + " 因贪婪深入蛮荒而耗尽盘缠，现已堕落成遗民，被遣返回城。"),
                false
            );
        
        // Strip the player of their profession upon falling.
        ProfessionSystem.resetProfession(player);
        }
    }

    /**
     * Handles player death, applying death tax and checking for class changes.
     * To be called from LivingDeathEvent handler.
     * @param event The LivingDeathEvent.
     */
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        player.getCapability(PlayerDataCapability.PLAYER_DATA).ifPresent(playerData -> {
            double balance = playerData.getBalance();
            // Death Tax: Total Assets * (2% + Armor Value / 100)
            double armorValue = player.getArmorValue();
            double taxRate = 0.02 + (armorValue / 100.0);
            double taxAmount = balance * taxRate;

            // EconomySystem should handle the balance change and then trigger updatePlayerClass
            EconomySystem.subtractBalance(player, taxAmount);
        });
    }

    /**
     * Allows a Pariah to pay to restore their credit and become a citizen again.
     * This is called from the Power Core GUI.
     * @param player The player paying the fee.
     * @return True if successful.
     */
    public static boolean payCreditRecoveryFee(ServerPlayer player) {
        return player.getCapability(PlayerDataCapability.PLAYER_DATA).map(playerData -> {
            final double FEE = 500.0;
            final double MIN_BALANCE_TO_PAY = 700.0; // $500 fee + $200 minimum for Restricted status

            if (playerData.getClassTier() != 0) {
                player.sendSystemMessage(Component.literal("§c只有遗民需要恢复信用。"));
                return false;
            }

            if (playerData.getBalance() < MIN_BALANCE_TO_PAY) {
                player.sendSystemMessage(Component.literal("§c您的余额不足 " + MIN_BALANCE_TO_PAY + "，无法支付信用恢复费。"));
                return false;
            }

            if (EconomySystem.subtractBalance(player, FEE)) {
                // Also remove [低保户] (Welfare Recipient) tag if present.
                if (playerData.isWelfareRecipient()) {
                    playerData.setWelfareRecipient(false);
                }

                playerData.setCanEscapePariah(true); // Allow promotion on the next class check
                updatePlayerClass(player); // Re-evaluate class immediately

                player.sendSystemMessage(Component.literal("§e[系统] 赎身费用 $500.0 已扣除。档案重置中... §a[✔] [低保户] / [遗民] 标签已移除。请保持努力工作。"));
                return true;
            }
            return false;
        }).orElse(false);
    }
}