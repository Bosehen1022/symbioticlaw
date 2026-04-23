package com.symbioticlaw.event;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.WorldData;
import com.symbioticlaw.integration.ModIntegration;
import com.symbioticlaw.system.TaxSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = Symbioticlaw.MODID)
public class WaystoneEvents {

    private static final TaxSystem TAX_SYSTEM = new TaxSystem();
    private static boolean waystonesIntegrationActive = false;

    private static Class<?> waystoneTeleportEventClass = null;
    private static Class<?> waystoneClass = null;
    private static Method getWaystoneMethod = null;
    private static Method getPosMethod = null;
    private static Method getDimensionMethod = null;
    private static Method setCanceledMethod = null;
    private static Method getEntityMethod = null;

    public static void init() {
        if (!ModIntegration.isWaystonesLoaded()) {
            System.out.println("[SymbioticLaw] Waystones模组未加载，跳过传送石碑集成。");
            return;
        }

        try {
            waystoneTeleportEventClass = Class.forName("net.blay09.mods.waystones.api.WaystoneTeleportEvent$Pre");
            waystoneClass = Class.forName("net.blay09.mods.waystones.api.Waystone");

            getWaystoneMethod = waystoneTeleportEventClass.getMethod("getWaystone");
            getPosMethod = waystoneClass.getMethod("getPos");
            getDimensionMethod = waystoneClass.getMethod("getDimension");
            setCanceledMethod = waystoneTeleportEventClass.getMethod("setCanceled", boolean.class);
            getEntityMethod = waystoneTeleportEventClass.getMethod("getEntity");

            MinecraftForge.EVENT_BUS.addListener(WaystoneEvents::onWaystoneTeleportPre);
            MinecraftForge.EVENT_BUS.addListener(WaystoneEvents::onWaystoneTeleportPost);

            waystonesIntegrationActive = true;
            System.out.println("[SymbioticLaw] Waystones集成已激活。传送将被征税。");
        } catch (Exception e) {
            System.out.println("[SymbioticLaw] Waystones集成失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void onWaystoneTeleportPre(Object event) {
        if (!waystonesIntegrationActive) return;

        try {
            Object entity = getEntityMethod.invoke(event);
            if (!(entity instanceof ServerPlayer player)) {
                return;
            }

            Object waystone = getWaystoneMethod.invoke(event);
            BlockPos toPos = (BlockPos) getPosMethod.invoke(waystone);
            ResourceKey<Level> toDim = (ResourceKey<Level>) getDimensionMethod.invoke(waystone);

            BlockPos fromPos = player.blockPosition();
            ResourceKey<Level> fromDim = player.level().dimension();

            double cost = TAX_SYSTEM.calculateWaystoneCost(player, fromPos, toPos, fromDim, toDim);

            var playerDataOpt = player.getCapability(PlayerDataCapability.INSTANCE).resolve();
            if (playerDataOpt.isEmpty()) {
                setCanceledMethod.invoke(event, true);
                player.sendSystemMessage(Component.literal("§c[系统错误] 无法获取玩家数据，传送已取消。"));
                return;
            }

            IPlayerData playerData = playerDataOpt.get();

            if (playerData.getBalance() < cost) {
                setCanceledMethod.invoke(event, true);
                player.sendSystemMessage(Component.literal(
                    String.format("§c[🚫 资金不足] 传送费用 $%.2f，您的余额 $%.2f。请前往核心赚取更多资金。",
                        cost, playerData.getBalance())));

                sendCostBreakdown(player, fromPos, toPos, fromDim, toDim, cost, playerData);
                return;
            }

            playerData.setBalance(playerData.getBalance() - cost);
            playerData.addTaxPaidThisCycle(cost);
            WorldData.get(player.serverLevel()).addTotalTaxCollected(cost);

            player.sendSystemMessage(Component.literal(
                String.format("§e[传送税] 已扣除 $%.2f 里程税/维度税。", cost)));

            sendCostBreakdown(player, fromPos, toPos, fromDim, toDim, cost, playerData);

            sendActionBar(player, cost);

        } catch (Exception e) {
            System.out.println("[SymbioticLaw] Waystones传送处理错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void onWaystoneTeleportPost(Object event) {
        if (!waystonesIntegrationActive) return;

        try {
            Method getEntity = event.getClass().getMethod("getEntity");
            Object entity = getEntity.invoke(event);

            if (!(entity instanceof ServerPlayer player)) {
                return;
            }

            checkDangerZoneWarning(player);
        } catch (Exception e) {
            System.out.println("[SymbioticLaw] Waystones传送后处理错误: " + e.getMessage());
        }
    }

    private static void sendActionBar(ServerPlayer player, double cost) {
        Component actionBarMessage = Component.literal(String.format("§6§l[-$%.2f 传送税] §7余额: $%.2f",
            cost, player.getCapability(PlayerDataCapability.INSTANCE).map(IPlayerData::getBalance).orElse(0.0)));

        player.connection.send(new ClientboundSetActionBarTextPacket(actionBarMessage));
    }

    private static void sendCostBreakdown(ServerPlayer player, BlockPos from, BlockPos to,
                                          ResourceKey<Level> fromDim, ResourceKey<Level> toDim,
                                          double cost, IPlayerData playerData) {
        boolean isCrossDimension = !fromDim.equals(toDim);
        double baseFee = 20.0;

        player.sendSystemMessage(Component.literal("§7═══════════════════════════════════"));
        player.sendSystemMessage(Component.literal("§7[费用明细]"));

        if (isCrossDimension) {
            player.sendSystemMessage(Component.literal(String.format("§7跨界基费: $%.2f", getCrossDimBaseFee(fromDim))));
            player.sendSystemMessage(Component.literal(String.format("§7终点区域溢价: $%.2f", getDestinationPrice(to))));
        } else {
            double distance = Math.sqrt(from.distSqr(to));
            double rate = getRateForLocation(from, to);
            player.sendSystemMessage(Component.literal(String.format("§7基础费用: $%.2f", baseFee)));
            player.sendSystemMessage(Component.literal(String.format("§7距离: %.0f 格", distance)));
            player.sendSystemMessage(Component.literal(String.format("§7费率: $%.2f/格", rate)));
        }

        double taxMultiplier = TAX_SYSTEM.getMtax(playerData);
        if (taxMultiplier < 1.0) {
            double reliefPercent = (1.0 - taxMultiplier) * 100;
            player.sendSystemMessage(Component.literal(String.format("§a职业/共生减免: -%.0f%%", reliefPercent)));
        }

        player.sendSystemMessage(Component.literal(String.format("§7───────────────────────────────────")));
        player.sendSystemMessage(Component.literal(String.format("§e总计: $%.2f", cost)));
        player.sendSystemMessage(Component.literal(String.format("§7当前余额: $%.2f", playerData.getBalance())));
        player.sendSystemMessage(Component.literal("§7═══════════════════════════════════"));
    }

    private static double getCrossDimBaseFee(ResourceKey<Level> fromDim) {
        if (fromDim.equals(Level.NETHER)) {
            return com.symbioticlaw.Config.CROSS_DIM_BASE_FEE_NETHER.get();
        } else if (fromDim.equals(Level.END)) {
            return com.symbioticlaw.Config.CROSS_DIM_BASE_FEE_END.get();
        } else {
            return com.symbioticlaw.Config.CROSS_DIM_BASE_FEE_MODDED.get();
        }
    }

    private static double getDestinationPrice(BlockPos destination) {
        double distFromCore = Math.sqrt(destination.distSqr(BlockPos.ZERO));
        if (distFromCore <= 30) {
            return 0;
        } else if (distFromCore <= 2000) {
            return 200;
        } else if (distFromCore <= 5000) {
            return 800;
        } else {
            return 1500;
        }
    }

    private static double getRateForLocation(BlockPos from, BlockPos to) {
        double maxRate = 0.0;
        for (BlockPos loc : new BlockPos[]{from, to}) {
            double distFromCore = Math.sqrt(loc.distSqr(BlockPos.ZERO));
            double rate;
            if (distFromCore > 5000) {
                rate = com.symbioticlaw.Config.MILEAGE_RATE_TIER3.get();
            } else if (distFromCore > 2000) {
                rate = com.symbioticlaw.Config.MILEAGE_RATE_TIER2.get();
            } else {
                rate = com.symbioticlaw.Config.MILEAGE_RATE_TIER1.get();
            }
            maxRate = Math.max(maxRate, rate);
        }
        return maxRate;
    }

    private static void checkDangerZoneWarning(ServerPlayer player) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            double distanceToCore = Math.sqrt(player.distanceToSqr(BlockPos.ZERO.getX(), player.getY(), BlockPos.ZERO.getZ()));
            if (distanceToCore > 5000 && playerData.getClassTier() < 2) {
                player.sendSystemMessage(Component.literal("§c[⚠ 危险区域警告] 您已进入蛮荒区。请确保余额充足以支付回程费用。"));
            }
        });
    }
}