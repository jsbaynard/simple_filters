package monster.giz;

import monster.giz.blocks.FilterBlock;
import monster.giz.blocks.entity.FilterBlockEntity;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class SimpleFilters implements ModInitializer {

	public static final String NAMESPACE = "simple_filters";
	public static Identifier idOf(String loc) {
		return Identifier.of(NAMESPACE, loc);
	}

	public static final Block FILTER_BLOCK = new FilterBlock(AbstractBlock.Settings.copy(Blocks.HOPPER).nonOpaque());
	public static final BlockEntityType<FilterBlockEntity> FILTER_BLOCK_ENTITY = register("filter", BlockEntityType.Builder.create(FilterBlockEntity::new, FILTER_BLOCK).build());

	public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(NAMESPACE, path), blockEntityType);
	}

	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, idOf("filter"), FILTER_BLOCK);
		Registry.register(Registries.ITEM, idOf("filter"), new BlockItem(FILTER_BLOCK, new Item.Settings()));

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.addAfter(Items.HOPPER, FILTER_BLOCK);
		});

		SFLogger.log("Simple Filters initialized.");
	}
}