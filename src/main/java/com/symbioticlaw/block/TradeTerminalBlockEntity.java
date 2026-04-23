package com.symbioticlaw.block;

import com.symbioticlaw.gui.menu.TradeTerminalMenu;
import com.symbioticlaw.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class TradeTerminalBlockEntity extends BlockEntity implements MenuProvider {
    public TradeTerminalBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.TRADE_TERMINAL_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return Component.literal("交易终端");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @Nonnull Inventory pPlayerInventory, @Nonnull Player pPlayer) {
        return new TradeTerminalMenu(pContainerId, pPlayerInventory, this.worldPosition);
    }
}