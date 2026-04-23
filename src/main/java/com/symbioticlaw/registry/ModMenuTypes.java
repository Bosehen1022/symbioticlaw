package com.symbioticlaw.registry;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.gui.menu.PowerCoreMenu;
import com.symbioticlaw.gui.menu.TradeTerminalMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Symbioticlaw.MODID);

    public static final RegistryObject<MenuType<PowerCoreMenu>> CORE_MENU =
            registerMenuType(PowerCoreMenu::new, "power_core_menu");

    public static final RegistryObject<MenuType<TradeTerminalMenu>> TRADE_TERMINAL_MENU =
            registerMenuType(TradeTerminalMenu::new, "trade_terminal_menu");

    // DELETED non-compliant menus



    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
