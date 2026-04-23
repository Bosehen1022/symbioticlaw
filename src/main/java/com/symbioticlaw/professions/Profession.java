package com.symbioticlaw.professions;

import net.minecraft.network.chat.Component;

public enum Profession {
    UNEMPLOYED("无业游民", 0.5),
    MINER("矿工", 1.2),
    FARMER("农夫", 1.1),
    CHEF("厨师", 1.15),
    ANGLER("渔夫", 1.1),
    ADVENTURER("冒险家", 1.25),
    BLACKSMITH("铁匠", 1.2);

    private final String displayName;
    private final double incomeModifier;

    Profession(String displayName, double incomeModifier) {
        this.displayName = displayName;
        this.incomeModifier = incomeModifier;
    }

    public Component getDisplayName() {
        return Component.literal(this.displayName);
    }

    public double getIncomeModifier() {
        return this.incomeModifier;
    }

    public com.symbioticlaw.data.JobType toJobType() {
        return com.symbioticlaw.data.JobType.valueOf(this.name());
    }
}
