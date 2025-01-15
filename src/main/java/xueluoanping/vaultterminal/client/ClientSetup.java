package xueluoanping.vaultterminal.client;


import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import xueluoanping.vaultterminal.ModContents;
import xueluoanping.vaultterminal.SafeReader;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientEvent(FMLClientSetupEvent event) {
        SafeReader.logger("Register Client");
        event.enqueueWork(() -> {
            MenuScreens.register(ModContents.containerType.get(), SimpleScreen::new);

        });
    }


    @SubscribeEvent
    public static void onBuildCreativeModeTabContentsEvent(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModContents.vault_terminal_item.get());
        }
    }
}
