package monster.giz.simple_filters;

import monster.giz.simple_filters.blocks.FilterBlock;
import monster.giz.simple_filters.blocks.SFBlocks;
import monster.giz.simple_filters.blocks.entity.SFBlockEntities;
import monster.giz.simple_filters.network.c2s.FilterFramePunchPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleFilters implements ModInitializer {

	public static final String NAMESPACE = "simple_filters";
	public static final Logger LOGGER = LoggerFactory.getLogger(SimpleFilters.NAMESPACE);

	public static Identifier idOf(String loc) {
		return Identifier.of(NAMESPACE, loc);
	}

	@Override
	public void onInitialize() {
		SFGamerules.register();
		SFBlocks.register();
		SFBlockEntities.register();

		LOGGER.info("Simple Filters initialized.");

		PayloadTypeRegistry.playC2S().register(FilterFramePunchPayload.ID, FilterFramePunchPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(FilterFramePunchPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				Block block = context.player().getEntityWorld().getBlockState(payload.blockPos()).getBlock();
				if (block instanceof FilterBlock filter) {
					filter.onPunched(context.player().getEntityWorld(), payload.blockPos(), context.player().isCreative());
				}
			});
		});
	}
}

