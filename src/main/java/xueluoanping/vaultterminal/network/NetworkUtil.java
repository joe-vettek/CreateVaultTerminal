package xueluoanping.vaultterminal.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import xueluoanping.vaultterminal.client.SimpleMenu;

import java.util.function.Supplier;

public class NetworkUtil {

    public static ClientLevel getClient() {
        return Minecraft.getInstance().level;
    }

    public static boolean processSolarTermsMessage(GiveItemMessage giveItemMessage, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
        {
            ServerPlayer sender = context.get().getSender();
            if (sender != null
                    && context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                if (sender.containerMenu instanceof SimpleMenu simpleMenu
                        && sender.containerMenu.containerId == giveItemMessage.containerId) {
                    simpleMenu.shouldNextOneCount = !giveItemMessage.isShiftPressed;
                    simpleMenu.clickMenuButton(sender, giveItemMessage.slotIndex);
                }
            }
        });
        return true;
    }

}
