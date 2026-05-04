package com.symbioticlaw.gui.menu;

import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.MarketInfo;
import com.symbioticlaw.registry.ModBlocks;
import com.symbioticlaw.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TradeTerminalMenu extends AbstractContainerMenu {
    private final IItemHandler sellSlotHandler = new ItemStackHandler(1);
    public final List<MarketInfo.MarketItemWithPrice> marketItemsWithPrices = new ArrayList<>();
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public TradeTerminalMenu(int pContainerId, Inventory pPlayerInventory, BlockPos pBlockPos) {
        this(pContainerId, pPlayerInventory, ContainerLevelAccess.create(pPlayerInventory.player.level(), pBlockPos), new SimpleContainerData(4));

        pPlayerInventory.player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            int profId = playerData.getProfessionId();
            this.data.set(0, profId);
            if (profId > 0) {
                this.data.set(1, playerData.getProfessionLevel(profId));
                long xpBits = Double.doubleToLongBits(playerData.getProfessionXp(profId));
                this.data.set(2, (int) (xpBits >> 32));
                this.data.set(3, (int) xpBits);
            }
        });
    }

    public TradeTerminalMenu(int pContainerId, Inventory pPlayerInventory, Player player) {
        this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL, new SimpleContainerData(4));

        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            int profId = playerData.getProfessionId();
            this.data.set(0, profId);
            if (profId > 0) {
                this.data.set(1, playerData.getProfessionLevel(profId));
                long xpBits = Double.doubleToLongBits(playerData.getProfessionXp(profId));
                this.data.set(2, (int) (xpBits >> 32));
                this.data.set(3, (int) xpBits);
            }
        });
    }

    public TradeTerminalMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf extraData) {
        this(pContainerId, pPlayerInventory, ContainerLevelAccess.create(pPlayerInventory.player.level(), extraData.readBlockPos()), new SimpleContainerData(4));
    }

    private TradeTerminalMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess access, ContainerData data) {
        super(ModMenuTypes.TRADE_TERMINAL_MENU.get(), pContainerId);
        this.data = data;
        this.access = access;

        this.addSlot(new SlotItemHandler(sellSlotHandler, 0, 8, 62));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(pPlayerInventory, i, 8 + i * 18, 142));
        }

        addDataSlots(data);
    }

    public int getProfessionId() {
        return this.data.get(0);
    }

    public int getProfessionLevel() {
        return this.data.get(1);
    }

    public double getProfessionXp() {
        long xpBits = ((long) this.data.get(2) << 32) | (this.data.get(3) & 0xFFFFFFFFL);
        return Double.longBitsToDouble(xpBits);
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player pPlayer, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (pIndex == 0) {
            if (!moveItemStackTo(sourceStack, 1, 37, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(sourceStack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(pPlayer, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(@Nonnull Player pPlayer) {
        return stillValid(this.access, pPlayer, ModBlocks.TRADE_TERMINAL.get());
    }

    public ItemStack getSellSlotItem() {
        return this.sellSlotHandler.getStackInSlot(0);
    }

    public MarketInfo.AdjustedPrice getAdjustedPrice(com.symbioticlaw.data.MarketItem marketItem) {
        for (MarketInfo.MarketItemWithPrice itemWithPrice : marketItemsWithPrices) {
            if (itemWithPrice.item().itemId.equals(marketItem.itemId)) {
                return itemWithPrice.adjustedPrice();
            }
        }
        return new MarketInfo.AdjustedPrice(0, 0);
    }
}
