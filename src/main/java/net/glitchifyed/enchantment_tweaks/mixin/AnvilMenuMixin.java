package net.glitchifyed.enchantment_tweaks.mixin;


import net.glitchifyed.enchantment_tweaks.ETGameRules;
import net.glitchifyed.enchantment_tweaks.EnchantmentTweaks;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Mixin(AnvilMenu.class)
abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow @Final private DataSlot cost;
    @Shadow private int repairItemCountCost;

    @Shadow private String itemName;
    @Shadow private boolean onlyRenaming;

    @Unique private static final int INPUT_SLOT = 0;
    @Unique private static final int ADDITIONAL_SLOT = 1;
    @Unique private static final int OUTPUT_SLOT = 0;

    @Unique private boolean canPickup;

    public AnvilMenuMixin(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition itemInputSlots) {
        super(menuType, containerId, inventory, access, itemInputSlots);
    }

    @Inject(method = "mayPickup", at = @At("RETURN"), cancellable = true)
    public void mayPickup(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(canPickup && (player.hasInfiniteMaterials() || player.experienceLevel >= cost.get()));
    }

    @Inject(method = "onTake", at = @At("RETURN"))
    public void onTake(Player player, ItemStack carried, CallbackInfo ci) {
        EnchantmentTweaks.LOGGER.info(String.valueOf(cost.get()));

        EnchantmentTweaks.ForceSyncClient(player);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    public void createResult(CallbackInfo ci) {
        ItemStack inputStack = inputSlots.getItem(INPUT_SLOT);
        ItemStack additionalStack = inputSlots.getItem(ADDITIONAL_SLOT);

        ItemStack outputStack = ItemStack.EMPTY;

        int expCost = 0;
        int repairCost = 0;
        int repairDamage = 0;

        boolean modified = false;
        boolean renaming = false;

        boolean itemsAreEqual = inputStack.is(additionalStack.getItem());

        int repairFullCost = ETGameRules.GetValue(ETGameRules.REPAIR_FULL_COST);

        if (!inputStack.isEmpty()) {
            outputStack = inputStack.copy();

            var inputItemName = inputStack.getItemName().getString();
            var customNameComponent = inputStack.getCustomName();

            boolean itemNameContainsText = itemName != null && !itemName.isBlank();
            boolean customNameIsDifferent = customNameComponent == null && !Objects.equals(itemName, inputItemName) || customNameComponent != null && !Objects.equals(itemName, customNameComponent.getString());

            renaming = itemNameContainsText && customNameIsDifferent;

            if (renaming) {
                outputStack.set(DataComponents.CUSTOM_NAME, Component.literal(itemName));
            }

            if (!additionalStack.isEmpty()) {
                if (EnchantmentHelper.canStoreEnchantments(outputStack) && EnchantmentHelper.canStoreEnchantments(additionalStack) && (itemsAreEqual || !inputStack.is(Items.ENCHANTED_BOOK) && additionalStack.is(Items.ENCHANTED_BOOK))) {
                    ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(outputStack));

                    var inputEnchants = EnchantmentHelper.getEnchantmentsForCrafting(inputStack);
                    var additionalEnchants = EnchantmentHelper.getEnchantmentsForCrafting(additionalStack);

                    boolean secondaryIsBook = additionalStack.is(Items.ENCHANTED_BOOK);
                    boolean twoBooks = inputStack.is(Items.ENCHANTED_BOOK) && secondaryIsBook;

                    boolean acceptable = true;

                    for (var entry : additionalEnchants.keySet()) {
                        var enchantment = entry.value();

                        boolean incompatible = false;

                        for (var entry2 : inputEnchants.keySet()) {
                            if (entry != entry2 && !Enchantment.areCompatible(entry, entry2)) {
                                incompatible = true;

                                break;
                            }
                        }

                        if (incompatible)
                            continue;

                        if (!enchantment.canEnchant(outputStack) && !twoBooks) {
                            if (secondaryIsBook) {
                                continue;
                            }

                            acceptable = false;

                            break;
                        }

                        var level = additionalEnchants.getLevel(entry);
                        var primaryLevel = inputEnchants.getLevel(entry);

                        // takes the highest value or increases the level up to the max level if both are equal
                        var resultingLevel = level == primaryLevel ? Math.min(level + 1, enchantment.getMaxLevel()) : Math.max(level, primaryLevel);


                        builder.set(entry, resultingLevel);
                    }

                    if (acceptable) {
                        var outputComponent = builder.toImmutable();

                        int levelDifference = 0;

                        if (!inputEnchants.isEmpty() && !additionalEnchants.isEmpty()) {
                            int inputCost = getCostFromInputsAndOutput(inputEnchants, additionalEnchants, outputComponent);
                            int additionalCost = getCostFromInputsAndOutput(additionalEnchants, inputEnchants, outputComponent);

                            levelDifference = Math.min(inputCost, additionalCost);
                        }
                        else if (inputEnchants.isEmpty() != additionalEnchants.isEmpty()) {
                            for (var entry : outputComponent.keySet()) {
                                int inputLevel = inputEnchants.getLevel(entry);
                                int additionalLevel = additionalEnchants.getLevel(entry);

                                int diff = Math.abs(inputLevel - additionalLevel);

                                levelDifference += diff;
                            }
                        }

                        if (levelDifference > 0) {
                            modified = true;

                            expCost += levelDifference * ETGameRules.GetValue(ETGameRules.COMBINE_LEVEL_COST);
                        }

                        if (!modified && itemsAreEqual && inputStack.isDamageableItem() && (inputStack.getDamageValue() != 0 || additionalStack.getDamageValue() != 0)) {
                            modified = true;
                        }

                        EnchantmentHelper.setEnchantments(outputStack, outputComponent);
                    }
                }
                else
                    modified = inputStack.isValidRepairItem(additionalStack);

                if (modified) {
                    if (itemsAreEqual && inputStack.isDamageableItem()) {
                        repairDamage = additionalStack.getMaxDamage() - additionalStack.getDamageValue();
                    }
                    else if (inputStack.isValidRepairItem(additionalStack)) {
                        int maxDamage = inputStack.getMaxDamage();

                        int repairAmount = maxDamage / repairFullCost * additionalStack.getCount();
                        int maxRepairAmount = Math.min(repairAmount, inputStack.getDamageValue());

                        if (maxRepairAmount != 0) {
                            repairCost = (int) Math.ceil(maxRepairAmount / (float)maxDamage * repairFullCost);
                            repairDamage = maxRepairAmount;
                        }
                        else {
                            modified = false;
                        }
                    }
                }
            }
        }

        if (renaming) {
            if (modified)
                renaming = false;
            else
                modified = true;
        }

        if (modified) {
            if (repairDamage > 0) {
                int currentDamage = inputStack.getDamageValue();
                int newDamage = Math.max(currentDamage - repairDamage, 0);

                int diff = currentDamage - newDamage;
                if (diff > 0) {
                    outputStack.setDamageValue(newDamage);

                    if (ETGameRules.GetValue(ETGameRules.REPAIRS_COST_EXP)) {
                    int minDamage = additionalStack.isDamageableItem() ? Math.min(inputStack.getDamageValue(), additionalStack.getDamageValue()) : inputStack.getDamageValue();
                    int minDiff = minDamage - newDamage;

                    int levels = Math.round(minDiff / (float)inputStack.getMaxDamage() * repairFullCost);

                    expCost += levels;
                    }
                }
            }
        }
        else {
            outputStack = ItemStack.EMPTY;
        }

        resultSlots.setItem(OUTPUT_SLOT, outputStack);
        cost.set(expCost);
        repairItemCountCost = repairCost;
        onlyRenaming = renaming;

        canPickup = modified;

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundContainerSetDataPacket(containerId, 0, cost.get()));
        }

        ci.cancel();
    }

    @Unique
    private static int getCostFromInputsAndOutput(ItemEnchantments input, ItemEnchantments sacrifice, ItemEnchantments output) {
        int cost = 0;

        for (var entry : output.keySet()) {
            int outputLevel = output.getLevel(entry);
            int inputLevel = input.getLevel(entry);
            int sacrificeLevel = sacrifice.getLevel(entry);

            if (outputLevel > inputLevel) {
                cost += sacrificeLevel;
            }
        }

        return cost;
    }
}
