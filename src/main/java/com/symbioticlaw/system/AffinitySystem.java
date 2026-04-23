package com.symbioticlaw.system;

import com.symbioticlaw.contract.Contract;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.contract.ContractType;
import com.symbioticlaw.data.ContractData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;
import java.util.Optional;
import java.util.stream.Stream;

public class AffinitySystem {

    public void initiateBond(ServerPlayer initiator, ServerPlayer target) {
        ContractData contractData = ContractData.get(initiator.serverLevel());
        boolean alreadyInBond = contractData.getContracts().values().stream()
                .anyMatch(c -> c.type == ContractType.BOND && (c.issuerId.equals(initiator.getUUID()) || c.acceptorId.equals(initiator.getUUID())));
        if (alreadyInBond) {
            initiator.sendSystemMessage(Component.literal("§c你已经有共生伙伴了。"));
            return;
        }
        boolean targetAlreadyInBond = contractData.getContracts().values().stream()
                .anyMatch(c -> c.type == ContractType.BOND && (c.issuerId.equals(target.getUUID()) || c.acceptorId.equals(target.getUUID())));
        if (targetAlreadyInBond) {
            initiator.sendSystemMessage(Component.literal("§c对方已经有共生伙伴了。"));
            return;
        }

        Component message = Component.literal("§b[☍] 收到来自 " + initiator.getGameProfile().getName() + " 的共生协议请求。 §7条款：里程税减免 20% | 义务：保持 30格 物理距离。")
                .append(Component.literal(" [§a✔ 确认缔结]")
                        .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sy bond accept " + initiator.getUUID()))))
                .append(" ")
                .append(Component.literal("[§c✖ 拒绝请求]")
                        .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sy bond deny " + initiator.getUUID()))));
        target.sendSystemMessage(message);

        initiator.sendSystemMessage(Component.literal("§e[系统] 正在尝试与 " + target.getGameProfile().getName() + " 建立链接... 请求已发送。"));
    }

    public void finalizeBond(ServerPlayer initiator, ServerPlayer target) {
        initiator.getCapability(PlayerDataCapability.INSTANCE).ifPresent(initiatorData -> {
            target.getCapability(PlayerDataCapability.INSTANCE).ifPresent(targetData -> {
                long threeDaysInTicks = 3 * 24 * 60 * 60 * 20;
                Contract bond = new Contract(initiator.getUUID(), initiator.getGameProfile().getName(), target.getUUID(), target.getGameProfile().getName(), threeDaysInTicks);
                ContractData.get(initiator.serverLevel()).addContract(bond);

                initiatorData.setAffinityPartnerUUID(target.getUUID());
                initiatorData.setAffinityPartnerName(target.getGameProfile().getName());
                targetData.setAffinityPartnerUUID(initiator.getUUID());
                targetData.setAffinityPartnerName(initiator.getGameProfile().getName());
            });
        });

        Component message = Component.literal("§b[☍] 协议生效！您与 " + target.getGameProfile().getName() + " 正式确认为 [共生伙伴]。 §7初始同步率: 150。警告：距离产生不了美，只会产生税。请保持形影不离。");
        initiator.sendSystemMessage(message);
        target.sendSystemMessage(Component.literal("§b[☍] 协议生效！您与 " + initiator.getGameProfile().getName() + " 正式确认为 [共生伙伴]。 §7初始同步率: 150。警告：距离产生不了美，只会产生税。请保持形影不离。"));
    }

    public void denyBond(ServerPlayer initiator, ServerPlayer target) {
        initiator.sendSystemMessage(Component.literal("§c[✖] 链接建立失败：目标拒绝了协议。"));
        target.sendSystemMessage(Component.literal("§7[系统] 您拒绝了共生协议请求。"));
    }

    public void tick(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        if (overworld == null) return;

        ContractData contractData = ContractData.get(overworld);
        java.util.List<Contract> toRemove = new java.util.ArrayList<>();
        java.util.Map<Contract, String> reasons = new java.util.HashMap<>();

        contractData.getContracts().values().stream()
                .filter(c -> c.type == ContractType.BOND)
                .forEach(contract -> {
                    if (contract.isExpired()) {
                        toRemove.add(contract);
                        reasons.put(contract, "契约到期");
                        return;
                    }

                    ServerPlayer player1 = server.getPlayerList().getPlayer(contract.issuerId);
                    ServerPlayer player2 = server.getPlayerList().getPlayer(contract.acceptorId);

                    boolean inRange = player1 != null && player2 != null && 
                                      player1.level().dimension() == player2.level().dimension() &&
                                      (player1.distanceToSqr(player2) - Math.pow(player1.getY() - player2.getY(), 2)) < 900; // 30*30

                    if (inRange) {
                        contract.affinity += 1;
                    } else {
                        contract.affinity -= 1;
                    }

                    if (contract.affinity < 100) {
                        toRemove.add(contract);
                        reasons.put(contract, "长期物理分离");
                        return;
                    }
                    contractData.setDirty();
                });
        
        for (Contract contract : toRemove) {
            breakBond(server, contractData, contract, reasons.get(contract));
        }
    }

    private void breakBond(MinecraftServer server, ContractData contractData, Contract contract, String reason) {
        contractData.removeContract(contract.contractId);
        
        ServerPlayer player1 = server.getPlayerList().getPlayer(contract.issuerId);
        ServerPlayer player2 = server.getPlayerList().getPlayer(contract.acceptorId);
        String p1Name = player1 != null ? player1.getGameProfile().getName() : contract.issuerName;
        String p2Name = player2 != null ? player2.getGameProfile().getName() : contract.acceptorName;

        Component message1 = Component.literal("§c[⚠] 警告：由于" + reason + "，您与 " + p2Name + " 的共生链接已强制断开。");
        if (player1 != null) {
            player1.sendSystemMessage(message1);
        }

        Component message2 = Component.literal("§c[⚠] 警告：由于" + reason + "，您与 " + p1Name + " 的共生链接已强制断开。");
        if (player2 != null) {
            player2.sendSystemMessage(message2);
        }
    }

    public void renewBond(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ContractData contractData = ContractData.get(level);
        Optional<Contract> bond = contractData.getContracts().values().stream()
                .filter(c -> c.type == ContractType.BOND && (c.issuerId.equals(player.getUUID()) || c.acceptorId.equals(player.getUUID())))
                .findFirst();

        if (bond.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c你没有共生伙伴。"));
            return;
        }

        Contract contract = bond.get();
        long twoHoursInMillis = 2 * 60 * 60 * 1000;

        if (contract.expiryTime - System.currentTimeMillis() < twoHoursInMillis) {
            long threeDaysInMillis = 3L * 24 * 60 * 60 * 1000;
            contract.expiryTime += threeDaysInMillis;
            contractData.setDirty();

            player.sendSystemMessage(Component.literal("§b共生协议已续签。"));

            UUID partnerUUID = contract.issuerId.equals(player.getUUID()) ? contract.acceptorId : contract.issuerId;
            ServerPlayer partner = player.getServer().getPlayerList().getPlayer(partnerUUID);
            if (partner != null) {
                 partner.sendSystemMessage(Component.literal("§b你的共生伙伴已续签协议。"));
            }
        } else {
            player.sendSystemMessage(Component.literal("§c还未到续签时间窗口。"));
        }
    }
}
