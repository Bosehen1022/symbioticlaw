package com.symbioticlaw.command;

import com.symbioticlaw.system.AffinitySystem;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class RenewBondCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sl")
                .then(Commands.literal("bondrenew")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            new AffinitySystem().renewBond(player);
                            return 1;
                        })
                )
        );
    }
}
