package net.glitchifyed.rebalancedmending.mixin;

import net.glitchifyed.rebalancedmending.RMGameRules;
import net.glitchifyed.rebalancedmending.RebalancedMending;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Unique
    private static final int TICKS_BETWEEN_REPAIRS_MAX = 400;
    @Unique
    private static final int TICKS_BETWEEN_REPAIRS_MIN = 80;
    @Unique
    private static final int DURABILITY_INCREASE_ON_REPAIR = 1;

    @Unique
    private int ticksSinceLastRepair;

    @Inject(method = "tick", at = @At("RETURN"))
    public void tickMending(CallbackInfo ci) {
        var player = (ServerPlayer) (Object) this;

        var maxExpLevel = player.level().getGameRules().get(RMGameRules.MENDING_MAX_EXP_LEVEL);
        var playerExpLevel = player.experienceLevel;
        var progress = Math.min(playerExpLevel / (float)maxExpLevel, 1);
        var ticksBetweenRepairs = (TICKS_BETWEEN_REPAIRS_MAX - (TICKS_BETWEEN_REPAIRS_MAX - TICKS_BETWEEN_REPAIRS_MIN) * progress);

        if (ticksSinceLastRepair < ticksBetweenRepairs) {
            ticksSinceLastRepair++;

            return;
        }

        ticksSinceLastRepair = 0;

        var inventory = player.getInventory();

        for (var itemStack : inventory) {
            if (!doesItemStackRepair(itemStack) || !itemStack.isDamaged())
                continue;

            itemStack.setDamageValue(Math.max(itemStack.getDamageValue() - DURABILITY_INCREASE_ON_REPAIR, 0));
        }
    }

    @Unique
    private static boolean doesItemStackRepair(ItemStack itemStack) {
        return EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.REPAIR_WITH_XP);
    }
}
