package com.symbioticlaw.registry;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.item.ExplorersAlmanac;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Symbioticlaw.MODID);

    public static final RegistryObject<Item> EXPLORERS_ALMANAC = ITEMS.register("explorers_almanac", ExplorersAlmanac::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
