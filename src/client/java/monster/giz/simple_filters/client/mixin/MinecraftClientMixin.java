package monster.giz.simple_filters.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import monster.giz.simple_filters.SimpleFilters;
import monster.giz.simple_filters.blocks.FilterBlock;
import monster.giz.simple_filters.network.c2s.play.FilterFramePunchPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow @Nullable public HitResult crosshairTarget;
    @Shadow @Nullable public ClientWorld world;
    @Shadow @Nullable public ClientPlayerEntity player;

    // Vanilla Minecraft does not communicate the exact position a block is punched. Since I need the
    // built-in item frame to drop it's item when punched, I added a client side condition for checking
    // for this and sending a packet to the server.

    @Inject(
            method = "doAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;attackBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"), cancellable = true)
    public void simple_filters$captureFilterPunch(CallbackInfoReturnable<Boolean> cir, @Local BlockPos blockPos) {
        BlockHitResult result = (BlockHitResult) crosshairTarget;
        BlockState state = world.getBlockState(blockPos);
        if (state.getBlock() instanceof FilterBlock block) {
            if (block.hitEmbeddedItemFrame(state.get(FilterBlock.FILTER_FACING), result)) {
                ClientPlayNetworking.send(new FilterFramePunchPayload(blockPos));
                if (this.player.isCreative()) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

}
