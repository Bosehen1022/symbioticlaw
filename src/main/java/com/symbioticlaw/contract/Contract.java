package com.symbioticlaw.contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class Contract {
    public final UUID contractId;
    public final UUID issuerId;
    public final String issuerName;
    public final ContractType type;
    public final Item objectiveItem;
    public final int objectiveAmount;
    public final double reward;
    public long expiryTime;

    public ContractStatus status;
    public UUID acceptorId;
    public String acceptorName;

    // For BOND contracts
    public int affinity;

    public Contract(UUID issuerId, String issuerName, ContractType type, Item objectiveItem, int objectiveAmount, double reward, long durationTicks) {
        this.contractId = UUID.randomUUID();
        this.issuerId = issuerId;
        this.issuerName = issuerName;
        this.type = type;
        this.objectiveItem = objectiveItem;
        this.objectiveAmount = objectiveAmount;
        this.reward = reward;
        this.expiryTime = System.currentTimeMillis() + (durationTicks / 20 * 1000);
        this.status = ContractStatus.OPEN;
    }

    public Contract(UUID partner1Id, String partner1Name, UUID partner2Id, String partner2Name, long durationTicks) {
        this.contractId = UUID.randomUUID();
        this.issuerId = partner1Id;
        this.issuerName = partner1Name;
        this.type = ContractType.BOND;
        this.objectiveItem = null;
        this.objectiveAmount = 0;
        this.reward = 0;
        this.expiryTime = System.currentTimeMillis() + (durationTicks / 20 * 1000);
        this.status = ContractStatus.ACCEPTED; // Bonds are active immediately
        this.acceptorId = partner2Id;
        this.acceptorName = partner2Name;
        this.affinity = 150;
    }

    // Private constructor for network deserialization
    private Contract(UUID contractId, UUID issuerId, String issuerName, ContractType type, Item objectiveItem, int objectiveAmount, double reward, long expiryTime, ContractStatus status, UUID acceptorId, String acceptorName, int affinity) {
        this.contractId = contractId;
        this.issuerId = issuerId;
        this.issuerName = issuerName;
        this.type = type;
        this.objectiveItem = objectiveItem;
        this.objectiveAmount = objectiveAmount;
        this.reward = reward;
        this.expiryTime = expiryTime;
        this.status = status;
        this.acceptorId = acceptorId;
        this.acceptorName = acceptorName;
        this.affinity = affinity;
    }

    private Contract(CompoundTag nbt) {
        this.contractId = nbt.getUUID("contractId");
        this.issuerId = nbt.getUUID("issuerId");
        this.issuerName = nbt.getString("issuerName");
        this.type = ContractType.valueOf(nbt.getString("type"));
        this.objectiveItem = nbt.contains("objectiveItem") ? ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(nbt.getString("objectiveItem"))) : null;
        this.objectiveAmount = nbt.getInt("objectiveAmount");
        this.reward = nbt.getDouble("reward");
        this.expiryTime = nbt.getLong("expiryTime");
        this.status = ContractStatus.valueOf(nbt.getString("status"));
        if (nbt.hasUUID("acceptorId")) {
            this.acceptorId = nbt.getUUID("acceptorId");
            this.acceptorName = nbt.getString("acceptorName");
        }
        if (this.type == ContractType.BOND) {
            this.affinity = nbt.getInt("affinity");
        }
    }

    public CompoundTag save(CompoundTag nbt) {
        nbt.putUUID("contractId", this.contractId);
        nbt.putUUID("issuerId", this.issuerId);
        nbt.putString("issuerName", this.issuerName);
        nbt.putString("type", this.type.name());
        ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(this.objectiveItem);
        if (itemKey != null) {
            nbt.putString("objectiveItem", itemKey.toString());
        }
        nbt.putInt("objectiveAmount", this.objectiveAmount);
        nbt.putDouble("reward", this.reward);
        nbt.putLong("expiryTime", this.expiryTime);
        nbt.putString("status", this.status.name());
        if (this.acceptorId != null) {
            nbt.putUUID("acceptorId", this.acceptorId);
            nbt.putString("acceptorName", this.acceptorName);
        }
        if (this.type == ContractType.BOND) {
            nbt.putInt("affinity", this.affinity);
        }
        return nbt;
    }

    public static Contract load(CompoundTag nbt) {
        return new Contract(nbt);
    }

    public void toBuffer(FriendlyByteBuf buf) {
        buf.writeUUID(this.contractId);
        buf.writeUUID(this.issuerId);
        buf.writeUtf(this.issuerName);
        buf.writeEnum(this.type);
        ResourceLocation itemRl = this.objectiveItem != null ? ForgeRegistries.ITEMS.getKey(this.objectiveItem) : null;
        buf.writeBoolean(itemRl != null);
        if (itemRl != null) {
            buf.writeResourceLocation(itemRl);
        }
        buf.writeInt(this.objectiveAmount);
        buf.writeDouble(this.reward);
        buf.writeLong(this.expiryTime);
        buf.writeEnum(this.status);
        buf.writeBoolean(this.acceptorId != null);
        if (this.acceptorId != null) {
            buf.writeUUID(this.acceptorId);
            buf.writeUtf(this.acceptorName);
        }
        if (this.type == ContractType.BOND) {
            buf.writeInt(this.affinity);
        }
    }

    public static Contract fromBuffer(FriendlyByteBuf buf) {
        UUID contractId = buf.readUUID();
        UUID issuerId = buf.readUUID();
        String issuerName = buf.readUtf();
        ContractType type = buf.readEnum(ContractType.class);
        Item objectiveItem = null;
        if (buf.readBoolean()) {
            objectiveItem = ForgeRegistries.ITEMS.getValue(buf.readResourceLocation());
        }
        int objectiveAmount = buf.readInt();
        double reward = buf.readDouble();
        long expiryTime = buf.readLong();
        ContractStatus status = buf.readEnum(ContractStatus.class);

        UUID acceptorId = null;
        String acceptorName = null;
        if (buf.readBoolean()) {
            acceptorId = buf.readUUID();
            acceptorName = buf.readUtf();
        }

        int affinity = 0;
        if (type == ContractType.BOND) {
            affinity = buf.readInt();
        }

        return new Contract(contractId, issuerId, issuerName, type, objectiveItem, objectiveAmount, reward, expiryTime, status, acceptorId, acceptorName, affinity);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > this.expiryTime;
    }

    public void accept(UUID acceptorId, String acceptorName) {
        if (this.status == ContractStatus.OPEN) {
            this.status = ContractStatus.ACCEPTED;
            this.acceptorId = acceptorId;
            this.acceptorName = acceptorName;
        }
    }
}