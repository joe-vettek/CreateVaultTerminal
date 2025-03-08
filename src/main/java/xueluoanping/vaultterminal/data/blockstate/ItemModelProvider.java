package xueluoanping.vaultterminal.data.blockstate;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import xueluoanping.vaultterminal.ModContents;
import xueluoanping.vaultterminal.VaultTerminal;
import xueluoanping.vaultterminal.util.RegisterFinderUtil;

public class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {


	public static final String GENERATED = "item/generated";
	public static final String HANDHELD = "item/handheld";

	public ItemModelProvider(PackOutput generator, ExistingFileHelper existingFileHelper) {
		super(generator, VaultTerminal.MOD_ID, existingFileHelper);
	}


	@Override
	protected void registerModels() {
		withExistingParent(itemName(ModContents.vault_terminal_item.get()), VaultTerminal.rl("block/vault_terminal_off")	);
	}


	private String itemName(Item item) {
		return RegisterFinderUtil.getItemKey(item).getPath();
	}

	public ResourceLocation resourceItem(String path) {
		return new ResourceLocation(VaultTerminal.MOD_ID, "item/" + path);
	}



}
