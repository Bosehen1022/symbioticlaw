
package com.symbioticlaw.system;

import com.symbioticlaw.professions.AdventurerProfessionManager;
import com.symbioticlaw.professions.AnglerProfessionManager;
import com.symbioticlaw.professions.BlacksmithProfessionManager;
import com.symbioticlaw.professions.FarmerProfessionManager;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.core.ModTags;
import com.symbioticlaw.data.JobType;
import com.symbioticlaw.data.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ProfessionSystem {

    private static final Map<String, Integer> MINER_XP_TIERS = new HashMap<>();
    private static final Map<String, Integer> CHEF_XP_TIERS = new HashMap<>();
    private static final Map<String, Integer> QUOTA_OPTIONS = new HashMap<>();
    private static final Random RANDOM = new Random();

    static {
        // Miner Tiers
        MINER_XP_TIERS.put("minecraft:coal", 1);
        MINER_XP_TIERS.put("minecraft:copper_ingot", 2);
        MINER_XP_TIERS.put("minecraft:iron_ingot", 5);
        MINER_XP_TIERS.put("minecraft:gold_ingot", 5);
        MINER_XP_TIERS.put("minecraft:diamond", 20);
        MINER_XP_TIERS.put("minecraft:emerald", 20);
        MINER_XP_TIERS.put("minecraft:netherite_scrap", 100);
        MINER_XP_TIERS.put("minecraft:netherite_ingot", 100);

        // Chef Tiers (The Menu Matrix)
        // Tier 1 (快餐/配给) - 5 XP
        CHEF_XP_TIERS.put("farmersdelight:stuffed_potato", 5);
        CHEF_XP_TIERS.put("farmersdelight:hamburger", 5);
        CHEF_XP_TIERS.put("farmersdelight:bacon_and_eggs", 5);
        CHEF_XP_TIERS.put("farmersdelight:pasta_with_meatballs", 5);
        CHEF_XP_TIERS.put("minecraft:pumpkin_pie", 5);

        // Tier 2 (精制甜点) - 8 XP
        CHEF_XP_TIERS.put("minecraft:cake", 8);
        CHEF_XP_TIERS.put("create:sweet_roll", 8);

        // Tier 3 (盛宴/大餐) - 25 XP
        CHEF_XP_TIERS.put("farmersdelight:roast_chicken_block", 25);
        CHEF_XP_TIERS.put("farmersdelight:shepherds_pie_block", 25);
        CHEF_XP_TIERS.put("farmersdelight:honey_glazed_ham_block", 25);
        CHEF_XP_TIERS.put("farmersdelight:stuffed_pumpkin_block", 25);

        // Tier 4 (创造特供) - 5 XP
        CHEF_XP_TIERS.put("create:bar_of_chocolate", 5);
        CHEF_XP_TIERS.put("create:honeyed_apple", 5);

        // Daily Quota Options
        QUOTA_OPTIONS.put("minecraft:oak_log", 64);
        QUOTA_OPTIONS.put("farmersdelight:tomato", 128);
        QUOTA_OPTIONS.put("minecraft:porkchop", 32);
        QUOTA_OPTIONS.put("minecraft:carrot", 128);
        QUOTA_OPTIONS.put("minecraft:potato", 128);
        QUOTA_OPTIONS.put("minecraft:wheat", 256);
    }

    public void addMinerXp(ServerPlayer player, String itemId, int amount) {
        if (!isMinerItem(itemId)) {
            return;
        }

        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            if (playerData.getProfession().toJobType() != JobType.MINER) {
                return;
            }

            int xpToAdd = MINER_XP_TIERS.getOrDefault(itemId, 0) * amount;
            if (xpToAdd > 0) {
                double currentXp = playerData.getProfessionXp(com.symbioticlaw.professions.Profession.MINER);
                int currentLevel = playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.MINER);
                double newXp = currentXp + xpToAdd;

                long xpForNextLevel = getXpForLevel(JobType.MINER, currentLevel + 1);

                while (newXp >= xpForNextLevel) {
                    currentLevel++;
                    newXp -= xpForNextLevel;
                    xpForNextLevel = getXpForLevel(JobType.MINER, currentLevel + 1);
                    playerData.setProfessionLevel(com.symbioticlaw.professions.Profession.MINER, currentLevel);
                    player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                    player.sendSystemMessage(Component.literal("§e[⚑ 晋升] 您的矿工等级提升至 Lv." + currentLevel + "！收购加成提升。"));
                }

                playerData.setProfessionXp(com.symbioticlaw.professions.Profession.MINER, newXp);

                if (currentLevel == 50) {
                    // Broadcast to server
                    String playerName = player.getGameProfile().getName();
                    Component message = Component.literal("§b[♛ 传奇] 玩家 " + playerName + " 已加冕为【资源大亨 (Lv.50)】。他掌握了地壳的秘密。");
                    player.getServer().getPlayerList().broadcastSystemMessage(message, false);
                }
            }
        });
    }

    public static long getXpForLevel(JobType job, int level) {
        if (level <= 1) {
            switch (job) {
                case MINER: return 100;
                case FARMER: return 150;
                case CHEF: return 200;
                case ANGLER: return 150;
                case ADVENTURER: return 100;
                case BLACKSMITH: return 120;
                default: return 100;
            }
        }
        switch (job) {
            case MINER: return (long) (100 * Math.pow(level, 2.5));
            case FARMER: return (long) (150 * Math.pow(level, 2.6));
            case CHEF: return (long) (200 * Math.pow(level, 2.8));
            case ANGLER: return (long) (150 * Math.pow(level, 2.8)); // High curve to prevent AFK leveling
            case ADVENTURER: return (long) (100 * Math.pow(level, 2.5)); // Standard curve
            case BLACKSMITH: return (long) (120 * Math.pow(level, 2.6)); // Moderate curve
            default: return (long) (100 * Math.pow(level, 2.5));
        }
    }

    public double getMinerPriceBonus(int level) {
        if (level >= 46) {
            return 0.15;
        } else if (level >= 31) {
            return 0.10;
        } else if (level >= 11) {
            return 0.05;
        } else {
            return 0.0;
        }
    }

    public boolean isMinerItem(String itemId) {
        return MINER_XP_TIERS.containsKey(itemId) || isMinerCompressedItem(itemId);
    }

    public boolean isMinerCompressedItem(String itemId) {
        return itemId.endsWith("_block") && (itemId.contains("iron") || itemId.contains("gold") || itemId.contains("diamond") || itemId.contains("netherite") || itemId.contains("copper"));
    }

    // ================== FARMER / GROWER ==================

    public void addFarmerXp(ServerPlayer player, ItemStack stack, int amount) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            if (playerData.getProfession().toJobType() != JobType.FARMER) {
                return;
            }

            FarmerProfessionManager.FarmerItemCategory category = FarmerProfessionManager.getCategoryFor(stack);

            if (category != null) {
                // Calculate XP using the new system
                double xpPerItem = FarmerProfessionManager.getXpPerItem(category);
                double xpToAdd = xpPerItem * amount;
                
                // Special handling for processed items
                if (category == FarmerProfessionManager.FarmerItemCategory.PROCESSED) {
                    if (playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.FARMER) < 20) {
                        player.sendSystemMessage(Component.literal("§c[!] 等级不足：需要农夫等级达到20级才能出售加工品。"));
                        return;
                    }
                }

                if (xpToAdd > 0) {
                    double currentXp = playerData.getProfessionXp(com.symbioticlaw.professions.Profession.FARMER);
                    int currentLevel = playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.FARMER);
                    double newXp = currentXp + xpToAdd;

                    long xpForNextLevel = getXpForLevel(JobType.FARMER, currentLevel + 1);

                    boolean leveledUp = false;
                    while (newXp >= xpForNextLevel && currentLevel < 50) {
                        currentLevel++;
                        newXp -= xpForNextLevel;
                        playerData.setProfessionLevel(com.symbioticlaw.professions.Profession.FARMER, currentLevel);
                        leveledUp = true;
                        xpForNextLevel = getXpForLevel(JobType.FARMER, currentLevel + 1);
                    }
                    
                    // Play level up sound and message only if leveled up
                    if (leveledUp) {
                        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                        player.sendSystemMessage(Component.literal("§a[🌾 丰收] 您的农夫等级提升至 Lv." + currentLevel + "！收购加成提升。"));
                        
                        // Broadcast at level 50
                        if (currentLevel == 50) {
                            String playerName = player.getGameProfile().getName();
                            Component message = Component.literal("§b[♛ 传奇] 玩家 " + playerName + " 已加冕为【生命编织者 (Lv.50)】。他滋养着这片土地。");
                            player.getServer().getPlayerList().broadcastSystemMessage(message, false);
                        }
                    }

                    playerData.setProfessionXp(com.symbioticlaw.professions.Profession.FARMER, newXp);
                    playerData.addTotalItemsSold(com.symbioticlaw.professions.Profession.FARMER, amount);
                }
            }
        });
    }



    public double getFarmerPriceBonus(int level) {
        if (level >= 46) return 0.15; // 生命编织者 (Weaver)
        if (level >= 31) return 0.10; // 大地主 (Landlord)
        if (level >= 11) return 0.05; // 庄园主 (Planter)
        return 0.0;  // 佃农 (Peasant)
    }

    public boolean isFarmerItem(ItemStack stack) {
        return FarmerProfessionManager.isFarmerItem(stack);
    }

    public boolean isFarmerProcessedItem(ItemStack stack) {
        return FarmerProfessionManager.getCategoryFor(stack) == FarmerProfessionManager.FarmerItemCategory.PROCESSED;
    }

    // ================== CHEF / COOK ==================

    public void addChefXp(ServerPlayer player, ItemStack stack, int amount) {
        String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        if (!isChefItem(itemId)) {
            return;
        }

        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            if (playerData.getProfession().toJobType() != JobType.CHEF) {
                return;
            }

            int xpToAdd = CHEF_XP_TIERS.getOrDefault(itemId, 0) * amount;
            if (xpToAdd > 0) {
                double currentXp = playerData.getProfessionXp(com.symbioticlaw.professions.Profession.CHEF);
                int currentLevel = playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.CHEF);
                double newXp = currentXp + xpToAdd;

                long xpForNextLevel = getXpForLevel(JobType.CHEF, currentLevel + 1);

                while (newXp >= xpForNextLevel && currentLevel < 50) {
                    currentLevel++;
                    newXp -= xpForNextLevel;
                    playerData.setProfessionLevel(com.symbioticlaw.professions.Profession.CHEF, currentLevel);
                    player.level().playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0f, 1.0f);
                    player.sendSystemMessage(Component.literal("§b[🍲 烹饪] 您的厨师等级提升至 Lv." + currentLevel + "！收购加成提升。"));
                    xpForNextLevel = getXpForLevel(JobType.CHEF, currentLevel + 1);
                }

                playerData.setProfessionXp(com.symbioticlaw.professions.Profession.CHEF, newXp);
                playerData.addTotalItemsSold(com.symbioticlaw.professions.Profession.CHEF, amount);
            }
        });
    }



    public double getChefPriceBonus(int level) {
        if (level >= 46) return 0.15; // 味觉大师
        if (level >= 31) return 0.10; // 膳食官
        if (level >= 11) return 0.05; // 主厨
        return 0.0;  // 帮厨
    }

    public boolean isChefItem(String itemId) {
        return CHEF_XP_TIERS.containsKey(itemId);
    }
    
    // ================== DAILY QUOTA ==================

    public void refreshDailyQuota(ServerPlayer player) {
//        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
//            if (playerData.getProfession().toJobType() == JobType.FARMER) {
//                Object[] items = QUOTA_OPTIONS.keySet().toArray();
//                String randomItem = (String) items[RANDOM.nextInt(items.length)];
//                int requiredAmount = QUOTA_OPTIONS.get(randomItem);
//                double reward = (50 + RANDOM.nextInt(51)); // Random reward between 50 and 100
//
//                playerData.setDailyQuotaItem(randomItem);
//                playerData.setDailyQuotaRequiredAmount(requiredAmount);
//                playerData.setDailyQuotaProgress(0);
//                playerData.setDailyQuotaReward(reward);
//                playerData.setDailyQuotaCompletionTime(0); // Reset completion time
//
//                player.sendSystemMessage(Component.literal("§a[i] 新的每日定额已发布！需求: " + requiredAmount + "x " + randomItem));
//            }
//        });
    }

    // Get miner title based on level
    public String getMinerTitle(int level) {
        if (level >= 46) return "资源大亨 (Tycoon)";
        if (level >= 31) return "资深工头 (Foreman)";
        if (level >= 11) return "正式矿工 (Certified)";
        return "实习矿工 (Intern)";
    }

    // Get farmer title based on level
    public String getFarmerTitle(int level) {
        if (level >= 46) return "生命编织者 (Weaver)";
        if (level >= 31) return "大地主 (Landlord)";
        if (level >= 11) return "庄园主 (Planter)";
        return "佃农 (Peasant)";
    }
    
    // Check if player can sell compressed blocks (Lv 20+)
    public boolean canSellCompressedBlocks(int minerLevel) {
        return minerLevel >= 20;
    }
    
    // ================== ANGLER / FISHER ==================
    
    public void addAnglerXp(ServerPlayer player, ItemStack stack, int amount) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            if (playerData.getProfession().toJobType() != JobType.ANGLER) {
                return;
            }

            AnglerProfessionManager.AnglerItemTier tier = AnglerProfessionManager.getTierFor(stack);

            if (tier != null) {
                // Calculate XP using the Anti-AFK Matrix
                double xpPerItem = AnglerProfessionManager.getXpPerItem(tier);
                double xpToAdd = xpPerItem * amount;
                
                // Trash items give no XP
                if (tier == AnglerProfessionManager.AnglerItemTier.TIER_2_TRASH) {
                    return;
                }

                if (xpToAdd > 0) {
                    double currentXp = playerData.getProfessionXp(com.symbioticlaw.professions.Profession.ANGLER);
                    int currentLevel = playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.ANGLER);
                    double newXp = currentXp + xpToAdd;

                    long xpForNextLevel = getXpForLevel(JobType.ANGLER, currentLevel + 1);

                    boolean leveledUp = false;
                    while (newXp >= xpForNextLevel && currentLevel < 50) {
                        currentLevel++;
                        newXp -= xpForNextLevel;
                        playerData.setProfessionLevel(com.symbioticlaw.professions.Profession.ANGLER, currentLevel);
                        leveledUp = true;
                        xpForNextLevel = getXpForLevel(JobType.ANGLER, currentLevel + 1);
                    }
                    
                    // Play level up sound and message only if leveled up
                    if (leveledUp) {
                        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                        player.sendSystemMessage(Component.literal("§b[🎣 打捞] 您的渔夫等级提升至 Lv." + currentLevel + "！收购加成提升。"));
                        
                        // Broadcast at level 50
                        if (currentLevel == 50) {
                            String playerName = player.getGameProfile().getName();
                            Component message = Component.literal("§b[♛ 传奇] 玩家 " + playerName + " 已加冕为【深渊主宰 (Lv.50)】。连海底的怪物都畏惧他的网。");
                            player.getServer().getPlayerList().broadcastSystemMessage(message, false);
                        }
                    }

                    playerData.setProfessionXp(com.symbioticlaw.professions.Profession.ANGLER, newXp);
                    playerData.addTotalItemsSold(com.symbioticlaw.professions.Profession.ANGLER, amount);
                }
            }
        });
    }
    
    public double getAnglerPriceBonus(int level) {
        return AnglerProfessionManager.getPriceBonus(level);
    }

    public boolean isAnglerItem(ItemStack stack) {
        return AnglerProfessionManager.isAnglerItem(stack);
    }

    public String getAnglerTitle(int level) {
        return AnglerProfessionManager.getTitle(level);
    }
    
    // ================== ADVENTURER / EXPLORER ==================
    
    /**
     * Add XP to adventurer based on sale amount
     * Formula: $10 of sale = 1 XP
     */
    public void addAdventurerXp(ServerPlayer player, double saleAmount) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            if (playerData.getProfession().toJobType() != JobType.ADVENTURER) {
                return;
            }

            double xpToAdd = saleAmount / 10.0; // $10 = 1 XP
            
            if (xpToAdd > 0) {
                double currentXp = playerData.getProfessionXp(com.symbioticlaw.professions.Profession.ADVENTURER);
                int currentLevel = playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.ADVENTURER);
                double newXp = currentXp + xpToAdd;

                long xpForNextLevel = getXpForLevel(JobType.ADVENTURER, currentLevel + 1);

                boolean leveledUp = false;
                while (newXp >= xpForNextLevel && currentLevel < 50) {
                    currentLevel++;
                    newXp -= xpForNextLevel;
                    playerData.setProfessionLevel(com.symbioticlaw.professions.Profession.ADVENTURER, currentLevel);
                    leveledUp = true;
                    xpForNextLevel = getXpForLevel(JobType.ADVENTURER, currentLevel + 1);
                }
                
                // Play level up sound and message only if leveled up
                if (leveledUp) {
                    player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                    
                    // Display special effects - ender particles and fire
                    ((ServerLevel) player.level()).sendParticles(
                        net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH,
                        player.getX(), player.getY(), player.getZ(),
                        20, 0.5, 0.5, 0.5, 0.1
                    );
                    
                    double taxRelief = AdventurerProfessionManager.getTaxRelief(currentLevel) * 100;
                    player.sendSystemMessage(Component.literal(
                        String.format("§6[🗺️ 边界突破] 您的开拓等级提升至 Lv.%d！核心已降低您的传送与关税豁免至 %.0f%%.", 
                            currentLevel, taxRelief)));
                    
                    // Broadcast at level 50
                    if (currentLevel == 50) {
                        String playerName = player.getGameProfile().getName();
                        Component message = Component.literal("§b[♛ 传奇] 玩家 " + playerName + " 已加冕为【位面先锋 (Lv.50)】。他征服了所有维度的恐惧。");
                        player.getServer().getPlayerList().broadcastSystemMessage(message, false);
                    }
                }

                playerData.setProfessionXp(com.symbioticlaw.professions.Profession.ADVENTURER, newXp);
            }
        });
    }
    
    public double getAdventurerPriceBonus(int level) {
        return AdventurerProfessionManager.getPriceBonus(level);
    }
    
    public double getAdventurerTaxRelief(int level) {
        return AdventurerProfessionManager.getTaxRelief(level);
    }
    
    public boolean isAdventurerItem(ItemStack stack) {
        return AdventurerProfessionManager.isAdventurerItem(stack);
    }
    
    public String getAdventurerTitle(int level) {
        return AdventurerProfessionManager.getTitle(level);
    }
    
    // ================== BLACKSMITH / SMITH ==================
    
    /**
     * Add XP to blacksmith based on sale amount
     * Formula: $50 of sale = 1 XP
     */
    public void addBlacksmithXp(ServerPlayer player, double saleAmount) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            if (playerData.getProfession().toJobType() != JobType.BLACKSMITH) {
                return;
            }

            double xpToAdd = saleAmount / 50.0; // $50 = 1 XP
            
            if (xpToAdd > 0) {
                double currentXp = playerData.getProfessionXp(com.symbioticlaw.professions.Profession.BLACKSMITH);
                int currentLevel = playerData.getProfessionLevel(com.symbioticlaw.professions.Profession.BLACKSMITH);
                double newXp = currentXp + xpToAdd;

                long xpForNextLevel = getXpForLevel(JobType.BLACKSMITH, currentLevel + 1);

                boolean leveledUp = false;
                while (newXp >= xpForNextLevel && currentLevel < 50) {
                    currentLevel++;
                    newXp -= xpForNextLevel;
                    playerData.setProfessionLevel(com.symbioticlaw.professions.Profession.BLACKSMITH, currentLevel);
                    leveledUp = true;
                    xpForNextLevel = getXpForLevel(JobType.BLACKSMITH, currentLevel + 1);
                }
                
                // Play level up sound and message only if leveled up
                if (leveledUp) {
                    player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
                    player.sendSystemMessage(Component.literal(
                        String.format("§8[⚒️ 熔炉] 您的铁匠等级提升至 Lv.%d！收购加成提升。", currentLevel)));
                    
                    // Broadcast at level 50
                    if (currentLevel == 50) {
                        String playerName = player.getGameProfile().getName();
                        Component message = Component.literal(
                            "§b[♛ 传奇] 玩家 " + playerName + " 已加冕为【火神 (Lv.50)】。他的锻锤能击碎星辰。");
                        player.getServer().getPlayerList().broadcastSystemMessage(message, false);
                    }
                }

                playerData.setProfessionXp(com.symbioticlaw.professions.Profession.BLACKSMITH, newXp);
            }
        });
    }
    
    public double getBlacksmithPriceBonus(int level) {
        return BlacksmithProfessionManager.getPriceBonus(level);
    }
    
    public boolean isBlacksmithItem(ItemStack stack) {
        return BlacksmithProfessionManager.isBlacksmithItem(stack);
    }
    
    public String getBlacksmithTitle(int level) {
        return BlacksmithProfessionManager.getTitle(level);
    }
    
    public boolean canSalvage(ItemStack stack, int level) {
        return BlacksmithProfessionManager.canSalvage(stack, level);
    }
}
