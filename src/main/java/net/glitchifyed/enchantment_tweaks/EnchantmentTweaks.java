package net.glitchifyed.enchantment_tweaks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class EnchantmentTweaks implements ModInitializer {

    public static final String MODID = "enchantment_tweaks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final List<Item> BundleStackSizeBlacklist = new ArrayList<>();

    public static final Dictionary<Block, Double> BeaconBlockPowers = new Hashtable<>();

    public static ServerLevel currentServerLevel;

    public static final double MAX_BEACON_BLOCK_POWER = 5.0;

    @Override
    public void onInitialize() {
        AddItemsToBundleBlacklist();
        AddPowersToBeaconBlocks();

        ETGameRules.Register();

        ServerLevelEvents.LOAD.register(((server, level) -> {
            currentServerLevel = level;
        }));
    }


    public static void AddItemsToBundleBlacklist() {
        BundleStackSizeBlacklist.add(Items.BUNDLE);
    }

    public static void AddPowersToBeaconBlocks() {
        BeaconBlockPowers.put(Blocks.IRON_BLOCK, 1.25);
        BeaconBlockPowers.put(Blocks.GOLD_BLOCK, 1.5);
        BeaconBlockPowers.put(Blocks.EMERALD_BLOCK, 2.0);
        BeaconBlockPowers.put(Blocks.DIAMOND_BLOCK, 3.0);
        BeaconBlockPowers.put(Blocks.NETHERITE_BLOCK, 5.0);
    }


    public static void WarnPlayer(Player player, String text, TextColor color) {
        if (player == null)
            return;

        player.sendOverlayMessage(Component.literal(text == null ? "This item is broken and cannot be used!" : text).withColor(color));
    }

    public static void WarnPlayer(Player player, String text) {
        WarnPlayer(player, text, TextColor.RED);
    }

    public static void ForceSyncClient(Player player) {
        if (player == null)
            return;

        player.inventoryMenu.sendAllDataToRemote();
        player.containerMenu.sendAllDataToRemote();
    }

//    public static int GetItemStackEnchantment(ItemStack itemStack, ResourceKey<Enchantment> enchantment) {
//        if (currentServerLevel == null)
//            return 0;
//
//        return itemStack.getEnchantments().getLevel(currentServerLevel.registryAccess().getOrThrow(enchantment));
//    }
//
//    public static boolean GetIsRaining() {
//        if (currentServerLevel == null)
//            return false;
//
//        return currentServerLevel.isRaining();
//    }
}
