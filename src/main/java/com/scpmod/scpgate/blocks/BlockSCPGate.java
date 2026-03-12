package com.scpmod.scpgate.blocks;

import com.scpmod.scpgate.init.ModBlocks;
import com.scpmod.scpgate.tileentity.TileEntitySCPGate;
import com.scpmod.scpgate.tileentity.TileEntitySCPGateFrame;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockSCPGate extends Block implements ITileEntityProvider {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    /** Evita ricorsione durante la distruzione della struttura. */
    public static boolean isBeingBroken = false;

    public BlockSCPGate() {
        super(Material.IRON);
        setHardness(5.0f);
        setResistance(2000.0f);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    // ─── BlockState ──────────────────────────────────────────────────────────

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing[] vals = { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST };
        return getDefaultState().withProperty(FACING, vals[meta & 3]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        switch (state.getValue(FACING)) {
            case SOUTH: return 1;
            case EAST:  return 2;
            case WEST:  return 3;
            default:    return 0; // NORTH
        }
    }

    // ─── Rendering ───────────────────────────────────────────────────────────

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED; // solo TESR
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    // ─── Bounding box (fisico) ────────────────────────────────────────────────

    private static final AxisAlignedBB BB_NS = new AxisAlignedBB(0, 0, 0.25, 1, 1, 0.75);
    private static final AxisAlignedBB BB_EW = new AxisAlignedBB(0.25, 0, 0, 0.75, 1, 1);

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing f = state.getValue(FACING);
        return (f == EnumFacing.NORTH || f == EnumFacing.SOUTH) ? BB_NS : BB_EW;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntitySCPGate) {
            if (((TileEntitySCPGate) te).getOpenProgress() > 0.001f) return NULL_AABB;
        }
        return getBoundingBox(state, worldIn, pos);
    }

    // ─── Placement ───────────────────────────────────────────────────────────

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        if (worldIn.isRemote) return;

        EnumFacing facing = state.getValue(FACING);

        // Verifica che ci sia spazio libero per la struttura 4×3
        for (int ox = 0; ox < 4; ox++) {
            for (int oy = 0; oy < 3; oy++) {
                if (ox == 0 && oy == 0) continue;
                BlockPos fp = getOffsetPos(pos, facing, ox, oy);
                IBlockState fs = worldIn.getBlockState(fp);
                if (!fs.getBlock().isReplaceable(worldIn, fp)) {
                    worldIn.setBlockToAir(pos);
                    if (placer instanceof EntityPlayer) {
                        ((EntityPlayer) placer).sendMessage(
                            new net.minecraft.util.text.TextComponentString(
                                "\u00a7cNon c'è spazio sufficiente per l'SCP Gate (4×3)!"));
                    }
                    return;
                }
            }
        }

        // Piazza i frame block
        for (int ox = 0; ox < 4; ox++) {
            for (int oy = 0; oy < 3; oy++) {
                if (ox == 0 && oy == 0) continue;
                BlockPos fp = getOffsetPos(pos, facing, ox, oy);
                worldIn.setBlockState(fp,
                    ModBlocks.SCP_GATE_FRAME.getDefaultState()
                        .withProperty(BlockSCPGateFrame.FACING, facing));
                TileEntity te = worldIn.getTileEntity(fp);
                if (te instanceof TileEntitySCPGateFrame) {
                    ((TileEntitySCPGateFrame) te).setMasterPos(pos);
                }
            }
        }
    }

    // ─── Distruzione struttura ────────────────────────────────────────────────

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!isBeingBroken && !worldIn.isRemote) {
            isBeingBroken = true;
            EnumFacing facing = state.getValue(FACING);
            for (int ox = 0; ox < 4; ox++) {
                for (int oy = 0; oy < 3; oy++) {
                    if (ox == 0 && oy == 0) continue;
                    BlockPos fp = getOffsetPos(pos, facing, ox, oy);
                    if (worldIn.getBlockState(fp).getBlock() == ModBlocks.SCP_GATE_FRAME) {
                        worldIn.setBlockToAir(fp);
                    }
                }
            }
            isBeingBroken = false;
        }
        super.breakBlock(worldIn, pos, state);
    }

    // ─── Redstone ────────────────────────────────────────────────────────────

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntitySCPGate) {
                recheckPower(worldIn, pos, state, (TileEntitySCPGate) te);
            }
        }
    }

    public static void recheckPower(World world, BlockPos masterPos, IBlockState masterState,
                                    TileEntitySCPGate gateTe) {
        EnumFacing facing = masterState.getValue(FACING);
        boolean anyPowered = world.isBlockPowered(masterPos);
        for (int ox = 0; ox < 4 && !anyPowered; ox++) {
            for (int oy = 0; oy < 3 && !anyPowered; oy++) {
                if (ox == 0 && oy == 0) continue;
                BlockPos fp = getOffsetPos(masterPos, facing, ox, oy);
                anyPowered = world.isBlockPowered(fp);
            }
        }
        gateTe.setPowered(anyPowered);
    }

    // ─── TileEntity ──────────────────────────────────────────────────────────

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySCPGate();
    }

    // ─── Click (nessuna azione) ───────────────────────────────────────────────

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
                                    EntityPlayer playerIn, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        return false; // nessuna azione al click, solo redstone
    }

    // ─── Utilità ─────────────────────────────────────────────────────────────

    /** Restituisce la posizione nel mondo data l'offset locale (ox, oy) dal blocco master. */
    public static BlockPos getOffsetPos(BlockPos masterPos, EnumFacing facing, int ox, int oy) {
        switch (facing) {
            case NORTH:
            case SOUTH:
                return masterPos.add(ox, oy, 0);
            case EAST:
            case WEST:
                return masterPos.add(0, oy, ox);
            default:
                return masterPos.add(ox, oy, 0);
        }
    }
}
