package xueluoanping.vaultterminal;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import xueluoanping.vaultterminal.block.ReaderBlock;
import xueluoanping.vaultterminal.client.SimpleMenu;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContents {
    public static final DeferredRegister<Item> ITEM_DEFERRED_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, VaultTerminal.MOD_ID);
    public static final DeferredRegister<Block> BLOCK_DEFERRED_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, VaultTerminal.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE_DEFERRED_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, VaultTerminal.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPE_DEFERRED_REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, VaultTerminal.MOD_ID);


    public static final RegistryObject<Block> vault_terminal = BLOCK_DEFERRED_REGISTER.register("vault_terminal", () -> new ReaderBlock(BlockBehaviour.Properties.of()
            .strength(2F, 3600000.8F).noLootTable().pushReaction(PushReaction.BLOCK)
            .sound(SoundType.GLASS).noOcclusion()));
    // public static final RegistryObject<Item> itemBlock = DREntityBlockItems.register("one", () -> new BlockItem(fluiddrawer.get(), new Item.Properties()));
    // public static final RegistryObject<BlockEntityType<BlockEntityOne>> tankTileEntityType = DRBlockEntities.register("one",
    //         () ->  BlockEntityType.Builder.of(BlockEntityOne::new, fluiddrawer.get()).build( null));
    public static final RegistryObject<Item> vault_terminal_item = ITEM_DEFERRED_REGISTER.register("vault_terminal",
            () -> new BlockItem(vault_terminal.get(),new Item.Properties().durability(1).setNoRepair()));

    public static final RegistryObject<MenuType<SimpleMenu>> containerType = MENU_TYPE_DEFERRED_REGISTER.register("vault_terminal_container", () -> IForgeMenuType.create(SimpleMenu::new));

}
