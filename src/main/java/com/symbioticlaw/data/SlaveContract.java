package com.symbioticlaw.data;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class SlaveContract {
    public final UUID ownerUUID;
    public final UUID slaveUUID;
    public final double incomeShare = 0.4; // 40%
    public final long contractStartTime;

    public SlaveContract(UUID ownerUUID, UUID slaveUUID, long startTime) {
        this.ownerUUID = ownerUUID;
        this.slaveUUID = slaveUUID;
        this.contractStartTime = startTime;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("ownerUUID", ownerUUID);
        tag.putUUID("slaveUUID", slaveUUID);
        tag.putLong("contractStartTime", contractStartTime);
        return tag;
    }

    public static SlaveContract deserializeNBT(CompoundTag tag) {
        UUID owner = tag.getUUID("ownerUUID");
        UUID slave = tag.getUUID("slaveUUID");
        long startTime = tag.getLong("contractStartTime");
        return new SlaveContract(owner, slave, startTime);
    }
}
