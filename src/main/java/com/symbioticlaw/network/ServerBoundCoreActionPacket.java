package com.symbioticlaw.network;

import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.UnclaimedIncomeData;
import com.symbioticlaw.data.WorldData;
import com.symbioticlaw.system.AffinitySystem;
import com.symbioticlaw.system.ClassSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundCoreActionPacket {

    public enum ActionType {
        COMPLETE_CONTRACT,
        PAY_CREDIT_RECOVERY,
        REQUEST_SURVIVAL_PASS,
        CLAIM_WELFARE,
        PURCHASE_BUSINESS_VISA,
        RENEW_BOND,
        BREAK_BOND,
        CLAIM_INCOME
    }

    private final ActionType action;

    public ServerBoundCoreActionPacket(ActionType action) {
        this.action = action;
    }

    public ServerBoundCoreActionPacket(FriendlyByteBuf buffer) {
        this.action = buffer.readEnum(ActionType.class);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.action);
    }

    public static ServerBoundCoreActionPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundCoreActionPacket(buffer.readEnum(ActionType.class));
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // This should be a singleton or obtained from a central place, but for now a new instance is fine as it's stateless
            ClassSystem classSystem = new ClassSystem();

            switch (action) {
                case REQUEST_SURVIVAL_PASS: {
                    classSystem.requestSurvivalPass(player);
                    PlayerDataCapability.sync(player);
                    break;
                }
                case PAY_CREDIT_RECOVERY: {
                    classSystem.payCreditRecoveryFee(player);
                    PlayerDataCapability.sync(player);
                    break;
                }
                case CLAIM_WELFARE: {
                    classSystem.claimWelfare(player);
                    PlayerDataCapability.sync(player);
                    break;
                }
                case PURCHASE_BUSINESS_VISA: {
                    classSystem.buyBusinessVisa(player);
                    PlayerDataCapability.sync(player);
                    break;
                }
                case RENEW_BOND: {
                    new AffinitySystem().renewBond(player);
                    PlayerDataCapability.sync(player);
                    break;
                }
                case CLAIM_INCOME: {
                    WorldData worldData = WorldData.get(player.serverLevel());
                    BlockPos corePos = worldData.getCorePosition();

                    if (corePos.equals(BlockPos.ZERO) || player.distanceToSqr(corePos.getX(), corePos.getY(), corePos.getZ()) > 100) { // 10 block radius
                        player.sendSystemMessage(Component.literal("§c你必须在权力核心附近才能领取收入。"));
                        return;
                    }

                    UnclaimedIncomeData incomeData = worldData.getUnclaimedIncomeData();
                    double amountToClaim = incomeData.claimIncome(player.getUUID());

                    if (amountToClaim <= 0) {
                        player.sendSystemMessage(Component.literal("§e你没有可领取的离线收入。"));
                        return;
                    }

                    player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                        playerData.addBalance(amountToClaim);
                        player.sendSystemMessage(Component.literal(String.format("§a[✔] 成功领取离线收入 $%.2f。", amountToClaim)));
                        worldData.setDirty();
                        PlayerDataCapability.sync(player);
                    });
                    break;
                }
                // Other cases will be implemented later.
            }
        });
        context.setPacketHandled(true);
    }
}
