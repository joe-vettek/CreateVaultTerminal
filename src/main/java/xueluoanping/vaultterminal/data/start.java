package xueluoanping.vaultterminal.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import xueluoanping.vaultterminal.SafeReader;
import xueluoanping.vaultterminal.data.blockstate.BlockStatesDataProvider;
import xueluoanping.vaultterminal.data.blockstate.ItemModelProvider;
import xueluoanping.vaultterminal.data.lang.Lang_EN;
import xueluoanping.vaultterminal.data.lang.Lang_ZH;
import xueluoanping.vaultterminal.data.recipe.SRRecipeProvider;
import xueluoanping.vaultterminal.data.tag.TagsDataProvider;

import java.util.concurrent.CompletableFuture;


public class start {
    public final static String MODID = SafeReader.MOD_ID;

    public static void dataGen(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        if (event.includeServer()) {
            SafeReader.logger("Generate We Data!!!");

            TagsDataProvider blockTags = new TagsDataProvider(packOutput,lookupProvider, MODID, helper);
            generator.addProvider(event.includeServer(),blockTags);
            // generator.addProvider(event.includeServer(),new FDLItemTagsProvider(packOutput, lookupProvider, blockTags.contentsGetter()));
            //
            // generator.addProvider(event.includeServer(),new LFTLootTableProvider(packOutput));
            generator.addProvider(event.includeServer(),new SRRecipeProvider(packOutput));
        }
        if (event.includeClient()) {
            generator.addProvider(event.includeClient(),new BlockStatesDataProvider(packOutput,helper));
            generator.addProvider(event.includeClient(),new ItemModelProvider(packOutput,helper));
            generator.addProvider(event.includeClient(),new Lang_EN(packOutput,helper));
            generator.addProvider(event.includeClient(),new Lang_ZH(packOutput,helper));
        }


    }
}
