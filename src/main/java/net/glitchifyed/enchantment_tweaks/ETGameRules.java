package net.glitchifyed.enchantment_tweaks;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public class ETGameRules {

    public static GameRule<Integer> MENDING_MAX_EXP_LEVEL;


    public static void Register() {
        MENDING_MAX_EXP_LEVEL = GameRuleBuilder
                .forInteger(50)
                .category(GameRuleCategory.MISC)
                .buildAndRegister(Identifier.fromNamespaceAndPath(EnchantmentTweaks.MODID, "mending_max_exp_level"));
    }

}
