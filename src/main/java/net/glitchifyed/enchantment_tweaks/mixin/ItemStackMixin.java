package net.glitchifyed.enchantment_tweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.glitchifyed.enchantment_tweaks.ETGameRules;
import net.glitchifyed.enchantment_tweaks.EnchantmentTweaks;
import net.glitchifyed.enchantment_tweaks.interfaces.ItemStackInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackInterface {
    @Unique
    private Player currentPlayer;

    @Override
    public boolean EnchantmentTweaks$IsBroken() {
        ItemStack itemStack = (ItemStack) (Object) this;

        return itemStack.isDamageableItem() && itemStack.getDamageValue() >= itemStack.getMaxDamage();
    }

    @Unique
    public boolean IsItemElytraIncompatible() {
        if (currentPlayer == null || !currentPlayer.isFallFlying() || ETGameRules.GetValue(currentPlayer.level().dimension() == Level.END ? ETGameRules.ELYTRA_BOOSTING_IN_END : ETGameRules.ELYTRA_BOOSTING))
            return false;

        ItemStack itemStack = (ItemStack) (Object) this;

        if (itemStack.is(Items.FIREWORK_ROCKET)) {
            return true;
        }

//        if (itemStack.is(Items.TRIDENT) && EnchantmentTweaks.GetIsRaining() && EnchantmentTweaks.GetItemStackEnchantment(itemStack, Enchantments.RIPTIDE) > 0) {
//            return true;
//        }
//
//        if (itemStack.is(ItemTags.SPEARS) && EnchantmentTweaks.GetItemStackEnchantment(itemStack, Enchantments.LUNGE) > 0) {
//            return true;
//        }

        return false;
    }


    @ModifyReturnValue(method = "isBroken", at = @At("RETURN"))
    private boolean isBroken(boolean original) {
        if (currentPlayer == null)
            return original;

        return false;
    }

    @ModifyReturnValue(method = "nextDamageWillBreak", at = @At("RETURN"))
    private boolean nextDamageWillBreak(boolean original) {
        if (currentPlayer == null)
            return original;

        return false;
    }


    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void getCurrentPlayer(Level level, Entity owner, EquipmentSlot slot, CallbackInfo ci) {
        if (owner instanceof Player player)
            currentPlayer = player;
        else
            currentPlayer = null;
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (IsItemElytraIncompatible()) {
            EnchantmentTweaks.WarnPlayer(player, "Elytra boosting is disabled.");

            cir.setReturnValue(InteractionResult.FAIL);

            return;
        }

        if (EnchantmentTweaks$IsBroken()) {
            cir.setReturnValue(InteractionResult.FAIL);

            EnchantmentTweaks.WarnPlayer(player, null);
            EnchantmentTweaks.ForceSyncClient(player);
        }
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (EnchantmentTweaks$IsBroken()) {
            cir.setReturnValue(InteractionResult.FAIL);

            EnchantmentTweaks.WarnPlayer(context.getPlayer(), null);
            EnchantmentTweaks.ForceSyncClient(context.getPlayer());
        }
    }

    @Inject(method = "canDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void canDestroyBlock(BlockState state, Level level, BlockPos pos, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (EnchantmentTweaks$IsBroken()) {
            cir.setReturnValue(false);

            EnchantmentTweaks.WarnPlayer(player, null);
            EnchantmentTweaks.ForceSyncClient(player);
        }
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"), cancellable = true)
    private void forEachModifierOne(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> consumer, CallbackInfo ci) {
        if (EnchantmentTweaks$IsBroken()) {
            ci.cancel();

            EnchantmentTweaks.WarnPlayer(currentPlayer, null);
        }
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V", at = @At("HEAD"), cancellable = true)
    private void forEachModifierTwo(EquipmentSlotGroup slot, TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> consumer, CallbackInfo ci) {
        if (EnchantmentTweaks$IsBroken()) {
            ci.cancel();

            EnchantmentTweaks.WarnPlayer(currentPlayer, null);
        }
    }
}
