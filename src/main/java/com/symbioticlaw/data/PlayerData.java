package com.symbioticlaw.data;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;
import net.minecraft.core.BlockPos;

public class PlayerData {

    private UUID playerUUID;

    // Economy
    private double balance = 500.0;
    private double taxDebt = 0.0;
    private long welfareCooldown = 0;
    private int onlineTickCount = 0;

    // Identity
    private int classTier = 1; // 0: Pariah, 1: Restricted, 2: Free
    private boolean isFallen = false;
    private JobType jobType = JobType.UNEMPLOYED;
    private UUID slaveOwnerUUID;

    // Affinity
    private UUID affinityPartnerUUID;
    private int affinityLevel = 0;
    private long affinityLastScan = 0;
    private long bondExpireTime = 0;

    // Border
    private BlockPos homePosition = BlockPos.ZERO;
    private int movementRadius = 200;
    private long borderViolationTimer = 0;

    // Visa
    private long visaExpireTime = 0;
    private long survivalPassExpireTime = 0;
    private long survivalPassCooldown = 0;

    // Exile
    private boolean isExile = false;
    private double exileDebt = 0.0;

    // Professions
    private int minerLevel = 0;
    private double minerXp = 0.0;
    private int farmerLevel = 0;
    private double farmerXp = 0.0;
    private long totalBiomassSold = 0;
    private int chefLevel = 0;
    private double chefXp = 0.0;
    private long totalDishesServed = 0;
    private int anglerLevel = 0;
    private double anglerXp = 0.0;
    private int adventurerLevel = 0;
    private double adventurerXp = 0.0;
    private int smithLevel = 0;
    private double smithXp = 0.0;

