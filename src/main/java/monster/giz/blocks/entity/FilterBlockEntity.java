package monster.giz.blocks.entity;

import monster.giz.blocks.FilterBlock;
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

import static monster.giz.SimpleFilters.FILTER_BLOCK_ENTITY;

public class FilterBlockEntity extends BlockEntity {

    public static final Box INPUT_AREA_SHAPE = Block.createCuboidShape(0.0, 12.0, 0.0, 16.0, 24.0, 16.0).getBoundingBoxes().getFirst();

    private Item filteredItem;

    public static final int COMPARATOR_PULSE_DURATION = 20;
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
        BlockState blockAbove = world.getBlockState(pos.add(0, 1, 0));
        if (blockAbove.isSolidBlock(world, pos.add(0, 1, 0))) {
            return;
        }
        be.getInputEntities().forEach(be::acceptItem);
    }

    public int calculateComparatorOutput() {
        if (pulseTicks > 0) {
            return 15;
        }
        return 0;
    }

    private void pulseComparator() {
        this.pulseTicks = COMPARATOR_PULSE_DURATION;
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
        Box box = INPUT_AREA_SHAPE.offset(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        return world.getEntitiesByClass(ItemEntity.class, box, EntityPredicates.VALID_ENTITY);
    }

    public void setFilteredItem(Item filteredItem) {
        this.filteredItem = filteredItem;
        notifyStateChange();
    }

    public void acceptItem(ItemEntity itemEntity) {
        if (filteredItem != null && !itemEntity.getStack().isOf(filteredItem) && !filteredItem.equals(Items.AIR)) {
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
