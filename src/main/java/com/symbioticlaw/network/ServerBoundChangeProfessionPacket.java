package com.symbioticlaw.network;

import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.professions.Profession;
import com.symbioticlaw.system.ProfessionChangeSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundChangeProfessionPacket {
    private final Profession profession;

    public ServerBoundChangeProfessionPacket(Profession profession) {
        this.profession = profession;
    }

    public ServerBoundChangeProfessionPacket(FriendlyByteBuf buf) {
        this.profession = buf.readEnum(Profession.class);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(this.profession);
    }

    public static void handle(ServerBoundChangeProfessionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            
            // 检查是否可以转职
            if (ProfessionChangeSystem.canChangeProfession(player, msg.profession)) {
                // 显示契约信息
                ProfessionChangeSystem.displayContractInfo(player, msg.profession);
                
                // 执行转职
                ProfessionChangeSystem.executeProfessionChange(player, msg.profession);

                // 同步玩家数据
                player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                    NetworkHandler.sendToPlayer(player, new PlayerDataSyncPacket(playerData));
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void encode(ServerBoundChangeProfessionPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ServerBoundChangeProfessionPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundChangeProfessionPacket(buffer);
    }
}
