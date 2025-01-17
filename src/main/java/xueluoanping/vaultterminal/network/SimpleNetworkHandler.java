package xueluoanping.vaultterminal.network;


import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import xueluoanping.vaultterminal.SafeReader;

import java.util.List;

public final class SimpleNetworkHandler {
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(SafeReader.rl("main"))
            .networkProtocolVersion(() -> SafeReader.NETWORK_VERSION)
            .serverAcceptedVersions(SafeReader.NETWORK_VERSION::equals)
            .clientAcceptedVersions(SafeReader.NETWORK_VERSION::equals)
            .simpleChannel();

    public static void init() {
        int id = 0;
        // registerMessage(id++, SolarTermsMessage.class, SolarTermsMessage::new);
        // registerMessage(id++, BiomeWeatherMessage.class, BiomeWeatherMessage::new);
        var a = CHANNEL.messageBuilder(GiveItemMessage.class, id++)
                .encoder(GiveItemMessage::toBytes)
                .decoder(GiveItemMessage::new);
        // if (FMLLoader.getDist() == Dist.DEDICATED_SERVER)
        a.consumerNetworkThread(NetworkUtil::processSolarTermsMessage);
        a.add();

    }

    public static <MSG> void send(MSG msg) {
        SimpleNetworkHandler.CHANNEL.send(PacketDistributor.SERVER.with(null), msg);
    }


}
