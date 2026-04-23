package com.symbioticlaw;

import com.symbioticlaw.gui.hud.AlmanacHud;
import com.symbioticlaw.registry.ModBlockEntities;
import com.symbioticlaw.registry.ModMenuTypes;
import com.symbioticlaw.registry.ModSounds;
import com.symbioticlaw.gui.screen.CoreScreen;
import com.symbioticlaw.gui.hud.BorderWarningOverlay;
import com.symbioticlaw.gui.screen.TradeTerminalScreen;
import com.symbioticlaw.network.ModMessages;
import com.symbioticlaw.network.NetworkHandler;
import com.symbioticlaw.registry.ModBlocks;
import com.symbioticlaw.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Symbioticlaw.MODID)
public class Symbioticlaw {

    public static final String MODID = "symbioticlaw";
    private static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("removal")
    public Symbioticlaw() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(com.symbioticlaw.event.MachinePlacementEvents.class);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
        event.enqueueWork(ModMessages::register);
        
        // 初始化Waystones集成（如果模组存在）
        com.symbioticlaw.event.WaystoneEvents.init();
        
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenuTypes.CORE_MENU.get(), CoreScreen::new);
                MenuScreens.register(ModMenuTypes.TRADE_TERMINAL_MENU.get(), TradeTerminalScreen::new);
            });
        }

        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("almanac_hud", AlmanacHud.HUD_ALMANAC);
            event.registerAboveAll("border_warning", BorderWarningOverlay.HUD_BORDER_WARNING);
        }
    }
}
