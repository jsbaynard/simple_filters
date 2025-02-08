package monster.giz.simple_filters.render.block.entity;

import monster.giz.simple_filters.blocks.FilterBlock;
import monster.giz.simple_filters.blocks.entity.FilterBlockEntity;
import monster.giz.simple_filters.client.ducks.ItemModelTransformationAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

import java.util.HashMap;

public class FilterBlockEntityRenderer implements BlockEntityRenderer<FilterBlockEntity> {

    private final ItemRenderer itemRenderer;
    public static final float VANILLA_BLOCK_ITEM_SCALE_FACTOR = 0.375f;
    private final BakedModelManager modelManager;

    private final HashMap<Item, Boolean> rescaleMap = new HashMap<>();

    public FilterBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super();
        this.itemRenderer = context.getItemRenderer();
        this.modelManager = MinecraftClient.getInstance().getBakedModelManager();

        rescaleMap.put(Items.CHEST, true);
        rescaleMap.put(Items.TRAPPED_CHEST, true);
        rescaleMap.put(Items.ENDER_CHEST, true);
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
    // BlockItem can apply to items with 'generated' models too, hence the following logic.
    private float getScaleFactor(Item item) {
        Boolean cachedScale = rescaleMap.get(item);
        if (cachedScale != null) {
            return cachedScale ? 0.5F : 0.35F;
        }

        if (!(item instanceof BlockItem)) {
            rescaleMap.put(item, false);
            return 0.35F;
        }

        boolean result = isBlockItemModel(item);
        rescaleMap.put(item, result);

        return result ? 0.5F : 0.35F;
    }

    private boolean isBlockItemModel(Item item) {
        Identifier id = Registries.ITEM.getId(item);
        ItemModel model = modelManager.getItemModel(id);

        if (model instanceof BasicItemModel) {
            return ((ItemModelTransformationAccess) model)
                    .simple_filters$getModelRightHandScaleValue() == VANILLA_BLOCK_ITEM_SCALE_FACTOR;
        }
        return false;
    }


    private void renderItem(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        itemRenderer.renderItem(null, stack, ModelTransformationMode.FIXED, false, matrices, vertexConsumers, null, light, overlay, 0);
        matrices.pop();
    }

}