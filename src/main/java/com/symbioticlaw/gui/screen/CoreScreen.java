package com.symbioticlaw.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.client.ClientPlayerData;
import com.symbioticlaw.data.JobType;
import com.symbioticlaw.gui.menu.PowerCoreMenu;
import com.symbioticlaw.network.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CoreScreen extends AbstractContainerScreen<PowerCoreMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Symbioticlaw.MODID, "textures/gui/power_core_gui.png");
    private static final ResourceLocation PARIAH_STAMP = new ResourceLocation(Symbioticlaw.MODID, "textures/gui/pariah_stamp.png");
    private static final ResourceLocation RESTRICTED_STAMP = new ResourceLocation(Symbioticlaw.MODID, "textures/gui/restricted_stamp.png");
    private static final ResourceLocation FREE_STAMP = new ResourceLocation(Symbioticlaw.MODID, "textures/gui/free_stamp.png");

    private Tab currentTab = Tab.PROFILE;

    private int selectedProfessionId = -1;
    private boolean showCareerConfirmDialog = false;
    private String careerConfirmMessage = "";

    private int selectedPariahIndex = -1;
    private int selectedSlaveIndex = -1;
    private List<ClientBoundPariahListPacket.PariahInfo> pariahList = new ArrayList<>();
    private List<ClientBoundSlaveListPacket.SlaveInfo> slaveList = new ArrayList<>();
    private int lastPariahListVersion = -1;
    private int lastSlaveListVersion = -1;

    private EditBox bondPartnerBox;

    private int lastPlayerDataVersion = -1;

    public CoreScreen(PowerCoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 220;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.currentTab = Tab.PROFILE;
        rebuildWidgets();
    }

    private void switchTab(Tab newTab) {
        if (this.currentTab == newTab) return;

        this.currentTab = newTab;
        this.selectedPariahIndex = -1;
        this.selectedSlaveIndex = -1;
        this.showCareerConfirmDialog = false;

        if (newTab == Tab.CONTRACTS) {
            NetworkHandler.INSTANCE.sendToServer(new ServerBoundRequestNearbyPariahsPacket());
            NetworkHandler.INSTANCE.sendToServer(new ServerBoundRequestSlaveListPacket());
        }

        rebuildWidgets();
    }

    protected void rebuildWidgets() {
        clearWidgets();
        addTabButtons();

        switch (this.currentTab) {
            case PROFILE -> initProfileTab();
            case CONTRACTS -> initContractsTab();
            case ADMIN -> initAdminTab();
        }
    }

    private void addTabButtons() {
        int tabX = leftPos;
        int tabY = topPos;
        int tabWidth = 58;
        int tabHeight = 18;

        addRenderableWidget(Button.builder(
            Component.literal("档案"),
            btn -> switchTab(Tab.PROFILE)
        ).bounds(tabX, tabY + 3, tabWidth, tabHeight).build());

        if (canAccessContractsTab()) {
            addRenderableWidget(Button.builder(
                Component.literal("契约"),
                btn -> switchTab(Tab.CONTRACTS)
            ).bounds(tabX + tabWidth + 2, tabY + 3, tabWidth, tabHeight).build());
        }

        if (isOp()) {
            addRenderableWidget(Button.builder(
                Component.literal("管理"),
                btn -> switchTab(Tab.ADMIN)
            ).bounds(tabX + (tabWidth + 2) * 2, tabY + 3, tabWidth, tabHeight).build());
        }
    }

    private void initProfileTab() {
        if (minecraft == null || minecraft.player == null) return;

        minecraft.player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            int classTier = playerData.getClassTier();
            int startY = topPos + 30;
            int btnWidth = 160;
            int btnHeight = 16;
            int btnX = leftPos + 8;

            switch (classTier) {
                case -1, 0 -> initPariahButtons(btnX, startY, btnWidth, btnHeight, playerData, classTier);
                case 1 -> initRestrictedButtons(btnX, startY, btnWidth, btnHeight);
                case 2 -> initFreeCitizenButtons();
            }

            initBondSection(topPos + 170, btnWidth, btnHeight, playerData);
        });
    }

    private void initPariahButtons(int btnX, int startY, int btnWidth, int btnHeight, IPlayerData playerData, int classTier) {
        addRenderableWidget(Button.builder(
            Component.literal("领取低保 ($100)"),
            btn -> NetworkHandler.INSTANCE.sendToServer(
                new ServerBoundCareerActionPacket(ServerBoundCareerActionPacket.ActionType.CLAIM_WELFARE))
        ).bounds(btnX, startY, btnWidth, btnHeight)
            .tooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.literal("每日配给 $100。24小时冷却。")))
            .build());

        addRenderableWidget(Button.builder(
            Component.literal("生存假票"),
            btn -> NetworkHandler.INSTANCE.sendToServer(
                new ServerBoundCareerActionPacket(ServerBoundCareerActionPacket.ActionType.REQUEST_SURVIVAL_PASS))
        ).bounds(btnX, startY + 20, btnWidth, btnHeight)
            .tooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.literal("10分钟出城权。24小时冷却。")))
            .build());

        boolean canRepair = playerData.getBalance() >= 700;
        Button repairBtn = Button.builder(
            Component.literal("信用修复 ($500)"),
            btn -> NetworkHandler.INSTANCE.sendToServer(
                new ServerBoundCoreActionPacket(ServerBoundCoreActionPacket.ActionType.PAY_CREDIT_RECOVERY))
        ).bounds(btnX, startY + 40, btnWidth, btnHeight)
            .tooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.literal("支付$500恢复为受限公民，保留余额。需要当前余额≥$700。")))
            .build();
        repairBtn.active = canRepair;
        addRenderableWidget(repairBtn);
    }

    private void initRestrictedButtons(int btnX, int startY, int btnWidth, int btnHeight) {
        addRenderableWidget(Button.builder(
            Component.literal("商务签证 ($50/小时)"),
            btn -> NetworkHandler.INSTANCE.sendToServer(
                new ServerBoundCoreActionPacket(ServerBoundCoreActionPacket.ActionType.PURCHASE_BUSINESS_VISA))
        ).bounds(btnX, startY, btnWidth, btnHeight)
            .tooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.literal("解除200格限制，持续1小时。")))
            .build());
    }

    private void initFreeCitizenButtons() {
        int startY = topPos + 30;
        int btnX = leftPos + 8;
        int btnWidth = 160;

        this.bondPartnerBox = new EditBox(this.font, btnX, startY, 100, 14, Component.literal(""));
        this.bondPartnerBox.setHint(Component.literal("玩家名"));
        this.bondPartnerBox.setMaxLength(16);
        addRenderableWidget(this.bondPartnerBox);

        addRenderableWidget(Button.builder(
            Component.literal("转账"),
            btn -> {
                String target = bondPartnerBox.getValue().trim();
                if (!target.isEmpty()) {
                }
            }
        ).bounds(btnX + 105, startY, 55, 14).build());
    }

    private void initBondSection(int y, int btnWidth, int btnHeight, IPlayerData playerData) {
        int btnX = leftPos + 8;

        if (playerData.getAffinityPartnerUUID() != null) {
            addRenderableWidget(Button.builder(
                Component.literal("刷新协议"),
                btn -> NetworkHandler.INSTANCE.sendToServer(
                    new ServerBoundCoreActionPacket(ServerBoundCoreActionPacket.ActionType.RENEW_BOND))
            ).bounds(btnX, y, btnWidth, btnHeight).build());
        } else {
            addRenderableWidget(Button.builder(
                Component.literal("☍ 缔结共生"),
                btn -> {
                    if (minecraft != null) {
                        minecraft.setScreen(new BondCreationScreen(this));
                    }
                }
            ).bounds(btnX, y, btnWidth, btnHeight).build());
        }
    }

    private void initContractsTab() {
        addRenderableWidget(Button.builder(
            Component.literal("刷新"),
            btn -> {
                NetworkHandler.INSTANCE.sendToServer(new ServerBoundRequestNearbyPariahsPacket());
                NetworkHandler.INSTANCE.sendToServer(new ServerBoundRequestSlaveListPacket());
            }
        ).bounds(leftPos + 130, topPos + 25, 38, 14).build());

        addRenderableWidget(Button.builder(
            Component.literal("签约"),
            btn -> {
                if (selectedPariahIndex >= 0 && selectedPariahIndex < pariahList.size()) {
                    UUID pariahUUID = pariahList.get(selectedPariahIndex).getUuid();
                    NetworkHandler.INSTANCE.sendToServer(new ServerBoundSignContractPacket(pariahUUID));
                }
            }
        ).bounds(leftPos + 130, topPos + 80, 38, 16).build());

        addRenderableWidget(Button.builder(
            Component.literal("提取"),
            btn -> NetworkHandler.INSTANCE.sendToServer(
                new ServerBoundCoreActionPacket(ServerBoundCoreActionPacket.ActionType.CLAIM_INCOME))
        ).bounds(leftPos + 130, topPos + 145, 38, 16).build());

        addRenderableWidget(Button.builder(
            Component.literal("释放"),
            btn -> {
                if (selectedSlaveIndex >= 0 && selectedSlaveIndex < slaveList.size()) {
                    UUID slaveUUID = slaveList.get(selectedSlaveIndex).getUuid();
                    NetworkHandler.INSTANCE.sendToServer(new ServerBoundReleaseSlavePacket(slaveUUID));
                }
            }
        ).bounds(leftPos + 130, topPos + 165, 38, 16).build());
    }

    private void initAdminTab() {
        int btnWidth = 160;
        int btnHeight = 18;
        int btnX = leftPos + 8;
        int currentY = topPos + 30;

        addRenderableWidget(Button.builder(
            Component.literal("📍 锚点迁移"),
            btn -> NetworkHandler.INSTANCE.sendToServer(
                new ServerBoundAdminActionPacket(ServerBoundAdminActionPacket.ActionType.RESET_CORE_POSITION))
        ).bounds(btnX, currentY, btnWidth, btnHeight).build());
        currentY += btnHeight + 4;

        addRenderableWidget(Button.builder(
            Component.literal("📢 强制日报"),
            btn -> NetworkHandler.INSTANCE.sendToServer(
                new ServerBoundAdminActionPacket(ServerBoundAdminActionPacket.ActionType.FORCE_BROADCAST))
        ).bounds(btnX, currentY, btnWidth, btnHeight).build());
        currentY += btnHeight + 4;

        addRenderableWidget(Button.builder(
            Component.literal("💀 处决名单"),
            btn -> NetworkHandler.INSTANCE.sendToServer(new ServerBoundRequestExecutionListPacket())
        ).bounds(btnX, currentY, btnWidth, btnHeight).build());
    }

    @Override
    public void containerTick() {
        super.containerTick();
        updateClientData();
        if (bondPartnerBox != null) bondPartnerBox.tick();
    }

    private void updateClientData() {
        if (currentTab == Tab.CONTRACTS) {
            int newPariahVersion = ClientPlayerData.getPariahListVersion();
            if (newPariahVersion != lastPariahListVersion) {
                lastPariahListVersion = newPariahVersion;
                pariahList = ClientPlayerData.getPariahList();
            }

            int newSlaveVersion = ClientPlayerData.getSlaveListVersion();
            if (newSlaveVersion != lastSlaveListVersion) {
                lastSlaveListVersion = newSlaveVersion;
                slaveList = ClientPlayerData.getSlaveList();
            }
        }
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        switch (currentTab) {
            case PROFILE -> renderProfileLabels(graphics);
            case CONTRACTS -> renderContractsLabels(graphics);
            case ADMIN -> renderAdminLabels(graphics);
        }
    }

    private void renderProfileLabels(GuiGraphics g) {
        if (minecraft == null || minecraft.player == null) return;

        minecraft.player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            int infoX = leftPos + 100;
            int infoY = topPos + 28;

            g.drawString(this.font, "§f" + minecraft.player.getGameProfile().getName(), infoX, infoY, 0xFFFFFF, false);
            infoY += 12;
            g.drawString(this.font, "§e$" + String.format("%.1f", playerData.getBalance()), infoX, infoY, 0xFFD700, false);
            infoY += 12;

            net.minecraft.core.BlockPos corePos = this.menu.getCorePos();
            double dist = Math.sqrt(minecraft.player.position().distanceToSqr(corePos.getX() + 0.5, corePos.getY() + 0.5, corePos.getZ() + 0.5));
            String zone = dist > 5000 ? "蛮荒区" : (dist > 2000 ? "中产区" : "核心区");
            g.drawString(this.font, "§7" + zone, infoX, infoY, 0xAAAAAA, false);

            renderClassStamp(g, leftPos + 8, topPos + 25, playerData.getClassTier());

            if (playerData.getClassTier() == 2) {
                g.drawCenteredString(this.font, Component.literal("§o§7财富即自由"), leftPos + imageWidth / 2, topPos + 130, 0xAAAAAA);
                g.drawString(this.font, "§7税率: 25%", leftPos + 8, topPos + 145, 0xFFFFFF, false);
            }

            int bondY = topPos + 160;
            if (playerData.getAffinityPartnerUUID() != null) {
                String partnerName = playerData.getAffinityPartnerName() != null ?
                    playerData.getAffinityPartnerName() : "<Partner>";
                g.drawString(this.font, "§b共生: " + partnerName, leftPos + 8, bondY, 0xFFFFFF, false);
                bondY += 12;
                int affinity = playerData.getAffinityLevel();
                int progressWidth = (int) (150 * Math.min(1.0, affinity / 150.0));
                int color = affinity < 50 ? 0xFF0000 : (affinity < 100 ? 0xFFFF00 : 0x00FF00);
                g.fill(leftPos + 8, bondY, leftPos + 8 + progressWidth, bondY + 4, color);
                g.drawString(this.font, "§7同步: " + affinity + " (0.8x)", leftPos + 8, bondY + 6, 0xAAAAAA, false);
            } else {
                g.drawString(this.font, "§7孤立个体 (1.0x)", leftPos + 8, bondY, 0xFFFFFF, false);
            }
        });
    }

    private void renderClassStamp(GuiGraphics g, int x, int y, int classTier) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        ResourceLocation stamp;
        switch(classTier) {
            case -1, 0 -> stamp = PARIAH_STAMP;
            case 1 -> stamp = RESTRICTED_STAMP;
            case 2 -> stamp = FREE_STAMP;
            default -> stamp = null;
        }

        if (stamp != null) {
            RenderSystem.setShaderTexture(0, stamp);
            g.blit(stamp, x, y, 0, 0, 36, 36, 36, 36);
        }

        RenderSystem.disableBlend();
    }

    private void renderContractsLabels(GuiGraphics g) {
        int listX = leftPos + 8;
        int pariahListY = topPos + 25;
        int slaveListY = topPos + 105;

        g.drawString(this.font, "§e遗民列表", listX, pariahListY, 0xFFFFFF, false);
        pariahListY += 12;

        g.fill(listX - 1, pariahListY - 1, listX + 115, pariahListY + 62, 0xFF333333);

        for (int i = 0; i < Math.min(3, pariahList.size()); i++) {
            var pariah = pariahList.get(i);
            int itemY = pariahListY + i * 20;

            if (i == selectedPariahIndex) {
                g.fill(listX, itemY, listX + 114, itemY + 19, 0xFF555555);
            }

            g.drawString(this.font, "§f" + pariah.getName(), listX + 2, itemY + 2, 0xFFFFFF, false);
            g.drawString(this.font, "§7$" + String.format("%.0f", pariah.getBalance()), listX + 2, itemY + 11, 0xAAAAAA, false);
        }

        if (pariahList.isEmpty()) {
            g.drawString(this.font, "§7无可用遗民", listX + 20, pariahListY + 20, 0xAAAAAA, false);
        }

        g.drawString(this.font, "§e我的奴隶", listX, slaveListY, 0xFFFFFF, false);
        slaveListY += 12;

        g.fill(listX - 1, slaveListY - 1, listX + 115, slaveListY + 62, 0xFF333333);

        double totalPending = 0;
        for (int i = 0; i < Math.min(3, slaveList.size()); i++) {
            var slave = slaveList.get(i);
            int itemY = slaveListY + i * 20;
            totalPending += slave.getPendingTax();

            if (i == selectedSlaveIndex) {
                g.fill(listX, itemY, listX + 114, itemY + 19, 0xFF555555);
            }

            g.drawString(this.font, "§f" + slave.getName(), listX + 2, itemY + 2, 0xFFFFFF, false);
            g.drawString(this.font, "§7待: $" + String.format("%.0f", slave.getPendingTax()), listX + 2, itemY + 11, 0xAAAAAA, false);
        }

        if (slaveList.isEmpty()) {
            g.drawString(this.font, "§7无奴隶", listX + 30, slaveListY + 20, 0xAAAAAA, false);
        }

        g.drawString(this.font, "§a待领: $" + String.format("%.0f", totalPending), leftPos + 130, topPos + 130, 0x00FF00, false);
    }

    private void renderAdminLabels(GuiGraphics g) {
        g.drawCenteredString(this.font, Component.literal("§4§l行政面板"), leftPos + imageWidth / 2, topPos + 8, 0xFF0000);
        g.drawCenteredString(this.font, Component.literal("§7— 权力核心 —"), leftPos + imageWidth / 2, topPos + 185, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (currentTab == Tab.CONTRACTS) {
            int listX = leftPos + 8;
            int pariahListY = topPos + 37;
            int slaveListY = topPos + 117;

            if (mouseX >= listX && mouseX <= listX + 115 &&
                mouseY >= pariahListY && mouseY <= pariahListY + 62) {
                int clickedIndex = (int) ((mouseY - pariahListY) / 20);
                if (clickedIndex >= 0 && clickedIndex < pariahList.size()) {
                    selectedPariahIndex = clickedIndex;
                    return true;
                }
            }

            if (mouseX >= listX && mouseX <= listX + 115 &&
                mouseY >= slaveListY && mouseY <= slaveListY + 62) {
                int clickedIndex = (int) ((mouseY - slaveListY) / 20);
                if (clickedIndex >= 0 && clickedIndex < slaveList.size()) {
                    selectedSlaveIndex = clickedIndex;
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean canAccessContractsTab() {
        if (minecraft == null || minecraft.player == null) return false;
        var playerData = minecraft.player.getCapability(PlayerDataCapability.INSTANCE).orElse(null);
        if (playerData == null) return false;
        return playerData.getClassTier() == 2 || playerData.hasSlaves();
    }

    private boolean isOp() {
        return minecraft != null && minecraft.player != null && minecraft.player.hasPermissions(2);
    }

    enum Tab {
        PROFILE,
        CONTRACTS,
        ADMIN
    }
}
