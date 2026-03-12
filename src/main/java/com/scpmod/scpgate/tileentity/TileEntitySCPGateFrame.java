package com.scpmod.scpgate.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class TileEntitySCPGateFrame extends TileEntity {

    @Nullable
    private BlockPos masterPos;

    public void setMasterPos(BlockPos pos) {
        this.masterPos = pos;
        markDirty();
    }

    @Nullable
    public BlockPos getMasterPos() {
        return masterPos;
    }

    // ─── NBT ─────────────────────────────────────────────────────────────────

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (masterPos != null) {
            compound.setInteger("masterX", masterPos.getX());
            compound.setInteger("masterY", masterPos.getY());
            compound.setInteger("masterZ", masterPos.getZ());
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("masterX")) {
            masterPos = new BlockPos(
                compound.getInteger("masterX"),
                compound.getInteger("masterY"),
                compound.getInteger("masterZ")
            );
        }
    }

    // ─── Sync server → client ─────────────────────────────────────────────────

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
}
