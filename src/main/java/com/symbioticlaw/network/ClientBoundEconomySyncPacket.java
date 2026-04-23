package com.symbioticlaw.network;

import com.symbioticlaw.client.ClientEconomyCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 服务端发送综合经济数据同步包
 * 包含：批发价格、税率、经济指数、市场数据、每日定额
 * 
 * 服务器 -> 客户端
 */
public class ClientBoundEconomySyncPacket {
    
    // 批发价格
    private final Map<String, Double> wholesalePrices;
    
    // 税率
    private final double transferTaxRate;
    private final double incomeTaxRate;
    private final double salesTaxRate;
    private final double classTaxMultiplier;
    
    // 经济指数
    private final double economyIndex;
    private final double inflationRate;
    private final long totalMoneySupply;
    
    // 每日定额
    private final String dailyQuotaItem;
    private final int dailyQuotaRequired;
    private final int dailyQuotaProgress;
    private final double dailyQuotaReward;
    
    public ClientBoundEconomySyncPacket(
            Map<String, Double> wholesalePrices,
            double transferTaxRate,
            double incomeTaxRate,
            double salesTaxRate,
            double classTaxMultiplier,
            double economyIndex,
            double inflationRate,
            long totalMoneySupply,
            String dailyQuotaItem,
            int dailyQuotaRequired,
            int dailyQuotaProgress,
            double dailyQuotaReward) {
        this.wholesalePrices = wholesalePrices;
        this.transferTaxRate = transferTaxRate;
        this.incomeTaxRate = incomeTaxRate;
        this.salesTaxRate = salesTaxRate;
        this.classTaxMultiplier = classTaxMultiplier;
        this.economyIndex = economyIndex;
        this.inflationRate = inflationRate;
        this.totalMoneySupply = totalMoneySupply;
        this.dailyQuotaItem = dailyQuotaItem;
        this.dailyQuotaRequired = dailyQuotaRequired;
        this.dailyQuotaProgress = dailyQuotaProgress;
        this.dailyQuotaReward = dailyQuotaReward;
    }
    
    public ClientBoundEconomySyncPacket(FriendlyByteBuf buf) {
        // 读取批发价格
        int wholesaleSize = buf.readInt();
        this.wholesalePrices = new HashMap<>();
        for (int i = 0; i < wholesaleSize; i++) {
            this.wholesalePrices.put(buf.readUtf(), buf.readDouble());
        }
        
        // 读取税率
        this.transferTaxRate = buf.readDouble();
        this.incomeTaxRate = buf.readDouble();
        this.salesTaxRate = buf.readDouble();
        this.classTaxMultiplier = buf.readDouble();
        
        // 读取经济指数
        this.economyIndex = buf.readDouble();
        this.inflationRate = buf.readDouble();
        this.totalMoneySupply = buf.readLong();
        
        // 读取每日定额
        this.dailyQuotaItem = buf.readUtf();
        this.dailyQuotaRequired = buf.readInt();
        this.dailyQuotaProgress = buf.readInt();
        this.dailyQuotaReward = buf.readDouble();
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        // 写入批发价格
        buf.writeInt(wholesalePrices.size());
        for (Map.Entry<String, Double> entry : wholesalePrices.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeDouble(entry.getValue());
        }
        
        // 写入税率
        buf.writeDouble(transferTaxRate);
        buf.writeDouble(incomeTaxRate);
        buf.writeDouble(salesTaxRate);
        buf.writeDouble(classTaxMultiplier);
        
        // 写入经济指数
        buf.writeDouble(economyIndex);
        buf.writeDouble(inflationRate);
        buf.writeLong(totalMoneySupply);
        
        // 写入每日定额
        buf.writeUtf(dailyQuotaItem);
        buf.writeInt(dailyQuotaRequired);
        buf.writeInt(dailyQuotaProgress);
        buf.writeDouble(dailyQuotaReward);
    }
    
    public static void handle(ClientBoundEconomySyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 更新客户端缓存
            ClientEconomyCache.updateWholesalePrices(msg.wholesalePrices);
            ClientEconomyCache.updateTaxRates(
                msg.transferTaxRate, 
                msg.incomeTaxRate, 
                msg.salesTaxRate, 
                msg.classTaxMultiplier
            );
            ClientEconomyCache.updateEconomyIndex(
                msg.economyIndex, 
                msg.inflationRate, 
                msg.totalMoneySupply
            );
            ClientEconomyCache.updateDailyQuota(
                msg.dailyQuotaItem,
                msg.dailyQuotaRequired,
                msg.dailyQuotaProgress,
                msg.dailyQuotaReward
            );
            ClientEconomyCache.markSynced();
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void encode(ClientBoundEconomySyncPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ClientBoundEconomySyncPacket decode(FriendlyByteBuf buffer) {
        return new ClientBoundEconomySyncPacket(buffer);
    }
}
