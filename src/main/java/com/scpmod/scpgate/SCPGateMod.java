package com.scpmod.scpgate;

import com.scpmod.scpgate.init.ModBlocks;
import com.scpmod.scpgate.init.ModSounds;
import com.scpmod.scpgate.proxy.CommonProxy;
import com.scpmod.scpgate.tileentity.TileEntitySCPGate;
import com.scpmod.scpgate.tileentity.TileEntitySCPGateFrame;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = SCPGateMod.MODID, name = SCPGateMod.NAME, version = SCPGateMod.VERSION,
     acceptedMinecraftVersions = "[1.12,1.13)")
public class SCPGateMod {

    public static final String MODID = "scpgate";
    public static final String NAME  = "SCP Gate Mod";
    public static final String VERSION = "1.0.0";

    @SidedProxy(
        clientSide = "com.scpmod.scpgate.proxy.ClientProxy",
        serverSide = "com.scpmod.scpgate.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.Instance
    public static SCPGateMod INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModSounds.register();
        ModBlocks.register();
        GameRegistry.registerTileEntity(TileEntitySCPGate.class,  MODID + ":scp_gate");
        GameRegistry.registerTileEntity(TileEntitySCPGateFrame.class, MODID + ":scp_gate_frame");
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
}
