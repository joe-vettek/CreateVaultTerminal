package xueluoanping.vaultterminal.block;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import xueluoanping.vaultterminal.ModContents;
import xueluoanping.vaultterminal.block.base.SimpleHorizontalBlock;
import xueluoanping.vaultterminal.client.SimpleMenu;

import java.util.ArrayList;


public class ReaderBlock extends SimpleHorizontalBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    protected final static VoxelShape shape_N = Shapes.box(0.125, 0.0625, 0.75, 0.875, 1, 1);
    protected final static VoxelShape shape_S = Shapes.box(0.125, 0.0625, 0, 0.875, 1, 0.3125);
    protected final static VoxelShape shape_W = Shapes.box(0.75, 0.0625, 0.125, 1, 1, 0.875);
    protected final static VoxelShape shape_E = Shapes.box(0, 0.0625, 0.125, 0.3125, 1, 0.875);
    protected final static VoxelShape[] shapes = new VoxelShape[]{
            shape_S, shape_W, shape_N, shape_E
    };

    public ReaderBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(OPEN));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return shapes[pState.getValue(FACING).get2DDataValue()];
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pState.getValue(OPEN)) {
            if (pPlayer.getItemInHand(pHand).isEmpty()
                    && pPlayer instanceof ServerPlayer serverPlayer) {
                pLevel.setBlockAndUpdate(pPos, pState.setValue(OPEN, true));
                Direction value = pState.getValue(FACING);
                NetworkHooks.openScreen(
                        serverPlayer, new MenuProvider() {
                            @Override
                            public @NotNull Component getDisplayName() {
                                return Component.translatable("menu.create_safe_reader.tittle");
                            }

                            @Override
                            public @NotNull AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
                                return new SimpleMenu(ModContents.containerType.get(), pContainerId, pPlayerInventory, pPos, value);
                            }

                        }, friendlyByteBuf -> {
                            friendlyByteBuf.writeBlockPos(pPos);
                            friendlyByteBuf.writeEnum(value);
                            BlockEntity blockEntity1;
                            try {
                                blockEntity1 = pLevel.getBlockEntity(pPos.relative(value.getOpposite()));
                            } catch (Exception e) {
                                blockEntity1 = null;
                            }
                            if (blockEntity1 != null) {
                                LazyOptional<IItemHandler> capability = blockEntity1.getCapability(ForgeCapabilities.ITEM_HANDLER);
                                if (capability.resolve().isPresent()) {
                                    IItemHandler iItemHandler = capability.resolve().get();
                                    ArrayList<ItemStack> itemStacks = new ArrayList<>();
                                    for (int j = 0; j < iItemHandler.getSlots(); j++) {
                                        ItemStack stackInSlot = iItemHandler.getStackInSlot(j);
                                        if (!stackInSlot.isEmpty()) {
                                            itemStacks.add(stackInSlot);
                                        }
                                    }
                                    friendlyByteBuf.writeVarInt(itemStacks.size());
                                    for (ItemStack stackInSlot : itemStacks) {
                                        friendlyByteBuf.writeItemStack(stackInSlot, false);
                                    }
                                }
                            }
                        }
                );
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }


}
