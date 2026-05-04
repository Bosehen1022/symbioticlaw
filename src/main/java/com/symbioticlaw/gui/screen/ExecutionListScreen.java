package com.symbioticlaw.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ExecutionListScreen extends Screen {
    private final Screen parent;
    private final List<String> executionList = new ArrayList<>();

    public ExecutionListScreen(Screen parent, List<String> executionList) {
        super(Component.literal("处决名单"));
        this.parent = parent;
        this.executionList.addAll(executionList);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        int y = 40;
        for (String playerName : executionList) {
            pGuiGraphics.drawString(this.font, playerName, this.width / 2 - 50, y, 0xFF0000);
            y += 12;
        }

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
