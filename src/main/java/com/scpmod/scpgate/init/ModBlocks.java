package com.scpmod.scpgate.init;

import com.scpmod.scpgate.SCPGateMod;
import com.scpmod.scpgate.blocks.BlockSCPGate;
import com.scpmod.scpgate.blocks.BlockSCPGateFrame;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ModBlocks {

    public static BlockSCPGate     SCP_GATE;
    public static BlockSCPGateFrame SCP_GATE_FRAME;

    public static void register() {
        SCP_GATE       = registerBlock(new BlockSCPGate(),      "scp_gate");
        SCP_GATE_FRAME = registerFrameBlock(new BlockSCPGateFrame(), "scp_gate_frame");
    }

    private static <T extends Block> T registerBlock(T block, String name) {
        block.setRegistryName(SCPGateMod.MODID, name);
        block.setUnlocalizedName(SCPGateMod.MODID + "." + name);
        ForgeRegistries.BLOCKS.register(block);

        ItemBlock item = new ItemBlock(block);
        item.setRegistryName(block.getRegistryName());
        ForgeRegistries.ITEMS.register(item);
        return block;
    }

    /** Frame block: no item in-game (players cannot pick it up). */
    private static <T extends Block> T registerFrameBlock(T block, String name) {
        block.setRegistryName(SCPGateMod.MODID, name);
        block.setUnlocalizedName(SCPGateMod.MODID + "." + name);
        ForgeRegistries.BLOCKS.register(block);
        // deliberately no ItemBlock
        return block;
    }
}
