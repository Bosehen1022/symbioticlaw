package com.symbioticlaw.data;

/**
 * 职业类型枚举
 */
public enum JobType {
    UNEMPLOYED(0, "unemployed", "无业"),
    MINER(1, "miner", "矿工"),
    FARMER(2, "farmer", "农民"),
    CHEF(3, "chef", "厨师"),
    ANGLER(4, "angler", "渔夫"),
    ADVENTURER(5, "adventurer", "探险家"),
    BLACKSMITH(6, "blacksmith", "铁匠");

    private final int numericId;
    private final String id;
    private final String displayName;

    JobType(int numericId, String id, String displayName) {
        this.numericId = numericId;
        this.id = id;
        this.displayName = displayName;
    }

    public int getNumericId() {
        return numericId;
    }

    public String getId() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }

    public static JobType fromId(String id) {
        for (JobType job : values()) {
            if (job.id.equalsIgnoreCase(id)) {
                return job;
            }
        }
        return UNEMPLOYED;
    }
    
    /**
     * 从数字ID获取职业
     */
    public static JobType fromId(int numericId) {
        for (JobType job : values()) {
            if (job.numericId == numericId) {
                return job;
            }
        }
        return UNEMPLOYED;
    }
    
    /**
     * 检查是否是有职业（非无业）
     */
    public boolean isEmployed() {
        return this != UNEMPLOYED;
    }
    
    /**
     * 获取所有可选职业（排除无业）
     */
    public static JobType[] getAvailableJobs() {
        return new JobType[] { MINER, FARMER, CHEF, ANGLER, ADVENTURER, BLACKSMITH };
    }
}
