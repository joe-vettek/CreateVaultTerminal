package xueluoanping.vaultterminal.client;

import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerBundlePacket extends BundlePacket<ServerGamePacketListener> {

    protected ServerBundlePacket(Iterable<Packet<ServerGamePacketListener>> pPackets) {
        super(pPackets);
    }

    @Override
    public void handle(ServerGamePacketListener pHandler) {
        for (Packet<ServerGamePacketListener> packet : subPackets()) {
            packet.handle(pHandler);
        }
    }
}
