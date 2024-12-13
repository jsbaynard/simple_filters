package monster.giz.simple_filters.render.block.entity;

import monster.giz.simple_filters.blocks.FilterBlock;
import monster.giz.simple_filters.blocks.entity.FilterBlockEntity;
import monster.giz.simple_filters.client.access.BasicItemModelAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

import java.util.HashMap;

public class FilterBlockEntityRenderer implements BlockEntityRenderer<FilterBlockEntity> {

    private final ItemRenderer itemRenderer;
    public static final float VANILLA_BLOCK_ITEM_SCALE_FACTOR = 0.375f;
    private final BakedModelManager modelManager;
    private final HashMap<Item, Boolean> rescaleMap;

    public FilterBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super();
        this.itemRenderer = context.getItemRenderer();
        this.modelManager = MinecraftClient.getInstance().getBakedModelManager();
        this.rescaleMap = new HashMap<>();
    }

    // Yes, this is disgusting. No, I'm not messing with this anymore.
    // If you think you can do better, please PR it. Please.
    @Override
    public void render(FilterBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!entity.hasFilteredItem()) {
            return;
        }

        ItemStack stack = entity.getFilteredStack();
        Direction facing = entity.getCachedState().get(FilterBlock.FILTER_FACING);

        float scale = getScaleFactor(stack.getItem());

        switch (facing) {
            case WEST:
                matrices.translate(0.175, 0.31, 0.5);
                matrices.scale(scale, scale, scale);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                break;
            case EAST:
                matrices.translate(0.825, 0.31, 0.5);
                matrices.scale(scale, scale, scale);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270));
                break;
            case NORTH:
                matrices.translate(0.5, 0.31, 0.175);
                matrices.scale(scale, scale, scale);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                break;
            case SOUTH:
                matrices.translate(0.5, 0.31, 0.825);
                matrices.scale(scale, scale, scale);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(360));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                break;
        }
        renderItem(stack, matrices, vertexConsumers, light, overlay);
    }


    // Purpose of this is to make items with a block item model bigger for better visibilty.
    // BlockItem applies to items with 'generated' models too, hence the following logic.
    private float getScaleFactor(Item item) {
        if (!(item instanceof BlockItem)) {
            return 0.35F;
        }

        Boolean bool = rescaleMap.get(item);
        if (bool == null) {
            boolean result = false;
            Identifier id = Registries.ITEM.getId(item);
            ItemModel model = modelManager.getItemModel(id);

            if (model instanceof BasicItemModel basic) {
                if (((BasicItemModelAccess) basic).simplefilters$getModelRightHandScaleValue() == VANILLA_BLOCK_ITEM_SCALE_FACTOR) {
                    result = true;
                }
            }
            rescaleMap.put(item, result);
            return result ? 0.5F : 0.35F;
        }
        return bool ? 0.5F : 0.35F;
    }


    private void renderItem(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        itemRenderer.renderItem(null, stack, ModelTransformationMode.FIXED, false, matrices, vertexConsumers, null, light, overlay, 0);
        matrices.pop();
    }

}