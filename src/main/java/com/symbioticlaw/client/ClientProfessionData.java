package com.symbioticlaw.client;

import com.symbioticlaw.data.JobType;

/**
 * 客户端职业数据存储
 */
public class ClientProfessionData {
    public static JobType jobType = JobType.UNEMPLOYED;
    public static double balance = 0.0;

    // Miner
    public static int minerLevel = 0;
    public static double minerXp = 0.0;

    // Farmer
    public static int farmerLevel = 0;
    public static double farmerXp = 0.0;
    public static long totalBiomassSold = 0;

    // Chef
    public static int chefLevel = 0;
    public static double chefXp = 0.0;
    public static long totalDishesServed = 0;
    
    // Angler
    public static int anglerLevel = 0;
    public static double anglerXp = 0.0;
    public static long totalFishSold = 0;
    
    // Adventurer
    public static int adventurerLevel = 0;
    public static double adventurerXp = 0.0;
    public static long totalArtifactsSold = 0;
    
    // Blacksmith
    public static int blacksmithLevel = 0;
    public static double blacksmithXp = 0.0;
    public static long totalItemsRepaired = 0;

    // Daily Quota
    public static String dailyQuotaItem = "";
    public static int dailyQuotaRequiredAmount = 0;
    public static int dailyQuotaProgress = 0;
    public static double dailyQuotaReward = 0.0;
    public static long dailyQuotaCompletionTime = 0;

    public static void setProfessionData(JobType jobType, 
                                       int minerLevel, double minerXp, 
                                       int farmerLevel, double farmerXp, long totalBiomassSold, 
                                       int chefLevel, double chefXp, long totalDishesServed,
                                       int anglerLevel, double anglerXp, long totalFishSold,
                                       int adventurerLevel, double adventurerXp, long totalArtifactsSold,
                                       int blacksmithLevel, double blacksmithXp, long totalItemsRepaired,
                                       double balance, 
                                       String dailyQuotaItem, int dailyQuotaRequiredAmount, 
                                       int dailyQuotaProgress, double dailyQuotaReward, long dailyQuotaCompletionTime) {
        ClientProfessionData.jobType = jobType;
        ClientProfessionData.balance = balance;

        ClientProfessionData.minerLevel = minerLevel;
        ClientProfessionData.minerXp = minerXp;

        ClientProfessionData.farmerLevel = farmerLevel;
        ClientProfessionData.farmerXp = farmerXp;
        ClientProfessionData.totalBiomassSold = totalBiomassSold;

        ClientProfessionData.chefLevel = chefLevel;
        ClientProfessionData.chefXp = chefXp;
        ClientProfessionData.totalDishesServed = totalDishesServed;
        
        ClientProfessionData.anglerLevel = anglerLevel;
        ClientProfessionData.anglerXp = anglerXp;
        ClientProfessionData.totalFishSold = totalFishSold;
        
        ClientProfessionData.adventurerLevel = adventurerLevel;
        ClientProfessionData.adventurerXp = adventurerXp;
        ClientProfessionData.totalArtifactsSold = totalArtifactsSold;
        
        ClientProfessionData.blacksmithLevel = blacksmithLevel;
        ClientProfessionData.blacksmithXp = blacksmithXp;
        ClientProfessionData.totalItemsRepaired = totalItemsRepaired;

        ClientProfessionData.dailyQuotaItem = dailyQuotaItem;
        ClientProfessionData.dailyQuotaRequiredAmount = dailyQuotaRequiredAmount;
        ClientProfessionData.dailyQuotaProgress = dailyQuotaProgress;
        ClientProfessionData.dailyQuotaReward = dailyQuotaReward;
        ClientProfessionData.dailyQuotaCompletionTime = dailyQuotaCompletionTime;
    }
}
