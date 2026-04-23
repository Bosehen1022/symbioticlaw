package com.symbioticlaw.command;

import com.symbioticlaw.system.ClassSystem;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class RequestSurvivalPassCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sl")
                .then(Commands.literal("survivalpass")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            new ClassSystem().requestSurvivalPass(player);
                            return 1;
                        })
                )
        );
    }
}
