package net.glitchifyed.enchantment_tweaks.mixin;

import net.glitchifyed.enchantment_tweaks.EnchantmentTweaks;
import net.glitchifyed.enchantment_tweaks.interfaces.ItemStackInterface;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Shadow
    public abstract Inventory getInventory();

    @Inject(method = "turtleHelmetTick", at = @At("HEAD"), cancellable = true)
    private void turtleHelmetTick(CallbackInfo ci) {
        // Helmet slot
        ItemStack helmetStack = getInventory().getItem(39);
        ItemStackInterface helmetStackExt = (ItemStackInterface) (Object) helmetStack;

        if (helmetStack.getItem() == Items.TURTLE_HELMET && helmetStackExt.EnchantmentTweaks$IsBroken()) {
            ci.cancel();

            ServerPlayer player = (ServerPlayer) (Object) this;

            EnchantmentTweaks.WarnPlayerBrokenItem(player, "Turtle Shell is broken and has no water breathing effect!");
        }
    }


}
