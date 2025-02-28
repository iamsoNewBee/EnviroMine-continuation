package enviromine.handlers;

import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import enviromine.EntityPhysicsBlock;
import enviromine.blocks.BlockBurningCoal;
import enviromine.blocks.BlockDavyLamp;
import enviromine.blocks.BlockElevator;
import enviromine.blocks.BlockEsky;
import enviromine.blocks.BlockFireTorch;
import enviromine.blocks.BlockFlammableCoal;
import enviromine.blocks.BlockFreezer;
import enviromine.blocks.BlockGas;
import enviromine.blocks.BlockNoPhysics;
import enviromine.blocks.materials.MaterialElevator;
import enviromine.blocks.materials.MaterialGas;
import enviromine.blocks.tiles.TileEntityBurningCoal;
import enviromine.blocks.tiles.TileEntityDavyLamp;
import enviromine.blocks.tiles.TileEntityElevator;
import enviromine.blocks.tiles.TileEntityEsky;
import enviromine.blocks.tiles.TileEntityFreezer;
import enviromine.blocks.tiles.TileEntityGas;
import enviromine.core.EM_ConfigHandler.EnumLogVerbosity;
import enviromine.core.EM_Settings;
import enviromine.core.EnviroMine;
import enviromine.items.EnviroArmor;
import enviromine.items.EnviroItemWaterBottle;
import enviromine.items.ItemDavyLamp;
import enviromine.items.ItemElevator;
import enviromine.items.ItemSpoiledMilk;
import enviromine.items.RottenFood;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashMap;

public class ObjectHandler
{
	public static HashMap<Block, ArrayList<Integer>> igniteList = new HashMap<Block, ArrayList<Integer>>();
	public static ArmorMaterial camelPackMaterial;

    public static Item frostyWaterBottle;
    public static Item coldWaterBottle;
    public static Item badColdWaterBottle;
    public static Item saltWaterBottle;
    public static Item badWaterBottle;
    public static Item warmWaterBottle;
    public static Item badWarmWaterBottle;
    public static Item hotWaterBottle;

	public static Item airFilter;
	public static Item davyLamp;
	public static Item gasMeter;
	public static Item rottenFood;
	public static Item spoiledMilk;

	public static ItemArmor camelPack;
	public static ItemArmor gasMask;
	public static ItemArmor hardHat;

	public static Block davyLampBlock;
	public static Block elevator;
	public static Block gasBlock;
	public static Block fireGasBlock;

	public static Block flammableCoal;
	public static Block burningCoal;
	public static Block fireTorch;
	public static Block offTorch;

	public static Block esky;
	public static Block freezer;

	public static Block noPhysBlock;

	public static int renderGasID;
	public static int renderSpecialID;

	public static Material gasMat;
	public static Material elevatorMat;

