package net.minecraftforge.registries;

import net.minecraft.util.ResourceLocation;

public interface IForgeRegistryModifiable<V extends ForgeRegistryEntry<V>> extends IForgeRegistry<V>
{
    void clear();
    V remove(ResourceLocation key);
    boolean isLocked();
}
