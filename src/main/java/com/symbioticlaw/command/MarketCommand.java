package com.symbioticlaw.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.symbioticlaw.data.MarketData;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public class MarketCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("market")
                .requires(source -> source.hasPermission(2)) // Operator level permission
                .then(Commands.literal("set")
                        .then(Commands.argument("item", ItemArgument.item(context))
                                .then(Commands.argument("basePrice", DoubleArgumentType.doubleArg(0.0))
                                        .then(Commands.argument("targetStock", IntegerArgumentType.integer(0))
                                                .executes(cmdContext -> {
                                                    ItemInput itemInput = ItemArgument.getItem(cmdContext, "item");
                                                    double basePrice = DoubleArgumentType.getDouble(cmdContext, "basePrice");
                                                    int targetStock = IntegerArgumentType.getInteger(cmdContext, "targetStock");
                                                    ServerLevel level = cmdContext.getSource().getLevel();
                                                    MarketData marketData = MarketData.get(level);

                                                    ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(itemInput.getItem());
                                                    marketData.setMarketItem(itemId.toString(), basePrice, targetStock);

                                                    cmdContext.getSource().sendSuccess(() -> Component.literal(
                                                            "Set market data for " + itemId + " with base price " + basePrice + " and target stock " + targetStock
                                                    ), true);
                                                    return 1;
                                                })))))
        );
    }
}
