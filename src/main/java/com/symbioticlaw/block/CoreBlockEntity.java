package com.symbioticlaw.block;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.gui.menu.PowerCoreMenu;
import com.symbioticlaw.registry.ModBlockEntities;
import com.symbioticlaw.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

public class CoreBlockEntity extends BlockEntity implements MenuProvider {

    private int syncedClassTier = -1;

    public CoreBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.POWER_CORE_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return Component.literal("权力核心");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @Nonnull Inventory pPlayerInventory, @Nonnull Player pPlayer) {
        return new PowerCoreMenu(pContainerId, pPlayerInventory, pPlayer);
    }

    public static void serverTick(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull CoreBlockEntity blockEntity) {
        if (level.getGameTime() % 20 == 0) {
            Player nearestPlayer = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, false);
            int currentTier = -1;
            if (nearestPlayer != null) {
                currentTier = nearestPlayer.getCapability(PlayerDataCapability.INSTANCE).map(IPlayerData::getClassTier).orElse(-1);
            }

            if (blockEntity.syncedClassTier != currentTier) {
                blockEntity.syncedClassTier = currentTier;
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull CoreBlockEntity blockEntity) {
        ClientTicker.tick(level, pos, state, blockEntity);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void load(@Nonnull CompoundTag pTag) {
        super.load(pTag);
        this.syncedClassTier = pTag.getInt("ClassTier");
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("ClassTier", this.syncedClassTier);
    }

    public int getSyncedClassTier() {
        return syncedClassTier;
    }

    @OnlyIn(Dist.CLIENT)
    private static class ClientTicker {
        private static final Map<BlockPos, AbstractTickableSoundInstance> humSounds = new WeakHashMap<>();

        public static void tick(Level level, BlockPos pos, BlockState state, CoreBlockEntity blockEntity) {
            if (blockEntity.syncedClassTier != -1 && level.getGameTime() % 5 == 0) {
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 0.5;
                double z = pos.getZ() + 0.5;

                for(int i = 0; i < 2; ++i) {
                    double px = x + (level.random.nextDouble() - 0.5) * 2.0;
                    double py = y + (level.random.nextDouble() - 0.5) * 2.0;
                    double pz = z + (level.random.nextDouble() - 0.5) * 2.0;

                    switch (blockEntity.syncedClassTier) {
                        case 0: level.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, 0, 0, 0); break;
                        case 1: level.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0, 0, 0); break;
                        case 2: level.addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0, 0); break;
                    }
                }
            }

            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            double distance = player.position().distanceTo(pos.getCenter());
            AbstractTickableSoundInstance existingSound = humSounds.get(pos);

            if (existingSound != null && existingSound.isStopped()) {
                humSounds.remove(pos);
                existingSound = null;
            }

            if (distance <= 10.0) {
                if (existingSound == null) {
                    AbstractTickableSoundInstance newSound = new AbstractTickableSoundInstance(ModSounds.CORE_HUM_LOOP.get(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom()) {
                        private boolean stopped = false;

                        @Override
                        public void tick() {
                            if (stopped) return;
                            if (!(Minecraft.getInstance().level.getBlockEntity(pos) instanceof CoreBlockEntity)) {
                                stopped = true;
                                return;
                            }

                            double dist = Minecraft.getInstance().player.position().distanceTo(pos.getCenter());
                            if (dist > 10.0) {
                                stopped = true;
                            } else {
                                this.x = pos.getX() + 0.5;
                                this.y = pos.getY() + 0.5;
                                this.z = pos.getZ() + 0.5;
                                this.volume = Mth.map((float)dist, 0, 10, 1.0f, 0.1f);
                            }
                        }

                        @Override
                        public boolean isStopped() {
                            return stopped || super.isStopped();
                        }
                    };
                    Minecraft.getInstance().getSoundManager().play(newSound);
                    humSounds.put(pos, newSound);
                }
            } else {
                if (existingSound != null) {
                    Minecraft.getInstance().getSoundManager().stop(existingSound);
                    humSounds.remove(pos);
                }
            }
        }
    }
}