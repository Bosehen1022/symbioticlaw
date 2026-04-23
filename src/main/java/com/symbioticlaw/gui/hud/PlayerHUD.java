package com.symbioticlaw.gui.hud;

import com.symbioticlaw.capability.PlayerDataCapability;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import com.symbioticlaw.capability.IPlayerData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;

public class PlayerHUD {

    private static long lastActionTime = 0;
    private static long lastMessageTime = 0;

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        mc.player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            RenderSystem.enableBlend();
            int yPos = 10;

            // Balance
            String balanceText = String.format("余额: $%.2f", playerData.getBalance());
            guiGraphics.drawString(mc.font, balanceText, 10, yPos, 0xFFFFFF, true);
            yPos += 12;

            // Class Tier
            String classText = "阶级: " + getClassTierName(playerData.getClassTier());
            guiGraphics.drawString(mc.font, classText, 10, yPos, getClassTierColor(playerData.getClassTier()), true);
            yPos += 12;

            // Profession
            String professionText = "职业: " + playerData.getProfession().getDisplayName().getString();
            guiGraphics.drawString(mc.font, professionText, 10, yPos, 0xFFFFFF, true);
            yPos += 12;

            // Profession XP Bar
            if (playerData.getProfession() != com.symbioticlaw.professions.Profession.UNEMPLOYED) {
                int level = playerData.getProfessionLevel(playerData.getProfession());
                double currentXp = playerData.getProfessionXp(playerData.getProfession());
                long xpForNextLevel = com.symbioticlaw.system.ProfessionSystem.getXpForLevel(playerData.getProfession().toJobType(), level + 1);

                int barWidth = 100;
                int progress = (int) ((currentXp / xpForNextLevel) * barWidth);
                String progressBar = "[" + "|".repeat(progress) + ".".repeat(barWidth - progress) + "]";

                String xpText = String.format("%s Lv.%d (%.1f / %d)", progressBar, level, currentXp, xpForNextLevel);
                guiGraphics.drawString(mc.font, xpText, 10, yPos, 0x00FF00, true);
                yPos += 12;
            }

            long currentTime = mc.player.level().getGameTime();

            // Survival Pass Timer
            if (playerData.getSurvivalPassExpireTime() > currentTime) {
                long remainingTicks = playerData.getSurvivalPassExpireTime() - currentTime;
                String passTimer = "生存假票: " + formatTime(remainingTicks);
                guiGraphics.drawString(mc.font, passTimer, 10, yPos, 0xFFFF55, true);
                yPos += 12;
            }

            // Visa Timer
            if (playerData.getVisaExpireTime() > currentTime) {
                long remainingTicks = playerData.getVisaExpireTime() - currentTime;
                String visaTimer = "商务签证: " + formatTime(remainingTicks);
                guiGraphics.drawString(mc.font, visaTimer, 10, yPos, 0x55FFFF, true);
                yPos += 12;
            }

            // Explorer's Almanac
            if (mc.player.getMainHandItem().is(Items.COMPASS) || mc.player.getOffhandItem().is(Items.COMPASS)) {
                yPos += 12; // Add some space
                guiGraphics.drawString(mc.font, "--- 探险家罗盘 ---", 10, yPos, 0xFFFF55, true);
                yPos += 12;

                BlockPos playerPos = mc.player.blockPosition();
                // Placeholder values for now
                guiGraphics.drawString(mc.font, String.format("位置: X:%d Z:%d (第一环)", playerPos.getX(), playerPos.getZ()), 10, yPos, 0xFFFFFF, true);
                yPos += 12;
                guiGraphics.drawString(mc.font, "回程票价: $140.00", 10, yPos, 0xFFFFFF, true);
                yPos += 12;
                guiGraphics.drawString(mc.font, "安全半径: 2000格", 10, yPos, 0xFFFFFF, true);
                yPos += 12;
                guiGraphics.drawString(mc.font, "状态: §a[安全]", 10, yPos, 0xFFFFFF, true);
            }

            // Subliminal messaging
            handleSubliminalMessages(guiGraphics, playerData, mc);
        });
    }

    private static void handleSubliminalMessages(GuiGraphics guiGraphics, IPlayerData playerData, Minecraft mc) {
        if (mc.player == null) return;
        long currentTime = mc.player.level().getGameTime();
        if (lastActionTime == 0) lastActionTime = currentTime;
        if (lastMessageTime == 0) lastMessageTime = currentTime;

        //if (mc.mouseHandler.isLeftPressed() || mc.mouseHandler.isRightPressed() || mc.keyboardHandler.isAnyPressed()) {
            lastActionTime = currentTime;
        //}

        if (currentTime - lastMessageTime > 200) { // Don't spam messages
            String message = null;
            if (playerData.getClassTier() == 0 && playerData.getBalance() < 100) {
                message = "§8饥饿感是最好的鞭策。";
            } else if (currentTime - lastActionTime > 1200) { // Idle for 1 min
                message = "§c静止即是死亡。";
            }

            if (message != null) {
                int x = (mc.getWindow().getGuiScaledWidth() - mc.font.width(message)) / 2;
                guiGraphics.drawString(mc.font, message, x, 40, 0xFFFFFF, true);
                lastMessageTime = currentTime;
            }
        }
    }

    private static String getClassTierName(int tier) {
        return switch (tier) {
            case 0 -> "遗民";
            case 1 -> "限制公民";
            case 2 -> "一等公民";
            default -> "未知";
        };
    }

    private static int getClassTierColor(int tier) {
        return switch (tier) {
            case 0 -> 0xFF5555; // Red
            case 1 -> 0xFFAA00; // Orange
            case 2 -> 0x55FF55; // Green
            default -> 0xFFFFFF; // White
        };
    }

    private static String formatTime(long ticks) {
        long totalSeconds = ticks / 20;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}