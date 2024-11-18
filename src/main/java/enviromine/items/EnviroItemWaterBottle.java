package enviromine.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import enviromine.EnviroPotion;
import enviromine.handlers.EM_StatusManager;
import enviromine.trackers.EnviroDataTracker;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class EnviroItemWaterBottle extends Item {
    public enum WATER_TYPES {
        FROSTY,
        DIRTY_COLD,
        CLEAN_COLD,
        SALTY,
        DIRTY,
        CLEAN, //Vanilla water bottle
        CLEAN_WARM,
        DIRTY_WARM,
        HOT
    }

    WATER_TYPES waterType;

    @SideOnly(Side.CLIENT)
    private IIcon field_94591_c;
    @SideOnly(Side.CLIENT)
    private IIcon field_94590_d;
    @SideOnly(Side.CLIENT)
    private IIcon field_94592_ct;

    public EnviroItemWaterBottle(WATER_TYPES waterType)
    {
        super();
        setTextureName("potion");
        this.waterType = waterType;
    }

    public WATER_TYPES getWaterType() {
        return waterType;
    }

    public static WATER_TYPES heatUp(WATER_TYPES waterType) {
        if(waterType == WATER_TYPES.FROSTY){
            return WATER_TYPES.CLEAN_COLD;
        }
        else if (waterType == WATER_TYPES.CLEAN_COLD){
            return WATER_TYPES.CLEAN;
        } else if (waterType == WATER_TYPES.DIRTY_COLD){
            return WATER_TYPES.DIRTY;
        }
        else if (waterType == WATER_TYPES.DIRTY) {
            return WATER_TYPES.CLEAN;
        }
        else if (waterType == WATER_TYPES.CLEAN) {
            return WATER_TYPES.CLEAN_WARM;
        }
        else if (waterType == WATER_TYPES.CLEAN_WARM) {
            return WATER_TYPES.HOT;
        }
        else if (waterType == WATER_TYPES.DIRTY_WARM) {
            return WATER_TYPES.HOT;
        } else if (waterType == WATER_TYPES.HOT) {
            return WATER_TYPES.HOT;
        } else {
            return WATER_TYPES.CLEAN;
        }
    }

    public static WATER_TYPES coolDown(WATER_TYPES waterType) {
        if (waterType == WATER_TYPES.FROSTY){
            return WATER_TYPES.FROSTY;
        }
        else if (waterType == WATER_TYPES.CLEAN_COLD){
            return WATER_TYPES.FROSTY;
        } else if (waterType == WATER_TYPES.DIRTY_COLD){
            return WATER_TYPES.FROSTY;
        }
        else if (waterType == WATER_TYPES.DIRTY) {
            return WATER_TYPES.DIRTY_COLD;
        }
        else if (waterType == WATER_TYPES.CLEAN) {
            return WATER_TYPES.CLEAN_COLD;
        }
        else if (waterType == WATER_TYPES.CLEAN_WARM) {
            return WATER_TYPES.CLEAN;
        }
        else if (waterType == WATER_TYPES.DIRTY_WARM) {
            return WATER_TYPES.DIRTY;
        }
        else if (waterType == WATER_TYPES.HOT) {
            return WATER_TYPES.CLEAN_WARM;
        } else {
            return WATER_TYPES.CLEAN;
        }
    }

    @Override
    public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        if(!par3EntityPlayer.capabilities.isCreativeMode)
        {
            --par1ItemStack.stackSize;
        }

        if(!par2World.isRemote)
        {
            EnviroDataTracker tracker = EM_StatusManager.lookupTracker(par3EntityPlayer);

            if(tracker != null)
            {

                if(tracker.bodyTemp >= 0)
                {
                    if(waterType == WATER_TYPES.FROSTY) {
                        if (tracker.bodyTemp >= 30.1) {
                            tracker.bodyTemp -= 0.5F;
                        }
                        tracker.hydrate(25F);
                    } else if (waterType == WATER_TYPES.DIRTY_COLD) {
                        if (tracker.bodyTemp >= 30.1) {
                            tracker.bodyTemp -= 0.1F;
                        }
                        tracker.hydrate(25F);
                    } else if (waterType == WATER_TYPES.CLEAN_COLD) {
                        if(tracker.bodyTemp >= 30.1)
                        {
                            tracker.bodyTemp -= 0.1F;
                        }
                        tracker.hydrate(25F);
                    } else if (waterType == WATER_TYPES.SALTY) {
                        if(par3EntityPlayer.getRNG().nextInt(1) == 0)
                        {
                            if(par3EntityPlayer.getActivePotionEffect(EnviroPotion.dehydration) != null && par3EntityPlayer.getRNG().nextInt(5) == 0)
                            {
                                int amp = par3EntityPlayer.getActivePotionEffect(EnviroPotion.dehydration).getAmplifier();
                                par3EntityPlayer.addPotionEffect(new PotionEffect(EnviroPotion.dehydration.id, 600, amp + 1));
                            } else
                            {
                                par3EntityPlayer.addPotionEffect(new PotionEffect(EnviroPotion.dehydration.id, 600));
                            }
                        }
                            if(tracker.bodyTemp > 37.05F)
                            {
                                tracker.bodyTemp -= 0.05F;
                            }
                            tracker.hydrate(10F);
                    } else if (waterType == WATER_TYPES.DIRTY) {
                        if(par3EntityPlayer.getRNG().nextInt(4) == 0)
                        {
                            par3EntityPlayer.addPotionEffect(new PotionEffect(Potion.hunger.id, 600));
                        }
                        if(par3EntityPlayer.getRNG().nextInt(4) == 0)
                        {
                            par3EntityPlayer.addPotionEffect(new PotionEffect(Potion.poison.id, 200));
                        }
                        if(tracker.bodyTemp > 37.05F)
                        {
                            tracker.bodyTemp -= 0.05F;
                        }
                        tracker.hydrate(25F);
                    } else if (waterType == WATER_TYPES.CLEAN_WARM) {
                        tracker.bodyTemp += 0.1F;
                        tracker.hydrate(25F);
                    } else if (waterType == WATER_TYPES.DIRTY_WARM) {
                        if(par3EntityPlayer.getRNG().nextInt(4) == 0)
                        {
                            par3EntityPlayer.addPotionEffect(new PotionEffect(Potion.hunger.id, 600));
                        }
                        if(par3EntityPlayer.getRNG().nextInt(4) == 0)
                        {
                            par3EntityPlayer.addPotionEffect(new PotionEffect(Potion.poison.id, 200));
                        }
                            if(tracker.bodyTemp >= 0)
                            {
                                tracker.bodyTemp += 0.1F;
                                tracker.hydrate(25F);
                            }
                    } else if (waterType == WATER_TYPES.HOT) {
                        tracker.bodyTemp += 0.5F;
                        tracker.hydrate(25F);
                    }
                }
            }
        }

        if(!par3EntityPlayer.capabilities.isCreativeMode)
        {
            if(par1ItemStack.stackSize <= 0)
            {
                return new ItemStack(Items.glass_bottle);
            }

            par3EntityPlayer.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));
        }

        return par1ItemStack;
    }

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
    @Override
    public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
    /**
     * Gets an icon index based on an item's damage value
     */
    @Override
    public IIcon getIconFromDamage(int par1)
    {
        return this.field_94590_d;
    }

    @SideOnly(Side.CLIENT)
    public int getColorFromDamage(int par1)
    {
        return PotionHelper.func_77915_a(par1, false);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2)
    {
        return par2 > 0 ? 16777215 : this.getColorFromDamage(par1ItemStack.getItemDamage());
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.drink;
    }

    /**
     * How long it takes to use or consume an item
     */
    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 32;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
        return par1ItemStack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister)
    {
        this.field_94590_d = par1IconRegister.registerIcon(this.getIconString() + "_" + "bottle_drinkable");
        this.field_94591_c = par1IconRegister.registerIcon(this.getIconString() + "_" + "bottle_splash");
        this.field_94592_ct = par1IconRegister.registerIcon(this.getIconString() + "_" + "overlay");
    }

    /**
     * Gets an icon index based on an item's damage value and the given render pass
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int par1, int par2)
    {
        return par2 == 0 ? this.field_94592_ct : super.getIconFromDamageForRenderPass(par1, par2);
    }

    @SideOnly(Side.CLIENT)
    public static IIcon func_94589_d(String par0Str)
    {
        return ItemPotion.func_94589_d(par0Str);
    }

}
