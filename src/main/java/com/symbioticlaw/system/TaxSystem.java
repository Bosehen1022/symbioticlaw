package com.symbioticlaw.system;

import com.symbioticlaw.Config;
import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.professions.Profession;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import com.symbioticlaw.data.WorldData;

public class TaxSystem {

    public double getMtax(IPlayerData playerData) {
        double professionDiscount = 0;
        if (playerData.getProfession() == Profession.ADVENTURER) {
            int level = playerData.getProfessionLevel(Profession.ADVENTURER);
            if (level >= 46) {
                professionDiscount = 0.30;
            } else if (level >= 31) {
                professionDiscount = 0.20;
            } else if (level >= 11) {
                professionDiscount = 0.10;
            }
        }

        double bondDiscount = playerData.getAffinityPartnerUUID() != null ? 0.20 : 0;
        return 1.0 - professionDiscount - bondDiscount;
    }

    public double calculatePeriodicTax(IPlayerData playerData) {
        return switch (playerData.getClassTier()) {
            case 0 -> 5.0;  // Pariah
            case 1 -> 10.0; // Restricted Citizen
            case 2 -> 18.0; // Free Citizen
            default -> 18.0; // Default to highest tax as a safeguard
        };
    }

    public double calculateDimensionTax(ResourceKey<Level> toDim) {
        return calculateDimensionTax(toDim, null);
    }
    
    /**
     * Calculate dimension tax with player-specific discounts
     * @param toDim Target dimension
     * @param playerData Player data for discount calculation (can be null)
     * @return Final tax amount after discounts
     */
    public double calculateDimensionTax(ResourceKey<Level> toDim, IPlayerData playerData) {
        double baseTax;
        String dimNamespace = toDim.location().getNamespace();
        String dimPath = toDim.location().getPath();
        
        if (toDim.equals(Level.NETHER)) {
            baseTax = Config.DIMENSION_TAX_NETHER.get();
        } else if (toDim.equals(Level.END)) {
            baseTax = Config.DIMENSION_TAX_END.get();
        } else if (dimPath.contains("twilightforest") || dimPath.contains("twilight_forest")) {
            // Twilight Forest - Tier 2 (受控异常区)
            baseTax = Config.DIMENSION_TAX_TWILIGHT_FOREST.get();
        } else if (dimPath.contains("blue_skies") || dimPath.contains("everbright") || dimPath.contains("everdawn")) {
            // Blue Skies - Tier 2 (生态变异区)
            baseTax = Config.DIMENSION_TAX_BLUE_SKIES.get();
        } else if (dimPath.contains("undergarden")) {
            // Undergarden - Tier 3 (重度污染区)
            baseTax = Config.DIMENSION_TAX_UNDERGARDEN.get();
        } else if (dimPath.contains("deeperdarker") || dimPath.contains("otherside")) {
            // Deeper & Darker - Tier 4 (深渊湮灭区)
            baseTax = Config.DIMENSION_TAX_DEEPER_DARKER.get();
        } else if (!toDim.equals(Level.OVERWORLD)) {
            // Any other dimension that is not the Overworld is considered a modded dimension
            baseTax = Config.DIMENSION_TAX_MODDED.get();
        } else {
            return 0; // No tax for entering the Overworld
        }
        
        // Apply adventurer tax relief
        if (playerData != null) {
            double relief = getMtax(playerData); // Returns discount multiplier (0.7 for 30% relief)
            baseTax *= relief;
        }
        
        return baseTax;
    }

