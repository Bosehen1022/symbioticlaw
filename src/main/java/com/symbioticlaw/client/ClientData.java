package com.symbioticlaw.client;

public class ClientData {
    public static double returnFee = 0.0;
    public static double safetyRadius = 0.0;
    public static int almanacStatus = 0;

    public static void setAlmanacData(double fee, double radius, int status) {
        returnFee = fee;
        safetyRadius = radius;
        almanacStatus = status;
    }
}
