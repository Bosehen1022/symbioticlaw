package com.symbioticlaw.event;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.system.AffinitySystem;
import com.symbioticlaw.system.BorderSystem;
import com.symbioticlaw.system.ClassSystem;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.system.ReportSystem;
import com.symbioticlaw.system.TaxSystem;
import com.symbioticlaw.system.ExileSystem;
import com.symbioticlaw.system.VisaSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Symbioticlaw.MODID)
public class TickHandler {

    private static final ClassSystem classSystem = new ClassSystem();
    private static final BorderSystem borderSystem = new BorderSystem();
    private static final AffinitySystem affinitySystem = new AffinitySystem();
    private static final ReportSystem reportSystem = new ReportSystem();
    private static final TaxSystem taxSystem = new TaxSystem();
    private static final com.symbioticlaw.system.SubliminalSystem subliminalSystem = new com.symbioticlaw.system.SubliminalSystem();
    private static final com.symbioticlaw.system.HudDataSystem hudDataSystem = new com.symbioticlaw.system.HudDataSystem();
    private static final com.symbioticlaw.system.DailyQuotaSystem dailyQuotaSystem = new com.symbioticlaw.system.DailyQuotaSystem();
    private static final VisaSystem visaSystem = new VisaSystem();
    private static final ExileSystem exileSystem = new ExileSystem();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MinecraftServer server = event.getServer();
            if (server.overworld() == null) return;
            long gameTime = server.overworld().getGameTime();

            server.getPlayerList().getPlayers().forEach(player -> {
                player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                    playerData.setOnlineTickCount(playerData.getOnlineTickCount() + 1);
                    if (playerData.getOnlineTickCount() >= 24000) {
                        playerData.setOnlineTickCount(0);
                        taxSystem.applyPeriodicTax(player, playerData);
                    }
                    playerData.setIdleTicks(playerData.getIdleTicks() + 1);
                    subliminalSystem.tick(player, playerData);
                    hudDataSystem.tick(player);
                });
            });

            // Schedule every tick for border checks
            if (gameTime % 20 == 0) { // Every second
                borderSystem.tick(server);
            }

            // Schedule for ClassSystem (and later others)
            if (gameTime % 200 == 0) { // Every 10 seconds
                classSystem.tick(server);
            }

            if (gameTime % 1200 == 0) { // Every 60 seconds
                affinitySystem.tick(server);
                visaSystem.tick(server);
                exileSystem.tick(server);
            }

            if (gameTime % 24000 == 0) { // Every 20 minutes (1 Minecraft day)
                reportSystem.tick(server);
                //dailyQuotaSystem.tick(server.overworld());
            }
        }
    }
}
