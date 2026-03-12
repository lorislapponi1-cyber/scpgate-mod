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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockSCPGateFrame extends Block implements ITileEntityProvider {

    public static final PropertyDirection FACING =
            PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockSCPGateFrame() {
        super(Material.IRON);
        setHardness(5.0f);
        setResistance(2000.0f);
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
            default:    return 0;
        }
    }

    // ─── Rendering: completamente invisibile, ci pensa il TESR del master ────

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state)   { return false; }

    // ─── Bounding box ────────────────────────────────────────────────────────

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
        if (te instanceof TileEntitySCPGateFrame) {
            BlockPos mp = ((TileEntitySCPGateFrame) te).getMasterPos();
            if (mp != null) {
                TileEntity mte = worldIn.getTileEntity(mp);
                if (mte instanceof TileEntitySCPGate) {
                    if (((TileEntitySCPGate) mte).getOpenProgress() > 0.001f) return NULL_AABB;
                }
            }
        }
        return getBoundingBox(state, worldIn, pos);
    }

    // ─── Nessun drop: il master dropa l'item ─────────────────────────────────

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return new ArrayList<>();
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    // ─── Distruzione: rompe tutta la struttura e droppa item dal master ───────

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!BlockSCPGate.isBeingBroken && !worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntitySCPGateFrame) {
                BlockPos masterPos = ((TileEntitySCPGateFrame) te).getMasterPos();
                if (masterPos != null) {
                    IBlockState ms = worldIn.getBlockState(masterPos);
                    if (ms.getBlock() == ModBlocks.SCP_GATE) {
                        // Dropa l'item
                        ms.getBlock().dropBlockAsItem(worldIn, masterPos, ms, 0);
                        // Rompe il master (e di conseguenza tutti i frame)
                        BlockSCPGate.isBeingBroken = true;
                        worldIn.setBlockToAir(masterPos);
                        // Rimuovi tutti gli altri frame
                        EnumFacing facing = ms.getValue(BlockSCPGate.FACING);
                        for (int ox = 0; ox < 4; ox++) {
                            for (int oy = 0; oy < 3; oy++) {
                                if (ox == 0 && oy == 0) continue;
                                BlockPos fp = BlockSCPGate.getOffsetPos(masterPos, facing, ox, oy);
                                if (!fp.equals(pos) && worldIn.getBlockState(fp).getBlock() == ModBlocks.SCP_GATE_FRAME) {
                                    worldIn.setBlockToAir(fp);
                                }
                            }
                        }
                        BlockSCPGate.isBeingBroken = false;
                    }
                }
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    // ─── Redstone: notifica il master ────────────────────────────────────────

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntitySCPGateFrame) {
                BlockPos mp = ((TileEntitySCPGateFrame) te).getMasterPos();
                if (mp != null) {
                    TileEntity mte = worldIn.getTileEntity(mp);
                    if (mte instanceof TileEntitySCPGate) {
                        IBlockState ms = worldIn.getBlockState(mp);
                        BlockSCPGate.recheckPower(worldIn, mp, ms, (TileEntitySCPGate) mte);
                    }
                }
            }
        }
    }

    // ─── TileEntity ──────────────────────────────────────────────────────────

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySCPGateFrame();
    }
}