    public double calculateWaystoneCost(ServerPlayer player, BlockPos from, BlockPos to, ResourceKey<Level> fromDim, ResourceKey<Level> toDim) {
        IPlayerData playerData = player.getCapability(PlayerDataCapability.INSTANCE).orElse(null);
        if (playerData == null) return Double.MAX_VALUE;

        boolean isOverworldFrom = fromDim.equals(Level.OVERWORLD);
        boolean isOverworldTo = toDim.equals(Level.OVERWORLD);

        double baseCost;
        if (isOverworldFrom && isOverworldTo) {
            // Overworld to Overworld
            baseCost = calculateOverworldMileageTax(from, to);
        } else if (!isOverworldFrom && isOverworldTo) {
            // Other dimension to Overworld
            baseCost = calculateCrossDimensionTax(fromDim, to);
        } else if (isOverworldFrom && !isOverworldTo) {
            // Overworld to other dimension
            // 规格 §4.5: TotalCost = 维度进入税 + 目的地里程费
            // 目的地里程费按目的维度的费率计算
            double dimensionTax = calculateDimensionTax(toDim);
            double mileageInDest = calculateDimensionalMileageTax(to, BlockPos.ZERO, toDim);
            baseCost = dimensionTax + mileageInDest;
        } else {
            // Within the same non-Overworld dimension or between two non-Overworld dimensions
            baseCost = calculateDimensionalMileageTax(from, to, fromDim);
        }
        
        // Apply adventurer tax relief
        return baseCost * getMtax(playerData);
    }

    public double calculateMileageTax(ServerPlayer player, BlockPos from, BlockPos to) {
        return calculateOverworldMileageTax(from, to);
    }

    private double calculateOverworldMileageTax(BlockPos from, BlockPos to) {
        double distance = Math.sqrt(from.distSqr(to));
        double rate = getRateForLocation(from, to);
        return 20 + (distance * rate);
    }

    private double calculateDimensionalMileageTax(BlockPos from, BlockPos to, ResourceKey<Level> dim) {
        double distance = Math.sqrt(from.distSqr(to));
        double rate;
        if (dim.equals(Level.NETHER)) {
            rate = Config.MILEAGE_RATE_NETHER.get();
        } else if (dim.equals(Level.END)) {
            rate = Config.MILEAGE_RATE_END.get();
        } else {
            rate = Config.MILEAGE_RATE_MODDED.get();
        }
        return 20 + (distance * rate);
    }

    private double calculateCrossDimensionTax(ResourceKey<Level> fromDim, BlockPos to) {
        double baseFee;
        if (fromDim.equals(Level.NETHER)) {
            baseFee = Config.CROSS_DIM_BASE_FEE_NETHER.get();
        } else if (fromDim.equals(Level.END)) {
            baseFee = Config.CROSS_DIM_BASE_FEE_END.get();
        } else {
            baseFee = Config.CROSS_DIM_BASE_FEE_MODDED.get();
        }

        double destinationPrice = getDestinationPrice(to);
        return baseFee + destinationPrice;
    }

    private double getRateForLocation(BlockPos... locations) {
        double maxRate = 0.0;
        for (BlockPos loc : locations) {
            double distFromCore = Math.sqrt(loc.distSqr(BlockPos.ZERO));
            if (distFromCore > 5000) {
                maxRate = Math.max(maxRate, Config.MILEAGE_RATE_TIER3.get());
            } else if (distFromCore > 2000) {
                maxRate = Math.max(maxRate, Config.MILEAGE_RATE_TIER2.get());
            } else {
                maxRate = Math.max(maxRate, Config.MILEAGE_RATE_TIER1.get());
            }
        }
        return maxRate;
    }

    public void applyPeriodicTax(ServerPlayer player, IPlayerData playerData) {
        double tax = calculatePeriodicTax(playerData);
        if (playerData.getBalance() >= tax) {
            playerData.setBalance(playerData.getBalance() - tax);
            playerData.addTaxPaidThisCycle(tax);
            WorldData.get(player.getServer().overworld()).addTotalTaxCollected(tax);
            player.sendSystemMessage(Component.literal("§7[税务] 周期税扣除 -$" + tax + " (在线时长 20min)。 §7当前余额: $" + playerData.getBalance()));
        } else {
            player.sendSystemMessage(Component.literal("§c[!] 余额不足！身份降级为 [遗民]。请立即前往核心报到。"));
            playerData.setClassTier(0);
        }
    }

    private double getDestinationPrice(BlockPos destination) {
        double distFromCore = Math.sqrt(destination.distSqr(BlockPos.ZERO));
        if (distFromCore <= 30) {
            return 0;
        } else if (distFromCore <= 2000) {
            return 200;
        } else if (distFromCore <= 5000) {
            return 800;
        } else {
            return 1500;
        }
    }
}
