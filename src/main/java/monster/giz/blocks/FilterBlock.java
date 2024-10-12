package monster.giz.blocks;

import com.mojang.serialization.MapCodec;
import monster.giz.blocks.entity.FilterBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FilterBlock extends BlockWithEntity {

    // Represents the hitbox as players/entities experience it
    private static final VoxelShape HITBOX =
            VoxelShapes.union(
                    VoxelShapes.cuboid(0, 0.625, 0, 1, 1, 1),
                    VoxelShapes.cuboid(0.25, 0, 0.1875, 0.75, 0.625, 0.25),
                    VoxelShapes.cuboid(0.25, 0, 0.75, 0.75, 0.625, 0.8125),
                    VoxelShapes.cuboid(0.75, 0, 0.1875, 0.8125, 0.625, 0.8125),
                    VoxelShapes.cuboid(0.1875, 0, 0.1875, 0.25, 0.625, 0.8125)
            );

    // Represents the hitbox as items see it, allowing for items to pass through the "chute"
    public static final VoxelShape ITEM_HITBOX = VoxelShapes.union(
            VoxelShapes.cuboid(0, 0.625, 0, 1, 1, 1),
            VoxelShapes.cuboid(0.25, 0, 0, 0.75, 0.625, 0.25),
            VoxelShapes.cuboid(0.25, 0, 0.75, 0.75, 0.625, 1),
            VoxelShapes.cuboid(0.75, 0, 0, 1, 0.625, 1),
            VoxelShapes.cuboid(0, 0, 0, 0.25, 0.625, 1)
            );

    // Collision for drawing the outline
    public static final VoxelShape OUTLINE_SHAPE = VoxelShapes.union(
            VoxelShapes.cuboid(0, 0.625, 0, 1, 1, 1),
            VoxelShapes.cuboid(0.1875, 0, 0.1875, 0.8125, 0.625, 0.8125)
    );

    public static final DirectionProperty FACING = DirectionProperty.of("facing", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    public static final BooleanProperty ENABLED = Properties.ENABLED;

    public FilterBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(ENABLED, Boolean.TRUE);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) return null;
        return (world2, pos, state2, be) -> FilterBlockEntity.serverTick(world, pos, state, (FilterBlockEntity) be);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext entityShapeContext) {
            Entity entity = entityShapeContext.getEntity();
            if (entity instanceof ItemEntity) {
                return ITEM_HITBOX;
            }
        }
        return HITBOX;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!state.get(FACING).equals(hit.getSide())) {
            return ItemActionResult.FAIL;
        }

        if (world.isClient) {
            return ItemActionResult.CONSUME;
        }

        FilterBlockEntity entity = getBlockEntity(world, pos);

        Item filterItem = entity.getFilteredItem();
        Item handItem = player.getMainHandStack().getItem();

        if (handItem == null) {
            if (filterItem == null) {
                return ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
            entity.setFilteredItem(null);
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return ItemActionResult.SUCCESS;
        }

        if (handItem.equals(filterItem)) {
            return ItemActionResult.SUCCESS;
        }

        entity.setFilteredItem(handItem);
        world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
        return ItemActionResult.SUCCESS;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
            this.updateEnabled(world, pos, state);
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        this.updateEnabled(world, pos, state);
    }

    private void updateEnabled(World world, BlockPos pos, BlockState state) {
        boolean bl = !world.isReceivingRedstonePower(pos);
        if (bl != state.get(ENABLED)) {
            world.setBlockState(pos, state.with(ENABLED, bl), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return getBlockEntity(world, pos).calculateComparatorOutput();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(ENABLED);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FilterBlockEntity(pos, state);
    }

    public FilterBlockEntity getBlockEntity(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof FilterBlockEntity ? (FilterBlockEntity) blockEntity : null;
    }

}
