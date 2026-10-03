package net.glitchifyed.enchantment_tweaks;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public class ETGameRules {

    public static GameRule<Integer> MENDING_MAX_EXP_LEVEL;

    public static GameRule<Boolean> FORCE_EXPERIMENTAL_MINECARTS;

    public static GameRule<Boolean> REPAIRS_COST_EXP;
    public static GameRule<Integer> REPAIR_FULL_COST;
    public static GameRule<Integer> COMBINE_LEVEL_COST;


    public static void Register() {
        MENDING_MAX_EXP_LEVEL = GameRuleBuilder
                .forInteger(50)
                .category(GameRuleCategory.MISC)
                .buildAndRegister(Identifier.fromNamespaceAndPath(EnchantmentTweaks.MODID, "mending_max_exp_level"));


        FORCE_EXPERIMENTAL_MINECARTS = GameRuleBuilder
                .forBoolean(false)
                .category(GameRuleCategory.MISC)
                .buildAndRegister(Identifier.fromNamespaceAndPath(EnchantmentTweaks.MODID, "force_experimental_minecarts"));


        REPAIRS_COST_EXP = GameRuleBuilder
                .forBoolean(false)
                .category(GameRuleCategory.MISC)
                .buildAndRegister(Identifier.fromNamespaceAndPath(EnchantmentTweaks.MODID, "repairs_cost_exp"));

        REPAIR_FULL_COST = GameRuleBuilder
                .forInteger(4)
                .category(GameRuleCategory.MISC)
                .buildAndRegister(Identifier.fromNamespaceAndPath(EnchantmentTweaks.MODID, "repair_full_cost"));

        COMBINE_LEVEL_COST = GameRuleBuilder
                .forInteger(2)
                .category(GameRuleCategory.MISC)
                .buildAndRegister(Identifier.fromNamespaceAndPath(EnchantmentTweaks.MODID, "combine_level_cost"));
    }

    public static <T> T GetValue(GameRule<T> gameRule) {
        if (EnchantmentTweaks.currentServerLevel == null)
            return gameRule.defaultValue();

        return EnchantmentTweaks.currentServerLevel.getGameRules().get(gameRule);
    }

}
