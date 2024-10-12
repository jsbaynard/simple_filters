package monster.giz.simple_filters.render.block.entity;

import monster.giz.simple_filters.blocks.FilterBlock;
import monster.giz.simple_filters.blocks.entity.FilterBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class FilterBlockEntityRenderer implements BlockEntityRenderer<FilterBlockEntity> {

    public FilterBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super();
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

        float scale = 0.35F;

        switch (facing) {
            case WEST:
                matrices.translate(0.175, 0.30, 0.5);
                matrices.scale(scale, scale, scale);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                break;
            case EAST:
                matrices.translate(0.825, 0.30, 0.5);
                matrices.scale(scale, scale, scale);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270));
                break;
            case NORTH:
                matrices.translate(0.5, 0.30, 0.175);
                matrices.scale(scale, scale, scale);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                break;
            case SOUTH:
                matrices.translate(0.5, 0.30, 0.825);
                matrices.scale(scale, scale, scale);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(360));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                break;
        }
        renderItem(item, entity.getWorld(), matrices, vertexConsumers, light, overlay);
    }

    private void renderItem(Item item, World world, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        matrices.push();

        MinecraftClient.getInstance().getItemRenderer().renderItem(
                item.getDefaultStack(),
                ModelTransformationMode.FIXED,
                light,
                overlay,
                matrices,
                vertexConsumers,
                world,
                0
        );

        matrices.pop();

    }

}