    // Daily Quota
    private int dailyQuotaProgress = 0;
    private String dailyQuotaItem = "";
    private int dailyQuotaRequiredAmount = 0;
    private double dailyQuotaReward = 0.0;


    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (playerUUID != null) {
            tag.putUUID("playerUUID", playerUUID);
        }
        tag.putDouble("balance", balance);
        tag.putDouble("taxDebt", taxDebt);
        tag.putLong("welfareCooldown", welfareCooldown);
        tag.putInt("onlineTickCount", onlineTickCount);
        tag.putInt("classTier", classTier);
        tag.putBoolean("isFallen", isFallen);
        tag.putString("jobType", jobType.getId());
        if (slaveOwnerUUID != null) {
            tag.putUUID("slaveOwnerUUID", slaveOwnerUUID);
        }
        if (affinityPartnerUUID != null) {
            tag.putUUID("affinityPartnerUUID", affinityPartnerUUID);
        }
        tag.putInt("affinityLevel", affinityLevel);
        tag.putLong("affinityLastScan", affinityLastScan);
        tag.putLong("bondExpireTime", bondExpireTime);
        if (homePosition != null) {
            tag.putLong("homePosition", homePosition.asLong());
        }
        tag.putInt("movementRadius", movementRadius);
        tag.putLong("borderViolationTimer", borderViolationTimer);
        tag.putLong("visaExpireTime", visaExpireTime);
        tag.putLong("survivalPassExpireTime", survivalPassExpireTime);
        tag.putLong("survivalPassCooldown", survivalPassCooldown);
        tag.putBoolean("isExile", isExile);
        tag.putDouble("exileDebt", exileDebt);
        tag.putInt("minerLevel", minerLevel);
        tag.putDouble("minerXp", minerXp);
        tag.putInt("farmerLevel", farmerLevel);
        tag.putDouble("farmerXp", farmerXp);
        tag.putLong("totalBiomassSold", totalBiomassSold);
        tag.putInt("chefLevel", chefLevel);
        tag.putDouble("chefXp", chefXp);
        tag.putLong("totalDishesServed", totalDishesServed);
        tag.putInt("anglerLevel", anglerLevel);
        tag.putDouble("anglerXp", anglerXp);
        tag.putInt("adventurerLevel", adventurerLevel);
        tag.putDouble("adventurerXp", adventurerXp);
        tag.putInt("smithLevel", smithLevel);
        tag.putDouble("smithXp", smithXp);
        tag.putInt("dailyQuotaProgress", dailyQuotaProgress);
        tag.putString("dailyQuotaItem", dailyQuotaItem);
        tag.putInt("dailyQuotaRequiredAmount", dailyQuotaRequiredAmount);
        tag.putDouble("dailyQuotaReward", dailyQuotaReward);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.hasUUID("playerUUID")) {
            playerUUID = tag.getUUID("playerUUID");
        }
        balance = tag.getDouble("balance");
        taxDebt = tag.getDouble("taxDebt");
        welfareCooldown = tag.getLong("welfareCooldown");
        onlineTickCount = tag.getInt("onlineTickCount");
        classTier = tag.getInt("classTier");
        isFallen = tag.getBoolean("isFallen");
        jobType = JobType.fromId(tag.getString("jobType"));
        if (tag.hasUUID("slaveOwnerUUID")) {
            slaveOwnerUUID = tag.getUUID("slaveOwnerUUID");
        }
        if (tag.hasUUID("affinityPartnerUUID")) {
            affinityPartnerUUID = tag.getUUID("affinityPartnerUUID");
        }
        affinityLevel = tag.getInt("affinityLevel");
        affinityLastScan = tag.getLong("affinityLastScan");
        bondExpireTime = tag.getLong("bondExpireTime");
        if (tag.contains("homePosition")) {
            homePosition = BlockPos.of(tag.getLong("homePosition"));
        }
        movementRadius = tag.getInt("movementRadius");
        borderViolationTimer = tag.getLong("borderViolationTimer");
        visaExpireTime = tag.getLong("visaExpireTime");
        survivalPassExpireTime = tag.getLong("survivalPassExpireTime");
        survivalPassCooldown = tag.getLong("survivalPassCooldown");
        isExile = tag.getBoolean("isExile");
        exileDebt = tag.getDouble("exileDebt");
        minerLevel = tag.getInt("minerLevel");
        minerXp = tag.getDouble("minerXp");
        farmerLevel = tag.getInt("farmerLevel");
        farmerXp = tag.getDouble("farmerXp");
        totalBiomassSold = tag.getLong("totalBiomassSold");
        chefLevel = tag.getInt("chefLevel");
        chefXp = tag.getDouble("chefXp");
        totalDishesServed = tag.getLong("totalDishesServed");
        anglerLevel = tag.getInt("anglerLevel");
        anglerXp = tag.getDouble("anglerXp");
        adventurerLevel = tag.getInt("adventurerLevel");
        adventurerXp = tag.getDouble("adventurerXp");
        smithLevel = tag.getInt("smithLevel");
        smithXp = tag.getDouble("smithXp");
        dailyQuotaProgress = tag.getInt("dailyQuotaProgress");
        dailyQuotaItem = tag.getString("dailyQuotaItem");
        dailyQuotaRequiredAmount = tag.getInt("dailyQuotaRequiredAmount");
        dailyQuotaReward = tag.getDouble("dailyQuotaReward");
    }

    // Getters and Setters

    public UUID getPlayerUUID() {
        return playerUUID;
    }


    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getTaxDebt() {
        return taxDebt;
    }

    public void setTaxDebt(double taxDebt) {
        this.taxDebt = taxDebt;
    }

    public long getWelfareCooldown() {
        return welfareCooldown;
    }

    public void setWelfareCooldown(long welfareCooldown) {
        this.welfareCooldown = welfareCooldown;
    }

    public int getOnlineTickCount() {
        return onlineTickCount;
    }

    public void setOnlineTickCount(int onlineTickCount) {
        this.onlineTickCount = onlineTickCount;
    }

    public int getClassTier() {
        return classTier;
    }

    public void setClassTier(int classTier) {
        this.classTier = classTier;
    }

    public boolean isFallen() {
        return isFallen;
    }

    public void setFallen(boolean fallen) {
        isFallen = fallen;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public UUID getSlaveOwnerUUID() {
        return slaveOwnerUUID;
    }

    public void setSlaveOwnerUUID(UUID slaveOwnerUUID) {
        this.slaveOwnerUUID = slaveOwnerUUID;
    }

    public UUID getAffinityPartnerUUID() {
        return affinityPartnerUUID;
    }

    public void setAffinityPartnerUUID(UUID affinityPartnerUUID) {
        this.affinityPartnerUUID = affinityPartnerUUID;
    }

    public int getAffinityLevel() {
        return affinityLevel;
    }

    public void setAffinityLevel(int affinityLevel) {
        this.affinityLevel = affinityLevel;
    }

    public long getAffinityLastScan() {
        return affinityLastScan;
    }

    public void setAffinityLastScan(long affinityLastScan) {
        this.affinityLastScan = affinityLastScan;
    }

    public long getBondExpireTime() {
        return bondExpireTime;
    }

    public void setBondExpireTime(long bondExpireTime) {
        this.bondExpireTime = bondExpireTime;
    }

    public BlockPos getHomePosition() {
        return homePosition;
    }

    public void setHomePosition(BlockPos homePosition) {
        this.homePosition = homePosition;
    }

    public int getMovementRadius() {
        return movementRadius;
    }

    public void setMovementRadius(int movementRadius) {
        this.movementRadius = movementRadius;
    }

    public long getBorderViolationTimer() {
        return borderViolationTimer;
    }

    public void setBorderViolationTimer(long borderViolationTimer) {
        this.borderViolationTimer = borderViolationTimer;
    }

    public long getVisaExpireTime() {
        return visaExpireTime;
    }

    public void setVisaExpireTime(long visaExpireTime) {
        this.visaExpireTime = visaExpireTime;
    }

    public long getSurvivalPassExpireTime() {
        return survivalPassExpireTime;
    }

    public void setSurvivalPassExpireTime(long survivalPassExpireTime) {
        this.survivalPassExpireTime = survivalPassExpireTime;
    }

    public long getSurvivalPassCooldown() {
        return survivalPassCooldown;
    }

    public void setSurvivalPassCooldown(long survivalPassCooldown) {
        this.survivalPassCooldown = survivalPassCooldown;
    }

    public boolean isExile() {
        return isExile;
    }
    public void setExile(boolean exile) {
        isExile = exile;
    }

    public double getExileDebt() {
        return exileDebt;
    }

    public void setExileDebt(double exileDebt) {
        this.exileDebt = exileDebt;
    }

    public int getMinerLevel() {
        return minerLevel;
    }

    public void setMinerLevel(int minerLevel) {
        this.minerLevel = minerLevel;
    }

    public double getMinerXp() {
        return minerXp;
    }

    public void setMinerXp(double minerXp) {
        this.minerXp = minerXp;
    }

    public int getFarmerLevel() {
        return farmerLevel;
    }

    public void setFarmerLevel(int farmerLevel) {
        this.farmerLevel = farmerLevel;
    }

    public double getFarmerXp() {
        return farmerXp;
    }

    public void setFarmerXp(double farmerXp) {
        this.farmerXp = farmerXp;
    }

    public long getTotalBiomassSold() {
        return totalBiomassSold;
    }

    public void setTotalBiomassSold(long totalBiomassSold) {
        this.totalBiomassSold = totalBiomassSold;
    }

    public int getChefLevel() {
        return chefLevel;
    }

    public void setChefLevel(int chefLevel) {
        this.chefLevel = chefLevel;
    }

    public double getChefXp() {
        return chefXp;
    }

    public void setChefXp(double chefXp) {
        this.chefXp = chefXp;
    }

    public long getTotalDishesServed() {
        return totalDishesServed;
    }

    public void setTotalDishesServed(long totalDishesServed) {
        this.totalDishesServed = totalDishesServed;
    }

    public int getAnglerLevel() {
        return anglerLevel;
    }

    public void setAnglerLevel(int anglerLevel) {
        this.anglerLevel = anglerLevel;
    }

    public double getAnglerXp() {
        return anglerXp;
    }

    public void setAnglerXp(double anglerXp) {
        this.anglerXp = anglerXp;
    }

    public int getAdventurerLevel() {
        return adventurerLevel;
    }

    public void setAdventurerLevel(int adventurerLevel) {
        this.adventurerLevel = adventurerLevel;
    }

    public double getAdventurerXp() {
        return adventurerXp;
    }

    public void setAdventurerXp(double adventurerXp) {
        this.adventurerXp = adventurerXp;
    }

    public int getSmithLevel() {
        return smithLevel;
    }

    public void setSmithLevel(int smithLevel) {
        this.smithLevel = smithLevel;
    }

    public double getSmithXp() {
        return smithXp;
    }

    public void setSmithXp(double smithXp) {
        this.smithXp = smithXp;
    }

    public int getDailyQuotaProgress() {
        return dailyQuotaProgress;
    }

    public void setDailyQuotaProgress(int dailyQuotaProgress) {
        this.dailyQuotaProgress = dailyQuotaProgress;
    }

    public String getDailyQuotaItem() {
        return dailyQuotaItem;
    }

    public void setDailyQuotaItem(String dailyQuotaItem) {
        this.dailyQuotaItem = dailyQuotaItem;
    }

    public int getDailyQuotaRequiredAmount() {
        return dailyQuotaRequiredAmount;
    }

    public void setDailyQuotaRequiredAmount(int dailyQuotaRequiredAmount) {
        this.dailyQuotaRequiredAmount = dailyQuotaRequiredAmount;
    }

    public double getDailyQuotaReward() {
        return dailyQuotaReward;
    }

    public void setDailyQuotaReward(double dailyQuotaReward) {
        this.dailyQuotaReward = dailyQuotaReward;
    }

    public void copyFrom(PlayerData source) {
        this.playerUUID = source.playerUUID;
        this.balance = source.balance;
        this.taxDebt = source.taxDebt;
        this.welfareCooldown = source.welfareCooldown;
        this.onlineTickCount = source.onlineTickCount;
        this.classTier = source.classTier;
        this.isFallen = source.isFallen;
        this.jobType = source.jobType;
        this.slaveOwnerUUID = source.slaveOwnerUUID;
        this.affinityPartnerUUID = source.affinityPartnerUUID;
        this.affinityLevel = source.affinityLevel;
        this.affinityLastScan = source.affinityLastScan;
        this.bondExpireTime = source.bondExpireTime;
        this.homePosition = source.homePosition;
        this.movementRadius = source.movementRadius;
        this.borderViolationTimer = source.borderViolationTimer;
        this.visaExpireTime = source.visaExpireTime;
        this.survivalPassExpireTime = source.survivalPassExpireTime;
        this.survivalPassCooldown = source.survivalPassCooldown;
        this.isExile = source.isExile;
        this.exileDebt = source.exileDebt;
        this.minerLevel = source.minerLevel;
        this.minerXp = source.minerXp;
        this.farmerLevel = source.farmerLevel;
        this.farmerXp = source.farmerXp;
        this.totalBiomassSold = source.totalBiomassSold;
        this.chefLevel = source.chefLevel;
        this.chefXp = source.chefXp;
        this.totalDishesServed = source.totalDishesServed;
        this.anglerLevel = source.anglerLevel;
        this.anglerXp = source.anglerXp;
        this.adventurerLevel = source.adventurerLevel;
        this.adventurerXp = source.adventurerXp;
        this.smithLevel = source.smithLevel;
        this.smithXp = source.smithXp;
        this.dailyQuotaProgress = source.dailyQuotaProgress;
        this.dailyQuotaItem = source.dailyQuotaItem;
        this.dailyQuotaRequiredAmount = source.dailyQuotaRequiredAmount;
        this.dailyQuotaReward = source.dailyQuotaReward;
    }
}