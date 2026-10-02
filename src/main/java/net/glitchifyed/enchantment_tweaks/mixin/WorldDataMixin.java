package net.glitchifyed.enchantment_tweaks.mixin;

import net.glitchifyed.enchantment_tweaks.MinecartTweaks;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldData.class)
public interface WorldDataMixin {
    @Inject(method = "enabledFeatures", at = @At("RETURN"), cancellable = true)
    private void enabledFeatures(CallbackInfoReturnable<FeatureFlagSet> cir) {
        cir.setReturnValue(MinecartTweaks.getEnabledFeaturesConsideringGameRule(cir.getReturnValue()));
    }
}