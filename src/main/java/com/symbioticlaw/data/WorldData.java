package com.symbioticlaw.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;
import java.util.stream.Collectors;

public class WorldData extends SavedData {
    private static final String NAME = "symbioticlaw_world_data";
    public final MarketData marketData = new MarketData();
    private BlockPos corePosition = BlockPos.ZERO;
    private final UnclaimedIncomeData unclaimedIncome = new UnclaimedIncomeData();
    private double totalTaxCollected = 0;
    private final Map<UUID, SlaveContract> contracts = new HashMap<>(); // Slave UUID -> Contract

    private String dailyQuotaItem = "";
    private int dailyQuotaAmount = 0;
    private double dailyQuotaReward = 0.0;
    private final Map<UUID, Boolean> dailyQuotaCompleted = new HashMap<>();
    private long lastQuotaRefreshTime = 0;

    public WorldData() {}

    private WorldData(CompoundTag tag) {
        if (tag.contains("marketData")) {
            marketData.load(tag.getCompound("marketData"));
        }
        if (tag.contains("corePosition")) {
            corePosition = NbtUtils.readBlockPos(tag.getCompound("corePosition"));
        }
        if (tag.contains("unclaimedIncome")) {
            unclaimedIncome.load(tag.getCompound("unclaimedIncome"));
        }
        if (tag.contains("totalTaxCollected")) {
            totalTaxCollected = tag.getDouble("totalTaxCollected");
        }
        if (tag.contains("contracts")) {
            ListTag list = tag.getList("contracts", 10);
            for (int i = 0; i < list.size(); i++) {
                SlaveContract contract = SlaveContract.deserializeNBT(list.getCompound(i));
                contracts.put(contract.slaveUUID, contract);
            }
        }
        if (tag.contains("dailyQuotaItem")) {
            dailyQuotaItem = tag.getString("dailyQuotaItem");
            dailyQuotaAmount = tag.getInt("dailyQuotaAmount");
            dailyQuotaReward = tag.getDouble("dailyQuotaReward");
            if (tag.contains("lastQuotaRefreshTime")) {
                lastQuotaRefreshTime = tag.getLong("lastQuotaRefreshTime");
            }
            dailyQuotaCompleted.clear();
            if (tag.contains("dailyQuotaCompleted")) {
                ListTag completedList = tag.getList("dailyQuotaCompleted", 10);
                for (int i = 0; i < completedList.size(); i++) {
                    CompoundTag completedTag = completedList.getCompound(i);
                    dailyQuotaCompleted.put(completedTag.getUUID("player"), completedTag.getBoolean("completed"));
                }
            }
        }
    }

    public static WorldData load(CompoundTag tag) {
        return new WorldData(tag);
    }

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        CompoundTag marketDataTag = new CompoundTag();
        marketData.save(marketDataTag);
        pCompoundTag.put("marketData", marketDataTag);

        pCompoundTag.put("corePosition", NbtUtils.writeBlockPos(corePosition));

        CompoundTag unclaimedIncomeTag = new CompoundTag();
        unclaimedIncome.save(unclaimedIncomeTag);
        pCompoundTag.put("unclaimedIncome", unclaimedIncomeTag);

        pCompoundTag.putDouble("totalTaxCollected", totalTaxCollected);

        ListTag contractList = new ListTag();
        for (SlaveContract contract : contracts.values()) {
            contractList.add(contract.serializeNBT());
        }
        pCompoundTag.put("contracts", contractList);

        pCompoundTag.putString("dailyQuotaItem", dailyQuotaItem);
        pCompoundTag.putInt("dailyQuotaAmount", dailyQuotaAmount);
        pCompoundTag.putDouble("dailyQuotaReward", dailyQuotaReward);
        pCompoundTag.putLong("lastQuotaRefreshTime", lastQuotaRefreshTime);

        ListTag completedList = new ListTag();
        for (Map.Entry<UUID, Boolean> entry : dailyQuotaCompleted.entrySet()) {
            CompoundTag completedTag = new CompoundTag();
            completedTag.putUUID("player", entry.getKey());
            completedTag.putBoolean("completed", entry.getValue());
            completedList.add(completedTag);
        }
        pCompoundTag.put("dailyQuotaCompleted", completedList);

        return pCompoundTag;
    }

    public static WorldData get(ServerLevel level) {
        if (level == null || level.getServer() == null) {
            return null;
        }
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(WorldData::load, WorldData::new, NAME);
    }

    public BlockPos getCorePosition() {
        return corePosition;
    }

    public void setCorePosition(BlockPos pos) {
        this.corePosition = pos;
        setDirty();
    }

    public UnclaimedIncomeData getUnclaimedIncomeData() {
        return unclaimedIncome;
    }

    public MarketData getMarketData() {
        return marketData;
    }

    public double getTotalTaxCollected() {
        return totalTaxCollected;
    }

    public void addTotalTaxCollected(double amount) {
        this.totalTaxCollected += amount;
        setDirty();
    }

    public void addContract(SlaveContract contract) {
        contracts.put(contract.slaveUUID, contract);
        setDirty();
    }

    public void removeContract(UUID slaveUUID) {
        contracts.remove(slaveUUID);
        setDirty();
    }

    public List<UUID> getSlavesOf(UUID ownerUUID) {
        return contracts.values().stream()
                .filter(c -> c.ownerUUID.equals(ownerUUID))
                .map(c -> c.slaveUUID)
                .collect(Collectors.toList());
    }

    public String getDailyQuotaItem() {
        return dailyQuotaItem;
    }

    public int getDailyQuotaAmount() {
        return dailyQuotaAmount;
    }

    public double getDailyQuotaReward() {
        return dailyQuotaReward;
    }

    public void setDailyQuota(String item, int amount, double reward) {
        this.dailyQuotaItem = item;
        this.dailyQuotaAmount = amount;
        this.dailyQuotaReward = reward;
        this.dailyQuotaCompleted.clear();
        this.lastQuotaRefreshTime = System.currentTimeMillis();
        setDirty();
    }

    public long getLastQuotaRefreshTime() {
        return lastQuotaRefreshTime;
    }

    public boolean hasCompletedDailyQuota(UUID playerUUID) {
        return dailyQuotaCompleted.getOrDefault(playerUUID, false);
    }

    public void setDailyQuotaCompleted(UUID playerUUID) {
        dailyQuotaCompleted.put(playerUUID, true);
        setDirty();
    }
}
