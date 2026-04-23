package com.symbioticlaw.contract;

import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

public class ContractManager {

    public static final ContractManager INSTANCE = new ContractManager();

    public void offerContract(ServerPlayer master, ServerPlayer slave) {
        slave.getCapability(PlayerDataCapability.INSTANCE).ifPresent(slaveData -> {
            double redemptionCost = (500 - slaveData.getBalance()) + (slaveData.isWelfareRecipient() ? 500 : 0);

            master.sendSystemMessage(Component.literal("§e[系统] 请求已发送。正在等待目标签署卖身契..."));

            Component message = Component.literal("§e[契约] 玩家 " + master.getGameProfile().getName() + " 欲为您支付 §6$" + redemptionCost + "§e 赎身。 §7您作为奴隶期间获得的所有收入将自动截留 40% 转给债主。若债主离线，系统将暂存收益，待其上线后去核心领取。 ")
                    .append(Component.literal("[§a✔ 点击自愿卖身§r]").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sy contract accept " + master.getUUID().toString()))))
                    .append(Component.literal(" "))
                    .append(Component.literal("[§c✖ 拒绝§r]").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sy contract deny " + master.getUUID().toString()))));

            slave.sendSystemMessage(message);
        });
    }
}