	public static void initItems()
	{

        frostyWaterBottle = new EnviroItemWaterBottle(EnviroItemWaterBottle.WATER_TYPES.FROSTY).setMaxStackSize(1).setUnlocalizedName("enviromine.frostywater").setCreativeTab(EnviroMine.enviroTab);
        coldWaterBottle = new EnviroItemWaterBottle(EnviroItemWaterBottle.WATER_TYPES.CLEAN_COLD).setMaxStackSize(1).setUnlocalizedName("enviromine.coldwater").setCreativeTab(EnviroMine.enviroTab);
        badColdWaterBottle = new EnviroItemWaterBottle(EnviroItemWaterBottle.WATER_TYPES.DIRTY_COLD).setMaxStackSize(1).setUnlocalizedName("enviromine.badcoldwater").setCreativeTab(EnviroMine.enviroTab);
        saltWaterBottle = new EnviroItemWaterBottle(EnviroItemWaterBottle.WATER_TYPES.SALTY).setMaxStackSize(1).setUnlocalizedName("enviromine.saltwater").setCreativeTab(EnviroMine.enviroTab);
        badWaterBottle = new EnviroItemWaterBottle(EnviroItemWaterBottle.WATER_TYPES.DIRTY).setMaxStackSize(1).setUnlocalizedName("enviromine.badwater").setCreativeTab(EnviroMine.enviroTab);
        warmWaterBottle = new EnviroItemWaterBottle(EnviroItemWaterBottle.WATER_TYPES.CLEAN_WARM).setMaxStackSize(1).setUnlocalizedName("enviromine.warmwater").setCreativeTab(EnviroMine.enviroTab);
        badWarmWaterBottle = new EnviroItemWaterBottle(EnviroItemWaterBottle.WATER_TYPES.DIRTY_WARM).setMaxStackSize(1).setUnlocalizedName("enviromine.badwarmwater").setCreativeTab(EnviroMine.enviroTab);
        hotWaterBottle = new EnviroItemWaterBottle(EnviroItemWaterBottle.WATER_TYPES.HOT).setMaxStackSize(1).setUnlocalizedName("enviromine.hotwater").setCreativeTab(EnviroMine.enviroTab);

        airFilter = new Item().setMaxStackSize(16).setUnlocalizedName("enviromine.airfilter").setCreativeTab(EnviroMine.enviroTab).setTextureName("enviromine:air_filter");
		rottenFood = new RottenFood(1).setMaxStackSize(64).setUnlocalizedName("enviromine.rottenfood").setCreativeTab(EnviroMine.enviroTab).setTextureName("enviromine:rot");
		spoiledMilk = new ItemSpoiledMilk().setUnlocalizedName("enviromine.spoiledmilk").setCreativeTab(EnviroMine.enviroTab).setTextureName("bucket_milk");

		camelPackMaterial = EnumHelper.addArmorMaterial("camelPack", EM_Settings.camelPackMax, new int[]{2, 2, 0, 0}, 0);

		camelPack = (ItemArmor)new EnviroArmor(camelPackMaterial, 4, 1).setTextureName("camel_pack").setUnlocalizedName("enviromine.camelpack").setCreativeTab(null);

		gasMask = (ItemArmor)new EnviroArmor(camelPackMaterial, 4, 0).setTextureName("gas_mask").setUnlocalizedName("enviromine.gasmask").setCreativeTab(null);
		hardHat = (ItemArmor)new EnviroArmor(camelPackMaterial, 4, 0).setTextureName("hard_hat").setUnlocalizedName("enviromine.hardhat").setCreativeTab(EnviroMine.enviroTab);
	}

