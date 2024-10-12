package monster.giz.simple_filters.render.block.entity;

import monster.giz.simple_filters.blocks.FilterBlock;
import monster.giz.simple_filters.blocks.entity.FilterBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class FilterBlockEntityRenderer implements BlockEntityRenderer<FilterBlockEntity> {

    private ItemRenderer itemRenderer;
    public static final float VANILLA_BLOCK_ITEM_SCALE_FACTOR = 0.375f;

    public FilterBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super();
        this.itemRenderer = context.getItemRenderer();
    }

    // Yes, this is disgusting. No, I'm not messing with this anymore.
    // If you think you can do better, please PR it. Please.
    @Override
    public void render(FilterBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!entity.hasFilteredItem()) {
            return;
        }

        Item item = entity.getFilteredItem();
        Direction facing = entity.getCachedState().get(FilterBlock.FACING);
        BakedModel model = itemRenderer.getModel(item.getDefaultStack(), null, null, 0);

        float scale = 0.35F;

        // This is a design choice, wanted items that have the block item model to appear larger in the frame
        // for better visual clarity.
        if (item instanceof BlockItem) {
            if (hasBlockItemTransformations(model)) {
                scale = 0.5F;
            }
        }

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
        renderItem(model, item.getDefaultStack(), matrices, vertexConsumers, light, overlay);
    }

    private boolean hasBlockItemTransformations(BakedModel model) {
        if (model.getTransformation().thirdPersonRightHand.scale.x == VANILLA_BLOCK_ITEM_SCALE_FACTOR) {
            return true;
        }
        return false;
    }

    private void renderItem(BakedModel model, ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        itemRenderer.renderItem(stack, ModelTransformationMode.FIXED, false, matrices, vertexConsumers, light, overlay, model);
        matrices.pop();

    }

}