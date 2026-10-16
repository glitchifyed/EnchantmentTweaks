package net.glitchifyed.enchantment_tweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.glitchifyed.enchantment_tweaks.EnchantmentTweaks;
import net.glitchifyed.enchantment_tweaks.UpdateBaseResult;
import net.glitchifyed.enchantment_tweaks.interfaces.BeaconBlockEntityInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin implements BeaconBlockEntityInterface {
    @Shadow private int levels;

    @Unique private double blockPower;

    @Override
    public double EnchantmentTweaks$GetBlockPower() {
        return blockPower;
    }

    @Override
    public void EnchantmentTweaks$SetBlockPower(double blockPower) {
        this.blockPower = blockPower;
    }

    @Override
    public int EnchantmentTweaks$GetLevels() {
        return levels;
    }

//    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;updateBase(Lnet/minecraft/world/level/Level;III)I"))
//    private static void tick(Level level, BlockPos pos, BlockState selfState, BeaconBlockEntity entity, CallbackInfo ci) {
//        EnchantmentTweaks.LOGGER.info("TICK");
//    }

    @Override
    public UpdateBaseResult EnchantmentTweaks$GetLevelsAndPower(Level level, int x, int y, int z) {
        int levels = 0;
        double power = 0;

        while (true) {
            levels++;

            int ly = y - levels;
            if (ly < level.getMinY())
                return new UpdateBaseResult(levels, power);

            double levelPower = 0;

            for (int lx = x - levels; lx <= x + levels; lx++) {
                for (int lz = z - levels; lz <= z + levels; lz++) {
                    var blockState = level.getBlockState(new BlockPos(lx, ly, lz));

                    if (!blockState.is(BlockTags.BEACON_BASE_BLOCKS))
                        return new UpdateBaseResult(levels, power);

                    levelPower += EnchantmentTweaks.BeaconBlockPowers.get(blockState.getBlock());
                }
            }

            power += levelPower / (EnchantmentTweaks.MAX_BEACON_BLOCK_POWER * Math.pow(levels * 2 + 1, 2));
        }
    }

    @Inject(method = "updateBase", at = @At("HEAD"), cancellable = true)
    private static void updateBase(Level level, int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        BeaconBlockEntityInterface beaconBlockEntityInterface = (BeaconBlockEntityInterface) level.getBlockEntity(new BlockPos(x, y, z));

        UpdateBaseResult result = beaconBlockEntityInterface.EnchantmentTweaks$GetLevelsAndPower(level, x, y, z);

        beaconBlockEntityInterface.EnchantmentTweaks$SetBlockPower(result.power);

        cir.setReturnValue(result.levels - 1);
    }

    @Inject(method = "applyEffects", at = @At("HEAD"), cancellable = true)
    private static void applyEffects(Level level, BlockPos worldPosition, int levels, @Nullable Holder<MobEffect> primaryPower, @Nullable Holder<MobEffect> secondaryPower, CallbackInfo ci) {
        if (level.isClientSide() || primaryPower == null) {
            ci.cancel();

            return;
        }

        BeaconBlockEntityInterface beaconBlockEntityInterface = (BeaconBlockEntityInterface) level.getBlockEntity(worldPosition);
        double power = beaconBlockEntityInterface.EnchantmentTweaks$GetBlockPower();

        double range = power * 100;

        int baseAmp = (int) power - 1;
        if (levels >= 4 && Objects.equals(primaryPower, secondaryPower)) {
            baseAmp++;
        }

        int durationTicks = (int) (power * 200);
        AABB bb = new AABB(worldPosition).inflate(range).expandTowards(0.0, level.getMinY(), 0.0).expandTowards(0.0, level.getMaxY(), 0.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, bb);

        for (Player player : players) {
            player.addEffect(new MobEffectInstance(primaryPower, durationTicks, baseAmp, true, true));
        }

        if (levels >= 4 && !Objects.equals(primaryPower, secondaryPower) && secondaryPower != null) {
            for (Player player : players) {
                player.addEffect(new MobEffectInstance(secondaryPower, durationTicks, 0, true, true));
            }
        }

        ci.cancel();
    }
}