	public static void registerItems()
	{
		GameRegistry.registerItem(badWaterBottle, "badWaterBottle");
		GameRegistry.registerItem(saltWaterBottle, "saltWaterBottle");
		GameRegistry.registerItem(coldWaterBottle, "coldWaterBottle");

        GameRegistry.registerItem(warmWaterBottle, "warmWaterBottle");
        GameRegistry.registerItem(badColdWaterBottle, "badColdWaterBottle");
        GameRegistry.registerItem(badWarmWaterBottle, "badWarmWaterBottle");
        GameRegistry.registerItem(frostyWaterBottle, "frostyWaterBottle");
        GameRegistry.registerItem(hotWaterBottle, "hotWaterBottle");

        GameRegistry.registerItem(airFilter, "airFilter");
		GameRegistry.registerItem(rottenFood, "rottenFood");
		GameRegistry.registerItem(spoiledMilk, "spoiledMilk");
		GameRegistry.registerItem(camelPack, "camelPack");
		GameRegistry.registerItem(gasMask, "gasMask");
		GameRegistry.registerItem(hardHat, "hardHat");

		// Empty Pack
		ItemStack camelStack1 = new ItemStack(camelPack);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger(EM_Settings.CAMEL_PACK_FILL_TAG_KEY, 0);
		tag.setInteger(EM_Settings.CAMEL_PACK_MAX_TAG_KEY, EM_Settings.camelPackMax);
		tag.setBoolean(EM_Settings.IS_CAMEL_PACK_TAG_KEY, true);
		tag.setString("camelPath", Item.itemRegistry.getNameForObject(camelPack));
		camelStack1.setTagCompound(tag);
		EnviroMine.enviroTab.addRawStack(camelStack1);

		// Full Pack
		ItemStack camelStack2 = new ItemStack(camelPack);
		tag = new NBTTagCompound();
		tag.setInteger(EM_Settings.CAMEL_PACK_FILL_TAG_KEY, EM_Settings.camelPackMax);
		tag.setInteger(EM_Settings.CAMEL_PACK_MAX_TAG_KEY, EM_Settings.camelPackMax);
		tag.setBoolean(EM_Settings.IS_CAMEL_PACK_TAG_KEY, true);
		tag.setString("camelPath", Item.itemRegistry.getNameForObject(camelPack));
		camelStack2.setTagCompound(tag);
		EnviroMine.enviroTab.addRawStack(camelStack2);

		// Empty Mask
		ItemStack mask = new ItemStack(gasMask);
		tag = new NBTTagCompound();
		tag.setInteger(EM_Settings.GAS_MASK_FILL_TAG_KEY, 0);
		tag.setInteger(EM_Settings.GAS_MASK_MAX_TAG_KEY, EM_Settings.gasMaskMax);
		mask.setTagCompound(tag);
		EnviroMine.enviroTab.addRawStack(mask);

		// Full Mask
		mask = new ItemStack(gasMask);
		tag = new NBTTagCompound();
		tag.setInteger(EM_Settings.GAS_MASK_FILL_TAG_KEY, EM_Settings.gasMaskMax);
		tag.setInteger(EM_Settings.GAS_MASK_MAX_TAG_KEY, EM_Settings.gasMaskMax);
		mask.setTagCompound(tag);
		EnviroMine.enviroTab.addRawStack(mask);
	}

	public static void initBlocks()
	{
		gasMat = new MaterialGas(MapColor.airColor);
		gasBlock = new BlockGas(gasMat).setBlockName("enviromine.gas").setCreativeTab(EnviroMine.enviroTab).setBlockTextureName("enviromine:gas_block");
		fireGasBlock = new BlockGas(gasMat).setBlockName("enviromine.firegas").setCreativeTab(EnviroMine.enviroTab).setBlockTextureName("enviromine:gas_block").setLightLevel(1.0F);

		elevatorMat = new MaterialElevator(MapColor.ironColor);
		elevator = new BlockElevator(elevatorMat).setBlockName("enviromine.elevator").setCreativeTab(EnviroMine.enviroTab).setBlockTextureName("iron_block");

		davyLampBlock = new BlockDavyLamp(Material.redstoneLight).setLightLevel(1.0F).setBlockName("enviromine.davy_lamp").setCreativeTab(EnviroMine.enviroTab);
		davyLamp = new ItemDavyLamp(davyLampBlock).setUnlocalizedName("enviromine.davylamp").setCreativeTab(EnviroMine.enviroTab);

		flammableCoal = new BlockFlammableCoal();
		burningCoal = new BlockBurningCoal(Material.rock).setBlockName("enviromine.burningcoal").setCreativeTab(EnviroMine.enviroTab);
		fireTorch = new BlockFireTorch(true).setTickRandomly(true).setBlockName("torch").setBlockTextureName("torch_on").setLightLevel(0.9375F).setCreativeTab(EnviroMine.enviroTab);
		offTorch = new BlockFireTorch(false).setTickRandomly(false).setBlockName("torch").setBlockTextureName("enviromine:torch_off").setLightLevel(0F).setCreativeTab(EnviroMine.enviroTab);
		esky = new BlockEsky(Material.iron).setBlockName("enviromine.esky").setCreativeTab(EnviroMine.enviroTab);
		freezer = new BlockFreezer(Material.iron).setBlockName("enviromine.freezer").setCreativeTab(EnviroMine.enviroTab);

		noPhysBlock = new BlockNoPhysics();

		Blocks.redstone_torch.setLightLevel(0.9375F);
	}

