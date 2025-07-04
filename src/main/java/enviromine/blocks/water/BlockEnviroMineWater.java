package enviromine.blocks.water;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import com.hbm.util.ContaminationUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import enviromine.blocks.water.compat.BlockEnviroMineWater_NTM_SPACE;
import enviromine.core.EnviroMine;
import enviromine.utils.WaterUtils;

public class BlockEnviroMineWater extends BlockFluidClassic {

    public static IIcon stillWater;
    public static IIcon flowingWater;

    public BlockEnviroMineWater(Fluid fluid, Material material) {
        super(fluid, material);
    }

    // No, I DON'T want blue water everywhere, thanks
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess worldIn, int x, int y, int z) {
        int l = 0;
        int i1 = 0;
        int j1 = 0;

        for (int k1 = -1; k1 <= 1; ++k1) {
            for (int l1 = -1; l1 <= 1; ++l1) {
                int i2 = worldIn.getBiomeGenForCoords(x + l1, z + k1)
                    .getWaterColorMultiplier();
                l += (i2 & 16711680) >> 16;
                i1 += (i2 & 65280) >> 8;
                j1 += i2 & 255;
            }
        }

        return (l / 9 & 255) << 16 | (i1 / 9 & 255) << 8 | j1 / 9 & 255;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (!world.isRemote) {
            Block block = world.getBlock(x, y, z);
            if (block instanceof BlockEnviroMineWater water) {
                if (WaterUtils.getTypeFromFluid(water.getFluid()).isRadioactive) {
                    if (entity instanceof EntityLivingBase entityLivingBase) ContaminationUtil.contaminate(
                        entityLivingBase,
                        ContaminationUtil.HazardType.RADIATION,
                        ContaminationUtil.ContaminationType.CREATIVE,
                        0.125F);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(final int side, final int meta) {
        return (side == 0 || side == 1) ? stillWater : flowingWater;
    }

    @Override
    public boolean canDisplace(IBlockAccess world, int x, int y, int z) {
        return super.canDisplace(world, x, y, z);
    }

    @Override
    public boolean displaceIfPossible(World world, int x, int y, int z) {
        return super.displaceIfPossible(world, x, y, z);
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        stillWater = register.registerIcon("water_still");
        flowingWater = register.registerIcon("water_flow");
    }

    public void updateTick(World world, int x, int y, int z, Random rand) {
        if (EnviroMine.isHbmSpaceLoaded) {
            if (BlockEnviroMineWater_NTM_SPACE.checkEvaporation(world, x, y, z, tickRate)) {
                return; // Will not try to spill further if removed
            }
        }

        // INFINITE WATER
        if (!this.isSourceBlock(world, x, y, z)) {
            int adjacentSourceBlocks = (this.isSourceBlock(world, x - 1, y, z) ? 1 : 0)
                + (this.isSourceBlock(world, x + 1, y, z) ? 1 : 0)
                + (this.isSourceBlock(world, x, y, z - 1) ? 1 : 0)
                + (this.isSourceBlock(world, x, y, z + 1) ? 1 : 0);
            int densityDir = getDensity(world, x, y, z) > 0 ? -1 : 1;
            if (adjacentSourceBlocks >= 2 && (world.getBlock(x, y + densityDir, z)
                .getMaterial()
                .isSolid() || this.isSourceBlock(world, x, y + densityDir, z))) {
                world.setBlockMetadataWithNotify(x, y, z, 0, 3);
            }
        }

        super.updateTick(world, x, y, z, rand);
    }

}
