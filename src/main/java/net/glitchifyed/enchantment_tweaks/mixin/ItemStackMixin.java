package net.glitchifyed.enchantment_tweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.glitchifyed.enchantment_tweaks.EnchantmentTweaks;
import net.glitchifyed.enchantment_tweaks.interfaces.ItemStackInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public class ItemStackMixin implements ItemStackInterface {
    @Override
    public boolean EnchantmentTweaks$IsBroken() {
        ItemStack itemStack = (ItemStack) (Object) this;

        return itemStack.isDamageableItem() && itemStack.getDamageValue() >= itemStack.getMaxDamage();
    }


    @ModifyReturnValue(method = "isBroken", at = @At("RETURN"))
    private boolean isBroken(boolean original) {
        return false;
    }

    @ModifyReturnValue(method = "nextDamageWillBreak", at = @At("RETURN"))
    private boolean nextDamageWillBreak(boolean original) {
        return false;
    }


    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (EnchantmentTweaks$IsBroken()) {
            cir.setReturnValue(InteractionResult.FAIL);

            EnchantmentTweaks.WarnPlayerBrokenItem(player);
        }
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (EnchantmentTweaks$IsBroken()) {
            cir.setReturnValue(InteractionResult.FAIL);

            EnchantmentTweaks.WarnPlayerBrokenItem(context.getPlayer());
        }
    }

    @Inject(method = "canDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void canDestroyBlock(BlockState state, Level level, BlockPos pos, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (EnchantmentTweaks$IsBroken()) {
            cir.setReturnValue(false);

            EnchantmentTweaks.WarnPlayerBrokenItem(player);
        }
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"), cancellable = true)
    private void forEachModifierOne(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> consumer, CallbackInfo ci) {
        if (EnchantmentTweaks$IsBroken()) {
            ci.cancel();
        }
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V", at = @At("HEAD"), cancellable = true)
    private void forEachModifierTwo(EquipmentSlotGroup slot, TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> consumer, CallbackInfo ci) {
        if (EnchantmentTweaks$IsBroken()) {
            ci.cancel();
        }
    }
}
