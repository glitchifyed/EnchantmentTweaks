package net.glitchifyed.enchantment_tweaks.interfaces;

import net.glitchifyed.enchantment_tweaks.UpdateBaseResult;
import net.minecraft.world.level.Level;

public interface BeaconBlockEntityInterface {
    double EnchantmentTweaks$GetBlockPower();
    int EnchantmentTweaks$GetLevels();

    void EnchantmentTweaks$SetBlockPower(double blockPower);

    UpdateBaseResult EnchantmentTweaks$GetLevelsAndPower(Level level, int x, int y, int z);
}
