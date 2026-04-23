package com.symbioticlaw.gui.menu;

import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class PowerCoreMenu extends AbstractContainerMenu {
    
    private final ContainerData data;

    // Server-side constructor - Called from the BlockEntity
    public PowerCoreMenu(int containerId, Inventory playerInventory, Player player) {
        this(containerId, playerInventory, new SimpleContainerData(3)); // Use the private constructor
        
        // Populate the data container on the server
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            long balanceBits = Double.doubleToLongBits(playerData.getBalance());
            this.data.set(0, (int) (balanceBits >> 32));
            this.data.set(1, (int) balanceBits);
            this.data.set(2, playerData.getClassTier());
            // Note: Affinity level and other complex data are synced via PlayerDataSyncPacket
            // and accessed via ClientPlayerData on the screen. This menu only syncs essential data
            // needed for immediate rendering decisions not covered by the main sync packet.
        });
    }

    // Client-side constructor - Called by Forge
    public PowerCoreMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainerData(3));
    }

    // Private constructor for internal use
    private PowerCoreMenu(int containerId, Inventory playerInventory, ContainerData data) {
        super(ModMenuTypes.CORE_MENU.get(), containerId);
        this.data = data;
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
        // In the future, we might check distance to the core block here
        return true;
    }
}
