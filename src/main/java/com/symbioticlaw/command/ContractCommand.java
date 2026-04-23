package com.symbioticlaw.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.symbioticlaw.professions.Profession;
import com.symbioticlaw.system.P2PSystem;
import com.symbioticlaw.system.ProfessionSystem;
import com.symbioticlaw.system.SlaveSystem;
import com.symbioticlaw.data.WorldData;
import com.symbioticlaw.data.UnclaimedIncomeData;
import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class ContractCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pay")
                .requires(s -> s.hasPermission(0))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                .executes(context -> {
                                    ServerPlayer sender = context.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                    double amount = DoubleArgumentType.getDouble(context, "amount");
                                    new P2PSystem().transfer(sender, target, amount);
                                    return 1;
                                })))
        );

        dispatcher.register(Commands.literal("sy")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("contract")
                        .then(Commands.literal("initiate")
                                .then(Commands.argument("slave", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer owner = context.getSource().getPlayerOrException();
                                            ServerPlayer slave = EntityArgument.getPlayer(context, "slave");
                                            new SlaveSystem().initiateContract(owner, slave);
                                            return 1;
                                        })))
                        .then(Commands.literal("accept")
                                .then(Commands.argument("ownerUUID", StringArgumentType.string())
                                        .executes(context -> {
                                            ServerPlayer slave = context.getSource().getPlayerOrException();
                                            UUID ownerUUID = UUID.fromString(StringArgumentType.getString(context, "ownerUUID"));
                                            ServerPlayer owner = context.getSource().getServer().getPlayerList().getPlayer(ownerUUID);
                                            if (owner != null) {
                                                new SlaveSystem().finalizeContract(owner, slave);
                                            } else {
                                                slave.sendSystemMessage(Component.literal("§c错误：找不到契约发起人。"));
                                            }
                                            return 1;
                                        })))
                        .then(Commands.literal("deny")
                                .then(Commands.argument("ownerUUID", StringArgumentType.string())
                                        .executes(context -> {
                                            ServerPlayer slave = context.getSource().getPlayerOrException();
                                            UUID ownerUUID = UUID.fromString(StringArgumentType.getString(context, "ownerUUID"));
                                            ServerPlayer owner = context.getSource().getServer().getPlayerList().getPlayer(ownerUUID);
                                            if (owner != null) {
                                                new SlaveSystem().denyContract(owner, slave);
                                            } else {
                                                slave.sendSystemMessage(Component.literal("§c错误：找不到契约发起人。"));
                                            }
                                            return 1;
                                        })))
                        .then(Commands.literal("release")
                                .then(Commands.argument("slave", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer owner = context.getSource().getPlayerOrException();
                                            ServerPlayer slave = EntityArgument.getPlayer(context, "slave");
                                            new SlaveSystem().releaseSlave(owner, slave);
                                            return 1;
                                        })))
                )
                .then(Commands.literal("bond")
                        .then(Commands.literal("initiate")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer initiator = context.getSource().getPlayerOrException();
                                            ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                            new com.symbioticlaw.system.AffinitySystem().initiateBond(initiator, target);
                                            return 1;
                                        })))
                        .then(Commands.literal("accept")
                                .then(Commands.argument("initiatorUUID", StringArgumentType.string())
                                        .executes(context -> {
                                            ServerPlayer target = context.getSource().getPlayerOrException();
                                            UUID initiatorUUID = UUID.fromString(StringArgumentType.getString(context, "initiatorUUID"));
                                            ServerPlayer initiator = context.getSource().getServer().getPlayerList().getPlayer(initiatorUUID);
                                            if (initiator != null) {
                                                new com.symbioticlaw.system.AffinitySystem().finalizeBond(initiator, target);
                                            } else {
                                                target.sendSystemMessage(Component.literal("§c错误：找不到契约发起人。"));
                                            }
                                            return 1;
                                        })))
                        .then(Commands.literal("deny")
                                .then(Commands.argument("initiatorUUID", StringArgumentType.string())
                                        .executes(context -> {
                                            ServerPlayer target = context.getSource().getPlayerOrException();
                                            UUID initiatorUUID = UUID.fromString(StringArgumentType.getString(context, "initiatorUUID"));
                                            ServerPlayer initiator = context.getSource().getServer().getPlayerList().getPlayer(initiatorUUID);
                                            if (initiator != null) {
                                                new com.symbioticlaw.system.AffinitySystem().denyBond(initiator, target);
                                            } else {
                                                target.sendSystemMessage(Component.literal("§c错误：找不到契约发起人。"));
                                            }
                                            return 1;
                                        })))
                )
                .then(Commands.literal("job")
//                        .then(Commands.literal("join")
//                                .then(Commands.argument("profession", StringArgumentType.string())
//                                        .executes(context -> {
//                                            ServerPlayer player = context.getSource().getPlayerOrException();
//                                            String profName = StringArgumentType.getString(context, "profession");
//                                            try {
//                                                Profession prof = Profession.valueOf(profName.toUpperCase());
//                                                new ProfessionSystem().joinProfession(player, prof);
//                                            } catch (IllegalArgumentException e) {
//                                                player.sendSystemMessage(Component.literal("§c无效的职业。"));
//                                            }
//                                            return 1;
//                                        })))
//                        .then(Commands.literal("initiate")
//                                .then(Commands.argument("profession", StringArgumentType.string())
//                                        .executes(context -> {
//                                            ServerPlayer player = context.getSource().getPlayerOrException();
//                                            String profName = StringArgumentType.getString(context, "profession");
//                                            try {
//                                                Profession prof = Profession.valueOf(profName.toUpperCase());
//                                                new ProfessionSystem().initiateJoinProfession(player, prof);
//                                            } catch (IllegalArgumentException e) {
//                                                player.sendSystemMessage(Component.literal("§c无效的职业。"));
//                                            }
//                                            return 1;
//                                        })))
//                        .then(Commands.literal("resign")
//                                .executes(context -> {
//                                    ServerPlayer player = context.getSource().getPlayerOrException();
//                                    // This is a destructive action, should have a confirmation, but for now...
//                                    new ProfessionSystem().resign(player);
//                                    return 1;
//                                }))
//                        .then(Commands.literal("payfee")
//                                .then(Commands.argument("slaveUUID", StringArgumentType.string())
//                                        .executes(context -> {
//                                            ServerPlayer owner = context.getSource().getPlayerOrException();
//                                            UUID slaveUUID = UUID.fromString(StringArgumentType.getString(context, "slaveUUID"));
//                                            ServerPlayer slave = context.getSource().getServer().getPlayerList().getPlayer(slaveUUID);
//                                            if (slave != null) {
//                                                new ProfessionSystem().payAndResignForSlave(owner, slave);
//                                            } else {
//                                                owner.sendSystemMessage(Component.literal("§c错误：找不到该奴隶。"));
//                                            }
//                                            return 1;
//                                        })))
                )
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
                        }))
        );
    }
}
