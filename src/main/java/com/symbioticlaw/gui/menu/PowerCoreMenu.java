package com.symbioticlaw.gui.menu;

import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.registry.ModBlocks;
import com.symbioticlaw.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class PowerCoreMenu extends AbstractContainerMenu {
    
    private final ContainerData data;
    private final ContainerLevelAccess access;

    // Server-side constructor - Called from the BlockEntity
    public PowerCoreMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        this(containerId, playerInventory, ContainerLevelAccess.create(playerInventory.player.level(), blockPos), new SimpleContainerData(3));
        
        // Populate the data container on the server
        playerInventory.player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            long balanceBits = Double.doubleToLongBits(playerData.getBalance());
            this.data.set(0, (int) (balanceBits >> 32));
            this.data.set(1, (int) balanceBits);
            this.data.set(2, playerData.getClassTier());
        });
    }

    // Client-side constructor - Called by Forge
    public PowerCoreMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, ContainerLevelAccess.create(playerInventory.player.level(), extraData.readBlockPos()), new SimpleContainerData(3));
    }

    // Private constructor for internal use
    private PowerCoreMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access, ContainerData data) {
        super(ModMenuTypes.CORE_MENU.get(), containerId);
        this.data = data;
        this.access = access;
        addDataSlots(data);
    }


    public double getBalance() {
        long balanceBits = ((long) this.data.get(0) << 32) | (this.data.get(1) & 0xFFFFFFFFL);
        return Double.longBitsToDouble(balanceBits);
    }

    public int getClassTier() {
        return this.data.get(2);
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return stillValid(this.access, player, ModBlocks.POWER_CORE.get());
    }
}
