package monster.giz.render.block.entity;

import monster.giz.blocks.FilterBlock;
import monster.giz.blocks.entity.FilterBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class FilterBlockEntityRenderer implements BlockEntityRenderer<FilterBlockEntity> {

    public FilterBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super();
    }

    // Yes, this is disgusting. No, I'm not messing with this anymore.
    @Override
    public void render(FilterBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.hasFilteredItem()) {
            Direction facing = entity.getCachedState().get(FilterBlock.FACING);
            switch(facing) {
                case WEST:
                    matrices.translate(0.16, 0.30, 0.5);
                    matrices.scale(0.35F, 0.35F, 0.35F);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                    break;
                case EAST:
                    matrices.translate(0.84, 0.30, 0.5);
                    matrices.scale(0.35F, 0.35F, 0.35F);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270));
                    break;
                case NORTH:
                    matrices.translate(0.5, 0.30, 0.16);
                    matrices.scale(0.35F, 0.35F, 0.35F);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                    break;
                case SOUTH:
                    matrices.translate(0.5, 0.30, 0.84);
                    matrices.scale(0.35F, 0.35F, 0.35F);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(360));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                    break;
            }
            renderItem(entity, matrices, vertexConsumers, light, overlay);
        }
    }

    private void renderItem(FilterBlockEntity be, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Item item = be.getFilteredItem();

        matrices.push();

        MinecraftClient.getInstance().getItemRenderer().renderItem(
                item.getDefaultStack(),
                ModelTransformationMode.FIXED,
                light,
                overlay,
                matrices,
                vertexConsumers,
                be.getWorld(),
                0
        );

        matrices.pop();

    }

}