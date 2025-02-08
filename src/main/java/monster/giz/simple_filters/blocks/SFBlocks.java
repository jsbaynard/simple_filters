package monster.giz.simple_filters.blocks;

import monster.giz.simple_filters.SimpleFilters;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.function.Function;

public class SFBlocks {

    public static final Block FILTER_BLOCK = registerBlock("filter", FilterBlock::new, AbstractBlock.Settings.copy(Blocks.HOPPER).nonOpaque());

    public static Block registerBlock(String id, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, SimpleFilters.idOf(id));
        Block block = factory.apply(settings.registryKey(key));
        registerBlockItem(id, block);
        return Registry.register(Registries.BLOCK, key, block);
    }

    public static void registerBlockItem(String id, Block block) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, SimpleFilters.idOf(id));
        Item item = new BlockItem(block, new Item.Settings().registryKey(key).useBlockPrefixedTranslationKey());
        Registry.register(Registries.ITEM, key, item);
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
            content.addAfter(Items.HOPPER, FILTER_BLOCK);
        });
    }
}
