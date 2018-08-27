/*
 * Minecraft Forge
 * Copyright (c) 2018.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.common.collect.Multimap;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.RegistryManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * @author cpw
 *
 */
public final class FMLWorldPersistenceHook implements WorldPersistenceHooks.WorldPersistenceHook
{

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker WORLDPERSISTENCE = MarkerManager.getMarker("WP");

    @Override
    public String getModId()
    {
        return "fml";
    }

    @Override
    public NBTTagCompound getDataForWriting(SaveHandler handler, WorldInfo info)
    {
        NBTTagCompound fmlData = new NBTTagCompound();
        NBTTagList modList = new NBTTagList();
        ModList.get().getMods().forEach(mi->
        {
            final NBTTagCompound mod = new NBTTagCompound();
            mod.setString("ModId", mi.getModId());
            mod.setString("ModVersion", mi.getVersion().getVersionString());
            modList.add(mod);
        });
        fmlData.setTag("LoadingModList", modList);

        NBTTagCompound registries = new NBTTagCompound();
        fmlData.setTag("Registries", registries);
        LOGGER.debug(WORLDPERSISTENCE,"Gathering id map for writing to world save {}", info.getWorldName());

        for (Map.Entry<ResourceLocation, ForgeRegistry.Snapshot> e : RegistryManager.ACTIVE.takeSnapshot(true).entrySet())
        {
            NBTTagCompound data = new NBTTagCompound();
            registries.setTag(e.getKey().toString(), data);

            NBTTagList ids = new NBTTagList();
            for (Entry<ResourceLocation, Integer> item : e.getValue().ids.entrySet())
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("K", item.getKey().toString());
                tag.setInteger("V", item.getValue());
                ids.add(tag);
            }
            data.setTag("ids", ids);

            NBTTagList aliases = new NBTTagList();
            for (Entry<ResourceLocation, ResourceLocation> entry : e.getValue().aliases.entrySet())
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("K", entry.getKey().toString());
                tag.setString("V", entry.getValue().toString());
                aliases.add(tag);
            }
            data.setTag("aliases", aliases);

            NBTTagList overrides = new NBTTagList();
            for (Entry<ResourceLocation, String> entry : e.getValue().overrides.entrySet())
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("K", entry.getKey().toString());
                tag.setString("V", entry.getValue().toString());
                aliases.add(tag);
            }
            data.setTag("overrides", overrides);

            int[] blocked = new int[e.getValue().blocked.size()];
            int idx = 0;
            for (Integer i : e.getValue().blocked)
            {
                blocked[idx++] = i;
            }
            data.setIntArray("blocked", blocked);
            NBTTagList dummied = new NBTTagList();
            for (ResourceLocation entry : e.getValue().dummied)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("K", entry.toString());
                dummied.add(tag);
            }
            data.setTag("dummied", dummied);
        }
        return fmlData;
    }

    @Override
    public void readData(SaveHandler handler, WorldInfo info, NBTTagCompound tag)
    {
        if (tag.hasKey("LoadingModList"))
        {
            NBTTagList modList = tag.getTagList("LoadingModList", (byte)10);
            for (int i = 0; i < modList.size(); i++)
            {
                NBTTagCompound mod = modList.getCompoundTagAt(i);
                String modId = mod.getString("ModId");
                String modVersion = mod.getString("ModVersion");
                Optional<? extends ModContainer> container = ModList.get().getModContainerById(modId);
                if (!container.isPresent())
                {
                    LOGGER.error(WORLDPERSISTENCE,"This world was saved with mod {} which appears to be missing, things may not work well", modId);
                    continue;
                }
                if (!modVersion.equals(container.get().getModInfo().getVersion().getVersionString()))
                {
                    LOGGER.info(WORLDPERSISTENCE,"This world was saved with mod {} version {} and it is now at version {}, things may not work well", modId, modVersion, container.get().getModInfo().getVersion().getVersionString());
                }
            }
        }

        Multimap<ResourceLocation, ResourceLocation> failedElements = null;

        if (tag.hasKey("ModItemData") || tag.hasKey("ItemData")) // Pre 1.7
        {
            StartupQuery.notify("This save predates 1.7.10, it can no longer be loaded here. Please load in 1.7.10 or 1.8 first");
            StartupQuery.abort();
        }
        else if (tag.hasKey("Registries")) // 1.8, genericed out the 'registries' list
        {
            Map<ResourceLocation, ForgeRegistry.Snapshot> snapshot = new HashMap<>();
            NBTTagCompound regs = tag.getCompoundTag("Registries");
            for (String key : regs.getKeySet())
            {
                ForgeRegistry.Snapshot entry = new ForgeRegistry.Snapshot();
                NBTTagCompound ent = regs.getCompoundTag(key);
                snapshot.put(new ResourceLocation(key), entry);

                NBTTagList list = ent.getTagList("ids", 10);
                for (int x = 0; x < list.size(); x++)
                {
                    NBTTagCompound e = list.getCompoundTagAt(x);
                    entry.ids.put(new ResourceLocation(e.getString("K")), e.getInteger("V"));
                }

                list = ent.getTagList("aliases", 10);
                for (int x = 0; x < list.size(); x++)
                {
                    NBTTagCompound e = list.getCompoundTagAt(x);
                    entry.aliases.put(new ResourceLocation(e.getString("K")), new ResourceLocation(e.getString("V")));
                }

                int[] blocked = regs.getCompoundTag(key).getIntArray("blocked");
                for (int i : blocked)
                {
                    entry.blocked.add(i);
                }

                if (regs.getCompoundTag(key).hasKey("dummied")) // Added in 1.8.9 dev, some worlds may not have it.
                {
                    list = regs.getCompoundTag(key).getTagList("dummied",10);
                    for (int x = 0; x < list.size(); x++)
                    {
                        NBTTagCompound e = list.getCompoundTagAt(x);
                        entry.dummied.add(new ResourceLocation(e.getString("K")));
                    }
                }
            }
            failedElements = GameData.injectSnapshot(snapshot, true, true);
        }

        if (failedElements != null && !failedElements.isEmpty())
        {
            StringBuilder buf = new StringBuilder();
            buf.append("Forge Mod Loader could not load this save.\n\n")
               .append("There are ").append(failedElements.size()).append(" unassigned registry entries in this save.\n")
               .append("You will not be able to load until they are present again.\n\n");

            failedElements.asMap().forEach((name, entries) ->
            {
                buf.append("Missing ").append(name).append(":\n");
                entries.forEach(rl -> buf.append("    ").append(rl).append("\n"));
            });

            StartupQuery.notify(buf.toString());
            StartupQuery.abort();
        }
    }
}
