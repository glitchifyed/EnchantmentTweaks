package net.glitchifyed.enchantment_tweaks;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class MinecartTweaks {
    public static FeatureFlagSet modifyEnabledFeatures(FeatureFlagSet featureSet) {
        if (featureSet.contains(FeatureFlags.MINECART_IMPROVEMENTS))
            return featureSet;

        return featureSet.join(FeatureFlagSet.of(FeatureFlags.MINECART_IMPROVEMENTS));
    }

    public static FeatureFlagSet getEnabledFeaturesConsideringGameRule(FeatureFlagSet featureSet) {
        boolean gameRuleEnabled = EnchantmentTweaks.currentServerLevel != null && EnchantmentTweaks.currentServerLevel.getGameRules().get(ETGameRules.FORCE_EXPERIMENTAL_MINECARTS);
        FeatureFlagSet extraFeatureSet = FeatureFlagSet.of(FeatureFlags.MINECART_IMPROVEMENTS);

        if (featureSet.contains(FeatureFlags.MINECART_IMPROVEMENTS)) {
            if (gameRuleEnabled)
                return featureSet;

            return featureSet.subtract(extraFeatureSet);
        }

        if (!gameRuleEnabled)
            return featureSet;

        return featureSet.join(extraFeatureSet);
    }
}