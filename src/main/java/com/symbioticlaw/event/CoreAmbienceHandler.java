package com.symbioticlaw.event;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.WorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 权力核心环境音效处理器
 * 
 * 规格书要求:
 * - 低频嗡鸣 (The Hum)：进入10格范围播放循环音效
 * - 动态音量：距离越近，声音越响（模拟高压变压器旁的压迫感）
 * - 阶级粒子场：根据最近玩家阶级喷涌不同粒子
 */
@Mod.EventBusSubscriber(modid = Symbioticlaw.MODID)
public class CoreAmbienceHandler {
    
    // 核心作用半径
    private static final double CORE_RANGE = 10.0;
    // 音效播放间隔 (ticks)
    private static final int SOUND_INTERVAL = 100; // 5秒
    // 粒子生成间隔
    private static final int PARTICLE_INTERVAL = 20; // 1秒
    
    // 记录上次播放时间
    private static final Map<UUID, Long> lastSoundTime = new HashMap<>();
    private static final Map<UUID, Long> lastParticleTime = new HashMap<>();
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer().overworld() == null) return;
        
        WorldData worldData = WorldData.get(event.getServer().overworld());
        BlockPos corePos = worldData.getCorePosition();
        
        // 核心未放置
        if (corePos.equals(BlockPos.ZERO)) return;
        
        long currentTime = event.getServer().overworld().getGameTime();
        
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            processPlayerAmbience(player, corePos, currentTime);
        }
    }
    
    private static void processPlayerAmbience(ServerPlayer player, BlockPos corePos, long currentTime) {
        double distance = player.distanceToSqr(corePos.getX() + 0.5, corePos.getY() + 0.5, corePos.getZ() + 0.5);
        
        // 只在10格范围内生效
        if (distance > CORE_RANGE * CORE_RANGE) {
            lastSoundTime.remove(player.getUUID());
            return;
        }
        
        // 计算距离因子 (0.0 - 1.0)，越近越大
        double distanceFactor = 1.0 - (Math.sqrt(distance) / CORE_RANGE);
        
        // 播放环境音效
        playAmbienceSound(player, corePos, distanceFactor, currentTime);
        
        // 生成阶级粒子
        spawnClassParticles(player, corePos, currentTime);
    }
    
    private static void playAmbienceSound(ServerPlayer player, BlockPos corePos, double distanceFactor, long currentTime) {
        UUID playerId = player.getUUID();
        long lastTime = lastSoundTime.getOrDefault(playerId, 0L);
        
        // 检查是否到达播放间隔
        if (currentTime - lastTime < SOUND_INTERVAL) return;
        
        // 计算音量：距离越近，音量越大 (0.1 - 0.8)
        float volume = (float) (0.1 + (distanceFactor * 0.7));
        
        // 计算音调：稍低的音调营造压迫感
        float pitch = 0.5f;
        
        // 播放信标环境音效 (低频嗡鸣)
        player.level().playSound(
            null, // 不指定特定玩家，让附近的人都能听到
            corePos,
            SoundEvents.BEACON_AMBIENT,
            SoundSource.BLOCKS,
            volume,
            pitch
        );
        
        lastSoundTime.put(playerId, currentTime);
    }
    
    private static void spawnClassParticles(ServerPlayer player, BlockPos corePos, long currentTime) {
        UUID playerId = player.getUUID();
        long lastTime = lastParticleTime.getOrDefault(playerId, 0L);
        
        // 检查是否到达生成间隔
        if (currentTime - lastTime < PARTICLE_INTERVAL) return;
        
        // 获取玩家阶级决定粒子类型
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            int classTier = playerData.getClassTier();
            
            // 根据阶级选择粒子效果
            switch (classTier) {
                case -1, 0 -> spawnPariahParticles(player, corePos); // 遗民/奴隶：灰烟
                case 1 -> spawnRestrictedParticles(player, corePos); // 限制公民：电火花
                case 2 -> spawnFreeCitizenParticles(player, corePos); // 自由公民：青色光尘
                default -> spawnDefaultParticles(player, corePos);
            }
        });
        
        lastParticleTime.put(playerId, currentTime);
    }
    
    /**
     * 遗民粒子：灰烟 - 沉闷、污浊
     */
    private static void spawnPariahParticles(ServerPlayer player, BlockPos corePos) {
        // 使用大型烟雾粒子
        ((net.minecraft.server.level.ServerLevel)player.level()).sendParticles(
            net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
            corePos.getX() + 0.5,
            corePos.getY() + 1.0,
            corePos.getZ() + 0.5,
            3, // 数量
            0.5, 0.5, 0.5, // 扩散范围
            0.01 // 速度
        );
    }
    
    /**
     * 限制公民粒子：电火花 - 不稳定、警示
     */
    private static void spawnRestrictedParticles(ServerPlayer player, BlockPos corePos) {
        // 使用电火花粒子
        ((net.minecraft.server.level.ServerLevel)player.level()).sendParticles(
            net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
            corePos.getX() + 0.5,
            corePos.getY() + 1.0,
            corePos.getZ() + 0.5,
            5,
            0.3, 0.3, 0.3,
            0.5
        );
    }
    
    /**
     * 自由公民粒子：青色光尘 - 神圣、秩序
     */
    private static void spawnFreeCitizenParticles(ServerPlayer player, BlockPos corePos) {
        // 使用末地烛粒子 (青色光尘)
        ((net.minecraft.server.level.ServerLevel)player.level()).sendParticles(
            net.minecraft.core.particles.ParticleTypes.END_ROD,
            corePos.getX() + 0.5,
            corePos.getY() + 1.0,
            corePos.getZ() + 0.5,
            4,
            0.4, 0.4, 0.4,
            0.02
        );
    }
    
    private static void spawnDefaultParticles(ServerPlayer player, BlockPos corePos) {
        // 默认使用烟雾
        ((net.minecraft.server.level.ServerLevel)player.level()).sendParticles(
            net.minecraft.core.particles.ParticleTypes.SMOKE,
            corePos.getX() + 0.5,
            corePos.getY() + 1.0,
            corePos.getZ() + 0.5,
            2,
            0.3, 0.3, 0.3,
            0.01
        );
    }
    
    /**
     * 检查玩家是否在核心附近
     */
    public static boolean isNearCore(ServerPlayer player, BlockPos corePos) {
        double distance = player.distanceToSqr(corePos.getX() + 0.5, corePos.getY() + 0.5, corePos.getZ() + 0.5);
        return distance <= CORE_RANGE * CORE_RANGE;
    }
    
    /**
     * 获取距离核心的距离因子 (0.0 - 1.0)
     */
    public static double getDistanceFactor(ServerPlayer player, BlockPos corePos) {
        double distance = Math.sqrt(player.distanceToSqr(corePos.getX() + 0.5, corePos.getY() + 0.5, corePos.getZ() + 0.5));
        if (distance > CORE_RANGE) return 0.0;
        return 1.0 - (distance / CORE_RANGE);
    }
}
