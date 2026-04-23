package com.symbioticlaw.contract;

public enum ContractStatus {
    OPEN,       // Available for players to accept
    ACCEPTED,   // Accepted by a player, in progress
    COMPLETED,  // Objective met, reward pending
    CLOSED      // Reward collected or contract failed/expired
}