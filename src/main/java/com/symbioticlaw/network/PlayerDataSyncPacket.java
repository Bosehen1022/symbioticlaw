package com.symbioticlaw.network;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.client.ClientPlayerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * A comprehensive packet to sync all IPlayerData from server to client.
 */
public class PlayerDataSyncPacket {
    private final CompoundTag data;

    public PlayerDataSyncPacket(IPlayerData playerData) {
        this.data = new CompoundTag();
        playerData.saveNBTData(this.data);
    }

    private PlayerDataSyncPacket(CompoundTag data) {
        this.data = data;
    }

    public PlayerDataSyncPacket(FriendlyByteBuf buf) {
        this.data = buf.readNbt();
    }

    // 正确的 encode 方法
    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(this.data);
    }

    // 正确的 decode 方法
    public static PlayerDataSyncPacket decode(FriendlyByteBuf buf) {
        return new PlayerDataSyncPacket(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientPlayerData.onSyncPacket(this);
        });
        context.setPacketHandled(true);
    }

    public CompoundTag getData() {
        return data;
    }
}
