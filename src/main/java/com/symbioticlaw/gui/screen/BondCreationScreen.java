package com.symbioticlaw.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.symbioticlaw.network.NetworkHandler;
import com.symbioticlaw.network.ServerBoundBondCreatePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BondCreationScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("symbioticlaw:textures/gui/bond_creation_gui.png");

    private final Screen parentScreen;
    private EditBox partnerNameBox;

    protected BondCreationScreen(Screen parentScreen) {
        super(Component.literal("缔结共生协议"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.partnerNameBox = new EditBox(this.font, centerX - 75, centerY - 35, 150, 20, Component.literal(""));
        this.partnerNameBox.setHint(Component.literal("输入玩家名称"));
        this.partnerNameBox.setMaxLength(16);
        this.addRenderableWidget(this.partnerNameBox);

        this.addRenderableWidget(Button.builder(
            Component.literal("✔ 确认缔结"),
            btn -> {
                String partner = partnerNameBox.getValue().trim();
                if (!partner.isEmpty()) {
                    NetworkHandler.INSTANCE.sendToServer(new ServerBoundBondCreatePacket(partner));
                    this.onClose();
                }
            }
        ).bounds(centerX - 75, centerY + 35, 70, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("✖ 取消"),
            btn -> this.onClose()
        ).bounds(centerX + 5, centerY + 35, 70, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - 200) / 2;
        int y = (this.height - 160) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, 200, 160);

        super.render(graphics, mouseX, mouseY, partialTick);

        int textX = this.width / 2;
        int currentY = y + 15;

        graphics.drawCenteredString(this.font, Component.literal("§e§l共生协议"), textX, currentY, 0xFFFFFF);
        currentY += 20;

        graphics.drawCenteredString(this.font, Component.literal("§7═══ 协议条款 ═══"), textX, currentY, 0xAAAAAA);
        currentY += 18;

        graphics.drawString(this.font, "§7├ 里程税减免: §a20%", x + 15, currentY, 0xAAAAAA, false);
        currentY += 14;

        graphics.drawString(this.font, "§7├ 物理距离要求: §e30格", x + 15, currentY, 0xAAAAAA, false);
        currentY += 14;

        graphics.drawString(this.font, "§7├ 协议有效期: §c3天", x + 15, currentY, 0xAAAAAA, false);
        currentY += 14;

        graphics.drawString(this.font, "§7├ 同步衰减: §e-1/分钟", x + 15, currentY, 0xAAAAAA, false);
        currentY += 14;

        graphics.drawString(this.font, "§7└ 被动收入上限: §c$100/日", x + 15, currentY, 0xAAAAAA, false);
        currentY += 20;

        graphics.drawCenteredString(this.font, Component.literal("§c⚠ 解除需双方同意"), textX, currentY, 0xFF6666);
        currentY += 15;

        graphics.drawString(this.font, "§7输入伙伴名称:", x + 15, currentY, 0xFFFFFF, false);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parentScreen);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (partnerNameBox != null) partnerNameBox.tick();
    }
}