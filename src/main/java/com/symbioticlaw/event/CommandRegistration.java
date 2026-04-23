package com.symbioticlaw.event;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.command.ContractCommand;
import com.symbioticlaw.command.MarketCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Symbioticlaw.MODID)
public class CommandRegistration {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        ContractCommand.register(event.getDispatcher());
        MarketCommand.register(event.getDispatcher(), event.getBuildContext());
    }
}
