package monster.giz.simple_filters.blocks.entity;

import monster.giz.simple_filters.SimpleFilters;
import monster.giz.simple_filters.blocks.FilterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static monster.giz.simple_filters.SimpleFilters.FILTER_BLOCK_ENTITY;

public class FilterBlockEntity extends BlockEntity {

    public static final Box INPUT_AREA_SHAPE = Block.createCuboidShape(0.0, 12.0, 0.0, 16.0, 24.0, 16.0).getBoundingBoxes().getFirst();
    private final Box inputAreaBox = INPUT_AREA_SHAPE.offset(pos.getX(), pos.getY(), pos.getZ());

    private ItemStack filteredItemStack = ItemStack.EMPTY;

    private int pulseTicks;

    public static final String FILTERED_ITEMSTACK_NBT_KEY = "FilteredItemStack";

    public FilterBlockEntity(BlockPos pos, BlockState state) {
        super(FILTER_BLOCK_ENTITY, pos, state);
    }

    public static void serverTick(ServerWorld world, BlockPos pos, BlockState state, FilterBlockEntity be) {
        be.handleComparatorPulseTicks();

        if (!state.get(FilterBlock.ENABLED)) {
            return;
        }

        BlockPos abovePos = pos.up();
        BlockState blockAbove = world.getBlockState(abovePos);
        if (blockAbove.isSolidBlock(world, abovePos)) {
            return;
        }

        List<ItemEntity> entities = be.getInputEntities();
        entities.forEach(be::tryFilterItem);
    }

    public int calculateComparatorOutput() {
        return pulseTicks > 0 ? 15 : 0;
    }

    private void pulseComparator() {
        this.pulseTicks = getServerWorld().getGameRules().getInt(SimpleFilters.FILTER_COMPARATOR_OUTPUT_TICKS);
    }

    private void handleComparatorPulseTicks() {
        if (pulseTicks > 0) {
            pulseTicks--;
            if (pulseTicks == 0) {
                markDirty();
            }
        }
    }

    public ItemStack getFilteredStack() {
        return filteredItemStack.copy();
    }

    public boolean hasFilteredItem() {
        return !filteredItemStack.isEmpty();
    }

    public List<ItemEntity> getInputEntities() {
        return world.getEntitiesByClass(ItemEntity.class, inputAreaBox, EntityPredicates.VALID_ENTITY);
    }

    public void setFilteredStack(ItemStack stack) {
        this.filteredItemStack = stack.copy();
        markDirty();
    }

    public void tryFilterItem(ItemEntity itemEntity) {
        if (!canAcceptItem(itemEntity)) {
            return;
        }
        ItemEntity newItem = itemEntity.copy();
        newItem.setVelocity(0, 0, 0);
        newItem.setPos(pos.getX() + 0.5, pos.getY() - 0.15, pos.getZ() + 0.5);
        newItem.setPickupDelay(20);
        itemEntity.discard();
        world.spawnEntity(newItem);
        pulseComparator();
        markDirty();
    }

    public boolean canAcceptItem(ItemEntity itemEntity) {
        if (!getServerWorld().getGameRules().getBoolean(SimpleFilters.FILTER_EMPTY_ACCEPT_ALL) && (filteredItemStack == null || filteredItemStack.getItem().equals(Items.AIR))) {
            return false;
        }
        if (!filteredItemStack.isEmpty() && !itemEntity.getStack().isOf(filteredItemStack.getItem())) {
            return false;
        }
        return true;
    }

    public void dropFrameStack(Direction direction) {
        ItemStack stack = getFilteredStack().copy();
        setFilteredStack(ItemStack.EMPTY);
        BlockPos pos = getPos();
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.20;
        double z = pos.getZ() + 0.5;

        ServerWorld world = getServerWorld();
        switch (direction) {
            case NORTH:
                z -= 0.65;
                break;
            case SOUTH:
                z += 0.65;
                break;
            case EAST:
                x += 0.65;
                break;
            case WEST:
                x -= 0.65;
                break;
        }
        ItemEntity droppedItem = new ItemEntity(world, x, y, z, stack);
        droppedItem.setPos(x, y, z);
        world.spawnEntity(droppedItem);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        world.updateListeners(getPos(), getCachedState(), getCachedState(), Block.NOTIFY_ALL);
    }

    private ServerWorld getServerWorld() {
        return (ServerWorld) world;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        createNbt(nbt, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        filteredItemStack = ItemStack.fromNbtOrEmpty(registryLookup, nbt.getCompound(FILTERED_ITEMSTACK_NBT_KEY));
    }

    public NbtCompound createNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (!filteredItemStack.isEmpty()) {
            nbt.put(FILTERED_ITEMSTACK_NBT_KEY, filteredItemStack.toNbt(registryLookup));
        } else {
            nbt.put(FILTERED_ITEMSTACK_NBT_KEY, filteredItemStack.toNbtAllowEmpty(registryLookup));
        }
        return nbt;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = super.toInitialChunkDataNbt(registryLookup);
        return createNbt(nbt, registryLookup);
    }
}
