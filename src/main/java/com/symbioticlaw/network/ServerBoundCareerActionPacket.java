package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 职业相关操作数据包
 * 客户端 -> 服务器
 */
public class ServerBoundCareerActionPacket {
    
    public enum ActionType {
        REQUEST_CAREER_CHANGE,      // 申请转职
        CONFIRM_CAREER_CHANGE,      // 确认转职（签订契约）
        APPLY_FOR_JOB,              // 申请入职（无业->职业）
        RESIGN,                     // 辞职（转无业，有违约金）
        CLAIM_WELFARE,              // 领取低保
        REQUEST_SURVIVAL_PASS       // 申请生存假票
    }
    
    private final ActionType actionType;
    private final int professionId; // 目标职业ID（仅用于入职/转职）
    private final boolean confirmed; // 是否已确认契约
    
    public ServerBoundCareerActionPacket(ActionType actionType) {
        this(actionType, -1, false);
    }
    
    public ServerBoundCareerActionPacket(ActionType actionType, int professionId) {
        this(actionType, professionId, false);
    }
    
    public ServerBoundCareerActionPacket(ActionType actionType, int professionId, boolean confirmed) {
        this.actionType = actionType;
        this.professionId = professionId;
        this.confirmed = confirmed;
    }
    
    public ServerBoundCareerActionPacket(FriendlyByteBuf buf) {
        this.actionType = ActionType.values()[buf.readInt()];
        this.professionId = buf.readInt();
        this.confirmed = buf.readBoolean();
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.actionType.ordinal());
        buf.writeInt(this.professionId);
        buf.writeBoolean(this.confirmed);
    }
    
    public static void handle(ServerBoundCareerActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                com.symbioticlaw.system.CareerSystem.handleCareerAction(player, msg.actionType, msg.professionId, msg.confirmed);
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void encode(ServerBoundCareerActionPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ServerBoundCareerActionPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundCareerActionPacket(buffer);
    }
    
    public ActionType getActionType() {
        return actionType;
    }
    
    public int getProfessionId() {
        return professionId;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}
