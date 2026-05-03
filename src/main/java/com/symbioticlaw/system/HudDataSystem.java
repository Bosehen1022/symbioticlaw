package com.symbioticlaw.system;

import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.JobType;
import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.data.WorldData;
import com.symbioticlaw.network.ClientBoundAlmanacUpdatePacket;
import com.symbioticlaw.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

public class HudDataSystem {

    public void tick(ServerPlayer player) {
        if (player.level().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) && player.server.getTickCount() % 20 != 0) {
            return; // Only tick once per second
        }

        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            WorldData worldData = WorldData.get(player.serverLevel());
            if (worldData == null) return;
            BlockPos corePos = worldData.getCorePosition();
            if (corePos.equals(BlockPos.ZERO)) return;

            double balance = playerData.getBalance();
            double dx = player.getX() - (corePos.getX() + 0.5);
            double dz = player.getZ() - (corePos.getZ() + 0.5);

            // Calculate M_tax
            double mTax = calculateMTax(playerData);

            // Calculate Return Fee
            double returnFee = calculateReturnFee(dx, dz, mTax);

            // Calculate Safety Radius
            double safetyRadius = calculateSafetyRadius(balance, mTax);

            // Determine HUD Status
            int status = determineHudStatus(balance, returnFee);

            // Send packet to client
            NetworkHandler.sendToPlayer(player, new ClientBoundAlmanacUpdatePacket(returnFee, safetyRadius, status, corePos.getX(), corePos.getY(), corePos.getZ()));
        });
    }

    private double calculateMTax(com.symbioticlaw.capability.IPlayerData playerData) {
        double jobDiscount = 0.0;
        if (playerData.getProfession().toJobType() == JobType.ADVENTURER) {
            int level = playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.ADVENTURER);
            if (level >= 46) {
                jobDiscount = 0.3;
            } else if (level >= 31) {
                jobDiscount = 0.2;
            } else if (level >= 11) {
                jobDiscount = 0.1;
            }
        }

        double affinityDiscount = 0.0;
        if (playerData.getAffinityPartnerUUID() != null) {
            affinityDiscount = 0.2; // 20% discount for being in a bond
        }

        return 1.0 - jobDiscount - affinityDiscount;
    }

    private double calculateReturnFee(double dx, double dz, double mTax) {
        double distance = Math.sqrt(dx * dx + dz * dz);
        double rate = getRateForDistance(distance);
        return (20 + (distance * rate)) * mTax;
    }

    private double calculateSafetyRadius(double balance, double mTax) {
        double netBalance = balance - (20 * mTax);
        if (netBalance <= 0) {
            return 0;
        }

        double t1 = 220 * mTax;
        double t2 = 1520 * mTax;
        double t3 = 4020 * mTax;

        if (balance >= 20 * mTax && balance < t1) {
            return netBalance / (0.1 * mTax);
        } else if (balance >= t1 && balance < t2) {
            return 2000;
        } else if (balance >= t2 && balance < t3) {
            return netBalance / (0.3 * mTax);
        } else if (balance >= t3 && balance < 4020 * mTax) {
            return 5000;
        } else {
            return netBalance / (0.8 * mTax);
        }
    }

    private int determineHudStatus(double balance, double returnFee) {
        if (balance < returnFee) {
            return 2; // Danger
        } else if (balance < returnFee * 1.5) {
            return 1; // Warning
        } else {
            return 0; // Safe
        }
    }

    private double getRateForDistance(double distance) {
        if (distance <= 2000) {
            return 0.1;
        } else if (distance <= 5000) {
            return 0.3;
        } else {
            return 0.8;
        }
    }
}
