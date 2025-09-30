package monster.giz.simple_filters.render.block.entity.state;

import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;

public class FilterBlockEntityRenderState extends BlockEntityRenderState {
    public ItemRenderState itemRenderState = new ItemRenderState();
    public boolean useGeneratedItemScale = false;
}