package com.symbioticlaw.gui.hud;

import com.symbioticlaw.client.ClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class AlmanacHud {

    public static final IGuiOverlay HUD_ALMANAC = (gui, graphics, partialTicks, width, height) -> {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (!mainHand.is(ItemTags.COMPASSES) && !offHand.is(ItemTags.COMPASSES)) {
            return;
        }

        BlockPos playerPos = player.getOnPos();
        BlockPos corePos = new BlockPos(ClientData.coreX, ClientData.coreY, ClientData.coreZ);
        double distanceToCore = Math.sqrt(playerPos.distSqr(corePos));

        double returnCost = ClientData.returnFee;
        double safetyRadius = ClientData.safetyRadius;
        int statusId = ClientData.almanacStatus;

        String status;
        int statusColor;

        double remaining = safetyRadius - distanceToCore;

        switch (statusId) {
            case 1:
                status = "§e[警告]";
                statusColor = 0xFFAA00;
                break;
            case 2:
                status = "§c[危险]";
                statusColor = 0xFF5555;
                break;
            default:
                status = "§a[安全]";
                statusColor = 0x55FF55;
                break;
        }

        String line1 = String.format("位置: X:%d Z:%d (%s)", playerPos.getX(), playerPos.getZ(), getTierName(distanceToCore));
        String line2 = String.format("回程票价: $%.1f", returnCost);
        String line3 = String.format("安全半径: %.0f格 (剩余 %.0f格)", safetyRadius, remaining);
        String line4 = "状态: " + status;

        int y = 10;
        graphics.drawString(Minecraft.getInstance().font, line1, 10, y, 0xFFFFFF);
        graphics.drawString(Minecraft.getInstance().font, line2, 10, y += 12, 0xFFFFFF);
        graphics.drawString(Minecraft.getInstance().font, line3, 10, y += 12, 0xFFFFFF);
        graphics.drawString(Minecraft.getInstance().font, line4, 10, y += 12, statusColor);
    };

    private static String getTierName(double distance) {
        if (distance > 5000) {
            return "第三环 (蛮荒区)";
        } else if (distance > 2000) {
            return "第二环 (中产区)";
        } else {
            return "第一环 (核心区)";
        }
    }
}
