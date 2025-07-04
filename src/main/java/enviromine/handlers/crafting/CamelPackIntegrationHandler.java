
package enviromine.handlers.crafting;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import enviromine.core.EM_Settings;

public class CamelPackIntegrationHandler implements IRecipe {

    boolean isRemove;
    public ItemStack pack;
    public ItemStack armor;

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        if (!inv.getInventoryName()
            .equals("container.crafting")) {
            return false;
        }

        boolean hasPack = false;
        boolean hasArmor = false;

        this.isRemove = false;
        this.pack = null;
        this.armor = null;

        for (int i = inv.getSizeInventory() - 1; i >= 0; i--) {
            ItemStack item = inv.getStackInSlot(i);
            if (item == null) {
                continue;
            } else if (item.hasTagCompound() && item.stackTagCompound.hasKey(EM_Settings.IS_CAMEL_PACK_TAG_KEY)) {
                if (hasPack || isRemove) {
                    return false;
                } else {
                    pack = item.copy();
                    hasPack = true;
                }
            } else if (item.getItem() instanceof ItemArmor && ((ItemArmor) item.getItem()).armorType == 1) {
                String name = Item.itemRegistry.getNameForObject(item.getItem());
                if (EM_Settings.armorProperties.containsKey(name)
                    && EM_Settings.armorProperties.get(name).allowCamelPack) {
                    if (hasArmor) {
                        return false;
                    } else {
                        if (item.hasTagCompound()
                            && item.stackTagCompound.hasKey(EM_Settings.CAMEL_PACK_FILL_TAG_KEY)) {
                            if (hasPack) {
                                return false;
                            } else {
                                isRemove = true;
                            }
                        }
                        armor = item.copy();
                        hasArmor = true;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        boolean tmp = (hasArmor && armor != null && (isRemove || (hasPack && pack != null)));
        return tmp;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        this.matches(inv, null);

        if (armor != null) {
            if (isRemove) {
                Object obj = Item.itemRegistry.getObject(
                    armor.getTagCompound()
                        .getString("packName"));
                if (obj instanceof Item) {
                    ItemStack out = new ItemStack((Item) obj);

                    out.setTagCompound(new NBTTagCompound());
                    out.getTagCompound()
                        .setInteger(
                            EM_Settings.CAMEL_PACK_FILL_TAG_KEY,
                            armor.getTagCompound()
                                .getInteger(EM_Settings.CAMEL_PACK_FILL_TAG_KEY));
                    out.getTagCompound()
                        .setInteger(
                            EM_Settings.CAMEL_PACK_MAX_TAG_KEY,
                            armor.getTagCompound()
                                .getInteger(EM_Settings.CAMEL_PACK_MAX_TAG_KEY));
                    out.getTagCompound()
                        .setBoolean(EM_Settings.IS_CAMEL_PACK_TAG_KEY, true);

                    return out;
                }
            } else {
                if (!armor.hasTagCompound()) {
                    armor.setTagCompound(new NBTTagCompound());
                }

                armor.getTagCompound()
                    .setInteger(
                        EM_Settings.CAMEL_PACK_FILL_TAG_KEY,
                        pack.getTagCompound()
                            .getInteger(EM_Settings.CAMEL_PACK_FILL_TAG_KEY));
                armor.getTagCompound()
                    .setInteger(
                        EM_Settings.CAMEL_PACK_MAX_TAG_KEY,
                        pack.getTagCompound()
                            .getInteger(EM_Settings.CAMEL_PACK_MAX_TAG_KEY));
                armor.getTagCompound()
                    .setString("packName", Item.itemRegistry.getNameForObject(pack.getItem()));

                return armor;
            }
        }

        return null;
    }

    @Override
    public int getRecipeSize() {
        return 4;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return null;
    }

    @SubscribeEvent
    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        IInventory craftMatrix = event.craftMatrix;
        if (!(craftMatrix instanceof InventoryCrafting) || !craftMatrix.getInventoryName()
            .equals("container.crafting")) {
            return;
        }

        if (this.matches((InventoryCrafting) craftMatrix, event.player.worldObj)) {
            if (isRemove) {
                for (int i = craftMatrix.getSizeInventory() - 1; i >= 0; i--) {
                    ItemStack slot = craftMatrix.getStackInSlot(i);

                    if (slot == null) {
                        continue;
                    } else if (slot.hasTagCompound() && slot.getTagCompound()
                        .hasKey(EM_Settings.CAMEL_PACK_FILL_TAG_KEY)) {
                            slot.stackSize++;
                            slot.getTagCompound()
                                .removeTag(EM_Settings.CAMEL_PACK_FILL_TAG_KEY);
                            slot.getTagCompound()
                                .removeTag(EM_Settings.CAMEL_PACK_MAX_TAG_KEY);
                            slot.getTagCompound()
                                .removeTag("packName");
                        }
                }
            }
        }
    }
}
