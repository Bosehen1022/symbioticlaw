package com.symbioticlaw.event;

import com.symbioticlaw.Symbioticlaw;
import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.JobType;
import com.symbioticlaw.professions.BlacksmithProfessionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * 铁匠价格预览处理器
 * 
 * 规格书要求:
 * - 铁匠玩家：鼠标悬停在任何武器/防具上时，显示 §a[⚖ 铁匠回收价: $XXX]
 * - 非铁匠玩家：无价格预览
 * 
 * 这是客户端事件处理器 (Client-side only)
 */
@Mod.EventBusSubscriber(modid = Symbioticlaw.MODID, value = Dist.CLIENT)
public class BlacksmithTooltipHandler {
    
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        
        // 只处理装备类物品（武器、防具、工具）
        if (!isEquipment(stack)) return;
        
        // 获取玩家数据（客户端缓存）
        var player = event.getEntity();
        if (player == null) return;
        
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            // 检查是否是铁匠职业
            if (isBlacksmith(playerData)) {
                // 铁匠显示价格预览
                double price = BlacksmithProfessionManager.getBasePrice(stack);
                
                // 应用等级加成
                int level = playerData.getProfessionLevel(playerData.getProfession());
                double bonus = BlacksmithProfessionManager.getPriceBonus(level);
                price *= (1 + bonus);
                
                List<Component> tooltip = event.getToolTip();
                
                // 插入价格预览（在物品名称之后）
                if (tooltip.size() > 1) {
                    tooltip.add(1, Component.literal(""));
                    tooltip.add(2, Component.literal(String.format("§a[⚖ 铁匠回收价: $%.2f]", price)));
                    
                    // 如果是Tetra物品，显示特殊标记
                    if (BlacksmithProfessionManager.isTetraItem(stack)) {
                        tooltip.add(3, Component.literal("§b[Tetra模块化装备 - 高级回收]"));
                    }
                    
                    // 显示耐久度影响
                    if (stack.isDamageableItem()) {
                        double durabilityRatio = 1.0 - ((double) stack.getDamageValue() / stack.getMaxDamage());
                        if (durabilityRatio < 0.5) {
                            tooltip.add(4, Component.literal("§e[⚠ 耐久度低 - 回收价降低]"));
                        }
                    }
                }
            }
            // 非铁匠不显示任何价格信息
        });
    }
    
    /**
     * 检查玩家是否是铁匠
     */
    private static boolean isBlacksmith(IPlayerData playerData) {
        int professionId = playerData.getProfessionId();
        return professionId == JobType.BLACKSMITH.getNumericId();
    }
    
    /**
     * 检查物品是否是装备类
     */
    private static boolean isEquipment(ItemStack stack) {
        // 检查物品是否是武器、防具或工具
        String itemName = stack.getItem().getDescriptionId().toLowerCase();
        
        // 武器类
        if (itemName.contains("sword") || itemName.contains("axe") || 
            itemName.contains("pickaxe") || itemName.contains("shovel") ||
            itemName.contains("hoe") || itemName.contains("bow") ||
            itemName.contains("crossbow") || itemName.contains("trident")) {
            return true;
        }
        
        // 防具类
        if (itemName.contains("helmet") || itemName.contains("chestplate") ||
            itemName.contains("leggings") || itemName.contains("boots") ||
            itemName.contains("shield")) {
            return true;
        }
        
        // 工具类
        if (itemName.contains("flint_and_steel") || itemName.contains("shears") ||
            itemName.contains("fishing_rod") || itemName.contains("carrot_on_a_stick") ||
            itemName.contains("warped_fungus_on_a_stick")) {
            return true;
        }
        
        // 检查是否是Tetra物品（通过NBT或类名）
        if (BlacksmithProfessionManager.isTetraItem(stack)) {
            return true;
        }
        
        return false;
    }
}
