package net.glitchifyed.rebalancedmending.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrb.class)
public class ExperienceOrbMixin {
    @Inject(method = "repairPlayerItems", at = @At("HEAD"), cancellable = true)
    public void bypassMendingEffect(ServerPlayer player, int amount, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(amount);
    }
}
