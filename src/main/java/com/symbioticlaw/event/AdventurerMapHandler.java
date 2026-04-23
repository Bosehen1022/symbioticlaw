package com.symbioticlaw.event;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.JobType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Symbioticlaw.MODID)
public class AdventurerMapHandler {

    private static final int CHECK_INTERVAL = 20;

    private static final Set<String> EXPLORER_MAP_KEYWORDS = new HashSet<>();
    private static final Set<String> TREASURE_MAP_KEYWORDS = new HashSet<>();

    static {
        String[] explorerKeywords = {"explorer", "探险", "林业", "ocean", "woodland", "monument", "mansion", "探险家"};
        String[] treasureKeywords = {"treasure", "buried", "藏宝", "埋藏", "bounty", "loot"};

        for (String keyword : explorerKeywords) {
            EXPLORER_MAP_KEYWORDS.add(keyword.toLowerCase());
        }
        for (String keyword : treasureKeywords) {
            TREASURE_MAP_KEYWORDS.add(keyword.toLowerCase());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL != 0) return;

        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            if (isAdventurer(playerData)) {
                return;
            }

            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            boolean holdingMap = isExplorerMap(mainHand) || isExplorerMap(offHand) ||
                               isTreasureMap(mainHand) || isTreasureMap(offHand);

            if (holdingMap) {
                applyMapEffects(player);

                if (player.tickCount % (CHECK_INTERVAL * 5) == 0) {
                    player.sendSystemMessage(Component.literal("§c[🚫 精神力不足] 您无法解析这张古老的地图。只有冒险家能读懂这些坐标的含义。"));
                }
            }
        });
    }

    private static boolean isAdventurer(IPlayerData playerData) {
        int professionId = playerData.getProfessionId();
        return professionId == JobType.ADVENTURER.getNumericId();
    }

    private static boolean isExplorerMap(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(Items.FILLED_MAP)) {
            return false;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return false;
        }

        if (tag.contains("display")) {
            String name = tag.getCompound("display").getString("Name").toLowerCase();
            for (String keyword : EXPLORER_MAP_KEYWORDS) {
                if (name.contains(keyword)) {
                    return true;
                }
            }
        }

        if (tag.contains("maps", 9)) {
            ListTag maps = tag.getList("maps", 10);
            for (int i = 0; i < maps.size(); i++) {
                String mapId = maps.getString(i);
                for (String keyword : EXPLORER_MAP_KEYWORDS) {
                    if (mapId.toLowerCase().contains(keyword)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean isTreasureMap(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(Items.FILLED_MAP)) {
            return false;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return false;
        }

        if (tag.contains("display")) {
            String name = tag.getCompound("display").getString("Name").toLowerCase();
            for (String keyword : TREASURE_MAP_KEYWORDS) {
                if (name.contains(keyword)) {
                    return true;
                }
            }
        }

        if (tag.contains("maps", 9)) {
            ListTag maps = tag.getList("maps", 10);
            for (int i = 0; i < maps.size(); i++) {
                String mapId = maps.getString(i);
                for (String keyword : TREASURE_MAP_KEYWORDS) {
                    if (mapId.toLowerCase().contains(keyword)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static void applyMapEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN,
            60,
            1,
            false,
            true,
            true
        ));

        player.addEffect(new MobEffectInstance(
            MobEffects.BLINDNESS,
            40,
            0,
            false,
            true,
            true
        ));
    }
}