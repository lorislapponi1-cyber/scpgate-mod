package com.scpmod.scpgate.init;

import com.scpmod.scpgate.SCPGateMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ModSounds {

    public static SoundEvent DOOR_OPEN;
    public static SoundEvent DOOR_CLOSE;

    public static void register() {
        DOOR_OPEN  = registerSound("door.open");
        DOOR_CLOSE = registerSound("door.close");
    }

    private static SoundEvent registerSound(String name) {
        ResourceLocation rl = new ResourceLocation(SCPGateMod.MODID, name);
        SoundEvent ev = new SoundEvent(rl);
        ev.setRegistryName(rl);
        ForgeRegistries.SOUND_EVENTS.register(ev);
        return ev;
    }
}
