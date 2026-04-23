package com.symbioticlaw.gui.screen;

import com.symbioticlaw.network.NetworkHandler;
import com.symbioticlaw.network.ServerBoundReleaseSlavePacket;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.symbioticlaw.network.ClientBoundSlavesPacket;

public class ReleaseSlaveScreen extends Screen {

    public final Screen lastScreen;
    private PlayerSelectionList playerList;
    private Button releaseButton;
    private UUID selectedPlayerUUID;
    private final List<ClientBoundSlavesPacket.SlaveInfo> slaves;

    public ReleaseSlaveScreen(Screen lastScreen, List<ClientBoundSlavesPacket.SlaveInfo> slaves) {
        super(Component.literal("释放奴隶"));
        this.lastScreen = lastScreen;
        this.slaves = slaves;
    }

    public void updateSlaves(java.util.List<ClientBoundSlavesPacket.SlaveInfo> slaves) {
        this.playerList.children().clear();
        this.playerList.addEntries(slaves);
    }

    @Override
    protected void init() {
        super.init();

        this.playerList = new PlayerSelectionList(this.width, this.height, 32, this.height - 64);
        this.playerList.addEntries(this.slaves);
        this.addWidget(this.playerList);

        this.releaseButton = this.addRenderableWidget(Button.builder(Component.literal("释放"), button -> {
            if (this.selectedPlayerUUID != null) {
                NetworkHandler.INSTANCE.sendToServer(new ServerBoundReleaseSlavePacket(this.selectedPlayerUUID));
                this.onClose();
            }
        }).bounds(this.width / 2 - 154, this.height - 52, 150, 20).build());
        this.releaseButton.active = false;

        this.addRenderableWidget(Button.builder(Component.literal("取消"), button -> this.onClose())
                .bounds(this.width / 2 + 4, this.height - 52, 150, 20).build());
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.playerList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    private class PlayerSelectionList extends ObjectSelectionList<PlayerSelectionList.Entry> {

        public PlayerSelectionList(int width, int height, int y0, int y1) {
            super(ReleaseSlaveScreen.this.minecraft, width, height, y0, y1, 24);
        }

        public void addEntries(List<ClientBoundSlavesPacket.SlaveInfo> slaves) {
            for (ClientBoundSlavesPacket.SlaveInfo slave : slaves) {
                this.addEntry(new Entry(new GameProfile(slave.uuid(), slave.name())));
            }
        }

        @Override
        public void setSelected(Entry entry) {
            super.setSelected(entry);
            if (entry != null) {
                ReleaseSlaveScreen.this.selectedPlayerUUID = entry.profile.getId();
                ReleaseSlaveScreen.this.releaseButton.active = true;
            } else {
                ReleaseSlaveScreen.this.selectedPlayerUUID = null;
                ReleaseSlaveScreen.this.releaseButton.active = false;
            }
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private final GameProfile profile;

            public Entry(GameProfile profile) {
                this.profile = profile;
            }

            @Nonnull
            @Override
            public Component getNarration() {
                return Component.literal(this.profile.getName());
            }

            @Override
            public void render(@Nonnull GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
                pGuiGraphics.drawString(minecraft.font, this.profile.getName(), pLeft + 4, pTop + (pHeight - minecraft.font.lineHeight) / 2, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
                this.select();
                return true;
            }

            private void select() {
                PlayerSelectionList.this.setSelected(this);
            }
        }
    }
}
