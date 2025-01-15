package xueluoanping.vaultterminal.data.recipe;


import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;
import xueluoanping.vaultterminal.ModContents;

import java.util.function.Consumer;

public final class SRRecipeProvider extends RecipeProvider {

    public SRRecipeProvider(PackOutput generator) {
        super(generator);
    }


    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModContents.vault_terminal_item.get())
                .define('x', Tags.Items.GLASS)
                .define('y', Tags.Items.INGOTS_IRON)
                .define('z', ItemTags.LOGS)
                .pattern(" x ")
                .pattern(" y ")
                .pattern(" z ")
                .group("safe_reader")
                .unlockedBy("has_glass", has(Tags.Items.GLASS))
                .save(consumer);
    }


}
