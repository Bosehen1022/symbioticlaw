package com.symbioticlaw.data;

public class MarketInfo {
    public record AdjustedPrice(double buyPrice, double sellPrice) {}
    public record MarketItemWithPrice(MarketItem item, AdjustedPrice adjustedPrice) {}
}
