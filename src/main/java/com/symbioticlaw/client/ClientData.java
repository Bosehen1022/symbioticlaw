package com.symbioticlaw.client;

public class ClientData {
    public static double returnFee = 0.0;
    public static double safetyRadius = 0.0;
    public static int almanacStatus = 0;
    public static int coreX = 0;
    public static int coreY = 0;
    public static int coreZ = 0;

    public static void setAlmanacData(double fee, double radius, int status, int coreX, int coreY, int coreZ) {
        returnFee = fee;
        safetyRadius = radius;
        almanacStatus = status;
        ClientData.coreX = coreX;
        ClientData.coreY = coreY;
        ClientData.coreZ = coreZ;
    }
}
