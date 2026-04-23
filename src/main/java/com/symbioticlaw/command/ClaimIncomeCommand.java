package com.symbioticlaw.command;

import com.mojang.brigadier.CommandDispatcher;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.UnclaimedIncomeData;
import com.symbioticlaw.data.WorldData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ClaimIncomeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sl")
                .then(Commands.literal("claimincome")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            WorldData worldData = WorldData.get(player.serverLevel());
                            BlockPos corePos = worldData.getCorePosition();

                            if (corePos.equals(BlockPos.ZERO) || player.distanceToSqr(corePos.getX(), corePos.getY(), corePos.getZ()) > 100) { // 10 block radius
                                player.sendSystemMessage(Component.literal("§c你必须在权力核心附近才能领取收入。"));
                                return 0;
                            }

                            UnclaimedIncomeData incomeData = worldData.getUnclaimedIncomeData();
                            double amountToClaim = incomeData.claimIncome(player.getUUID());

                            if (amountToClaim <= 0) {
                                player.sendSystemMessage(Component.literal("§e你没有可领取的离线收入。"));
                                return 0;
                            }

                            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                                playerData.addBalance(amountToClaim);
                                player.sendSystemMessage(Component.literal(String.format("§a[✔] 成功领取离线收入 $%.2f。", amountToClaim)));
                                worldData.setDirty();
                            });

                            return 1;
                        })
                )
        );
    }
}
