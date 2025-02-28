package enviromine.core;

import cpw.mods.fml.common.Loader;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.relauncher.Side;
import enviromine.EnviroPotion;
import enviromine.core.commands.CommandPhysics;
import enviromine.core.commands.EnviroCommand;
import enviromine.core.commands.QuakeCommand;
import enviromine.core.proxies.EM_CommonProxy;
import enviromine.handlers.EnviroAchievements;
import enviromine.handlers.EnviroShaftCreationHandler;
import enviromine.handlers.ObjectHandler;
import enviromine.handlers.Legacy.LegacyHandler;
import enviromine.network.packet.PacketAutoOverride;
import enviromine.network.packet.PacketEnviroMine;
import enviromine.network.packet.PacketServerOverride;
import enviromine.utils.EnviroUtils;
import enviromine.world.WorldProviderCaves;
import enviromine.world.biomes.BiomeGenCaves;
import enviromine.world.features.WorldFeatureGenerator;
import enviromine.world.features.mineshaft.EM_VillageMineshaft;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.DimensionManager;

@Mod(modid = EM_Settings.MOD_ID, name = EM_Settings.MOD_NAME, version = EM_Settings.VERSION, guiFactory = EM_Settings.GUI_FACTORY)
public class EnviroMine
{
	public static Logger logger;
	public static BiomeGenCaves caves;
	public static EnviroTab enviroTab;

	@Instance(EM_Settings.MOD_ID)
	public static EnviroMine instance;

	@SidedProxy(clientSide = EM_Settings.Proxy + ".EM_ClientProxy", serverSide = EM_Settings.Proxy + ".EM_CommonProxy")
	public static EM_CommonProxy proxy;

	public SimpleNetworkWrapper network;

    public static boolean isHbmLoaded() {
        return Loader.isModLoaded("hbm");
    }
    public static boolean isHbmSpaceLoaded() {
        try {
            Class.forName("com.hbm.dim.SolarSystem"); //idk why this, but why not?
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    public static boolean isTCLoaded() {
        return Loader.isModLoaded("Thaumcraft");
    }

    public static boolean isSereneSeasonsLoaded() {
        return Loader.isModLoaded("sereneseasons");
    }

    //public static EM_WorldData theWorldEM;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		// The following overrides the mcmod.info file!
		// Adapted from Jabelar's Magic Beans:
		// https://github.com/jabelar/MagicBeans-1.7.10/blob/e48456397f9c6c27efce18e6b9ad34407e6bc7c7/src/main/java/com/blogspot/jabelarminecraft/magicbeans/MagicBeans.java
        event.getModMetadata().autogenerated = false ; // stops it from complaining about missing mcmod.info

        event.getModMetadata().name = 			// name
        		EnumChatFormatting.AQUA +
        		EM_Settings.MOD_NAME;

        event.getModMetadata().version = 		// version
        		EnumChatFormatting.DARK_AQUA +
        		EM_Settings.VERSION;

        event.getModMetadata().credits = 		// credits
        		EnumChatFormatting.AQUA +
        		"Big thanks to the IronArmy and EpicNation for all the hard work debugging this mod.";

        event.getModMetadata().authorList.clear();
        event.getModMetadata().authorList.add(  // authorList - added as a list
        		EnumChatFormatting.BLUE +
        		"Funwayguy, TimbuckTato, GenDeathrow, thislooksfun, AstroTibs"
        		);

        event.getModMetadata().url = EnumChatFormatting.GRAY +
        		EM_Settings.URL;

        event.getModMetadata().description = 	// description
	       		EnumChatFormatting.GREEN +
	       		"Adds more realism to Minecraft with environmental effects, physics, gases and a cave dimension.";

        event.getModMetadata().logoFile = "title.png";


		logger = event.getModLog();

		enviroTab = new EnviroTab("enviromine.enviroTab");

		LegacyHandler.preInit();
		LegacyHandler.init();

		proxy.preInit(event);

		ObjectHandler.initItems();
		ObjectHandler.registerItems();
		ObjectHandler.initBlocks();
		ObjectHandler.registerBlocks();

		// Load Configuration files And Custom files
		EM_ConfigHandler.initConfig();

		ObjectHandler.registerGases();
		ObjectHandler.registerEntities();

		if(EM_Settings.shaftGen == true)
		{
			VillagerRegistry.instance().registerVillageCreationHandler(new EnviroShaftCreationHandler());
			MapGenStructureIO.func_143031_a(EM_VillageMineshaft.class, "ViMS");
		}

		this.network = NetworkRegistry.INSTANCE.newSimpleChannel(EM_Settings.Channel);
		this.network.registerMessage(PacketEnviroMine.HandlerServer.class, PacketEnviroMine.class, 0, Side.SERVER);
		this.network.registerMessage(PacketEnviroMine.HandlerClient.class, PacketEnviroMine.class, 1, Side.CLIENT);
		this.network.registerMessage(PacketAutoOverride.Handler.class, PacketAutoOverride.class, 2, Side.CLIENT);
		this.network.registerMessage(PacketServerOverride.Handler.class, PacketServerOverride.class, 3, Side.CLIENT);


		GameRegistry.registerWorldGenerator(new WorldFeatureGenerator(), 20);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.init(event);

		ObjectHandler.registerRecipes();

		EnviroUtils.extendPotionList();

		EnviroPotion.RegisterPotions();

		EnviroAchievements.InitAchievements();

		caves = (BiomeGenCaves)(new BiomeGenCaves(EM_Settings.caveBiomeID).setColor(0).setBiomeName("Caves").setDisableRain().setTemperatureRainfall(1.0F, 0.0F));
		//GameRegistry.addBiome(caves);
		BiomeDictionary.registerBiomeType(caves, Type.WASTELAND);


		DimensionManager.registerProviderType(EM_Settings.caveDimID, WorldProviderCaves.class, false);
		DimensionManager.registerDimension(EM_Settings.caveDimID, EM_Settings.caveDimID);


		proxy.registerTickHandlers();
		proxy.registerEventHandlers();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit(event);
		EM_ConfigHandler.initConfig(); // Second pass for object initialized after pre-init
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event)
	{
		MinecraftServer server = MinecraftServer.getServer();
		ICommandManager command = server.getCommandManager();
		ServerCommandManager manager = (ServerCommandManager) command;

		manager.registerCommand(new CommandPhysics());
		manager.registerCommand(new EnviroCommand());
		manager.registerCommand(new QuakeCommand());
	}
}
