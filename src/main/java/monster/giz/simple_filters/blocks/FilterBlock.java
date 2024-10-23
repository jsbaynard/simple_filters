package monster.giz.simple_filters.blocks;

import com.mojang.serialization.MapCodec;
import monster.giz.simple_filters.blocks.entity.FilterBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
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
    public static final VoxelShape ITEM_HITBOX_OLD = VoxelShapes.union(
            VoxelShapes.cuboid(0, 0.625, 0, 1, 1, 1),
            VoxelShapes.cuboid(0.25, 0, 0, 0.75, 0.625, 0.25),
            VoxelShapes.cuboid(0.25, 0, 0.75, 0.75, 0.625, 1),
            VoxelShapes.cuboid(0.75, 0, 0, 1, 0.625, 1),
            VoxelShapes.cuboid(0, 0, 0, 0.25, 0.625, 1)
            );

    public static final VoxelShape ITEM_HITBOX = VoxelShapes.union(
            VoxelShapes.cuboid(0, 0.375, 0, 1, 1, 1),
            VoxelShapes.cuboid(0.25, 0, 0.75, 0.75, 0.375, 1),
            VoxelShapes.cuboid(0, 0, 0, 0.25, 0.375, 1),
            VoxelShapes.cuboid(0.75, 0, 0, 1, 0.375, 1),
            VoxelShapes.cuboid(0.25, 0, 0, 0.75, 0.375, 0.25)
    );

    // Collision for drawing the outline
    public static final VoxelShape OUTLINE_SHAPE = VoxelShapes.union(
            VoxelShapes.cuboid(0, 0.625, 0, 1, 1, 1),
            VoxelShapes.cuboid(0.1875, 0, 0.1875, 0.8125, 0.625, 0.8125)
    );

    public static final EnumProperty<Direction> FILTER_FACING = EnumProperty.of("facing", Direction.class, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    public static final BooleanProperty ENABLED = Properties.ENABLED;

    public FilterBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FILTER_FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(ENABLED, Boolean.TRUE);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) return null;
        return (world2, pos, state2, be) -> FilterBlockEntity.serverTick((ServerWorld) world, pos, state, (FilterBlockEntity) be);
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
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        Direction facing = state.get(FILTER_FACING);
        if (!facing.equals(hit.getSide())) {
            return ActionResult.FAIL;
        }

        ItemStack handItem = player.getMainHandStack();

        if (handItem.isEmpty()) {
            return ActionResult.FAIL;
        }

        if (world.isClient) {
            if (hitEmbeddedItemFrame(facing, hit)) return ActionResult.SUCCESS;
            return ActionResult.FAIL;
        }

        if (!hitEmbeddedItemFrame(facing, hit)) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }

        FilterBlockEntity entity = getBlockEntity(world, pos);
        ItemStack filterItem = entity.getFilteredStack();

        if (!filterItem.isEmpty()) {
            return ActionResult.FAIL;
        }

        entity.setFilteredStack(handItem.copyWithCount(1));
        handItem.decrementUnlessCreative(1, player);
        world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);

        return ActionResult.SUCCESS;
    }

    public void onPunched(World world, BlockPos pos, boolean creativeMode) {
        FilterBlockEntity entity = getBlockEntity(world, pos);
        BlockState state = world.getBlockState(pos);
        if (entity.hasFilteredItem()) {
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (creativeMode) {
                entity.setFilteredStack(ItemStack.EMPTY);
                return;
            }
            entity.dropFrameStack(state.get(FILTER_FACING));
        }
    }

    public boolean hitEmbeddedItemFrame(Direction facing, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos().offset(facing);
        Vec3d vec3d = hit.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());

        float x = (float) vec3d.getX();
        float y = (float) vec3d.getY();
        float z = (float) vec3d.getZ();

        Vec2f planePos = switch (facing) {
            case NORTH -> new Vec2f(1.0f - x, y);
            case SOUTH -> new Vec2f(x, y);
            case WEST -> new Vec2f(z, y);
            case EAST -> new Vec2f(1.0f - z, y);
            default -> new Vec2f(0, 0);
        };

        return planePos.x > 0.31 && planePos.x < 0.69 && planePos.y > 0.12 && planePos.y < 0.5;
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
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!player.isCreative()) {
            ItemEntity droppedItem = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, getBlockEntity(world, pos).getFilteredStack());
            world.spawnEntity(droppedItem);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
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
        builder.add(FILTER_FACING).add(ENABLED);
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