	public static void registerBlocks()
	{
		GameRegistry.registerBlock(gasBlock, "gas");
		GameRegistry.registerBlock(fireGasBlock, "firegas");
		GameRegistry.registerBlock(elevator, ItemElevator.class, "elevator");
		GameRegistry.registerBlock(davyLampBlock, ItemDavyLamp.class, "davy_lamp");
		GameRegistry.registerBlock(fireTorch, "firetorch");
		GameRegistry.registerBlock(offTorch, "offtorch");
		GameRegistry.registerBlock(burningCoal, "burningcoal");
		GameRegistry.registerBlock(flammableCoal, "flammablecoal");
		GameRegistry.registerBlock(esky, "esky");
		GameRegistry.registerBlock(freezer, "freezer");
		GameRegistry.registerBlock(noPhysBlock, "no_phys_block");

		// Must be done after registration
		Blocks.fire.setFireInfo(flammableCoal, 60, 100);

		// Ore Dictionary Stuffs
		OreDictionary.registerOre("oreCoal", flammableCoal);
	}

	public static void registerGases()
	{
	}

	public static void registerEntities()
	{
		int physID = EntityRegistry.findGlobalUniqueEntityId();
		EntityRegistry.registerGlobalEntityID(EntityPhysicsBlock.class, "EnviroPhysicsBlock", physID);
		EntityRegistry.registerModEntity(EntityPhysicsBlock.class, "EnviroPhysicsEntity", physID, EnviroMine.instance, 64, 1, true);
		GameRegistry.registerTileEntity(TileEntityGas.class, "enviromine.tile.gas");
		GameRegistry.registerTileEntity(TileEntityBurningCoal.class, "enviromine.tile.burningcoal");
		GameRegistry.registerTileEntity(TileEntityEsky.class, "enviromine.tile.esky");
		GameRegistry.registerTileEntity(TileEntityFreezer.class, "enviromine.tile.freezer");

		GameRegistry.registerTileEntity(TileEntityElevator.class, "enviromine.tile.elevator");


		GameRegistry.registerTileEntity(TileEntityDavyLamp.class, "enviromine.tile.davy_lamp");
	}

    public static ItemStack getItemStackFromWaterType(EnviroItemWaterBottle.WATER_TYPES type) {
        if(type == EnviroItemWaterBottle.WATER_TYPES.FROSTY) {
            return new ItemStack(frostyWaterBottle);
        } else if(type == EnviroItemWaterBottle.WATER_TYPES.DIRTY_COLD) {
            return new ItemStack(badColdWaterBottle);
        } else if(type == EnviroItemWaterBottle.WATER_TYPES.CLEAN_COLD) {
            return new ItemStack(coldWaterBottle);
        } else if(type == EnviroItemWaterBottle.WATER_TYPES.SALTY) {
            return new ItemStack(saltWaterBottle);
        } else if(type == EnviroItemWaterBottle.WATER_TYPES.DIRTY) {
            return new ItemStack(badWaterBottle);
        } else if(type == EnviroItemWaterBottle.WATER_TYPES.CLEAN) {
            return new ItemStack(Items.potionitem, 1, 0);
        } else if(type == EnviroItemWaterBottle.WATER_TYPES.CLEAN_WARM) {
            return new ItemStack(warmWaterBottle);
        } else if(type == EnviroItemWaterBottle.WATER_TYPES.DIRTY_WARM) {
            return new ItemStack(badWarmWaterBottle);
        } else if(type == EnviroItemWaterBottle.WATER_TYPES.HOT) {
            return new ItemStack(hotWaterBottle);
        } else {
            return new ItemStack(Items.potionitem, 1, 0);
        }
    }

