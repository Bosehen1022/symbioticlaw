package com.symbioticlaw.block;

import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.WorldData;
import com.symbioticlaw.network.ClientBoundWarningPacket;
import com.symbioticlaw.network.NetworkHandler;
import com.symbioticlaw.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CoreBlock extends BaseEntityBlock {
    public CoreBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nonnull
    @Override
    public RenderShape getRenderShape(@Nonnull BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState pState, @Nonnull Level pLevel, @Nonnull BlockPos pPos, @Nonnull Player pPlayer, @Nonnull InteractionHand pHand, @Nonnull BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (pPlayer.position().distanceToSqr(pPos.getCenter()) > 100.0) {
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientBoundWarningPacket("§c⚠ 接入被拒绝：生物信号微弱\n§7请进入至高权力核心 10格 范围内。", ClientBoundWarningPacket.WarningType.TITLE));
                pLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            return InteractionResult.FAIL;
        }

        return pPlayer.getCapability(PlayerDataCapability.INSTANCE).map(data -> {
            if (data.getClassTier() <= 0) {
                pPlayer.sendSystemMessage(Component.translatable("msg.core.access_denied_pariah").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            PlayerDataCapability.sync((ServerPlayer) pPlayer);

            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof MenuProvider menuProvider) {
                NetworkHooks.openScreen((ServerPlayer) pPlayer, menuProvider, pPos);
            }
            return InteractionResult.SUCCESS;
        }).orElse(InteractionResult.PASS);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState) {
        return ModBlockEntities.POWER_CORE_BLOCK_ENTITY.get().create(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level pLevel, @Nonnull BlockState pState, @Nonnull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide) {
            return createTickerHelper(pBlockEntityType, ModBlockEntities.POWER_CORE_BLOCK_ENTITY.get(), CoreBlockEntity::clientTick);
        } else {
            return createTickerHelper(pBlockEntityType, ModBlockEntities.POWER_CORE_BLOCK_ENTITY.get(), CoreBlockEntity::serverTick);
        }
    }

    @Override
    public void setPlacedBy(@Nonnull Level pLevel, @Nonnull BlockPos pPos, @Nonnull BlockState pState, @Nullable LivingEntity pPlacer, @Nonnull ItemStack pStack) {
        if (!pLevel.isClientSide && pPlacer instanceof Player player) {
            if (player.hasPermissions(2)) {
                WorldData worldData = WorldData.get((ServerLevel) pLevel);
                if (worldData.getCorePosition().equals(BlockPos.ZERO)) {
                    worldData.setCorePosition(pPos);

                    Component message = Component.literal("§c[至高权力核心] 已激活。社会法则重构开始...§r");
                    pLevel.getServer().getPlayerList().broadcastSystemMessage(message, false);

                    for (ServerPlayer serverPlayer : pLevel.getServer().getPlayerList().getPlayers()) {
                        serverPlayer.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                            playerData.setBalance(500.0);
                            playerData.setClassTier(1);
                        });
                    }
                }
            }
        }
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
    }

    @Override
    public void onRemove(@Nonnull BlockState pState, @Nonnull Level pLevel, @Nonnull BlockPos pPos, @Nonnull BlockState pNewState, boolean pIsMoving) {
        if (!pLevel.isClientSide) {
            if (!pState.is(pNewState.getBlock())) {
                WorldData worldData = WorldData.get((ServerLevel) pLevel);
                if (worldData.getCorePosition().equals(pPos)) {
                    worldData.setCorePosition(BlockPos.ZERO);
                    Component message = Component.literal("§7[至高权力核心] 已离线。社会法则回归混沌。§r");
                    pLevel.getServer().getPlayerList().broadcastSystemMessage(message, false);
                }
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
}