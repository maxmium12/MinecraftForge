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

package net.minecraftforge.fml.loading;

import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.fml.loading.moddiscovery.ScanResult;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraftforge.fml.Logging.SCAN;
import static net.minecraftforge.fml.Logging.fmlLog;

public class FMLJavaModLanguageProvider implements IModLanguageProvider
{
    private static class FMLModTarget implements IModLanguageProvider.IModLanguageLoader {
        private final String className;
        private final String modId;

        private FMLModTarget(String className, String modId)
        {
            this.className = className;
            this.modId = modId;
        }

        public String getModId()
        {
            return modId;
        }

        @Override
        public ModContainer loadMod(final ModInfo info, final ClassLoader modClassLoader)
        {
            return new FMLModContainer(info, className, modClassLoader);
        }
    }

    public static final Type MODANNOTATION = Type.getType("Lnet/minecraftforge/fml/common/Mod;");

    @Override
    public String name()
    {
        return "javafml";
    }

    @Override
    public Consumer<ScanResult> getFileVisitor() {
        return scanResult -> {
            final Map<String, FMLModTarget> modTargetMap = scanResult.getAnnotations().stream()
                    .filter(ad -> ad.getAnnotationType().equals(MODANNOTATION))
                    .peek(ad -> fmlLog.debug(SCAN, "Found @Mod class {} with id {}", ad.getClassType().getClassName(), ad.getAnnotationData().get("modid")))
                    .map(ad -> new FMLModTarget(ad.getClassType().getClassName(), (String)ad.getAnnotationData().get("modid")))
                    .collect(Collectors.toMap(FMLModTarget::getModId, Function.identity()));
            scanResult.addLanguageLoader(modTargetMap);
        };
    }
}
