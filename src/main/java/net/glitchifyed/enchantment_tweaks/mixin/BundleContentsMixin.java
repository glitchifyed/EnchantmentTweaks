package net.glitchifyed.enchantment_tweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DataResult;
import net.glitchifyed.enchantment_tweaks.ETGameRules;
import net.glitchifyed.enchantment_tweaks.EnchantmentTweaks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(BundleContents.class)
public class BundleContentsMixin {
    @ModifyReturnValue(method = "getWeight", at = @At("RETURN"))
    private static DataResult<Fraction> getWeight(DataResult<Fraction> original, @Local(argsOnly = true, name = "item") ItemInstance item) {
        if (item.getMaxStackSize() == 1 && !EnchantmentTweaks.BundleStackSizeBlacklist.contains(item.typeHolder().value()))
            return DataResult.success(Fraction.getFraction(1, ETGameRules.GetValue(ETGameRules.BUNDLE_UNSTACKABLE_MAX)));

        return original;
    }
}
