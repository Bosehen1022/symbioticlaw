package com.symbioticlaw.registry;

import com.symbioticlaw.Symbioticlaw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Symbioticlaw.MODID);

    public static final RegistryObject<SoundEvent> CORE_HUM_LOOP = registerSoundEvent("core_hum_loop");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Symbioticlaw.MODID, name)));
    }
}
