package com.symbioticlaw.network;

import com.symbioticlaw.client.ClientEconomyCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundDailyQuotaPacket {

    private final String itemName;
    private final int requiredAmount;
    private final int progress;
    private final double reward;

    public ClientBoundDailyQuotaPacket(String itemName, int requiredAmount, int progress, double reward) {
        this.itemName = itemName;
        this.requiredAmount = requiredAmount;
        this.progress = progress;
        this.reward = reward;
    }

    public ClientBoundDailyQuotaPacket(FriendlyByteBuf buf) {
        this.itemName = buf.readUtf();
        this.requiredAmount = buf.readInt();
        this.progress = buf.readInt();
        this.reward = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(itemName);
        buf.writeInt(requiredAmount);
        buf.writeInt(progress);
        buf.writeDouble(reward);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientEconomyCache.updateDailyQuota(itemName, requiredAmount, progress, reward);
        });
        context.setPacketHandled(true);
        return true;
    }
}
