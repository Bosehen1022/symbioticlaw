package com.symbioticlaw.capability;

import com.symbioticlaw.professions.Profession;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public interface IPlayerData {
    void saveNBTData(CompoundTag compound);
    void loadNBTData(CompoundTag compound);
    double getBalance();
    void setBalance(double balance);
    void addBalance(double amount);
    
    double getTaxDebt();
    void setTaxDebt(double taxDebt);
    
    int getClassTier();
    void setClassTier(int classTier);
    
    Profession getProfession();
    void setProfession(Profession profession);
    
    UUID getSlaveOwnerUUID();
    void setSlaveOwnerUUID(UUID ownerUUID);
    
    UUID getAffinityPartnerUUID();
    void setAffinityPartnerUUID(UUID partnerUUID);
    String getAffinityPartnerName();
    void setAffinityPartnerName(String name);
    
    int getAffinityLevel();
    void setAffinityLevel(int level);
    
    BlockPos getHomePosition();
    void setHomePosition(BlockPos pos);
    
    int getMovementRadius();
    void setMovementRadius(int radius);
    
    long getVisaExpireTime();
    void setVisaExpireTime(long time);
    
    boolean isExile();
    void setExile(boolean exile);
    
    double getExileDebt();
    void setExileDebt(double exileDebt);

    boolean isFallen();
    void setFallen(boolean fallen);

    int getProfessionLevel(Profession profession);
    void setProfessionLevel(Profession profession, int level);
    double getProfessionXp(Profession profession);
    void setProfessionXp(Profession profession, double xp);
    void addProfessionXp(Profession profession, double xp);

    long getTotalItemsSold(Profession profession);
    void setTotalItemsSold(Profession profession, long amount);
    void addTotalItemsSold(Profession profession, long amount);

    long getSurvivalPassExpireTime();
    void setSurvivalPassExpireTime(long time);

    long getSurvivalPassCooldown();
    void setSurvivalPassCooldown(long time);

    long getWelfareCooldown();
    void setWelfareCooldown(long time);

    boolean isWelfareRecipient();
    void setWelfareRecipient(boolean value);

    long getBondExpireTime();
    void setBondExpireTime(long time);

    int getOnlineTickCount();
    void setOnlineTickCount(int ticks);

    double getTaxPaidThisCycle();
    void setTaxPaidThisCycle(double amount);
    void addTaxPaidThisCycle(double amount);

    int getWelfareClaimsThisCycle();
    void setWelfareClaimsThisCycle(int count);
    void addWelfareClaimsThisCycle(int count);

    long getLastProfessionChangeTime();
    void setLastProfessionChangeTime(long time);

    UUID getActiveContractId();
    void setActiveContractId(UUID contractId);

    boolean hasSlaves();
    void setHasSlaves(boolean hasSlaves);

    long getLastSubliminalMessageTick();
    void setLastSubliminalMessageTick(long tick);

    int getIdleTicks();
    void setIdleTicks(int ticks);

    // Daily Quota System
    String getDailyQuotaItem();
    void setDailyQuotaItem(String itemId);
    
    int getDailyQuotaRequiredAmount();
    void setDailyQuotaRequiredAmount(int amount);
    
    int getDailyQuotaProgress();
    void setDailyQuotaProgress(int progress);
    
    double getDailyQuotaReward();
    void setDailyQuotaReward(double reward);
    
    long getDailyQuotaCompletionTime();
    void setDailyQuotaCompletionTime(long time);
    
    boolean isDailyQuotaCompleted();
    
    // ===== Chapter 5: Career Binding & Slavery System =====
    
    // 奴隶列表管理
    java.util.List<UUID> getSlaves();
    void setSlaves(java.util.List<UUID> slaves);
    void addSlave(UUID slaveUUID);
    void removeSlave(UUID slaveUUID);
    
    // 奴隶主UUID
    UUID getMasterUUID();
    void setMasterUUID(UUID masterUUID);
    
    // 待提取税金
    double getPendingTaxForMaster(UUID masterUUID);
    void setPendingTaxForMaster(UUID masterUUID, double amount);
    double getTotalExtractedTax(UUID masterUUID);
    void setTotalExtractedTax(UUID masterUUID, double amount);
    double claimPendingTax(UUID masterUUID);
    
    // 低保时间记录
    long getLastWelfareTime();
    void setLastWelfareTime(long time);
    
    // 生存假票时间
    long getLastSurvivalPassTime();
    void setLastSurvivalPassTime(long time);
    long getSurvivalPassExpiry();
    void setSurvivalPassExpiry(long time);
    
    // 职业使用整数ID
    int getProfessionId();
    void setProfessionId(int professionId);
    int getProfessionLevel(int professionId);
    void setProfessionLevel(int professionId, int level);
    double getProfessionXp(int professionId);
    void setProfessionXp(int professionId, double xp);

    void copyFrom(IPlayerData source);
}
