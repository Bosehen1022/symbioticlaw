package com.symbioticlaw.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.client.ClientEconomyCache;
import com.symbioticlaw.client.ClientEconomyCache.MarketItemInfo;
import com.symbioticlaw.data.JobType;
import com.symbioticlaw.gui.menu.TradeTerminalMenu;
import com.symbioticlaw.network.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TradeTerminalScreen extends AbstractContainerScreen<TradeTerminalMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("symbioticlaw:textures/gui/trade_terminal_gui.png");

    private int currentTab = 0;
    private int selectedMarketItem = -1;
    private double estimatedTotalPrice = 0.0;
    private List<MarketItemEntry> marketItems = new ArrayList<>();

    private EditBox transferTargetBox;
    private EditBox transferAmountBox;

    private List<WholesaleItemEntry> wholesaleItems = new ArrayList<>();
    private int selectedWholesaleIndex = -1;

    private EditBox buyAmountBox;

    private double bulkPrice = 0.0;
    private double singlePrice = 0.0;

    public void updateBulkPriceData(double bulkPrice, double singlePrice) {
        this.bulkPrice = bulkPrice;
        this.singlePrice = singlePrice;
    }

    public TradeTerminalScreen(TradeTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();
        this.currentTab = 0;
        rebuildWidgets();
    }

    private void switchTab(int newTab) {
        if (this.currentTab == newTab) return;
        this.currentTab = newTab;

        if (newTab == 1) {
            NetworkHandler.INSTANCE.sendToServer(new ServerBoundRequestDailyQuotaPacket());
        } else if (newTab == 3) {
            if (ClientEconomyCache.isWholesaleDataStale()) {
                NetworkHandler.INSTANCE.sendToServer(new ServerBoundRequestEconomySyncPacket());
            }
            initWholesaleData();
        } else if (newTab == 0) {
            initMarketData();
        }

        rebuildWidgets();
    }

    protected void rebuildWidgets() {
        clearWidgets();
        int x = leftPos;
        int y = topPos;

        addTabButtons(x, y);

        switch (currentTab) {
            case 0 -> initTradeTab(x, y);
            case 1 -> initProfessionTab(x, y);
            case 2 -> initTransferTab(x, y);
            case 3 -> initWholesaleTab(x, y);
        }
    }

    private void addTabButtons(int x, int y) {
        int tabY = y + 3;
        int tabWidth = 42;
        int tabHeight = 16;

        addRenderableWidget(Button.builder(Component.literal("交易"), btn -> switchTab(0)).bounds(x, tabY, tabWidth, tabHeight).build());
        addRenderableWidget(Button.builder(Component.literal("职业"), btn -> switchTab(1)).bounds(x + tabWidth + 2, tabY, tabWidth, tabHeight).build());
        addRenderableWidget(Button.builder(Component.literal("转账"), btn -> switchTab(2)).bounds(x + (tabWidth + 2) * 2, tabY, tabWidth, tabHeight).build());

        if (isChef()) {
            addRenderableWidget(Button.builder(Component.literal("批发"), btn -> switchTab(3)).bounds(x + (tabWidth + 2) * 3, tabY, tabWidth, tabHeight).build());
        }
    }

    private void initTradeTab(int x, int y) {
        addRenderableWidget(Button.builder(
            Component.literal("出售"),
            btn -> {
                ItemStack sellStack = this.menu.getSellSlotItem();
                if (!sellStack.isEmpty()) {
                    String itemId = ForgeRegistries.ITEMS.getKey(sellStack.getItem()).toString();
                    NetworkHandler.INSTANCE.sendToServer(new ServerBoundSellItemPacket(itemId, sellStack.getCount()));
                }
            }
        ).bounds(x + 120, y + 165, 48, 16).build());
    }

    private void initMarketData() {
        marketItems.clear();
        Map<String, MarketItemInfo> catalog = ClientEconomyCache.getMarketCache();

        if (catalog != null) {
            for (Map.Entry<String, MarketItemInfo> entry : catalog.entrySet()) {
                addMarketItemEntry(entry.getKey(), entry.getValue());
            }
        }

        marketItems.sort((a, b) -> Double.compare(b.currentPrice, a.currentPrice));
    }

    private void addMarketItemEntry(String itemId, MarketItemInfo info) {
        var item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemId));
        if (item != null && !item.equals(Items.AIR)) {
            ItemStack stack = new ItemStack(item);
            marketItems.add(new MarketItemEntry(
                itemId,
                info.displayName,
                info.basePrice,
                info.currentPrice,
                info.demand > 0 ? (int)(info.demand * 10) : 0,
                stack
            ));
        }
    }

    private void initProfessionTab(int x, int y) {
        if (minecraft == null || minecraft.player == null) return;

        minecraft.player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            int professionId = playerData.getProfessionId();

            if (professionId <= 0) {
                return;
            }

            addRenderableWidget(Button.builder(
                Component.literal("提交定额"),
                btn -> NetworkHandler.INSTANCE.sendToServer(new ServerBoundTurnInDailyQuotaPacket())
            ).bounds(x + 55, y + 80, 66, 18).build());
        });
    }

    private void initTransferTab(int x, int y) {
        int centerX = x + imageWidth / 2;
        int startY = y + 30;

        this.transferTargetBox = new EditBox(this.font, centerX - 70, startY, 140, 14, Component.literal(""));
        this.transferTargetBox.setHint(Component.literal("玩家名"));
        this.transferTargetBox.setMaxLength(16);
        addRenderableWidget(this.transferTargetBox);

        this.transferAmountBox = new EditBox(this.font, centerX - 70, startY + 18, 140, 14, Component.literal(""));
        this.transferAmountBox.setHint(Component.literal("金额"));
        this.transferAmountBox.setMaxLength(10);
        addRenderableWidget(this.transferAmountBox);

        addRenderableWidget(Button.builder(
            Component.literal("确认转账"),
            btn -> executeTransfer()
        ).bounds(centerX - 35, startY + 45, 70, 18).build());
    }

    private void executeTransfer() {
        if (transferTargetBox == null || transferAmountBox == null) return;

        String target = transferTargetBox.getValue().trim();
        String amountStr = transferAmountBox.getValue().trim();

        if (target.isEmpty() || amountStr.isEmpty()) return;

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount > 0) {
                NetworkHandler.INSTANCE.sendToServer(new ServerBoundTransferPacket(target, amount));
                transferTargetBox.setValue("");
                transferAmountBox.setValue("");
            }
        } catch (NumberFormatException ignored) {}
    }

    private void initWholesaleTab(int x, int y) {
        addRenderableWidget(Button.builder(
            Component.literal("刷新"),
            btn -> {
                NetworkHandler.INSTANCE.sendToServer(new ServerBoundRequestEconomySyncPacket());
                initWholesaleData();
            }
        ).bounds(x + 120, y + 25, 48, 14).build());

        this.buyAmountBox = new EditBox(this.font, x + 120, y + 43, 48, 14, Component.literal(""));
        this.buyAmountBox.setValue("1");
        this.buyAmountBox.setMaxLength(4);
        addRenderableWidget(this.buyAmountBox);

        addRenderableWidget(Button.builder(
            Component.literal("购买"),
            btn -> executeWholesalePurchase()
        ).bounds(x + 120, y + 60, 48, 16).build());
    }

    private void initWholesaleData() {
        wholesaleItems.clear();

        Map<String, Double> catalog = ClientEconomyCache.getWholesalePrices();

        for (Map.Entry<String, Double> entry : catalog.entrySet()) {
            String itemId = entry.getKey();
            double price = entry.getValue();

            var item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemId));
            if (item != null && !item.equals(Items.AIR)) {
                ItemStack stack = new ItemStack(item);
                wholesaleItems.add(new WholesaleItemEntry(
                    itemId,
                    stack.getDisplayName().getString(),
                    price,
                    stack
                ));
            }
        }

        wholesaleItems.sort((a, b) -> Double.compare(b.price, a.price));
    }

    private void executeWholesalePurchase() {
        if (selectedWholesaleIndex < 0 || selectedWholesaleIndex >= wholesaleItems.size()) return;

        int amount = 1;
        try {
            if (buyAmountBox != null && !buyAmountBox.getValue().isEmpty()) {
                amount = Integer.parseInt(buyAmountBox.getValue());
                amount = Math.max(1, Math.min(amount, 64));
            }
        } catch (NumberFormatException ignored) {}

        var item = wholesaleItems.get(selectedWholesaleIndex);
        NetworkHandler.INSTANCE.sendToServer(new ServerBoundWholesalePacket(item.itemId, amount));
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (transferTargetBox != null) transferTargetBox.tick();
        if (transferAmountBox != null) transferAmountBox.tick();
        if (buyAmountBox != null) buyAmountBox.tick();
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
        renderHeader(graphics);

        switch (currentTab) {
            case 0 -> renderTradeLabels(graphics);
            case 1 -> renderProfessionLabels(graphics);
            case 2 -> renderTransferLabels(graphics);
            case 3 -> renderWholesaleLabels(graphics);
        }
    }

    private void renderHeader(GuiGraphics g) {
        if (minecraft == null || minecraft.player == null) return;

        minecraft.player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            int classTier = playerData.getClassTier();
            double balance = playerData.getBalance();

            String className = switch(classTier) {
                case -1, 0 -> "§7遗民";
                case 1 -> "§e受限";
                case 2 -> "§b自由";
                default -> "§7?";
            };

            String header = className + " §7| §e$" + String.format("%.1f", balance);
            g.drawCenteredString(this.font, header, imageWidth / 2, 5, 0xFFFFFF);
        });
    }

    private void renderTradeLabels(GuiGraphics g) {
        int listX = leftPos + 6;
        int listY = topPos + 22;

        g.drawString(this.font, "§e收购", listX, listY, 0xFFFFFF, false);
        listY += 10;

        g.fill(listX - 1, listY - 1, listX + 110, listY + 110, 0xFF333333);

        for (int i = 0; i < Math.min(6, marketItems.size()); i++) {
            var item = marketItems.get(i);
            int itemY = listY + i * 18;

            if (i == selectedMarketItem) {
                g.fill(listX, itemY, listX + 109, itemY + 17, 0xFF555555);
            }

            String name = item.displayName;
            if (name.length() > 9) name = name.substring(0, 9);
            g.drawString(this.font, "§f" + name, listX + 2, itemY + 2, 0xFFFFFF, false);
            g.drawString(this.font, "§7$" + String.format("%.1f", item.currentPrice), listX + 2, itemY + 10, item.stock > 0 ? 0x00AA00 : 0xAA0000, false);
        }

        if (marketItems.isEmpty()) {
            g.drawString(this.font, "§7加载中...", listX + 30, listY + 40, 0xAAAAAA, false);
        }

        int detailX = leftPos + 120;
        int detailY = topPos + 22;
        g.drawString(this.font, "§e详情", detailX, detailY, 0xFFFFFF, false);
        detailY += 12;

        if (selectedMarketItem >= 0 && selectedMarketItem < marketItems.size()) {
            var selected = marketItems.get(selectedMarketItem);

            String name = selected.displayName;
            if (name.length() > 10) name = name.substring(0, 10);
            g.drawString(this.font, "§f" + name, detailX, detailY, 0xFFFFFF, false);
            detailY += 12;

            g.drawString(this.font, "§7单价:", detailX, detailY, 0xAAAAAA, false);
            g.drawString(this.font, "§e$" + String.format("%.2f", selected.currentPrice), detailX + 35, detailY, 0x00AA00, false);
            detailY += 12;

            if (selected.stock <= 0) {
                g.drawString(this.font, "§c⚠缺货", detailX, detailY, 0xFF0000, false);
            } else {
                g.drawString(this.font, "§7库存: " + selected.stock, detailX, detailY, 0xAAAAAA, false);
            }
            detailY += 12;

            int count = getSellSlotCount();
            if (count > 0) {
                double total = selected.currentPrice * count;
                g.drawString(this.font, "§7数量: " + count, detailX, detailY, 0xAAAAAA, false);
                detailY += 12;
                g.drawString(this.font, "§e总计:", detailX, detailY, 0xFFFFFF, false);
                g.drawString(this.font, "§a$" + String.format("%.2f", total), detailX, detailY + 10, 0x00FF00, false);
            }
        } else {
            g.drawString(this.font, "§7选择物品", detailX, detailY, 0xAAAAAA, false);
        }

        g.drawString(this.font, "§7放入出售", leftPos + 6, topPos + 165, 0xAAAAAA, false);
    }

    private int getSellSlotCount() {
        ItemStack stack = this.menu.getSellSlotItem();
        return stack.isEmpty() ? 0 : stack.getCount();
    }

    private void renderProfessionLabels(GuiGraphics g) {
        if (minecraft == null || minecraft.player == null) return;

        minecraft.player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            int professionId = playerData.getProfessionId();

            if (professionId <= 0) {
                g.drawCenteredString(this.font, "§7无职业", imageWidth / 2, 40, 0xAAAAAA);
                return;
            }

            JobType job = JobType.fromId(professionId);
            int level = playerData.getProfessionLevel(professionId);
            double xp = playerData.getProfessionXp(professionId);

            g.drawString(this.font, "§e职业: §f" + job.getDisplayName(), 10, 22, 0xFFFFFF, false);
            g.drawString(this.font, "§7等级: §fLv." + level, 10, 36, 0xFFFFFF, false);
            g.drawString(this.font, "§7经验: §f" + String.format("%.0f", xp), 10, 50, 0xFFFFFF, false);

            int xpProgress = (int) ((xp / (level * 500)) * 100);
            g.fill(10, 62, 10 + Math.min(156, xpProgress), 66, 0x00AA00);
            g.fill(10 + Math.min(156, xpProgress), 62, 166, 66, 0x333333);
            g.drawString(this.font, "§7进度: " + xpProgress + "%", 10, 68, 0xAAAAAA, false);

            int bonus = (int) ((level - 1) * 5 + 10);
            g.drawString(this.font, "§a加成: +" + bonus + "%", 10, 82, 0x00FF00, false);

            String quotaItem = ClientEconomyCache.getDailyQuotaItem();
            if (quotaItem != null && !quotaItem.isEmpty()) {
                g.drawString(this.font, "§e定额: §f" + quotaItem, 10, 98, 0xFFFFFF, false);
                g.drawString(this.font, "§7进度: " + ClientEconomyCache.getDailyQuotaProgress() + "/" + ClientEconomyCache.getDailyQuotaRequired(), 10, 112, 0xAAAAAA, false);
            }
        });
    }

    private void renderTransferLabels(GuiGraphics g) {
        g.drawCenteredString(this.font, "§eP2P 转账", imageWidth / 2, 22, 0xFFFFFF);

        double taxRate = ClientEconomyCache.getTransferTaxRate();
        g.drawCenteredString(this.font, Component.literal("§7税率: §c" + (int)(taxRate * 100) + "%"), imageWidth / 2, 110, 0xAAAAAA);

        if (transferAmountBox != null && !transferAmountBox.getValue().isEmpty()) {
            try {
                double amount = Double.parseDouble(transferAmountBox.getValue());
                double afterTax = amount * (1.0 - taxRate);
                double taxAmount = amount - afterTax;

                g.drawString(this.font, "§7税额: §c-$" + String.format("%.2f", taxAmount), 10, 130, 0xFF6600, false);
                g.drawString(this.font, "§a到账: §f$" + String.format("%.2f", afterTax), 10, 144, 0x00FF00, false);
            } catch (NumberFormatException ignored) {}
        }
    }

    private void renderWholesaleLabels(GuiGraphics g) {
        int listX = leftPos + 6;
        int listY = topPos + 22;

        g.drawString(this.font, "§e粮仓", listX, listY, 0xFFFFFF, false);
        listY += 12;

        g.fill(listX - 1, listY - 1, listX + 110, listY + 110, 0xFF333333);

        for (int i = 0; i < Math.min(6, wholesaleItems.size()); i++) {
            var entry = wholesaleItems.get(i);
            int itemY = listY + i * 18;

            if (i == selectedWholesaleIndex) {
                g.fill(listX, itemY, listX + 109, itemY + 17, 0xFF555555);
            }

            String name = entry.displayName;
            if (name.length() > 9) name = name.substring(0, 9);
            g.drawString(this.font, "§f" + name, listX + 2, itemY + 2, 0xFFFFFF, false);
            g.drawString(this.font, "§7$" + String.format("%.1f", entry.price), listX + 2, itemY + 10, 0x00AA00, false);
        }

        if (wholesaleItems.isEmpty()) {
            g.drawString(this.font, "§7加载中...", listX + 30, listY + 40, 0xAAAAAA, false);
        }

        int detailX = leftPos + 120;
        int detailY = topPos + 22;

        if (selectedWholesaleIndex >= 0 && selectedWholesaleIndex < wholesaleItems.size()) {
            var selected = wholesaleItems.get(selectedWholesaleIndex);
            g.drawString(this.font, "§e" + selected.displayName, detailX, detailY, 0xFFFFFF, false);
            detailY += 12;
            g.drawString(this.font, "§7单价: §e$" + String.format("%.2f", selected.price), detailX, detailY, 0xAAAAAA, false);
            detailY += 24;

            int amount = 1;
            try {
                if (buyAmountBox != null && !buyAmountBox.getValue().isEmpty()) {
                    amount = Integer.parseInt(buyAmountBox.getValue());
                }
            } catch (NumberFormatException ignored) {}

            double total = selected.price * amount;
            g.drawString(this.font, "§7x" + amount + " = §e$" + String.format("%.2f", total), detailX, detailY, 0x00FF00, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (currentTab == 0) {
            int listX = leftPos + 6;
            int listY = topPos + 32;

            if (mouseX >= listX && mouseX <= listX + 110 && mouseY >= listY && mouseY <= listY + 110) {
                int clickedIndex = (int) ((mouseY - listY) / 18);
                if (clickedIndex >= 0 && clickedIndex < marketItems.size()) {
                    selectedMarketItem = clickedIndex;
                    return true;
                }
            }
        }

        if (currentTab == 3) {
            int listX = leftPos + 6;
            int listY = topPos + 34;

            if (mouseX >= listX && mouseX <= listX + 110 && mouseY >= listY && mouseY <= listY + 110) {
                int clickedIndex = (int) ((mouseY - listY) / 18);
                if (clickedIndex >= 0 && clickedIndex < wholesaleItems.size()) {
                    selectedWholesaleIndex = clickedIndex;
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isChef() {
        if (minecraft == null || minecraft.player == null) return false;
        var playerData = minecraft.player.getCapability(PlayerDataCapability.INSTANCE).orElse(null);
        if (playerData == null) return false;
        return playerData.getProfession().toJobType() == JobType.CHEF;
    }

    private static class MarketItemEntry {
        final String itemId;
        final String displayName;
        final double basePrice;
        final double currentPrice;
        final int stock;
        final ItemStack stack;

        MarketItemEntry(String itemId, String displayName, double basePrice, double currentPrice, int stock, ItemStack stack) {
            this.itemId = itemId;
            this.displayName = displayName;
            this.basePrice = basePrice;
            this.currentPrice = currentPrice;
            this.stock = stock;
            this.stack = stack;
        }
    }

    private static class WholesaleItemEntry {
        final String itemId;
        final String displayName;
        final double price;
        final ItemStack stack;

        WholesaleItemEntry(String itemId, String displayName, double price, ItemStack stack) {
            this.itemId = itemId;
            this.displayName = displayName;
            this.price = price;
            this.stack = stack;
        }
    }
}