package xueluoanping.vaultterminal.block;


import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import xueluoanping.vaultterminal.ModContents;
import xueluoanping.vaultterminal.VaultTerminal;
import xueluoanping.vaultterminal.block.base.SimpleHorizontalBlock;
import xueluoanping.vaultterminal.client.SimpleMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


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
    public @NotNull InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pState.getValue(OPEN)) {
            if (pPlayer.getItemInHand(pHand).isEmpty()) {
                if (pPlayer instanceof ServerPlayer serverPlayer) {
                    pLevel.setBlockAndUpdate(pPos, pState.setValue(OPEN, true));
                    Direction facing = pState.getValue(FACING);
                    BlockState blockState = pLevel.getBlockState(pPos.relative(facing.getOpposite()));
                    Item item = Item.byBlock(blockState.getBlock());
                    Component component = blockState.hasBlockEntity() && item != Items.AIR ?
                            item.getName(item.getDefaultInstance()) : Component.empty();

                    Pair<ICapabilityProvider, List<ItemStack>> pairIL = ReaderBlock.getCapabilityProviderAndTItemList(pLevel, pPos, facing);
                    if (pairIL.first() != null) {
                        NetworkHooks.openScreen(
                                serverPlayer, new SimpleMenuProvider(component, pPos, facing),
                                new SimpleFriendlyByteBufConsumer(pPos, facing, pairIL.right()));
                    } else {
                        pPlayer.displayClientMessage(Component.translatable("hint.create_vault_terminal.not_open"), true);
                        pLevel.setBlockAndUpdate(pPos, pState.setValue(OPEN, false));
                    }
                }
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }


    public record SimpleMenuProvider(Component component, BlockPos blockPos,
                                     Direction direction) implements MenuProvider {

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable("menu.create_vault_terminal.tittle", this.component);
        }

        @Override
        public @NotNull AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
            return new SimpleMenu(ModContents.containerType.get(), pContainerId, pPlayerInventory, blockPos, direction);
        }
    }

    public record SimpleFriendlyByteBufConsumer(BlockPos blockPos,
                                                Direction facing,
                                                List<ItemStack> itemStacks) implements Consumer<FriendlyByteBuf> {

        @Override
        public void accept(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeBlockPos(blockPos);
            friendlyByteBuf.writeEnum(facing);
            friendlyByteBuf.writeVarInt(itemStacks.size());
            for (ItemStack stackInSlot : itemStacks) {
                friendlyByteBuf.writeItemStack(stackInSlot, false);
            }
        }
    }

    public static Pair<ICapabilityProvider, List<ItemStack>> getCapabilityProviderAndTItemList(Level world, BlockPos pos, Direction facing) {
        List<ItemStack> itemStacks = new ArrayList<>();
        ICapabilityProvider capabilityProvider;
        try {
            capabilityProvider = world.getBlockEntity(pos.relative(facing.getOpposite()));
        } catch (Exception e) {
            capabilityProvider = null;
        }

        // Client would get the data from server
        if (capabilityProvider != null) {
            LazyOptional<IItemHandler> capability = capabilityProvider.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (capability.resolve().isPresent() && !world.isClientSide()) {
                IItemHandler iItemHandler = capability.resolve().get();
                for (int j = 0; j < iItemHandler.getSlots(); j++) {
                    ItemStack stackInSlot = iItemHandler.getStackInSlot(j);
                    if (!stackInSlot.isEmpty()) {
                        ItemStack copy = stackInSlot.copy();
                        if (copy.getCount() < Byte.MAX_VALUE) {
                            itemStacks.add(copy);
                        } else {
                            int remainingCount = copy.getCount();
                            while (remainingCount > 0) {
                                // byte not allow too big values
                                // int max=Byte.MAX_VALUE;
                                int splitCount = Math.min(remainingCount, 64);
                                ItemStack splitStack = copy.copy();
                                splitStack.setCount(splitCount);
                                itemStacks.add(splitStack);
                                remainingCount -= splitCount;
                            }
                        }
                    }
                }
            } else {
                // not open for not valid block
                capabilityProvider = null;
            }
        }
        return Pair.of(capabilityProvider, itemStacks);
    }
}
