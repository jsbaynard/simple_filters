package monster.giz.simple_filters;

import monster.giz.simple_filters.blocks.FilterBlock;
import monster.giz.simple_filters.blocks.entity.FilterBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
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
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleFilters implements ModInitializer {

	public static final String NAMESPACE = "simple_filters";

	public static final Logger LOGGER = LoggerFactory.getLogger(SimpleFilters.NAMESPACE);

	public static final Block FILTER_BLOCK = new FilterBlock(AbstractBlock.Settings.copy(Blocks.HOPPER).nonOpaque());
	public static final BlockEntityType<FilterBlockEntity> FILTER_BLOCK_ENTITY = register("filter", BlockEntityType.Builder.create(FilterBlockEntity::new, FILTER_BLOCK).build());

	public static final GameRules.Key<GameRules.BooleanRule> FILTER_EMPTY_ACCEPT_ALL = GameRuleRegistry.register("filtersEmptyAcceptsAny", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
	public static final GameRules.Key<GameRules.IntRule> FILTER_COMPARATOR_OUTPUT_TICKS = GameRuleRegistry.register("filtersComparatorOutputTicks", GameRules.Category.MISC, GameRuleFactory.createIntRule(10, 0, 40));


	public static Identifier id(String loc) {
		return Identifier.of(NAMESPACE, loc);
	}

	public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(NAMESPACE, path), blockEntityType);
	}


	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, id("filter"), FILTER_BLOCK);
		Registry.register(Registries.ITEM, id("filter"), new BlockItem(FILTER_BLOCK, new Item.Settings()));

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.addAfter(Items.HOPPER, FILTER_BLOCK);
		});

		LOGGER.info("Simple Filters initialized.");
	}
}