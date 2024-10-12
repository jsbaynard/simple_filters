package monster.giz.simple_filters.blocks.entity;

import monster.giz.simple_filters.SimpleFilters;
import monster.giz.simple_filters.blocks.FilterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static monster.giz.simple_filters.SimpleFilters.FILTER_BLOCK_ENTITY;

public class FilterBlockEntity extends BlockEntity {

    public static final Box INPUT_AREA_SHAPE = Block.createCuboidShape(0.0, 12.0, 0.0, 16.0, 24.0, 16.0).getBoundingBoxes().getFirst();
    private final Box inputAreaBox = INPUT_AREA_SHAPE.offset(pos.getX(), pos.getY(), pos.getZ());

    private Item filteredItem = Items.AIR;

    private int pulseTicks;

    public FilterBlockEntity(BlockPos pos, BlockState state) {
        super(FILTER_BLOCK_ENTITY, pos, state);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, FilterBlockEntity be) {
        if (world.isClient) return;

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
        entities.forEach(be::acceptItem);
    }

    public int calculateComparatorOutput() {
        return pulseTicks > 0 ? 15 : 0;
    }

    private void pulseComparator() {
        this.pulseTicks = world.getGameRules().getInt(SimpleFilters.FILTER_COMPARATOR_OUTPUT_TICKS);
    }

    private void handleComparatorPulseTicks() {
        if (pulseTicks > 0) {
            pulseTicks--;
            if (pulseTicks == 0) {
                markDirty();
            }
        }
    }

    public Item getFilteredItem() {
        return filteredItem;
    }

    public boolean hasFilteredItem() {
        return filteredItem != null;
    }

    public List<ItemEntity> getInputEntities() {
        return world.getEntitiesByClass(ItemEntity.class, inputAreaBox, EntityPredicates.VALID_ENTITY);
    }

    public void setFilteredItem(Item filteredItem) {
        this.filteredItem = filteredItem;
        notifyStateChange();
    }

    public void acceptItem(ItemEntity itemEntity) {
        if (!world.getGameRules().getBoolean(SimpleFilters.FILTER_EMPTY_ACCEPT_ALL) &&
                (filteredItem == null || filteredItem.equals(Items.AIR))) {
            return;
        }

        if (filteredItem != null && !itemEntity.getStack().isOf(filteredItem) && !filteredItem.equals(Items.AIR)) {
            return;
        }

        // If conditions are met, proceed with processing the item
        ItemEntity newItem = itemEntity.copy();
        newItem.setVelocity(0, 0, 0);
        newItem.setPos(pos.getX() + 0.5, pos.getY() - 0.15, pos.getZ() + 0.5);
        newItem.setPickupDelay(20);
        itemEntity.discard();
        world.spawnEntity(newItem);
        pulseComparator();
        markDirty();
    }

    public void notifyStateChange() {
        markDirty();
        world.updateListeners(getPos(), getCachedState(), getCachedState(), Block.NOTIFY_ALL);
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
        filteredItem = Registries.ITEM.get(Identifier.of(nbt.getString("FilteredItem")));
    }

    public NbtCompound createNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putString("FilteredItem", Registries.ITEM.getId(filteredItem).toString());
        return nbt;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = super.toInitialChunkDataNbt(registryLookup);
        return createNbt(nbt, registryLookup);
    }
}
