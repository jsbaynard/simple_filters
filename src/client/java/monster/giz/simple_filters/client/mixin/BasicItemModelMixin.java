package monster.giz.simple_filters.client.mixin;

import monster.giz.simple_filters.client.ducks.ItemModelTransformationAccess;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.ModelSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;


@Mixin(BasicItemModel.class)
public abstract class BasicItemModelMixin implements ItemModel, ItemModelTransformationAccess {

    @Shadow
    public @Final ModelSettings settings;


    @Unique
    @Override
    public float simple_filters$getModelRightHandScaleValue() {
        return settings.transforms().thirdPersonRightHand().scale().x();
    }
}

