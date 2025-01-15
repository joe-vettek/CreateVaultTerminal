package xueluoanping.vaultterminal.data.blockstate;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import xueluoanping.vaultterminal.ModContents;
import xueluoanping.vaultterminal.SafeReader;
import xueluoanping.vaultterminal.util.RegisterFinderUtil;

public class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {


	public static final String GENERATED = "item/generated";
	public static final String HANDHELD = "item/handheld";

	public ItemModelProvider(PackOutput generator, ExistingFileHelper existingFileHelper) {
		super(generator, SafeReader.MOD_ID, existingFileHelper);
	}


	@Override
	protected void registerModels() {

		withExistingParent(itemName(ModContents.vault_terminal_item.get()), SafeReader.rl("block/vault_terminal_off")	);
		// basicItem(SafeReader.rl("fantasy_bracelet_1"));
		// basicItem(ModContents.safe_reader_item.get())
		// 		.override().predicate(new ResourceLocation("damage"),1).model(new ModelFile.UncheckedModelFile(SafeReader.rl("item/fantasy_bracelet_1")))
		// ;
	}


	private String itemName(Item item) {
		return RegisterFinderUtil.getItemKey(item).getPath();
	}

	public ResourceLocation resourceItem(String path) {
		return new ResourceLocation(SafeReader.MOD_ID, "item/" + path);
	}



}
