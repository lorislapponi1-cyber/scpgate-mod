package com.scpmod.scpgate.tileentity;

import com.scpmod.scpgate.init.ModSounds;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;

public class TileEntitySCPGate extends TileEntity implements ITickable {

    /** Velocità di animazione: 1/20 → 20 tick = 1 secondo per aprirsi/chiudersi */
    private static final float ANIM_SPEED = 1.0f / 20.0f;

    private boolean powered          = false;
    private float   openProgress     = 0.0f;
    private float   prevOpenProgress = 0.0f;

    // ─── Tick ─────────────────────────────────────────────────────────────────

    @Override
    public void update() {
        prevOpenProgress = openProgress;

        if (powered) {
            if (openProgress < 1.0f) {
                // Suono all'inizio dell'apertura
                if (openProgress == 0.0f && !world.isRemote) {
                    world.playSound(null, pos, ModSounds.DOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                openProgress = Math.min(1.0f, openProgress + ANIM_SPEED);
                markDirty();
                if (!world.isRemote) syncToClients();
            }
        } else {
            if (openProgress > 0.0f) {
                // Suono all'inizio della chiusura
                if (openProgress == 1.0f && !world.isRemote) {
                    world.playSound(null, pos, ModSounds.DOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                openProgress = Math.max(0.0f, openProgress - ANIM_SPEED);
                markDirty();
                if (!world.isRemote) syncToClients();
            }
        }
    }

    // ─── Accesso pubblico ─────────────────────────────────────────────────────

    public void setPowered(boolean val) {
        if (powered == val) return;
        powered = val;
        markDirty();
        if (!world.isRemote) syncToClients();
    }

    public boolean isPowered() { return powered; }

    /** Progresso attuale (0=chiuso, 1=aperto), usato per collisioni. */
    public float getOpenProgress() { return openProgress; }

    /** Progresso interpolato per il rendering (smooth animation). */
    public float getOpenProgress(float partialTicks) {
        return prevOpenProgress + (openProgress - prevOpenProgress) * partialTicks;
    }

    // ─── Sync server → client ─────────────────────────────────────────────────

    private void syncToClients() {
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    @Nullable
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

    // ─── NBT ─────────────────────────────────────────────────────────────────

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("powered",      powered);
        compound.setFloat("openProgress",   openProgress);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        powered          = compound.getBoolean("powered");
        openProgress     = compound.getFloat("openProgress");
        prevOpenProgress = openProgress;
    }

    // ─── Render bounding box (abbastanza grande per tutta la struttura 4×3) ───

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        // Conservativo: copre tutte le orientazioni
        double x = pos.getX(), y = pos.getY(), z = pos.getZ();
        return new AxisAlignedBB(x - 3, y, z - 3, x + 7, y + 4, z + 7);
    }
}
