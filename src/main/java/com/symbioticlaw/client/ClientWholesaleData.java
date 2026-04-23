package com.symbioticlaw.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户端批发数据缓存
 * 用于存储从服务端接收的批发目录
 */
public class ClientWholesaleData {
    
    private static Map<String, Double> cachedCatalog = new HashMap<>();
    private static boolean dataReceived = false;
    
    /**
     * 更新批发目录（从网络包接收）
     */
    public static void updateCatalog(Map<String, Double> catalog) {
        cachedCatalog = new HashMap<>(catalog);
        dataReceived = true;
    }
    
    /**
     * 获取缓存的批发目录
     */
    public static Map<String, Double> getCatalog() {
        return Collections.unmodifiableMap(cachedCatalog);
    }
    
    /**
     * 检查是否已接收数据
     */
    public static boolean isDataReceived() {
        return dataReceived;
    }
    
    /**
     * 清空缓存（例如切换世界时）
     */
    public static void clear() {
        cachedCatalog.clear();
        dataReceived = false;
    }
    
    /**
     * 获取特定物品的批发价格
     */
    public static double getPrice(String itemId) {
        return cachedCatalog.getOrDefault(itemId, 0.0);
    }
}
