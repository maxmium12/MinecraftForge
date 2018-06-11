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

package net.minecraftforge.fml.loading.moddiscovery;

import com.electronwill.nightconfig.core.path.PathConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.minecraftforge.fml.Logging.LOADING;
import static net.minecraftforge.fml.Logging.fmlLog;

public class ModFileParser {
    public static IModFileInfo readModList(final ModFile modFile) {
        fmlLog.debug(LOADING,"Parsing mod file candidate {}", modFile.getFilePath());
        final Path modsjson = modFile.getLocator().findPath(modFile, "META-INF", "mods.toml");
        if (!Files.exists(modsjson)) {
            fmlLog.warn(LOADING, "Mod file {} is missing mods.toml file", modFile);
            return null;
        }
        return loadModFile(modFile, modsjson);
    }

    public static IModFileInfo loadModFile(final ModFile file, final Path modsjson)
    {
        final PathConfig pathConfig = PathConfig.builder(modsjson).build();
        pathConfig.load();
        pathConfig.close();
        return new ModFileInfo(file, pathConfig);
    }

    protected static List<CoreModFile> getCoreMods(final ModFile modFile) {
        Map<String,String> coreModPaths;
        try {
            final Path coremodsjson = modFile.getLocator().findPath(modFile, "META-INF", "coremods.json");
            if (!Files.exists(coremodsjson)) {
                return Collections.emptyList();
            }
            final Type type = new TypeToken<Map<String, String>>() {}.getType();
            final Gson gson = new Gson();
            coreModPaths = gson.fromJson(Files.newBufferedReader(coremodsjson), type);
        } catch (IOException e) {
            fmlLog.debug(LOADING,"Failed to read coremod list coremods.json", e);
            return Collections.emptyList();
        }

        return coreModPaths.entrySet().stream().
                peek(e-> fmlLog.debug(LOADING,"Found coremod {} with Javascript path {}", e.getKey(), e.getValue())).
                map(e -> new CoreModFile(e.getKey(), modFile.getLocator().findPath(modFile, e.getValue()),modFile)).
                collect(Collectors.toList());
    }
}