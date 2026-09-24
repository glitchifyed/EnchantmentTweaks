package net.glitchifyed.enchantment_tweaks.mixin;

import com.google.common.collect.Lists;
import net.glitchifyed.enchantment_tweaks.EnchantmentTweaks;
import net.glitchifyed.enchantment_tweaks.interfaces.ItemStackInterface;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "selectEnchantment", at = @At("HEAD"), cancellable = true)
    private static void selectEnchantment(RandomSource random, ItemStack itemStack, int enchantmentCost, Stream<Holder<Enchantment>> source, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        // this game is super moddable. put some spacing in here so i can read it.
        // i love working with bytecode reconstructions.
        List<EnchantmentInstance> results = Lists.newArrayList();
        Enchantable enchantable = itemStack.get(DataComponents.ENCHANTABLE);

        if (enchantable != null) {
            // ew this hurts to read. i know this isn't their actual codebase but oh my goodness even so this is unacceptable.
            // this is the most purchased game in the world, so why is this stuff still all over the code 12 years after mojang
            // was acquired by microsoft? seriously though, what am i looking at here? stop reading this; just look at it:
            // enchantmentCost += 1 + random.nextInt(enchantable.value() / 4 + 1) + random.nextInt(enchantable.value() / 4 + 1);
            // float randomSpan = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
            // enchantmentCost = Mth.clamp(Math.round((float)enchantmentCost + (float)enchantmentCost * randomSpan), 1, Integer.MAX_VALUE);

            float randomSpan = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;

            // MAKE IT ANOTHER VARIABLE SO I CAN TELL WHAT IT'S DOING
            // also, couldn't they have just used a single nextInt call with /2 instead of /4?
            // i won't change that, because i'm not totally sure how their custom (WHY) random works, and i don't care to find out.
            int effectivenessUnclamped = enchantmentCost +
                    1 + random.nextInt(enchantable.value() / 4 + 1) + random.nextInt(enchantable.value() / 4 + 1);

            // and now it can be clamped. also, why did they go x + x * alpha, when they could just do x * (1 + alpha)?
            int effectiveness = Mth.clamp(
                    Math.round((float)effectivenessUnclamped * (1 + randomSpan)),
                    1, Integer.MAX_VALUE
            );

            // i think availableEnchantments is more descriptive than plain-old enchantments
            List<EnchantmentInstance> availableEnchantments = EnchantmentHelper.getAvailableEnchantmentResults(effectiveness, itemStack, source);

            if (!availableEnchantments.isEmpty()) {
                // bytecode had no name for this variable. i know mine is long, but at least it's more descriptive
                Optional selectedEnchantmentFromPossibilities = WeightedRandom.getRandomItem(random, availableEnchantments, EnchantmentInstance::weight);
                Objects.requireNonNull(results);

                // pretty sure this does the same thing
                // enchantmentSelection.ifPresent(results::add);
                if (selectedEnchantmentFromPossibilities.isPresent())
                    results.add((EnchantmentInstance) selectedEnchantmentFromPossibilities.get());

                // uses the level requirement in the enchanting menu to get an amount of enchants applied (max 3)
                int baseEnchantmentCount = Math.min(enchantmentCost / 7, 3);
                // gets a random value between -1, 1 to add to the base count (to range from 1-4)
                // weighted from most to least common: 0 extras, -1, +1
                // so at lvl 30 OCCASIONALLY the player may get FOUR enchantments
                // and SOMETIMES they'll only get 2
                // but most commonly they'll get the default (3 for lvl 30)
                int randomCountAdjustment = Math.clamp(random.nextInt(-4, 3) / 2, -1, 1);
                // add these two values together and WHABAM
                int enchantmentCount = Math.max(baseEnchantmentCount + randomCountAdjustment, 1);

                // this was the single line i wanted to change:
                // while(random.nextInt(50) <= enchantmentCost) {
                for (int i = 1; i < enchantmentCount; i++) {
                    // yeah that's right this thing works completely differently
                    // i would never have designed these systems this way. it makes very little sense.
                    // legacy code meets modern game standards, i suppose.

                    if (!results.isEmpty()) {
                        // what's with the type casting when it's not needed?
                        EnchantmentHelper.filterCompatibleEnchantments(availableEnchantments, results.getLast());
                    }

                    if (availableEnchantments.isEmpty()) {
                        break;
                    }

                    selectedEnchantmentFromPossibilities = WeightedRandom.getRandomItem(random, availableEnchantments, EnchantmentInstance::weight);
                    Objects.requireNonNull(results);

                    // same thing again isn't that a surprise?
                    if (selectedEnchantmentFromPossibilities.isPresent())
                        results.add((EnchantmentInstance) selectedEnchantmentFromPossibilities.get());

                    //iteratorCapacity /= 2;
                }
            }
        }

        // we're finally at the end. oh, what a mess this was (and still is)
        // at least i know what to avoid in my own projects
        // .net also makes it harder to do this kind of jank (though it's obviously still very possible!)
        // not totally sure if my changes to the code even help at all. all these systems need a redesign-
        // -in the future. it is a gargantuan task, however, so i don't expect that to be done any time soon.
        cir.setReturnValue(results);
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"), cancellable = true)
    private static void forEachModifierOne(ItemStack itemStack, EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> consumer, CallbackInfo ci) {
        ItemStackInterface itemStackInterface = (ItemStackInterface) (Object) itemStack;

        if (itemStackInterface.EnchantmentTweaks$IsBroken())
            ci.cancel();
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"), cancellable = true)
    private static void forEachModifierTwo(ItemStack itemStack, EquipmentSlotGroup slot, BiConsumer<Holder<Attribute>, AttributeModifier> consumer, CallbackInfo ci) {
        ItemStackInterface itemStackInterface = (ItemStackInterface) (Object) itemStack;

        if (itemStackInterface.EnchantmentTweaks$IsBroken())
            ci.cancel();
    }
}
