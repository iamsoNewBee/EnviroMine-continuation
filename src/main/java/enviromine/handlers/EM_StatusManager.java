package enviromine.handlers;

import com.google.common.base.Stopwatch;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.orbit.WorldProviderOrbit;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.ThreeInts;
import com.hbm.handler.atmosphere.AtmosphereBlob;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.armor.ArmorFSB;
import com.hbm.tileentity.machine.TileEntityCrucible;
import com.hbm.tileentity.machine.TileEntityDiFurnace;
import com.hbm.tileentity.machine.TileEntityDiFurnaceRTG;
import com.hbm.tileentity.machine.TileEntityFurnaceBrick;
import com.hbm.tileentity.machine.TileEntityFurnaceCombination;
import com.hbm.tileentity.machine.TileEntityFurnaceIron;
import com.hbm.tileentity.machine.TileEntityFurnaceSteel;
import com.hbm.tileentity.machine.TileEntityHeatBoiler;
import com.hbm.tileentity.machine.TileEntityHeatBoilerIndustrial;
import com.hbm.tileentity.machine.TileEntityHeaterElectric;
import com.hbm.tileentity.machine.TileEntityHeaterFirebox;
import com.hbm.tileentity.machine.TileEntityHeaterOilburner;
import com.hbm.tileentity.machine.TileEntityHeaterOven;
import com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge;
import com.hbm.tileentity.machine.TileEntityMachineCombustionEngine;
import com.hbm.tileentity.machine.TileEntityMachineCyclotron;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;
import com.hbm.tileentity.machine.TileEntityMachineHephaestus;
import com.hbm.tileentity.machine.TileEntityMachinePress;
import com.hbm.tileentity.machine.TileEntityMachineTurbineGas;
import com.hbm.tileentity.machine.TileEntityMachineTurbofan;
import com.hbm.tileentity.machine.TileEntityMachineWoodBurner;
import com.hbm.tileentity.machine.TileEntityNukeFurnace;
import com.hbm.tileentity.machine.TileEntityRtgFurnace;
import com.hbm.tileentity.machine.oil.TileEntityMachineCoker;
import com.hbm.tileentity.machine.oil.TileEntityMachineGasFlare;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKBase;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import enviromine.EnviroPotion;
import enviromine.client.gui.UI_Settings;
import enviromine.client.gui.hud.items.Debug_Info;
import enviromine.core.EM_ConfigHandler.EnumLogVerbosity;
import enviromine.core.EM_Settings;
import enviromine.core.EnviroMine;
import enviromine.network.packet.PacketEnviroMine;
import enviromine.trackers.EnviroDataTracker;
import enviromine.trackers.properties.ArmorProperties;
import enviromine.trackers.properties.BiomeProperties;
import enviromine.trackers.properties.BlockProperties;
import enviromine.trackers.properties.DimensionProperties;
import enviromine.trackers.properties.EntityProperties;
import enviromine.trackers.properties.ItemProperties;
import enviromine.utils.EnviroUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.FoodStats;
import net.minecraft.util.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.EnumPlantType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static enviromine.core.EM_Settings.BodyTempSleep;
import static enviromine.core.EnviroMine.isHbmLoaded;

public class EM_StatusManager
{
	public static final int AIR_QUALITY_DELTA_INDEX = 0;
	public static final int AMBIENT_TEMP_INDEX = 1;
	public static final int NEAR_LAVA_INDEX = 2;
	public static final int DEHYDRATION_DELTA_INDEX = 3;
	public static final int BODY_TEMP_DROP_SPEED_INDEX = 4;
	public static final int BODY_TEMP_RISE_SPEED_INDEX = 5;
	public static final int ANIMAL_HOSTILITY_INDEX = 6;
	public static final int SANITY_DELTA_INDEX = 7;
	public static HashMap<String,EnviroDataTracker> trackerList = new HashMap<String,EnviroDataTracker>();

	public static void addToManager(EnviroDataTracker tracker)
	{
		if(tracker.trackedEntity instanceof EntityPlayer)
		{
			trackerList.put("" + tracker.trackedEntity.getCommandSenderName(), tracker);
		} else
		{
			trackerList.put("" + tracker.trackedEntity.getEntityId(), tracker);
		}
	}

	public static void updateTracker(EnviroDataTracker tracker)
	{
		if(tracker == null)
		{
			return;
		}

		if(EnviroMine.proxy.isClient() && Minecraft.getMinecraft().isIntegratedServerRunning())
		{
			if(Minecraft.getMinecraft().isGamePaused() && !EnviroMine.proxy.isOpenToLAN())
			{
				return;
			}
		}

		tracker.updateTimer += 1;

		if(tracker.updateTimer >= 30) //TODO HARDCODED
		{
			tracker.updateData();

			if(!EnviroMine.proxy.isClient() || EnviroMine.proxy.isOpenToLAN())
			{
				syncMultiplayerTracker(tracker);
			}
		}
	}

	public static void syncMultiplayerTracker(EnviroDataTracker tracker)
	{
		if(!(tracker.trackedEntity instanceof EntityPlayer))
		{
			return;
		}

		tracker.fixFloatingPointErrors(); // Shortens data as much as possible before sending
		NBTTagCompound pData = new NBTTagCompound();
		pData.setInteger("id", 0);
		pData.setString("player", tracker.trackedEntity.getCommandSenderName());
		pData.setFloat("airQuality", tracker.airQuality);
		pData.setFloat("bodyTemp", tracker.bodyTemp);
		pData.setFloat("hydration", tracker.hydration);
		pData.setFloat("sanity", tracker.sanity);
		pData.setFloat("airTemp", tracker.airTemp);

		EnviroMine.instance.network.sendToAllAround(new PacketEnviroMine(pData), new TargetPoint(tracker.trackedEntity.worldObj.provider.dimensionId, tracker.trackedEntity.posX, tracker.trackedEntity.posY, tracker.trackedEntity.posZ, 128D));
	}

	public static EnviroDataTracker lookupTracker(EntityLivingBase entity)
	{
		if(entity instanceof EntityPlayer)
		{
            return trackerList.getOrDefault("" + entity.getCommandSenderName(), null);
		} else
		{
            return trackerList.getOrDefault("" + entity.getEntityId(), null);
		}
	}

	public static EnviroDataTracker lookupTrackerFromUsername(String username)
	{
        return trackerList.getOrDefault(username, null);
	}

	private static Stopwatch timer = Stopwatch.createUnstarted();

