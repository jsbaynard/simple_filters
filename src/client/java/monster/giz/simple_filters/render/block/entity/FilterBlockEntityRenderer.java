package monster.giz.simple_filters.render.block.entity;

import monster.giz.simple_filters.blocks.FilterBlock;
import monster.giz.simple_filters.blocks.entity.FilterBlockEntity;
import monster.giz.simple_filters.client.ducks.ItemModelTransformationAccess;
import monster.giz.simple_filters.render.block.entity.state.FilterBlockEntityRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;

public class FilterBlockEntityRenderer implements BlockEntityRenderer<FilterBlockEntity, FilterBlockEntityRenderState> {

    public static final float VANILLA_BLOCK_ITEM_SCALE_FACTOR = 0.375f;
    private final ItemModelManager itemModelManager;
    private final BakedModelManager bakedModelManager;

    private final HashMap<Item, Boolean> rescaleMap = new HashMap<>();

    public FilterBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemModelManager = context.itemModelManager();
        this.bakedModelManager = MinecraftClient.getInstance().getBakedModelManager();

        rescaleMap.put(Items.CHEST, true);
        rescaleMap.put(Items.TRAPPED_CHEST, true);
        rescaleMap.put(Items.ENDER_CHEST, true);
    }

    @Override
    public FilterBlockEntityRenderState createRenderState() {
        return new FilterBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(FilterBlockEntity filterBlockEntity, FilterBlockEntityRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(filterBlockEntity, state, tickProgress, cameraPos, crumblingOverlay);

        ItemStack stack = filterBlockEntity.getFilteredStack();
        ItemRenderState itemRenderState = new ItemRenderState();
        this.itemModelManager.clearAndUpdate(itemRenderState, stack, ItemDisplayContext.FIXED,
                filterBlockEntity.getWorld(), filterBlockEntity, 0);

        state.itemRenderState = itemRenderState;
        if (!isBlockItemModel(stack.getItem())) {
            state.useGeneratedItemScale = true;
        }
    }

    @Override
    public void render(FilterBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {
        Direction direction = state.blockState.get(FilterBlock.FILTER_FACING);
        renderItem(state, state.itemRenderState, matrices, queue, direction);
    }

    private void renderItem(FilterBlockEntityRenderState state, ItemRenderState itemRenderState, MatrixStack matrices, OrderedRenderCommandQueue queue, Direction direction) {
        float scale = state.useGeneratedItemScale ? 0.35f : 0.5f;
        matrices.push();
        switch (direction) {
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
        itemRenderState.render(matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
        matrices.pop();
    }

    private boolean isBlockItemModel(Item item) {
        Boolean cachedResult = rescaleMap.get(item);
        if (cachedResult != null) {
            return cachedResult;
        }

        if (!(item instanceof BlockItem)) {
            rescaleMap.put(item, false);
            return false;
        }

        Identifier itemId = Registries.ITEM.getId(item);
        ItemModel itemModel = bakedModelManager.getItemModel(itemId);

        boolean result = itemModel instanceof BasicItemModel
                && ((ItemModelTransformationAccess) itemModel)
                .simple_filters$getModelRightHandScaleValue() == VANILLA_BLOCK_ITEM_SCALE_FACTOR;

        rescaleMap.put(item, result);
        return result;
    }
}
