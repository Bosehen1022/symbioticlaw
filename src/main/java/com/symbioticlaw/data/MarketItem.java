package com.symbioticlaw.data;

import net.minecraft.nbt.CompoundTag;

public class MarketItem {
    public final String itemId;
    public double basePrice;
    public int targetStock;
    public int currentStock;
    public double lastCyclePrice;

    public MarketItem(String itemId, double basePrice, int targetStock, int currentStock) {
        this.itemId = itemId;
        this.basePrice = basePrice;
        this.targetStock = targetStock;
        this.currentStock = currentStock;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("itemId", itemId);
        tag.putDouble("basePrice", basePrice);
        tag.putInt("targetStock", targetStock);
        tag.putInt("currentStock", currentStock);
        tag.putDouble("lastCyclePrice", lastCyclePrice);
        return tag;
    }

    public static MarketItem deserializeNBT(CompoundTag tag) {
        MarketItem item = new MarketItem(
                tag.getString("itemId"),
                tag.getDouble("basePrice"),
                tag.getInt("targetStock"),
                tag.getInt("currentStock")
        );
        item.lastCyclePrice = tag.getDouble("lastCyclePrice");
        return item;
    }
}
