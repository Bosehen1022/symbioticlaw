package com.symbioticlaw.system;

import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Random;

public class SubliminalSystem {

    private static final Random RANDOM = new Random();

    public enum MessageType {
        PARIAH_SALE("§7\"系统拿走一半，是为了替你保管风险。\"", "§7\"你的劳动很廉价，但至少还是劳动。\""),
        IDLE("§c\"静止即是死亡。\"", "§c\"机器在转动，而你在休息？\""),
        NEAR_CORE("§b\"在这宏伟的造物面前，你如此渺小。\"", "§b\"安全。秩序。服从。\""),
        LOW_BALANCE("§8\"饥饿感是最好的鞭策。\"", "§8\"不要期待施舍。\"");

        private final String[] messages;

        MessageType(String... messages) {
            this.messages = messages;
        }

        public String getRandomMessage() {
            return messages[RANDOM.nextInt(messages.length)];
        }
    }

    public void triggerMessage(ServerPlayer player, MessageType type) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(data -> {
            long currentTime = player.level().getGameTime();
            // Cooldown to prevent spam
            if (currentTime - data.getLastSubliminalMessageTick() < 100) { // 5 seconds
                return;
            }
            data.setLastSubliminalMessageTick(currentTime);
            player.sendSystemMessage(Component.literal(type.getRandomMessage()), true);
        });
    }

    public void tick(ServerPlayer player, com.symbioticlaw.capability.IPlayerData playerData) {
        // Idle check
        if (playerData.getIdleTicks() > 1200) { // 60 seconds
            triggerMessage(player, MessageType.IDLE);
            playerData.setIdleTicks(0); // Reset after triggering to avoid spam
        }

        // Near core check
        com.symbioticlaw.data.WorldData worldData = com.symbioticlaw.data.WorldData.get(player.serverLevel());
        if (worldData.getCorePosition().distSqr(player.blockPosition()) < 100) { // 10 blocks radius
            triggerMessage(player, MessageType.NEAR_CORE);
        }

        // Low balance check
        if (playerData.getBalance() < 100 && !playerData.isWelfareRecipient()) {
            triggerMessage(player, MessageType.LOW_BALANCE);
        }
    }
}
