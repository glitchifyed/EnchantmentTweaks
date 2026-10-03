package net.glitchifyed.enchantment_tweaks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantmentTweaks implements ModInitializer {

    public static final String MODID = "enchantment_tweaks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static ServerLevel currentServerLevel;

    @Override
    public void onInitialize() {
        ETGameRules.Register();

        ServerLevelEvents.LOAD.register(((server, level) -> {
            currentServerLevel = level;
        }));
    }


    public static void WarnPlayerBrokenItem(Player player, String text) {
        if (player == null)
            return;

        player.sendOverlayMessage(Component.literal(text == null ? "This item is broken and cannot be used!" : text).withColor(TextColor.RED));
    }

    public static void ForceSyncClient(Player player) {
        if (player == null)
            return;

        player.inventoryMenu.sendAllDataToRemote();
        player.containerMenu.sendAllDataToRemote();
    }
}
