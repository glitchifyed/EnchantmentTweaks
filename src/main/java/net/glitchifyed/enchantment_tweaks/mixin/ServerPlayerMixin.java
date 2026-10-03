package net.glitchifyed.enchantment_tweaks.mixin;

import com.mojang.authlib.GameProfile;
import net.glitchifyed.enchantment_tweaks.ETGameRules;
import net.glitchifyed.enchantment_tweaks.EnchantmentTweaks;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Unique private static final int TICKS_BETWEEN_REPAIRS_MAX = 400;
    @Unique private static final int TICKS_BETWEEN_REPAIRS_MIN = 80;
    @Unique private static final int DURABILITY_INCREASE_ON_REPAIR = 1;

    @Unique private int ticksSinceLastRepair;
    @Unique private int ticksSinceFallFlying;

    @Unique private int stage;

    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo ci) {
        tickMending(ci);

        tickElytra(ci);
    }

    @Unique
    private void tickElytra(CallbackInfo ci) {
        if (!isFallFlying() || isInLiquid() || isInPowderSnow || !ETGameRules.GetValue(ETGameRules.ELYTRA_SICKNESS) || (!ETGameRules.GetValue(ETGameRules.ELYTRA_SICKNESS_IN_END) && level().dimension() == Level.END)) {
            ticksSinceFallFlying = 0;
            stage = 0;

            return;
        }

        ticksSinceFallFlying++;

        if (ticksSinceFallFlying < 100)
            return;

        if (stage < 1) {
            stage = 1;

            EnchantmentTweaks.WarnPlayer(this, "I don't feel so well..", TextColor.GREEN);
        }

        addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200));
        addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 49));

        if (ticksSinceFallFlying < 200)
            return;

        if (stage < 2) {
            stage = 2;

            EnchantmentTweaks.WarnPlayer(this, "I'm going to throw up..", TextColor.YELLOW);
        }

        addEffect(new MobEffectInstance(MobEffects.POISON, 100));

        if (ticksSinceFallFlying < 300)
            return;

        if (stage < 3) {
            stage = 3;

            addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20));

            EnchantmentTweaks.WarnPlayer(this, "I can't see..", TextColor.GOLD);
        }

        if (ticksSinceFallFlying < 400)
            return;

        if (stage < 4) {
            stage = 4;
            EnchantmentTweaks.WarnPlayer(this, "AHHHHH!!!!!!", TextColor.RED);
        }

        addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
    }

    @Unique
    private void tickMending(CallbackInfo ci) {
        var player = (ServerPlayer) (Object) this;

        var maxExpLevel = ETGameRules.GetValue(ETGameRules.MENDING_MAX_EXP_LEVEL);
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
