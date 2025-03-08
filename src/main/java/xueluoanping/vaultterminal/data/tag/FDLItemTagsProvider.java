package xueluoanping.vaultterminal.data.tag;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import xueluoanping.vaultterminal.VaultTerminal;

import java.util.concurrent.CompletableFuture;

public class FDLItemTagsProvider extends ItemTagsProvider {
    public FDLItemTagsProvider(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_, CompletableFuture<TagLookup<Block>> p_275322_) {
        super(p_275343_, p_275729_, p_275322_);
    }


    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var tag= ItemTags.create(VaultTerminal.rl("drawers"));
        // this.tag(tag).add(ModContents.fluiddrawer.get());
    }
}
