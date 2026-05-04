package com.symbioticlaw.capability;

import com.symbioticlaw.professions.Profession;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData implements IPlayerData, INBTSerializable<CompoundTag> {
    private double balance = 500.0;
    private double taxDebt = 0.0;
    private int classTier = 1;
    private Profession profession = Profession.UNEMPLOYED;
    private UUID slaveOwnerUUID = null;
    private UUID affinityPartnerUUID = null;
    private String affinityPartnerName = null;
    private int affinityLevel = 0;
    private BlockPos homePosition = BlockPos.ZERO;
    private int movementRadius = 200;
    private long visaExpireTime = 0L;
    private boolean isExile = false;
    private double exileDebt = 0.0;
    private boolean isFallen = false;
    private long survivalPassExpireTime = 0L;
    private long welfareCooldown = 0L;
    private boolean isWelfareRecipient = false;
    private long survivalPassCooldown = 0L;
    private long bondExpireTime = 0L;
    private int onlineTickCount = 0;
    private double taxPaidThisCycle = 0.0;
    private UUID activeContractId = null;
    private long lastProfessionChangeTime = 0L;
    private boolean hasSlaves = false;
    private int welfareClaimsThisCycle = 0;
    private long lastSubliminalMessageTick = 0;
    private int idleTicks = 0;
    private String dailyQuotaItem = "";
    private int dailyQuotaRequiredAmount = 0;
    private int dailyQuotaProgress = 0;
    private double dailyQuotaReward = 0.0;
    private long dailyQuotaCompletionTime = 0L;
    
    // Chapter 5: Career Binding & Slavery System
    private java.util.List<UUID> slaves = new java.util.ArrayList<>();
    private UUID masterUUID = null;
    private java.util.Map<UUID, Double> pendingTaxes = new java.util.HashMap<>();
    private java.util.Map<UUID, Double> extractedTaxes = new java.util.HashMap<>();
    private long lastWelfareTime = 0L;
    private long lastSurvivalPassTime = 0L;
    private long survivalPassExpiry = 0L;
    private int professionId = 0; // 0 = unemployed
    private java.util.Map<Integer, Integer> professionLevelById = new java.util.HashMap<>();
    private java.util.Map<Integer, Double> professionXpById = new java.util.HashMap<>();

    private final Map<Profession, Integer> professionLevels = new HashMap<>();
    private final Map<Profession, Double> professionXps = new HashMap<>();
    private final Map<Profession, Long> totalItemsSold = new HashMap<>();

    @Override
    public double getBalance() {
        return balance;
    }

    @Override
    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public void addBalance(double amount) {
        this.balance += amount;
    }

    @Override
    public double getTaxDebt() {
        return taxDebt;
    }

    @Override
    public void setTaxDebt(double taxDebt) {
        this.taxDebt = taxDebt;
    }

    @Override
    public int getClassTier() {
        return classTier;
    }

    @Override
    public void setClassTier(int classTier) {
        this.classTier = classTier;
    }

    @Override
    public Profession getProfession() {
        return profession;
    }

    @Override
    public void setProfession(Profession profession) {
        this.profession = profession;
    }

    @Override
    public UUID getSlaveOwnerUUID() {
        return slaveOwnerUUID;
    }

    @Override
    public void setSlaveOwnerUUID(UUID ownerUUID) {
        this.slaveOwnerUUID = ownerUUID;
    }

    @Override
    public UUID getAffinityPartnerUUID() {
        return affinityPartnerUUID;
    }

    @Override
    public void setAffinityPartnerUUID(UUID partnerUUID) {
        this.affinityPartnerUUID = partnerUUID;
    }

    @Override
    public String getAffinityPartnerName() {
        return affinityPartnerName;
    }

    @Override
    public void setAffinityPartnerName(String name) {
        this.affinityPartnerName = name;
    }

    @Override
    public int getAffinityLevel() {
        return affinityLevel;
    }

    @Override
    public void setAffinityLevel(int level) {
        this.affinityLevel = level;
    }

    @Override
    public BlockPos getHomePosition() {
        return homePosition;
    }

    @Override
    public void setHomePosition(BlockPos pos) {
        this.homePosition = pos != null ? pos : BlockPos.ZERO;
    }

    @Override
    public int getMovementRadius() {
        return movementRadius;
    }

    @Override
    public void setMovementRadius(int radius) {
        this.movementRadius = radius;
    }

    @Override
    public long getVisaExpireTime() {
        return visaExpireTime;
    }

    @Override
    public void setVisaExpireTime(long time) {
        this.visaExpireTime = time;
    }

    @Override
    public boolean isExile() {
        return isExile;
    }

    @Override
    public void setExile(boolean exile) {
        this.isExile = exile;
    }

    @Override
    public double getExileDebt() {
        return exileDebt;
    }

    @Override
    public void setExileDebt(double exileDebt) {
        this.exileDebt = exileDebt;
    }

    @Override
    public boolean isFallen() {
        return isFallen;
    }

    @Override
    public void setFallen(boolean fallen) {
        this.isFallen = fallen;
    }

    @Override
    public int getProfessionLevel(Profession profession) {
        return professionLevels.getOrDefault(profession, 0);
    }

    @Override
    public void setProfessionLevel(Profession profession, int level) {
        professionLevels.put(profession, level);
    }

    @Override
    public double getProfessionXp(Profession profession) {
        return professionXps.getOrDefault(profession, 0.0);
    }

    @Override
    public void setProfessionXp(Profession profession, double xp) {
        professionXps.put(profession, xp);
    }

    @Override
    public void addProfessionXp(Profession profession, double xp) {
        professionXps.put(profession, getProfessionXp(profession) + xp);
    }

    @Override
    public long getTotalItemsSold(Profession profession) {
        return totalItemsSold.getOrDefault(profession, 0L);
    }

    @Override
    public void setTotalItemsSold(Profession profession, long amount) {
        totalItemsSold.put(profession, amount);
    }

    @Override
    public void addTotalItemsSold(Profession profession, long amount) {
        totalItemsSold.put(profession, getTotalItemsSold(profession) + amount);
    }

    @Override
    public long getSurvivalPassExpireTime() {
        return survivalPassExpireTime;
    }

    @Override
    public void setSurvivalPassExpireTime(long time) {
        this.survivalPassExpireTime = time;
    }

    @Override
    public long getSurvivalPassCooldown() {
        return survivalPassCooldown;
    }

    @Override
    public void setSurvivalPassCooldown(long time) {
        this.survivalPassCooldown = time;
    }

    @Override
    public long getWelfareCooldown() {
        return welfareCooldown;
    }

    @Override
    public void setWelfareCooldown(long time) {
        this.welfareCooldown = time;
    }

    @Override
    public boolean isWelfareRecipient() {
        return isWelfareRecipient;
    }

    @Override
    public void setWelfareRecipient(boolean value) {
        this.isWelfareRecipient = value;
    }

    @Override
    public long getBondExpireTime() {
        return bondExpireTime;
    }

    @Override
    public void setBondExpireTime(long time) {
        this.bondExpireTime = time;
    }

    @Override
    public int getOnlineTickCount() {
        return onlineTickCount;
    }

    @Override
    public void setOnlineTickCount(int ticks) {
        this.onlineTickCount = ticks;
    }

    @Override
    public double getTaxPaidThisCycle() {
        return taxPaidThisCycle;
    }

    @Override
    public void setTaxPaidThisCycle(double amount) {
        this.taxPaidThisCycle = amount;
    }

    @Override
    public void addTaxPaidThisCycle(double amount) {
        this.taxPaidThisCycle += amount;
    }

    @Override
    public UUID getActiveContractId() {
        return activeContractId;
    }

    @Override
    public void setActiveContractId(UUID contractId) {
        this.activeContractId = contractId;
    }

    @Override
    public long getLastProfessionChangeTime() {
        return this.lastProfessionChangeTime;
    }

    @Override
    public void setLastProfessionChangeTime(long time) {
        this.lastProfessionChangeTime = time;
    }

    @Override
    public boolean hasSlaves() {
        return hasSlaves;
    }

    @Override
    public void setHasSlaves(boolean hasSlaves) {
        this.hasSlaves = hasSlaves;
    }

    @Override
    public int getWelfareClaimsThisCycle() {
        return welfareClaimsThisCycle;
    }

    @Override
    public void setWelfareClaimsThisCycle(int count) {
        this.welfareClaimsThisCycle = count;
    }

    @Override
    public void addWelfareClaimsThisCycle(int count) {
        this.welfareClaimsThisCycle += count;
    }

    @Override
    public long getLastSubliminalMessageTick() {
        return lastSubliminalMessageTick;
    }

    @Override
    public void setLastSubliminalMessageTick(long tick) {
        this.lastSubliminalMessageTick = tick;
    }

    @Override
    public int getIdleTicks() {
        return idleTicks;
    }

    @Override
    public void setIdleTicks(int ticks) {
        this.idleTicks = ticks;
    }

    @Override
    public String getDailyQuotaItem() { return dailyQuotaItem; }
    @Override
    public void setDailyQuotaItem(String itemId) { this.dailyQuotaItem = itemId; }
    
    @Override
    public int getDailyQuotaRequiredAmount() { return dailyQuotaRequiredAmount; }
    @Override
    public void setDailyQuotaRequiredAmount(int amount) { this.dailyQuotaRequiredAmount = amount; }
    
    @Override
    public int getDailyQuotaProgress() { return dailyQuotaProgress; }
    @Override
    public void setDailyQuotaProgress(int progress) { this.dailyQuotaProgress = progress; }
    
    @Override
    public double getDailyQuotaReward() { return dailyQuotaReward; }
    @Override
    public void setDailyQuotaReward(double reward) { this.dailyQuotaReward = reward; }
    
    @Override
    public long getDailyQuotaCompletionTime() { return dailyQuotaCompletionTime; }
    @Override
    public void setDailyQuotaCompletionTime(long time) { this.dailyQuotaCompletionTime = time; }
    
    @Override
    public boolean isDailyQuotaCompleted() { return dailyQuotaProgress >= dailyQuotaRequiredAmount; }
    
    // ===== Chapter 5: Career Binding & Slavery System =====
    
    @Override
    public java.util.List<UUID> getSlaves() { return new java.util.ArrayList<>(slaves); }
    
    @Override
    public void setSlaves(java.util.List<UUID> slaves) { this.slaves = new java.util.ArrayList<>(slaves); }
    
    @Override
    public void addSlave(UUID slaveUUID) { 
        if (!slaves.contains(slaveUUID)) {
            slaves.add(slaveUUID);
        }
    }
    
    @Override
    public void removeSlave(UUID slaveUUID) { slaves.remove(slaveUUID); }
    
    @Override
    public UUID getMasterUUID() { return slaveOwnerUUID; }
    
    @Override
    public void setMasterUUID(UUID masterUUID) { this.slaveOwnerUUID = masterUUID; }
    
    @Override
    public double getPendingTaxForMaster(UUID masterUUID) { 
        return pendingTaxes.getOrDefault(masterUUID, 0.0); 
    }
    
    @Override
    public void setPendingTaxForMaster(UUID masterUUID, double amount) { 
        pendingTaxes.put(masterUUID, amount); 
    }
    
    @Override
    public double getTotalExtractedTax(UUID masterUUID) { 
        return extractedTaxes.getOrDefault(masterUUID, 0.0); 
    }
    
    @Override
    public void setTotalExtractedTax(UUID masterUUID, double amount) { 
        extractedTaxes.put(masterUUID, amount); 
    }
    
    @Override
    public double claimPendingTax(UUID masterUUID) {
        double amount = pendingTaxes.getOrDefault(masterUUID, 0.0);
        pendingTaxes.put(masterUUID, 0.0);
        extractedTaxes.put(masterUUID, getTotalExtractedTax(masterUUID) + amount);
        return amount;
    }
    
    @Override
    public long getLastWelfareTime() { return lastWelfareTime; }
    
    @Override
    public void setLastWelfareTime(long time) { this.lastWelfareTime = time; }
    
    @Override
    public long getLastSurvivalPassTime() { return lastSurvivalPassTime; }
    
    @Override
    public void setLastSurvivalPassTime(long time) { this.lastSurvivalPassTime = time; }
    
    @Override
    public long getSurvivalPassExpiry() { return survivalPassExpiry; }
    
    @Override
    public void setSurvivalPassExpiry(long time) { this.survivalPassExpiry = time; }
    
    @Override
    public int getProfessionId() { return professionId; }
    
    @Override
    public void setProfessionId(int professionId) { this.professionId = professionId; }
    
    @Override
    public int getProfessionLevel(int professionId) { 
        return professionLevelById.getOrDefault(professionId, 0); 
    }
    
    @Override
    public void setProfessionLevel(int professionId, int level) { 
        professionLevelById.put(professionId, level); 
    }
    
    @Override
    public double getProfessionXp(int professionId) { 
        return professionXpById.getOrDefault(professionId, 0.0); 
    }
    
    @Override
    public void setProfessionXp(int professionId, double xp) { 
        professionXpById.put(professionId, xp); 
    }

    @Override
    public void copyFrom(IPlayerData source) {
        this.balance = source.getBalance();
        this.taxDebt = source.getTaxDebt();
        this.classTier = source.getClassTier();
        this.profession = source.getProfession();
        this.slaveOwnerUUID = source.getSlaveOwnerUUID();
        this.affinityPartnerUUID = source.getAffinityPartnerUUID();
        this.affinityPartnerName = source.getAffinityPartnerName();
        this.affinityLevel = source.getAffinityLevel();
        this.homePosition = source.getHomePosition();
        this.movementRadius = source.getMovementRadius();
        this.visaExpireTime = source.getVisaExpireTime();
        this.isExile = source.isExile();
        this.exileDebt = source.getExileDebt();
        this.isFallen = source.isFallen();
        this.survivalPassExpireTime = source.getSurvivalPassExpireTime();
        this.welfareCooldown = source.getWelfareCooldown();
        this.isWelfareRecipient = source.isWelfareRecipient();
        this.survivalPassCooldown = source.getSurvivalPassCooldown();
        this.bondExpireTime = source.getBondExpireTime();
        this.onlineTickCount = source.getOnlineTickCount();
        this.taxPaidThisCycle = source.getTaxPaidThisCycle();
        this.activeContractId = source.getActiveContractId();
        this.lastProfessionChangeTime = source.getLastProfessionChangeTime();
        this.hasSlaves = source.hasSlaves();
        this.welfareClaimsThisCycle = source.getWelfareClaimsThisCycle();
        this.lastSubliminalMessageTick = source.getLastSubliminalMessageTick();
        this.idleTicks = source.getIdleTicks();
        this.dailyQuotaItem = source.getDailyQuotaItem();
        this.dailyQuotaRequiredAmount = source.getDailyQuotaRequiredAmount();
        this.dailyQuotaProgress = source.getDailyQuotaProgress();
        this.dailyQuotaReward = source.getDailyQuotaReward();
        this.dailyQuotaCompletionTime = source.getDailyQuotaCompletionTime();
        
        // Chapter 5 data
        this.slaves = new java.util.ArrayList<>(source.getSlaves());
        this.lastWelfareTime = source.getLastWelfareTime();
        this.lastSurvivalPassTime = source.getLastSurvivalPassTime();
        this.survivalPassExpiry = source.getSurvivalPassExpiry();
        this.professionId = source.getProfessionId();

        for (Profession p : Profession.values()) {
            this.setProfessionLevel(p, source.getProfessionLevel(p));
            this.setProfessionXp(p, source.getProfessionXp(p));
            this.setTotalItemsSold(p, source.getTotalItemsSold(p));
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble("balance", balance);
        nbt.putDouble("taxDebt", taxDebt);
        nbt.putInt("classTier", classTier);
        nbt.putString("profession", profession.name());
        
        if (slaveOwnerUUID != null) nbt.putUUID("slaveOwnerUUID", slaveOwnerUUID);
        if (affinityPartnerUUID != null) {
            nbt.putUUID("affinityPartnerUUID", affinityPartnerUUID);
            if (affinityPartnerName != null) {
                nbt.putString("affinityPartnerName", affinityPartnerName);
            }
        }
        
        nbt.putInt("affinityLevel", affinityLevel);
        
        nbt.putInt("homeX", homePosition.getX());
        nbt.putInt("homeY", homePosition.getY());
        nbt.putInt("homeZ", homePosition.getZ());
        
        nbt.putInt("movementRadius", movementRadius);
        nbt.putLong("visaExpireTime", visaExpireTime);
        nbt.putBoolean("isExile", isExile);
        nbt.putBoolean("isFallen", isFallen);
        nbt.putDouble("exileDebt", exileDebt);
        nbt.putLong("survivalPassExpireTime", survivalPassExpireTime);
        nbt.putLong("survivalPassCooldown", survivalPassCooldown);
        nbt.putLong("welfareCooldown", welfareCooldown);
        nbt.putBoolean("isWelfareRecipient", isWelfareRecipient);
        nbt.putLong("bondExpireTime", bondExpireTime);
        nbt.putInt("onlineTickCount", onlineTickCount);
        nbt.putDouble("taxPaidThisCycle", taxPaidThisCycle);
        if (activeContractId != null) nbt.putUUID("activeContractId", activeContractId);
        nbt.putLong("lastProfessionChangeTime", lastProfessionChangeTime);
        nbt.putBoolean("hasSlaves", hasSlaves);
        nbt.putInt("welfareClaimsThisCycle", welfareClaimsThisCycle);
        nbt.putLong("lastSubliminalMessageTick", lastSubliminalMessageTick);
        nbt.putInt("idleTicks", idleTicks);
        nbt.putString("dailyQuotaItem", dailyQuotaItem);
        nbt.putInt("dailyQuotaRequiredAmount", dailyQuotaRequiredAmount);
        nbt.putInt("dailyQuotaProgress", dailyQuotaProgress);
        nbt.putDouble("dailyQuotaReward", dailyQuotaReward);
        nbt.putLong("dailyQuotaCompletionTime", dailyQuotaCompletionTime);
        
        // Chapter 5 serialization
        CompoundTag slavesTag = new CompoundTag();
        for (int i = 0; i < slaves.size(); i++) {
            slavesTag.putUUID("slave_" + i, slaves.get(i));
        }
        nbt.put("slaves", slavesTag);
        nbt.putInt("slaveCount", slaves.size());
        nbt.putLong("lastWelfareTime", lastWelfareTime);
        nbt.putLong("lastSurvivalPassTime", lastSurvivalPassTime);
        nbt.putLong("survivalPassExpiry", survivalPassExpiry);
        nbt.putInt("professionId", professionId);
        
        CompoundTag profLevels = new CompoundTag();
        for (java.util.Map.Entry<Integer, Integer> entry : professionLevelById.entrySet()) {
            profLevels.putInt("prof_" + entry.getKey(), entry.getValue());
        }
        nbt.put("professionLevelById", profLevels);
        
        CompoundTag profXps = new CompoundTag();
        for (java.util.Map.Entry<Integer, Double> entry : professionXpById.entrySet()) {
            profXps.putDouble("prof_" + entry.getKey(), entry.getValue());
        }
        nbt.put("professionXpById", profXps);

        CompoundTag levels = new CompoundTag();
        CompoundTag xps = new CompoundTag();
        CompoundTag totalSold = new CompoundTag();
        for (Profession p : Profession.values()) {
            levels.putInt(p.name(), getProfessionLevel(p));
            xps.putDouble(p.name(), getProfessionXp(p));
            totalSold.putLong(p.name(), getTotalItemsSold(p));
        }
        nbt.put("professionLevels", levels);
        nbt.put("professionXps", xps);
        nbt.put("totalItemsSold", totalSold);

        return nbt;
    }

    @Override
    public void saveNBTData(CompoundTag compound) {
        compound.merge(serializeNBT());
    }

    @Override
    public void loadNBTData(CompoundTag compound) {
        deserializeNBT(compound);
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.balance = nbt.getDouble("balance");
        this.taxDebt = nbt.getDouble("taxDebt");
        this.classTier = nbt.getInt("classTier");
        
        if (nbt.contains("profession")) {
            try {
                this.profession = Profession.valueOf(nbt.getString("profession"));
            } catch (Exception e) {
                this.profession = Profession.UNEMPLOYED;
            }
        }
        
        if (nbt.contains("slaveOwnerUUID")) this.slaveOwnerUUID = nbt.getUUID("slaveOwnerUUID");
        if (!nbt.contains("slaveOwnerUUID") && nbt.contains("masterUUID")) this.slaveOwnerUUID = nbt.getUUID("masterUUID");
        if (nbt.contains("affinityPartnerUUID")) {
            this.affinityPartnerUUID = nbt.getUUID("affinityPartnerUUID");
            if (nbt.contains("affinityPartnerName")) {
                this.affinityPartnerName = nbt.getString("affinityPartnerName");
            }
        }
        
        this.affinityLevel = nbt.getInt("affinityLevel");
        
        this.homePosition = new BlockPos(nbt.getInt("homeX"), nbt.getInt("homeY"), nbt.getInt("homeZ"));
        
        this.movementRadius = nbt.contains("movementRadius") ? nbt.getInt("movementRadius") : 200;
        this.visaExpireTime = nbt.getLong("visaExpireTime");
        this.isExile = nbt.getBoolean("isExile");
        this.isFallen = nbt.getBoolean("isFallen");
        this.exileDebt = nbt.getDouble("exileDebt");
        this.survivalPassExpireTime = nbt.getLong("survivalPassExpireTime");
        this.survivalPassCooldown = nbt.getLong("survivalPassCooldown");
        this.welfareCooldown = nbt.getLong("welfareCooldown");
        this.isWelfareRecipient = nbt.getBoolean("isWelfareRecipient");
        this.bondExpireTime = nbt.getLong("bondExpireTime");
        this.onlineTickCount = nbt.getInt("onlineTickCount");
        this.taxPaidThisCycle = nbt.getDouble("taxPaidThisCycle");
        if (nbt.contains("activeContractId")) this.activeContractId = nbt.getUUID("activeContractId");
        this.lastProfessionChangeTime = nbt.getLong("lastProfessionChangeTime");
        this.hasSlaves = nbt.getBoolean("hasSlaves");
        this.welfareClaimsThisCycle = nbt.getInt("welfareClaimsThisCycle");
        this.lastSubliminalMessageTick = nbt.getLong("lastSubliminalMessageTick");
        this.idleTicks = nbt.getInt("idleTicks");
        if (nbt.contains("dailyQuotaItem")) this.dailyQuotaItem = nbt.getString("dailyQuotaItem");
        if (nbt.contains("dailyQuotaRequiredAmount")) this.dailyQuotaRequiredAmount = nbt.getInt("dailyQuotaRequiredAmount");
        if (nbt.contains("dailyQuotaProgress")) this.dailyQuotaProgress = nbt.getInt("dailyQuotaProgress");
        if (nbt.contains("dailyQuotaReward")) this.dailyQuotaReward = nbt.getDouble("dailyQuotaReward");
        if (nbt.contains("dailyQuotaCompletionTime")) this.dailyQuotaCompletionTime = nbt.getLong("dailyQuotaCompletionTime");
        
        // Chapter 5 deserialization
        if (nbt.contains("slaves")) {
            CompoundTag slavesTag = nbt.getCompound("slaves");
            int slaveCount = nbt.getInt("slaveCount");
            this.slaves.clear();
            for (int i = 0; i < slaveCount; i++) {
                if (slavesTag.contains("slave_" + i)) {
                    this.slaves.add(slavesTag.getUUID("slave_" + i));
                }
            }
        }
        if (nbt.contains("lastWelfareTime")) this.lastWelfareTime = nbt.getLong("lastWelfareTime");
        if (nbt.contains("lastSurvivalPassTime")) this.lastSurvivalPassTime = nbt.getLong("lastSurvivalPassTime");
        if (nbt.contains("survivalPassExpiry")) this.survivalPassExpiry = nbt.getLong("survivalPassExpiry");
        if (nbt.contains("professionId")) this.professionId = nbt.getInt("professionId");
        
        if (nbt.contains("professionLevelById")) {
            CompoundTag profLevels = nbt.getCompound("professionLevelById");
            for (String key : profLevels.getAllKeys()) {
                try {
                    int profId = Integer.parseInt(key.replace("prof_", ""));
                    professionLevelById.put(profId, profLevels.getInt(key));
                } catch (NumberFormatException ignored) {}
            }
        }
        
        if (nbt.contains("professionXpById")) {
            CompoundTag profXps = nbt.getCompound("professionXpById");
            for (String key : profXps.getAllKeys()) {
                try {
                    int profId = Integer.parseInt(key.replace("prof_", ""));
                    professionXpById.put(profId, profXps.getDouble(key));
                } catch (NumberFormatException ignored) {}
            }
        }

        if (nbt.contains("professionLevels")) {
            CompoundTag levels = nbt.getCompound("professionLevels");
            for (Profession p : Profession.values()) {
                if (levels.contains(p.name())) {
                    setProfessionLevel(p, levels.getInt(p.name()));
                }
            }
        }
        if (nbt.contains("professionXps")) {
            CompoundTag xps = nbt.getCompound("professionXps");
            for (Profession p : Profession.values()) {
                if (xps.contains(p.name())) {
                    setProfessionXp(p, xps.getDouble(p.name()));
                }
            }
        }
        if (nbt.contains("totalItemsSold")) {
            CompoundTag totalSold = nbt.getCompound("totalItemsSold");
            for (Profession p : Profession.values()) {
                if (totalSold.contains(p.name())) {
                    setTotalItemsSold(p, totalSold.getLong(p.name()));
                }
            }
        }
    }
}
