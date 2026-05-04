package com.symbioticlaw.network;

import com.symbioticlaw.Symbioticlaw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Symbioticlaw.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, PlayerDataSyncPacket.class,
                PlayerDataSyncPacket::encode,
                PlayerDataSyncPacket::decode,
                (msg, ctx) -> msg.handle(ctx)
        );
        INSTANCE.registerMessage(id++, ClientBoundWarningPacket.class,
                ClientBoundWarningPacket::encode, ClientBoundWarningPacket::decode,
                ClientBoundWarningPacket::handle);
        INSTANCE.registerMessage(id++, ServerBoundRequestContractsPacket.class,
                ServerBoundRequestContractsPacket::encode,
                ServerBoundRequestContractsPacket::decode,
                ServerBoundRequestContractsPacket::handle
        );
        INSTANCE.registerMessage(id++, ClientBoundOpenContractsPacket.class,
                ClientBoundOpenContractsPacket::encode,
                ClientBoundOpenContractsPacket::decode,
                ClientBoundOpenContractsPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundAcceptContractPacket.class,
                ServerBoundAcceptContractPacket::encode,
                ServerBoundAcceptContractPacket::decode,
                ServerBoundAcceptContractPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundCoreActionPacket.class,
                ServerBoundCoreActionPacket::encode,
                ServerBoundCoreActionPacket::decode,
                ServerBoundCoreActionPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundChangeProfessionPacket.class,
                ServerBoundChangeProfessionPacket::encode,
                ServerBoundChangeProfessionPacket::decode,
                ServerBoundChangeProfessionPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundSellItemsPacket.class,
                ServerBoundSellItemsPacket::encode,
                ServerBoundSellItemsPacket::decode,
                ServerBoundSellItemsPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundBondCreatePacket.class,
                ServerBoundBondCreatePacket::encode,
                ServerBoundBondCreatePacket::decode,
                ServerBoundBondCreatePacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundRequestNearbyPariahsPacket.class,
                ServerBoundRequestNearbyPariahsPacket::encode,
                ServerBoundRequestNearbyPariahsPacket::decode,
                ServerBoundRequestNearbyPariahsPacket::handle
        );
        INSTANCE.registerMessage(id++, ClientBoundPariahListPacket.class,
                ClientBoundPariahListPacket::encode,
                ClientBoundPariahListPacket::decode,
                ClientBoundPariahListPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundSignContractPacket.class,
                ServerBoundSignContractPacket::encode,
                ServerBoundSignContractPacket::decode,
                ServerBoundSignContractPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundAdminActionPacket.class,
                ServerBoundAdminActionPacket::encode,
                ServerBoundAdminActionPacket::decode,
                ServerBoundAdminActionPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundRequestExecutionListPacket.class,
                ServerBoundRequestExecutionListPacket::encode,
                ServerBoundRequestExecutionListPacket::decode,
                ServerBoundRequestExecutionListPacket::handle
        );
        INSTANCE.registerMessage(id++, ClientBoundExecutionListPacket.class,
                ClientBoundExecutionListPacket::encode,
                ClientBoundExecutionListPacket::decode,
                ClientBoundExecutionListPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundReleaseSlavePacket.class,
                ServerBoundReleaseSlavePacket::encode,
                ServerBoundReleaseSlavePacket::decode,
                ServerBoundReleaseSlavePacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundRequestSlavesPacket.class,
                ServerBoundRequestSlavesPacket::encode,
                ServerBoundRequestSlavesPacket::decode,
                ServerBoundRequestSlavesPacket::handle
        );
        INSTANCE.registerMessage(id++, ClientBoundSlavesPacket.class,
                ClientBoundSlavesPacket::encode,
                ClientBoundSlavesPacket::decode,
                ClientBoundSlavesPacket::handle
        );
        INSTANCE.registerMessage(id++, ClientBoundUpdateMarketPacket.class,
                ClientBoundUpdateMarketPacket::toBytes,
                ClientBoundUpdateMarketPacket::fromBytes,
                ClientBoundUpdateMarketPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundBuyItemPacket.class,
                ServerBoundBuyItemPacket::toBytes,
                ServerBoundBuyItemPacket::fromBytes,
                ServerBoundBuyItemPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundSellItemPacket.class,
                ServerBoundSellItemPacket::toBytes,
                ServerBoundSellItemPacket::fromBytes,
                ServerBoundSellItemPacket::handle
        );

        INSTANCE.registerMessage(id++, RequestBulkPriceC2SPacket.class,
                RequestBulkPriceC2SPacket::toBytes,
                RequestBulkPriceC2SPacket::new,
                RequestBulkPriceC2SPacket::handle
        );

        INSTANCE.registerMessage(id++, UpdateBulkPriceS2CPacket.class,
                UpdateBulkPriceS2CPacket::toBytes,
                UpdateBulkPriceS2CPacket::new,
                UpdateBulkPriceS2CPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundSellBulkPacket.class,
                ServerBoundSellBulkPacket::toBytes,
                ServerBoundSellBulkPacket::new,
                ServerBoundSellBulkPacket::handle
        );

        INSTANCE.registerMessage(id++, ClientBoundAlmanacUpdatePacket.class,
                ClientBoundAlmanacUpdatePacket::encode,
                ClientBoundAlmanacUpdatePacket::decode,
                ClientBoundAlmanacUpdatePacket::handle
        );

        INSTANCE.registerMessage(id++, ServerBoundRequestDailyQuotaPacket.class,
                ServerBoundRequestDailyQuotaPacket::toBytes,
                ServerBoundRequestDailyQuotaPacket::new,
                ServerBoundRequestDailyQuotaPacket::handle
        );

        INSTANCE.registerMessage(id++, ClientBoundDailyQuotaPacket.class,
                ClientBoundDailyQuotaPacket::toBytes,
                ClientBoundDailyQuotaPacket::new,
                ClientBoundDailyQuotaPacket::handle
        );

        INSTANCE.registerMessage(id++, ServerBoundTurnInDailyQuotaPacket.class,
                ServerBoundTurnInDailyQuotaPacket::toBytes,
                ServerBoundTurnInDailyQuotaPacket::new,
                ServerBoundTurnInDailyQuotaPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundWholesalePacket.class,
                ServerBoundWholesalePacket::encode,
                ServerBoundWholesalePacket::decode,
                ServerBoundWholesalePacket::handle
        );
        
        // Chapter 5: Career Binding & Transfer System
        INSTANCE.registerMessage(id++, ServerBoundTransferPacket.class,
                ServerBoundTransferPacket::encode,
                ServerBoundTransferPacket::decode,
                ServerBoundTransferPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundCareerActionPacket.class,
                ServerBoundCareerActionPacket::encode,
                ServerBoundCareerActionPacket::decode,
                ServerBoundCareerActionPacket::handle
        );
        INSTANCE.registerMessage(id++, ClientBoundPariahListPacket.class,
                ClientBoundPariahListPacket::encode,
                ClientBoundPariahListPacket::decode,
                ClientBoundPariahListPacket::handle
        );
        INSTANCE.registerMessage(id++, ClientBoundSlaveListPacket.class,
                ClientBoundSlaveListPacket::encode,
                ClientBoundSlaveListPacket::decode,
                ClientBoundSlaveListPacket::handle
        );
        INSTANCE.registerMessage(id++, ClientBoundCareerPenaltyPacket.class,
                ClientBoundCareerPenaltyPacket::encode,
                ClientBoundCareerPenaltyPacket::decode,
                ClientBoundCareerPenaltyPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundRequestSlaveListPacket.class,
                ServerBoundRequestSlaveListPacket::encode,
                ServerBoundRequestSlaveListPacket::decode,
                ServerBoundRequestSlaveListPacket::handle
        );
        
        // Wholesale catalog packets
        INSTANCE.registerMessage(id++, ServerBoundRequestWholesaleCatalogPacket.class,
                ServerBoundRequestWholesaleCatalogPacket::encode,
                ServerBoundRequestWholesaleCatalogPacket::decode,
                ServerBoundRequestWholesaleCatalogPacket::handle
        );
        INSTANCE.registerMessage(id++, ClientBoundWholesaleCatalogPacket.class,
                ClientBoundWholesaleCatalogPacket::encode,
                ClientBoundWholesaleCatalogPacket::decode,
                ClientBoundWholesaleCatalogPacket::handle
        );
        
        // Economy sync packets (v0.0.2)
        INSTANCE.registerMessage(id++, ClientBoundEconomySyncPacket.class,
                ClientBoundEconomySyncPacket::encode,
                ClientBoundEconomySyncPacket::decode,
                ClientBoundEconomySyncPacket::handle
        );
        INSTANCE.registerMessage(id++, ServerBoundRequestEconomySyncPacket.class,
                ServerBoundRequestEconomySyncPacket::encode,
                ServerBoundRequestEconomySyncPacket::decode,
                ServerBoundRequestEconomySyncPacket::handle
        );
    }
    
    /**
     * 将自定义包转换为vanilla兼容包（用于1.20.1兼容性）
     */
    public static <MSG> net.minecraft.network.protocol.Packet<?> toVanillaPacket(MSG message) {
        return INSTANCE.toVanillaPacket(message, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <MSG> void sendToPlayer(ServerPlayer player, MSG message) {
        INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), message);
    }
}
