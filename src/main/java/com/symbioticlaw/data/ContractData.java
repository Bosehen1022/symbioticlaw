package com.symbioticlaw.data;

import com.symbioticlaw.contract.Contract;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public class ContractData extends SavedData {
    private static final String DATA_NAME = "symbioticlaw_contracts";
    private final Map<UUID, Contract> contracts = new ConcurrentHashMap<>();

    public Map<UUID, Contract> getContracts() {
        return contracts;
    }

    public void addContract(Contract contract) {
        contracts.put(contract.contractId, contract);
        setDirty();
    }

    public Contract getContract(UUID contractId) {
        return contracts.get(contractId);
    }

    public void removeContract(UUID contractId) {
        contracts.remove(contractId);
        setDirty();
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag pCompoundTag) {
        ListTag listTag = new ListTag();
        for (Contract contract : contracts.values()) {
            listTag.add(contract.save(new CompoundTag()));
        }
        pCompoundTag.put("contracts", listTag);
        return pCompoundTag;
    }

    @Nonnull
    public static ContractData load(@Nonnull CompoundTag pCompoundTag) {
        ContractData contractData = new ContractData();
        ListTag listTag = pCompoundTag.getList("contracts", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            Contract contract = Contract.load(listTag.getCompound(i));
            contractData.contracts.put(contract.contractId, contract);
        }
        return contractData;
    }

    public static ContractData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(ContractData::load, ContractData::new, DATA_NAME);
    }
}