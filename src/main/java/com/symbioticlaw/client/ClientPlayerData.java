package com.symbioticlaw.client;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerData;
import com.symbioticlaw.network.ClientBoundCareerPenaltyPacket;
import com.symbioticlaw.network.ClientBoundPariahListPacket;
import com.symbioticlaw.network.ClientBoundSlaveListPacket;
import com.symbioticlaw.network.PlayerDataSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 客户端玩家数据存储
 * 存储从服务器接收的各种数据，用于GUI显示
 */
@OnlyIn(Dist.CLIENT)
public class ClientPlayerData {

    private static final IPlayerData localPlayerData = new PlayerData();
    
    // 遗民列表
    private static List<ClientBoundPariahListPacket.PariahInfo> pariahList = new ArrayList<>();
    
    // 奴隶列表
    private static List<ClientBoundSlaveListPacket.SlaveInfo> slaveList = new ArrayList<>();
    private static double totalPendingTax = 0.0;
    
    // 职业变更惩罚信息
    private static ClientBoundCareerPenaltyPacket.CareerPenaltyInfo careerPenaltyInfo = null;
    
    // 数据版本（用于UI刷新）
    private static int pariahListVersion = 0;
    private static int slaveListVersion = 0;
    private static int careerInfoVersion = 0;

    public static Optional<IPlayerData> get() {
        if (Minecraft.getInstance().player == null) {
            return Optional.empty();
        }
        return Optional.of(localPlayerData);
    }

    public static void onSyncPacket(PlayerDataSyncPacket packet) {
        get().ifPresent(data -> data.loadNBTData(packet.getData()));
    }
    
    // ========== 遗民列表 ==========
    
    public static void updatePariahList(List<ClientBoundPariahListPacket.PariahInfo> list) {
        pariahList = new ArrayList<>(list);
        pariahListVersion++;
    }
    
    public static List<ClientBoundPariahListPacket.PariahInfo> getPariahList() {
        return new ArrayList<>(pariahList);
    }
    
    public static int getPariahListVersion() {
        return pariahListVersion;
    }
    
    // ========== 奴隶列表 ==========
    
    public static void updateSlaveList(List<ClientBoundSlaveListPacket.SlaveInfo> list, double totalTax) {
        slaveList = new ArrayList<>(list);
        totalPendingTax = totalTax;
        slaveListVersion++;
    }
    
    public static List<ClientBoundSlaveListPacket.SlaveInfo> getSlaveList() {
        return new ArrayList<>(slaveList);
    }
    
    public static double getTotalPendingTax() {
        return totalPendingTax;
    }
    
    public static int getSlaveListVersion() {
        return slaveListVersion;
    }
    
    // ========== 职业变更惩罚信息 ==========
    
    public static void setCareerPenaltyInfo(ClientBoundCareerPenaltyPacket.CareerPenaltyInfo info) {
        careerPenaltyInfo = info;
        careerInfoVersion++;
    }
    
    public static ClientBoundCareerPenaltyPacket.CareerPenaltyInfo getCareerPenaltyInfo() {
        return careerPenaltyInfo;
    }
    
    public static void clearCareerPenaltyInfo() {
        careerPenaltyInfo = null;
        careerInfoVersion++;
    }
    
    public static int getCareerInfoVersion() {
        return careerInfoVersion;
    }
    
    // ========== 数据清理 ==========
    
    public static void clearAll() {
        pariahList.clear();
        slaveList.clear();
        totalPendingTax = 0.0;
        careerPenaltyInfo = null;
        pariahListVersion++;
        slaveListVersion++;
        careerInfoVersion++;
    }
}
