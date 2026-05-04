package com.symbioticlaw.system;

import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.JobType;
import com.symbioticlaw.professions.Profession;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class DailyQuotaSystem {

    private static final Random RANDOM = new Random();
    
    // Miner daily quota options: item -> required amount
    private static final Map<String, Integer> MINER_QUOTA_OPTIONS = new HashMap<>();
    // Farmer daily quota options
    private static final Map<String, Integer> FARMER_QUOTA_OPTIONS = new HashMap<>();
    // Angler daily quota options
    private static final Map<String, Integer> ANGLER_QUOTA_OPTIONS = new HashMap<>();
    // Adventurer daily quota options
    private static final Map<String, Integer> ADVENTURER_QUOTA_OPTIONS = new HashMap<>();
    // Blacksmith daily quota options
    private static final Map<String, Integer> BLACKSMITH_QUOTA_OPTIONS = new HashMap<>();
    
    static {
        // Miner quotas
        MINER_QUOTA_OPTIONS.put("minecraft:coal", 32);
        MINER_QUOTA_OPTIONS.put("minecraft:copper_ingot", 16);
        MINER_QUOTA_OPTIONS.put("minecraft:iron_ingot", 16);
        MINER_QUOTA_OPTIONS.put("minecraft:gold_ingot", 8);
        MINER_QUOTA_OPTIONS.put("create:zinc_ingot", 16);
        MINER_QUOTA_OPTIONS.put("create:brass_ingot", 8);
        MINER_QUOTA_OPTIONS.put("minecraft:diamond", 4);
        MINER_QUOTA_OPTIONS.put("minecraft:emerald", 8);
        
        // Farmer quotas
        FARMER_QUOTA_OPTIONS.put("minecraft:oak_log", 64);
        FARMER_QUOTA_OPTIONS.put("minecraft:birch_log", 64);
        FARMER_QUOTA_OPTIONS.put("minecraft:spruce_log", 64);
        FARMER_QUOTA_OPTIONS.put("minecraft:wheat", 128);
        FARMER_QUOTA_OPTIONS.put("minecraft:carrot", 128);
        FARMER_QUOTA_OPTIONS.put("minecraft:potato", 128);
        FARMER_QUOTA_OPTIONS.put("farmersdelight:tomato", 128);
        FARMER_QUOTA_OPTIONS.put("farmersdelight:cabbage", 64);
        FARMER_QUOTA_OPTIONS.put("minecraft:porkchop", 32);
        FARMER_QUOTA_OPTIONS.put("minecraft:beef", 32);
        FARMER_QUOTA_OPTIONS.put("minecraft:chicken", 32);
        
        // Angler quotas (Daily Salvage)
        ANGLER_QUOTA_OPTIONS.put("minecraft:ink_sac", 32);
        ANGLER_QUOTA_OPTIONS.put("minecraft:pufferfish", 8);
        ANGLER_QUOTA_OPTIONS.put("minecraft:prismarine_shard", 16);
        ANGLER_QUOTA_OPTIONS.put("minecraft:prismarine_crystals", 16);
        ANGLER_QUOTA_OPTIONS.put("minecraft:nautilus_shell", 2);
        
        // Adventurer quotas (Daily Expedition Contract)
        ADVENTURER_QUOTA_OPTIONS.put("minecraft:bone", 64);
        ADVENTURER_QUOTA_OPTIONS.put("minecraft:gunpowder", 32);
        ADVENTURER_QUOTA_OPTIONS.put("minecraft:slime_ball", 16);
        ADVENTURER_QUOTA_OPTIONS.put("minecraft:phantom_membrane", 8);
        ADVENTURER_QUOTA_OPTIONS.put("minecraft:blaze_rod", 16);
        ADVENTURER_QUOTA_OPTIONS.put("minecraft:ender_pearl", 16);
        ADVENTURER_QUOTA_OPTIONS.put("minecraft:ghast_tear", 4);
        
        // Blacksmith quotas (Daily Salvage Contract)
        BLACKSMITH_QUOTA_OPTIONS.put("minecraft:iron_sword", 8);
        BLACKSMITH_QUOTA_OPTIONS.put("minecraft:iron_helmet", 4);
        BLACKSMITH_QUOTA_OPTIONS.put("minecraft:iron_chestplate", 2);
        BLACKSMITH_QUOTA_OPTIONS.put("minecraft:iron_leggings", 3);
        BLACKSMITH_QUOTA_OPTIONS.put("minecraft:iron_boots", 4);
        BLACKSMITH_QUOTA_OPTIONS.put("minecraft:shield", 4);
        BLACKSMITH_QUOTA_OPTIONS.put("minecraft:bow", 6);
        BLACKSMITH_QUOTA_OPTIONS.put("minecraft:diamond_pickaxe", 2);
        BLACKSMITH_QUOTA_OPTIONS.put("minecraft:iron_pickaxe", 6);
        BLACKSMITH_QUOTA_OPTIONS.put("minecraft:iron_axe", 6);
    }
    
    private static final int TICKS_PER_DAY = 24000; // 20 minutes

    /**
     * Refresh daily quota for a player if needed
     */
    public static void checkAndRefreshQuota(ServerPlayer player) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            Profession profession = playerData.getProfession();
            if (profession != Profession.MINER && profession != Profession.FARMER && 
                profession != Profession.ANGLER && profession != Profession.ADVENTURER &&
                profession != Profession.BLACKSMITH) {
                return; // Only miners, farmers, anglers, adventurers and blacksmiths have daily quotas
            }
            
            long currentTime = player.level().getGameTime();
            long timeSinceLastCompletion = currentTime - playerData.getDailyQuotaCompletionTime();
            
            // Check if quota needs refresh (completed and a day has passed, or never had a quota)
            if (playerData.getDailyQuotaItem().isEmpty() || 
                (playerData.isDailyQuotaCompleted() && timeSinceLastCompletion > TICKS_PER_DAY)) {
                refreshQuota(player, profession);
            }
        });
    }
    
    /**
     * Generate a new daily quota for the player
     */
    public static void refreshQuota(ServerPlayer player, Profession profession) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            Map<String, Integer> options;
            double reward;
            
            if (profession == Profession.MINER) {
                options = MINER_QUOTA_OPTIONS;
                reward = 50.0 + RANDOM.nextInt(51); // $50 - $100
            } else if (profession == Profession.FARMER) {
                options = FARMER_QUOTA_OPTIONS;
                reward = 50.0 + RANDOM.nextInt(51); // $50 - $100
            } else if (profession == Profession.ANGLER) {
                options = ANGLER_QUOTA_OPTIONS;
                reward = 50.0 + RANDOM.nextInt(31); // $50 - $80 for anglers
            } else if (profession == Profession.ADVENTURER) {
                options = ADVENTURER_QUOTA_OPTIONS;
                reward = 500.0 + RANDOM.nextInt(501); // $500 - $1000 for adventurers
            } else {
                options = BLACKSMITH_QUOTA_OPTIONS;
                reward = 80.0 + RANDOM.nextInt(71); // $80 - $150 for blacksmiths
            }
            
            // Select random item from options
            List<String> items = new ArrayList<>(options.keySet());
            String selectedItem = items.get(RANDOM.nextInt(items.size()));
            int requiredAmount = options.get(selectedItem);
            
            playerData.setDailyQuotaItem(selectedItem);
            playerData.setDailyQuotaRequiredAmount(requiredAmount);
            playerData.setDailyQuotaProgress(0);
            playerData.setDailyQuotaReward(reward);
            
            String itemName = selectedItem;
            ResourceLocation itemRl = ResourceLocation.tryParse(selectedItem);
            net.minecraft.world.item.Item item = itemRl != null ? ForgeRegistries.ITEMS.getValue(itemRl) : null;
            if (item != null) {
                itemName = item.getDescription().getString();
            }
                }
            } catch (Exception e) {
                // Use raw ID if name lookup fails
            }
                    }
            } catch (Exception e) {
                // Use raw ID if name lookup fails
            }
            
            String professionName;
            if (profession == Profession.MINER) {
                professionName = "矿工";
            } else if (profession == Profession.FARMER) {
                professionName = "农夫";
            } else if (profession == Profession.ANGLER) {
                professionName = "渔夫";
            } else if (profession == Profession.ADVENTURER) {
                professionName = "冒险家";
            } else {
                professionName = "铁匠";
            }
            player.sendSystemMessage(Component.literal(
                String.format("§e[每日定额] 新的%s悬赏已发布！需求: %d x %s，奖励: $%.0f", 
                    professionName, requiredAmount, itemName, reward)));
        });
    }
    
    /**
     * Turn in items for daily quota
     */
    public static void turnInQuota(ServerPlayer player) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            String quotaItem = playerData.getDailyQuotaItem();
            
            if (quotaItem.isEmpty()) {
                player.sendSystemMessage(Component.literal("§c你没有活动的每日定额。"));
                return;
            }
            
            if (playerData.isDailyQuotaCompleted()) {
                player.sendSystemMessage(Component.literal("§a你已经完成了今日的定额。"));
                return;
            }
            
            int requiredAmount = playerData.getDailyQuotaRequiredAmount();
            int currentProgress = playerData.getDailyQuotaProgress();
            int needed = requiredAmount - currentProgress;
            
            // Count items in inventory
            Inventory inventory = player.getInventory();
            int itemsFound = 0;
            
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (!stack.isEmpty()) {
                    ResourceLocation stackId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (stackId != null && stackId.toString().equals(quotaItem)) {
                        itemsFound += stack.getCount();
                    }
                }
            }
            
            if (itemsFound <= 0) {
                player.sendSystemMessage(Component.literal("§c你没有任何定额物品可提交。"));
                return;
            }
            
            // Take items
            int toTake = Math.min(itemsFound, needed);
            int remaining = toTake;
            
            for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
                ItemStack stack = inventory.getItem(i);
                if (!stack.isEmpty()) {
                    ResourceLocation stackId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (stackId != null && stackId.toString().equals(quotaItem)) {
                        if (stack.getCount() <= remaining) {
                            remaining -= stack.getCount();
                            inventory.setItem(i, ItemStack.EMPTY);
                        } else {
                            stack.shrink(remaining);
                            remaining = 0;
                        }
                    }
                }
            }
            
            // Update progress
            int newProgress = currentProgress + toTake;
            playerData.setDailyQuotaProgress(newProgress);
            
            String itemName = quotaItem;
            ResourceLocation itemRl2 = ResourceLocation.tryParse(quotaItem);
            net.minecraft.world.item.Item item2 = itemRl2 != null ? ForgeRegistries.ITEMS.getValue(itemRl2) : null;
            if (item2 != null) {
                itemName = item2.getDescription().getString();
            }
            
            if (newProgress >= requiredAmount) {
                // Quota completed!
                double reward = playerData.getDailyQuotaReward();
                playerData.addBalance(reward);
                playerData.setDailyQuotaCompletionTime(player.level().getGameTime());
                
                // Award bonus XP based on profession
                if (playerData.getProfession() == Profession.MINER) {
                    playerData.addProfessionXp(Profession.MINER, 50 + RANDOM.nextInt(51)); // 50-100 XP
                } else if (playerData.getProfession() == Profession.FARMER) {
                    playerData.addProfessionXp(Profession.FARMER, 50 + RANDOM.nextInt(51));
                } else if (playerData.getProfession() == Profession.ANGLER) {
                    playerData.addProfessionXp(Profession.ANGLER, 50 + RANDOM.nextInt(51)); // 50-100 XP for angler daily
                } else if (playerData.getProfession() == Profession.ADVENTURER) {
                    playerData.addProfessionXp(Profession.ADVENTURER, 50 + RANDOM.nextInt(51)); // 50-100 XP for adventurer daily
                } else if (playerData.getProfession() == Profession.BLACKSMITH) {
                    playerData.addProfessionXp(Profession.BLACKSMITH, 50 + RANDOM.nextInt(51)); // 50-100 XP for blacksmith daily
                }
                
                player.sendSystemMessage(Component.literal(
                    String.format("§a[✔] 每日定额完成！获得 $%.2f 奖金和额外经验。", reward)));
            } else {
                player.sendSystemMessage(Component.literal(
                    String.format("§e已提交 %d x %s，进度: %d/%d", 
                        toTake, itemName, newProgress, requiredAmount)));
            }
        });
    }
    
    /**
     * Get quota status message for display
     */
    public static String getQuotaStatus(ServerPlayer player) {
        final String[] result = {""};
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            String quotaItem = playerData.getDailyQuotaItem();
            if (quotaItem.isEmpty()) {
                result[0] = "§7暂无每日定额";
            } else if (playerData.isDailyQuotaCompleted()) {
                result[0] = "§a今日定额已完成";
            } else {
                String itemName = quotaItem;
                ResourceLocation itemRl2 = ResourceLocation.tryParse(quotaItem);
                net.minecraft.world.item.Item item2 = itemRl2 != null ? ForgeRegistries.ITEMS.getValue(itemRl2) : null;
                if (item2 != null) {
                    itemName = item2.getDescription().getString();
                }
                result[0] = String.format("§e%d/%d %s", 
                    playerData.getDailyQuotaProgress(), 
                    playerData.getDailyQuotaRequiredAmount(),
                    itemName);
            }
        });
        return result[0];
    }
}
