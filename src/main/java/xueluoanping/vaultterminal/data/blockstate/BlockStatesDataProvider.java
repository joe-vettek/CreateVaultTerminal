package xueluoanping.vaultterminal.data.blockstate;


import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;
import xueluoanping.vaultterminal.ModContents;
import xueluoanping.vaultterminal.VaultTerminal;
import xueluoanping.vaultterminal.block.ReaderBlock;

import java.util.List;


public class BlockStatesDataProvider extends BlockStateProvider {


    private final ExistingFileHelper existingFileHelper;

    public BlockStatesDataProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, VaultTerminal.MOD_ID, existingFileHelper);
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    protected void registerStatesAndModels() {
        for (var holder : List.of(ModContents.vault_terminal)) {
            getVariantBuilder(holder.get()).forAllStatesExcept(state -> ConfiguredModel.builder()
                    .modelFile(models().getExistingFile(resourceBlock(holder.getId().getPath()+(state.getValue(ReaderBlock.OPEN)?"_on":"_off"))))
                    .rotationY(getRotateYByFacing(state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                    .build());
        }

    }

    public void addSimple(Block block) {
        simpleBlock(block, models().getExistingFile(resourceBlock(blockName(block))));
    }

    private String blockName(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    public static ResourceLocation resourceBlock(String path) {
        return VaultTerminal.rl("block/" + path);
    }


    public static int getRotateYByFacing(Direction state) {
        switch (state) {
            case EAST -> {
                return 90;
            }
            case SOUTH -> {
                return 180;
            }
            case WEST -> {
                return 270;
            }
            default -> {
                return 0;
            }
        }
    }

}
