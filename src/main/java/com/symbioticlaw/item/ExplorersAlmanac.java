package com.symbioticlaw.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ExplorersAlmanac extends Item {
    public ExplorersAlmanac() {
        super(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1));
    }
}
