package com.scpmod.scpgate.proxy;

import com.scpmod.scpgate.SCPGateMod;
import com.scpmod.scpgate.init.ModBlocks;
import com.scpmod.scpgate.renderer.TESRSCPGate;
import com.scpmod.scpgate.tileentity.TileEntitySCPGate;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        // Registra il modello dell'item per il blocco master
        Item itemGate = Item.getItemFromBlock(ModBlocks.SCP_GATE);
        ModelLoader.setCustomModelResourceLocation(
            itemGate, 0,
            new ModelResourceLocation(SCPGateMod.MODID + ":scp_gate", "inventory")
        );

        // Il frame block non ha item, non serve registrare il modello item.
        // Mappa tutti gli stati del frame allo stesso model placeholder.
        ModelLoader.setCustomStateMapper(ModBlocks.SCP_GATE_FRAME,
            block -> java.util.Collections.emptyMap());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        // Registra il TESR per il blocco master
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySCPGate.class, new TESRSCPGate());
    }
}