	public static void registerRecipes()
	{
		String oreKeyAuBrass = "ingotGoldBrass";

		for (String oreDictName : new String[]{"ingotGold", "ingotBrass", "ingotAluminumBrass", "ingotAluminiumBrass"})
		{
            for (ItemStack oreItemStack : OreDictionary.getOres(oreDictName)) {
                OreDictionary.registerOre(oreKeyAuBrass, oreItemStack);
            }
		}

        ItemStack[] bottles = {
            new ItemStack(frostyWaterBottle),
            new ItemStack(badColdWaterBottle),
            new ItemStack(coldWaterBottle),
            new ItemStack(saltWaterBottle),
            new ItemStack(badWaterBottle),
            new ItemStack(Items.potionitem, 1, 0),
            new ItemStack(warmWaterBottle),
            new ItemStack(badWarmWaterBottle),
            new ItemStack(hotWaterBottle),
        };

        //HEATING
        for(ItemStack bottle : bottles) {
            if(bottle.getItem() != hotWaterBottle) { //How can you heat already hot water?
                EnviroItemWaterBottle.WATER_TYPES localType = EnviroItemWaterBottle.WATER_TYPES.CLEAN;

                if (bottle.equals(new ItemStack(Items.potionitem, 1, 0))) {
                    localType = EnviroItemWaterBottle.WATER_TYPES.CLEAN;
                } else if (bottle.getItem() instanceof EnviroItemWaterBottle enviroItemWaterBottle) {
                    localType = enviroItemWaterBottle.getWaterType();
                }

                GameRegistry.addSmelting(bottle, getItemStackFromWaterType(EnviroItemWaterBottle.heatUp(localType)), 0.0F);
            }
        }

//        GameRegistry.addSmelting(saltWaterBottle, new ItemStack(Items.potionitem, 1, 0), 0.0F);
//        GameRegistry.addSmelting(warmWaterBottle, new ItemStack(hotWaterBottle, 1, 0), 0.0F);                                               //WARM     - HOT
//        GameRegistry.addSmelting(badWarmWaterBottle, new ItemStack(hotWaterBottle, 1, 0), 0.0F);                                            //BAD WARM - HOT
//        GameRegistry.addSmelting(new ItemStack(Items.potionitem, 1, 0), new ItemStack(warmWaterBottle, 1, 0), 0.0F);      //CLEAN    - WARM
//        GameRegistry.addSmelting(badWaterBottle, new ItemStack(Items.potionitem, 1, 0), 0.0F);                                              //BAD      - CLEAN
//        GameRegistry.addSmelting(coldWaterBottle, new ItemStack(Items.potionitem, 1, 0), 0.0F);                                             //COLD     - CLEAN
//        GameRegistry.addSmelting(badColdWaterBottle, new ItemStack(Items.potionitem, 1, 0), 0.0F);                                          //BAD COLD - CLEAN
//        GameRegistry.addSmelting(frostyWaterBottle, new ItemStack(coldWaterBottle, 1, 0), 0.0F);                                            //FROSTY   - COLD

        GameRegistry.addShapelessRecipe(new ItemStack(frostyWaterBottle, 1, 0), new ItemStack(coldWaterBottle, 1, 0), new ItemStack(Blocks.ice, 1), new ItemStack(Blocks.ice, 1), new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1));
        GameRegistry.addShapelessRecipe(new ItemStack(frostyWaterBottle, 1, 0), new ItemStack(badColdWaterBottle, 1, 0), new ItemStack(Blocks.ice, 1), new ItemStack(Blocks.ice, 1), new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1));
        GameRegistry.addShapelessRecipe(new ItemStack(coldWaterBottle, 1, 0), new ItemStack(Items.potionitem, 1, 0), new ItemStack(Blocks.ice, 1));
        GameRegistry.addShapelessRecipe(new ItemStack(Items.potionitem, 1, 0), new ItemStack(warmWaterBottle, 1, 0), new ItemStack(Blocks.ice, 1));
        GameRegistry.addShapelessRecipe(new ItemStack(Items.potionitem, 1, 0), new ItemStack(hotWaterBottle, 1, 0), new ItemStack(Blocks.ice, 1), new ItemStack(Blocks.ice, 1), new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1) , new ItemStack(Blocks.ice, 1));

        GameRegistry.addShapelessRecipe(new ItemStack(badColdWaterBottle, 1, 0), new ItemStack(badWaterBottle, 1, 0), new ItemStack(Blocks.ice, 1));
        GameRegistry.addShapelessRecipe(new ItemStack(badColdWaterBottle, 1, 0), new ItemStack(coldWaterBottle, 1, 0), new ItemStack(Blocks.dirt, 1));
        GameRegistry.addShapelessRecipe(new ItemStack(badWarmWaterBottle, 1, 0), new ItemStack(warmWaterBottle, 1, 0), new ItemStack(Blocks.dirt, 1));

        GameRegistry.addShapelessRecipe(new ItemStack(badWaterBottle, 1, 0), new ItemStack(Items.potionitem, 1, 0), new ItemStack(Blocks.dirt, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(saltWaterBottle, 1, 0), new ItemStack(Items.potionitem, 1, 0), new ItemStack(Blocks.sand, 1));

		GameRegistry.addRecipe(new ItemStack(Items.slime_ball, 4, 0), " r ", "rwr", " r ", 'w', new ItemStack(spoiledMilk, 1, 0), 'r', new ItemStack(rottenFood, 1));
		GameRegistry.addRecipe(new ItemStack(Blocks.mycelium), "xyx", "yzy", "xyx", 'z', new ItemStack(Blocks.grass), 'x', new ItemStack(Blocks.brown_mushroom), 'y', new ItemStack(rottenFood, 1));
		GameRegistry.addRecipe(new ItemStack(Blocks.mycelium), "xyx", "yzy", "xyx", 'z', new ItemStack(Blocks.grass), 'y', new ItemStack(Blocks.brown_mushroom), 'x', new ItemStack(rottenFood, 1));
		GameRegistry.addRecipe(new ItemStack(Blocks.mycelium), "xyx", "yzy", "xyx", 'z', new ItemStack(Blocks.grass), 'x', new ItemStack(Blocks.red_mushroom), 'y', new ItemStack(rottenFood, 1));
		GameRegistry.addRecipe(new ItemStack(Blocks.mycelium), "xyx", "yzy", "xyx", 'z', new ItemStack(Blocks.grass), 'y', new ItemStack(Blocks.red_mushroom), 'x', new ItemStack(rottenFood, 1));
		GameRegistry.addRecipe(new ItemStack(Blocks.dirt, 1), "xxx", "xxx", "xxx", 'x', new ItemStack(rottenFood));


		GameRegistry.addRecipe(new ItemStack(gasMask, 1), "xxx", "xzx", "yxy", 'x', new ItemStack(Items.iron_ingot), 'y', new ItemStack(airFilter), 'z', new ItemStack(Blocks.glass_pane));
		GameRegistry.addRecipe(new ItemStack(hardHat, 1), "xyx", "xzx", 'x', new ItemStack(Items.dye, 1, 11), 'y', new ItemStack(Blocks.redstone_lamp), 'z', new ItemStack(Items.iron_helmet, 1, 0));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(hardHat, 1), " y ", "xxx", "x x", 'x', oreKeyAuBrass, 'y', new ItemStack(Blocks.redstone_lamp)));

		GameRegistry.addRecipe(new ItemStack(airFilter, 4), "xyx", "xzx", "xyx", 'x', new ItemStack(Items.iron_ingot), 'y', new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE), 'z', new ItemStack(Items.coal, 1, 1));
		GameRegistry.addRecipe(new ItemStack(airFilter, 4), "xyx", "xzx", "xyx", 'x', new ItemStack(Items.iron_ingot), 'y', new ItemStack(Items.paper), 'z', new ItemStack(Items.coal, 1, 1));
		GameRegistry.addRecipe(new ItemStack(airFilter, 4), "xyx", "xzx", "xpx", 'x', new ItemStack(Items.iron_ingot), 'y', new ItemStack(Items.paper), 'p', new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE),'z', new ItemStack(Items.coal, 1, 1));
		GameRegistry.addRecipe(new ItemStack(airFilter, 4), "xpx", "xzx", "xyx", 'x', new ItemStack(Items.iron_ingot), 'y', new ItemStack(Items.paper), 'p', new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE),'z', new ItemStack(Items.coal, 1, 1));

		GameRegistry.addRecipe(new ItemStack(elevator, 1, 0), "xyx", "z z", "z z", 'x', new ItemStack(Blocks.iron_block), 'y', new ItemStack(Blocks.redstone_lamp), 'z', new ItemStack(Blocks.iron_bars));
		GameRegistry.addRecipe(new ItemStack(elevator, 1, 1), "z z", "xyx", "www", 'x', new ItemStack(Blocks.iron_block), 'y', new ItemStack(Blocks.furnace), 'z', new ItemStack(Blocks.iron_bars), 'w', new ItemStack(Items.diamond_pickaxe));

		//GameRegistry.addRecipe(new ItemStack(davyLampBlock), " x ", "zyz", "xxx", 'x', new ItemStack(Items.gold_ingot), 'y', new ItemStack(Blocks.torch), 'z', new ItemStack(Blocks.glass_pane));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(davyLampBlock, 1), " x ", "zyz", "xxx", 'x', oreKeyAuBrass, 'y', new ItemStack(Blocks.torch), 'z', new ItemStack(Blocks.glass_pane)));

		GameRegistry.addShapelessRecipe(new ItemStack(davyLampBlock, 1, 1), new ItemStack(davyLampBlock, 1, 0), new ItemStack(Items.flint_and_steel, 1, OreDictionary.WILDCARD_VALUE));
		GameRegistry.addShapelessRecipe(new ItemStack(davyLampBlock, 1, 1), new ItemStack(davyLampBlock, 1, 0), new ItemStack(Blocks.torch));
		GameRegistry.addShapelessRecipe(new ItemStack(davyLampBlock, 1, 1), new ItemStack(davyLampBlock, 1, 0), new ItemStack(fireTorch));

		GameRegistry.addRecipe(new ItemStack(esky), "xxx", "yzy", "yyy", 'x', new ItemStack(Blocks.snow), 'y', new ItemStack(Items.dye, 1, 4), 'z', new ItemStack(Blocks.chest));
		GameRegistry.addRecipe(new ItemStack(freezer), "xyx", "yzy", "xyx", 'x', new ItemStack(Blocks.iron_block), 'y', new ItemStack(Blocks.ice), 'z', new ItemStack(esky));
		GameRegistry.addRecipe(new ItemStack(freezer), "xyx", "yzy", "xyx", 'x', new ItemStack(Blocks.iron_block), 'y', new ItemStack(Blocks.packed_ice), 'z', new ItemStack(esky));

		ItemStack camelStack = new ItemStack(camelPack);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger(EM_Settings.CAMEL_PACK_FILL_TAG_KEY, 0);
		tag.setInteger(EM_Settings.CAMEL_PACK_MAX_TAG_KEY, EM_Settings.camelPackMax);
		tag.setBoolean(EM_Settings.IS_CAMEL_PACK_TAG_KEY, true);
		tag.setString("camelPath", Item.itemRegistry.getNameForObject(camelPack));
		camelStack.setTagCompound(tag);
		GameRegistry.addRecipe(camelStack, "xxx", "xyx", "xxx", 'x', new ItemStack(Items.leather), 'y', new ItemStack(Items.glass_bottle));

		ItemStack camelStack2 = camelStack.copy();
		camelStack2.getTagCompound().setInteger(EM_Settings.CAMEL_PACK_FILL_TAG_KEY, 25);
		GameRegistry.addRecipe(camelStack2, "xxx", "xyx", "xxx", 'x', new ItemStack(Items.leather), 'y', new ItemStack(Items.potionitem, 1, 0));
	}

	public static String[] DefaultIgnitionSources()
	{
		String[] list = new String[]{
				"minecraft:flowing_lava",
				"minecraft:lava",
				"minecraft:torch",
				"minecraft:lit_furnace",
				"minecraft:fire",
				"minecraft:lit_pumpkin",
				"minecraft:lava_bucket",
				"enviromine:firegas",
				"enviromine:firetorch",
				"enviromine:burningcoal",
				"campfirebackport:campfire",
				"campfirebackport:soul_campfire",
				"CarpentersBlocks:blockCarpentersTorch",
				"CaveBiomes:stone_lavacrust",
				"chisel:jackolantern1",
				"chisel:jackolantern2",
				"chisel:jackolantern3",
				"chisel:jackolantern4",
				"chisel:jackolantern5",
				"chisel:jackolantern6",
				"chisel:jackolantern7",
				"chisel:jackolantern8",
				"chisel:jackolantern9",
				"chisel:jackolantern10",
				"chisel:jackolantern11",
				"chisel:jackolantern12",
				"chisel:jackolantern13",
				"chisel:jackolantern14",
				"chisel:jackolantern15",
				"chisel:jackolantern16",
				"chisel:torch1",
				"chisel:torch2",
				"chisel:torch3",
				"chisel:torch4",
				"chisel:torch5",
				"chisel:torch6",
				"chisel:torch7",
				"chisel:torch8",
				"demonmobs:hellfire",
				"easycoloredlights:easycoloredlightsCLStone=-1",
				"etfuturum:magma",
				"fossil:skullLantern",
				"GardenCore:small_fire",
				"harvestcraft:pamcandleDeco1",
				"harvestcraft:pamcandleDeco2",
				"harvestcraft:pamcandleDeco3",
				"harvestcraft:pamcandleDeco4",
				"harvestcraft:pamcandleDeco5",
				"harvestcraft:pamcandleDeco6",
				"harvestcraft:pamcandleDeco7",
				"harvestcraft:pamcandleDeco8",
				"harvestcraft:pamcandleDeco9",
				"harvestcraft:pamcandleDeco10",
				"harvestcraft:pamcandleDeco11",
				"harvestcraft:pamcandleDeco12",
				"harvestcraft:pamcandleDeco13",
				"harvestcraft:pamcandleDeco14",
				"harvestcraft:pamcandleDeco15",
				"harvestcraft:pamcandleDeco16",
				"IguanaTweaksTConstruct:clayBucketLava",
				"IguanaTweaksTConstruct:clayBucketsTinkers:*",
				"infernomobs:bucketpurelava",
				"infernomobs:cinderfallsword",
				"infernomobs:cinderfallswordverdant",
				"infernomobs:cinderfallswordazure",
				"infernomobs:emberscepter",
				"infernomobs:magmacharge",
				"infernomobs:magmascepter",
				"infernomobs:purelava",
				"infernomobs:scorchfire",
				"infernomobs:scorchfirescepter",
				"infernomobs:scorchfirecharge",
				"infernomobs:soulstoneinferno",
				"netherlicious:FoxFire",
				"netherlicious:MagmaBlock",
				"netherlicious:SoulFire",
				"Railcraft:lantern.stone",
				"Railcraft:lantern.metal",
				"Railcraft:lantern.stone",
				"TConstruct:decoration.stonetorch",
				"ThermalFoundation:bucket:*",
				"ThermalFoundation:FluidGlowstone",
				"ThermalFoundation:FluidEnder",
				"ThermalFoundation:FluidPyrotheum",
				"ThermalFoundation:FluidMana",
				"torchLevers:torchLever",
				"torchLevers:torchButton",
				"uptodate:magma_block",
			};

		return list;
	}

	public static void LoadIgnitionSources(String[] listIn)
	{
		for(String source : listIn)
		{
			try
			{
				Block sBlock = Block.getBlockFromName(source);
				igniteList.put(sBlock, new ArrayList<Integer>());
				if (EM_Settings.loggerVerbosity >= EnumLogVerbosity.ALL.getLevel()) EnviroMine.logger.log(Level.INFO, "Registered "+ sBlock.getLocalizedName() +"("+source+") as an ignition source.");
			}catch(NullPointerException e)
			{
				if (EM_Settings.loggerVerbosity >= EnumLogVerbosity.NORMAL.getLevel()) EnviroMine.logger.log(Level.ERROR, "Could not find "+ source);
			}

		}
	}
}
