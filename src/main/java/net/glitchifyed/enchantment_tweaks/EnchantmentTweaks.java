package net.glitchifyed.enchantment_tweaks;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantmentTweaks implements ModInitializer {

    public static final String MODID = "enchantment_tweaks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        ETGameRules.Register();
    }
}
