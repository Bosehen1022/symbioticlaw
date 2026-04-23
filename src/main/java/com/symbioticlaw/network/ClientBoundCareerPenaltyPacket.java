package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 职业变更惩罚信息数据包
 * 服务器 -> 客户端
 * 用于显示违约金等详细信息
 */
public class ClientBoundCareerPenaltyPacket {
    
    public static class CareerPenaltyInfo {
        private final int currentProfessionId;
        private final int targetProfessionId;
        private final double penaltyAmount;       // 违约金
        private final int currentLevel;           // 当前等级
        private final double currentXp;           // 当前经验值（将被清零）
        private final boolean canAfford;          // 是否负担得起
        private final String professionName;      // 职业名称
        
        public CareerPenaltyInfo(int currentProfessionId, int targetProfessionId, 
                                 double penaltyAmount, int currentLevel, double currentXp,
                                 boolean canAfford, String professionName) {
            this.currentProfessionId = currentProfessionId;
            this.targetProfessionId = targetProfessionId;
            this.penaltyAmount = penaltyAmount;
            this.currentLevel = currentLevel;
            this.currentXp = currentXp;
            this.canAfford = canAfford;
            this.professionName = professionName;
        }
        
        public int getCurrentProfessionId() { return currentProfessionId; }
        public int getTargetProfessionId() { return targetProfessionId; }
        public double getPenaltyAmount() { return penaltyAmount; }
        public int getCurrentLevel() { return currentLevel; }
        public double getCurrentXp() { return currentXp; }
        public boolean canAfford() { return canAfford; }
        public String getProfessionName() { return professionName; }
    }
    
    private final CareerPenaltyInfo info;
    
    public ClientBoundCareerPenaltyPacket(CareerPenaltyInfo info) {
        this.info = info;
    }
    
    public ClientBoundCareerPenaltyPacket(FriendlyByteBuf buf) {
        boolean hasInfo = buf.readBoolean();
        if (hasInfo) {
            this.info = new CareerPenaltyInfo(
                buf.readInt(),
                buf.readInt(),
                buf.readDouble(),
                buf.readInt(),
                buf.readDouble(),
                buf.readBoolean(),
                buf.readUtf()
            );
        } else {
            this.info = null;
        }
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(info != null);
        if (info != null) {
            buf.writeInt(info.currentProfessionId);
            buf.writeInt(info.targetProfessionId);
            buf.writeDouble(info.penaltyAmount);
            buf.writeInt(info.currentLevel);
            buf.writeDouble(info.currentXp);
            buf.writeBoolean(info.canAfford);
            buf.writeUtf(info.professionName);
        }
    }
    
    public CareerPenaltyInfo getInfo() {
        return info;
    }
    
    public static void handle(ClientBoundCareerPenaltyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            com.symbioticlaw.client.ClientPlayerData.setCareerPenaltyInfo(msg.getInfo());
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void encode(ClientBoundCareerPenaltyPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ClientBoundCareerPenaltyPacket decode(FriendlyByteBuf buffer) {
        return new ClientBoundCareerPenaltyPacket(buffer);
    }
}
