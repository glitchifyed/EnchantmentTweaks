package net.glitchifyed.enchantment_tweaks;

import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantmentTweaks implements ModInitializer {

    public static final String MODID = "enchantment_tweaks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        ETGameRules.Register();
    }


    public static void WarnPlayerBrokenItem(Player player, String text) {
        if (player == null)
            return;

        ServerPlayer serverPlayer = (ServerPlayer) player;

        serverPlayer.sendOverlayMessage(Component.literal(text == null ? "This item is broken and cannot be used!" : text).withColor(TextColor.RED));
    }

    public static void ForceSyncClient(Player player) {
        if (player == null)
            return;

        ServerPlayer serverPlayer = (ServerPlayer) player;

        serverPlayer.inventoryMenu.sendAllDataToRemote();
        //serverPlayer.connection.send(new ClientboundUpdateMobEffectPacket(serverPlayer.getId(), MobEffects.WATER_BREATHING.))
    }
}
