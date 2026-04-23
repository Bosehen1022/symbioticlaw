package com.symbioticlaw.registry;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.block.CoreBlockEntity;
import com.symbioticlaw.block.TradeTerminalBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Symbioticlaw.MODID);

    public static final RegistryObject<BlockEntityType<CoreBlockEntity>> POWER_CORE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("power_core_entity", () ->
                    BlockEntityType.Builder.of(CoreBlockEntity::new,
                            ModBlocks.POWER_CORE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TradeTerminalBlockEntity>> TRADE_TERMINAL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("trade_terminal_entity", () ->
                    BlockEntityType.Builder.of(TradeTerminalBlockEntity::new,
                            ModBlocks.TRADE_TERMINAL.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
