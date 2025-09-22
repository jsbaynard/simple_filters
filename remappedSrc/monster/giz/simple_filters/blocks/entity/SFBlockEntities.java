package monster.giz.simple_filters.blocks.entity;

import monster.giz.simple_filters.SimpleFilters;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static monster.giz.simple_filters.blocks.SFBlocks.FILTER_BLOCK;

public class SFBlockEntities {

    public static final BlockEntityType<FilterBlockEntity> FILTER_BLOCK_ENTITY = registerBlockEntity("filter", FabricBlockEntityTypeBuilder.create(FilterBlockEntity::new, FILTER_BLOCK).build());

    private static <T extends BlockEntityType<?>> T registerBlockEntity(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(SimpleFilters.NAMESPACE, path), blockEntityType);
    }

    public static void register() {}
}
