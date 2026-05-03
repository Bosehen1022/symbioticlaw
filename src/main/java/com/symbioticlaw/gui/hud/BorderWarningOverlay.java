package com.symbioticlaw.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class BorderWarningOverlay {
    private static final ResourceLocation VIGNETTE_RED = new ResourceLocation("symbioticlaw", "textures/gui/vignette_red.png");
    private static boolean show = false;
    private static int countdown = 0;

    public static final IGuiOverlay HUD_BORDER_WARNING = (gui, poseStack, partialTicks, width, height) -> {
        if (!show) return;

        float alpha = (float) (Math.sin(Minecraft.getInstance().level.getGameTime() / 10.0) * 0.2 + 0.6);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        poseStack.blit(VIGNETTE_RED, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    };

    public static void setShow(boolean show, int countdown) {
        BorderWarningOverlay.show = show;
        BorderWarningOverlay.countdown = countdown;
    }
}
