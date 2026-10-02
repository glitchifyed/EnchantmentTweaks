package net.glitchifyed.enchantment_tweaks.mixin;

import net.glitchifyed.enchantment_tweaks.MinecartTweaks;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRules.class)
public class GameRulesMixin {

    @ModifyVariable(method = "codec", at = @At("HEAD"), argsOnly = true, name = "enabledFeatures")
    private static FeatureFlagSet addMinecartImprovementsCodec(FeatureFlagSet enabledFeatures) {
        return MinecartTweaks.modifyEnabledFeatures(enabledFeatures);
    }

    @ModifyVariable(method = "<init>(Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/world/level/gamerules/GameRuleMap;)V", at = @At("HEAD"), argsOnly = true, name = "enabledFeatures")
    private static FeatureFlagSet addMinecartImprovementsDoubleArg(FeatureFlagSet enabledFeatures) {
        return MinecartTweaks.modifyEnabledFeatures(enabledFeatures);
    }

    @ModifyVariable(method = "<init>(Lnet/minecraft/world/flag/FeatureFlagSet;)V", at = @At("HEAD"), argsOnly = true, name = "enabledFeatures")
    private static FeatureFlagSet addMinecartImprovementsSingleArg(FeatureFlagSet enabledFeatures) {
        return MinecartTweaks.modifyEnabledFeatures(enabledFeatures);
    }

}