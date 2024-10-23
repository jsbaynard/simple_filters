package monster.giz.simple_filters;

import monster.giz.simple_filters.blocks.FilterBlock;
import monster.giz.simple_filters.blocks.SimpleFiltersBlocks;
import monster.giz.simple_filters.blocks.entity.FilterBlockEntity;
import monster.giz.simple_filters.network.c2s.play.FilterFramePunchPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static monster.giz.simple_filters.blocks.SimpleFiltersBlocks.FILTER_BLOCK;

public class SimpleFilters implements ModInitializer {

	public static final String NAMESPACE = "simple_filters";
	public static final Logger LOGGER = LoggerFactory.getLogger(SimpleFilters.NAMESPACE);

	public static final BlockEntityType<FilterBlockEntity> FILTER_BLOCK_ENTITY = registerBlockEntity("filter", FabricBlockEntityTypeBuilder.create(FilterBlockEntity::new, FILTER_BLOCK).build());

	public static final GameRules.Key<GameRules.BooleanRule> FILTER_EMPTY_ACCEPT_ALL = GameRuleRegistry.register("filtersEmptyAcceptsAny", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
	public static final GameRules.Key<GameRules.IntRule> FILTER_COMPARATOR_OUTPUT_TICKS = GameRuleRegistry.register("filtersComparatorOutputTicks", GameRules.Category.MISC, GameRuleFactory.createIntRule(10, 0, 40));

	public static Identifier idOf(String loc) {
		return Identifier.of(NAMESPACE, loc);
	}

	public static <T extends BlockEntityType<?>> T registerBlockEntity(String path, T blockEntityType) {
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(NAMESPACE, path), blockEntityType);
	}

	@Override
	public void onInitialize() {
		SimpleFiltersBlocks.register();

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.addAfter(Items.HOPPER, FILTER_BLOCK);
		});

		LOGGER.info("Simple Filters initialized.");

		PayloadTypeRegistry.playC2S().register(FilterFramePunchPayload.ID, FilterFramePunchPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(FilterFramePunchPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				Block block = context.player().getWorld().getBlockState(payload.blockPos()).getBlock();
				if (block instanceof FilterBlock filter) {
					filter.onPunched(context.player().getWorld(), payload.blockPos(), context.player().isCreative());
				}
			});
		});

	}
}

