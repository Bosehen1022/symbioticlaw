package com.symbioticlaw.data;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UnclaimedIncomeData {

    private final Map<UUID, Double> unclaimedIncome = new HashMap<>();

    public void addIncome(UUID ownerId, double amount) {
        unclaimedIncome.put(ownerId, unclaimedIncome.getOrDefault(ownerId, 0.0) + amount);
    }

    public double getIncome(UUID ownerId) {
        return unclaimedIncome.getOrDefault(ownerId, 0.0);
    }

    public double claimIncome(UUID ownerId) {
        Double income = unclaimedIncome.remove(ownerId);
        return income != null ? income : 0.0;
    }

    public void load(CompoundTag nbt) {
        unclaimedIncome.clear();
        CompoundTag incomeTag = nbt.getCompound("UnclaimedIncome");
        for (String key : incomeTag.getAllKeys()) {
            unclaimedIncome.put(UUID.fromString(key), incomeTag.getDouble(key));
        }
    }

    public void save(CompoundTag nbt) {
        CompoundTag incomeTag = new CompoundTag();
        for (Map.Entry<UUID, Double> entry : unclaimedIncome.entrySet()) {
            incomeTag.putDouble(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("UnclaimedIncome", incomeTag);
    }
}