	public static float[] getSurroundingData(EntityLivingBase entityLiving, int cubeRadius) {
        if (EnviroMine.proxy.isClient() && entityLiving.getCommandSenderName().equals(Minecraft.getMinecraft().thePlayer.getCommandSenderName()) && !timer.isRunning()) {
            timer.start();
        }

        float[] data = new float[8];

        float sanityRate = 0F;
        float sanityStartRate = sanityRate;

        float quality = 0;
        double leaves = 0;
        float sanityBoost = 0;

        float dropSpeed = 0.001F;
        float riseSpeed = 0.001F;

        float blockAndItemTempInfluence = 0F;
        float cooling = 0;
        float dehydrateBonus = 0.0F;
        int animalHostility = 0;
        boolean nearLava = false;
        float dist = 0;
        float solidBlocks = 0;
        float totalBlocks = 0;

        int i = MathHelper.floor_double(entityLiving.posX);
        int j = MathHelper.floor_double(entityLiving.posY);
        int k = MathHelper.floor_double(entityLiving.posZ);

        boolean airVentConst = false;

        if (entityLiving.worldObj == null) {
            return data;
        }

        Chunk chunk = entityLiving.worldObj.getChunkFromBlockCoords(i, k);

        if (chunk == null) {
            return data;
        }

        BiomeGenBase biome = chunk.getBiomeGenForWorldCoords(i & 15, k & 15, entityLiving.worldObj.getWorldChunkManager());

        if (biome == null) {
            return data;
        }

        DimensionProperties dimensionProp = null;
        if (DimensionProperties.base.hasProperty(entityLiving.worldObj.provider.dimensionId)) {
            dimensionProp = DimensionProperties.base.getProperty(entityLiving.worldObj.provider.dimensionId);
        }


        float surroundingBiomeTempSamplesSum = 0;
        int surroundingBiomeTempSamplesCount = 0;

        boolean isDay = entityLiving.worldObj.isDaytime();

        if (entityLiving.worldObj.provider.hasNoSky) {
            isDay = false;
        }

        int lightLev = 0;
        int blockLightLev = 0;

        if (j > 0) {
            if (j >= 256) {
                lightLev = 15;
                blockLightLev = 15;
            } else {
                lightLev = chunk.getSavedLightValue(EnumSkyBlock.Sky, i & 0xf, j, k & 0xf);
                blockLightLev = chunk.getSavedLightValue(EnumSkyBlock.Block, i & 0xf, j, k & 0xf);
            }
        }

        if (!isDay && blockLightLev <= 1 && entityLiving.getActivePotionEffect(Potion.nightVision) == null) {
            if (dimensionProp == null || !dimensionProp.override || dimensionProp.darkAffectSanity) {
                sanityStartRate = -0.01F; //TODO HARDCODED
                sanityRate = -0.01F;
            }
        }

        // Scan a cube around the player
        int cubeRadius_squared = cubeRadius * cubeRadius;

        for (int y = -cubeRadius; y <= cubeRadius; y++) {
            float radiusAtVerticalSlice = MathHelper.sqrt_float(cubeRadius_squared - (y * y));
            int radiusAtVerticalSlice_floored = MathHelper.floor_float(radiusAtVerticalSlice);
            float radiusAtVerticalSlice_squared = radiusAtVerticalSlice * radiusAtVerticalSlice;

            // East-West position
            for (int x = -radiusAtVerticalSlice_floored; x <= radiusAtVerticalSlice_floored; x++) {
                float radiusAtXSlice = MathHelper.sqrt_float(radiusAtVerticalSlice_squared - (x * x));
                int radiusAtXSlice_floored = MathHelper.floor_float(radiusAtXSlice);

                // South-North position
                for (int z = -radiusAtXSlice_floored; z <= radiusAtXSlice_floored; z++) {
                    if (y == 0) {
                        Chunk testChunk = entityLiving.worldObj.getChunkFromBlockCoords((i + x), (k + z));
                        BiomeGenBase checkBiome = testChunk.getBiomeGenForWorldCoords((i + x) & 15, (k + z) & 15, entityLiving.worldObj.getWorldChunkManager());

                        if (checkBiome != null) {
                            BiomeProperties biomeOverride = null;
                            if (BiomeProperties.base.hasProperty(checkBiome)) {
                                biomeOverride = BiomeProperties.base.getProperty(checkBiome);
                            }

                            if (biomeOverride != null && biomeOverride.biomeOveride) {
                                if(EnviroMine.isHbmSpaceLoaded()) {
                                    CBT_Atmosphere atmosphere = entityLiving.worldObj.provider instanceof WorldProviderOrbit ? null : CelestialBody.getTrait(entityLiving.worldObj, CBT_Atmosphere.class);
                                    if(atmosphere != null) {
                                        if(atmosphere.hasFluid(Fluids.AIR, 0.19) || atmosphere.hasFluid(Fluids.OXYGEN, 0.09)) {
                                            surroundingBiomeTempSamplesSum += biomeOverride.ambientTemp_TERRAFORMED;
                                        } else {
                                            surroundingBiomeTempSamplesSum += biomeOverride.ambientTemp;
                                        }
                                    } else {
                                        surroundingBiomeTempSamplesSum += biomeOverride.ambientTemp;
                                    }
                                } else {
                                    surroundingBiomeTempSamplesSum += biomeOverride.ambientTemp;
                                }
                            } else {
                                //surBiomeTemps += EnviroUtils.getBiomeTemp(checkBiome);
                                surroundingBiomeTempSamplesSum += EnviroUtils.getBiomeTemp((i + x), (j + y), (k + z), checkBiome);
                            }

                            surroundingBiomeTempSamplesCount += 1;
                        }
                    }

                    if (!EM_PhysManager.blockNotSolid(entityLiving.worldObj, x + i, y + j, z + k, false)) {
                        solidBlocks += 1;
                    }
                    totalBlocks += 1;

                    dist = (float) entityLiving.getDistance(i + x, j + y, k + z);

                    Block block = Blocks.air;
                    int meta = 0;

                    block = entityLiving.worldObj.getBlock(i + x, j + y, k + z);

                    if(isHbmLoaded() && EM_Settings.EnableHBMMachinesHeat) {
                        TileEntity tileentity = entityLiving.worldObj.getTileEntity(i + x, j + y, k + z);

                        if (tileentity != null) {
                            float FireboxMax = TileEntityHeaterFirebox.maxHeatEnergy;
                            float HeaterOvenMax = TileEntityHeaterOven.maxHeatEnergy;

                            //Calculation accuracy:
                            //✅ - close to the expected value
                            //🟧 - different from the expected value, but not much
                            //❌ - very different from the expected value
                            //Expected - ((value/DIV) / 2) ≈ temp in deg

                            if (tileentity instanceof TileEntityMachinePress press) {
                                if (press.burnTime > 0 && press.speed > 0) {
                                    //Coal - 1600/16 = 64℃ (expected 50) ✅
                                    //Bale - 32000/16 = 514℃ (expected 1000) - 500℃ hard-cap ✅
                                    //Works in space - ✅
                                    //TODO CONSTANT ALERT
                                    blockAndItemTempInfluence += getTempFalloff(Math.min((press.burnTime / EM_Settings.BurnerPressHeatDivisor), EM_Settings.BurnerPressHeatHardCap*2), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if (tileentity instanceof TileEntityHeaterFirebox firebox) {
                                if (firebox.burnTime > 0 && firebox.heatEnergy > 0) {
                                    //Coal - 200/2 = 52(60)℃ (expected 50) ✅
                                    //Bale - 1500/2 = 350(309)℃ (expected 375) ✅
                                    //Works in space - ❌
                                    blockAndItemTempInfluence += getTempFalloff(firebox.burnHeat / EM_Settings.FireboxHeatDivisor, dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if (tileentity instanceof TileEntityHeaterOven heaterOven) {
                                if (heaterOven.burnTime > 0 && heaterOven.heatEnergy > 0) {
                                    //Coal - 1000/4 = 129(112)℃  (expected 125) ✅
                                    //Bale - 7500/4 = 869(877)℃ (expected 937,5) 🟧
                                    //Works in space - ❌
                                    blockAndItemTempInfluence += getTempFalloff(heaterOven.burnHeat / EM_Settings.HeaterOvenHeatDivisor, dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if (tileentity instanceof TileEntityHeaterOilburner oilburner) {
                                if (oilburner.isOn && oilburner.heatEnergy > 0) {
                                    //Max - 100_000/200 = 245℃ (expected 250) ✅
                                    //Works in space - ❌
                                    blockAndItemTempInfluence += getTempFalloff(oilburner.heatEnergy / EM_Settings.FluidBurnerHeatDivisor, dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if (tileentity instanceof TileEntityHeaterElectric heaterElectric) {
                                if (heaterElectric.isOn && heaterElectric.heatEnergy > 0) {
                                    //Max (no) - 10_000/20 = 244(249)℃ (expected 250) - 250℃ hard-cap ✅
                                    //Works in space - ✅
                                    //TODO CONSTANT ALERT
                                    blockAndItemTempInfluence += getTempFalloff(Math.min(heaterElectric.heatEnergy / EM_Settings.HeaterElectricHeatDivisor, (EM_Settings.HeaterElectricHeatHardCap*2)) , dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if (tileentity instanceof TileEntityFurnaceIron furnaceIron) {
                                if (furnaceIron.wasOn) {
                                    //Coal - 2000/2 = 458℃ (expected 500) ✅
                                    //Bale - 64000/2 = 15343℃ (expected 16000) - 1000℃ hard-cap ✅
                                    //Works in space - ❌
                                    //TODO CONSTANT ALERT
                                    blockAndItemTempInfluence += getTempFalloff(Math.min(furnaceIron.burnTime / EM_Settings.IronFurnaceHeatDivisor, (EM_Settings.IronFurnaceHeatHardCap*2)), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if (tileentity instanceof TileEntityFurnaceSteel furnaceSteel) {
                                if (furnaceSteel.wasOn) {
                                    //Max - 100_000 (35000)/200 = 85℃ (expected 87,5) ✅
                                    //Works in space - ❌
                                    blockAndItemTempInfluence += getTempFalloff(furnaceSteel.heat / EM_Settings.SteelFurnaceHeatDivisor, dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if (tileentity instanceof TileEntityFurnaceCombination furnaceCombination) {
                                if (furnaceCombination.wasOn) {
                                    //Max - 100_000/200 = 228℃ (expected 250) ✅
                                    //Works in space - ❌
                                    blockAndItemTempInfluence += getTempFalloff(furnaceCombination.heat / EM_Settings.CombinationOvenHeatDivisor, dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if (tileentity instanceof TileEntityCrucible crucible) {
                                if(crucible.heat > 0) {
                                    //Max - 100_000/1000 = 101℃ (expected 100) ✅
                                    //Works in space - ✅
                                    blockAndItemTempInfluence += getTempFalloff(crucible.heat / EM_Settings.CrucibleHeatDivisor, dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if (tileentity instanceof TileEntityHeatBoiler boiler) {
                                float heat = boiler.heat;

                                if (heat <= FireboxMax) {
                                    heat = boiler.heat / EM_Settings.BoilerHeatDivisor;
                                } else if (heat <= HeaterOvenMax) {
                                    heat = Math.max(boiler.heat / (EM_Settings.BoilerHeatDivisor * EM_Settings.BoilerHeaterOvenDivisorConstant), ((FireboxMax / EM_Settings.BoilerHeatDivisor)));
                                } else if (heat <= TileEntityHeatBoiler.maxHeat) {
                                    heat = Math.max(boiler.heat / (EM_Settings.BoilerHeatDivisor * EM_Settings.BoilerMAXDivisorConstant), ((HeaterOvenMax / (EM_Settings.BoilerHeatDivisor * EM_Settings.BoilerHeaterOvenDivisorConstant))));
                                }
                                //Max (real) - 3_200_000/(200*10) - actually 999_001/(200*10) = 510℃
                                //Max (HO)     - 500_000/(200*2)  = 592℃ (expected 625) ✅
                                //Max (FB)     - 100_000/200      = 245℃ (expected 250) ✅
                                //Works in space - ✅
                                blockAndItemTempInfluence += getTempFalloff(heat, dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                            }
                            else if (tileentity instanceof TileEntityHeatBoilerIndustrial boilerIndustrial) {
                                float heat = boilerIndustrial.heat;

                                if (heat <= FireboxMax) {
                                    heat = boilerIndustrial.heat / EM_Settings.BoilerIndustrialHeatDivisor;
                                } else if (heat <= HeaterOvenMax) {
                                    heat = Math.max(boilerIndustrial.heat / (EM_Settings.BoilerIndustrialHeatDivisor * EM_Settings.BoilerIndustrialHeaterOvenDivisorConstant), ((FireboxMax / EM_Settings.BoilerIndustrialHeatDivisor)));
                                } else if (heat <= TileEntityHeatBoilerIndustrial.maxHeat) {
                                    heat = Math.max(boilerIndustrial.heat / (EM_Settings.BoilerIndustrialHeatDivisor * EM_Settings.BoilerIndustrialMAXDivisorConstant), ((HeaterOvenMax / (EM_Settings.BoilerIndustrialHeatDivisor * EM_Settings.BoilerIndustrialHeaterOvenDivisorConstant))));
                                }

                                //Max (real)   - 12_800_000/(200*10) - actually 999_001/(200*10) = 506℃
                                //Max (HO)        - 500_000/(200*2)  = 510℃ (expected 625) 🟧
                                //Max (FB)        - 100_000/200      = 210℃ (expected 250) ✅
                                //Works in space - ✅
                                blockAndItemTempInfluence += getTempFalloff(heat, dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                            }
                            else if(tileentity instanceof TileEntityFurnaceBrick furnaceBrick) {
                                if (furnaceBrick.burnTime > 0) {
                                    //Coal - 1600/16  = 58℃ (expected 50) ✅
                                    //Bale - 32000/16 = 475℃ (expected 1000) - 500℃ hard-cap ✅
                                    //Works in space - ❌
                                    //TODO CONSTANT ALERT
                                    blockAndItemTempInfluence += getTempFalloff(Math.min((furnaceBrick.burnTime / EM_Settings.FurnaceBrickHeatDivisor), EM_Settings.FurnaceBrickHeatHardCap*2), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if(tileentity instanceof TileEntityDiFurnace diFurnace) {
                                if(diFurnace.progress > 0) {
                                    //FUEL - 12800/64 = 105℃ (expected 100) ✅
                                    //Works in space - ❌
                                    blockAndItemTempInfluence += getTempFalloff((diFurnace.fuel / EM_Settings.DiFurnaceHeatDivisor), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if(tileentity instanceof TileEntityDiFurnaceRTG diFurnaceRTG) {
                                if(diFurnaceRTG.progress > 0) {
                                    //Power level (max) = 600 X6 = 3600/2 = who knows, TO FUCKING FAST℃ (expected 900) ❔
                                    //Power level (min) = 3 X6 =     18/2 = too small amount, not even shown℃ (expected 4.5) ❔
                                    //Works in space - ✅
                                    blockAndItemTempInfluence += getTempFalloff((diFurnaceRTG.getPower() / EM_Settings.DiFurnaceRTGHeatDivisor), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if(tileentity instanceof TileEntityNukeFurnace nukeFurnace) {
                                if(nukeFurnace.isProcessing()) {
                                    //Operations (max) = 200/0.2 = 114℃ (expected 500) ❌
                                    //Operations (min) =   5/0.2 = not enough to see℃ (expected 12,5) ❔
                                    //Works in space - ✅
                                    blockAndItemTempInfluence += getTempFalloff((nukeFurnace.dualPower / EM_Settings.NukeFurnaceHeatDivisor), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if(tileentity instanceof TileEntityRtgFurnace rtgFurnace) {
                                //this shouldn't really be a constant, but I don't give a fuck
                                if(rtgFurnace.isProcessing()){
                                    //Works in space - ✅
                                    //TODO CONSTANT ALERT
                                    blockAndItemTempInfluence += getTempFalloff((EM_Settings.RTGFurnaceHeatConstant*2), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if(tileentity instanceof TileEntityMachineWoodBurner woodBurner) {
                                int powerGen = 0;
                                try {
                                    Field powerGenz = TileEntityMachineWoodBurner.class.getDeclaredField("powerGen"); //куадусешщт
                                    powerGenz.setAccessible(true);
                                    powerGen = (int) powerGenz.get(woodBurner);
                                } catch (NoSuchFieldException | IllegalAccessException ignored) {}

                                if(woodBurner.isOn && powerGen > 0 ) {
                                //Coal - 1600/16 = 59℃ (expected 50) ✅
                                //Works in space - ❌
                                blockAndItemTempInfluence += getTempFalloff((woodBurner.burnTime / EM_Settings.WoodBurningGenHeatDivisor), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                            }
                        }
                            else if(tileentity instanceof TileEntityMachineDiesel diesel) {
                                if(diesel.tank.getFill() > 0 && TileEntityMachineDiesel.getHEFromFuel(diesel.tank.getTankType()) > 0L) {
                                    //Works in space - ❌
                                    //TODO CONSTANT ALERT
                                    blockAndItemTempInfluence += getTempFalloff((EM_Settings.DieselGenHeatConstant*2), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if(tileentity instanceof TileEntityMachineCombustionEngine combustionEngine) {
                                if(combustionEngine.wasOn) {
                                    //Works in space - ❌
                                    //TODO CONSTANT ALERT
                                    blockAndItemTempInfluence += getTempFalloff((EM_Settings.ICEHeatConstant*2), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if(tileentity instanceof TileEntityMachineCyclotron cyclotron) {
                                if(cyclotron.progress > 0) {
                                    //Works in space - ✅
                                    //TODO CONSTANT ALERT
                                    blockAndItemTempInfluence += getTempFalloff((EM_Settings.CyclotronHeatConstant*2), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if(tileentity instanceof TileEntityMachineHephaestus hephaestus) { //GeoThermal
                                if(hephaestus.getTotalHeat() > 0) {
                                    //Max - 10_000/10 = 493℃ (expected 500) ✅
                                    //Works in space - ✅
                                    blockAndItemTempInfluence += getTempFalloff((hephaestus.getTotalHeat() / EM_Settings.GeothermalGenHeatDivisor), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if(tileentity instanceof TileEntityRBMKBase rbmkBase) {
                                if(rbmkBase.heat > 0) {
                                    //Max - 1500/5 = 293℃ (expected 300) ✅
                                    //Works in space - ✅
                                    blockAndItemTempInfluence += getTempFalloff(Math.min(((float)rbmkBase.heat / EM_Settings.RBMKRodHeatDivisor), EM_Settings.RBMKRodHeatHardCap*2), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if(tileentity instanceof TileEntityMachineArcFurnaceLarge arcFurnaceLarge) {
                                if(arcFurnaceLarge.isProgressing) {
                                    //Works in space - ✅
                                    //TODO CONSTANT ALERT
                                    blockAndItemTempInfluence += getTempFalloff((EM_Settings.ArcFurnaceHeatConstant*2), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if(tileentity instanceof TileEntityMachineGasFlare gasFlare) {
                                int powerGen = 0;
                                try {
                                    Field output = TileEntityMachineGasFlare.class.getDeclaredField("output");
                                    output.setAccessible(true);
                                    powerGen = (int) output.get(gasFlare);
                                } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                                if(gasFlare.doesBurn && powerGen > 0) {
                                    //Works in space - ✅ (why?)
                                    //TODO CONSTANT ALERT
                                    blockAndItemTempInfluence += getTempFalloff((EM_Settings.FlareStackHeatConstant*2), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if(tileentity instanceof TileEntityMachineCoker coker) {
                                if(coker.wasOn && coker.heat > 0) {
                                    //Max - 100_000/1000 = 58℃ (expected 50)✅
                                    //Works in space - ✅
                                    blockAndItemTempInfluence += getTempFalloff((coker.heat / EM_Settings.CokerHeatDivisor), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if(tileentity instanceof TileEntityMachineTurbofan turbofan) {
                                if(turbofan.wasOn) {
                                    //Works in space - ❌
                                    //TODO CONSTANT ALERT
                                    blockAndItemTempInfluence += getTempFalloff((turbofan.afterburner > 0 ? EM_Settings.TurbofanAfterburnerHeatConstant*2 : EM_Settings.TurbofanHeatConstant*2), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                }
                            }
                            else if(tileentity instanceof TileEntityMachineTurbineGas turbineGas) {
                                    if(turbineGas.temp > 0) {
                                        //Works in space - ✅
                                        blockAndItemTempInfluence += getTempFalloff((turbineGas.temp / EM_Settings.CCGasTurbineHeatDivisor), dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                                    }
                                }
                            }
                    }

                    if (block != Blocks.air) {
                        meta = entityLiving.worldObj.getBlockMetadata(i + x, j + y, k + z);
                    }

                    if (BlockProperties.base.hasProperty(block, meta)) {
                        BlockProperties blockProps = BlockProperties.base.getProperty(block, meta);

                        if (blockProps.air > 0F) {
                            leaves += (blockProps.air / 0.1F);
                        } else if (quality >= blockProps.air && blockProps.air < 0 && quality <= 0) {
                            quality += blockProps.air;
                        }
                        if (blockProps.enableTemp) {
                            if (blockAndItemTempInfluence <= getTempFalloff(blockProps.temp, dist, cubeRadius, EM_Settings.blockTempDropoffPower) && blockProps.temp > 0F) {
                                blockAndItemTempInfluence += getTempFalloff(blockProps.temp, dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                            } else if (blockProps.temp < 0F) {
                                cooling += getTempFalloff(-blockProps.temp, dist, cubeRadius, EM_Settings.blockTempDropoffPower);
                            }
                        }
                        if (sanityRate >= blockProps.sanity && blockProps.sanity < 0 && sanityRate <= 0) {
                            sanityRate += blockProps.sanity;
                        } else if (sanityRate <= blockProps.sanity && blockProps.sanity > 0F) {
                            if (block instanceof BlockFlower) {
                                if (isDay || entityLiving.worldObj.provider.hasNoSky) {
                                    if (sanityBoost < blockProps.sanity) {
                                        sanityBoost += blockProps.sanity;
                                    }
                                }
                            } else {
                                if (sanityBoost < blockProps.sanity) {
                                    sanityBoost += blockProps.sanity;
                                }
                            }
                        }
                    }

                    if (block.getMaterial() == Material.lava) {
                        nearLava = true;
                    }
                }
            }
        }

        if (entityLiving instanceof EntityPlayer player) {

            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = player.inventory.mainInventory[slot];

                if (stack == null) {
                    continue;
                }

                float stackMult = 1F;

                if (stack.stackSize > 1) {
                    stackMult = (stack.stackSize - 1F) / 63F + 1F;
                }
//TODO dynamic hazards

//                float HotlevelCelc = (HazardSystem.getHazardLevelFromStack(stack, HazardRegistry.HOT)) * 100F;
//                float Asbestoslevel = -(HazardSystem.getHazardLevelFromStack(stack, HazardRegistry.ASBESTOS));
//                float Coallevel = -((HazardSystem.getHazardLevelFromStack(stack, HazardRegistry.COAL)) / 2);
//                float Digammalevel = -((HazardSystem.getHazardLevelFromStack(stack, HazardRegistry.DIGAMMA)) * 5);
//
//                if(HotlevelCelc > 0) {
//                    if (blockAndItemTempInfluence <= HotlevelCelc * stackMult && HotlevelCelc > 0F) {
//                        blockAndItemTempInfluence += HotlevelCelc * stackMult;
//                    }
//                }
//                if(Asbestoslevel > 0) {
//                    if (quality >= Asbestoslevel * stackMult && Asbestoslevel < 0 && quality <= 0) {
//                        quality += Asbestoslevel * stackMult;
//                    }
//                }
//                if(Coallevel > 0) {
//                    if (quality >= Coallevel * stackMult && Coallevel < 0 && quality <= 0) {
//                        quality += Coallevel * stackMult;
//                    }
//                }
//                if(Digammalevel > 0) {
//                    if (sanityRate >= Digammalevel * stackMult && Digammalevel < 0 && sanityRate <= 0) {
//                        sanityRate += Digammalevel * stackMult;
//                    }
//                }

                if (ItemProperties.base.hasProperty(stack)) {
                    ItemProperties itemProps = ItemProperties.base.getProperty(stack);

                    if (itemProps.ambAir > 0F) {
                        leaves += (itemProps.ambAir / 0.1F) * stackMult;
                    } else if (quality >= itemProps.ambAir * stackMult && itemProps.ambAir < 0 && quality <= 0) {
                        quality += itemProps.ambAir * stackMult;
                    }
                    if (blockAndItemTempInfluence <= itemProps.ambTemp * stackMult && itemProps.enableTemp && itemProps.ambTemp > 0F) {
                        blockAndItemTempInfluence += itemProps.ambTemp * stackMult;
                    } else if (itemProps.enableTemp && itemProps.ambTemp < 0F) {
                        cooling += -itemProps.ambTemp * stackMult;
                    }

                    if (sanityRate >= itemProps.ambSanity * stackMult && itemProps.ambSanity < 0 && sanityRate <= 0) {
                        sanityRate += itemProps.ambSanity * stackMult;
                    } else if (sanityBoost <= itemProps.ambSanity * stackMult && itemProps.ambSanity > 0F) {
                        if (stack.getItem() instanceof ItemBlock) {
                            if (((ItemBlock) stack.getItem()).field_150939_a instanceof BlockFlower) {
                                if (isDay || entityLiving.worldObj.provider.hasNoSky) {
                                    sanityBoost += itemProps.ambSanity * stackMult;
                                }
                            } else {
                                sanityBoost += itemProps.ambSanity * stackMult;
                            }
                        } else {
                            sanityBoost += itemProps.ambSanity * stackMult;
                        }
                    }
                } else if (stack.getItem() instanceof ItemBlock itemBlock) {
                    if (itemBlock.field_150939_a instanceof BlockFlower && (isDay || entityLiving.worldObj.provider.hasNoSky) && sanityBoost <= 0.1F) {
                        if (((BlockFlower) itemBlock.field_150939_a).getPlantType(entityLiving.worldObj, i, j, k) == EnumPlantType.Plains) {
                            sanityBoost += 0.1F; //TODO HARDCODED
                        }
                    }
                }
            }
        }

        //TODO HARDCODED
        if (lightLev > 1 && !entityLiving.worldObj.provider.hasNoSky) {
            quality += 2F;
            sanityRate += 0.5F;
        } else if (sanityRate <= sanityStartRate && sanityRate > -0.1F && blockLightLev <= 1 && entityLiving.getActivePotionEffect(Potion.nightVision) == null) {
            sanityRate -= 0.1F;
        }

        if (dimensionProp != null && entityLiving.posY > dimensionProp.sealevel * 0.75 && !entityLiving.worldObj.provider.hasNoSky) {
            quality += 2F;
        }


        float biomeTemperature = (surroundingBiomeTempSamplesSum / surroundingBiomeTempSamplesCount);
        float maxHighAltitudeTemp = -30F; // Max temp at high altitude
        float minLowAltitudeTemp = 30F; // Min temp at low altitude (Geothermal Heating)

        //TODO CHANGE FUCKING HARDCODE

        if (!entityLiving.worldObj.provider.hasNoSky) {
            if (entityLiving.posY < 48) {
                if (minLowAltitudeTemp - biomeTemperature > 0) {
                    biomeTemperature += (minLowAltitudeTemp - biomeTemperature) * (1F - (entityLiving.posY / 48F));
                }
            } else if (entityLiving.posY > 90 && entityLiving.posY < 256) {
                if (maxHighAltitudeTemp - biomeTemperature < 0) {
                    biomeTemperature -= MathHelper.abs(maxHighAltitudeTemp - biomeTemperature) * ((entityLiving.posY - 90F) / 166F);
                }
            } else if (entityLiving.posY >= 256) {
                biomeTemperature = Math.min(biomeTemperature, maxHighAltitudeTemp);
            }
        }

        biomeTemperature -= cooling;

        if (entityLiving instanceof EntityPlayer) {
            if (((EntityPlayer) entityLiving).isPlayerSleeping()) {
                biomeTemperature += BodyTempSleep;
            }
        }

        if(dimensionProp == null || !dimensionProp.override || dimensionProp.weatherAffectsTemp) {
            float biomeTemperatureRain = 6F;
            float biomeTemperatureThunder = 8F;
            //float biomeTemperatureShade = 2.5F; //the FUCK is that

            boolean biomeTemperatureRainBool = false;
            boolean biomeTemperatureThunderBool = false;

            if (biome != null) {
                BiomeProperties biomeOverride = null;
                if (BiomeProperties.base.hasProperty(biome)) {
                    biomeOverride = BiomeProperties.base.getProperty(biome);
                }
                if (biomeOverride != null && biomeOverride.biomeOveride) {
                    biomeTemperatureRain = biomeOverride.TemperatureRainDecrease;
                    biomeTemperatureThunder = biomeOverride.TemperatureThunderDecrease;
                    biomeTemperatureRainBool = biomeOverride.TemperatureRainBool;
                    biomeTemperatureThunderBool = biomeOverride.TemperatureThunderBool;
                    //biomeTemperatureShade = biomeOverride.TemperatureShadeDecrease; //Uh what
                }
            }

            if (entityLiving.worldObj.isRaining() && biome.rainfall != 0.0F && biomeTemperatureRainBool) {
                biomeTemperature -= biomeTemperatureRain;
                animalHostility = -1;

                if (entityLiving.worldObj.canBlockSeeTheSky(i, j, k)) {
                    dropSpeed = 0.01F;
                }
            } else if (entityLiving.worldObj.isThundering() && biome.rainfall != 0.0F && biomeTemperatureThunderBool) {
                biomeTemperature -= biomeTemperatureThunder;
                animalHostility = -1;

                if (entityLiving.worldObj.canBlockSeeTheSky(i, j, k)) {
                    dropSpeed = 0.01F; //TODO HARDCODED
                }
            }

        } // Dimension Overrides End

        float biomeTemperatureShade = 2.5F; //who the fuck writing that code? (-grammar)
        if (biome != null) {
            BiomeProperties biomeOverride = null;
            if (BiomeProperties.base.hasProperty(biome)) {
                biomeOverride = BiomeProperties.base.getProperty(biome);
            } if (biomeOverride != null && biomeOverride.biomeOveride) {
                biomeTemperatureShade = biomeOverride.TemperatureShadeDecrease; //A, it was me
            }
        }
        // 	Shade
        if (!entityLiving.worldObj.canBlockSeeTheSky(i, j, k) && isDay && !entityLiving.worldObj.isRaining()) {
            biomeTemperature -= biomeTemperatureShade; //WHA-
        }

        if ((!entityLiving.worldObj.provider.hasNoSky && dimensionProp == null) || (dimensionProp != null && dimensionProp.override && dimensionProp.dayNightTemp)) {
            boolean isDesertBiome = false;
            float DesertBiomeTemperatureMultiplier = 1F;

            float biome_DAWN_TEMPERATURE = 4F;
            float biome_DAY_TEMPERATURE = 0F;
            float biome_DUSK_TEMPERATURE = 4F;
            float biome_NIGHT_TEMPERATURE = 8F;

            float biome_DAWN_TEMPERATURE_TERRAFORMED = 4F;
            float biome_DAY_TEMPERATURE_TERRAFORMED = 0F;
            float biome_DUSK_TEMPERATURE_TERRAFORMED = 4F;
            float biome_NIGHT_TEMPERATURE_TERRAFORMED = 8F;

            float biome_EARLY_SPRING_TEMPERATURE_DECREASE =  5.0F;
            float biome_MID_SPRING_TEMPERATURE_DECREASE   = -2.0F;
            float biome_LATE_SPRING_TEMPERATURE_DECREASE  = -1.0F;
            float biome_EARLY_SUMMER_TEMPERATURE_DECREASE = -1.0F;
            float biome_MID_SUMMER_TEMPERATURE_DECREASE   = -3.0F;
            float biome_LATE_SUMMER_TEMPERATURE_DECREASE  = -1.0F;
            float biome_EARLY_AUTUMN_TEMPERATURE_DECREASE =  6.0F;
            float biome_MID_AUTUMN_TEMPERATURE_DECREASE   =  8.0F;
            float biome_LATE_AUTUMN_TEMPERATURE_DECREASE  = 10.0F;
            float biome_EARLY_WINTER_TEMPERATURE_DECREASE = 12.0F;
            float biome_MID_WINTER_TEMPERATURE_DECREASE   = 16.0F;
            float biome_LATE_WINTER_TEMPERATURE_DECREASE  = 10.0F;


            if (biome != null) {
                BiomeProperties biomeOverride = null;
                if (BiomeProperties.base.hasProperty(biome)) {
                    biomeOverride = BiomeProperties.base.getProperty(biome);
                }
                if (biomeOverride != null && biomeOverride.biomeOveride) {

                    isDesertBiome = biomeOverride.isDesertBiome;
                    DesertBiomeTemperatureMultiplier = biomeOverride.DesertBiomeTemperatureMultiplier;

                    biome_DAWN_TEMPERATURE = biomeOverride.DAWN_TEMPERATURE;
                    biome_DAY_TEMPERATURE = biomeOverride.DAY_TEMPERATURE;
                    biome_DUSK_TEMPERATURE = biomeOverride.DUSK_TEMPERATURE;
                    biome_NIGHT_TEMPERATURE = biomeOverride.NIGHT_TEMPERATURE;

                    biome_DAWN_TEMPERATURE_TERRAFORMED = biomeOverride.DAWN_TEMPERATURE_TERRAFORMED;
                    biome_DAY_TEMPERATURE_TERRAFORMED = biomeOverride.DAY_TEMPERATURE_TERRAFORMED;
                    biome_DUSK_TEMPERATURE_TERRAFORMED = biomeOverride.DUSK_TEMPERATURE_TERRAFORMED;
                    biome_NIGHT_TEMPERATURE_TERRAFORMED = biomeOverride.NIGHT_TEMPERATURE_TERRAFORMED;

                    biome_EARLY_SPRING_TEMPERATURE_DECREASE  = biomeOverride.EARLY_SPRING_TEMPERATURE_DECREASE;
                    biome_MID_SPRING_TEMPERATURE_DECREASE    = biomeOverride.MID_SPRING_TEMPERATURE_DECREASE;
                    biome_LATE_SPRING_TEMPERATURE_DECREASE   = biomeOverride.LATE_SPRING_TEMPERATURE_DECREASE;
                    biome_EARLY_SUMMER_TEMPERATURE_DECREASE  = biomeOverride.EARLY_SUMMER_TEMPERATURE_DECREASE;
                    biome_MID_SUMMER_TEMPERATURE_DECREASE    = biomeOverride.MID_SUMMER_TEMPERATURE_DECREASE;
                    biome_LATE_SUMMER_TEMPERATURE_DECREASE   = biomeOverride.LATE_SUMMER_TEMPERATURE_DECREASE;
                    biome_EARLY_AUTUMN_TEMPERATURE_DECREASE  = biomeOverride.EARLY_AUTUMN_TEMPERATURE_DECREASE;
                    biome_MID_AUTUMN_TEMPERATURE_DECREASE    = biomeOverride.MID_AUTUMN_TEMPERATURE_DECREASE;
                    biome_LATE_AUTUMN_TEMPERATURE_DECREASE   = biomeOverride.LATE_AUTUMN_TEMPERATURE_DECREASE;
                    biome_EARLY_WINTER_TEMPERATURE_DECREASE  = biomeOverride.EARLY_WINTER_TEMPERATURE_DECREASE;
                    biome_MID_WINTER_TEMPERATURE_DECREASE    = biomeOverride.MID_WINTER_TEMPERATURE_DECREASE;
                    biome_LATE_WINTER_TEMPERATURE_DECREASE   = biomeOverride.LATE_WINTER_TEMPERATURE_DECREASE;
                }
            }

            float currentTime = entityLiving.worldObj.getWorldTime();

            float temperatureChange;

            if(EnviroMine.isHbmSpaceLoaded()) {
                CelestialBody body = CelestialBody.getBody(entityLiving.worldObj);
                float fullCycle = Math.round((float) (body.getRotationalPeriod() / (1 - (1 / body.getPlanet().getOrbitalPeriod()))));
                float phasePeriod = fullCycle/4F;
                CBT_Atmosphere atmosphere = entityLiving.worldObj.provider instanceof WorldProviderOrbit ? null : CelestialBody.getTrait(entityLiving.worldObj, CBT_Atmosphere.class);
                if(atmosphere != null) {
                    if(atmosphere.hasFluid(Fluids.AIR, 0.19) || atmosphere.hasFluid(Fluids.OXYGEN, 0.09)) {
                        temperatureChange = calculateTemperatureChangeSpace(currentTime % fullCycle, phasePeriod, biome_DAWN_TEMPERATURE_TERRAFORMED, biome_DAY_TEMPERATURE_TERRAFORMED, biome_DUSK_TEMPERATURE_TERRAFORMED, biome_NIGHT_TEMPERATURE_TERRAFORMED);
                        airVentConst = true;
                    } else {
                        temperatureChange = calculateTemperatureChangeSpace(currentTime % fullCycle, phasePeriod, biome_DAWN_TEMPERATURE, biome_DAY_TEMPERATURE, biome_DUSK_TEMPERATURE, biome_NIGHT_TEMPERATURE);
                    }
                } else {
                    temperatureChange = calculateTemperatureChangeSpace(currentTime % fullCycle, phasePeriod, biome_DAWN_TEMPERATURE, biome_DAY_TEMPERATURE, biome_DUSK_TEMPERATURE, biome_NIGHT_TEMPERATURE);
                }
            } else {
                temperatureChange = calculateTemperatureChange(currentTime % 24000L, biome_DAWN_TEMPERATURE, biome_DAY_TEMPERATURE, biome_DUSK_TEMPERATURE, biome_NIGHT_TEMPERATURE);
            }

            if (biome.rainfall <= 0F || isDesertBiome) {
                biomeTemperature -= temperatureChange * DesertBiomeTemperatureMultiplier;
            } else {
                biomeTemperature -= temperatureChange;
            }

            if(EnviroMine.isSereneSeasonsLoaded()) {
                Season.SubSeason currentSubSeason = SeasonHelper.getSeasonState(entityLiving.worldObj).getSubSeason();
                if(currentSubSeason != null) {
                    switch (currentSubSeason) {
                        case EARLY_SPRING -> biomeTemperature -= biome_EARLY_SPRING_TEMPERATURE_DECREASE;
                        case MID_SPRING -> biomeTemperature -= biome_MID_SPRING_TEMPERATURE_DECREASE;
                        case LATE_SPRING -> biomeTemperature -= biome_LATE_SPRING_TEMPERATURE_DECREASE;

                        case EARLY_SUMMER -> biomeTemperature -= biome_EARLY_SUMMER_TEMPERATURE_DECREASE;
                        case MID_SUMMER -> biomeTemperature -= biome_MID_SUMMER_TEMPERATURE_DECREASE;
                        case LATE_SUMMER -> biomeTemperature -= biome_LATE_SUMMER_TEMPERATURE_DECREASE;

                        case EARLY_AUTUMN -> biomeTemperature -= biome_EARLY_AUTUMN_TEMPERATURE_DECREASE;
                        case MID_AUTUMN -> biomeTemperature -= biome_MID_AUTUMN_TEMPERATURE_DECREASE;
                        case LATE_AUTUMN -> biomeTemperature -= biome_LATE_AUTUMN_TEMPERATURE_DECREASE;

                        case EARLY_WINTER -> biomeTemperature -= biome_EARLY_WINTER_TEMPERATURE_DECREASE;
                        case MID_WINTER -> biomeTemperature -= biome_MID_WINTER_TEMPERATURE_DECREASE;
                        case LATE_WINTER -> biomeTemperature -= biome_LATE_WINTER_TEMPERATURE_DECREASE;
                    }
                }
            }

            if(EnviroMine.isHbmSpaceLoaded()) {
                //How stupid am I, that I simply forgot that `(int)` does crazy things with negative numbers?
                ThreeInts pos = new ThreeInts(MathHelper.floor_double(entityLiving.posX), MathHelper.floor_double(entityLiving.posY + entityLiving.getEyeHeight()), MathHelper.floor_double(entityLiving.posZ));

                List<AtmosphereBlob> currentBlobs = ChunkAtmosphereManager.proxy.getBlobs(entityLiving.worldObj, pos.x, pos.y, pos.z);
                    for (AtmosphereBlob blob : currentBlobs) {
                        if (blob.hasFluid(Fluids.AIR, 0.19) || blob.hasFluid(Fluids.OXYGEN, 0.09)) {
                            biomeTemperature = 24.6F; //TODO HARDCODED
                            airVentConst = true;
                        }
                    }
            }
        }

        @SuppressWarnings("unchecked")
        List<Entity> mobList = entityLiving.worldObj.getEntitiesWithinAABBExcludingEntity(entityLiving, AxisAlignedBB.getBoundingBox(entityLiving.posX - 2, entityLiving.posY - 2, entityLiving.posZ - 2, entityLiving.posX + 3, entityLiving.posY + 3, entityLiving.posZ + 3));

        Iterator<Entity> iterator = mobList.iterator();

        float avgEntityTemp = 0.0F;
        int validEntities = 0;

        EnviroDataTracker tracker = lookupTracker(entityLiving);

        if (tracker == null) {
            if (EM_Settings.loggerVerbosity >= EnumLogVerbosity.LOW.getLevel())
                EnviroMine.logger.log(Level.ERROR, "Tracker updating as null! Crash imminent!");
        }

        while (iterator.hasNext()) {
            Entity mob = (Entity) iterator.next();

            if (!(mob instanceof EntityLivingBase)) {
                continue;
            }

            EnviroDataTracker mobTrack = lookupTracker((EntityLivingBase) mob);
            EntityProperties livingProps = null;


            if (EntityProperties.base.hasProperty(mob)) {
                livingProps = EntityProperties.base.getProperty(mob);
            }

//			if(EntityList.getEntityID(mob) > 0)
//			{
//				if(EM_Settings.livingProperties.containsKey(EntityList.getEntityID(mob)))
//				{
//					livingProps = EM_Settings.livingProperties.get(EntityList.getEntityID(mob));
//				}
//			} else if(EntityRegistry.instance().lookupModSpawn(mob.getClass(), false) != null)
//			{
//				if(EM_Settings.livingProperties.containsKey(EntityRegistry.instance().lookupModSpawn(mob.getClass(), false).getModEntityId() + 128))
//				{
//					livingProps = EM_Settings.livingProperties.get(EntityRegistry.instance().lookupModSpawn(mob.getClass(), false).getModEntityId() + 128);
//				}
//			}

            // Villager assistance. Once per day, certain villagers will heal your sanity, hydration, high body temp; or will feed you.
            if (mob instanceof EntityVillager villager && entityLiving instanceof EntityPlayer && entityLiving.canEntityBeSeen(mob) && EM_Settings.villageAssist) {
                Village village = entityLiving.worldObj.villageCollectionObj.findNearestVillage(MathHelper.floor_double(villager.posX), MathHelper.floor_double(villager.posY), MathHelper.floor_double(villager.posZ), 32);

                long assistTime = villager.getEntityData().getLong("Enviro_Assist_Time");
                long worldTime = entityLiving.worldObj.provider.getWorldTime();

                if (village != null && village.getReputationForPlayer(entityLiving.getCommandSenderName()) >= 5 && !villager.isChild() && Math.abs(worldTime - assistTime) > 24000) {
                    if (villager.getProfession() == 2) // Priest
                    {
                        if (sanityBoost < 5F) {
                            sanityBoost = 5F; //TODO HARDCODED
                        }

                        ((EntityPlayer) entityLiving).addStat(EnviroAchievements.tradingFavours, 1);
                    } else if (villager.getProfession() == 0 && isDay) // Farmer
                    {
                        if (tracker.hydration < 50F) {
                            tracker.hydration = 100F; //TODO HARDCODED

                            if (tracker.bodyTemp >= 38F) {
                                tracker.bodyTemp -= 1F; //TODO HARDCODED
                            }
                            entityLiving.worldObj.playSoundAtEntity(entityLiving, "random.drink", 1.0F, 1.0F);
                            villager.playSound("mob.villager.yes", 1.0F, 1.0F);
                            villager.getEntityData().setLong("Enviro_Assist_Time", worldTime);

                            ((EntityPlayer) entityLiving).addStat(EnviroAchievements.tradingFavours, 1);
                        }
                    } else if (villager.getProfession() == 4 && isDay) // Butcher
                    {
                        FoodStats food = ((EntityPlayer) entityLiving).getFoodStats();
                        if (food.getFoodLevel() <= 10) { //TODO HARDCODED
                            food.setFoodLevel(20);
                            entityLiving.worldObj.playSoundAtEntity(entityLiving, "random.burp", 0.5F, entityLiving.worldObj.rand.nextFloat() * 0.1F + 0.9F);
                            villager.playSound("mob.villager.yes", 1.0F, 1.0F);
                            villager.getEntityData().setLong("Enviro_Assist_Time", worldTime);

                            ((EntityPlayer) entityLiving).addStat(EnviroAchievements.tradingFavours, 1);
                        }
                    }
                }
            }

            if (livingProps != null && entityLiving.canEntityBeSeen(mob)) {
                if (sanityRate >= livingProps.ambSanity && livingProps.ambSanity < 0 && sanityRate <= 0) {
                    sanityRate += livingProps.ambSanity;
                } else if (sanityRate <= livingProps.ambSanity && livingProps.ambSanity > 0F) {
                    if (sanityBoost < livingProps.ambSanity) {
                        sanityBoost += livingProps.ambSanity;
                    }
                }

                if (livingProps.ambAir > 0F) {
                    leaves += (livingProps.ambAir / 0.1F);
                } else if (quality >= livingProps.ambAir && livingProps.ambAir < 0 && quality <= 0) {
                    quality += livingProps.ambAir;
                }

                dehydrateBonus -= livingProps.ambHydration;
            }

            if (mobTrack != null) {
                if (livingProps != null) {
                    if (!livingProps.bodyTemp || !livingProps.shouldTrack) {
                        avgEntityTemp += livingProps.ambTemp;
                    } else {
                        avgEntityTemp += mobTrack.bodyTemp;
                    }
                } else {
                    avgEntityTemp += mobTrack.bodyTemp;
                }
                validEntities += 1;
            } else {
                if (livingProps != null) {
                    if (!livingProps.bodyTemp || !livingProps.shouldTrack) {
                        avgEntityTemp += livingProps.ambTemp;
                    } else {
                        avgEntityTemp += 36.6F;
                    }
                    validEntities += 1;
                } else if (!(mob instanceof EntityMob)) {
                    avgEntityTemp += 36.6F;
                    validEntities += 1;
                }
            }
        }

        if (validEntities > 0) {
            avgEntityTemp /= validEntities;

            if (biomeTemperature < avgEntityTemp - 12F) { //TODO HARDCODED
                biomeTemperature = (biomeTemperature + (avgEntityTemp - 12F)) / 2;
            }
        }

        float fireProt = 0;

        {
            ItemStack helmet = entityLiving.getEquipmentInSlot(4);
            ItemStack plate = entityLiving.getEquipmentInSlot(3);
            ItemStack legs = entityLiving.getEquipmentInSlot(2);
            ItemStack boots = entityLiving.getEquipmentInSlot(1);

            float tempMultTotal = 0F;
            float addTemp = 0F;

            if (helmet != null) {
                NBTTagList enchTags = helmet.getEnchantmentTagList();

                if (enchTags != null) {
                    for (int index = 0; index < enchTags.tagCount(); index++) {
                        int enID = ((NBTTagCompound) enchTags.getCompoundTagAt(index)).getShort("id");
                        int enLV = ((NBTTagCompound) enchTags.getCompoundTagAt(index)).getShort("lvl");

                        if (enID == Enchantment.respiration.effectId) {
                            leaves += 3F * enLV;
                        } else if (enID == Enchantment.fireProtection.effectId) {
                            fireProt += enLV;
                        }
                    }
                }

                if (ArmorProperties.base.hasProperty(helmet)) {
                    ArmorProperties props = ArmorProperties.base.getProperty(helmet);

                    if (isDay) {
                        if (entityLiving.worldObj.canBlockSeeTheSky(i, j, k) && biomeTemperature > 0F) {
                            tempMultTotal += (props.sunMult - 1.0F);
                            addTemp += props.sunTemp;
                        } else {
                            tempMultTotal += (props.shadeMult - 1.0F);
                            addTemp += props.shadeTemp;
                        }
                    } else {
                        tempMultTotal += (props.nightMult - 1.0F);
                        addTemp += props.nightTemp;
                    }

                    if (props.air > 0F) {
                        leaves += (props.air / 0.1F);
                    } else if (quality >= props.air && props.air < 0 && quality <= 0) {
                        quality += props.air;
                    }

                    if (sanityRate >= props.sanity && props.sanity < 0 && sanityRate <= 0) {
                        sanityRate += props.sanity;
                    } else if (sanityBoost <= props.sanity && props.sanity > 0F) {
                        sanityBoost += props.sanity;
                    }
                }
            }
            if (plate != null) {
                NBTTagList enchTags = plate.getEnchantmentTagList();

                if (enchTags != null) {
                    for (int index = 0; index < enchTags.tagCount(); index++) {
                        int enID = ((NBTTagCompound) enchTags.getCompoundTagAt(index)).getShort("id");
                        int enLV = ((NBTTagCompound) enchTags.getCompoundTagAt(index)).getShort("lvl");

                        if (enID == Enchantment.fireProtection.effectId) {
                            fireProt += enLV;
                        }
                    }
                }

                if (ArmorProperties.base.hasProperty(plate)) {
                    ArmorProperties props = ArmorProperties.base.getProperty(plate);

                    if (isDay) {
                        if (entityLiving.worldObj.canBlockSeeTheSky(i, j, k) && biomeTemperature > 0F) {
                            tempMultTotal += (props.sunMult - 1.0F);
                            addTemp += props.sunTemp;
                        } else {
                            tempMultTotal += (props.shadeMult - 1.0F);
                            addTemp += props.shadeTemp;
                        }
                    } else {
                        tempMultTotal += (props.nightMult - 1.0F);
                        addTemp += props.nightTemp;
                    }

                    if ((quality <= props.air && props.air > 0F) || (quality >= props.air && props.air < 0 && quality <= 0)) {
                        quality += props.air;
                    }

                    if (sanityRate >= props.sanity && props.sanity < 0 && sanityRate <= 0) {
                        sanityRate += props.sanity;
                    } else if (sanityBoost <= props.sanity && props.sanity > 0F) {
                        sanityBoost += props.sanity;
                    }
                }
            }
            if (legs != null) {
                NBTTagList enchTags = legs.getEnchantmentTagList();

                if (enchTags != null) {
                    for (int index = 0; index < enchTags.tagCount(); index++) {
                        int enID = ((NBTTagCompound) enchTags.getCompoundTagAt(index)).getShort("id");
                        int enLV = ((NBTTagCompound) enchTags.getCompoundTagAt(index)).getShort("lvl");

                        if (enID == Enchantment.fireProtection.effectId) {
                            fireProt += enLV;
                        }
                    }
                }

                if (ArmorProperties.base.hasProperty(legs)) {
                    ArmorProperties props = ArmorProperties.base.getProperty(legs);

                    if (isDay) {
                        if (entityLiving.worldObj.canBlockSeeTheSky(i, j, k) && biomeTemperature > 0F) {
                            tempMultTotal += (props.sunMult - 1.0F);
                            addTemp += props.sunTemp;
                        } else {
                            tempMultTotal += (props.shadeMult - 1.0F);
                            addTemp += props.shadeTemp;
                        }
                    } else {
                        tempMultTotal += (props.nightMult - 1.0F);
                        addTemp += props.nightTemp;
                    }

                    if ((quality <= props.air && props.air > 0F) || (quality >= props.air && props.air < 0 && quality <= 0)) {
                        quality += props.air;
                    }

                    if (sanityRate >= props.sanity && props.sanity < 0 && sanityRate <= 0) {
                        sanityRate += props.sanity;
                    } else if (sanityBoost <= props.sanity && props.sanity > 0F) {
                        sanityBoost += props.sanity;
                    }
                }
            }
            if (boots != null) {
                NBTTagList enchTags = boots.getEnchantmentTagList();

                if (enchTags != null) {
                    for (int index = 0; index < enchTags.tagCount(); index++) {
                        int enID = ((NBTTagCompound) enchTags.getCompoundTagAt(index)).getShort("id");
                        int enLV = ((NBTTagCompound) enchTags.getCompoundTagAt(index)).getShort("lvl");

                        if (enID == Enchantment.fireProtection.effectId) {
                            fireProt += enLV;
                        }
                    }
                }

                if (ArmorProperties.base.hasProperty(boots)) {
                    ArmorProperties props = ArmorProperties.base.getProperty(boots);

                    if (isDay) {
                        if (entityLiving.worldObj.canBlockSeeTheSky(i, j, k) && biomeTemperature > 0F) {
                            tempMultTotal += (props.sunMult - 1.0F);
                            addTemp += props.sunTemp;
                        } else {
                            tempMultTotal += (props.shadeMult - 1.0F);
                            addTemp += props.shadeTemp;
                        }
                    } else {
                        tempMultTotal += (props.nightMult - 1.0F);
                        addTemp += props.nightTemp;
                    }

                    if ((quality <= props.air && props.air > 0F) || (quality >= props.air && props.air < 0 && quality <= 0)) {
                        quality += props.air;
                    }

                    if (sanityRate >= props.sanity && props.sanity < 0 && sanityRate <= 0) {
                        sanityRate += props.sanity;
                    } else if (sanityBoost <= props.sanity && props.sanity > 0F) {
                        sanityBoost += props.sanity;
                    }
                }
            }

            biomeTemperature *= (1F + tempMultTotal);
            biomeTemperature += addTemp;
            fireProt = 1F - fireProt / 18F;
        }

        //TODO HARDCODED
        if (entityLiving.isInWater()) {
            if (biomeTemperature > 25F) {
                if (biomeTemperature > 50F) {
                    biomeTemperature -= 50F;
                } else {
                    biomeTemperature = 25F;
                }
            }
            dropSpeed = 0.01F;
        }

        float ambientTemperature = 0F;

        if (blockAndItemTempInfluence > biomeTemperature) {
            ambientTemperature = (biomeTemperature + blockAndItemTempInfluence) / 2; //TODO HARDCODED, CONSTANT ALERT
            if (blockAndItemTempInfluence > (biomeTemperature + 5F)) {
                riseSpeed = 0.005F; //TODO HARDCODED
            }
        } else {
            ambientTemperature = biomeTemperature;
        }

        if (entityLiving.getActivePotionEffect(Potion.hunger) != null) {
            dehydrateBonus += 0.1F; //TODO HARDCODED
        }

        if (nearLava) {
            if (riseSpeed <= 0.005F) { //TODO HARDCODED
                riseSpeed = 0.005F;
            }
            dehydrateBonus += 0.05F;
            if (animalHostility == 0) {
                animalHostility = 1;
            }
        }

        BiomeProperties biomeProp = null;
        if (BiomeProperties.base.hasProperty(biome)) {
            biomeProp = BiomeProperties.base.getProperty(biome);

            if (biomeProp != null && biomeProp.biomeOveride) {
                dehydrateBonus += biomeProp.dehydrateRate;

                if (biomeProp.tempRate > 0) {
                    riseSpeed += biomeProp.tempRate;
                } else {
                    dropSpeed += biomeProp.tempRate;
                }

                float temperatureRate = 0;
                if(biomeProp.tempRate_DAWN != 0 || biomeProp.tempRate_DAY != 0 || biomeProp.tempRate_DUSK != 0 || biomeProp.tempRate_NIGHT != 0) {
                    float currentTime = entityLiving.worldObj.getWorldTime();
                    if(EnviroMine.isHbmSpaceLoaded()) {
                        CelestialBody body = CelestialBody.getBody(entityLiving.worldObj);
                        float phasePeriod = Math.round((float) (body.getRotationalPeriod() / (1 - (1 / body.getPlanet().getOrbitalPeriod()))) / 4F);
                        temperatureRate = calculateTemperatureChangeSpace(currentTime, phasePeriod, biomeProp.tempRate_DAWN, biomeProp.tempRate_DAY, biomeProp.tempRate_DUSK, biomeProp.tempRate_NIGHT);
                    }
                    else {
                        temperatureRate = calculateTemperatureChange(currentTime % 24000L, biomeProp.tempRate_DAWN, biomeProp.tempRate_DAY, biomeProp.tempRate_DUSK, biomeProp.tempRate_NIGHT);
                    }
                }

                if(!airVentConst) {
                    if (temperatureRate > 0) {
                        riseSpeed += temperatureRate;
                    } else {
                        dropSpeed -= temperatureRate; //INVERTED LOGIC, AND MULTIPLIED BY 10,  FU-
                    }
                }



                sanityRate += biomeProp.sanityRate;
            }

        }

        if (biome.getIntRainfall() == 0 && isDay) {
            dehydrateBonus += 0.05F; //TODO HARDCODED
            if (animalHostility == 0) {
                animalHostility = 1;
            }
        }
        if(isHbmLoaded()) {
            ItemStack helmet = entityLiving.getEquipmentInSlot(4);
            ItemStack plate0 = entityLiving.getEquipmentInSlot(3);
            ItemStack legs = entityLiving.getEquipmentInSlot(2);
            ItemStack boots = entityLiving.getEquipmentInSlot(1);
            ArmorProperties helmetprops = null;
            ArmorProperties plateprops = null;
            ArmorProperties legsprops = null;
            ArmorProperties bootsprops = null;
            boolean ImmunityBurning = false;
            boolean ImmunityFull = false;
            if(helmet != null) {if (ArmorProperties.base.hasProperty(helmet)) {helmetprops = ArmorProperties.base.getProperty(helmet);}}
            if(plate0 != null) {if (ArmorProperties.base.hasProperty(plate0)) {plateprops = ArmorProperties.base.getProperty(plate0);}}
            if(legs != null) {if (ArmorProperties.base.hasProperty(legs)) {legsprops = ArmorProperties.base.getProperty(legs);}}
            if(boots != null) {if (ArmorProperties.base.hasProperty(boots)) {bootsprops = ArmorProperties.base.getProperty(boots);}}
            if(helmetprops != null && plateprops != null && legsprops != null && bootsprops != null) {
                if(helmetprops.isTemperatureResistance && plateprops.isTemperatureResistance && legsprops.isTemperatureResistance && bootsprops.isTemperatureResistance) {
                    ImmunityBurning = true; // All armor isTemperatureResistance ? ImmunityBurning = true
                    ImmunityFull = helmetprops.isTemperatureSealed && plateprops.isTemperatureSealed && legsprops.isTemperatureSealed && bootsprops.isTemperatureSealed;
                    // All armor isTemperatureSealed ? ImmunityFull = true
                } else {
                    ImmunityBurning = false; // All armor NOT isTemperatureResistance ? ImmunityBurning = false
                }
            }

            if (entityLiving instanceof EntityPlayer player && ArmorFSB.hasFSBArmor(player)) {
                ItemStack plate = player.inventory.armorInventory[2];
                ArmorFSB chestplate = (ArmorFSB) plate.getItem();
                if (!entityLiving.isPotionActive(Potion.fireResistance) && !(chestplate.fireproof)) {
                    if (entityLiving.worldObj.getBlock(i, j, k).getMaterial() == Material.lava && !(chestplate == ModItems.hev_plate || chestplate == ModItems.envsuit_plate) && !ImmunityFull) {
                        ambientTemperature += EM_Settings.LavaBlockAmbientTemperature;
                        riseSpeed += EM_Settings.RiseSpeedLava;
                    } else if (entityLiving.worldObj.getBlock(i, j, k).getMaterial() == Material.lava && (chestplate == ModItems.hev_plate || chestplate == ModItems.envsuit_plate) && !ImmunityFull) {
                        ambientTemperature += EM_Settings.BurningambientTemperature;
                        riseSpeed += EM_Settings.RiseSpeedLavaDecr;
                    }
                    else if (entityLiving.isBurning() && !(chestplate == ModItems.hev_plate || chestplate == ModItems.envsuit_plate) && !ImmunityBurning) {
                        if (ambientTemperature <= EM_Settings.BurningambientTemperature) {
                            ambientTemperature += EM_Settings.BurningambientTemperature;
                        }
                        if (riseSpeed < EM_Settings.RiseSpeedMin) {
                            riseSpeed = EM_Settings.RiseSpeedMin;
                        }
                    }
                }
            }
        }
        if (!entityLiving.isPotionActive(Potion.fireResistance)) {
            ItemStack helmet = entityLiving.getEquipmentInSlot(4);
            ItemStack plate = entityLiving.getEquipmentInSlot(3);
            ItemStack legs = entityLiving.getEquipmentInSlot(2);
            ItemStack boots = entityLiving.getEquipmentInSlot(1);
            ArmorProperties helmetprops = null;
            ArmorProperties plateprops = null;
            ArmorProperties legsprops = null;
            ArmorProperties bootsprops = null;
            boolean ImmunityBurning = false;
            boolean ImmunityFull = false;
            if(helmet != null) {if (ArmorProperties.base.hasProperty(helmet)) {helmetprops = ArmorProperties.base.getProperty(helmet);}}
            if(plate != null) {if (ArmorProperties.base.hasProperty(plate)) {plateprops = ArmorProperties.base.getProperty(plate);}}
            if(legs != null) {if (ArmorProperties.base.hasProperty(legs)) {legsprops = ArmorProperties.base.getProperty(legs);}}
            if(boots != null) {if (ArmorProperties.base.hasProperty(boots)) {bootsprops = ArmorProperties.base.getProperty(boots);}}
            if(helmetprops != null && plateprops != null && legsprops != null && bootsprops != null) {
                if(helmetprops.isTemperatureResistance && plateprops.isTemperatureResistance && legsprops.isTemperatureResistance && bootsprops.isTemperatureResistance) {
                    ImmunityBurning = true;
                    ImmunityFull = helmetprops.isTemperatureSealed && plateprops.isTemperatureSealed && legsprops.isTemperatureSealed && bootsprops.isTemperatureSealed;
                } else {
                    ImmunityBurning = false;
                }
            }
            if (entityLiving.worldObj.getBlock(i, j, k).getMaterial() == Material.lava && !ImmunityFull) {
                if(ImmunityBurning) {
                    ambientTemperature += EM_Settings.BurningambientTemperature;
                    riseSpeed = EM_Settings.RiseSpeedLavaDecr;
                } else {
                    ambientTemperature += EM_Settings.LavaBlockAmbientTemperature;
                    riseSpeed = EM_Settings.RiseSpeedLava;
                }
            } else if (entityLiving.isBurning() && !ImmunityBurning) {
                if (ambientTemperature <= EM_Settings.BurningambientTemperature) {
                    ambientTemperature += EM_Settings.BurningambientTemperature;
                }
                if (riseSpeed < EM_Settings.RiseSpeedMin) {
                    riseSpeed = EM_Settings.RiseSpeedMin;
                }
            }
        }

		quality += (leaves * 0.1F);
		sanityRate += sanityBoost;

		if(quality < 0)
		{
			quality *= ((float)solidBlocks)/totalBlocks;
		}

		if(entityLiving.isSprinting())
		{
//TODO HARDCODED
			dehydrateBonus += 0.05F;
			if(riseSpeed < 0.01F)
			{
				riseSpeed = 0.01F;
			}
			ambientTemperature += EM_Settings.SprintambientTemperature;
		}

		if(dimensionProp != null && dimensionProp.override)
		{
			quality = quality * (float) dimensionProp.airMulti + dimensionProp.airRate;
			riseSpeed = riseSpeed * (float) dimensionProp.tempMulti + dimensionProp.tempRate;
			dropSpeed = dropSpeed * (float) dimensionProp.tempMulti + dimensionProp.tempRate;
			sanityRate = sanityRate * (float) dimensionProp.sanityMulti + dimensionProp.sanityRate;
			dehydrateBonus = dehydrateBonus * (float) dimensionProp.hydrationMulti + dimensionProp.hydrationRate;
		}

		// Air quality delta
		data[AIR_QUALITY_DELTA_INDEX] = quality * (float)EM_Settings.airMult;
		// Air temp
		data[AMBIENT_TEMP_INDEX] = entityLiving.isPotionActive(Potion.fireResistance) && ambientTemperature > 36.6F? 36.6F : (ambientTemperature > 36.6F? 36.6F + ((ambientTemperature-36.6F) * fireProt): ambientTemperature);
		// Is "near lava"?
		data[NEAR_LAVA_INDEX] = nearLava? 1 : 0;
		// Dehydration
		data[DEHYDRATION_DELTA_INDEX] = dehydrateBonus * (float)EM_Settings.hydrationMult;
		data[BODY_TEMP_DROP_SPEED_INDEX] = dropSpeed * (float)EM_Settings.tempMult;
		data[BODY_TEMP_RISE_SPEED_INDEX] = riseSpeed * (float)EM_Settings.tempMult * (tracker.bodyTemp < 36.6F? 1F : fireProt);
		data[ANIMAL_HOSTILITY_INDEX] = animalHostility;
		data[SANITY_DELTA_INDEX] = sanityRate * (float)EM_Settings.sanityMult;

		if(EnviroMine.proxy.isClient() && entityLiving.getCommandSenderName().equals(Minecraft.getMinecraft().thePlayer.getCommandSenderName()) && timer.isRunning())
		{
			timer.stop();
			Debug_Info.DB_timer = timer.toString();
			timer.reset();
		}
		return data;
	}

    // Function to calculate temperature change
    public static float calculateTemperatureChange(float currentTime, float DAWN_TEMPERATURE, float DAY_TEMPERATURE, float DUSK_TEMPERATURE, float NIGHT_TEMPERATURE) {
        float temperatureChange;
        // from 0 to 6000 ticks (dawn to noon)
        if (currentTime >= 0 && currentTime < 6000) {
            temperatureChange = DAWN_TEMPERATURE - ((DAWN_TEMPERATURE - DAY_TEMPERATURE) / 6000f) * currentTime;
        }
        // from 6000 to 12000 ticks (noon to dusk)
        else if (currentTime >= 6000 && currentTime < 12000) {
            temperatureChange = DAY_TEMPERATURE + ((DUSK_TEMPERATURE - DAY_TEMPERATURE) / 6000f) * (currentTime - 6000);
        }
        // from 12000 to 18000 ticks (dusk to midnight)
        else if (currentTime >= 12000 && currentTime < 18000) {
            temperatureChange = DUSK_TEMPERATURE + ((NIGHT_TEMPERATURE - DUSK_TEMPERATURE) / 6000f) * (currentTime - 12000);
        }
        // from 18000 to 24000 ticks (midnight to dawn)
        else if (currentTime >= 18000 && currentTime < 24000) {
            temperatureChange = NIGHT_TEMPERATURE - ((NIGHT_TEMPERATURE - DAWN_TEMPERATURE) / 6000f) * (currentTime - 18000);
        }
        else {
            // If currentTime doesn't fall within the specified range
            temperatureChange = 0;
        }

        return temperatureChange;
    }
    public static float calculateTemperatureChangeSpace(float currentTime, float phasePeriod, float DAWN_TEMPERATURE, float DAY_TEMPERATURE, float DUSK_TEMPERATURE, float NIGHT_TEMPERATURE) {
        float temperatureChange;
        // dawn to noon
        if (currentTime >= 0 && currentTime < phasePeriod) {
            temperatureChange = DAWN_TEMPERATURE - ((DAWN_TEMPERATURE - DAY_TEMPERATURE) / phasePeriod) * currentTime;
        }
        // noon to dusk
        else if (currentTime >= phasePeriod && currentTime < phasePeriod*2) {
            temperatureChange = DAY_TEMPERATURE + ((DUSK_TEMPERATURE - DAY_TEMPERATURE) / phasePeriod) * (currentTime - phasePeriod);
        }
        // dusk to midnight
        else if (currentTime >= phasePeriod*2 && currentTime < phasePeriod*3) {
            temperatureChange = DUSK_TEMPERATURE + ((NIGHT_TEMPERATURE - DUSK_TEMPERATURE) / phasePeriod) * (currentTime - phasePeriod*2);
        }
        // midnight to dawn
        else if (currentTime >= phasePeriod*3 && currentTime < phasePeriod*4) {
            temperatureChange = NIGHT_TEMPERATURE - ((NIGHT_TEMPERATURE - DAWN_TEMPERATURE) / phasePeriod) * (currentTime - phasePeriod*3);
        }
        else {
            // If currentTime doesn't fall within the specified range
            temperatureChange = 0;
        }

        return temperatureChange;
    }

	public static void removeTracker(EnviroDataTracker tracker)
	{
		if(trackerList.containsValue(tracker))
		{
			tracker.isDisabled = true;
			if(tracker.trackedEntity instanceof EntityPlayer)
			{
				trackerList.remove(tracker.trackedEntity.getCommandSenderName());
			} else
			{
				trackerList.remove("" + tracker.trackedEntity.getEntityId());
			}
		}
	}

	public static void saveAndRemoveTracker(EnviroDataTracker tracker)
	{
		if(trackerList.containsValue(tracker))
		{
			tracker.isDisabled = true;
			NBTTagCompound tags = tracker.trackedEntity.getEntityData();
			tags.setFloat("ENVIRO_AIR", tracker.airQuality);
			tags.setFloat("ENVIRO_HYD", tracker.hydration);
			tags.setFloat("ENVIRO_TMP", tracker.bodyTemp);
			tags.setFloat("ENVIRO_SAN", tracker.sanity);
			if(tracker.trackedEntity instanceof EntityPlayer)
			{
				trackerList.remove(tracker.trackedEntity.getCommandSenderName());
			} else
			{
				trackerList.remove("" + tracker.trackedEntity.getEntityId());
			}
		}
	}

	public static void saveTracker(EnviroDataTracker tracker)
	{
		NBTTagCompound tags = tracker.trackedEntity.getEntityData();
		tags.setFloat("ENVIRO_AIR", tracker.airQuality);
		tags.setFloat("ENVIRO_HYD", tracker.hydration);
		tags.setFloat("ENVIRO_TMP", tracker.bodyTemp);
		tags.setFloat("ENVIRO_SAN", tracker.sanity);
	}

    //Pizdec prosto
	public static void removeAllTrackers()
	{
        for (EnviroDataTracker tracker : trackerList.values()) {
            tracker.isDisabled = true;
        }
		trackerList.clear();
	}

    //EBANIY BLYAT'
	public static void saveAndDeleteAllTrackers()
	{
        for (EnviroDataTracker tracker : trackerList.values()) {
            tracker.isDisabled = true;
            NBTTagCompound tags = tracker.trackedEntity.getEntityData();
            tags.setFloat("ENVIRO_AIR", tracker.airQuality);
            tags.setFloat("ENVIRO_HYD", tracker.hydration);
            tags.setFloat("ENVIRO_TMP", tracker.bodyTemp);
            tags.setFloat("ENVIRO_SAN", tracker.sanity);
        }
		trackerList.clear();
	}

	public static void saveAndDeleteWorldTrackers(World world)
	{
		HashMap<String,EnviroDataTracker> tempList = new HashMap<String,EnviroDataTracker>(trackerList);
        for (EnviroDataTracker tracker : tempList.values()) {
            if (tracker.trackedEntity.worldObj == world) {
                NBTTagCompound tags = tracker.trackedEntity.getEntityData();
                tags.setFloat("ENVIRO_AIR", tracker.airQuality);
                tags.setFloat("ENVIRO_HYD", tracker.hydration);
                tags.setFloat("ENVIRO_TMP", tracker.bodyTemp);
                tags.setFloat("ENVIRO_SAN", tracker.sanity);
                tracker.isDisabled = true;
                if (tracker.trackedEntity instanceof EntityPlayer) {
                    trackerList.remove(tracker.trackedEntity.getCommandSenderName());
                } else {
                    trackerList.remove("" + tracker.trackedEntity.getEntityId());
                }
            }
        }
	}

	public static void saveAllWorldTrackers(World world)
	{
		HashMap<String,EnviroDataTracker> tempList = new HashMap<String,EnviroDataTracker>(trackerList);
        for (EnviroDataTracker tracker : tempList.values()) {
            if (tracker.trackedEntity.worldObj == world) {
                NBTTagCompound tags = tracker.trackedEntity.getEntityData();
                tags.setFloat("ENVIRO_AIR", tracker.airQuality);
                tags.setFloat("ENVIRO_HYD", tracker.hydration);
                tags.setFloat("ENVIRO_TMP", tracker.bodyTemp);
                tags.setFloat("ENVIRO_SAN", tracker.sanity);
            }
        }
	}

	public static EntityPlayer findPlayer(String username)
	{
		World[] worlds = new World[1];

		if(EnviroMine.proxy.isClient())
		{
			if(Minecraft.getMinecraft().isIntegratedServerRunning())
			{
				worlds = MinecraftServer.getServer().worldServers;
			} else
			{
				worlds[0] = Minecraft.getMinecraft().thePlayer.worldObj;
			}
		} else
		{
			worlds = MinecraftServer.getServer().worldServers;
		}

		for(int i = worlds.length - 1; i >= 0; i -= 1)
		{
			if(worlds[i] == null)
			{
				continue;
			}
			EntityPlayer player = worlds[i].getPlayerEntityByName(username);

			if(player != null)
			{
				if(player.isEntityAlive())
				{
					return player;
				}
			}
		}

		return null;
	}

	public static void createFX(EntityLivingBase entityLiving)
	{
		float rndX = (entityLiving.getRNG().nextFloat() * entityLiving.width * 2) - entityLiving.width;
		float rndY = entityLiving.getRNG().nextFloat() * entityLiving.height;
		float rndZ = (entityLiving.getRNG().nextFloat() * entityLiving.width * 2) - entityLiving.width;
		EnviroDataTracker tracker = lookupTracker(entityLiving);

		if(entityLiving instanceof EntityPlayer && !(entityLiving instanceof EntityPlayerMP))
		{
			rndY = -rndY;
		}

		if(tracker != null)
		{
			if(tracker.bodyTemp >= EM_Settings.SweatTemperature && UI_Settings.sweatParticals)
			{
				entityLiving.worldObj.spawnParticle("dripWater", entityLiving.posX + rndX, entityLiving.posY + rndY, entityLiving.posZ + rndZ, 0.0D, 0.0D, 0.0D);
			}

			if(tracker.trackedEntity.isPotionActive(EnviroPotion.insanity) && UI_Settings.insaneParticals)
			{
				entityLiving.worldObj.spawnParticle("portal", entityLiving.posX + rndX, entityLiving.posY + rndY, entityLiving.posZ + rndZ, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	public static float getTempFalloff(float blockTemperature, float cartesianDistance, int scanRadius, float dropoffPower)
	{
		return cartesianDistance <= scanRadius ? (float) (blockTemperature * (1 - Math.pow(MathHelper.clamp_float(cartesianDistance-EM_Settings.auraRadius, 0, scanRadius)/scanRadius, 1F/dropoffPower))) : 0F;
	}
}
