package com.symbioticlaw.client;

import com.symbioticlaw.contract.Contract;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class ClientContractCache {
    private static final Map<UUID, Contract> CACHE = new ConcurrentHashMap<>();

    public static void updateCache(List<Contract> contracts) {
        CACHE.clear();
        for (Contract contract : contracts) {
            CACHE.put(contract.contractId, contract);
        }
    }

    public static Contract getContract(UUID id) {
        return CACHE.get(id);
    }
}